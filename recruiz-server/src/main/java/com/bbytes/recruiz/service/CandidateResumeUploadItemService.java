package com.bbytes.recruiz.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.repository.CandidateResumeUploadItemRepository;
import com.bbytes.recruiz.utils.OSUtils;

@Service
public class CandidateResumeUploadItemService extends AbstractService<CandidateResumeUploadItem, Long> {

	private Logger logger = LoggerFactory.getLogger(CandidateResumeUploadItemService.class);

	private CandidateResumeUploadItemRepository candidateResumeUploadItemRepository;

	@Autowired
	public CandidateResumeUploadItemService(CandidateResumeUploadItemRepository candidateResumeUploadItemRepository) {
		super(candidateResumeUploadItemRepository);
		this.candidateResumeUploadItemRepository = candidateResumeUploadItemRepository;
	}

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private FileService fileService;

	@Autowired
	private CandidateResumeBulkUploadBatchService candidateResumeBulkUploadBatchService;

	@Value("${base.url}")
	private String baseUrl;

	public List<CandidateResumeUploadItem> findByBatchId(String batchId) {
		return candidateResumeUploadItemRepository.findByBatchId(batchId);
	}

	public List<CandidateResumeUploadItem> findByBatchIdAndStatus(String batchId, String status) {
		return candidateResumeUploadItemRepository.findByBatchIdAndStatus(batchId, status);
	}

	public List<CandidateResumeUploadItem> findByStatus(String status) {
		return candidateResumeUploadItemRepository.findByStatus(status);
	}

	public List<CandidateResumeUploadItem> findTop1000ByStatus(String status) {
		return candidateResumeUploadItemRepository.findTop1000ByStatus(status);
	}

	public Page<CandidateResumeUploadItem> findByBatchId(String batchId, Pageable pageRequest) {
		return candidateResumeUploadItemRepository.findByBatchId(batchId, pageRequest);
	}

	public Page<CandidateResumeUploadItem> findByBatchIdAndStatus(String batchId, String status, Pageable pageRequest) {
		return candidateResumeUploadItemRepository.findByBatchIdAndStatus(batchId, status, pageRequest);
	}

	public List<CandidateResumeUploadItem> findTop1000ByBatchIdAndStatus(String batchId, String status) {
		return candidateResumeUploadItemRepository.findTop1000ByBatchIdAndStatus(batchId, status);
	}

	public List<CandidateResumeUploadItem> findTop1000ByBatchIdAndStatusInOrderByStatusDesc(String batchId,
			List<String> statusList) {
		return candidateResumeUploadItemRepository.findTop1000ByBatchIdAndStatusInOrderByStatusDesc(batchId,
				statusList);
	}

	public Long countByBatchIdAndStatusIn(String batchId, List<String> statusList) {
		return candidateResumeUploadItemRepository.countByBatchIdAndStatusIn(batchId, statusList);
	}

	public Long countByBatchIdAndStatus(String batchId, String status) {
		return candidateResumeUploadItemRepository.countByBatchIdAndStatus(batchId, status);
	}

	public Long countByStatus(String status) {
		return candidateResumeUploadItemRepository.countByStatus(status);
	}

	public Long countByStatusIn(List<String> statuses) {
		return candidateResumeUploadItemRepository.countByStatusIn(statuses);
	}

	public List<String> findDistinctBatchIdsByStatus(String status) {
		return candidateResumeUploadItemRepository.findDistinctBatchIdsByStatus(status);
	}

	public Page<CandidateResumeUploadItem> findByStatus(String status, Pageable pageRequest) {
		return candidateResumeUploadItemRepository.findByStatus(status, pageRequest);
	}

	// get all the failed items, add to a zip file and return it
	public String getFailedFilesAsZip(String batchId) {
		CandidateResumeBulkUploadBatch batch = candidateResumeBulkUploadBatchService.findByBatchId(batchId);

		if (batch != null && batch.getFailedFileZipUrl() != null && !batch.getFailedFileZipUrl().isEmpty()) {
			return batch.getFailedFileZipUrl();
		}

		List<CandidateResumeUploadItem> failedItem = candidateResumeUploadItemRepository.findByBatchIdAndStatus(batchId,
				ResumeUploadFileStatus.FAILED.toString());
		if (failedItem != null && !failedItem.isEmpty()) {
			List<File> failedFiles = new ArrayList<>();

			File tempZipDir = new File(OSUtils.getBulkUploadTempFolder() + "/" + new Date().getTime() + "/");
			tempZipDir.mkdir();

			for (CandidateResumeUploadItem candidateResumeUploadItem : failedItem) {
				try {
					if (candidateResumeUploadItem.getFilePath().startsWith("http")) {
						File file = s3DownloadClient.getS3File(fileService.getBulkUploadBucketbucket(),
								candidateResumeUploadItem.getFilePath());

						Files.copy(file.toPath(), (new File(tempZipDir.getPath() + "/" + file.getName())).toPath(),
								StandardCopyOption.REPLACE_EXISTING);

						failedFiles.add(file);
					} else {
						File file = new File(candidateResumeUploadItem.getFilePath());
						if (file.exists()) {
							failedFiles.add(file);
							Files.copy(file.toPath(), (new File(tempZipDir.getPath() + "/" + file.getName())).toPath(),
									StandardCopyOption.REPLACE_EXISTING);
						} else {
							
							String httpUrl = s3DownloadClient.getURL(candidateResumeUploadItem.getFilePath(), fileService.getBulkUploadBucketbucket());
							

							file = s3DownloadClient.getS3File(fileService.getBulkUploadBucketbucket(),
									httpUrl);

							Files.copy(file.toPath(), (new File(tempZipDir.getPath() + "/" + file.getName())).toPath(),
									StandardCopyOption.REPLACE_EXISTING);

						}
					}
				} catch (Exception ex) {
					logger.warn(ex.getMessage(), ex);
				}
			}

			String zipFilePath = "";
			try {
				String zipTargetPath = OSUtils.getBulkUploadTempFolder() + "/" + new Date().getTime() + ".zip";
				fileService.zip(tempZipDir.getPath(), zipTargetPath);

				File zipFile = new File(zipTargetPath);
				zipFilePath = zipTargetPath;
				if (zipFilePath.startsWith("/")) {
					zipFilePath = zipFilePath.substring(1, zipFilePath.length());
				}
				PutObjectResult result = s3Client.upload(zipFile, fileService.getFailedFilesBucket());
				if (result != null) {
					String downloadFilePath = s3DownloadClient.getURL(zipFile.getName(),
							fileService.getFailedFilesBucket());
					batch.setFailedFileZipUrl(downloadFilePath);
					candidateResumeBulkUploadBatchService.save(batch);

					// deleting temp folder and zip file after uploading to
					// s3
					FileUtils.deleteDirectory(tempZipDir);
					zipFile.delete();
					return downloadFilePath;
				} else {
					return zipFilePath;
				}

			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
				return zipFilePath;
			}

		}

		return null;
	}

	@Transactional
	public Long deleteItemsByBatchIdAndStatus(String batchId,String status) {
	    Long deletedItemsCount = candidateResumeUploadItemRepository.deleteByBatchIdAndStatus(batchId, status);
	    return deletedItemsCount;
	}
	
}
