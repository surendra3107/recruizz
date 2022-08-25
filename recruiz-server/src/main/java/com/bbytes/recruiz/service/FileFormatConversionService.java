package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class FileFormatConversionService {

	private static Logger logger = LoggerFactory.getLogger(FileFormatConversionService.class);

	private OfficeManager officeManager;

	@Resource
	private Environment environment;

	private int maxQueueSizePerTenant = 200;

	private volatile Map<String,Integer> tenantToQueueSize = new HashMap<String,Integer>();

	@PostConstruct
	public void start() {

		try {
			String officeHomeDir = environment.getProperty("office.home.directory");
			if (officeHomeDir == null || officeHomeDir.isEmpty()) {
				throw new RecruizException("'office.home.directory' property value  not set", ErrorHandler.SERVER_ERROR);
			}

			officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(officeHomeDir).setTaskExecutionTimeout(240000L)
					.setTaskQueueTimeout(60000L).setPortNumbers(8101, 8102).setMaxTasksPerProcess(300).buildOfficeManager();

			if (officeManager.isRunning()) {
				officeManager.stop();
				officeManager.start();
			} else {
				officeManager.start();
			}

			if (!officeManager.isRunning())
				throw new RecruizException("Open Office connect to service via pipe failed to start",
						ErrorHandler.OPEN_OFFICE_SERVICE_FAILURE);

		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@PreDestroy
	public void stop() {
		try {
			if (officeManager != null) {
				officeManager.stop();
			}
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	/**
	 * Used only by bulk upload to control the number of request sent to
	 * conversion service
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws RecruizException
	 */
	public String queueFileConvert(String filePath) throws IOException, RecruizException {
		String tenantId  = TenantContextHolder.getTenant();
		int queueSize = 0;
		
		if (tenantToQueueSize.get(tenantId) == null) {
			tenantToQueueSize.put(tenantId, 0);
		}
		
		try {
			// sleep if the queue is big
			while ((queueSize = tenantToQueueSize.get(tenantId)) > maxQueueSizePerTenant) {
				logger.info("########### Conversion queue is full so waiting for 1 sec #############");
				Thread.sleep(1000);
			}

			logger.info("%%%%%%%%%%%% Conversion queue for tenant "+ tenantId +" is free now with size "+ queueSize +"  %%%%%%%%%%%%%");
			queueSize++;
			tenantToQueueSize.put(tenantId, queueSize);
			return convert(filePath, "pdf");
		} catch (InterruptedException e) {
			// do nothing
		} finally {
			queueSize--;
			tenantToQueueSize.put(tenantId, queueSize);
		}

		throw new RecruizException("File conversion failed in queue file convert service ");
	}

	public String convert(String filePath) throws IOException {
		return convert(filePath, "pdf");
	}

	public String convert(String filePath, String targetFileExtension) throws IOException {
		if (FilenameUtils.getExtension(filePath).equalsIgnoreCase(targetFileExtension)) {
			return filePath;
		}
		File inputFile = new File(filePath);
		File outputFile = new File(FilenameUtils.removeExtension(filePath) + "." + targetFileExtension);
		try {
			convert(inputFile, outputFile);
		} catch (Throwable ex) {
			String errorMsg = getFileNamesForErrorMessage(inputFile, outputFile);
			logger.error(errorMsg + ex.getMessage(), ex);
			return filePath;
		}
		return FilenameUtils.removeExtension(filePath) + "." + targetFileExtension;
	}

	public void convert(File inputFile, File outputFile) {
		if (inputFile != null && inputFile.exists()) {

			// try {
			// openOfficeConnection.connect();
			OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager, new DefaultDocumentFormatRegistry());
			converter.convert(inputFile, outputFile);

			// } catch (Throwable e) {
			// String errorMsg = getFileNamesForErrorMessage(inputFile,
			// outputFile);
			// logger.error(errorMsg + " , " + e.getMessage(), e);
			// }
		}
	}

	private String getFileNamesForErrorMessage(File inputFile, File outputFile) {
		String errorMsg = "";
		if (inputFile != null) {
			errorMsg = errorMsg + " Input File = " + inputFile.getAbsolutePath() + "\n";
		}

		if (outputFile != null) {
			errorMsg = errorMsg + " Output File = " + outputFile.getAbsolutePath() + "\n";
		}
		return errorMsg;
	}

}