package com.bbytes.recruiz.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.auth.jwt.TokenDataHolder;
import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.domain.EmailedFileItem;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.mail.service.imap.ImapClient;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.EmailAccountDetailService;
import com.bbytes.recruiz.service.EmailedFileItemService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ThreadTenantSecurityUtil;

import lombok.Data;
import net.lingala.zip4j.exception.ZipException;

@Data
public class EmailedFileProcessTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(EmailedFileProcessTask.class);

    private CandidateService candidateService;

    private UploadFileService uploadFileService;

    private S3Client s3Client;

    private S3DownloadClient s3DownloadClient;

    private PageableService pageableService;

    private FileService fileService;

    private EmailedFileItemService emailedFileItemService;

    private String candidateFolderPath;

    private List<String> tenantIds;

    private TokenAuthenticationProvider tokenAuthenticationProvider;

    private EmailAccountDetailService emailClientDetailService;

    private SchedulerTaskTenantState resumeBulkTenantState;

    /**
     * If queue is true then we queue the parser and conversion as it might be
     * running in multiple threads
     */
    private boolean queue;

    public EmailedFileProcessTask(List<String> tenantIds, boolean queue) {
	this.tenantIds = tenantIds;
	this.queue = queue;
    }

    /**
     * It will add the file to candidate and delete the passed files as it is a
     * temp file passed to this method
     * 
     */
    @Override
    public void run() {

	for (String tenantId : tenantIds) {

	    if (resumeBulkTenantState.isTenantIdToCheckEmailFileProcessingTaskRunning(tenantId)) {
//		logger.error("Emailed file is being processed for " + tenantId);
		continue;
	    }

	    // create auth object here and set it to tenant context holder
	    TenantContextHolder.setTenant(tenantId);

	    resumeBulkTenantState.setTenantIdToCheckEmailFileProcessTaskRunning(tenantId);

	    List<EmailClientDetails> emailClientDetails = emailClientDetailService.getAllEmailDetails();

//	    logger.error("Going to process emailed files for " + tenantId + " with total " + emailClientDetails.size()
//		    + " email client");

	    for (EmailClientDetails emailClientDetail : emailClientDetails) {

		TokenDataHolder tokenDataHolder = tokenAuthenticationProvider
			.getAuthTokenHolderForUser(emailClientDetail.getRecruizEmail(), tenantId);

		MultiTenantAuthenticationToken auth = new MultiTenantAuthenticationToken(tenantId,
			tokenDataHolder.getUser(), tokenDataHolder.getWebMode(), tokenDataHolder.getWebRequestMode(),
			tokenDataHolder.getUserTimeZone(), tokenDataHolder.getUserLocale());

		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);

		List<EmailedFileItem> emailedFile = emailedFileItemService.findByStatusAndRecruizEmail(
			ResumeBulkBatchUploadStatus.UPLOADING.getDisplayName(), emailClientDetail.getRecruizEmail());

		if (emailedFile == null || emailedFile.isEmpty()) {
//		    logger.error("No Emailed file found for " + emailClientDetail.getEmailId() + ", Tenant "
//			    + TenantContextHolder.getTenant());
		    continue;
		}
//		logger.error("Going to process " + emailedFile.size() + " files for email "
//			+ emailClientDetail.getEmailId() + " Tenant " + TenantContextHolder.getTenant());
		processEmailedResumeFiles(emailedFile);

	    }
	    resumeBulkTenantState.setTenantIdToCheckEmailFileProcessTaskDone(tenantId);
	}

    }

    private void processEmailedResumeFiles(List<EmailedFileItem> emailedFiles) {
	if (emailedFiles == null || emailedFiles.isEmpty())
	    return;

//	logger.error("\n\n----------------Processing total :" + emailedFiles.size() + " files for "
//		+ TenantContextHolder.getTenant() + "\n\n");

	for (EmailedFileItem resumeFileFromTempLocation : emailedFiles) {
	    File file = null;
	    try {
		file = new File(resumeFileFromTempLocation.getFilePath());
		if (!file.exists()) {
		    resumeFileFromTempLocation.setFailedReason("File is not present on disk");
		    resumeFileFromTempLocation.setStatus(ResumeUploadFileStatus.FAILED.name());
		    continue;
		}

		logger.error("\n\n############## processing file " + file.getName() + " for tenant : "
			+ TenantContextHolder.getTenant());
		logger.error("###########call  queueParseResume() method from  EmailedFileProcessTask.java #############");	
		Candidate candidate = null;
		candidate = candidateService.addResumeFileAsCandidate(file);

		if (null == candidate) {
		    resumeFileFromTempLocation.setFailedReason("Colud get parsed content form file");
		    resumeFileFromTempLocation.setStatus(ResumeUploadFileStatus.FAILED.name());
		    continue;
		}
		
		if(candidate.getKeySkills() == null || candidate.getKeySkills().isEmpty()) {
		    resumeFileFromTempLocation.setFailedReason("No skill set found for candidate");
		    resumeFileFromTempLocation.setStatus(ResumeUploadFileStatus.FAILED.name());
		    continue;
		}

		// upload resume to server
		candidate = candidateService.candidateBulkParserSave(candidate);
		candidate.setActualSource(Source.EmailSync.getDisplayName());
		candidate.setSource(Source.EmailSync.getDisplayName());

		// if candidate object is created in database then do rest
		// of
		// the work of adding candidate files
		if (candidate != null && candidate.getCid() > 0) {
		    resumeFileFromTempLocation.setStatus(ResumeUploadFileStatus.SUCCESS.name());
		    // this will add file to folder called resume on server
		    uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");

		    String originalResume = uploadFileService.uploadFileToLocalServer(file,
			    StringUtils.cleanFileName(file.getName()), "resume", candidate.getCid() + "");

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
		    String resumePath = uploadFileService.uploadFileToLocalServer(file,
			    StringUtils.cleanFileName(file.getName()), FileType.Original_Resume.getDisplayName(),
			    candidate.getCid() + "");

		    // converted file upload.
		    if (resumePath != null) {
			candidateService.uploadCandidateFiles(resumePath, file.getName(),
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
		    } catch (Exception ex) {
			logger.error("\n\n*******Failed to mask resume*********", ex);
		    }

		}
	    } catch (Throwable ex) {
		logger.error(ex.getMessage(), ex);
		resumeFileFromTempLocation.setFailedReason(ex.getMessage());
		resumeFileFromTempLocation.setStatus(ResumeUploadFileStatus.FAILED.name());
	    } finally {
		// deleting the temp resume file if it exists
		try {
		    if (file != null && file.exists()) {
			file.delete();
		    }
		} catch (Throwable ex) {
		    logger.warn(ex.getMessage(), ex);
		}
	    }
	}

	// save all file status here
	emailedFileItemService.save(emailedFiles);

    }
}