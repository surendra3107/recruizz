package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.FileDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.AmazonS3StorageService;
import com.bbytes.recruiz.service.CandidateFileService;
import com.bbytes.recruiz.service.CandidateResumeUploadItemService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;

@RestController
public class FileController {

	private static Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private FileService fileService;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private CandidateResumeUploadItemService candidateResumeUploadItemService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	AmazonS3StorageService amazonS3StorageService;

	@Value("${export.folderPath.path}")
	private String rootFolderPath;

	@Autowired
	private CandidateFileService candidateFileService;
	
	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;

	@Value("${candidate.aws.bucketname}")
	private String BUCKET_NAME;

	@RequestMapping(value = "/api/v1/candidate/files/download", headers = "Accept=*/*", method = RequestMethod.GET)
	public void downloadFile(HttpServletResponse response,
			@RequestParam(value = "fileName", required = false) String fileName,
			@RequestParam(value = "isBulk", required = false) String isBulk) throws IOException, RecruizException {

		if(fileName != null && !fileName.isEmpty() && !fileName.startsWith("http") && isBulk != null && !isBulk.trim().isEmpty()){
			File file = new File(fileName);
			if(!file.exists()){
				fileName = s3DownloadClient.getURL(fileName, fileService.getBulkUploadBucketbucket());
				if(!s3DownloadClient.isFileExists(fileService.getBulkUploadBucketbucket(), fileName)) {
					InputStream errorFile = new ClassPathResource(GlobalConstants.ERROR_FILE_PATH).getInputStream();
					sendErrorFileResponse(response, GlobalConstants.ERROR_FILE_NAME, errorFile);
				}
			}
		}

		if (fileName == null || fileName.isEmpty()) {
			InputStream errorFile = new ClassPathResource(GlobalConstants.ERROR_FILE_PATH).getInputStream();
			sendErrorFileResponse(response, GlobalConstants.ERROR_FILE_NAME, errorFile);
		} else if (fileName.startsWith("http")) {
			URL url = new URL(fileName);
			fileName = url.getPath();
			fileName = fileName.replaceAll("%20", "+");
			if (fileName.startsWith("/")) {
				fileName = fileName.substring(1, fileName.length());
			}
			InputStream s3Stream = null;
			if(isBulk != null && !isBulk.isEmpty() && isBulk.equalsIgnoreCase("yes")){
				s3Stream = s3DownloadClient.downloadAsStream(fileName, fileService.getBulkUploadBucketbucket());
			}else{
				s3Stream = s3DownloadClient.downloadAsStream(fileName, fileService.getTenantBucket());	
			}

			String folderInTemp = System.currentTimeMillis() + "";
			File s3File = FileUtils.writeToFile(folderInTemp, url.getFile(), s3Stream);
			sendFileResponse(response, fileName, s3File.toPath());

		}else{

			try{
				InputStream s3Stream = null;
				String s3CandidateFilePath = fileName;
				if(s3CandidateFilePath.contains(BUCKET_NAME)){
					s3CandidateFilePath = s3CandidateFilePath.split(BUCKET_NAME+"/")[1];
				}
				s3Stream = amazonS3StorageService.getFileByLocationId(BUCKET_NAME, s3CandidateFilePath);		
				String folderInTemp = System.currentTimeMillis() + "";
				File s3File = FileUtils.writeToFile(folderInTemp, s3Stream);
				sendFileResponse(response, fileName, s3File.toPath());
			}catch(Exception e){
				sendFileResponse(response, fileName, Paths.get(fileName));
			}
		}
	}

