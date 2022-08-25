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
public class EmailedFileUploadTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(EmailedFileUploadTask.class);

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

    public EmailedFileUploadTask(List<String> tenantIds, boolean queue) {
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

	    if(resumeBulkTenantState.isTenantIdToCheckEmailFecthTaskRunning(tenantId)) {
		continue;
	    }
	    
	    // create auth object here and set it to tenant context holder
	    TenantContextHolder.setTenant(tenantId);
	    
	    resumeBulkTenantState.setTenantIdToCheckEmailFecthTaskRunning(tenantId);

	    List<EmailClientDetails> emailClientDetails = emailClientDetailService.getAllEmailDetails();

	    for (EmailClientDetails emailClientDetail : emailClientDetails) {

		TokenDataHolder tokenDataHolder = tokenAuthenticationProvider
			.getAuthTokenHolderForUser(emailClientDetail.getRecruizEmail(), tenantId);

		MultiTenantAuthenticationToken auth = new MultiTenantAuthenticationToken(tenantId,
			tokenDataHolder.getUser(), tokenDataHolder.getWebMode(), tokenDataHolder.getWebRequestMode(),
			tokenDataHolder.getUserTimeZone(), tokenDataHolder.getUserLocale());

		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);

		/******
		 * Implementation changed so waiting for UI, ImapClient is null
		 * here initialize it before using
		 *****/
		//ImapClient imapClient = null;
		 ImapClient imapClient = new ImapClient();
		 Map<String,List<File>> attachedFiles = imapClient.getAttachedFilesFromEmail(emailClientDetail);
		 if(null != attachedFiles && ! attachedFiles.isEmpty()) {
		     addFilesToDatabase(attachedFiles, emailClientDetail.getEmailId(),emailClientDetail.getRecruizEmail());    
		 }
//		 else {
//		     logger.error("No email attachments for " + emailClientDetail.getEmailId() +" -> " + TenantContextHolder.getTenant());     
//		 }
		 
		// updating last fetched max uid & setting start, end date as null
		emailClientDetailService.save(emailClientDetail);
	    }
	    resumeBulkTenantState.setTenantIdToCheckEmailFecthTaskDone(tenantId);
	}

    }
    
    
    // make file entry to database
    private void addFilesToDatabase(Map<String,List<File>> emailedFiles, String emailId,String recruizEmail) {
	for (Entry<String, List<File>> email : emailedFiles.entrySet()) {
	    for (File file : email.getValue()) {
		EmailedFileItem emailedFile = new EmailedFileItem();
		    emailedFile.setEmailId(emailId);
		    emailedFile.setFileName(file.getName());
		    emailedFile.setFilePath(file.getPath());
		    emailedFile.setFileSystem(GlobalConstants.BULK_UPLOAD_FILE_SYSTEM_LOCAL);
		    emailedFile.setStatus(ResumeBulkBatchUploadStatus.UPLOADING.getDisplayName());
		    emailedFile.setSubject(email.getKey());
		    emailedFile.setRecruizEmailId(recruizEmail);
		    emailedFileItemService.save(emailedFile);
	    }
	    
	}
	
	
	
    }
    
    
    
    

    private void processEmailedResumeFiles(List<File> emailedFiles) {
	if (emailedFiles == null || emailedFiles.isEmpty())
	    return;

	logger.warn("\n\n----------------Processing total :" + emailedFiles.size() + " files for "
		+ TenantContextHolder.getTenant() + "\n\n");

	for (File resumeFileFromTempLocation : emailedFiles) {

	    try {
		logger.warn("\n\n############## processing file " + resumeFileFromTempLocation.getName()
			+ " for tenant : " + TenantContextHolder.getTenant());
		Candidate candidate = null;
		candidate = candidateService.addResumeFileAsCandidate(resumeFileFromTempLocation);

		// upload resume to server
		candidate = candidateService.candidateBulkParserSave(candidate);
		candidate.setActualSource(Source.EmailShare.getDisplayName());
		candidate.setSource(Source.EmailShare.getDisplayName());

		// if candidate object is created in database then do rest
		// of
		// the work of adding candidate files
		if (candidate != null && candidate.getCid() > 0) {
		    // this will add file to folder called resume on server
		    uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");

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
		    } catch (Exception ex) {
			logger.error("\n\n*******Failed to mask resume*********", ex);
		    }

		}
	    } catch (Throwable ex) {
		logger.error(ex.getMessage(), ex);
	    } finally {
		// deleting the temp resume file if it exists
		try {
		    if (resumeFileFromTempLocation != null && resumeFileFromTempLocation.exists()) {
			resumeFileFromTempLocation.delete();
		    }
		} catch (Throwable ex) {
		    logger.warn(ex.getMessage(), ex);
		}
	    }
	}
    }

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