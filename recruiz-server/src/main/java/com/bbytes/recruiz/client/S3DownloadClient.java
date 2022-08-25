package com.bbytes.recruiz.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bbytes.recruiz.utils.FileUtils;

@Service
public class S3DownloadClient {

	private static Logger logger = LoggerFactory.getLogger(S3DownloadClient.class);

	@Autowired
	@Qualifier("downloadClient")
	private AmazonS3Client amazonS3Client;

	public String getURL(String filename, String bucket) {
		return amazonS3Client.getResourceUrl(bucket, filename);
	}

	public byte[] download(String filename, String bucket) throws IOException {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, filename);
		S3Object s3Object = amazonS3Client.getObject(getObjectRequest);
		S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
		byte[] bytes = IOUtils.toByteArray(objectInputStream);
		return bytes;
	}

	public InputStream downloadAsStream(String filename, String bucket) throws IOException {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, filename);
		S3Object s3Object = amazonS3Client.getObject(getObjectRequest);
		S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
		return objectInputStream;
	}

	public List<S3ObjectSummary> list(String bucket) {
		ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName(bucket));
		List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
		return s3ObjectSummaries;
	}

	public void deleteS3File(String bucketName, String s3RelativeFilePath) {
		amazonS3Client.deleteObject(bucketName, s3RelativeFilePath);
	}

	// this will return file object by taking bucket name and http path from s3
	public File getS3File(String bucketName, String httpFileUrl) {
		try {
			URL url = new URL(httpFileUrl);
			httpFileUrl = url.getPath();
			httpFileUrl = httpFileUrl.replaceAll("%20", "+");
			if (httpFileUrl.startsWith("/")) {
				httpFileUrl = httpFileUrl.substring(1, httpFileUrl.length());
			}
			InputStream stream = downloadAsStream(httpFileUrl, bucketName);
			String folderInTemp = System.currentTimeMillis() + "";
			File s3File = FileUtils.writeToFile(folderInTemp, url.getFile(), stream);
			return s3File;
		} catch (IOException e) {
			logger.warn("Failed to get Stream from S3", e);
		}
		return null;
	}

	// this will delete all the files in a folder in s3 and then it will delete
	// the folder as well
	public void deleteS3Folder(String bucketName, String folderName) {

		for (S3ObjectSummary file : amazonS3Client.listObjects(bucketName, folderName).getObjectSummaries()) {
			amazonS3Client.deleteObject(bucketName, file.getKey());
		}
	}

	// to check if the file exists on s3
	public boolean isFileExists(String bucketName, String httpFileUrl) throws MalformedURLException {

		URL url = new URL(httpFileUrl);
		httpFileUrl = url.getPath();
		httpFileUrl = httpFileUrl.replaceAll("%20", "+");
		if (httpFileUrl.startsWith("/")) {
			httpFileUrl = httpFileUrl.substring(1, httpFileUrl.length());
		}

		return amazonS3Client.doesObjectExist(bucketName, httpFileUrl);

	}
}