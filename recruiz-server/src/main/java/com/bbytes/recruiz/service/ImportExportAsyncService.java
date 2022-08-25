package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.CustomRounds;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.DefaultRounds;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.scheduler.SchedulerTaskTenantState;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ThreadTenantSecurityUtil;

/**
 * This is import export Async Service to make all calls to be Async
 * 
 * @author Akshay
 *
 */
@Service
public class ImportExportAsyncService {

	private static final Logger logger = LoggerFactory.getLogger(ImportExportAsyncService.class);
	@Autowired
	private UserRoleService userRoleService;
	
	@Autowired
	private ImportExportService importExportService;
	
	@Autowired
	private BoardCustomStatusService boardCustomStatusService;
	
	@Autowired
	private CustomRoundService customRoundService;
	
	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientRepository clientRepository;
	
	@Autowired
	private RoundService roundService;
	
	@Autowired
	private RoundCandidateService roundCandidateService;

	private final String exportDataTemplate = GlobalConstants.EMAIL_TEMPLATE_EXPORT_DATA;

	private final String exportDataSubject = "Recruiz - Data download link ";

	@Autowired
	private IEmailService emailService;

	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Value("${base.url}")
	private String baseUrl;

	/**
	 * This is Async method for exporting client, position and candidate data
	 * 
	 * @author Akshay
	 * @param tenantId
	 * @param isExportFile
	 * @param loggendInUser
	 * @throws IOException
	 * @throws RecruizException
	 */
	@Async
	public void startExportDataAsync(String tenantId, boolean isExportFile, User loggendInUser)
			throws IOException, RecruizException {

		TenantContextHolder.setTenant(tenantId);
		// this block is for long running process
		// set the state to running
		resumeBulkTenantState.setExportJobTaskRunningNow(tenantId);

		String targetZipName = importExportService.exportData(tenantId, isExportFile, loggendInUser);

		String downloadLink = baseUrl + "/pub/export/data/" + targetZipName;

		List<String> emailList = Arrays.asList(loggendInUser.getEmail());
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, loggendInUser.getName());
		emailBody.put(GlobalConstants.DOWNLOAD_LINK, downloadLink);

