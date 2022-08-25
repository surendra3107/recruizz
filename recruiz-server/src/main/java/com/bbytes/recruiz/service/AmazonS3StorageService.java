package com.bbytes.recruiz.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Strings;

@Service
public class AmazonS3StorageService {

	private static final Logger logger = LoggerFactory.getLogger(AmazonS3StorageService.class);
	
	@Value("${cloud.aws.credentials.accessKey}")
	private String YourAccessKeyID;

	@Value("${cloud.aws.credentials.secretKey}")
	private String YourSecretAccessKey;
	
	private static final String SUFFIX = "/";
	//private static final String YourAccessKeyID = "AKIA6NPMYK3J7DOIIGMK";
	//private static final String YourSecretAccessKey = "k+fmhjaePd9vG16o4oQcZ75XDQxGRtkuXDa+mvMA";
	
	/*AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
	
	// create a client connection based on credentials
	AmazonS3 s3client = new AmazonS3Client(credentials);*/
	
	public void createBucket(String bucketName){
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		s3client.createBucket(bucketName);
	}
	
	public void createFolder(String bucketName, String folderName){
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		// create meta-data for your folder and set content-length to 0
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(0);
				// create empty content
				InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
				// create a PutObjectRequest passing the folder name suffixed by /
				PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
						folderName + SUFFIX, emptyContent, metadata);
				// send request to S3 to create folder
				s3client.putObject(putObjectRequest);
	}
	
	/**
	 * This method first deletes all the files in given folder and than the
	 * folder itself
	 */
	public void deleteFolder(String bucketName, String folderName) {
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		List<S3ObjectSummary> fileList = s3client.listObjects(bucketName, folderName).getObjectSummaries();
		for (S3ObjectSummary file : fileList) {
			s3client.deleteObject(bucketName, file.getKey());
		}
		s3client.deleteObject(bucketName, folderName);
	}
	
	public void deleteBucket(String bucketName){
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		s3client.deleteBucket(bucketName);
	}
	
	public void uploadFile(String bucketName, String fileName, File file){
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}
	
	
	public InputStream getFileByLocationId(String bucketName,String fileLocationId) {
		AWSCredentials credentials = new BasicAWSCredentials(YourAccessKeyID, YourSecretAccessKey);
		
		// create a client connection based on credentials
		AmazonS3 s3client = new AmazonS3Client(credentials);
		
		if (Strings.isNullOrEmpty(bucketName)) {
			logger.error("No bucket name is specified.");
			return null;
		}

		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileLocationId);

		S3Object s3Object = s3client.getObject(getObjectRequest);

		logger.error("Successfully retrieved the file from S3 bucket {}", getObjectRequest.getBucketName());

		return s3Object.getObjectContent();
	}
	 
	
}
