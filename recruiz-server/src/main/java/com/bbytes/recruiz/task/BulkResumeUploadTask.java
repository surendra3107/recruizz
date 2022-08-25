package com.bbytes.recruiz.task;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;

import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.service.CandidateResumeUploadItemService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.ThreadTenantSecurityUtil;

import lombok.Data;

@Data
public class BulkResumeUploadTask implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(BulkResumeUploadTask.class);

	private CandidateService candidateService;

	private S3Client s3Client;

	private S3DownloadClient s3DownloadClient;

	private CandidateResumeUploadItemService candidateResumeUploadItemService;

	private UploadFileService uploadFileService;

	private PageableService pageableService;

	private FileService fileService;

	private String candidateFolderPath;

	private Authentication auth;

	private String tenantId;
	
	private String batchId;

	/**
	 * If queue is true then we queue the parser and conversion as it might be
	 * running in multiple threads
	 */
	private boolean queue;

	private Page<CandidateResumeUploadItem> candidateResumeUploadItems;

	public BulkResumeUploadTask(Page<CandidateResumeUploadItem> candidateResumeUploadItems, String tenantId,
			Authentication auth, boolean queue) {
		this.candidateResumeUploadItems = candidateResumeUploadItems;
		this.tenantId = tenantId;
		this.auth = auth;
		this.queue = queue;
	}

	public BulkResumeUploadTask(Page<CandidateResumeUploadItem> candidateResumeUploadItems, String tenantId,
			Authentication auth, boolean queue, String batchId) {
		this.candidateResumeUploadItems = candidateResumeUploadItems;
		this.tenantId = tenantId;
		this.auth = auth;
		this.queue = queue;
		this.batchId = batchId;
	}
	
	/**
	 * It will add the file to candidate and delete the passed files as it is a temp
	 * file passed to this method
	 * 
	 */
	@Override
	public void run() {

		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);

		processPendingResumeItems(candidateResumeUploadItems);

	}

	private void processPendingResumeItems(Page<CandidateResumeUploadItem> candidateResumeUploadItems) {
		if (candidateResumeUploadItems == null)
			return;

		// loop thru each file item that are pending in db and process the
		// files
		for (CandidateResumeUploadItem candidateResumeUploadDBItem : candidateResumeUploadItems) {

			candidateResumeUploadDBItem = candidateResumeUploadItemService.findOne(candidateResumeUploadDBItem.getId());
			if (ResumeUploadFileStatus.SUCCESS.equals(candidateResumeUploadDBItem.getStatus())
					|| ResumeUploadFileStatus.FAILED.equals(candidateResumeUploadDBItem.getStatus())) {
				continue;
			}

			candidateResumeUploadDBItem.setStatus(ResumeUploadFileStatus.PROCESSING.toString());
			candidateResumeUploadDBItem = candidateResumeUploadItemService.save(candidateResumeUploadDBItem);

			File resumeFileFromTempLocation = null;
			try {
				// if file is on local then get it from local disk
				if (null != candidateResumeUploadDBItem.getFileSystem() && GlobalConstants.BULK_UPLOAD_FILE_SYSTEM_LOCAL
						.equalsIgnoreCase(candidateResumeUploadDBItem.getFileSystem())) {
					resumeFileFromTempLocation = new File(candidateResumeUploadDBItem.getFilePath());

					if (!resumeFileFromTempLocation.exists()) {
						logger.warn("\n\n******************File not Found " + candidateResumeUploadDBItem.getFilePath()
								+ " disc : " + candidateResumeUploadDBItem.getFileSystem() + "\n\n");
						// making entry as success expecting file will be always
						// present for first time
						candidateResumeUploadDBItem.setStatus(ResumeUploadFileStatus.SUCCESS.toString());
						candidateResumeUploadItemService.save(candidateResumeUploadDBItem);
						continue;
					}
				} else if (null == candidateResumeUploadDBItem.getFileSystem()
						|| (null != candidateResumeUploadDBItem.getFileSystem()
								&& GlobalConstants.BULK_UPLOAD_FILE_SYSTEM_S3
										.equalsIgnoreCase(candidateResumeUploadDBItem.getFileSystem()))) {
					// if the file path starts with http means s3 or file system
					// is fo s3 then get it froom s3
					InputStream s3ResumeFileStream = s3DownloadClient.downloadAsStream(
							candidateResumeUploadDBItem.getFilePath(), fileService.getBulkUploadBucketbucket());
					String folderInTemp = System.currentTimeMillis() + "";
					resumeFileFromTempLocation = FileUtils.writeToFile(folderInTemp,
							candidateResumeUploadDBItem.getFileName(), s3ResumeFileStream);
				}

			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}

			if (resumeFileFromTempLocation == null || !resumeFileFromTempLocation.exists()) {
				candidateResumeUploadDBItem = candidateResumeUploadItemService
						.findOne(candidateResumeUploadDBItem.getId());
				if (candidateResumeUploadDBItem != null) {
					candidateResumeUploadDBItem.setFailedReason("File not found in drive or s3");
					candidateResumeUploadItemService.save(candidateResumeUploadDBItem);
				}
				continue;
			}

			try {

				Candidate candidate = null;
				logger.error("###########call  queueParseResume() method from  BulkResumeUploadTask.java #############");	
				if (queue) {
					candidate = candidateService.addResumeFileAsCandidateToQueue(resumeFileFromTempLocation);
				} else {
					candidate = candidateService.addResumeFileAsCandidate(resumeFileFromTempLocation);
				}

				// upload resume to server
				candidate = candidateService.candidateBulkParserSaveData(candidate, batchId);

				// if candidate object is created in database then do rest
				// of
				// the work of adding candidate files
				if (candidate != null && candidate.getCid() > 0) {
					// this will add file to folder called resume on server
					uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");

					if (!resumeFileFromTempLocation.exists()) {
						// file does not exists then escaping the next process
						candidateResumeUploadDBItem.setStatus(ResumeUploadFileStatus.SUCCESS.toString());
						candidateResumeUploadItemService.save(candidateResumeUploadDBItem);
						continue;
					}
					String originalResume = uploadFileService.uploadFileToLocalServer(resumeFileFromTempLocation,
							StringUtils.cleanFileName(resumeFileFromTempLocation.getName()), "resume",
							candidate.getCid() + "");

					String convertedResume = "";
					if (queue) {
						convertedResume = fileService.queueFileConvert(originalResume);
					} else {
						convertedResume = fileService.convert(originalResume);
					}

					if (convertedResume != null) {
						candidateService.updateCandidateResume(candidate, convertedResume);
					}

					// add to resume docs
					String resumePath = uploadFileService.uploadFileToLocalServer(resumeFileFromTempLocation,
							StringUtils.cleanFileName(resumeFileFromTempLocation.getName()),
							FileType.Original_Resume.getDisplayName(), candidate.getCid() + "");

					// converted file upload.
					if (resumePath != null) {
						candidateService.uploadCandidateFiles(resumePath, resumeFileFromTempLocation.getName(),
								FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "",
								convertedResume);
					}

					// masking resume
					try {
						long cid = candidate.getCid();
						if (cid == 0) {
							cid = candidateService.getCandidateByEmail(candidate.getEmail()).getCid();
						}
						Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
						if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
							candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
						}
					} catch (Throwable ex) {
						logger.warn("*******Failed to mask resume*********", ex);
					}

					candidateResumeUploadDBItem.setStatus(ResumeUploadFileStatus.SUCCESS.toString());
				}
			} catch (Throwable ex) {
				logger.warn(ex.getMessage(), ex);
				if (ex instanceof ObjectOptimisticLockingFailureException || ex instanceof LockAcquisitionException) {
					candidateResumeUploadDBItem.setFailedReason("File update process failure");
				}else {
					candidateResumeUploadDBItem.setFailedReason(ex.getMessage());
				}
				String path = fileService.copyFileToFailedTempForBulkUpload(resumeFileFromTempLocation);
				candidateResumeUploadDBItem.setFilePath(path);
			} finally {
				// deleting the temp resume file if it exists
				if (resumeFileFromTempLocation != null && resumeFileFromTempLocation.exists()) {
					resumeFileFromTempLocation.delete();
				}

				// in case if the status is still pending then make sure it
				// is
				// updated to failed
				if (candidateResumeUploadDBItem.getStatus().equalsIgnoreCase(ResumeUploadFileStatus.PENDING.toString())
						|| candidateResumeUploadDBItem.getStatus()
								.equalsIgnoreCase(ResumeUploadFileStatus.PROCESSING.toString())) {
					candidateResumeUploadDBItem.setStatus(ResumeUploadFileStatus.FAILED.toString());

					// in case of file processing failed move it to recruiz tmp
					// folder so that it will be available for downloading
					String path = fileService.copyFileToFailedTempForBulkUpload(resumeFileFromTempLocation);
					candidateResumeUploadDBItem.setFilePath(path);
				}

				// deleting the temp resume file if it exists
				if (resumeFileFromTempLocation != null && resumeFileFromTempLocation.exists()) {
					resumeFileFromTempLocation.delete();
				}

			}

			candidateResumeUploadItemService.save(candidateResumeUploadDBItem);
		}
	}
}