		emailService.sendEmail(exportDataTemplate, emailList,
				exportDataSubject + loggendInUser.getOrganization().getOrgName(), emailBody);

	}

	/**
	 * This is Async method for importing client, position and candidate data
	 * 
	 * @author Akshay
	 * @param tenantId
	 * @param auth
	 * @param loggendInUser
	 * @param importFile
	 * @param headerMap
	 * @param importType
	 * @param batchId
	 * @throws IOException
	 * @throws RecruizException
	 */
	@Async
	public void startImportDataAsync(String tenantId, Authentication auth, User loggendInUser, File importFile,
			Map<String, String> headerMap, String importType, String batchId) throws IOException, RecruizException {

		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);

		// this block is for long running process
		// set the state to running
		resumeBulkTenantState.setImportJobTaskRunningNow(tenantId);

		try {
			if (headerMap != null && !headerMap.isEmpty() && importType != null && !importType.isEmpty()
					&& batchId != null && !batchId.isEmpty())
				importExportService.startImportData(importFile, headerMap, importType, batchId, loggendInUser);
		} finally {
			// set the state to done after coming to this finally block
			resumeBulkTenantState.setImportJobTaskDone(tenantId);
		}

	}
	
	
	@Async
	public void createExcelReportAsync(String tenantId, Authentication auth) throws InvalidFormatException, IOException, RecruizException {
		ThreadTenantSecurityUtil.setAuthAndTenantThreadContext(tenantId, auth);
		logger.error("Tenant name ===="+tenantId);
		checkAllUserByReportStatus();
	}



	public void checkAllUserByReportStatus() throws RecruizException, InvalidFormatException, IOException{

		UserRole role = userRoleService.getRolesByName("Super Admin");
		List<User> userList = userRepository.findOneByUserRole(role);
		for (User user : userList) {

			if(user!=null){
				Organization org = user.getOrganization();
				if(org!=null){
					OrganizationConfiguration config = org.getOrganizationConfiguration();
					if(config.getCustomReportEnabled()){
						getPositionAllStageAllStatusReport(user.getReporttimeperiod(),user);
					}
				}
			}
		}

	}


	// custom board report given for a client
	// @Sajin - For Intelliswift
	public void getPositionAllStageAllStatusReport(String timePeriod, User user)
			throws InvalidFormatException, IOException, RecruizException {

		try{
			// List<Position> positionList = positionService.findAll();
			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();


			if (timePeriod.equalsIgnoreCase("Last_month")) {
				cal.add(Calendar.MONTH, -1);

			} else if (timePeriod.equalsIgnoreCase("Three_months")) {
				cal.add(Calendar.MONTH, -3);

			} else if (timePeriod.equalsIgnoreCase("One_week")) {
				cal.add(Calendar.DATE, -7);
			} else {
				// By default one month
				cal.add(Calendar.MONTH, -1);

			}

			Date startdate = cal.getTime();

			List<Position> positionList = positionRepository.findByModificationDateBetween(startdate, enddate);
			logger.error("total positions ======" + positionList.size());

			/*Set<Position> positions = new HashSet<>();
		positions.addAll(positionList);*/

			Map<String, String> statusMap = getBoardStatusList();

			Map<String, String> stageMap = new LinkedHashMap<>();
			stageMap.put(DefaultRounds.Sourcing.getDisplayName(), DefaultRounds.Sourcing.getDisplayName());
			List<CustomRounds> customRounds = customRoundService.findAll();
			if (null != customRounds && !customRounds.isEmpty()) {
				for (CustomRounds customRound : customRounds) {
					stageMap.put(customRound.getName(), customRound.getName());
				}
			}

			/*
			 * int sheetColumnSize = 5000 + statusMap.size(); int sheetRowSize =
			 * positions.size() + 5000;
			 */

			int sheetColumnSize = 500 + statusMap.size();
			int sheetRowSize = (positionList.size() * 3) + 500;

			Object[][] data = new Object[sheetRowSize][sheetColumnSize];
			String[] header = new String[sheetColumnSize];
			header[0] = "Sl No.";
			header[1] = "Job Received Date";
			header[2] = "Client";
			header[3] = "Requirement Details";
			header[4] = "No. of Openings";
			header[5] = "Requirement Status";
			header[6] = "# of Profiles Sourced";

			for (Position eachPosition : positionList) {

				List<Round> allRounds = roundService.getRoundsByBoardPositionCode(eachPosition.getBoard());

				for (Round eachRound : allRounds) {

					String roundstagename = eachRound.getRoundName();
					if (!stageMap.containsValue(roundstagename)) {

						stageMap.put(roundstagename, roundstagename);
					}
				}

			}

			int stagerow = 0;
			int statusrow = 1;
			int datarow = 2;
			int serialnumber = 1;

			for (Position eachPosition : positionList) {

				List<Position> positions = new ArrayList<>();
				positions.add(eachPosition);
				List<Client> clientName = clientRepository.findByPositionsIn(positions);
				Position position = eachPosition;

				data[datarow][0] = serialnumber;
				data[datarow][1] = DateUtil.formateDate(position.getOpenedDate(), DateUtil.DATE_FORMAT_MONTH);
				data[datarow][2] = clientName.get(0).getClientName();
				data[datarow][3] = position.getTitle();
				data[datarow][4] = position.getTotalPosition();
				data[datarow][5] = position.getStatus();
				data[datarow][6] = roundCandidateService.getCountByPositionCode(position.getPositionCode());

				List<Round> allRounds = roundService.getRoundsByBoardPositionCode(position.getBoard());

				int columnIndex = 7;

				for (Round eachRound : allRounds) {

					String roundstagename = eachRound.getRoundName();

					for (Entry<String, String> statusentry : statusMap.entrySet()) {

						String statusname = statusentry.getKey();

						Long count = roundCandidateService.getCountByPositionCodeAndRoundAndStatus(
								position.getPositionCode(), eachRound.getId(), statusname);
						data[stagerow][0] = "";
						data[stagerow][1] = "";
						data[stagerow][2] = "";
						data[stagerow][3] = "";
						data[stagerow][4] = "";
						data[stagerow][5] = "";
						data[stagerow][6] = "";

						data[statusrow][0] = "";
						data[statusrow][1] = "";
						data[statusrow][2] = "";
						data[statusrow][3] = "";
						data[statusrow][4] = "";
						data[statusrow][5] = "";
						data[statusrow][6] = "";

						// Stage in row 0, status in row 1, data in row 2
						data[stagerow][columnIndex] = roundstagename;
						data[statusrow][columnIndex] = statusname;
						data[datarow][columnIndex] = count;

						++columnIndex;

					}

				}

				stagerow = stagerow + 3;
				datarow = datarow + 3;
				statusrow = statusrow + 3;
				++serialnumber;

			}

			SimpleDateFormat formatterData = new SimpleDateFormat("dd-MM-yy");
			String fromExcelFile = formatterData.format(startdate);
			String toExcelFile = formatterData.format(enddate);

			String sheetName = "All_Status_And_Stages_" + user.getName() + "_" + fromExcelFile + "_To_" + toExcelFile;

			importExportService.resultSetToExcelExportForAllStatusAndStages(header, data, sheetName,user,
					null);

		}catch(Exception e){
			logger.error("got error in "+user.getEmail()+" while creating All status and all stages report"+e);
		}
	}

	
	// to get board default and custom status together
	public Map<String, String> getBoardStatusList() {
		Map<String, String> statusMap = new LinkedHashMap<>();
		for (BoardStatus status : BoardStatus.values()) {
			statusMap.put(status.name(), status.getDisplayName());
		}
		// get customs status and add it to list
		List<BoardCustomStatus> customStatus = boardCustomStatusService.findAll();
		if (null != customStatus && !customStatus.isEmpty()) {
			for (BoardCustomStatus boardCustomStatus : customStatus) {
				statusMap.put(boardCustomStatus.getStatusKey(), boardCustomStatus.getStatusName());
			}
		}
		return statusMap;
	}
	
}
