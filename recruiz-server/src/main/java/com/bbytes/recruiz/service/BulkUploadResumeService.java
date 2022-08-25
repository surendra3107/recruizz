package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.task.BulkResumeUploadTask;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.NetworkUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ThreadTenantSecurityUtil;
import com.bbytes.recruiz.utils.ThreadUtils;
import com.google.common.collect.Lists;

import net.lingala.zip4j.exception.ZipException;

@Service
public class BulkUploadResumeService {

	private static final Logger logger = LoggerFactory.getLogger(BulkUploadResumeService.class);

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private CandidateResumeBulkUploadBatchService candidateResumeBulkUploadBatchService;

	@Autowired
	private CandidateResumeUploadItemService candidateResumeUploadItemService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private FileService fileService;

	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${rchilli.v7.service.url}")
	private String resumeParserServerUrl;

	private int retryLimit = 30;

	/**
	 * Prepare the data(resume files) to db and then call resume bulk upload
	 * service in async mode
	 * 
	 * @param tenantId
	 * @param auth
	 * @param batchId
	 * @param fileList
	 * @param source 
	 * @param folderId 
	 * @throws Exception
	 */
	//@Async
	public void resumeBulkUploadAsync(String tenantId, Authentication auth, String batchId, List<File> fileList, String source, String folderId)
			throws Exception {

		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);

		fileList = processZipOrRarFiles(fileList);

		if (fileList == null)
			return;

		if (batchId == null)
			batchId = System.currentTimeMillis() + "";

