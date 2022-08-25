package com.bbytes.recruiz.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.task.EmailedFileProcessTask;
import com.bbytes.recruiz.task.EmailedFileUploadTask;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.NetworkUtils;
import com.bbytes.recruiz.utils.ThreadUtils;
import com.google.common.collect.Lists;

import net.lingala.zip4j.exception.ZipException;

@Service
public class EmailedResumeUploadService {

	private static final Logger logger = LoggerFactory.getLogger(EmailedResumeUploadService.class);

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private FileService fileService;
	
	@Autowired
	private EmailedFileItemService emailedFileItemService;
	
	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;
	
	@Autowired
	private EmailAccountDetailService emailClientDetailService;
	
	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${rchilli.v7.service.url}")
	private String resumeParserServerUrl;

	private int retryLimit = 30;

	@Async
	public void processTenantListToAddEmailedFiles(List<String> tenantList) {
		processEmailsFromTenantList(tenantList);
	}

	private void processEmailsFromTenantList(List<String> tenantList) {

		if (tenantList == null || tenantList.isEmpty())
			return;

		int totalThreadCount = 4;
		int listPartitionSize = tenantList.size() / totalThreadCount;

		// the if the partition size > 0 then allowing it to be partitioned of
		// length 2 (max) i.e: each partition will have max 2 objects
		if (listPartitionSize == 0) {
			listPartitionSize = 1;
		} else {
			listPartitionSize = 2;
		}

		List<List<String>> tenantListChunks = Lists.partition(tenantList, listPartitionSize);

		// this can be more than the totalThreadCount defined before as the
		// number is not a round number then the totalThreadCount goes up by one
		totalThreadCount = tenantListChunks.size();

		ExecutorService executor = ThreadUtils.newFixedThreadPool("thread-group-emailed-filed", 5);

		try {
		    
			for (int threadLoopIndex = 0; threadLoopIndex < totalThreadCount; threadLoopIndex++) {

				int retry = 1;
				// dont proceed if parser server is down , try for 30
				// times
				// then quit
				while (!NetworkUtils.pingURL(resumeParserServerUrl) && retry < retryLimit) {
					try {
					//	logger.info("Parser URL   " + resumeParserServerUrl);
					//	logger.info("Parser not reachable so waiting, retry  " + retry);
						Thread.sleep(5000);
						retry++;
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}

				}

				if (retry >= retryLimit) {
					continue;
				}

				EmailedFileUploadTask emailedResumeTenant = new EmailedFileUploadTask(
						tenantListChunks.get(threadLoopIndex), true);
				emailedResumeTenant.setCandidateFolderPath(candidateFolderPath);
				emailedResumeTenant.setPageableService(pageableService);
				emailedResumeTenant.setFileService(fileService);
				emailedResumeTenant.setS3Client(s3Client);
				emailedResumeTenant.setS3DownloadClient(s3DownloadClient);
				emailedResumeTenant.setUploadFileService(uploadFileService);
				emailedResumeTenant.setTokenAuthenticationProvider(tokenAuthenticationProvider);
				emailedResumeTenant.setEmailClientDetailService(emailClientDetailService);
				emailedResumeTenant.setCandidateService(candidateService);
				emailedResumeTenant.setResumeBulkTenantState(resumeBulkTenantState);
				emailedResumeTenant.setEmailedFileItemService(emailedFileItemService);
				// sleep for 500 millisecs before be bombard parser server with
				// multi thread request
				Thread.sleep(500);

				executor.execute(emailedResumeTenant);
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
				// TODO
			}
		}
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


	@Async
	public void processEmailFiles(List<String> tenantList) {
	    processEmailsFilesTenantList(tenantList);
	}
	
	private void processEmailsFilesTenantList(List<String> tenantList) {

		if (tenantList == null || tenantList.isEmpty())
			return;

		int totalThreadCount = 4;
		int listPartitionSize = tenantList.size() / totalThreadCount;

		// the if the partition size > 0 then allowing it to be partitioned of
		// length 2 (max) i.e: each partition will have max 2 objects
		if (listPartitionSize == 0) {
			listPartitionSize = 1;
		} else {
			listPartitionSize = 2;
		}

		List<List<String>> tenantListChunks = Lists.partition(tenantList, listPartitionSize);

		// this can be more than the totalThreadCount defined before as the
		// number is not a round number then the totalThreadCount goes up by one
		totalThreadCount = tenantListChunks.size();

		ExecutorService executor = ThreadUtils.newFixedThreadPool("thread-group-emailed-file-processing", 5);

		try {
		    
			for (int threadLoopIndex = 0; threadLoopIndex < totalThreadCount; threadLoopIndex++) {

				int retry = 1;
				// dont proceed if parser server is down , try for 30
				// times
				// then quit
				while (!NetworkUtils.pingURL(resumeParserServerUrl) && retry < retryLimit) {
					try {
			//			logger.info("Parser URL   " + resumeParserServerUrl);
				//		logger.info("Parser not reachable so waiting, retry  " + retry);
						Thread.sleep(5000);
						retry++;
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}

				}

				if (retry >= retryLimit) {
					continue;
				}

				EmailedFileProcessTask emailedResumeTenant = new EmailedFileProcessTask(
						tenantListChunks.get(threadLoopIndex), true);
				emailedResumeTenant.setCandidateFolderPath(candidateFolderPath);
				emailedResumeTenant.setPageableService(pageableService);
				emailedResumeTenant.setFileService(fileService);
				emailedResumeTenant.setS3Client(s3Client);
				emailedResumeTenant.setS3DownloadClient(s3DownloadClient);
				emailedResumeTenant.setUploadFileService(uploadFileService);
				emailedResumeTenant.setTokenAuthenticationProvider(tokenAuthenticationProvider);
				emailedResumeTenant.setEmailClientDetailService(emailClientDetailService);
				emailedResumeTenant.setCandidateService(candidateService);
				emailedResumeTenant.setResumeBulkTenantState(resumeBulkTenantState);
				emailedResumeTenant.setEmailedFileItemService(emailedFileItemService);
				// sleep for 500 millisecs before be bombard parser server with
				// multi thread request
				Thread.sleep(500);

				executor.execute(emailedResumeTenant);
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
				// TODO
			}
		}
	}

	



}
