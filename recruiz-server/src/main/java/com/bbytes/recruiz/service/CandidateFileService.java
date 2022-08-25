package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateFileRepository;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.common.collect.Lists;

@Service
public class CandidateFileService extends AbstractService<CandidateFile, Long> {

	private static final Logger logger = LoggerFactory.getLogger(CandidateFileService.class);

	private CandidateFileRepository candidateFileRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private CandidateService candidateService;
	
	@Autowired
	private UploadFileService uploadFileService;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${dummy.resume.pdf.path}")
	private String dumyResumeFilePath;
	
	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;
	
	@Value("${candidate.aws.bucketname}")
	private String bucketName;
	

	@Autowired
	public CandidateFileService(CandidateFileRepository candidateFileRepository) {
		super(candidateFileRepository);
		this.candidateFileRepository = candidateFileRepository;
	}

	@Transactional
	public void saveCandidateFile(CandidateFile file) {
		save(file);
	}

	@Transactional
	public void deleteCandidateFile(String filePath, String candidateId)
			throws NumberFormatException, RecruizException {

		List<CandidateFile> candidateFile = candidateFileRepository.findByFilePath(filePath);
		// Candidate candidate =
		// candidateService.getCandidateById(Long.parseLong(candidateId));

		if (candidateFile != null && !candidateFile.isEmpty()) {
			for (CandidateFile candidateFile2 : candidateFile) {
				if (candidateFile2.getFileType().equalsIgnoreCase(FileType.Masked_Resume_Original.getDisplayName())
						&& !candidateFile2.getFilePath().endsWith(".pdf")) {
					List<CandidateFile> convertedMaskedFile = candidateFileRepository.findByFileTypeAndCandidateId(
							FileType.Masked_Resume_Converted.getDisplayName(), candidateId);
					if (convertedMaskedFile != null && !convertedMaskedFile.isEmpty()) {
						for (CandidateFile cfile : convertedMaskedFile) {
							fileService.deleteFile(cfile.getFilePath());
							try{
								if(cfile.getFilePath().contains(bucketName))
									uploadFileService.deleteCandidateFolderFromAWS(cfile.getFilePath());
							}catch(Exception e){
								
							}
							
							candidateFileRepository.delete(cfile);
						}
					}
				}
				fileService.deleteFile(candidateFile2.getFilePath());
				
				if(candidateFile2.getFilePath().contains(bucketName)){
					uploadFileService.deleteCandidateFolderFromAWS(candidateFile2.getFilePath());
				}
				
				candidateFileRepository.delete(candidateFile2);
			}
		}
	}

	@Transactional(readOnly = true)
	public boolean fileExists(String file) {
		List<CandidateFile> candidateFile = candidateFileRepository.findByFilePath(file);
		if (candidateFile != null && candidateFile.size() > 0)
			return true;

		return false;
	}

	@Transactional(readOnly = true)
	public List<CandidateFile> getCandidateFile(String candidateId) {
		return candidateFileRepository.findByCandidateId(candidateId);
	}

	
	public List<CandidateFile> getCandidateFileByTypeAndId(String filetype, String candidateId) {
		return candidateFileRepository.findByFileTypeAndCandidateId(filetype, candidateId);
	}

	@Transactional(readOnly = true)
	public boolean fileExistsForCandidateWithType(String filetype, String candidateId) {
		List<CandidateFile> files = candidateFileRepository.findByFileTypeAndCandidateId(filetype, candidateId);
		if (files == null || files.isEmpty())
			return false;
		return true;
	}

	/**
	 * to upload candidate cover letter
	 * 
	 * @param candidate
	 * @throws IOException
	 */

	public void uploadCandidateCoverLetter(Candidate candidate) throws IOException {
		// adding cover page here
		try{
			if ((candidate.getCoverFileContent() != null && !candidate.getCoverFileContent().isEmpty())
					&& (candidate.getCoverFileName() != null && !candidate.getCoverFileName().isEmpty())) {
				byte[] coverFileBytes = Base64.decode(candidate.getCoverFileContent().getBytes());
				if (coverFileBytes != null && coverFileBytes.length > 0) {

					File candidateResumeFolder = new File(folderPath + File.separator + "files" + File.separator
							+ TenantContextHolder.getTenant() + File.separator + "candidate" + File.separator
							+ candidate.getCid() + File.separator + FileType.COVER_LETTER.getDisplayName());

					if (!candidateResumeFolder.exists())
						org.apache.commons.io.FileUtils.forceMkdir(candidateResumeFolder);

					File coverFile = new File(candidateResumeFolder + "/" + candidate.getCoverFileName());

					org.apache.commons.io.FileUtils.writeByteArrayToFile(coverFile, coverFileBytes);
					candidate.setCoverLetterPath(coverFile.getAbsolutePath());
					candidateService.save(candidate);
				}
			}
		}catch(Exception e){
			logger.error(""+e);
		}
	}