		if (fileList.size() <=10 ) {
			
			for (File file : fileList) {
				processSingleResume(tenantId, auth, batchId, file,source, folderId);		
			}
		
		} else {
			updateCandidateBatchUploadToDB(batchId, fileList, source, folderId);
		}

	}

	/**
	 * If only one fiel is uploaded then we exec this method as we dont for
	 * thread executor pool approach
	 * 
	 * @param tenantId
	 * @param auth
	 * @param batchId
	 * @param resumeFileFromTempFolder
	 * @param source 
	 * @param folderId 
	 * @throws IOException
	 */
	private void processSingleResume(String tenantId, Authentication auth, String batchId,
			File resumeFileFromTempFolder, String source, String folderId) throws IOException {

		CandidateResumeUploadItem candidateResumeUploadItem = updateSingleCandidateResumeFileUploadToDB(batchId,
				resumeFileFromTempFolder,source, folderId);
		List<CandidateResumeUploadItem> candidateResumeUploadItems = new ArrayList<CandidateResumeUploadItem>();
		candidateResumeUploadItems.add(candidateResumeUploadItem);

		BulkResumeUploadTask bulkResumeUploadTask = new BulkResumeUploadTask(
				new PageImpl<CandidateResumeUploadItem>(candidateResumeUploadItems), tenantId, auth, false, batchId);
		
		bulkResumeUploadTask.setCandidateFolderPath(candidateFolderPath);
		bulkResumeUploadTask.setCandidateResumeUploadItemService(candidateResumeUploadItemService);
		bulkResumeUploadTask.setCandidateService(candidateService);
		bulkResumeUploadTask.setPageableService(pageableService);
		bulkResumeUploadTask.setFileService(fileService);
		bulkResumeUploadTask.setS3Client(s3Client);
		bulkResumeUploadTask.setS3DownloadClient(s3DownloadClient);
		bulkResumeUploadTask.setUploadFileService(uploadFileService);
		bulkResumeUploadTask.run();

	}

	@Async
	public void resumeBulkUploadAsync(String tenantId, Authentication auth,
			List<CandidateResumeUploadItem> candidateResumeUploadItems, String batchId) {
		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);
		resumeBulkUpload(tenantId, auth, candidateResumeUploadItems,batchId);
	}

	private void resumeBulkUpload(String tenantId, Authentication auth,
			List<CandidateResumeUploadItem> candidateResumeUploadItems, String batchId) {

		if (candidateResumeUploadItems == null || candidateResumeUploadItems.isEmpty())
			return;

		// if the file is zip or rar it may contain more than one file
		// then
		// it comes here .
		// find total pages based on pending files to be processed
		int totalThreadCount = 4;
		int queryRequestPageSize = candidateResumeUploadItems.size() / totalThreadCount;

		// the request page size cannot be 0
		if (queryRequestPageSize == 0)
			queryRequestPageSize = 1;

		List<List<CandidateResumeUploadItem>> candidateResumeUploadItemsChunks = Lists
				.partition(candidateResumeUploadItems, queryRequestPageSize);

		// this can be more than the totalThreadCount defined before as the
		// number is not a round number then the totalThreadCount goes up by one
		totalThreadCount = candidateResumeUploadItemsChunks.size();

		ExecutorService executor = ThreadUtils.newFixedThreadPool("thread-group-resume-bulk-" + tenantId,
				totalThreadCount);

		// this block is for long running process
		// set the state to running
		resumeBulkTenantState.setResumeBulkTaskRunningNow(tenantId);

		try {
			for (int threadLoopIndex = 0; threadLoopIndex < totalThreadCount; threadLoopIndex++) {

				
				if(!NetworkUtils.pingURL(resumeParserServerUrl))
					continue;
				
				/*int retry = 1;
				// dont proceed if parser server is down , try for 30
				// times
				// then quit
				while (!NetworkUtils.pingURL(resumeParserServerUrl) && retry < retryLimit) {
					try {
						logger.info("Parser URL   " + resumeParserServerUrl);
						logger.info("Parser not reachable so waiting, retry  " + retry);
						Thread.sleep(5000);
						retry++;
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}

				}

				if (retry >= retryLimit) {
					continue;
				}*/

				Page<CandidateResumeUploadItem> candidateResumeUploadItemChunk = new PageImpl<CandidateResumeUploadItem>(
						candidateResumeUploadItemsChunks.get(threadLoopIndex));

				BulkResumeUploadTask bulkResumeUploadTask = new BulkResumeUploadTask(candidateResumeUploadItemChunk,
						tenantId, auth, true, batchId );
				bulkResumeUploadTask.setCandidateFolderPath(candidateFolderPath);
				bulkResumeUploadTask.setCandidateResumeUploadItemService(candidateResumeUploadItemService);
				bulkResumeUploadTask.setCandidateService(candidateService);
				bulkResumeUploadTask.setPageableService(pageableService);
				bulkResumeUploadTask.setFileService(fileService);
				bulkResumeUploadTask.setS3Client(s3Client);
				bulkResumeUploadTask.setS3DownloadClient(s3DownloadClient);
				bulkResumeUploadTask.setUploadFileService(uploadFileService);

				// sleep for 500 millisecs before be bombard parser server with
				// multi thread request
				Thread.sleep(500);

				executor.execute(bulkResumeUploadTask);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				executor.shutdown();
				try {
					executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			} finally {
				// set the state to done after coming to this finally block
				resumeBulkTenantState.setResumeBulkTaskDone(tenantId);
			}

		}

		logger.info("BULK RESUME UPLOAD JOBS COMPLETED !!!!!");
	}

	/**
	 * Create a batch update object with give batch id and files as pending
	 * state and then save to db , then it is passed to a thread to process
	 * these pending files
	 * 
	 * We dont start the bg job if the file is being uploaded to s3
	 * 
	 * @param batchId
	 * @param fileList
	 * @param source 
	 * @param folderId 
	 * @return
	 * @throws IOException
	 */
	private CandidateResumeBulkUploadBatch updateCandidateBatchUploadToDB(String batchId, List<File> fileList, String source, String folderId) {

		if (batchId == null || fileList == null || fileList.isEmpty() || source == null)
			return null;
		
		CandidateResumeBulkUploadBatch candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService
				.findByBatchId(batchId);

		if (candidateResumeBulkUploadBatch == null) {
			candidateResumeBulkUploadBatch = new CandidateResumeBulkUploadBatch();
			candidateResumeBulkUploadBatch.setBatchId(batchId);
			candidateResumeBulkUploadBatch.setSource(source);
			if(folderId!=null && !folderId.trim().equalsIgnoreCase("") && !folderId.trim().isEmpty()){
				candidateResumeBulkUploadBatch.setFolderId(folderId);
			}
			
			candidateResumeBulkUploadBatch.setOwner(userService.getLoggedInUserEmail());
		}

		candidateResumeBulkUploadBatch.setStatus(ResumeBulkBatchUploadStatus.UPLOADING.toString());
		candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);
		List<CandidateResumeUploadItem> candidateResumeUploadItemList = new ArrayList<>();
		List<List<File>> fileListChunks = Lists.partition(fileList, 20);

		for (List<File> fileListChunk : fileListChunks) {

			for (File resumeFile : fileListChunk) {
				try {
					CandidateResumeUploadItem candidateResumeUploadItem = candidateResumeItemFromResumeFile(batchId,
							resumeFile);
					//logger.error("************** Going to upload " + resumeFile.getName() + " @" + (new Date()).toString());
					candidateResumeUploadItem = candidateResumeBulkUploadBatch
							.addCandidateResumeUploadItem(candidateResumeUploadItem);
					//logger.error("************** upload Done" + resumeFile.getName() + " @" + (new Date()).toString());
					//candidateResumeUploadItem = candidateResumeUploadItemService.save(candidateResumeUploadItem);
					//logger.error("************** saved" + resumeFile.getName() + " @" + (new Date()).toString());
					candidateResumeUploadItemList.add(candidateResumeUploadItem);
					// delete the file from tmp folder
					fileService.deleteFileInNewThread(resumeFile);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		}
		candidateResumeUploadItemService.save(candidateResumeUploadItemList);
		candidateResumeBulkUploadBatch.setStatus(ResumeBulkBatchUploadStatus.SCHEDULED.toString());
		candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);
		

		return candidateResumeBulkUploadBatch;

	}

	/**
	 * Create a batch update object with give batch id and files as pending
	 * state and then save to db , then it is passed to a thread to process
	 * these pending files
	 * 
	 * @param batchId
	 * @param source 
	 * @param folderId 
	 * @param fileList
	 * @return
	 * @throws IOException
	 */
	private CandidateResumeUploadItem updateSingleCandidateResumeFileUploadToDB(String batchId, File resumeFile, String source, String folderId)
			throws IOException {
		if (resumeFile == null || !resumeFile.exists())
			return null;

		CandidateResumeBulkUploadBatch candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService
				.findByBatchId(batchId);
		if (candidateResumeBulkUploadBatch == null) {
			candidateResumeBulkUploadBatch = new CandidateResumeBulkUploadBatch();
			candidateResumeBulkUploadBatch.setBatchId(batchId);
			candidateResumeBulkUploadBatch.setSource(source);
			
			if(folderId!=null && !folderId.trim().equalsIgnoreCase("") && !folderId.trim().isEmpty()){
				candidateResumeBulkUploadBatch.setFolderId(folderId);
			}
			
			candidateResumeBulkUploadBatch.setOwner(userService.getLoggedInUserEmail());
			candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);
		}

//		logger.error("************** Going to upload " + resumeFile.getName() + " @" + (new Date()).toString());
		CandidateResumeUploadItem candidateResumeUploadItem = candidateResumeItemFromResumeFile(batchId, resumeFile);
//		logger.error("************** upload Done" + resumeFile.getName() + " @" + (new Date()).toString());
		
		candidateResumeUploadItem = candidateResumeBulkUploadBatch
				.addCandidateResumeUploadItem(candidateResumeUploadItem);
		candidateResumeUploadItem = candidateResumeUploadItemService.save(candidateResumeUploadItem);

		candidateResumeBulkUploadBatch.setStatus(ResumeBulkBatchUploadStatus.IN_PROGRESS.toString());
		candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);

		return candidateResumeUploadItem;
	}

	/**
	 * Convert a resume file to db candidateResumeUploadItem
	 * 
	 * @param batchId
	 * @param resumeFile
	 * @return
	 * @throws IOException
	 */
	private CandidateResumeUploadItem candidateResumeItemFromResumeFile(String batchId, File resumeFile)
			throws IOException {
		CandidateResumeUploadItem candidateResumeUploadItem = new CandidateResumeUploadItem();
		candidateResumeUploadItem.setFileName(resumeFile.getName());

		candidateResumeUploadItem.setStatus(ResumeUploadFileStatus.PENDING.toString());
		double kilobytes = (resumeFile.length() / 1024);
		candidateResumeUploadItem.setFileSize(kilobytes);
		
		try{
			String tempFilePath = fileService.copyFileToRecruizTempForBulkUpload(resumeFile);
			candidateResumeUploadItem.setFilePath(tempFilePath);
			candidateResumeUploadItem.setFileSystem(GlobalConstants.BULK_UPLOAD_FILE_SYSTEM_LOCAL);
		}catch(Exception ex){
			String s3FileName = TenantContextHolder.getTenant() + "/" + batchId + "/" + resumeFile.getName();
			s3Client.upload(resumeFile, s3FileName, fileService.getBulkUploadBucketbucket());
			candidateResumeUploadItem.setFilePath(s3FileName);
			candidateResumeUploadItem.setFileSystem(GlobalConstants.BULK_UPLOAD_FILE_SYSTEM_S3);
		}
		
		return candidateResumeUploadItem;
	}

	/**
	 * If the file list has zip or rar files the unzip and copy all the files
	 * inside the zip and copy to final list
	 * 
	 * @param fileList
	 * @return
	 * @throws ZipException
	 */
	private List<File> processZipOrRarFiles(List<File> fileList) throws ZipException {
		List<File> finalList = new ArrayList<>();

		for (File resumeFile : fileList) {
			if (FileUtils.isZip(resumeFile)) {
				String fileNameWithOutExt = FilenameUtils.removeExtension(resumeFile.getName());
				finalList.addAll(FileUtils.unZip(resumeFile, FileUtils.getTempDirForBulkUpload(fileNameWithOutExt)));
				fileService.deleteFileInNewThread(resumeFile);
			} else if (FileUtils.isRar(resumeFile)) {
				String fileNameWithOutExt = FilenameUtils.removeExtension(resumeFile.getName());
				finalList.addAll(FileUtils.unRar(resumeFile, FileUtils.getTempDirForBulkUpload(fileNameWithOutExt)));
				fileService.deleteFileInNewThread(resumeFile);
			} else {
				finalList.add(resumeFile);
			}
		}
		return finalList;

	}
}
