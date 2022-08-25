package com.bbytes.recruiz.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.CandidateNotes;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ImportJobBatch;
import com.bbytes.recruiz.domain.ImportJobUploadItem;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectNotes;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CategoryOptions;
import com.bbytes.recruiz.enums.DefaultRounds;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.IndustryOptions;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.utils.CandidateFileHeaderConstant;
import com.bbytes.recruiz.utils.ClientFileHeaderConstant;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PositionFileHeaderConstant;
import com.bbytes.recruiz.utils.ProspectFileHeaderConstant;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.UserFileHeaderConstant;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fdsapi.ResultSetConverter;
import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Import Export Service
 * 
 * @author Akshay
 *
 */
@Service
public class ImportExportService {

	private static Logger logger = LoggerFactory.getLogger(ImportExportService.class);

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH-mm-ss";

	private static final String FILE_HEADERS = "fileHeaders";

	private static final String FILE_PATH = "filePath";

	private static final String IMPORT_TYPE = "importType";

	private static final String HEADER_CONSTANTS = "headerConstants";

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private VelocityEngine templateEngine;

	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private ProspectService prospectService;

	@Autowired
	private ProspectNotesService prospectNotesService;

	@Autowired
	private FileService fileService;

	@Autowired
	private ImportJobBatchService importJobBatchService;

	@Autowired
	private ImportJobUploadItemService importJobUploadItemService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	public static int defaultPageSize = 100;

	@Resource
	private Environment environment;

	@Value("${export.folderPath.path}")
	private String exportDataRootPath;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${dummy.resume.pdf.path}")
	private String candidateDummyResumeFilePath;

	/**
	 * Exporting client, position and candidate data
	 * 
	 * @param tenantId
	 * @param isExportFile
	 * @param loggendInUser
	 * @return
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	public String exportData(String tenantId, boolean isExportFile, User loggendInUser) throws IOException {

		String targetZipName = null;

		try {
			String dateString = DateTime.now().toString(DATE_TIME_FORMAT);
			// generating random string
			String randomKey = com.bbytes.recruiz.utils.StringUtils.randomString();
			String exportId = dateString + "-" + randomKey;
			targetZipName = tenantId + "_" + exportId;

			// calling method for exporting client data
			exportingClientData(exportId, loggendInUser.getOrganization().getOrgType());

			// calling method for exporting position data
			exportingPositionData(exportId, loggendInUser.getOrganization().getOrgType());

			// calling method for exporting candidate data
			exportingCandidateData(exportId, isExportFile);

			// calling method for exporting prospect data
			exportingProspectData(exportId, loggendInUser.getOrganization().getOrgType());

			// zipping all export data
			fileService.zip(exportDataRootPath + File.separator + tenantId + "_" + exportId,
					exportDataRootPath + File.separator + targetZipName + ".zip");
		} finally {
			// making export job task done
			resumeBulkTenantState.setExportJobTaskDone(tenantId);
		}

		return targetZipName;

	}

	/**
	 * @param exportId
	 * @param candidateList
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	private void exportingCandidateData(String exportId, boolean isExportFile) throws IOException {

		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		String[] header;
		File excelFile = null;

		try {
			Page<Candidate> candidateList = getPageableCandidate(0);

			if (candidateList.getTotalElements() <= 0)
				return;

			excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.CANDIDATE_DATA_FILE_NAME);

			// creating excel workbook and excel worksheet object
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet(GlobalConstants.CANDIDATE_DATA_FILE_NAME);

			header = new String[] { "Full Name", "Email", "Mobile No", "Alternate Email", "Alternate Mobile No", "Current Location",
					"Current Company", "Current Designation", "Total Experience (Years)", "Employement Type", "Highest Qualification",
					"Current CTC", "Expected CTC", "Key Skills", "Resume Link", "Notice Period (Days)", "Serving Notice Period",
					"Last working Day", "Preferred Location", "Date of Birth", "Source", "Sourced Date", "Source Details", "Sourced By",
					"Gender", "Nationality", "Marital Status", "Languages", "Category", "Sub Category",
					"Average Stay In Company ( in months)", "Longest Stay In Company ( in months)", "Academic Qualification 1",
					"University/Institute 1", "Academic Qualification 2", "University/Institute 2", "Academic Qualification 3",
					"University/Institute 3", "Communication", "LinkedIn Profile", "Facebook Profile", "Twitter Profile", "Github Profile",
					"Comments", "Notes 1", "Notes 2", "Notes 3", "Notes 4", "Notes 5" };

			int currentRow = 0;
			Row row = sheet.createRow(currentRow);

			int numCols = writeHeaderInExcel(header, row);

			for (int i = 1; i <= candidateList.getTotalPages(); i++) {
				currentRow = getCandidateExcel(candidateList.getContent(), exportId, isExportFile, header, row, sheet, currentRow);
				candidateList = getPageableCandidate(i);
			}

			// Autosize columns( resize column according to column name)
			for (int i = 0; i < numCols; i++) {
				sheet.autoSizeColumn((short) i);
			}

			// stream writing into workbook file
			fileOutputStream = new FileOutputStream(excelFile);
			workbook.write(fileOutputStream);

		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (fileOutputStream != null)
				fileOutputStream.close();
			if (workbook != null)
				workbook.close();
		}
	}

	/**
	 * 
	 * @param exportId
	 * @param isExportFile
	 * @param orgType
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	private void exportingPositionData(String exportId, String orgType) throws IOException {

		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		String[] header;
		File excelFile = null;

		try {
			Page<Position> positionList = getPageablePosition(0);

			if (positionList.getTotalElements() <= 0)
				return;

			if (GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(orgType)) {
				excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.POSITION_DATA_FILE_NAME);

				// creating excel workbook and excel worksheet object
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(GlobalConstants.POSITION_DATA_FILE_NAME);

				header = new String[] { "Position Name", "Client Name", "Location", "Total Openings", "Created Date", "Job Description",
						"Industry", "Category", "Job Type", "Close By Date", "Education Qualification", "Experience Range",
						"Remote Work option", "Required Skill Set", "Good Skill Set", "CTC Range", "Job Url", "Notes", "Created By" };
			} else {
				excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.POSITION_DATA_FILE_NAME);

				// creating excel workbook and excel worksheet object
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(GlobalConstants.POSITION_DATA_FILE_NAME);

				header = new String[] { "Position Name", "Department Name", "Location", "Total Openings", "Created Date", "Job Description",
						"Industry", "Category", "Job Type", "Close By Date", "Education Qualification", "Experience Range",
						"Remote Work option", "Required Skill Set", "Good Skill Set", "CTC Range", "Job Url", "Notes", "Created By" };
			}

			int currentRow = 0;
			Row row = sheet.createRow(currentRow);

			int numCols = writeHeaderInExcel(header, row);

			for (int i = 1; i <= positionList.getTotalPages(); i++) {
				currentRow = getPositionExcel(positionList.getContent(), header, row, sheet, currentRow);
				positionList = getPageablePosition(i);
			}

			// Autosize columns( resize column according to column name)
			for (int i = 0; i < numCols; i++) {
				sheet.autoSizeColumn((short) i);
			}

			// stream writing into workbook file
			fileOutputStream = new FileOutputStream(excelFile);
			workbook.write(fileOutputStream);

		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (fileOutputStream != null)
				fileOutputStream.close();
			if (workbook != null)
				workbook.close();
		}
	}

	/**
	 * 
	 * @param exportId
	 * @param orgType
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	private void exportingClientData(String exportId, String orgType) throws IOException {

		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		String[] header;
		File excelFile = null;

		try {

			Page<Client> clientList = getPageableClient(0);

			if (clientList.getTotalElements() <= 0)
				return;

			if (GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(orgType)) {
				excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.CLIENT_DATA_FILE_NAME);

				// creating excel workbook and excel worksheet object
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(GlobalConstants.CLIENT_DATA_FILE_NAME);
				header = new String[] { "Client Name", "Address", "Location", "Website", "Employee Size", "Turn Over", "About Client",
						"Created Date" };
			} else {
				excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.DEPARTMENT_DATA_FILE_NAME);

				// creating excel workbook and excel worksheet object
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet(GlobalConstants.DEPARTMENT_DATA_FILE_NAME);
				header = new String[] { "Department Name", "Address", "Location", "Website", "Employee Size", "Turn Over",
						"About Department", "Created Date" };
			}

			int currentRow = 0;
			Row row = sheet.createRow(currentRow);

			int numCols = writeHeaderInExcel(header, row);

			for (int i = 1; i <= clientList.getTotalPages(); i++) {
				currentRow = getClientExcel(clientList.getContent(), header, row, sheet, currentRow);
				clientList = getPageableClient(i);
			}

			// Autosize columns( resize column according to column name)
			for (int i = 0; i < numCols; i++) {
				sheet.autoSizeColumn((short) i);
			}

			// stream writing into workbook file
			fileOutputStream = new FileOutputStream(excelFile);
			workbook.write(fileOutputStream);

		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (fileOutputStream != null)
				fileOutputStream.close();
			if (workbook != null)
				workbook.close();
		}
	}

	/**
	 * 
	 * @param exportId
	 * @param orgType
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	private void exportingProspectData(String exportId, String orgType) throws IOException {

		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		String[] header;
		File excelFile = null;

		try {

			Page<Prospect> prospectList = getPageableProspect(0);

			// empty prospect list and corporate account wont be export
			if (prospectList.getTotalElements() <= 0 || GlobalConstants.SIGNUP_MODE_CORPORATE.equalsIgnoreCase(orgType))
				return;

			excelFile = new File(createRootPath(exportId) + File.separator + GlobalConstants.PROSPECT_DATA_FILE_NAME);

			// creating excel workbook and excel worksheet object
			workbook = new XSSFWorkbook();
			sheet = workbook.createSheet(GlobalConstants.PROSPECT_DATA_FILE_NAME);
			header = new String[] { "Company Name", "Name", "Email", "Mobile No", "Designation", "Status", "Location", "Address", "Website",
					"Source", "Prospect Rating", "Created Date", "Created By", "Value", "Percentage", "Industry", "Category", "Note 1",
					"Note 2", "Note 3" };

			int currentRow = 0;
			Row row = sheet.createRow(currentRow);

			int numCols = writeHeaderInExcel(header, row);

			for (int i = 1; i <= prospectList.getTotalPages(); i++) {
				currentRow = getProspectExcel(prospectList.getContent(), header, row, sheet, currentRow);
				prospectList = getPageableProspect(i);
			}

			// Autosize columns( resize column according to column name)
			for (int i = 0; i < numCols; i++) {
				sheet.autoSizeColumn((short) i);
			}

			// stream writing into workbook file
			fileOutputStream = new FileOutputStream(excelFile);
			workbook.write(fileOutputStream);

		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (fileOutputStream != null)
				fileOutputStream.close();
			if (workbook != null)
				workbook.close();
		}
	}

	/**
	 * @param header
	 * @param row
	 * @return
	 */
	private int writeHeaderInExcel(String[] header, Row row) {

		int numCols = header.length;

		// writing header for excel document
		for (int i = 0; i < numCols; i++) {
			String title = header[i];
			Cell cell = row.createCell(i);
			cell.setCellValue(title);
		}
		return numCols;
	}

	@Transactional(readOnly = true)
	private Page<Client> getPageableClient(int pageNo) {
		Pageable Pageable = new PageRequest(pageNo, defaultPageSize);
		Page<Client> clientList = clientService.findAll(Pageable);
		return clientList;
	}

	private Page<Position> getPageablePosition(int pageNo) {
		Pageable Pageable = new PageRequest(pageNo, defaultPageSize);
		Page<Position> positionList = positionService.findAll(Pageable);
		return positionList;
	}

	@Transactional(readOnly = true)
	private Page<Candidate> getPageableCandidate(int pageNo) {
		Pageable Pageable = new PageRequest(pageNo, defaultPageSize);
		Page<Candidate> candidateList = candidateService.findAll(Pageable);
		return candidateList;
	}

	@Transactional(readOnly = true)
	private Page<Prospect> getPageableProspect(int pageNo) {
		Pageable Pageable = new PageRequest(pageNo, defaultPageSize);
		Page<Prospect> prospectList = prospectService.findAll(Pageable);
		return prospectList;
	}

	/**
	 * Excel writer for client data
	 * 
	 * @param clientList
	 * @param header
	 * @param row
	 * @param sheet
	 * @param currentRow
	 * @return
	 */
	private int getClientExcel(List<Client> clientList, String[] header, Row row, XSSFSheet sheet, int currentRow) {

		// writing result data for excel document
		if (clientList != null && !clientList.isEmpty()) {
			for (Client client : clientList) {
				row = sheet.createRow(++currentRow);
				int colNum = 0;
				row.createCell(colNum++).setCellValue(client.getClientName());
				row.createCell(colNum++).setCellValue(client.getAddress());
				row.createCell(colNum++).setCellValue(client.getClientLocation());
				row.createCell(colNum++).setCellValue(client.getWebsite());
				row.createCell(colNum++).setCellValue(client.getEmpSize());
				row.createCell(colNum++).setCellValue(client.getTurnOvr());
				String notes = "";
				if (client.getNotes() != null && client.getNotes().length() > 30000) {
					notes = client.getNotes().substring(0, 30000);
				}
				notes = client.getNotes();
				row.createCell(colNum++).setCellValue(notes);
				row.createCell(colNum++).setCellValue(DateUtil.formateDateAndTime(client.getCreationDate()));
			}
		}
		return currentRow;
	}