	// to be used from a scheduler
	// @Async
	public void uploadCandidateFilesToS3(String tenant) {
		List<BigInteger> candidateIds = candidateService.getCandidateidsOfLocalFiles();

		// returning if no candidate found
		if (candidateIds == null || candidateIds.isEmpty())
			return;

		if (candidateIds != null && !candidateIds.isEmpty()) {
			logger.info(
					"@@@@@@@@@@@@@@2 S3 Sync is started for " + TenantContextHolder.getTenant() + " @@@@@@@@@@@@@@@@@");
			List<List<BigInteger>> partionedCandidateIds = Lists.partition(candidateIds, 10);
			for (List<BigInteger> idList : partionedCandidateIds) {

				List<Long> ids = new ArrayList<>();
				for (BigInteger id : idList) {
					ids.add(id.longValue());
				}

				List<Candidate> candidates = candidateService.getCandidatesByIds(ids);
				for (Candidate candidate : candidates) {
					boolean markS3Enabled = true;
					try {
						if (candidate.getResumeLink() != null && !candidate.getResumeLink().isEmpty()
								&& !candidate.getResumeLink().startsWith("http")) {

							File resumeFile = new File(candidate.getResumeLink());
							String resumeLink = candidate.getResumeLink();
							if (!resumeFile.exists()) {
								if (resumeLink.endsWith(".pdf")) {
									logger.warn("########" + resumeLink + " not found trying for doc format ");
									resumeLink = resumeLink.replace(".pdf", ".doc");
									resumeFile = new File(resumeLink);
									if (!resumeFile.exists()) {
										logger.warn("########" + resumeLink + " not found trying for docx format ");
										resumeLink = resumeLink.replace(".doc", ".docx");
										resumeFile = new File(resumeLink);
										if (!resumeFile.exists()) {
											resumeFile = new File(dumyResumeFilePath);
										}
									}
								}
							}
							String bucketFilePath = resumeLink.replace(folderPath + "/", "");
							bucketFilePath = StringUtils.cleanFilePath(bucketFilePath); // bucketFilePath.replaceAll("
							// ",
							// "_");
							PutObjectResult putObject = s3Client.upload(resumeFile, bucketFilePath,
									fileService.getTenantBucket());
							if (putObject != null) {
								String s3ResumeUrl = s3Client.getURL(bucketFilePath, fileService.getTenantBucket());
								candidate.setResumeLink(s3ResumeUrl);
							} else {
								markS3Enabled = false;
							}

						}
					} catch (IOException e) {
						markS3Enabled = false;
						logger.warn(e.getMessage(), e);
					}
					List<CandidateFile> candidateFiles = candidateFileRepository
							.findByCandidateIdAndFilePathStartsWith(candidate.getCid() + "", folderPath);
					if (candidateFiles != null && !candidateFiles.isEmpty()) {
						for (CandidateFile candidateFile : candidateFiles) {
							try {
								String bucketFilePath = candidateFile.getFilePath().replace(folderPath + "/", "");
								bucketFilePath = bucketFilePath.replaceAll(" ", "_");
								s3Client.upload(new File(candidateFile.getFilePath()), bucketFilePath,
										fileService.getTenantBucket());
								String s3Url = s3Client.getURL(bucketFilePath, fileService.getTenantBucket());
								candidateFile.setFilePath(s3Url);
								save(candidateFile);
								logger.info("**********saving " + candidateFile.getCandidateId() + "*****"
										+ candidateFile.getFilePath() + "************");
							} catch (IOException e) {
								markS3Enabled = false;
								logger.warn(e.getMessage(), e);
							}
						}
					}

					if (markS3Enabled) {
						candidate.setS3Enabled(true);
					}
					// call transactional method here
					candidateService.save(candidate);
				}
			}
			logger.warn("@@@@@@@@@@@@@@@@ Finished sync for " + TenantContextHolder.getTenant() + "@@@@@@@@@@@");
		}

	}

