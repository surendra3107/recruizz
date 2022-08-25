package com.bbytes.recruiz.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

@Service
public class S3Client {

	private static Logger logger = LoggerFactory.getLogger(S3Client.class);

	@Autowired
	@Qualifier("uploadClient")
	private AmazonS3Client amazonS3Client;

	@Autowired
	private TransferManager amazonS3TransferClient;

	public Upload uploadAsync(String filePath, String filename, String bucket) throws FileNotFoundException {
		Upload upload = amazonS3TransferClient.upload(bucket, filename, new File(filePath));
		return upload;
	}

	public PutObjectResult upload(String filePath, String filePathRelativeToBucket, String bucketName)
			throws IOException {
		return upload(new FileInputStream(filePath), filePathRelativeToBucket, bucketName);
	}

	public PutObjectResult upload(File file, String bucketName) throws IOException {
		return upload(new FileInputStream(file.getAbsolutePath()), file.getName(), bucketName);
	}

	public Upload uploadAsync(File file, String bucket) throws FileNotFoundException {
		Upload upload = amazonS3TransferClient.upload(bucket, file.getName(), file);
		return upload;
	}

	public PutObjectResult upload(File file, String filePathRelativeToBucket, String bucket) throws IOException {
		return upload(new FileInputStream(file.getAbsolutePath()), filePathRelativeToBucket, bucket);
	}

	public Upload uploadAsync(File file, String filePathRelativeToBucket, String bucket) throws FileNotFoundException {
		Upload upload = amazonS3TransferClient.upload(bucket, filePathRelativeToBucket, file);
		return upload;
	}

	public PutObjectResult upload(InputStream inputStream, String filePathRelativeToBucket, String bucket)
			throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();

		byte[] bytes = IOUtils.toByteArray(inputStream);
		metadata.setContentLength(bytes.length);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, filePathRelativeToBucket, byteArrayInputStream,
				metadata);
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

		PutObjectResult putObjectResult = null;
		try {
			putObjectResult = amazonS3Client.putObject(putObjectRequest);
		} catch (Exception ex) {
			logger.warn("*******Failed to upload to s3**********", ex);
			putObjectResult = null;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return putObjectResult;
	}

	public Upload uploadAsync(InputStream inputStream, String filePathRelativeToBucket, String bucket) {
		Upload upload = amazonS3TransferClient.upload(bucket, filePathRelativeToBucket, inputStream, null);
		return upload;
	}

	public List<PutObjectResult> upload(MultipartFile[] multipartFiles, String bucket) throws IOException {
		List<PutObjectResult> putObjectResults = new ArrayList<>();

		for (MultipartFile multipartFile : multipartFiles) {
			if (!StringUtils.isEmpty(multipartFile.getOriginalFilename())) {
				putObjectResults
						.add(upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), bucket));
			}
		}

		return putObjectResults;
	}

	public String getURL(String filename, String bucket) {
		return amazonS3Client.getResourceUrl(bucket, filename);
	}
}