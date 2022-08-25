package com.bbytes.recruiz.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class UploadFileService {

	private static final Logger log = LoggerFactory.getLogger(UploadFileService.class);

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${candidate.aws.bucketname}")
	private String BUCKET_NAME;

	@Autowired
	AmazonS3StorageService amazonS3StorageService;

	@Autowired
	CandidateFileService candidateFileService;

	private static final String SUFFIX = "/";

	public void createFolderStructureForCandidate(String path, String id) {

		File candidateDocsFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "candidate" + File.separator + id + File.separator + "docs");
		if (!candidateDocsFolder.exists())
			candidateDocsFolder.mkdirs();

		File candidateResumeFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "candidate" + File.separator + id + File.separator + "resume");
		if (!candidateResumeFolder.exists())
			candidateResumeFolder.mkdirs();
	}

	public String createFolderStructureForEmailActivity(String userEmail) throws IOException {
		File emailActivityFolder = new File( folderPath + File.separator+File.separator + "files" + File.separator + TenantContextHolder.getTenant() + File.separator
				+ "email_activity" + File.separator + userEmail + File.separator + "attachments"+ File.separator + System.currentTimeMillis());
		if (!emailActivityFolder.exists())
			emailActivityFolder.mkdirs();

		return emailActivityFolder.getAbsolutePath();
	}

	public String createFolderStructureForPosition(String id) {

		File candidateDocsFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "position" + File.separator + id + File.separator + "jd");
		if (!candidateDocsFolder.exists())
			candidateDocsFolder.mkdirs();
		
		return candidateDocsFolder.getPath();
	}

	// create folder structure for client files
	public String createFolderStructureForClient(String id) {

		File clientDocsFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "client" + File.separator + id);
		if (!clientDocsFolder.exists())
			clientDocsFolder.mkdirs();

		return clientDocsFolder.getPath();
	}


	// create folder structure for position files
	public String createFolderStructureForPositionFiles(String id) {

		File positionDocsFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "position" + File.separator + id);
		if (!positionDocsFolder.exists())
			positionDocsFolder.mkdirs();

		return positionDocsFolder.getPath();
	}


	// to create folder structure for employee
	public String createFolderStructureForEmployee(String id) {

		File employeeDocsFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "employee" + File.separator + id);
		if (!employeeDocsFolder.exists())
			employeeDocsFolder.mkdirs();

		return employeeDocsFolder.getPath();
	}

	public void createFolderStructureForRequestedPosition(String id) {

		File requestedPosition = new File(folderPath + File.separator + "files" + File.separator
				+ TenantContextHolder.getTenant() + File.separator + "requested position" + File.separator + id
				+ File.separator + "requested position");
		if (!requestedPosition.exists())
			requestedPosition.mkdirs();
	}

	/**
	 * creating folder structure for DP
	 * 
	 * @param id
	 */
	public void createFolderStructureForCandidateDP(String id) {

		File candidateDPFolder = new File(
				folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
				+ File.separator + "candidate" + File.separator + id + File.separator + "dp");
		if (!candidateDPFolder.exists())
			candidateDPFolder.mkdirs();
	}

	public String uploadFileToLocalServer(File file, String fileName, String pathType, String cid)
			throws RecruizException, IOException {
		if (!file.exists()) {
			return null;
		}
		return uploadFileToLocalServer(Files.readAllBytes(file.toPath()), fileName, pathType, cid);
	}

	public String uploadFileToLocalServer(byte[] bytes, String fileName, String pathType, String cid)
			throws RecruizException, IOException {
		String filePath = "";
		String rootPath = "";
		// Creating the directory to store file
		if (pathType.equalsIgnoreCase("resume")) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "resume";
		} else if (pathType.equalsIgnoreCase("Masked Resume Original")) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "Masked Resume";
		} else if (pathType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator
			+ FileType.Original_Resume.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.JD.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "position" + File.separator + cid + File.separator + "jd";
		} else if (pathType.equalsIgnoreCase(FileType.DP.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator
			+ FileType.DP.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.EMAIL_ATTACHMENT.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "EMAIL_ATTACHMENTS" + File.separator + File.separator + cid 
			+ File.separator + FileType.EMAIL_ATTACHMENT.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.COVER_LETTER.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "EMAIL_ATTACHMENTS" + File.separator + File.separator + cid 
			+ File.separator + FileType.COVER_LETTER.getDisplayName();
		} else {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "docs";
		}

		File dir = new File(rootPath);
		if (!dir.exists())
			dir.mkdirs();

		// Create the file on server
		File serverFile = new File(dir.getAbsolutePath() + File.separator + fileName);
		if (serverFile.exists()) {
			// this will replace the resume file and JD file if it is
			// present (latest requirement)
			if (pathType.equalsIgnoreCase("jd") || pathType.equalsIgnoreCase("resume")
					|| pathType.equalsIgnoreCase(FileType.DP.getDisplayName())
					|| pathType.equalsIgnoreCase(FileType.EMAIL_ATTACHMENT.getDisplayName())
					|| pathType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
				// if (serverFile.delete()) {
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				log.debug("Server File Location=" + serverFile.getAbsolutePath());
				filePath = serverFile.getAbsolutePath();
				// }
			} else {
				throw new RecruizException(ErrorHandler.FILE_UPLOAD_FAILED, ErrorHandler.FILE_EXISTS);
			}
		} else {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
			stream.write(bytes);
			stream.close();
			log.debug("Server File Location=" + serverFile.getAbsolutePath());
			filePath = serverFile.getAbsolutePath();
		}
		return filePath;
	}

	public String uploadFileToLocalServer(InputStream fsStream, String fileName, String pathType, String cid)
			throws RecruizException, IOException {
		String filePath = "";
		String rootPath = "";
		// Creating the directory to store file
		if (pathType.equalsIgnoreCase("resume")) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "resume";
		} else if (pathType.equalsIgnoreCase("Masked Resume Original")) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "Masked Resume";
		} else if (pathType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator
			+ FileType.Original_Resume.getDisplayName();
		} else if (pathType.equalsIgnoreCase("jd")) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "position" + File.separator + cid + File.separator + "jd";
		} else if (pathType.equalsIgnoreCase(FileType.DP.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator
			+ FileType.DP.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.EMAIL_ATTACHMENT.getDisplayName())) {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "EMAIL_ATTACHMENTS" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + FileType.EMAIL_ATTACHMENT.getDisplayName();
		} else {
			rootPath = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "candidate" + File.separator + cid + File.separator + "docs";
		}

		File dir = new File(rootPath);
		if (!dir.exists())
			dir.mkdirs();

		// Create the file on server
		File serverFile = new File(dir.getAbsolutePath() + File.separator + fileName);
		if (serverFile.exists()) {
			// this will replace the resume file and JD file if it is
			// present (latest requirement)
			if (pathType.equalsIgnoreCase("jd") || pathType.equalsIgnoreCase("resume")
					|| pathType.equalsIgnoreCase(FileType.DP.getDisplayName())
					|| pathType.equalsIgnoreCase(FileType.EMAIL_ATTACHMENT.getDisplayName())) {

				// if (serverFile.delete()) {
				Files.copy(fsStream, serverFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				log.debug("Server File Location=" + serverFile.getAbsolutePath());
				filePath = serverFile.getAbsolutePath();
				// }
			} else {
				log.warn("File " + serverFile.getAbsolutePath()
				+ "exists so retruning the file path of the existing one, not replaced.");
				serverFile.getAbsolutePath();
				// throw new
				// RecruizException(ErrorHandler.FILE_UPLOAD_FAILED,
				// ErrorHandler.FILE_EXISTS);
			}
		} else {
			Files.copy(fsStream, serverFile.toPath());
			// InputStream inputStream = new FileInputStream(serverFile);
			log.debug("Server File Location=" + serverFile.getAbsolutePath());
			filePath = serverFile.getAbsolutePath();
		}

		return filePath;

	}

	public void createFolderStructureInAWSForCandidate(String folderPath2, String id) {

		String candidateDocsFolder = "files" + SUFFIX + TenantContextHolder.getTenant()
		+ SUFFIX + "candidate" + SUFFIX + id + SUFFIX + "docs";

		amazonS3StorageService.createFolder(BUCKET_NAME, candidateDocsFolder);

		/*String candidateResumeFolder = "files" + SUFFIX + TenantContextHolder.getTenant()
						+ SUFFIX + "candidate" + SUFFIX + id + SUFFIX + "resume";

		amazonS3StorageService.createFolder(BUCKET_NAME, candidateResumeFolder);*/
	}

	public String uploadFileToAWSServer(File resumeFile, String cleanFileName, String pathType, String cid) {


		String filePath = "";
		String rootPath = "";
		// Creating the directory to store file
		if (pathType.equalsIgnoreCase("resume")) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "resume";
		} else if (pathType.equalsIgnoreCase("Masked Resume Original")) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "Masked Resume";
		} else if (pathType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + FileType.Original_Resume.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.JD.getDisplayName())) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "position" + SUFFIX + cid + SUFFIX + "jd";
		} else if (pathType.equalsIgnoreCase(FileType.DP.getDisplayName())) {
			rootPath =  "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX+ FileType.DP.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.EMAIL_ATTACHMENT.getDisplayName())) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "docs" + SUFFIX + FileType.EMAIL_ATTACHMENT.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.COVER_LETTER.getDisplayName())) {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "docs" + SUFFIX + FileType.COVER_LETTER.getDisplayName();
		} else {
			rootPath = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "docs";
		}

		amazonS3StorageService.createFolder(BUCKET_NAME, rootPath);

		amazonS3StorageService.uploadFile(BUCKET_NAME, rootPath+SUFFIX+cleanFileName, resumeFile);

		return BUCKET_NAME+SUFFIX+rootPath+SUFFIX+cleanFileName;
	}

	public void readMaskedFilesAndUploadInAws(long cid) {

		//System.out.println("Start readMaskedFilesAndUploadInAws ====");

		String maskedFileDirectory = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
		+ File.separator + "candidate" + File.separator + cid + File.separator + "Masked Resume";

		File maskedDirectory = new File(maskedFileDirectory);
		if (maskedDirectory.exists()) {

			String maskedFileDirectoryAws = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "candidate" + SUFFIX + cid + SUFFIX + "Masked Resume";

			amazonS3StorageService.createFolder(BUCKET_NAME, maskedFileDirectoryAws);


			File[] listOfFiles = maskedDirectory.listFiles();

			if(listOfFiles!=null && listOfFiles.length>0)
				for (File fileData : listOfFiles) {

					String fileName = maskedFileDirectoryAws + SUFFIX +fileData.getName();
					amazonS3StorageService.uploadFile(BUCKET_NAME, fileName, fileData.getAbsoluteFile());
				}


			List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(cid+"");

			for (CandidateFile candidateFile : candidateFiles) {

				if(candidateFile.getFileType().equalsIgnoreCase("Masked Resume Converted")){
					candidateFile.setFilePath(maskedFileDirectoryAws + SUFFIX +candidateFile.getFileName());
					candidateFileService.save(candidateFile);
				}else if(candidateFile.getFileType().equalsIgnoreCase("Masked Resume Original")){
					candidateFile.setFilePath(maskedFileDirectoryAws + SUFFIX +candidateFile.getFileName());
					candidateFileService.save(candidateFile);
				}
			}

			//	System.out.println("end readMaskedFilesAndUploadInAws ====");

		}

	}

	public void deleteCandidateFolderFromAWS(String s3CandidatePath) {
		
		if(s3CandidatePath.contains(BUCKET_NAME)){
			s3CandidatePath = s3CandidatePath.split(BUCKET_NAME+"/")[1];
		}
		
		amazonS3StorageService.deleteFolder(BUCKET_NAME,s3CandidatePath);
	}

	public String createFolderAndUploadFileInAwsForPosition(Long pid, File fileToUpload, String fileName) {
		
			String positionFileDirectoryAws = "files" + SUFFIX + TenantContextHolder.getTenant()
			+ SUFFIX + "position" + SUFFIX + pid + SUFFIX + "jd";

			amazonS3StorageService.createFolder(BUCKET_NAME, positionFileDirectoryAws);
			
			String positionfileName = positionFileDirectoryAws + SUFFIX +fileToUpload.getName();
			amazonS3StorageService.uploadFile(BUCKET_NAME, positionfileName, fileToUpload.getAbsoluteFile());
			
			return BUCKET_NAME+ SUFFIX +positionFileDirectoryAws + SUFFIX +fileName;
			
	}

	public String createFolderAndUploadFileInAwsForClient(Long cid, File fileToUpload, String fileName) {
		
		String clientFileDirectoryAws = "files" + SUFFIX + TenantContextHolder.getTenant()
		+ SUFFIX + "client" + SUFFIX + cid + SUFFIX + "jd";

		amazonS3StorageService.createFolder(BUCKET_NAME, clientFileDirectoryAws);
		
		String clientfileName = clientFileDirectoryAws + SUFFIX +fileToUpload.getName();
		amazonS3StorageService.uploadFile(BUCKET_NAME, clientfileName, fileToUpload.getAbsoluteFile());
		
		return BUCKET_NAME+ SUFFIX +clientFileDirectoryAws + SUFFIX +fileName;
	}
}