	// to delete files from local server which is uploaded on s3
	public void deleteLocalFiles() throws URISyntaxException {

		long pageTotalCount = candidateService.getCountByS3Enabled();
		int totalPages = (int) (pageTotalCount / 1000);
		if (totalPages == 0) {
			totalPages += 1;
		} else if ((int) (pageTotalCount) % 1000 != 0) {
			totalPages += 1;
		}

		for (int i = 0; i < totalPages; i++) {
			Pageable pageable = new PageRequest(i, 1000);
			Page<Candidate> candidatePage = candidateService.getAllS3UploadedCandidate(pageable);
			if (candidatePage.getContent() != null && !candidatePage.getContent().isEmpty()) {
				for (Candidate candidate : candidatePage.getContent()) {
					try {
						// delete the resume file here
						if (candidate.getResumeLink() != null && !candidate.getResumeLink().isEmpty()
								&& candidate.getResumeLink().startsWith("http")) {
							String candidateFileFolderPath = folderPath + "/files/" + TenantContextHolder.getTenant()
							+ "/candidate/" + candidate.getCid() + "/";
							File fileToBeDeleted = new File(candidateFileFolderPath);
							FileUtils.deleteDirectory(fileToBeDeleted);

						}
					} catch (Exception e) {
						logger.warn("*********************Error Deleting file for candidate id " + candidate.getCid()
						+ ", Tenant " + TenantContextHolder.getTenant() + " ***************", e);
					}
				}
			}
		}

	}

	// to check which all files does not exists on s3
	public void fileExistsOnServer() throws URISyntaxException, IOException {

		long pageTotalCount = candidateService.getCountByS3Enabled();
		int totalPages = (int) (pageTotalCount / 1000);
		if (totalPages == 0) {
			totalPages += 1;
		} else if ((int) (pageTotalCount) % 1000 != 0) {
			totalPages += 1;
		}

		logger.info(
				"**************Started checking for tenant ****** " + TenantContextHolder.getTenant() + "************");

		File logFile = new File("/opt/exists/" + TenantContextHolder.getTenant() + ".txt");
		logFile.createNewFile();

		for (int i = 0; i < totalPages; i++) {
			Pageable pageable = new PageRequest(i, 1000);
			Page<Candidate> candidatePage = candidateService.getAllS3UploadedCandidate(pageable);
			if (candidatePage.getContent() != null && !candidatePage.getContent().isEmpty()) {
				for (Candidate candidate : candidatePage.getContent()) {
					try {
						// delete the resume file here
						if (candidate.getResumeLink() != null && !candidate.getResumeLink().isEmpty()
								&& candidate.getResumeLink().startsWith("http")) {
							// check if resume exists on prod
							String prodbucketName = "recruiz-prod-file-storage";
							boolean exists = s3DownloadClient.isFileExists(prodbucketName, candidate.getResumeLink());
							if (!exists) {
								// log here

								FileUtils.writeStringToFile(logFile, "\n" + TenantContextHolder.getTenant() + ","
										+ candidate.getCid() + "," + candidate.getEmail(), true);
							}

						}
					} catch (Exception e) {
						logger.info("*********************Error Deleting file for candidate id " + candidate.getCid()
						+ ", Tenant " + TenantContextHolder.getTenant() + " ***************", e);
					}
				}
			}
		}
		FileUtils.writeStringToFile(logFile,
				"\n*****************Done****" + TenantContextHolder.getTenant() + "**************\n\n", true);
	}

	/**
	 * TO check if candidate have masked resume
	 * 
	 * @param cid
	 * @return
	 */
	public boolean hasMaskedResume(Long cid) {
		List<CandidateFile> maskedResume = getCandidateFileByTypeAndId(
				FileType.Masked_Resume_Converted.getDisplayName(), cid + "");
		if (null == maskedResume || maskedResume.isEmpty()) {
			return false;
		}
		return true;
	}

	// to check if file exists with same name
	public boolean isFileExists(String cid, String fileName) {
		List<CandidateFile> files = candidateFileRepository.findByCandidateIdAndFileName(cid, fileName);
		if (null == files || files.isEmpty()) {
			return false;
		}
		return true;
	}

	public RestResponse editFileNameOfCandidate(String cid, String oldFileName, String newFileName) {

		if(cid==null || cid.equals("") || cid.isEmpty() || oldFileName==null || oldFileName.equals("") || oldFileName.isEmpty() 
				|| newFileName==null || newFileName.equals("") || newFileName.isEmpty())
			return new RestResponse(RestResponse.FAILED, "candidate_Id, newFileName, oldFileName All are required in argument!! here something missing.");

		List<CandidateFile> files = candidateFileRepository.findByCandidateIdAndFileName(cid, oldFileName);

		if (null == files || files.isEmpty() || files.size()<0)
			return new RestResponse(RestResponse.FAILED, "file is not exist");

		try{
			for (CandidateFile candidateFile : files) {

				if(candidateFile.getFileName().equalsIgnoreCase(oldFileName)){
					candidateFile.setFileName(newFileName);
					candidateFileRepository.save(candidateFile);
				}
			}
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, "Internal server error");
		}

		return new RestResponse(RestResponse.SUCCESS, "file name succussfully changed !!");
	}

	public List<CandidateFile> getCandidateFileByStorageModeAWS() {
		// TODO Auto-generated method stub
		return candidateFileRepository.getCandidateFileByStorageModeAWS();
	}

}