	/**
	 * Excel writer for prospect data
	 * 
	 * @param prospectList
	 * @param header
	 * @param row
	 * @param sheet
	 * @param currentRow
	 * @return
	 */
	private int getProspectExcel(List<Prospect> prospectList, String[] header, Row row, XSSFSheet sheet, int currentRow) {

		// writing result data for excel document
		if (prospectList != null && !prospectList.isEmpty()) {
			for (Prospect prospect : prospectList) {

				List<ProspectNotes> prospectNotes = prospectNotesService.getProspectNotesByModificationDateDesc(prospect);

				row = sheet.createRow(++currentRow);
				int colNum = 0;
				row.createCell(colNum++).setCellValue(prospect.getCompanyName());
				row.createCell(colNum++).setCellValue(prospect.getName());
				row.createCell(colNum++).setCellValue(prospect.getEmail());
				row.createCell(colNum++).setCellValue(prospect.getMobile());
				row.createCell(colNum++).setCellValue(prospect.getDesignation());
				row.createCell(colNum++).setCellValue(prospect.getStatus());
				row.createCell(colNum++).setCellValue(prospect.getLocation());
				row.createCell(colNum++).setCellValue(prospect.getAddress());
				row.createCell(colNum++).setCellValue(prospect.getWebsite());
				row.createCell(colNum++).setCellValue(prospect.getSource());
				row.createCell(colNum++).setCellValue(prospect.getProspectRating());
				row.createCell(colNum++).setCellValue(DateUtil.formateDateAndTime(prospect.getCreationDate()));
				row.createCell(colNum++).setCellValue(prospect.getOwner());
				row.createCell(colNum++).setCellValue(prospect.getCurrency() + StringUtils.SPACE + prospect.getDealSize());
				row.createCell(colNum++).setCellValue(prospect.getPercentage());
				row.createCell(colNum++).setCellValue(prospect.getIndustry());
				row.createCell(colNum++).setCellValue(prospect.getCategory());

				row.createCell(colNum++)
						.setCellValue(prospectNotes != null && !prospectNotes.isEmpty()
								? Jsoup.parse(prospectNotes.get(0).getNotes() != null ? prospectNotes.get(0).getNotes() : "").text()
								: "");

				row.createCell(colNum++)
						.setCellValue(prospectNotes != null && !prospectNotes.isEmpty() && prospectNotes.size() > 1
								? Jsoup.parse(prospectNotes.get(1).getNotes() != null ? prospectNotes.get(1).getNotes() : "").text()
								: "");

				row.createCell(colNum++)
						.setCellValue(prospectNotes != null && !prospectNotes.isEmpty() && prospectNotes.size() > 2
								? Jsoup.parse(prospectNotes.get(2).getNotes() != null ? prospectNotes.get(2).getNotes() : "").text()
								: "");
			}
		}
		return currentRow;
	}

	/**
	 * Excel writer for position data
	 * 
	 * @param positionList
	 * @param header
	 * @param row
	 * @param sheet
	 * @param currentRow
	 * @return
	 * @throws IOException
	 */
	private int getPositionExcel(List<Position> positionList, String[] header, Row row, XSSFSheet sheet, int currentRow)
			throws IOException {

		if (positionList != null && !positionList.isEmpty()) {
			for (Position position : positionList) {

				row = sheet.createRow(++currentRow);
				int colNum = 0;
				row.createCell(colNum++).setCellValue(position.getTitle());
				row.createCell(colNum++).setCellValue(position.getClient().getClientName());
				row.createCell(colNum++).setCellValue(position.getLocation());
				row.createCell(colNum++).setCellValue(position.getTotalPosition());
				row.createCell(colNum++).setCellValue(DateUtil.formateDateAndTime(position.getCreationDate()));
				row.createCell(colNum++)
						.setCellValue(Jsoup.parse(position.getDescription() != null ? position.getDescription() : "").text());
				row.createCell(colNum++).setCellValue(position.getIndustry());
				row.createCell(colNum++).setCellValue(position.getFunctionalArea());
				row.createCell(colNum++).setCellValue(position.getType());
				row.createCell(colNum++).setCellValue(DateUtil.formateDate(position.getCloseByDate()));
				row.createCell(colNum++).setCellValue(StringUtils.join(position.getEducationalQualification(), ','));
				row.createCell(colNum++).setCellValue(position.getExperienceRange());
				row.createCell(colNum++).setCellValue(position.isRemoteWork());
				row.createCell(colNum++).setCellValue(StringUtils.join(position.getReqSkillSet(), ','));
				row.createCell(colNum++).setCellValue(StringUtils.join(position.getGoodSkillSet(), ','));
				row.createCell(colNum++)
						.setCellValue(position.getSalUnit() + StringUtils.SPACE + position.getMinSal() + "-" + position.getMaxSal());
				row.createCell(colNum++).setCellValue(position.getPositionUrl());
				row.createCell(colNum++).setCellValue(position.getNotes());
				row.createCell(colNum++).setCellValue(position.getOwner());
			}
		}
		return currentRow;
	}

	/**
	 * Excel writer for candidate data
	 * 
	 * @param candidateList
	 * @param exportId
	 * @param isExportFile
	 * @param header
	 * @param row
	 * @param sheet
	 * @param currentRow
	 * @return
	 * @throws IOException
	 */
	private int getCandidateExcel(List<Candidate> candidateList, String exportId, boolean isExportFile, String[] header, Row row,
			XSSFSheet sheet, int currentRow) throws IOException {

		if (candidateList != null && !candidateList.isEmpty()) {
			for (Candidate candidate : candidateList) {

				File resumeFile = null;
				if (isExportFile) {
					resumeFile = exportCandidateFiles(exportId, candidate, resumeFile);
				}
				List<CandidateEducationDetails> candidateEducationDetails = new ArrayList<>(candidate.getEducationDetails());
				List<CandidateNotes> candidateNotes = new ArrayList<>(candidate.getNotes());

				row = sheet.createRow(++currentRow);
				int colNum = 0;
				row.createCell(colNum++).setCellValue(candidate.getFullName());
				row.createCell(colNum++).setCellValue(candidate.getEmail());
				row.createCell(colNum++).setCellValue(candidate.getMobile());
				row.createCell(colNum++).setCellValue(candidate.getAlternateEmail());
				row.createCell(colNum++).setCellValue(candidate.getAlternateMobile());
				row.createCell(colNum++).setCellValue(candidate.getCurrentLocation());
				row.createCell(colNum++).setCellValue(candidate.getCurrentCompany());
				row.createCell(colNum++).setCellValue(candidate.getCurrentTitle());
				row.createCell(colNum++).setCellValue(candidate.getTotalExp());
				row.createCell(colNum++).setCellValue(candidate.getEmploymentType());
				row.createCell(colNum++).setCellValue(candidate.getHighestQual());
				row.createCell(colNum++).setCellValue(candidate.getCurrentCtc());
				row.createCell(colNum++).setCellValue(candidate.getExpectedCtc());
				row.createCell(colNum++).setCellValue(StringUtils.join(candidate.getKeySkills(), ","));
				row.createCell(colNum++).setCellValue(
						resumeFile != null
								? "." + File.separator + GlobalConstants.CANDIDATES + File.separator + candidate.getCid() + File.separator
										+ FileType.Original_Resume.getDisplayName() + File.separator + resumeFile.getName()
								: "");
				row.createCell(colNum++).setCellValue(candidate.getNoticePeriod());
				row.createCell(colNum++).setCellValue(candidate.isNoticeStatus());
				row.createCell(colNum++).setCellValue(DateUtil.formateDate(candidate.getLastWorkingDay()));
				row.createCell(colNum++).setCellValue(candidate.getPreferredLocation());
				row.createCell(colNum++).setCellValue(DateUtil.formateDate(candidate.getDob()));
				row.createCell(colNum++).setCellValue(candidate.getSource());
				row.createCell(colNum++).setCellValue(DateUtil.formateDate(candidate.getSourcedOnDate()));
				row.createCell(colNum++).setCellValue(candidate.getSourceDetails());
				row.createCell(colNum++).setCellValue(candidate.getOwner());
				row.createCell(colNum++).setCellValue(candidate.getGender());
				row.createCell(colNum++).setCellValue(candidate.getNationality());
				row.createCell(colNum++).setCellValue(candidate.getMaritalStatus());
				row.createCell(colNum++).setCellValue(candidate.getLanguages());
				row.createCell(colNum++).setCellValue(candidate.getCategory());
				row.createCell(colNum++).setCellValue(candidate.getSubCategory());
				row.createCell(colNum++).setCellValue(candidate.getAverageStayInCompany());
				row.createCell(colNum++).setCellValue(candidate.getLongestStayInCompany());
				row.createCell(colNum++)
						.setCellValue(candidateEducationDetails != null && !candidateEducationDetails.isEmpty()
								? candidateEducationDetails.get(0).getBoard()
								: "");
				row.createCell(colNum++)
						.setCellValue(candidateEducationDetails != null && !candidateEducationDetails.isEmpty()
								? candidateEducationDetails.get(0).getCollege()
								: "");
				row.createCell(colNum++).setCellValue(
						candidateEducationDetails != null && !candidateEducationDetails.isEmpty() && candidateEducationDetails.size() > 1
								? candidateEducationDetails.get(1).getBoard()
								: "");
				row.createCell(colNum++).setCellValue(
						candidateEducationDetails != null && !candidateEducationDetails.isEmpty() && candidateEducationDetails.size() > 1
								? candidateEducationDetails.get(1).getCollege()
								: "");
				row.createCell(colNum++).setCellValue(
						candidateEducationDetails != null && !candidateEducationDetails.isEmpty() && candidateEducationDetails.size() > 2
								? candidateEducationDetails.get(2).getBoard()
								: "");
				row.createCell(colNum++).setCellValue(
						candidateEducationDetails != null && !candidateEducationDetails.isEmpty() && candidateEducationDetails.size() > 2
								? candidateEducationDetails.get(2).getCollege()
								: "");
				row.createCell(colNum++).setCellValue(candidate.getCommunication());
				row.createCell(colNum++).setCellValue(candidate.getLinkedinProf());
				row.createCell(colNum++).setCellValue(candidate.getFacebookProf());
				row.createCell(colNum++).setCellValue(candidate.getTwitterProf());
				row.createCell(colNum++).setCellValue(candidate.getGithubProf());
				row.createCell(colNum++).setCellValue(candidate.getComments());
				row.createCell(colNum++)
						.setCellValue(candidateNotes != null && !candidateNotes.isEmpty()
								? Jsoup.parse(candidateNotes.get(0).getNotes() != null ? candidateNotes.get(0).getNotes() : "").text()
								: "");
				row.createCell(colNum++)
						.setCellValue(candidateNotes != null && !candidateNotes.isEmpty() && candidateNotes.size() > 1
								? Jsoup.parse(candidateNotes.get(1).getNotes() != null ? candidateNotes.get(1).getNotes() : "").text()
								: "");
				row.createCell(colNum++)
						.setCellValue(candidateNotes != null && !candidateNotes.isEmpty() && candidateNotes.size() > 2
								? Jsoup.parse(candidateNotes.get(2).getNotes() != null ? candidateNotes.get(2).getNotes() : "").text()
								: "");
				row.createCell(colNum++)
						.setCellValue(candidateNotes != null && !candidateNotes.isEmpty() && candidateNotes.size() > 3
								? Jsoup.parse(candidateNotes.get(3).getNotes() != null ? candidateNotes.get(3).getNotes() : "").text()
								: "");
				row.createCell(colNum++)
						.setCellValue(candidateNotes != null && !candidateNotes.isEmpty() && candidateNotes.size() > 4
								? Jsoup.parse(candidateNotes.get(4).getNotes() != null ? candidateNotes.get(4).getNotes() : "").text()
								: "");
			}

		}
		return currentRow;
	}