	public void sendErrorFileResponse(HttpServletResponse response, String fileName, InputStream errorFile)
			throws IOException {

		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		response.setContentType(mimeType);
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + fileName + "\""));

		IOUtils.copy(errorFile, response.getOutputStream());
	}

	public void sendS3Response(HttpServletResponse response, String fileName, InputStream fileStream)
			throws IOException {

		/*
		 * if (!file.exists()) { throw new
		 * RecruizWarnException(ErrorHandler.NO_FILE,
		 * ErrorHandler.FILE_DOES_NOT_EXIST); }
		 */

		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		response.setContentType(mimeType);

		/*
		 * "Content-Disposition : inline" will show viewable types [like
		 * images/text/pdf/anything viewable by browser] right on browser while
		 * others(zip e.g) will be directly downloaded [may provide save as
		 * popup, based on your browser setting.]
		 */
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + fileName + "\""));

		/*
		 * "Content-Disposition : attachment" will be directly download, may
		 * provide save as popup, based on your browser setting
		 */
		// response.setHeader("Content-Disposition",
		// String.format("attachment; filename=\"%s\"", file.getName()));

		response.setContentLength(fileStream.available());
		IOUtils.copy(fileStream, response.getOutputStream());
	}

	public void sendFileResponse(HttpServletResponse response, String fileName, Path filePath) throws IOException {

		// Path filePath = Paths.get(path);
		String name = filePath.getFileName().toString();
		logger.debug("File requested for download : " + name);

		if (filePath.toFile() == null || !filePath.toFile().exists()) {
			return;
		}

		/*
		 * if (!file.exists()) { throw new
		 * RecruizWarnException(ErrorHandler.NO_FILE,
		 * ErrorHandler.FILE_DOES_NOT_EXIST); }
		 */

		String mimeType = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		response.setContentType(mimeType);

		/*
		 * "Content-Disposition : inline" will show viewable types [like
		 * images/text/pdf/anything viewable by browser] right on browser while
		 * others(zip e.g) will be directly downloaded [may provide save as
		 * popup, based on your browser setting.]
		 */
		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + filePath.getFileName().toString() + "\""));

		/*
		 * "Content-Disposition : attachment" will be directly download, may
		 * provide save as popup, based on your browser setting
		 */
		// response.setHeader("Content-Disposition",
		// String.format("attachment; filename=\"%s\"", file.getName()));

		response.setContentLength((int) filePath.toFile().length());
		Files.copy(filePath, response.getOutputStream());
		System.out.println(response);
	}

	/**
	 * upload file to pubset and it will return the file pubset path
	 */
	@RequestMapping(value = "/api/v1/file/upload", method = RequestMethod.POST)
	public RestResponse uploadFileToPubset(@RequestPart("json") @Valid FileDTO fileDTO,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException, RecruizException {

		String filePath = fileService.copyToPubset(fileDTO, file);
		return new RestResponse(RestResponse.SUCCESS, filePath);
	}

	/**
	 * Delete pubsetFile
	 * 
	 * @return
	 * @throws IOException
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/file/pubset/delete", method = RequestMethod.DELETE)
	public RestResponse deleteFile(@RequestParam("fileUrl") String fileUrl) throws IOException, RecruizException {

		fileService.deletedPubsetFile(fileUrl);
		return new RestResponse(RestResponse.SUCCESS, "File Deleted");
	}

	@RequestMapping(value = "/api/v1/bulk/failed/files", method = RequestMethod.GET)
	public void downloadFailedFiles(HttpServletResponse response,
			@RequestParam(value = "batchId", required = true) String batchId) throws IOException, RecruizException {
		String fileName = candidateResumeUploadItemService.getFailedFilesAsZip(batchId);

		if (fileName.startsWith("http")) {
			URL url = new URL(fileName);
			fileName = url.getPath();
			fileName = fileName.replaceAll("%20", "+");
			if (fileName.startsWith("/")) {
				fileName = fileName.substring(1, fileName.length());
			}
			InputStream s3Stream = s3DownloadClient.downloadAsStream(fileName, fileService.getFailedFilesBucket());
			String folderInTemp = System.currentTimeMillis() + "";
			File s3File = FileUtils.writeToFile(folderInTemp, url.getFile(), s3Stream);
			sendFileResponse(response, fileName, s3File.toPath());
		} else {
			sendFileResponse(response, fileName, Paths.get(fileName));
		}
	}

	@RequestMapping(value = "/api/v1/bulk/failed/files/list", method = RequestMethod.GET)
	public RestResponse downloadFailedFilesList(@RequestParam(value = "batchId", required = true) String batchId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder)
			throws IOException, RecruizException {

		Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField,
				pageableService.getSortDirection(sortOrder));

		Page<CandidateResumeUploadItem> failedItems = candidateResumeUploadItemService.findByBatchIdAndStatus(batchId,
				ResumeUploadFileStatus.FAILED.toString(), pageable);

		return new RestResponse(true, failedItems);

	}
	
	// to check if candidate file exists with name
	@RequestMapping(value = "/api/v1/candidate/file/exists/{cid}", method = RequestMethod.GET)
	public RestResponse downloadFailedFilesList(@PathVariable String cid,@RequestParam String fileName) {
	    RestResponse response = null;
			boolean exists = candidateFileService.isFileExists(cid,fileName);
			response = new RestResponse(true,exists);
	    return response;
	}
	
	
	 // change file name of candidate doc's
		@RequestMapping(value = "/api/v1/candidate/file/editFileNameOfCandidate", method = RequestMethod.GET)
		public RestResponse editFileNameOfCandidate(@RequestParam String cid,@RequestParam String oldFileName, @RequestParam String newFileName) {
		    
		    return candidateFileService.editFileNameOfCandidate(cid,oldFileName,newFileName);
		}
	
}
