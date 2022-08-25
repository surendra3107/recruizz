package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.ImportJobBatch;
import com.bbytes.recruiz.domain.ImportJobUploadItem;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.ImportFileDTO;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.ImportExportAsyncService;
import com.bbytes.recruiz.service.ImportExportService;
import com.bbytes.recruiz.service.ImportJobBatchService;
import com.bbytes.recruiz.service.ImportJobUploadItemService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * This is Import Export Controller
 * 
 * @author Akshay
 *
 */
@RestController
public class ImportExportController {

	private static Logger logger = LoggerFactory.getLogger(ImportExportController.class);

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private ImportExportAsyncService importExportAsyncService;

	@Autowired
	private ResourceLoader resourceloader;

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private ImportJobBatchService importJobBatchService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Autowired
	private ImportJobUploadItemService importJobUploadItemService;
	
	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Value("${export.folderPath.path}")
	private String rootFolderPath;

	/**
	 * This API is used to export the recruiz data like candidate, client and
	 * position data and files
	 * 
	 * @author Akshay
	 * @param isExportFile
	 * @return
	 * @throws IOException
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/export/data", method = RequestMethod.GET)
	public RestResponse exportRecruizData(@RequestParam(value = "exportFiles", required = false) String isExportFile)
			throws IOException, RecruizException {
	    
	 // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.ExportRecruizData.name());

		User loggedInUser = userService.getLoggedInUserObject();
		String tenantId = TenantContextHolder.getTenant();

		if (resumeBulkTenantState.isTenantExportJobTaskRunning(tenantId)) {
			logger.info(
					"#################  Currently this tenant export job is busy  = " + tenantId + " ###############");
			return new RestResponse(RestResponse.FAILED, "Already Export job initiated", ErrorHandler.EXPORT_JOB_BUSY);
		}

		if (loggedInUser != null)
			importExportAsyncService.startExportDataAsync(tenantId, Boolean.parseBoolean(isExportFile), loggedInUser);

		return new RestResponse(RestResponse.SUCCESS, "Export job initiated");
	}

	/**
	 * Upload import file and return header map
	 * 
	 * @param file
	 * @param importType
	 * @return
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/import/upload", method = RequestMethod.POST)
	public RestResponse uploadImportFile(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "importType") String importType)
			throws RecruizException, IllegalStateException, IOException {

	 // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.UploadImportFile.name());
	    
		// checking uploaded file is .csv or excel file
		if (!"csv".equals(FilenameUtils.getExtension(file.getOriginalFilename()))
				&& !"xlsx".equals(FilenameUtils.getExtension(file.getOriginalFilename()))
				&& !"xls".equals(FilenameUtils.getExtension(file.getOriginalFilename())))
			return new RestResponse(RestResponse.FAILED, "Invalid file format", ErrorHandler.INVALID_FILE_FORMAT);

		File importFile = fileService.multipartToFileForBulkUpload(file);
		Map<String, Object> headerMap = importExportService.getUploadedFileHeaderMap(importFile, importType);

		return new RestResponse(RestResponse.SUCCESS, headerMap);

	}

	/**
	 * The importData method is used to import all candidate, position or client
	 * data from CSV or excel file and update into db
	 * 
	 * @param importFileDTO
	 * @return
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/import/data", method = RequestMethod.POST)
	public RestResponse importData(@RequestBody ImportFileDTO importFileDTO)
			throws RecruizException, IllegalStateException, IOException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ImportData.name());
	    
		RestResponse response;
		if (importFileDTO.getFilePath() == null || importFileDTO.getFilePath().isEmpty())
			return new RestResponse(RestResponse.FAILED, "File Path must not be null");

		File importFile = new File(importFileDTO.getFilePath());
		if (!importFile.exists())
			return new RestResponse(RestResponse.FAILED, "File does not exist");
		if (importFileDTO.getHeaderMapList() == null || importFileDTO.getHeaderMapList().isEmpty())
			return new RestResponse(RestResponse.FAILED, "Header map list must not be null");

		Map<String, String> headerMap = new HashMap<String, String>();
		for (Map<String, String> map : importFileDTO.getHeaderMapList()) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				headerMap.put(entry.getKey(), entry.getValue());
			}
		}

		User loggedInUser = userService.getLoggedInUserObject();

		String batchId = String.valueOf(Math.round(Math.random() * 90000) + 100000 + System.currentTimeMillis());

		if (loggedInUser != null)
			importExportAsyncService.startImportDataAsync(TenantContextHolder.getTenant(),
					SecurityContextHolder.getContext().getAuthentication(), loggedInUser, importFile, headerMap,
					importFileDTO.getImportType(), batchId);

		response = new RestResponse(RestResponse.SUCCESS, "Import job initiated");

		return response;
	}


	/**
	 * To Download Failed Import data
	 * 
	 * @param response
	 * @param batchId
	 * @throws RecruizException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	@RequestMapping(value = "/api/v1/import/failed/items/download", headers = "Accept=*/*", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadExcelImportItemsReport(HttpServletResponse response,
			@RequestParam(value = "batchId") String batchId)
			throws RecruizException, InvalidFormatException, IOException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DownloadExcelImportItemsReport.name());
	    
		File exceltFile = null;

		Report report = importExportService.getFailedImportItemsReport(batchId);
		if (report != null && report.getReportData() != null) {
			exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
					(Object[][]) report.getReportData(), "Failed Import Report", null);
		}

		Path getPathFromServer = exceltFile.toPath();
		// checking if file exists in path
		if (getPathFromServer.toFile() == null || !getPathFromServer.toFile().exists()) {
			return;
		}

		String mimeType = URLConnection.guessContentTypeFromName(getPathFromServer.getFileName().toString());
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + getPathFromServer.getFileName().toString() + "\""));

		response.setContentLength((int) getPathFromServer.toFile().length());
		response.setHeader("recruiz-file-name", getPathFromServer.getFileName().toString());
		Files.copy(getPathFromServer, response.getOutputStream());

	}

	/**
	 * Get sample Excel file for import data
	 * 
	 * @param response
	 * @throws RecruizException
	 * @throws IOException
	 */
	@RequestMapping(value = "/api/v1/import/data/sample/download", headers = "Accept=*/*", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void getSampleBulkUploadFile(HttpServletResponse response,
			@RequestParam(value = "importType") String importType) throws RecruizException, IOException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetSampleBulkUploadFile.name());
	    
		InputStream fileStream = null;
		switch (importType) {
		case GlobalConstants.CLIENTS:
		case GlobalConstants.DEPARTMENTS:
			fileStream = resourceloader.getResource(GlobalConstants.SAMPLE_IMPORT_CLIENT_DATA_FILE).getInputStream();
			break;
		case GlobalConstants.POSITIONS:
			fileStream = resourceloader.getResource(GlobalConstants.SAMPLE_IMPORT_POSITION_DATA_FILE).getInputStream();
			break;
		case GlobalConstants.CANDIDATES:
			fileStream = resourceloader.getResource(GlobalConstants.SAMPLE_IMPORT_CANDIDATE_DATA_FILE).getInputStream();
			break;
		case GlobalConstants.PROSPECTS:
			fileStream = resourceloader.getResource(GlobalConstants.SAMPLE_IMPORT_PROSPECT_DATA_FILE).getInputStream();
			break;
		}
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader("Content-Disposition",
				String.format("attachment; filename=\"%s\"", "sample-data-format.xlsx"));
		response.setHeader("recruiz-file-name", "sample-data-format.xlsx");
		// writing file into stream and download
		IOUtils.copy(fileStream, response.getOutputStream());
		response.flushBuffer();

	}

	/**
	 * API to download the exported zip file
	 * 
	 * @author Akshay
	 * @param response
	 * @param fileName
	 * @throws IOException
	 * @throws RecruizException
	 */
	
	@RequestMapping(value = "/pub/export/data/{fileName}", headers = "Accept=*/*", method = RequestMethod.GET)
	public void downloadExportedData(HttpServletResponse response, @PathVariable("fileName") String fileName)
			throws IOException, RecruizException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				"Not available", "Not available",
				UsageActionType.DownloadExportedData.name());
	    
		Path path = Paths.get(rootFolderPath + File.separator + fileName + ".zip");

		// checking if file exists in path
		if (path.toFile() == null || !path.toFile().exists()) {
			return;
		}
		String mimeType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		response.setContentType(mimeType);
		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + path.getFileName().toString() + "\""));
		response.setContentLength((int) path.toFile().length());
		// writing file into stream and download
		Files.copy(Paths.get(rootFolderPath + File.separator + fileName + ".zip"), response.getOutputStream());
	}

	/**
	 * get import data stats
	 * 
	 * @param pageNo
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	
	@RequestMapping(value = "/api/v1/import/data/stat", method = RequestMethod.GET)
	public RestResponse getImportDataStat(@RequestParam(value = "pageNo", required = false) String pageNo)
			throws RecruizException, ParseException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetImportDataStat.name());
	    
		Page<ImportJobBatch> importJobBatches = importJobBatchService.findByOwner(userService.getLoggedInUserEmail(),
				pageableService.getPageRequestObject(pageNo, null));

		RestResponse response = new RestResponse(RestResponse.SUCCESS, importJobBatches);
		return response;
	}

	/**
	 * Get import upload items stats for failed rows
	 * 
	 * @param pageNo
	 * @param batchId
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/import/data/items/stat", method = RequestMethod.GET)
	public RestResponse getImportUploadItemsStat(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam("batchId") String batchId) throws RecruizException, ParseException {

		 // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetImportUploadItemsStat.name());
	    
		Page<ImportJobUploadItem> importJobUploadItems = importJobUploadItemService.findByBatchId(batchId,
				pageableService.getPageRequestObject(pageNo, null));

		RestResponse response = new RestResponse(RestResponse.SUCCESS, importJobUploadItems);
		return response;
	}

}