	/**
	 * Exporting all files related to candidates
	 * 
	 * @param exportId
	 * @param candidate
	 * @param resumeFile
	 * @return
	 * @throws IOException
	 */
	private File exportCandidateFiles(String exportId, Candidate candidate, File resumeFile) throws IOException {
		String path;
		List<CandidateFile> candidateFiles = candidateFileService.getCandidateFile(String.valueOf(candidate.getCid()));

		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile file : candidateFiles) {
				if (file != null && file.getFilePath() != null && !file.getFilePath().isEmpty()) {
					try {
						if (file.getFilePath().startsWith("http")) {

							File docsFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), file.getFilePath());

							if (docsFile != null && docsFile.exists()) {
								String docsPath = createFolderStructureForExportData(candidate.getCid() + "", file.getFileType(), exportId);
								Files.copy(docsFile.toPath(), new File(docsPath + File.separator + docsFile.getName()).toPath(),
										StandardCopyOption.REPLACE_EXISTING);

								// deleting file from temp folder after getting
								// from
								// s3
								docsFile.delete();
							}
						} else {

							File docsFile = new File(file.getFilePath());
							if (docsFile.exists()) {
								String docsPath = createFolderStructureForExportData(candidate.getCid() + "", file.getFileType(), exportId);
								FileUtils.copyFile(new File(file.getFilePath()), new File(docsPath + File.separator + docsFile.getName()));
							}
						}
					} catch (Exception e) {
						logger.warn("##### Export Candidate Failure (Files) ####" + e.getMessage());
					}

				}
			}
		}

		if (candidate.getResumeLink() != null && !candidate.getResumeLink().isEmpty()) {

			try {
				if (candidate.getResumeLink().startsWith("http")) {
					File file = s3DownloadClient.getS3File(fileService.getTenantBucket(), candidate.getResumeLink());
					path = createFolderStructureForExportData(candidate.getCid() + "", GlobalConstants.RESUME, exportId);
					Files.copy(file.toPath(), new File(path + File.separator + file.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				} else {
					resumeFile = new File(candidate.getResumeLink());
				}

			} catch (Exception e) {
				logger.warn("##### Export Candidate Failure (Resume) ####" + e.getMessage());
			}

		}
		return resumeFile;
	}

	/**
	 * Getting headermap of updated import file
	 * 
	 * @param importFile
	 * @param importType
	 * @return
	 * @throws RecruizException
	 */
	public Map<String, Object> getUploadedFileHeaderMap(File importFile, String importType) throws RecruizException {

		String fileExtension = FilenameUtils.getExtension(importFile.getName());

		// storing imported file into server for further import work
		String getFilePath = fileService.copyFileToRecruizExportFolder(importFile);

		Map<String, Object> headerMap = new LinkedHashMap<String, Object>();

		switch (fileExtension) {
		case "xls":
		case "xlsx":

			if (GlobalConstants.CLIENTS.equals(importType) || GlobalConstants.DEPARTMENTS.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;
			} else if (GlobalConstants.POSITIONS.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;
			} else if (GlobalConstants.CANDIDATES.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;

			} else if (GlobalConstants.USERS.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;

			} else if (GlobalConstants.PROSPECTS.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;

			}
		case "csv":

			if (GlobalConstants.CLIENTS.equals(importType)) {

				List<String> columnList = getHeaderListFromCSV(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;
			} else if (GlobalConstants.POSITIONS.equals(importType)) {

				List<String> columnList = getHeaderListFromCSV(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;
			} else if (GlobalConstants.USERS.equals(importType)) {

				List<String> columnList = getHeaderListFromCSV(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;

			} else if (GlobalConstants.PROSPECTS.equals(importType)) {

				List<String> columnList = getHeaderListFromExcel(importFile);
				List<BaseDTO> getHeaderMap = getHeaderMap(columnList, importType);

				headerMap.put(HEADER_CONSTANTS, getHeaderList(importType));
				headerMap.put(FILE_HEADERS, getHeaderMap);
				headerMap.put(FILE_PATH, getFilePath);
				headerMap.put(IMPORT_TYPE, importType);
				return headerMap;

			}
		default:
			return null;
		}
	}

	private List<String> getHeaderList(String importType) throws RecruizException {

		List<String> headerList = new LinkedList<String>();

		switch (importType) {
		case GlobalConstants.CLIENTS:
		case GlobalConstants.DEPARTMENTS:

			headerList.add(ClientFileHeaderConstant.Not_Applicable);
			headerList.add(ClientFileHeaderConstant.Client_Name);
			headerList.add(ClientFileHeaderConstant.Address);
			headerList.add(ClientFileHeaderConstant.Location);
			headerList.add(ClientFileHeaderConstant.Website);
			headerList.add(ClientFileHeaderConstant.Employee_Strength);
			headerList.add(ClientFileHeaderConstant.Turnover);
			headerList.add(ClientFileHeaderConstant.About);

			break;

		case GlobalConstants.POSITIONS:

			headerList.add(PositionFileHeaderConstant.Not_Applicable);
			headerList.add(PositionFileHeaderConstant.Client_Name);
			headerList.add(PositionFileHeaderConstant.Position_Name);
			headerList.add(PositionFileHeaderConstant.Position_Code);
			headerList.add(PositionFileHeaderConstant.Close_by_Date);
			headerList.add(PositionFileHeaderConstant.Number_of_Openings);
			headerList.add(PositionFileHeaderConstant.Location);
			headerList.add(PositionFileHeaderConstant.Job_Description);
			headerList.add(PositionFileHeaderConstant.Educational_Qualification);
			headerList.add(PositionFileHeaderConstant.Min_Experience);
			headerList.add(PositionFileHeaderConstant.Max_Experience);
			headerList.add(PositionFileHeaderConstant.Salary_Currency);
			headerList.add(PositionFileHeaderConstant.Min_Salary);
			headerList.add(PositionFileHeaderConstant.Max_Salary);
			headerList.add(PositionFileHeaderConstant.Industry);
			headerList.add(PositionFileHeaderConstant.Category);
			headerList.add(PositionFileHeaderConstant.Job_Type);
			headerList.add(PositionFileHeaderConstant.Remote_Work);
			headerList.add(PositionFileHeaderConstant.Skill_Set);
			headerList.add(PositionFileHeaderConstant.Job_URL);
			headerList.add(PositionFileHeaderConstant.Good_Skill_Set);
			headerList.add(PositionFileHeaderConstant.Notes);

			break;

		case GlobalConstants.CANDIDATES:

			headerList.add(CandidateFileHeaderConstant.Not_Applicable);
			headerList.add(CandidateFileHeaderConstant.Full_Name);
			headerList.add(CandidateFileHeaderConstant.Email_Address);
			headerList.add(CandidateFileHeaderConstant.Mobile_No);
			headerList.add(CandidateFileHeaderConstant.Alternate_Email);
			headerList.add(CandidateFileHeaderConstant.Alternate_Mobile);
			headerList.add(CandidateFileHeaderConstant.Current_Company);
			headerList.add(CandidateFileHeaderConstant.Current_Title);
			headerList.add(CandidateFileHeaderConstant.Current_Location);
			headerList.add(CandidateFileHeaderConstant.Highest_Qualification);
			headerList.add(CandidateFileHeaderConstant.Total_Experience);
			headerList.add(CandidateFileHeaderConstant.Employment_Type);
			headerList.add(CandidateFileHeaderConstant.Date_of_Birth);
			headerList.add(CandidateFileHeaderConstant.Current_CTC);
			headerList.add(CandidateFileHeaderConstant.Expected_CTC);
			headerList.add(CandidateFileHeaderConstant.Notice_Period);
			headerList.add(CandidateFileHeaderConstant.Preferred_Location);
			headerList.add(CandidateFileHeaderConstant.Nationality);
			headerList.add(CandidateFileHeaderConstant.Marital_Status);
			headerList.add(CandidateFileHeaderConstant.Languages);
			headerList.add(CandidateFileHeaderConstant.Category);
			headerList.add(CandidateFileHeaderConstant.Sub_Category);
			headerList.add(CandidateFileHeaderConstant.Average_Stay_In_Company);
			headerList.add(CandidateFileHeaderConstant.Longest_Stay_In_Company);
			headerList.add(CandidateFileHeaderConstant.Last_Working_Day);
			headerList.add(CandidateFileHeaderConstant.Previous_Employment);
			headerList.add(CandidateFileHeaderConstant.Industry);
			headerList.add(CandidateFileHeaderConstant.Address);
			headerList.add(CandidateFileHeaderConstant.Serving_Notice_Period);
			headerList.add(CandidateFileHeaderConstant.Source);
			headerList.add(CandidateFileHeaderConstant.Source_Details);
			headerList.add(CandidateFileHeaderConstant.Sourced_Date);
			headerList.add(CandidateFileHeaderConstant.Key_Skills);
			headerList.add(CandidateFileHeaderConstant.Gender);
			headerList.add(CandidateFileHeaderConstant.AcademicQualification1);
			headerList.add(CandidateFileHeaderConstant.University_Institute1);
			headerList.add(CandidateFileHeaderConstant.AcademicQualification2);
			headerList.add(CandidateFileHeaderConstant.University_Institute2);
			headerList.add(CandidateFileHeaderConstant.AcademicQualification3);
			headerList.add(CandidateFileHeaderConstant.University_Institute3);
			headerList.add(CandidateFileHeaderConstant.Communication);
			headerList.add(CandidateFileHeaderConstant.LinkedIn_Profile);
			headerList.add(CandidateFileHeaderConstant.Github_Profile);
			headerList.add(CandidateFileHeaderConstant.Facebook_Profile);
			headerList.add(CandidateFileHeaderConstant.Twitter_Profile);
			headerList.add(CandidateFileHeaderConstant.Comments);
			headerList.add(CandidateFileHeaderConstant.Last_Active);
			headerList.add(CandidateFileHeaderConstant.Notes1);
			headerList.add(CandidateFileHeaderConstant.Notes2);
			headerList.add(CandidateFileHeaderConstant.Notes3);
			headerList.add(CandidateFileHeaderConstant.Notes4);
			headerList.add(CandidateFileHeaderConstant.Notes5);

			break;

		case GlobalConstants.USERS:

			headerList.add(UserFileHeaderConstant.Name);
			headerList.add(UserFileHeaderConstant.Email_Address);
			headerList.add(UserFileHeaderConstant.Role_Name);

			break;

		case GlobalConstants.PROSPECTS:

			headerList.add(ProspectFileHeaderConstant.Prospect_Name);
			headerList.add(ProspectFileHeaderConstant.Company_Name);
			headerList.add(ProspectFileHeaderConstant.Email_Address);
			headerList.add(ProspectFileHeaderConstant.Mobile_No);
			headerList.add(ProspectFileHeaderConstant.Designation);
			headerList.add(ProspectFileHeaderConstant.Location);
			headerList.add(ProspectFileHeaderConstant.Address);
			headerList.add(ProspectFileHeaderConstant.Website);
			headerList.add(ProspectFileHeaderConstant.Source);
			headerList.add(ProspectFileHeaderConstant.Industry);
			headerList.add(ProspectFileHeaderConstant.Category);
			headerList.add(ProspectFileHeaderConstant.Prospect_Rating);
			headerList.add(ProspectFileHeaderConstant.Notes1);
			headerList.add(ProspectFileHeaderConstant.Notes2);
			headerList.add(ProspectFileHeaderConstant.Notes3);

			break;
		}

		return headerList;
	}

	/**
	 * This method starts the import data using Excel or CSV file
	 * 
	 * @param importFile
	 * @param headerMap
	 * @param importType
	 * @param batchId
	 * @param loggedInUser
	 * @throws RecruizException
	 */
	public void startImportData(File importFile, Map<String, String> headerMap, String importType, String batchId, User loggedInUser)
			throws RecruizException {

		String fileExtension = FilenameUtils.getExtension(importFile.getName());

		switch (fileExtension) {
		case "csv":
			importDataByCSV(importFile, headerMap, batchId, importType, loggedInUser);
		case "xls":
		case "xlsx":
			importDataByExcel(importFile, headerMap, batchId, importType, loggedInUser);
		}
	}

	/**
	 * Return header list of excel import file
	 * 
	 * @param importFile
	 * @return
	 * @throws RecruizException
	 */
	private List<String> getHeaderListFromExcel(File importFile) throws RecruizException {

		List<String> columnList = new LinkedList<String>();
		try {
			Workbook workbook;
			FileInputStream excelFile = new FileInputStream(importFile);

			String fileExtension = FilenameUtils.getExtension(importFile.getName());

			if ("xls".equals(fileExtension)) {
				// Get the workbook instance for XLS file
				workbook = new HSSFWorkbook(excelFile);
			} else {
				// Get the workbook instance for XLSX file
				workbook = new XSSFWorkbook(excelFile);
			}

			// Get first sheet from the workbook
			Sheet workSheet = workbook.getSheetAt(0);

			// Get first row
			Row firstRow = workSheet.getRow(0);
			// get the first column index for a row
			short minColIx = firstRow.getFirstCellNum();
			// get the last column index for a row
			short maxColIx = firstRow.getLastCellNum();
			// loop from first to last index
			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				// get the cell
				Cell cell = firstRow.getCell(colIx);
				// add the cell contents (name of column) to the list
				if (cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty())
					columnList.add(cell.getStringCellValue());
			}
			workbook.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.IMPORT_DATA_FAILED);
		}
		return columnList;
	}

	/**
	 * Return header list using CSV file
	 * 
	 * @param importFile
	 * @return
	 * @throws RecruizException
	 */
	private List<String> getHeaderListFromCSV(File importFile) throws RecruizException {

		List<String> columnList = new LinkedList<String>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		String[] headerRow = new String[50];
		try {
			br = new BufferedReader(new FileReader(importFile));

			// reading csv file line by line
			while ((line = br.readLine()) != null) {

				// use semicolon as separator
				headerRow = Arrays.copyOf(line.split(cvsSplitBy), line.split(cvsSplitBy).length);

				try {
					for (int iterator = 0; iterator < headerRow.length; iterator++) {
						if (headerRow[iterator] != null) {
							columnList.add(headerRow[iterator]);
						}
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				break;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.IMPORT_DATA_FAILED);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.getMessage();
				}
			}
		}
		return columnList;
	}

	private List<BaseDTO> getHeaderMap(List<String> columnList, String importType) {

		Map<String, String> headerMap = new LinkedHashMap<String, String>();

		switch (importType) {
		case GlobalConstants.CLIENTS:
		case GlobalConstants.DEPARTMENTS:

			for (String column : columnList) {

				headerMap = getClientHeaderMap(headerMap, column);
				if (headerMap.get(column) == null || headerMap.get(column).isEmpty())
					headerMap.put(column, column);

			}
			break;
		case GlobalConstants.POSITIONS:

			for (String column : columnList) {

				headerMap = getPositionHeaderMap(headerMap, column);
				if (headerMap.get(column) == null || headerMap.get(column).isEmpty())
					headerMap.put(column, column);

			}
			break;
		case GlobalConstants.CANDIDATES:

			for (String column : columnList) {

				headerMap = getCandidateHeaderMap(headerMap, column);

				if (headerMap.get(column) == null || headerMap.get(column).isEmpty())
					headerMap.put(column, column);

			}
			break;
		case GlobalConstants.USERS:

			for (String column : columnList) {

				headerMap = getUserHeaderMap(headerMap, column);

				if (headerMap.get(column) == null || headerMap.get(column).isEmpty())
					headerMap.put(column, column);

			}
			break;
		case GlobalConstants.PROSPECTS:

			for (String column : columnList) {

				headerMap = getUserHeaderMap(headerMap, column);

				if (headerMap.get(column) == null || headerMap.get(column).isEmpty())
					headerMap.put(column, column);

			}
			break;
		}

		List<BaseDTO> headerMapList = dataModelToDTOConversionService.convertRolesToEntityDTOList(headerMap);

		return headerMapList;

	}

	/**
	 * @param headerMap
	 * @param column
	 */
	private Map<String, String> getUserHeaderMap(Map<String, String> headerMap, String column) {

		String[] nameWords = { "full name", "name" };

		String[] emailWords = { "emailaddress", "email id", "email", "mail id", "mail", "email address", "mail address" };

		String[] roleWords = { "role name", "role" };

		for (int i = 0; i <= nameWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(nameWords[i].toLowerCase()) != -1) {

				headerMap.put(column, UserFileHeaderConstant.Name);
				break;
			}
		}

		for (int i = 0; i <= emailWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(emailWords[i].toLowerCase()) != -1) {

				headerMap.put(column, UserFileHeaderConstant.Email_Address);
				break;
			}
		}

		for (int i = 0; i <= roleWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(roleWords[i].toLowerCase()) != -1) {

				headerMap.put(column, UserFileHeaderConstant.Role_Name);
				break;
			}
		}

		return headerMap;
	}

	/**
	 * @param headerMap
	 * @param column
	 */
	private Map<String, String> getClientHeaderMap(Map<String, String> headerMap, String column) {

		String[] nameWords = { "client", "department", "client name", "department name", "name", "client/department name" };

		String[] locationWords = { "location", "current location" };

		String[] addressWords = { "address", "current address", "postal address" };

		String[] websiteWords = { "website", "website name", "weburl", "company website", "company url" };

		String[] empSizeWords = { "employee strength of company", "employee strength", "strength", "employee size", "emp size",
				"company size", "size" };

		String[] turnoverWords = { "turnover", "revenue", "turn over" };

		String[] aboutWords = { "about", "about company" };

		for (int i = 0; i <= nameWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(nameWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Client_Name);
				break;
			}
		}

		for (int i = 0; i <= addressWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(addressWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Address);
				break;
			}
		}

		for (int i = 0; i <= locationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(locationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Location);
				break;
			}
		}

		for (int i = 0; i <= websiteWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(websiteWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Website);
				break;
			}
		}

		for (int i = 0; i <= empSizeWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(empSizeWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Employee_Strength);
				break;
			}
		}

		for (int i = 0; i <= turnoverWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(turnoverWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.Turnover);
				break;
			}
		}
		for (int i = 0; i <= aboutWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(aboutWords[i].toLowerCase()) != -1) {

				headerMap.put(column, ClientFileHeaderConstant.About);
				break;
			}
		}
		return headerMap;
	}

	/**
	 * @param headerMap
	 * @param column
	 */
	private Map<String, String> getPositionHeaderMap(Map<String, String> headerMap, String column) {

		String[] clientNameWords = { "client", "department", "client name", "department name", "client/department name" };

		String[] nameWords = { "position", "position name" };

		String[] codeWords = { "positioncode", "position code", "code", "job code" };

		String[] dateWords = { "close by date", "close date", "position close on", "close on" };

		String[] numberWords = { "number of openings", "total openings", "total", "total jobs" };

		String[] locationWords = { "location", "current location" };

		String[] descWords = { "job description", "description", "details", "job details" };

		String[] eduQualificationWords = { "educational qualification", "qualification", "highest education", "highest qualification",
				"post graduation", "graduation" };

		String[] experienceWords = { "min experience", "max experience", "experience", "total experience" };

		String[] currenyWords = { "salary currency", "salary unit", "currency" };

		String[] salaryWords = { "min salary", "max salary", "salary", "min ctc", "max ctc", "ctc", "current ctc", "current salary" };

		String[] industryWords = { "industry" };

		String[] categoryWords = { "category", "functional area" };

		String[] typeWords = { "job type", "type" };

		String[] skillsWords = { "skill set", "key skills", "skills set", "skills", "skill", "skill(s)" };

		String[] goodSkillsWords = { "Good Skill Set", "Good Skills Set", "Good Skill(s) Set" };

		String[] remoteWorkWords = { "remote work", "remote work option" };

		String[] jobUrlWords = { "job url", "job web site", "url" };

		String[] notesWords = { "comments", "comment" };

		for (int i = 0; i <= clientNameWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(clientNameWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Client_Name);
				break;
			}
		}

		for (int i = 0; i <= nameWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(nameWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Position_Name);
				break;
			}
		}

		for (int i = 0; i <= codeWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(codeWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Position_Code);
				break;
			}
		}

		for (int i = 0; i <= dateWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(dateWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Close_by_Date);
				break;
			}
		}

		for (int i = 0; i <= numberWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(numberWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Number_of_Openings);
				break;
			}
		}

		for (int i = 0; i <= locationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(locationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Location);
				break;
			}
		}

		for (int i = 0; i <= descWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(descWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Job_Description);
				break;
			}
		}

		for (int i = 0; i <= eduQualificationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(eduQualificationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Educational_Qualification);
				break;
			}
		}

		for (int i = 0; i <= experienceWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(experienceWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Min_Experience);
				break;
			}
		}

		for (int i = 0; i <= experienceWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(experienceWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Max_Experience);
				break;
			}
		}
		for (int i = 0; i <= currenyWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(currenyWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Salary_Currency);
				break;
			}
		}
		for (int i = 0; i <= salaryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(salaryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Min_Salary);
				break;
			}
		}
		for (int i = 0; i <= salaryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(salaryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Max_Salary);
				break;
			}
		}
		for (int i = 0; i <= industryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(industryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Industry);
				break;
			}
		}
		for (int i = 0; i <= categoryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(categoryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Category);
				break;
			}
		}
		for (int i = 0; i <= typeWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(typeWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Job_Type);
				break;
			}
		}
		for (int i = 0; i <= skillsWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(skillsWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Skill_Set);
				break;
			}
		}
		for (int i = 0; i <= goodSkillsWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(goodSkillsWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Good_Skill_Set);
				break;
			}
		}

		for (int i = 0; i <= remoteWorkWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(remoteWorkWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Remote_Work);
				break;
			}
		}

		for (int i = 0; i <= jobUrlWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(jobUrlWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Job_URL);
				break;
			}
		}

		for (int i = 0; i <= notesWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(notesWords[i].toLowerCase()) != -1) {

				headerMap.put(column, PositionFileHeaderConstant.Notes);
				break;
			}
		}

		return headerMap;
	}

	/**
	 * @param headerMap
	 * @param column
	 */
	private Map<String, String> getCandidateHeaderMap(Map<String, String> headerMap, String column) {

		String[] nameWords = { "candidate name", "full name", "fullname", "name of candidate", "name" };

		String[] emailWords = { "emailaddress", "email id", "email", "mail id", "mail", "email address", "mail address" };

		String[] mobileWords = { "mobile", "mobile number", "mobile no", "phone", "phone no", "phone number", "telephone no",
				"telephone number", "telephone", "contact no", "contact number", "contact" };

		String[] altEmailWords = { "alternate email", "alternate email id", "alternate email", "alternate mail id" };

		String[] altMobileWords = { "alternate mobile", "alternate mobile number", "alternate mobile no", "alternate phone",
				"alternate phone no", "alternate phone number", "alternate telephone no", "alternate telephone number",
				"alternate telephone" };

		String[] companyWords = { "current company", "current organization", "company", "current employer", "employer" };

		String[] designationWords = { "title", "current title", "current designation", "designation" };

		String[] locationWords = { "location", "current location", "home town", "city", "current city" };

		String[] experienceWords = { "min experience", "max experience", "experience", "total experience" };

		String[] eduQualificationWords = { "educational qualification", "qualification", "highest education", "highest qualification",
				"post graduation", "graduation" };

		String[] birthWords = { "date of birth", "birth date", "dob" };

		String[] skillsWords = { "skill set", "skills set", "skills", "skill", "skill(s)", "key skills", "keyskills" };

		String[] currentSalaryWords = { "min salary", "max salary", "salary", "min ctc", "max ctc", "ctc", "current ctc", "current salary",
				"annual salary" };

		String[] expectedSalaryWords = { "expected ctc", "expected salary" };

		String[] prefLocationWords = { "preferred location" };

		String[] empTypenWords = { "employment type", "emp type", "job type", "type" };

		String[] noticePeriodWords = { "notice period (in days)", "notice period" };

		String[] servingNoticePeriodWords = { "serving notice period", "currently serving" };

		String[] lastWorkingWords = { "last working day", "last day", "last active date", "last active day" };

		String[] genderWords = { "gender" };

		String[] prevEmploymentWords = { "previous employement" };

		String[] industryWords = { "industry" };

		String[] addressWords = { "address", "current address", "postal address" };

		String[] sourceWords = { "source" };

		String[] sourcedDateWords = { "source date" };

		String[] sourceDetailsWords = { "source details", "source info" };

		String[] nationalityWords = { "nationality" };

		String[] maritalWords = { "marital status" };

		String[] languagesWords = { "languages", "language" };

		String[] categoryWords = { "category" };

		String[] subCategoryWords = { "sub category" };

		String[] avgStayWords = { "average stay in company (in months)", "average stay in company", "average stay" };

		String[] longStayWords = { "longest stay in company (in months)", "longest stay in company", "longest stay", "long stay" };

		String[] communicationWords = { "communication" };

		String[] linkedInWords = { "linkedin profile", "linkedin" };

		String[] twitterWords = { "twitter profile", "twitter" };

		String[] githubWords = { "github profile", "github" };

		String[] facebookWords = { "facebook profile", "facebook" };

		String[] notesWords = { "notes/comments", "note", "comments", "summary" };

		String[] lastActiveWords = { "last active date", "last active day", "last active" };

		for (int i = 0; i <= nameWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(nameWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Full_Name);
				break;
			}
		}

		for (int i = 0; i <= emailWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(emailWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Email_Address);
				break;
			}
		}

		for (int i = 0; i <= mobileWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(mobileWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Mobile_No);
				break;
			}
		}

		for (int i = 0; i <= altEmailWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(altEmailWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Alternate_Email);
				break;
			}
		}

		for (int i = 0; i <= altMobileWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(altMobileWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Alternate_Mobile);
				break;
			}
		}

		for (int i = 0; i <= companyWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(companyWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Current_Company);
				break;
			}
		}

		for (int i = 0; i <= designationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(designationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Current_Title);
				break;
			}
		}

		for (int i = 0; i <= locationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(locationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Current_Location);
				break;
			}
		}

		for (int i = 0; i <= experienceWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(experienceWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Total_Experience);
				break;
			}
		}

		for (int i = 0; i <= eduQualificationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(eduQualificationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Highest_Qualification);
				break;
			}
		}

		for (int i = 0; i <= birthWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(birthWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Date_of_Birth);
				break;
			}
		}

		for (int i = 0; i <= skillsWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(skillsWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Key_Skills);
				break;
			}
		}

		for (int i = 0; i <= currentSalaryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(currentSalaryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Current_CTC);
				break;
			}
		}
		for (int i = 0; i <= expectedSalaryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(expectedSalaryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Expected_CTC);
				break;
			}
		}

		for (int i = 0; i <= prefLocationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(prefLocationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Preferred_Location);
				break;
			}
		}

		for (int i = 0; i <= empTypenWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(empTypenWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Employment_Type);
				break;
			}
		}

		for (int i = 0; i <= noticePeriodWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(noticePeriodWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Notice_Period);
				break;
			}
		}

		for (int i = 0; i <= servingNoticePeriodWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(servingNoticePeriodWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Serving_Notice_Period);
				break;
			}
		}

		for (int i = 0; i <= lastWorkingWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(lastWorkingWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Last_Working_Day);
				break;
			}
		}

		for (int i = 0; i <= genderWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(genderWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Gender);
				break;
			}
		}

		for (int i = 0; i <= sourceWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(sourceWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Source);
				break;
			}
		}

		for (int i = 0; i <= sourcedDateWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(sourceWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Sourced_Date);
				break;
			}
		}

		for (int i = 0; i <= sourceDetailsWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(sourceDetailsWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Source_Details);
				break;
			}
		}

		for (int i = 0; i <= nationalityWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(nationalityWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Nationality);
				break;
			}
		}

		for (int i = 0; i <= maritalWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(maritalWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Marital_Status);
				break;
			}
		}

		for (int i = 0; i <= languagesWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(languagesWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Languages);
				break;
			}
		}

		for (int i = 0; i <= categoryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(categoryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Category);
				break;
			}
		}

		for (int i = 0; i <= subCategoryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(subCategoryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Sub_Category);
				break;
			}
		}

		for (int i = 0; i <= avgStayWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(avgStayWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Average_Stay_In_Company);
				break;
			}
		}

		for (int i = 0; i <= longStayWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(longStayWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Longest_Stay_In_Company);
				break;
			}
		}
		for (int i = 0; i <= communicationWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(communicationWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Communication);
				break;
			}
		}
		for (int i = 0; i <= linkedInWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(linkedInWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.LinkedIn_Profile);
				break;
			}
		}
		for (int i = 0; i <= facebookWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(facebookWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Facebook_Profile);
				break;
			}
		}
		for (int i = 0; i <= twitterWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(twitterWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Twitter_Profile);
				break;
			}
		}
		for (int i = 0; i <= githubWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(githubWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Github_Profile);
				break;
			}
		}
		for (int i = 0; i <= notesWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(notesWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Comments);
				break;
			}
		}

		for (int i = 0; i <= prevEmploymentWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(prevEmploymentWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Previous_Employment);
				break;
			}
		}
		for (int i = 0; i <= industryWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(industryWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Industry);
				break;
			}
		}
		for (int i = 0; i <= addressWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(addressWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Address);
				break;
			}
		}
		for (int i = 0; i <= lastActiveWords.length - 1; i++) {

			if (column.toLowerCase().indexOf(lastActiveWords[i].toLowerCase()) != -1) {

				headerMap.put(column, CandidateFileHeaderConstant.Last_Active);
				break;
			}
		}

		return headerMap;
	}

	/**
	 * Importing bulk data using Excel file
	 * 
	 * @param importFile
	 * @param headerMap
	 * @param batchId
	 * @param importType
	 * @param loggedInUser
	 * @throws RecruizException
	 */
	private void importDataByExcel(File importFile, Map<String, String> headerMap, String batchId, String importType, User loggedInUser)
			throws RecruizException {

		try {
			Workbook workbook;
			FileInputStream excelFile = new FileInputStream(importFile);

			String fileExtension = FilenameUtils.getExtension(importFile.getName());

			if ("xls".equals(fileExtension)) {
				// Get the workbook instance for XLS file
				workbook = new HSSFWorkbook(excelFile);
			} else {
				// Get the workbook instance for XLSX file
				workbook = new XSSFWorkbook(excelFile);
			}

			// Get first sheet from the workbook
			Sheet workSheet = workbook.getSheetAt(0);

			// Iterate through each rows from first sheet
			Iterator<Row> iterator = workSheet.iterator();

			// get total number of row count in the sheet
			int totalRowCount = workSheet.getPhysicalNumberOfRows() - 1;

			// updating total row count in db
			ImportJobBatch importJobBatch = updateTotalRowCount(batchId, totalRowCount, importType, loggedInUser, headerMap.toString(),
					importFile.getPath());

			Map<Integer, String> columnIndexMap = new LinkedHashMap<Integer, String>();
			// Get first row
			Row firstRow = workSheet.getRow(0);
			// get the first column index for a row
			short minColIx = firstRow.getFirstCellNum();
			// get the last column index for a row
			short maxColIx = firstRow.getLastCellNum();
			// loop from first to last index
			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				// get the cell
				Cell cell = firstRow.getCell(colIx);
				// add the cell index and cell contents (name of column) to the
				// map
				columnIndexMap.put(cell.getColumnIndex(), cell.getStringCellValue());
			}

			List<Map<String, String>> rowAsMapList = new ArrayList<Map<String, String>>();

			while (iterator.hasNext()) {
				Map<String, String> rowAsMap = new LinkedHashMap<String, String>();
				Row row = iterator.next();
				if (row.getRowNum() == 0) {
					continue;// skip first row, as it contains column names
				}

				// For each row, iterate through each columns
				for (int i = 0; i < row.getLastCellNum(); i++) {

					Cell cell = row.getCell(i);

					if (cell == null) {
						rowAsMap.put(columnIndexMap.get(i), null);
					} else {
						if (CellType.STRING == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), cell.getStringCellValue());
						} else if (CellType.NUMERIC == cell.getCellTypeEnum()) {
							if (isDateValue(columnIndexMap.get(i), headerMap))
								rowAsMap.put(columnIndexMap.get(i), DateUtil.formateDate(cell.getDateCellValue()));
							else
								rowAsMap.put(columnIndexMap.get(i), String.valueOf(cell.getNumericCellValue()));
						} else if (CellType.BOOLEAN == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), String.valueOf(cell.getBooleanCellValue()));
						} else if (CellType.BLANK == cell.getCellTypeEnum()) {
							rowAsMap.put(columnIndexMap.get(i), cell.getStringCellValue());
						}
					}
				}
				// adding each row of file into list
				rowAsMapList.add(rowAsMap);

				// batch update of 100-100 rows
				if (rowAsMapList.size() == 100) {

					switch (importType) {
					case GlobalConstants.CLIENTS:
					case GlobalConstants.DEPARTMENTS:

						importJobBatch = updateClient(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.POSITIONS:

						importJobBatch = updatePosition(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.CANDIDATES:

						importJobBatch = updateCandidate(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.PROSPECTS:

						importJobBatch = updateProspect(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					}

					rowAsMapList.clear();
				}
			}

			// batch update for less than 100 rows
			if (rowAsMapList != null && rowAsMapList.size() > 0) {
				switch (importType) {
				case GlobalConstants.CLIENTS:
				case GlobalConstants.DEPARTMENTS:

					importJobBatch = updateClient(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.POSITIONS:

					importJobBatch = updatePosition(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.CANDIDATES:

					importJobBatch = updateCandidate(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.PROSPECTS:

					importJobBatch = updateProspect(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				}
				rowAsMapList.clear();
			}

			// updating status once import job is completed
			importJobBatch.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.toString());
			importJobBatchService.save(importJobBatch);

			workbook.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.IMPORT_DATA_FAILED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.IMPORT_DATA_FAILED);
		}
	}

	private boolean isDateValue(String dateString, Map<String, String> headerMap) {

		if (dateString.equals(headerMap.get(CandidateFileHeaderConstant.Date_of_Birth))) {
			return true;
		} else if (dateString.equals(headerMap.get(CandidateFileHeaderConstant.Last_Working_Day))) {
			return true;
		} else if (dateString.equals(headerMap.get(PositionFileHeaderConstant.Close_by_Date))) {
			return true;
		} else
			return false;
	}

	/**
	 * Importing bulk data using CSV file
	 * 
	 * @param importFile
	 * @param headerMap
	 * @param batchId
	 * @param importType
	 * @param loggedInUser
	 * @throws RecruizException
	 */
	@SuppressWarnings("unchecked")
	private void importDataByCSV(File importFile, Map<String, String> headerMap, String batchId, String importType, User loggedInUser)
			throws RecruizException {

		ImportJobBatch importJobBatch = null;
		CSVReader csvReader = null;
		try {

			// CSV reader used only for getting total row count since CsvMapper
			// does not have
			csvReader = new CSVReader(new FileReader(importFile));
			List<String[]> content = csvReader.readAll();
			int totalRowCount = content.size() - 1;

			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = CsvSchema.emptySchema().withColumnSeparator(',').withHeader();
			MappingIterator<Map<String, String>> iterator = mapper.readerFor(Map.class).with(schema).readValues(importFile);

			// updating total row count in db
			importJobBatch = updateTotalRowCount(batchId, totalRowCount, importType, loggedInUser, headerMap.toString(),
					importFile.getPath());

			List<Map<String, String>> rowAsMapList = new ArrayList<Map<String, String>>();

			while (iterator.hasNext()) {
				Map<String, String> rowAsMap = iterator.next();

				// adding each row of file into list
				rowAsMapList.add(rowAsMap);

				// batch update of 100-100 rows
				if (rowAsMapList.size() == 100) {
					switch (importType) {
					case GlobalConstants.CLIENTS:
					case GlobalConstants.DEPARTMENTS:

						importJobBatch = updateClient(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.POSITIONS:

						importJobBatch = updatePosition(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.CANDIDATES:

						importJobBatch = updateCandidate(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					case GlobalConstants.PROSPECTS:

						importJobBatch = updateProspect(headerMap, rowAsMapList, batchId, loggedInUser);

						break;
					}
					rowAsMapList.clear();
				}
			}

			if (rowAsMapList != null && rowAsMapList.size() > 0) {
				switch (importType) {
				case GlobalConstants.CLIENTS:
				case GlobalConstants.DEPARTMENTS:

					importJobBatch = updateClient(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.POSITIONS:

					importJobBatch = updatePosition(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.CANDIDATES:

					importJobBatch = updateCandidate(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				case GlobalConstants.PROSPECTS:

					importJobBatch = updateProspect(headerMap, rowAsMapList, batchId, loggedInUser);

					break;
				}
				rowAsMapList.clear();
				iterator.close();
			}
			// updating status once import job is completed
			importJobBatch.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.toString());
			importJobBatchService.save(importJobBatch);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.IMPORT_DATA_FAILED);
		} finally {
			if (csvReader != null) {
				try {
					csvReader.close();
				} catch (IOException ie) {
					logger.error(ie.getMessage());
				}
			}
		}
	}

	//@Transactional
	private void saveCandidateData(List<Candidate> candidateList, ImportJobBatch importJobBatch, File dummyResumeFile) throws IOException {

		long successRowCount = 0;
		long failedRowCount = 0;
		if (candidateList != null && !candidateList.isEmpty()) {
			for (Candidate candidate : candidateList) {
				try {

					if (candidate.getCid() > 0) {
						candidateService.save(candidate);
						candidateActivityService.detailsUpdated(candidate);
					} else {
						candidate = candidateService.addCandidate(candidate);
						try {
							if (candidate.getCid() > 0) {
								uploadFileService.createFolderStructureForCandidate(candidateFolderPath, candidate.getCid() + "");
								String originalResume = uploadFileService.uploadFileToLocalServer(dummyResumeFile, "dummy-file.pdf",
										"resume", candidate.getCid() + "");
								String convertedResume = fileService.convert(originalResume);
								candidateService.updateCandidateResume(candidate, convertedResume);

								// add to resume docs
								String resumePath = uploadFileService.uploadFileToLocalServer(dummyResumeFile, dummyResumeFile.getName(),
										FileType.Original_Resume.getDisplayName(), candidate.getCid() + "");
								// convert file first then upload it.
								String convertedResumePath = fileService.convert(resumePath);
								candidateService.uploadCandidateFiles(resumePath, dummyResumeFile.getName(),
										FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "", convertedResumePath);
							}
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
						}
					}
					successRowCount++;
				} catch (Throwable ex) {
					logger.error(ex.getMessage(), ex);
					failedRowCount++;
					// getting proper exception message
					String messsage = com.bbytes.recruiz.utils.StringUtils.getExceptionMessage(ex, GlobalConstants.CANDIDATES);
					updateImportFailedRows(importJobBatch.getBatchId(), messsage, candidate.getFullName(), candidate.getEmail(),
							importJobBatch);
				}
			}
		}
		// updating successfully added candidate row count
		importJobBatch.addSuccessRowCount(successRowCount);
		importJobBatch.addFailedRowCount(failedRowCount);
		importJobBatchService.save(importJobBatch);
	}

	/**
	 * Updating client data
	 * 
	 * @param headerMap
	 * @param rowAsMapList
	 * @param batchId
	 * @param loggedInUser
	 * @return
	 */
	private ImportJobBatch updateClient(Map<String, String> headerMap, List<Map<String, String>> rowAsMapList, String batchId,
			User loggedInUser) {

		ImportJobBatch importJobBatch = importJobBatchService.findByBatchId(batchId);

		// each 0-100 chunks making into 20 each partition
		List<List<Map<String, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<String, String>> rowAsListChunk : rowAsListChunks) {

			long successCount = 0;
			long failedRowCount = 0;

			for (Map<String, String> rowAsMap : rowAsListChunk) {

				try {
					if (rowAsMap != null && !rowAsMap.isEmpty()) {
						Client clientFromDB = null;

						if (rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)) != null)
							clientFromDB = clientService.getClientByName(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)));

						// checking client exist in db
						if (clientFromDB != null) {

							clientFromDB = updateClientFromFile(rowAsMap, headerMap, clientFromDB);

							clientService.save(clientFromDB);

							successCount++;

						} else if (rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)) != null) {

							Client client = new Client();
							if (rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)) != null
									&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)).trim().isEmpty()) {
								client.setClientName(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)).trim());
								client = updateClientFromFile(rowAsMap, headerMap, client);
								client.setOwner(loggedInUser.getEmail());
								client.setStatus(Status.Active.toString());

								clientService.save(client);

								successCount++;
							}
						}
					} else {
						// updating failed import rows in database
						updateImportFailedRows(batchId, "Empty Row", "N/A", "N/A", importJobBatch);
						failedRowCount++;
					}
				} catch (Throwable ex) {

					logger.warn(ex.getMessage(), ex);

					// getting proper exception message
					String messsage = com.bbytes.recruiz.utils.StringUtils.getExceptionMessage(ex, GlobalConstants.CLIENTS);
					// updating failed import rows in database
					updateImportFailedRows(batchId, messsage, rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)),
							rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Client_Name)), importJobBatch);
					failedRowCount++;
				}
			}

			importJobBatch.addFailedRowCount(failedRowCount);
			importJobBatch.addSuccessRowCount(successCount);
			importJobBatch = importJobBatchService.save(importJobBatch);
		}

		return importJobBatch;
	}

	/**
	 * Updating Position data
	 * 
	 * @param headerMap
	 * @param rowAsMapList
	 * @param batchId
	 * @param loggedInUser
	 * @return
	 */
	@Transactional
	private ImportJobBatch updatePosition(Map<String, String> headerMap, List<Map<String, String>> rowAsMapList, String batchId,
			User loggedInUser) {

		ImportJobBatch importJobBatch = importJobBatchService.findByBatchId(batchId);

		// each 0-100 chunks making into 20 each partition
		List<List<Map<String, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<String, String>> rowAsListChunk : rowAsListChunks) {

			long successCount = 0;
			long failedRowCount = 0;

			for (Map<String, String> rowAsMap : rowAsListChunk) {

				try {
					if (rowAsMap != null && !rowAsMap.isEmpty()) {

						if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Client_Name)) != null) {

							Client clientFromDB = null;

							clientFromDB = clientService
									.getClientByName(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Client_Name)));

							if (clientFromDB == null) {

								// adding client if not there while adding
								// position
								clientFromDB = saveClient(headerMap, loggedInUser, rowAsMap);
							}

							Position positionFromDB = null;

							if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code)) != null
									|| rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)) != null)

								positionFromDB = positionService.getPositionByImportIdentifier(
										rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code)) != null
												? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code))
												: rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)) + ":"
														+ loggedInUser.getEmail());

							// checking position exist in db
							if (positionFromDB != null && clientFromDB != null) {

								positionFromDB.setClient(clientFromDB);
								positionFromDB = updatePositionFromFile(rowAsMap, headerMap, positionFromDB);

								boolean isValidExpRang = positionExpRangeValid(headerMap, rowAsMap);

								if (isValidExpRang) {
									positionFromDB
											.setExperienceRange(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience)));
								} else {
									positionFromDB
											.setExperienceRange(com.bbytes.recruiz.utils.StringUtils.getNumberFromString(
													rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Min_Experience)))
													+ "-"
													+ com.bbytes.recruiz.utils.StringUtils.getNumberFromString(
															rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience)))
													+ " Years");
								}

								positionService.save(positionFromDB);

								successCount++;

							} else if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code)) != null
									|| rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)) != null) {

								Position position = new Position();

								position.setClient(clientFromDB);

								savePosition(headerMap, loggedInUser, rowAsMap, position);

								successCount++;
							} else {
								// updating failed import rows in database
								updateImportFailedRows(batchId, "Position Name field empty", "N/A", "N/A", importJobBatch);
								failedRowCount++;
							}
						} else {
							// updating failed import rows in database
							updateImportFailedRows(batchId, "Client/Department Name field empty",
									rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)), "N/A", importJobBatch);
							failedRowCount++;
						}

					} else {
						// updating failed import rows in database
						updateImportFailedRows(batchId, "Empty Row", "N/A", "N/A", importJobBatch);
						failedRowCount++;
					}
				} catch (Throwable ex) {

					logger.warn(ex.getMessage(), ex);

					// getting proper exception message
					String messsage = com.bbytes.recruiz.utils.StringUtils.getExceptionMessage(ex, GlobalConstants.POSITIONS);

					// updating failed import rows in database
					updateImportFailedRows(batchId, messsage, rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)),
							rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code)), importJobBatch);

					failedRowCount++;
				}
			}

			importJobBatch.addFailedRowCount(failedRowCount);
			importJobBatch.addSuccessRowCount(successCount);
			importJobBatchService.save(importJobBatch);
		}
		return importJobBatch;
	}

	/**
	 * Updating prospect data
	 * 
	 * @param headerMap
	 * @param rowAsMapList
	 * @param batchId
	 * @param loggedInUser
	 * @return
	 */
	private ImportJobBatch updateProspect(Map<String, String> headerMap, List<Map<String, String>> rowAsMapList, String batchId,
			User loggedInUser) {

		ImportJobBatch importJobBatch = importJobBatchService.findByBatchId(batchId);

		// each 0-100 chunks making into 20 each partition
		List<List<Map<String, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<String, String>> rowAsListChunk : rowAsListChunks) {

			long successCount = 0;
			long failedRowCount = 0;

			for (Map<String, String> rowAsMap : rowAsListChunk) {

				try {
					if (rowAsMap != null && !rowAsMap.isEmpty()) {
						Prospect prospectFromDB = null;

						if (rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)) != null)
							prospectFromDB = prospectService
									.getProspectByCompanyName(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)));

						// checking client exist in db
						if (prospectFromDB != null) {

							prospectFromDB = updateProspectFromFile(rowAsMap, headerMap, prospectFromDB);

							prospectService.save(prospectFromDB);

							successCount++;

						} else if (rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)) != null) {

							Prospect prospect = new Prospect();
							if (rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)) != null
									&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)).trim().isEmpty()) {
								prospect.setCompanyName(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)).trim());
								prospect = updateProspectFromFile(rowAsMap, headerMap, prospect);
								prospect.setOwner(loggedInUser.getEmail());
								prospect.setStatus(ProspectStatus.New.toString());

								prospectService.save(prospectFromDB);

								successCount++;
							}
						}
					} else {
						// updating failed import rows in database
						updateImportFailedRows(batchId, "Empty Row", "N/A", "N/A", importJobBatch);
						failedRowCount++;
					}
				} catch (Throwable ex) {

					logger.warn(ex.getMessage(), ex);

					// getting proper exception message
					String messsage = com.bbytes.recruiz.utils.StringUtils.getExceptionMessage(ex, GlobalConstants.PROSPECTS);
					// updating failed import rows in database
					updateImportFailedRows(batchId, messsage, rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)),
							rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Company_Name)), importJobBatch);
					failedRowCount++;
				}
			}

			importJobBatch.addFailedRowCount(failedRowCount);
			importJobBatch.addSuccessRowCount(successCount);
			importJobBatch = importJobBatchService.save(importJobBatch);
		}

		return importJobBatch;
	}

	/**
	 * Saving position along with board and default rounds
	 * 
	 * @param headerMap
	 * @param loggedInUser
	 * @param rowAsMap
	 * @param clientFromDB
	 * @param position
	 */
	private void savePosition(Map<String, String> headerMap, User loggedInUser, Map<String, String> rowAsMap, Position position) {

		position.setImportIdentifier(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code)) != null
				? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Code))
				: rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)) + ":" + loggedInUser.getEmail());
		position.setTitle(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)).trim());

		position = updatePositionFromFile(rowAsMap, headerMap, position);

		position.setOwner(loggedInUser.getEmail());
		position.setStatus(Status.Active.toString());

		boolean isValidExpRang = positionExpRangeValid(headerMap, rowAsMap);

		if (isValidExpRang) {
			position.setExperienceRange(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience)));
		} else {
			position.setExperienceRange(com.bbytes.recruiz.utils.StringUtils
					.getNumberFromString(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Min_Experience))) + "-"
					+ com.bbytes.recruiz.utils.StringUtils
							.getNumberFromString(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience)))
					+ " Years");
		}

		// creating Board for the position
		Board board = new Board();
		board.setStatus(true);

		// Creating default rounds for the position which will be add to the
		// board
		int roundStartOrder = 1;
		for (DefaultRounds round : DefaultRounds.values()) {
			Round roundOne = new Round();
			if (round.getDisplayName().equalsIgnoreCase("Sourcing")) {
				roundOne.setRoundType(GlobalConstants.ROUND_DEFAULT_TYPE_SOURCE);
			}
			roundOne.setRoundName(round.getDisplayName());
			roundOne.setOrderNo(roundStartOrder);
			roundOne.setBoard(board);
			board.getRounds().add(roundOne);
			roundStartOrder++;
		}

		position.setBoard(board);
		Set<User> hrExecutives = new HashSet<User>();
		hrExecutives.add(loggedInUser);
		position.setHrExecutives(hrExecutives);

		String[] positionName = com.bbytes.recruiz.utils.StringUtils.cleanFileName(position.getTitle().trim()).split(" ");
		String posCode = "";
		posCode = positionName[0].substring(0, 2) + "_" + com.bbytes.recruiz.utils.StringUtils.randomString();

		position.setPositionCode(posCode.toLowerCase());

		positionService.save(position);
	}

	private boolean positionExpRangeValid(Map<String, String> headerMap, Map<String, String> rowAsMap) {

		String expRangeHeaderValue = rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience));

		if (expRangeHeaderValue.contains("Years") || expRangeHeaderValue.contains("Year")) {

			String[] exprange = com.bbytes.recruiz.utils.StringUtils
					.extractExpYears(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Experience)));
			boolean isValidExpRange = exprange.length > 1 && (exprange[0] != null && !exprange[0].isEmpty())
					&& (exprange[1] != null && !exprange[1].isEmpty());
			return isValidExpRange;
		}
		return false;

	}

	/**
	 * Saving client with name and other data
	 * 
	 * @param headerMap
	 * @param loggedInUser
	 * @param rowAsMap
	 */
	@Transactional
	private Client saveClient(Map<String, String> headerMap, User loggedInUser, Map<String, String> rowAsMap) {
		Client client = new Client();

		client.setClientName(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Client_Name)));
		client.setAddress(GlobalConstants.NOT_APPLICABLE);
		client.setClientLocation(GlobalConstants.NOT_APPLICABLE);
		client.setStatus(Status.Active.toString());
		client.setOwner(loggedInUser.getEmail());
		client.setNotes(GlobalConstants.NOT_APPLICABLE);

		client = clientService.save(client);
		return client;
	}

	/**
	 * 
	 * @param headerMap
	 * @param rowAsMapList
	 * @param batchId
	 * @param loggedInUser
	 * @return
	 * @throws IOException
	 */
	private ImportJobBatch updateCandidate(Map<String, String> headerMap, List<Map<String, String>> rowAsMapList, String batchId,
			User loggedInUser) throws IOException {

		ImportJobBatch importJobBatch = importJobBatchService.findByBatchId(batchId);

		// each 0-100 chunks making into 20 each partition
		List<List<Map<String, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<String, String>> rowAsListChunk : rowAsListChunks) {

			long failedRowCount = 0;
			List<Candidate> candidateList = new ArrayList<>();

			for (Map<String, String> rowAsMap : rowAsListChunk) {

				try {
					if (rowAsMap != null && !rowAsMap.isEmpty()) {
						Candidate candidateFromDB = null;

						List<String> emails = parseEmails(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Email_Address)));

						List<String> mobileNos = parseMobileNos(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Mobile_No)));

						if (!emails.isEmpty()) {
							if (emails.size() > 0) {

								candidateFromDB = candidateService.getCandidateByEmail(emails.get(0));

								// checking candidate exist in db
								if (candidateFromDB != null) {

									if (!emails.isEmpty()) {
										if (emails.size() > 1) {
											candidateFromDB.setAlternateEmail(emails.get(1));
										}
									}

									if (!mobileNos.isEmpty()) {
										if (mobileNos.size() > 1) {
											candidateFromDB.setMobile(mobileNos.get(0));
											candidateFromDB.setAlternateMobile(mobileNos.get(1));
										} else
											candidateFromDB.setMobile(mobileNos.get(0));
									}

									candidateFromDB = updateCandidateDetailsFromFile(rowAsMap, headerMap, candidateFromDB, loggedInUser);

									candidateFromDB.setStatus(candidateFromDB.getStatus());
									candidateFromDB.setResumeLink(candidateFromDB.getResumeLink());

									candidateList.add(candidateFromDB);

								} else {
									// if candidate is not present add with a
									// dummy
									// resume
									Candidate candidate = new Candidate();

									if (!emails.isEmpty()) {
										if (emails.size() > 1) {
											candidate.setEmail(emails.get(0));
											candidate.setAlternateEmail(emails.get(1));
										} else
											candidate.setEmail(emails.get(0));
									}

									if (!mobileNos.isEmpty()) {
										if (mobileNos.size() > 1) {
											candidate.setMobile(mobileNos.get(0));
											candidate.setAlternateMobile(mobileNos.get(1));
										} else
											candidate.setMobile(mobileNos.get(0));
									}

									candidate = updateCandidateDetailsFromFile(rowAsMap, headerMap, candidate, loggedInUser);

									candidate.setOwner(loggedInUser.getEmail());

									candidateList.add(candidate);
								}
							}
						} else {

							// updating failed import rows in database
							updateImportFailedRows(batchId, "Email address field empty",
									rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)), "N/A", importJobBatch);

							failedRowCount++;
						}
					} else {

						// updating failed import rows in database
						updateImportFailedRows(batchId, "Empty Row", "N/A", "N/A", importJobBatch);

						failedRowCount++;
					}

				} catch (Throwable ex) {

					logger.warn(ex.getMessage(), ex);

					// getting proper exception message
					String messsage = com.bbytes.recruiz.utils.StringUtils.getExceptionMessage(ex, GlobalConstants.CANDIDATES);

					// updating failed import rows in database
					updateImportFailedRows(batchId, messsage, rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)),
							rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Email_Address)), importJobBatch);
				}
			}

			importJobBatch.addFailedRowCount(failedRowCount);
			importJobBatchService.save(importJobBatch);

			// updating candidates with dummy resumes
			File dummyResumeFile = new File(candidateDummyResumeFilePath);
			saveCandidateData(candidateList, importJobBatch, dummyResumeFile);
		}
		return importJobBatch;
	}

	/**
	 * Updating failed rows into import job
	 * 
	 * @param batchId
	 * @param reason
	 * @param name
	 * @param identifier
	 * @param importJobBatch
	 */
	private void updateImportFailedRows(String batchId, String reason, String name, String identifier, ImportJobBatch importJobBatch) {

		ImportJobUploadItem importJobUploadItem = new ImportJobUploadItem();
		importJobUploadItem.setBatchId(batchId);
		importJobUploadItem.setImportJobBatch(importJobBatch);
		importJobUploadItem.setFailedReason(reason);
		importJobUploadItem.setStatus(ResumeUploadFileStatus.FAILED.toString());
		importJobUploadItem.setName(name);
		importJobUploadItem.setIdentifier(identifier);

		importJobUploadItemService.save(importJobUploadItem);
	}

	/**
	 * Method return updated import job batch of total row counts
	 * 
	 * @param batchId
	 * @param totalRows
	 * @param importType
	 * @param loggedInUser
	 * @return
	 */
	private ImportJobBatch updateTotalRowCount(String batchId, int totalRows, String importType, User loggedInUser, String headerMap,
			String filePath) {

		ImportJobBatch importJobBatch = importJobBatchService.findByBatchId(batchId);

		if (importJobBatch == null) {
			importJobBatch = new ImportJobBatch();
			importJobBatch.setBatchId(batchId);
			importJobBatch.setOwner(loggedInUser.getEmail());
			importJobBatch.setImportType(importType);
			importJobBatch.setTotalRowCount(totalRows);
			importJobBatch.setFilePath(filePath);
			importJobBatch.setHeaderMap(headerMap);
		}
		// this check comes only for pending rows and doing from scheduler so
		// resetting success and failed count
		else {
			importJobBatch.setTotalRowCount(totalRows);
			importJobBatch.setSuccessRowCount(0);
			importJobBatch.setFailedRowCount(0);
			List<ImportJobUploadItem> importJobUploadItems = importJobUploadItemService.findByBatchId(batchId);

			// clearing all failed reason items
			importJobUploadItemService.delete(importJobUploadItems);
			importJobBatch.getImportJobUploadItems().clear();
		}
		importJobBatch = importJobBatchService.save(importJobBatch);
		return importJobBatch;
	}

	private List<String> parseMobileNos(String numbers) {
		Set<String> result = new HashSet<>();
		if (numbers != null && !numbers.trim().isEmpty()) {

			List<String> mobileNos = com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(numbers);
			for (String mobileNo : mobileNos) {
				mobileNo = mobileNo.trim();
				result.add(com.bbytes.recruiz.utils.StringUtils.formatParseNumber(mobileNo.replace("+", "")));
			}
		}
		return new ArrayList<>(result);
	}

	private List<String> parseEmails(String emails) {
		Set<String> result = new LinkedHashSet<>();
		if (emails != null && !emails.trim().isEmpty()) {

			List<String> emailAddresses = com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(emails);
			for (String email : emailAddresses) {
				email = email.trim();
				result.add(email);
			}
		}
		return new LinkedList<>(result);
	}

	/**
	 * updating candidate details using rowMap
	 * 
	 * @param rowAsMap
	 * @param headerMap
	 * @param candidate
	 * @return
	 */
	private Candidate updateCandidateDetailsFromFile(Map<String, String> rowAsMap, Map<String, String> headerMap, Candidate candidate,
			User loggedInUser) {

		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)) == null
				|| rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)).isEmpty()) {
			if (candidate.getEmail() != null) {
				int index = candidate.getEmail().indexOf("@");
				if (index != -1)
					candidate.setFullName(candidate.getEmail().substring(0, index));
			}
		} else
			candidate.setFullName(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)) != null
					&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)).isEmpty()
							? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Full_Name)).trim()
							: candidate.getFullName());
		candidate.setEmail(candidate.getEmail());
		candidate.setMobile(candidate.getMobile());
		if (candidate.getAlternateEmail() != null)
			candidate.setAlternateEmail(candidate.getAlternateEmail());
		else
			candidate.setAlternateEmail(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Email)) != null
					&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Email)).isEmpty()
							? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Email)).trim()
							: candidate.getAlternateEmail());
		if (candidate.getAlternateMobile() != null)
			candidate.setAlternateMobile(candidate.getAlternateMobile());
		else
			candidate.setAlternateMobile(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Mobile)) != null
					&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Mobile)).isEmpty()
							? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Alternate_Mobile)).trim()
							: candidate.getAlternateMobile());
		candidate.setCurrentCompany(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Company)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Company)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Company)).trim()
						: candidate.getCurrentCompany());
		candidate.setCurrentTitle(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Title)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Title)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Title)).trim()
						: candidate.getCurrentTitle());
		candidate.setCurrentLocation(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Location)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Location)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_Location)).trim()
						: candidate.getCurrentLocation());
		candidate.setTotalExp(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Total_Experience)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Total_Experience)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils
								.getDoubleFromString(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Total_Experience)).trim())
						: candidate.getTotalExp());
		candidate.setHighestQual(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Highest_Qualification)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Highest_Qualification)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Highest_Qualification)).trim()
						: candidate.getHighestQual());
		candidate.setDob(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Date_of_Birth)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Date_of_Birth)).isEmpty()
						? DateUtil.parseDate(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Date_of_Birth)).trim())
						: candidate.getDob());
		candidate.setKeySkills(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Key_Skills)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Key_Skills)).isEmpty()
						? new HashSet<String>(com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Key_Skills)).trim()))
						: candidate.getKeySkills());
		candidate.setCurrentCtc(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_CTC)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_CTC)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils.parseSalaryString(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Current_CTC)).trim())
						: candidate.getCurrentCtc());
		candidate.setExpectedCtc(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Expected_CTC)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Expected_CTC)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils.parseSalaryString(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Expected_CTC)).trim())
						: candidate.getExpectedCtc());
		candidate.setPreferredLocation(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Preferred_Location)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Preferred_Location)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Preferred_Location)).trim()
						: candidate.getPreferredLocation());
		candidate.setEmploymentType(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Employment_Type)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Employment_Type)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Employment_Type)).trim()
						: candidate.getEmploymentType());
		candidate.setNoticePeriod(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notice_Period)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notice_Period)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils
								.getIntegerFromString(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notice_Period)).trim())
						: candidate.getNoticePeriod());
		candidate.setNoticeStatus(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Serving_Notice_Period)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Serving_Notice_Period)).isEmpty()
						? Boolean.valueOf(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Serving_Notice_Period)).trim())
						: candidate.isNoticeStatus());
		candidate.setLastWorkingDay(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Working_Day)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Working_Day)).isEmpty()
						? DateUtil.parseDate(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Working_Day)).trim())
						: candidate.getLastWorkingDay());
		candidate.setGender(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Gender)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Gender)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils.parseGender(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Gender)).trim())
						: candidate.getGender());
		candidate.setPreviousEmployment(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Previous_Employment)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Previous_Employment)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Previous_Employment)).trim()
						: candidate.getPreviousEmployment());
		candidate.setIndustry(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Industry)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Industry)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Industry)).trim()
						: candidate.getIndustry());
		candidate.setAddress(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Address)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Address)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Address)).trim()
						: candidate.getAddress());

		// getting source and source details from excel sheet
		String source = rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source)).trim()
						: Source.DataImport.getDisplayName();
		String sourceDetails = rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source_Details)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source_Details)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Source_Details)).trim()
						: candidate.getSourceDetails();
		if (candidate.getCid() > 0)
			candidateService.setSourceInfo(candidate, loggedInUser.getEmail(), source, sourceDetails);
		else {
			candidate.setSourcedOnDate(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sourced_Date)) != null
					&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sourced_Date)).isEmpty()
							? DateUtil.parseDate(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sourced_Date)).trim())
							: new Date());
			candidate.setSource(source);
			candidate.setOwner(loggedInUser.getEmail());
			candidate.setSourceDetails(sourceDetails);
		}

		candidate.setNationality(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Nationality)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Nationality)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Nationality)).trim()
						: candidate.getNationality());
		candidate.setMaritalStatus(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Marital_Status)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Marital_Status)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Marital_Status)).trim()
						: candidate.getMaritalStatus());
		candidate.setLanguages(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Languages)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Languages)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Languages)).trim()
						: candidate.getLanguages());
		candidate.setCategory(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Category)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Category)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Category)).trim()
						: candidate.getCategory());
		candidate.setSubCategory(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sub_Category)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sub_Category)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Sub_Category)).trim()
						: candidate.getSubCategory());
		candidate.setAverageStayInCompany(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Average_Stay_In_Company)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Average_Stay_In_Company)).isEmpty()
						? Integer.valueOf(com.bbytes.recruiz.utils.StringUtils.getNumberFromString(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Average_Stay_In_Company)).trim()))
						: candidate.getAverageStayInCompany());
		candidate.setLongestStayInCompany(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Longest_Stay_In_Company)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Longest_Stay_In_Company)).isEmpty()
						? Integer.valueOf(com.bbytes.recruiz.utils.StringUtils.getNumberFromString(
								rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Longest_Stay_In_Company)).trim()))
						: candidate.getLongestStayInCompany());
		candidate.setCommunication(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Communication)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Communication)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Communication)).trim()
						: candidate.getCommunication());
		candidate.setLinkedinProf(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.LinkedIn_Profile)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.LinkedIn_Profile)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.LinkedIn_Profile)).trim()
						: candidate.getLinkedinProf());
		candidate.setFacebookProf(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Facebook_Profile)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Facebook_Profile)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Facebook_Profile)).trim()
						: candidate.getFacebookProf());
		candidate.setTwitterProf(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Twitter_Profile)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Twitter_Profile)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Twitter_Profile)).trim()
						: candidate.getTwitterProf());
		candidate.setGithubProf(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Github_Profile)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Github_Profile)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Github_Profile)).trim()
						: candidate.getGithubProf());
		candidate.setComments(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Comments)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Comments)).isEmpty()
						? rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Comments)).trim()
						: candidate.getComments());
		candidate.setLastActive(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Active)) != null
				&& !rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Active)).isEmpty()
						? DateUtil.parseDate(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Last_Active)).trim())
						: candidate.getLastActive());
		candidate = updateCandidateNotes(rowAsMap, headerMap, candidate, loggedInUser);
		candidate = updateCandidateEducationDetails(rowAsMap, headerMap, candidate, loggedInUser);
		return candidate;
	}

	private Candidate updateCandidateNotes(Map<String, String> rowAsMap, Map<String, String> headerMap, Candidate candidate,
			User loggedInUser) {

		Set<CandidateNotes> candidateNotes = new HashSet<CandidateNotes>();
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes1)) != null) {

			CandidateNotes candidateNote = new CandidateNotes();
			candidateNote.setNotes(headerMap.get(CandidateFileHeaderConstant.Notes1) + " : "
					+ rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes1)));
			candidateNote.setAddedBy(loggedInUser.getEmail());
			candidateNote.setCandidateId(candidate);

			candidateNotes.add(candidateNote);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes2)) != null) {

			CandidateNotes candidateNote = new CandidateNotes();
			candidateNote.setNotes(headerMap.get(CandidateFileHeaderConstant.Notes2) + " : "
					+ rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes2)));
			candidateNote.setAddedBy(loggedInUser.getEmail());
			candidateNote.setCandidateId(candidate);

			candidateNotes.add(candidateNote);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes3)) != null) {

			CandidateNotes candidateNote = new CandidateNotes();
			candidateNote.setNotes(headerMap.get(CandidateFileHeaderConstant.Notes3) + " : "
					+ rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes3)));
			candidateNote.setAddedBy(loggedInUser.getEmail());
			candidateNote.setCandidateId(candidate);

			candidateNotes.add(candidateNote);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes4)) != null) {

			CandidateNotes candidateNote = new CandidateNotes();
			candidateNote.setNotes(headerMap.get(CandidateFileHeaderConstant.Notes4) + " : "
					+ rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes4)));
			candidateNote.setAddedBy(loggedInUser.getEmail());
			candidateNote.setCandidateId(candidate);

			candidateNotes.add(candidateNote);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes5)) != null) {

			CandidateNotes candidateNote = new CandidateNotes();
			candidateNote.setNotes(headerMap.get(CandidateFileHeaderConstant.Notes5) + " : "
					+ rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.Notes5)));
			candidateNote.setAddedBy(loggedInUser.getEmail());
			candidateNote.setCandidateId(candidate);

			candidateNotes.add(candidateNote);
		}
		candidate.setNotes(candidateNotes);
		return candidate;
	}

	private Candidate updateCandidateEducationDetails(Map<String, String> rowAsMap, Map<String, String> headerMap, Candidate candidate,
			User loggedInUser) {

		Set<CandidateEducationDetails> candidateEducationDetailsList = new HashSet<CandidateEducationDetails>();
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification1)) != null
				|| rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute1)) != null) {

			CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails();
			candidateEducationDetails.setBoard(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification1)));
			candidateEducationDetails.setCollege(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute1)));
			candidateEducationDetails.setCandidate(candidate);

			candidateEducationDetailsList.add(candidateEducationDetails);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification2)) != null
				|| rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute2)) != null) {

			CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails();
			candidateEducationDetails.setBoard(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification2)));
			candidateEducationDetails.setCollege(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute2)));
			candidateEducationDetails.setCandidate(candidate);

			candidateEducationDetailsList.add(candidateEducationDetails);
		}
		if (rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification3)) != null
				|| rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute3)) != null) {

			CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails();
			candidateEducationDetails.setBoard(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.AcademicQualification3)));
			candidateEducationDetails.setCollege(rowAsMap.get(headerMap.get(CandidateFileHeaderConstant.University_Institute3)));
			candidateEducationDetails.setCandidate(candidate);

			candidateEducationDetailsList.add(candidateEducationDetails);
		}
		candidate.setEducationDetails(candidateEducationDetailsList);
		return candidate;
	}

	/**
	 * updating client details using rowMap
	 * 
	 * @param rowAsMap
	 * @param headerMap
	 * @param client
	 * @return
	 */
	private Client updateClientFromFile(Map<String, String> rowAsMap, Map<String, String> headerMap, Client client) {
		client.setClientName(client.getClientName());
		client.setAddress(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Address)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Address)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Address)).trim()
						: client.getAddress());
		client.setClientLocation(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Location)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Location)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Location)).trim()
						: client.getClientLocation());
		client.setWebsite(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Website)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Website)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Website)).trim()
						: client.getWebsite());
		client.setEmpSize(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Employee_Strength)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Employee_Strength)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Employee_Strength)).trim().replaceAll("^0*([0-9]+).*", "$1")
						: client.getEmpSize());
		client.setTurnOvr(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Turnover)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Turnover)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.Turnover)).trim().replaceAll("^0*([0-9]+).*", "$1")
						: client.getTurnOvr());
		client.setNotes(rowAsMap.get(headerMap.get(ClientFileHeaderConstant.About)) != null
				&& !rowAsMap.get(headerMap.get(ClientFileHeaderConstant.About)).isEmpty()
						? rowAsMap.get(headerMap.get(ClientFileHeaderConstant.About)).trim()
						: client.getNotes());
		return client;
	}

	/**
	 * updating prospect details using rowMap
	 * 
	 * @param rowAsMap
	 * @param headerMap
	 * @param prospect
	 * @return
	 */
	private Prospect updateProspectFromFile(Map<String, String> rowAsMap, Map<String, String> headerMap, Prospect prospect) {
		prospect.setCompanyName(prospect.getCompanyName());
		prospect.setName(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Name)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Name)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Name)).trim()
						: prospect.getName());
		prospect.setEmail(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Email_Address)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Email_Address)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Email_Address)).trim()
						: prospect.getEmail());
		prospect.setMobile(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Mobile_No)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Mobile_No)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Mobile_No)).trim()
						: prospect.getMobile());
		prospect.setDesignation(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Designation)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Designation)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Designation)).trim()
						: prospect.getDesignation());
		prospect.setLocation(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Location)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Location)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Location)).trim()
						: prospect.getLocation());
		prospect.setAddress(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Address)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Address)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Address)).trim()
						: prospect.getAddress());
		prospect.setWebsite(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Website)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Website)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Website)).trim()
						: prospect.getWebsite());
		prospect.setSource(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Source)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Source)).isEmpty()
						? rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Source)).trim()
						: prospect.getSource());
		prospect.setProspectRating(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Rating)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Rating)).isEmpty()
						? Double.valueOf(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Prospect_Rating)).trim()).intValue()
						: (prospect.getProspectRating()));

		prospect = getIndustry(rowAsMap, headerMap, prospect);
		prospect = getCategory(rowAsMap, headerMap, prospect);

		return prospect;
	}

	/**
	 * updating position details using rowMap
	 * 
	 * @param rowAsMap
	 * @param headerMap
	 * @param position
	 * @return
	 */
	private Position updatePositionFromFile(Map<String, String> rowAsMap, Map<String, String> headerMap, Position position) {
		position.setClient(position.getClient());
		position.setTitle(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)).isEmpty()
						? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Position_Name)).trim()
						: position.getTitle());
		position.setCloseByDate(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Close_by_Date)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Close_by_Date)).isEmpty()
						? DateUtil.parseDate(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Close_by_Date)).trim())
						: (position.getCloseByDate() != null ? position.getCloseByDate() : new Date()));
		position.setTotalPosition(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Number_of_Openings)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Number_of_Openings)).isEmpty()
						? Double.valueOf(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Number_of_Openings)).trim()).intValue()
						: (position.getTotalPosition() != 0 ? position.getTotalPosition() : 1));
		position.setLocation(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Location)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Location)).isEmpty()
						? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Location)).trim().replaceAll("[;,]", "|")
						: (position.getLocation() != null ? position.getLocation() : "N/A"));
		position.setDescription(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Description)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Description)).isEmpty()
						? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Description)).trim()
						: (position.getDescription() != null ? position.getDescription() : "N/A"));
		position.setEducationalQualification(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Educational_Qualification)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Educational_Qualification)).isEmpty()
						? new HashSet<String>(com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(
								rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Educational_Qualification)).trim()))
						: (position.getEducationalQualification() != null && !position.getEducationalQualification().isEmpty()
								? position.getEducationalQualification()
								: new HashSet<String>(Arrays.asList("N/A"))));
		position.setMinSal(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Min_Salary)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Min_Salary)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils.parseSalaryString(
								rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Min_Salary)).trim())
						: position.getMinSal());
		position.setMaxSal(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Salary)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Salary)).isEmpty()
						? com.bbytes.recruiz.utils.StringUtils.parseSalaryString(
								rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Max_Salary)).trim())
						: position.getMaxSal());

		position = getIndustry(rowAsMap, headerMap, position);
		position = getCategory(rowAsMap, headerMap, position);
		position = getJobType(rowAsMap, headerMap, position);

		position.setRemoteWork(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Remote_Work)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Remote_Work)).isEmpty()
						? Boolean.valueOf(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Remote_Work)).trim())
						: position.isRemoteWork());
		position.setReqSkillSet(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Skill_Set)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Skill_Set)).isEmpty()
						? new HashSet<String>(com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(
								rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Skill_Set)).trim()))
						: (position.getReqSkillSet() != null && !position.getReqSkillSet().isEmpty() ? position.getReqSkillSet()
								: new HashSet<String>(Arrays.asList("N/A"))));
		position.setGoodSkillSet(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Good_Skill_Set)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Good_Skill_Set)).isEmpty()
						? new HashSet<String>(com.bbytes.recruiz.utils.StringUtils.commaORSemicolonSeparateStringToList(
								rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Good_Skill_Set)).trim()))
						: position.getGoodSkillSet());
		position.setPositionUrl(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_URL)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_URL)).isEmpty()
						? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_URL)).trim()
						: position.getPositionUrl());
		position.setNotes(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Notes)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Notes)).isEmpty()
						? rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Notes)).trim()
						: position.getNotes());
		return position;
	}

	private Position getIndustry(Map<String, String> rowAsMap, Map<String, String> headerMap, Position position) {
		if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Industry)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Industry)).isEmpty()) {
			for (IndustryOptions industryOption : IndustryOptions.values()) {
				if (industryOption.getDisplayName().equals(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Industry)).trim())) {
					position.setIndustry(industryOption.getDisplayName());
					return position;
				}
			}
		}
		position.setIndustry(IndustryOptions.IT_SW.getDisplayName());
		return position;
	}

	private Position getCategory(Map<String, String> rowAsMap, Map<String, String> headerMap, Position position) {
		if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Category)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Category)).isEmpty()) {
			for (CategoryOptions categoryOption : CategoryOptions.values()) {
				if (categoryOption.getDisplayName().equals(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Category)).trim())) {
					position.setFunctionalArea(categoryOption.getDisplayName());
					return position;
				}
			}
		}
		position.setFunctionalArea(CategoryOptions.IT_Software_Application_Programming.getDisplayName());
		return position;
	}

	private Position getJobType(Map<String, String> rowAsMap, Map<String, String> headerMap, Position position) {
		if (rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Type)) != null
				&& !rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Type)).isEmpty()) {
			for (EmploymentType employmentType : EmploymentType.values()) {
				if (employmentType.name().equals(rowAsMap.get(headerMap.get(PositionFileHeaderConstant.Job_Type)).trim())) {
					position.setType(employmentType.name());
					return position;
				}
			}
		}
		position.setType(EmploymentType.FullTime.toString());
		return position;
	}

	private Prospect getIndustry(Map<String, String> rowAsMap, Map<String, String> headerMap, Prospect prospect) {
		if (rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Industry)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Industry)).isEmpty()) {
			for (IndustryOptions industryOption : IndustryOptions.values()) {
				if (industryOption.name().equals(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Industry)).trim())) {
					prospect.setIndustry(industryOption.name());
					return prospect;
				}
			}
		}
		prospect.setIndustry(IndustryOptions.IT_SW.getDisplayName());
		return prospect;
	}

	private Prospect getCategory(Map<String, String> rowAsMap, Map<String, String> headerMap, Prospect prospect) {
		if (rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Category)) != null
				&& !rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Category)).isEmpty()) {
			for (CategoryOptions categoryOption : CategoryOptions.values()) {
				if (categoryOption.name().equals(rowAsMap.get(headerMap.get(ProspectFileHeaderConstant.Category)).trim())) {
					prospect.setCategory(categoryOption.name());
					return prospect;
				}
			}
		}
		prospect.setCategory(CategoryOptions.IT_Software_Application_Programming.getDisplayName());
		return prospect;
	}

	/**
	 * Return report object of import export upload items
	 * 
	 * @return
	 * @throws RecruizException
	 */
	public Report getFailedImportItemsReport(String batchId) throws RecruizException {

		String template = "import_upload_items.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("batchId", batchId);
		String sql = getSQL(template, templateModel);

		Report report = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				return report;
			}
		});
		return report;
	}

	/**
	 * Creating folder structure for export data
	 * 
	 * @param id
	 * @param pathType
	 * @return
	 */
	private String createFolderStructureForExportData(String id, String pathType, String exportId) {

		String folderPath = "";

		if (pathType.equalsIgnoreCase(GlobalConstants.JOB_DESCIRPTION)) {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.POSITIONS + File.separator + id + File.separator + GlobalConstants.JOB_DESCIRPTION;
		} else if (pathType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.CANDIDATES + File.separator + id + File.separator + FileType.Original_Resume.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.Masked_Resume_Original.getDisplayName())) {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.CANDIDATES + File.separator + id + File.separator + GlobalConstants.Masked_Resume;
		} else if (pathType.equalsIgnoreCase(FileType.Original_Converted_Resume.getDisplayName())) {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.CANDIDATES + File.separator + id + File.separator + FileType.Original_Resume.getDisplayName();
		} else if (pathType.equalsIgnoreCase(FileType.Masked_Resume_Converted.getDisplayName())) {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.CANDIDATES + File.separator + id + File.separator + GlobalConstants.Masked_Resume;
		} else {
			folderPath = exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId + File.separator
					+ GlobalConstants.CANDIDATES + File.separator + id + File.separator + GlobalConstants.DOCS;
		}

		File directory = new File(folderPath);
		if (!directory.exists())
			directory.mkdirs();
		return directory.getPath();
	}

	/**
	 * Creating folder structure for root folder path
	 * 
	 * @param exportId
	 * @return
	 */
	private String createRootPath(String exportId) {

		File rootFolder = new File(exportDataRootPath + File.separator + TenantContextHolder.getTenant() + "_" + exportId);
		if (!rootFolder.exists())
			rootFolder.mkdirs();
		return rootFolder.getPath();
	}

	/**
	 * Export as excel document
	 * 
	 * @author Akshay
	 * 
	 * @param metaData
	 * @param resultsetData
	 * @param sheetName
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public File resultSetToExcelExport(Object[] metaData, Object[][] resultsetData, String sheetName, File excelFile)
			throws IOException, InvalidFormatException {

		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		if (excelFile != null && excelFile.exists()) {

			// appending to existing excel workbook file
			workbook = new XSSFWorkbook(new FileInputStream(excelFile));

		} else {
			// creating temp excel file
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			excelFile = new File(tempDir, sheetName + ".xlsx");

			// creating excel workbook and excel worksheet object
			workbook = new XSSFWorkbook();
		}

		if (workbook.getSheet(sheetName) == null) {
			XSSFSheet sheet = workbook.createSheet(com.bbytes.recruiz.utils.StringUtils.cleanFileName(sheetName));

			try {
				int currentRow = 0;
				Row row = sheet.createRow(currentRow);
				int numCols = metaData.length;
				if (resultsetData != null) {

					// writing header as result meta data for excel document
					CellStyle cellStyle = workbook.createCellStyle();
					for (int i = 0; i < numCols; i++) {
						String title = (String) metaData[i];
						Cell cell = row.createCell(i);
						cell.setCellValue(title);

						cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
						cell.setCellStyle(cellStyle);
					}

					// writing result data for excel document
					for (Object[] result : resultsetData) {
						row = sheet.createRow(++currentRow);
						int colNum = 0;
						for (Object field : result) {
							Cell cell = row.createCell(colNum++);
							if (null == field) {
								cell.setCellValue((String) "");
								continue;
							}
							
							if (field instanceof String) {
								cell.setCellValue((String) field);
							} else if (field instanceof Integer) {
								cell.setCellValue((Integer) field);
							} else if (field instanceof Long) {
								cell.setCellValue((Long) field);
							} else if (field instanceof Double) {
								cell.setCellValue((Double) field);
							}
						}
					}
				} else {
					String title = "No Data";
					Cell cell = row.createCell(0);
					cell.setCellValue(title);
				}
				// Autosize columns( resize column according to column name)
				for (int i = 0; i < numCols; i++) {
					sheet.autoSizeColumn((short) i);
				}

				// stream writing into workbook file
				fileOutputStream = new FileOutputStream(excelFile);
				workbook.write(fileOutputStream);
				workbook.close();

				
				
				
			} catch (FileNotFoundException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			} finally {
				if (fileOutputStream != null)
					fileOutputStream.close();
			}
		}
		return excelFile;
	}

	private String getSQL(String template, Map<String, Object> model) {
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, "query-templates/import-export-query/" + template,
				"UTF-8", model);
		return sql;
	}

	// @Narinder
	public File resultSetToExcelExportForAllStatusAndStages(Object[] metaData, Object[][] resultsetData, String sheetName,User user, File excelFile)
			throws IOException, InvalidFormatException {

		
		/*SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		String excelFileDate = formatter.format(new Date());
		sheetName = "All_Status_And_Stages_"+excelFileDate;*/
		File excelFolder = new File(
				candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "reports"
						+ File.separator + "All_Status_And_Stages"+ File.separator + user.getUserId());
		
		if (!excelFolder.exists()){
			excelFolder.mkdirs();
		}else{
			FileUtils.deleteDirectory(excelFolder);
			excelFolder.mkdirs();
		}
		FileOutputStream fileOutputStream = null;
		XSSFWorkbook workbook = null;
		if (excelFile != null && excelFile.exists()) {

			// appending to existing excel workbook file
			workbook = new XSSFWorkbook(new FileInputStream(excelFile));

		} else {
			// creating temp excel file
			//File tempDir = new File(System.getProperty("java.io.tmpdir"));
			excelFile = new File(excelFolder, sheetName + ".xlsx");

			// creating excel workbook and excel worksheet object
			workbook = new XSSFWorkbook();
		}

		if (workbook.getSheet(sheetName) == null) {
			XSSFSheet sheet = workbook.createSheet(com.bbytes.recruiz.utils.StringUtils.cleanFileName(sheetName));

			try {
				int currentRow = 0;
				Row row = sheet.createRow(currentRow);
				int numCols = metaData.length;
				if (resultsetData != null) {

					// writing header as result meta data for excel document
					CellStyle cellStyle = workbook.createCellStyle();
					for (int i = 0; i < numCols; i++) {
						String title = (String) metaData[i];
						Cell cell = row.createCell(i);
						cell.setCellValue(title);

						cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
						cell.setCellStyle(cellStyle);
					}

					// writing result data for excel document
					for (Object[] result : resultsetData) {
						row = sheet.createRow(++currentRow);
						int colNum = 0;
						for (Object field : result) {
							if (null == field) {
								continue;
							}
							Cell cell = row.createCell(colNum++);
							if (field instanceof String) {
								cell.setCellValue((String) field);
							} else if (field instanceof Integer) {
								cell.setCellValue((Integer) field);
							} else if (field instanceof Long) {
								cell.setCellValue((Long) field);
							} else if (field instanceof Double) {
								cell.setCellValue((Double) field);
							}
						}
					}
				} else {
					String title = "No Data";
					Cell cell = row.createCell(0);
					cell.setCellValue(title);
				}
				// Autosize columns( resize column according to column name)
				for (int i = 0; i < numCols; i++) {
					sheet.autoSizeColumn((short) i);
				}

				// stream writing into workbook file
				fileOutputStream = new FileOutputStream(excelFile);
				workbook.write(fileOutputStream);
				workbook.close();
				
			} catch (FileNotFoundException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			} finally {
				if (fileOutputStream != null)
					fileOutputStream.close();
			}
		}
		
   /*	File latestFile = new File(excelFolder+sheetName + ".xlsx"); 
		if(latestFile.exists()){
			System.out.println("yes latest file is exists====================");
			File file;
			
			SimpleDateFormat formatterData = new SimpleDateFormat("dd-MM-yy");
		    Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			Date previousDate = cal.getTime();
					
			String excelFileDte = formatterData.format(previousDate);
			String sheetNme = "All_Status_And_Stages_"+excelFileDte;
			String excelFolderdata = candidateFolderPath + File.separator + "reports" + File.separator + TenantContextHolder.getTenant()
							+ File.separator + "All_Status_And_Stages"+ File.separator +sheetNme+".xlsx";
			
		    file = new File(excelFolderdata);
		    
		    if(file.exists()){
		    	file.delete();
		    	System.out.println("yes previous file is exists or deleted successfully====================");
		    }
		    
		}*/
		return excelFile;
	}
	
	
}
