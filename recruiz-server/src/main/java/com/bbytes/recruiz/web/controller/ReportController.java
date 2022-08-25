package com.bbytes.recruiz.web.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.PerformanceReportTimePeriod;
import com.bbytes.recruiz.enums.ReportInterval;
import com.bbytes.recruiz.enums.ReportTimePeriod;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.rest.dto.models.ReportDropdownDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.WordCloudDTO;
import com.bbytes.recruiz.rest.dto.models.teamware_report.PrefTrend;
import com.bbytes.recruiz.rest.dto.models.teamware_report.RecPrefDTO;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.ImportExportService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.QueryService;
import com.bbytes.recruiz.service.ReportService;
import com.bbytes.recruiz.service.TeamService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.ReportConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.visualization.datasource.base.TypeMismatchException;

/**
 * Report Controller - Used for graphs and report purpose
 *
 * @author Akshay
 *
 */
/**
 * @author sourav-bb
 *
 */

@RestController
@RequestMapping(value = "/api/v1/report")
public class ReportController {

	private static final String DEPARTMENT_NAME = "Department Name";

	private static final String CLIENT_NAME = "Client Name";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReportService reportService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private QueryService queryService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ResourceLoader resourceloader;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	/**
	 * API to get graph data of overall candidate sourcing channels
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/candidate/sourcing", method = RequestMethod.GET)
	public RestResponse getOverallCandidateSourcingChannels() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallCandidateSourcingChannels();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of overall position status
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/position/status", method = RequestMethod.GET)
	public RestResponse getOverallPositionStatus() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();
		Report report = reportService.overallPositionStatus();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of overall client status
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/client/status", method = RequestMethod.GET)
	public RestResponse getOverallClientStatus() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallClientStatus();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get total count data of position, client and candidate
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/client/postion/candidate/count", method = RequestMethod.GET)
	public RestResponse getOverallClientPositionCandidateCount() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallClientPositionCandidateCount();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of overall positions sourcing channels
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/position/sourcing", method = RequestMethod.GET)
	public RestResponse getOverallPositionSourcingChannels() throws RecruizException {

		Report report = candidateService.getAllPositionSoucingChannelMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of overall positions rejection mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/position/rejection", method = RequestMethod.GET)
	public RestResponse getOverallPositionRejectionMix() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.getAllPositionCandidateRejectionMix();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get report data of average time to close all positions
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/position/avgtime/close", method = RequestMethod.GET)
	public RestResponse getOverallAverageTimeToClosePosition() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallAverageTimeToClosePosition();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get report data of overall client
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/client", method = RequestMethod.GET)
	public RestResponse getOverallClientReport() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallClientReport();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get report data of overall client and position
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/client/position", method = RequestMethod.GET)
	public RestResponse getClientwisePositionCountReport() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.clientwisePositionCountReport();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get report data of clientwise positions and recruiters count
	 *
	 * @param clientName
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/client/{clientName}/position/recruiters/{userEmail:.+}", method = RequestMethod.POST)
	public RestResponse getClientwisePositionAndRecruiterReport(@PathVariable("clientName") String clientName,
			@PathVariable("userEmail") String userEmail, @RequestParam(value = "userEmails", required = false) List<String> userEmails,
			@RequestParam(value = "clients", required = false) List<String> clients, @RequestBody ReportDropdownDTO reportDropdownDTO)
					throws RecruizException {

		List<String> userEmailList = new ArrayList<>();
		if (userEmails != null && !userEmails.isEmpty()) {
			userEmailList.addAll(userEmails);
		} else {
			userEmailList.add(userEmail);
		}

		List<String> clientNames = new ArrayList<>();
		if (clients != null && !clients.isEmpty()) {
			clientNames.addAll(clients);
		} else {
			clientNames.add(clientName);
		}

		Report report = clientwisePositionAndRecruiterReport(clientNames, reportDropdownDTO, userEmailList);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	
	
	@RequestMapping(value = "/client/{clientName}/position/recruiters/excel/{userEmail:.+}", method = RequestMethod.POST)
	public void getClientwisePositionAndRecruiterExcelReport(HttpServletResponse response,@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException, IOException {

		File report = null;
		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());

		report = reportService.getClientStatusReportExcel(interval);
		
		writeExcelReport(response, report);
		
	}
	
	
	
	
	/**
	 * @param clientName
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report clientwisePositionAndRecruiterReport(List<String> clientNames, ReportDropdownDTO reportDropdownDTO,
			List<String> userEmails) throws RecruizException {

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.clientwisePositionAndRecruiterReport(clientNames, reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate(), userEmails);
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.clientwisePositionAndRecruiterReport(clientNames, startReportDate, endReportDate, userEmails);
		}
		
		
		List<String> vendors = new ArrayList<>();
		
		if(report.getReportData()!=null)
		for (Object[] result : (Object[][])report.getReportData()) {
		
			try{
				Client client = clientService.getClient((String) result[0]);
				Position position = positionService.getPositionByNameAndClientName(client.getId(),(String) result[1]);
				
				if(position.getVendors().size()>0){
					String name = "";int k=0;
					for (Vendor vendor : position.getVendors()) {
						if(k==0){
							name = vendor.getName();
							k=1;
						}else{
							name = name+","+vendor.getName();
						}
						
					}
					
					vendors.add(name);
				}else{
					vendors.add("");
				}
				
				
			}catch(Exception e){
				e.printStackTrace();
				vendors.add("");
			}
				
		}
		
		report.setVendors(vendors);
		
		return report;
	}

	
	
	
	
	/**
	 * API to get graph data of sourcing channels mix by position with given
	 * time period
	 *
	 * @param positionCode
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/position/{positionCode}/sourcing", method = RequestMethod.POST)
	public RestResponse getPerPositionSoucingChannelMixByTimePeriod(@PathVariable("positionCode") String positionCode,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException, ParseException {

		Report report = perPositionSoucingChannelMixByTimePeriod(positionCode, reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param positionCode
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report perPositionSoucingChannelMixByTimePeriod(String positionCode, ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.perPositionSoucingChannelMixByTimePeriod(reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate(), positionCode);
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.perPositionSoucingChannelMixByTimePeriod(startReportDate, endReportDate, positionCode);
		}
		return report;
	}

	/**
	 * API to get graph data of average time to close by position with given
	 * time period
	 *
	 * @param positionCode
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/avgtime/close", method = RequestMethod.POST)
	public RestResponse getPerPositionAvgTimeToCloseByTimePeriod(@PathVariable("positionCode") String positionCode,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.perPositionAvgTimeToCloseByTimePeriod(reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate(), positionCode);
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.perPositionAvgTimeToCloseByTimePeriod(startReportDate, endReportDate, positionCode);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of candidate rejection mix by position with given
	 * time period
	 *
	 * @param positionCode
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/{positionCode}/rejection", method = RequestMethod.POST)
	public RestResponse getPerPositionCandidateRejectionByTimePeriod(@PathVariable("positionCode") String positionCode,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		Report report = perPositionCandidateRejectionByTimePeriod(positionCode, reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param positionCode
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report perPositionCandidateRejectionByTimePeriod(String positionCode, ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.perPositionCandidateRejectionByTimePeriod(reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate(), positionCode);
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.perPositionCandidateRejectionByTimePeriod(startReportDate, endReportDate, positionCode);
		}
		return report;
	}

	
	@RequestMapping(value = "/position/candidate/offerletter/report", method = RequestMethod.POST)
	private RestResponse positionwiseOfferLetterStatusReport(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		
		if(reportDropdownDTO.getTimePeriod()==null && reportDropdownDTO.getStartDate()==null){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setEndDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			
			 Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
			
		}else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			reportDropdownDTO.setStartDate(startEndDates[0]);
			reportDropdownDTO.setEndDate(startEndDates[1]);
		}

		return reportService.positionwiseCandidateOfferLetterStatusReport(reportDropdownDTO.getStartDate(), reportDropdownDTO.getEndDate());
		 
	}
	
	

	@RequestMapping(value = "/candidate/offerletter/reportByStatus", method = RequestMethod.POST)
	private RestResponse offerLetterStatusWiseLinkReport(@RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		
		Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField);
		
		if(reportDropdownDTO.getTimePeriod()==null && reportDropdownDTO.getStartDate()==null){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setEndDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			
			 Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
			
		}else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			reportDropdownDTO.setStartDate(startEndDates[0]);
			reportDropdownDTO.setEndDate(startEndDates[1]);
		}

		return reportService.offerLetterStatusWiseLinkReport(reportDropdownDTO.getStartDate(), reportDropdownDTO.getEndDate(),status, pageable);
		 
	}
	
	
	/**
	 * API to get report data of positionwise candidate status
	 *
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/position/candidate/status", method = RequestMethod.POST)
	public RestResponse getPositionwiseCandidateStatusReport(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		Report report = positionwiseCandidateStatusReport(reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report positionwiseCandidateStatusReport(ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		
		if(reportDropdownDTO.getTimePeriod()==null && reportDropdownDTO.getStartDate()==null){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setEndDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			
			 Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
			
		}else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			reportDropdownDTO.setStartDate(startEndDates[0]);
			reportDropdownDTO.setEndDate(startEndDates[1]);
		}

		return reportService.positionwiseCandidateStatusReport(reportDropdownDTO.getStartDate(), reportDropdownDTO.getEndDate());
		
	}

	/**
	 * API to get candidate key skills word cloud data
	 *
	 * @return
	 */
	@RequestMapping(value = "/candidate/wordcloud", method = RequestMethod.GET)
	public RestResponse getCandidateKeySkillsWordCloud() {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		List<WordCloudDTO> wordCloudList = reportService.getCandidateKeySkillWordCloud();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, wordCloudList);
		return response;
	}

	/**
	 * API to get graph data of candidate pool growth periodically
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/candidate/pool/periodically", method = RequestMethod.GET)
	public RestResponse getCandidateTotalPoolPeriodically() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.candidateTotalPoolPeriodically();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of overall candidate sourcing channels mix by given
	 * time period
	 *
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/candidate/pool/sourcing", method = RequestMethod.POST)
	public RestResponse getCandidateOverallSourcingChannelsByTimperiod(@RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = candidateOverallSourcingChannelsByTimperiod(reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report candidateOverallSourcingChannelsByTimperiod(ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;
		String timePeriod = "INTERVAL 1 MONTH";
		
		
		
		if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Today")){
			timePeriod = "CURDATE()";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_Week")){
			timePeriod = "INTERVAL 7 DAY";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_3_Months")){
			timePeriod = "INTERVAL 3 MONTH";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_3_Months")){
			timePeriod = "INTERVAL 6 MONTH";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("12 MONTH")){
			timePeriod = "INTERVAL 12 MONTH";
		}
			

		// if timeperiod is not provided from UI it will set as 'Last Month'
		
		/*if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}*/
		
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Custom")) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.candidateOverallSourcingChannelsByTimperiod(reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate());
		} else {
		/*	Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];*/
			report = reportService.candidateOverallSourcingChannelsByTimperiodWithOutCustom(timePeriod);
		}
		return report;
	}

	/**
	 * API to get graph data of overall candidate gender mix by given time
	 * period
	 *
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/candidate/pool/gender", method = RequestMethod.POST)
	public RestResponse getCandidateOverallGenderMixByTimperiod(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		Report report = candidateOverallGenderMixByTimperiod(reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report candidateOverallGenderMixByTimperiod(ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;
		String timePeriod = "INTERVAL 1 MONTH";
		
		
		
		if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Today")){
			timePeriod = "CURDATE()";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_Week")){
			timePeriod = "INTERVAL 7 DAY";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_3_Months")){
			timePeriod = "INTERVAL 3 MONTH";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Last_6_Months")){
			timePeriod = "INTERVAL 6 MONTH";
		}else if(reportDropdownDTO.getTimePeriod().equalsIgnoreCase("12 MONTH")){
			timePeriod = "INTERVAL 12 MONTH";
		}
			

		// if timeperiod is not provided from UI it will set as 'Last Month'
		
		/*if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}*/
		
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || reportDropdownDTO.getTimePeriod().equalsIgnoreCase("Custom")) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.candidateOverallGenderMixByTimperiod(reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate());
		} else {
			//Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			//Date startReportDate = startEndDates[0];
			//Date endReportDate = startEndDates[1];
			report = reportService.candidateOverallGenderMixByTimperiod(timePeriod);
		}
		return report;
	}

	/**
	 * API to get graph data of candidate pool growth month wise
	 *
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/candidate/pool", method = RequestMethod.POST)
	public RestResponse getMonthwiseCandidatePool(@RequestParam List<Long> teamIds, @RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {
		Map<String, Object> responseMap = new HashMap<>();
		Report report = monthwiseCandidatePool(reportDropdownDTO.getTimePeriod());

		if ((teamIds.size() == 1 && teamIds.get(0) == -1) || permissionService.isSuperAdmin()) {
			teamIds = teamService.getAllTeamIds();
		}
		if (null == teamIds || teamIds.isEmpty()) {
			return new RestResponse(false, ErrorHandler.NO_TEAM_ADDED, ErrorHandler.NO_TEAM_FOUND);
		}

		Date[] startEndDates = new Date[2];
		// teamIds = getAllChildrenTeamIds(team, teamIds);
		startEndDates = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		responseMap.put("positionGraphData", positionService.getDashboardPositionGraphData(teamIds, startEndDates[0], startEndDates[1]));
		responseMap.put("clientGraphData", positionService.getDashboardClientGraphData(teamIds, startEndDates[0], startEndDates[1]));
		responseMap.put("candidate", report);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, responseMap);
		return response;
	}

	/**
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report monthwiseCandidatePool(String timePeriod) throws RecruizException {

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = PerformanceReportTimePeriod.Last_3_Months.getInterval();
		}
		Report report = reportService.monthwiseCandidatePool(timePeriod);
		return report;
	}

	/**
	 * API to get graph data of candidates pool by recruiters
	 *
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/candidate/pool", method = RequestMethod.GET)
	public RestResponse getMonthwiseCandidatePoolByRecruiters(@RequestParam(value = "timePeriod", required = false) String timePeriod)
			throws RecruizException {

		Report report = monthwiseCandidatePoolByRecruiters(timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report monthwiseCandidatePoolByRecruiters(String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.monthwiseCandidatePoolByRecruiters(timePeriod);
		return report;
	}

	/**
	 * API to get graph data of profile forwarded by recruiters
	 *
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/profileForward", method = RequestMethod.GET)
	public RestResponse getMonthwiseProfileForwardedByRecruiters(@RequestParam(value = "timePeriod", required = false) String timePeriod)
			throws RecruizException {

		Report report = monthwiseProfileForwardedByRecruiters(timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report monthwiseProfileForwardedByRecruiters(String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.monthwiseProfileForwardedByRecruiters(timePeriod);
		return report;
	}

	/**
	 * API to get graph data of positions closed by recruiters
	 *
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/positionsClosed", method = RequestMethod.GET)
	public RestResponse getMonthwisePositionsClosedByRecruiters(@RequestParam(value = "timePeriod", required = false) String timePeriod)
			throws RecruizException {

		Report report = monthwisePositionsClosedByRecruiters(timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report monthwisePositionsClosedByRecruiters(String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.monthwisePositionsClosedByRecruiters(timePeriod);
		return report;
	}

	/**
	 * API to get graph data of interviews scheduled by recruiters
	 *
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/interviewSchedule", method = RequestMethod.GET)
	public RestResponse getMonthwiseInterviewsScheduledByRecruiters(@RequestParam(value = "timePeriod", required = false) String timePeriod)
			throws RecruizException {

		Report report = monthwiseInterviewsScheduledByRecruiters(timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report monthwiseInterviewsScheduledByRecruiters(String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.monthwiseInterviewsScheduledByRecruiters(timePeriod);
		return report;
	}

	/**
	 * API to get graph data of sourcing channels mix by recruiters for given
	 * time period
	 *
	 * @param userEmail
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/{userEmail:.+}/sourcing", method = RequestMethod.GET)
	public RestResponse getRecruiterswiseCandidateSourcingChannels(@PathVariable("userEmail") String userEmail,
			@RequestParam(value = "timePeriod", required = false) String timePeriod) throws RecruizException {

		Report report = recruiterswiseCandidateSourcingChannels(userEmail, timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param userEmail
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	private Report recruiterswiseCandidateSourcingChannels(String userEmail, String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.recruiterswiseCandidateSourcingChannels(userEmail, timePeriod);
		return report;
	}

	/**
	 * API to get report data of count by recruiters for given time period
	 *
	 * @param userEmail
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/recruiters/{userEmail:.+}/count", method = RequestMethod.GET)
	public RestResponse getRecruiterswiseCountReport(@PathVariable("userEmail") String userEmail,
			@RequestParam(value = "timePeriod", required = false) String timePeriod) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.recruiterswiseCountReport(userEmail, timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of candidate board count by recruiters for given
	 * time period
	 *
	 * @param userEmail
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/recruiters/{userEmail:.+}/board", method = RequestMethod.GET)
	public RestResponse getRecruiterswiseCandidateBoardReport(@PathVariable("userEmail") String userEmail,
			@RequestParam(value = "timePeriod", required = false) String timePeriod) throws RecruizException, TypeMismatchException {

		Report report = recruiterswiseCandidateBoardReport(userEmail, timePeriod);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param userEmail
	 * @param timePeriod
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	private Report recruiterswiseCandidateBoardReport(String userEmail, String timePeriod) throws RecruizException, TypeMismatchException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		// if timeperiod is not provided from UI it will set as 'Last 3 Months'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriod = ReportInterval.Last_3_Months.getIntervalValue();
		}
		Report report = reportService.recruiterswiseCandidateBoardReport(userEmail, timePeriod);
		return report;
	}

	/**
	 * API to get graph data of overall prospect status
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/proospect/status", method = RequestMethod.GET)
	public RestResponse getOverallProspectStatus() {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Report report = reportService.overallProspectStatus();

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * API to get graph data of prospects pool by given time period
	 *
	 * @param timePeriod
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/prospect/pool", method = RequestMethod.POST)
	public RestResponse getUserwiseProspectsPoolByTimperiod(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		Report report = userwiseProspectsPoolByTimperiod(reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report userwiseProspectsPoolByTimperiod(ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.userWiseProspectsPoolByTimperiod(reportDropdownDTO.getStartDate(), reportDropdownDTO.getEndDate());
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.userWiseProspectsPoolByTimperiod(startReportDate, endReportDate);
		}
		return report;
	}

	/**
	 * API to get report data of user wise prospect reports
	 *
	 * @param userEmail
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/prospect/{userEmail:.+}", method = RequestMethod.POST)
	public RestResponse getUserwiseProspectReport(@PathVariable(value = "userEmail", required = false) String userEmail,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		List<String> userEmails = new ArrayList<>();
		userEmails.add(userEmail);

		Report report = userwiseProspectReport(userEmails, reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 *
	 * @param userEmail
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report userwiseProspectReport(List<String> userEmail, ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.userWiseProspectReport(userEmail.get(0), reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate());
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.userWiseProspectReport(userEmail.get(0), startReportDate, endReportDate);
		}
		return report;
	}

	/**
	 * API to get report data of invoice report
	 *
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/overall/invoice/{clientName}/{invoiceStatus}", method = RequestMethod.POST)
	public RestResponse getInvoiceReport(@PathVariable("clientName") String clientName, @PathVariable("invoiceStatus") String invoiceStatus,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		Report report = invoiceReport(clientName, invoiceStatus, reportDropdownDTO);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, report);
		return response;
	}

	/**
	 *
	 * @param reportDropdownDTO
	 * @return
	 * @throws RecruizException
	 */
	private Report invoiceReport(String clientName, String invoiceStatus, ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		Integer timePeriodValue;
		Report report = null;

		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (reportDropdownDTO.getTimePeriod() == null || reportDropdownDTO.getTimePeriod().isEmpty()) {
			timePeriodValue = ReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = ReportTimePeriod.valueOf(reportDropdownDTO.getTimePeriod()).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (ReportTimePeriod.Custom.equals(reportDropdownDTO.getTimePeriod()) || timePeriodValue == -1) {
			if (reportDropdownDTO.getStartDate() != null && reportDropdownDTO.getEndDate() != null)
				report = reportService.invoiceReport(clientName, invoiceStatus, reportDropdownDTO.getStartDate(),
						reportDropdownDTO.getEndDate());
		} else {
			Date[] startEndDates = DateTimeUtils.getStartDateEndDate(timePeriodValue);
			Date startReportDate = startEndDates[0];
			Date endReportDate = startEndDates[1];
			report = reportService.invoiceReport(clientName, invoiceStatus, startReportDate, endReportDate);
		}
		return report;
	}

	/**
	 * API to get drop down list with position name and position code
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/dropdown/positions", method = RequestMethod.GET)
	public RestResponse getPositionsWithPositionCode() throws RecruizException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		List<Position> positionListFromDB = positionRepository.findAll();
		List<BaseDTO> positionList = new ArrayList<BaseDTO>();

		for (Position position : positionListFromDB) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(position.getPositionCode());
			baseDTO.setValue(position.getTitle());
			positionList.add(baseDTO);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, positionList);
		return response;
	}

	/**
	 * API to get drop down list with user name and user email
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/dropdown/users", method = RequestMethod.GET)
	public RestResponse getUsers() throws RecruizException {

		List<User> userListFromDB = userService.getJoinedAppUsers();

		List<BaseDTO> userList = new ArrayList<BaseDTO>();

		for (User user : userListFromDB) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(user.getEmail());
			baseDTO.setValue(user.getName());
			userList.add(baseDTO);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, userList);
		return response;
	}

	/**********************
	 * To Download Report *
	 **********************
	 * @param response
	 * @param reportName
	 * @param clientName
	 * @param reportDropdownDTO
	 * @throws RecruizException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/download", headers = "Accept=*/*", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadExcelReport(HttpServletResponse response, @RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(value = "reportName") String reportName, @RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "userEmail", required = false) String userEmail)
					throws RecruizException, IOException, InvalidFormatException, TypeMismatchException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		File exceltFile = null;

		List<String> userEmails = new ArrayList<>();
		userEmails.add(userEmail);

		List<String> clientNames = new ArrayList<>();
		clientNames.add(clientName);

		// exporting excel report file
		exceltFile = getExcelReportFile(reportName, reportDropdownDTO, clientNames, userEmails, positionCode, exceltFile);

		writeExcelReport(response, exceltFile);

	}

	/**********************
	 * To Download Invoice Report *
	 **********************
	 * @param response
	 * @param reportName
	 * @param clientName
	 * @param invoiceStatus
	 * @param reportDropdownDTO
	 * @throws RecruizException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws TypeMismatchException
	 */
	@RequestMapping(value = "/invoice/download", headers = "Accept=*/*", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadInvoiceExcelReport(HttpServletResponse response, @RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(value = "reportName") String reportName, @RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "invoiceStatus", required = false) String invoiceStatus)
					throws RecruizException, IOException, InvalidFormatException, TypeMismatchException {

		// checking app user and view report permission
		hasAppUserAndReportPermission();

		File exceltFile = null;

		Report report = invoiceReport(clientName, invoiceStatus, reportDropdownDTO);
		if (report != null) {
			exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(), (Object[][]) report.getReportData(),
					"Invoice Report", null);
		}

		writeExcelReport(response, exceltFile);

	}

	private void writeExcelReport(HttpServletResponse response, File exceltFile) throws IOException {
		if (exceltFile == null) {
			// if excel file not present then returning no data file
			InputStream fileStream = resourceloader.getResource(GlobalConstants.NO_DATA_FILE).getInputStream();
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "recruiz_no_data.xlsx"));
			response.setHeader("recruiz-file-name", "recruiz_no_data.xlsx");
			// writing file into stream and download
			IOUtils.copy(fileStream, response.getOutputStream());
			response.flushBuffer();
			return;
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
		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + getPathFromServer.getFileName().toString() + "\""));

		response.setContentLength((int) getPathFromServer.toFile().length());
		response.setHeader("recruiz-file-name", getPathFromServer.getFileName().toString());
		Files.copy(getPathFromServer, response.getOutputStream());
	}

	/**
	 * @param reportDropdownDTO
	 * @param reportName
	 * @param clientName
	 * @param exceltFile
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws TypeMismatchException
	 */
	private File getExcelReportFile(String reportName, ReportDropdownDTO reportDropdownDTO, List<String> clientNames,
			List<String> userEmails, String positionCode, File exceltFile)
					throws RecruizException, IOException, InvalidFormatException, TypeMismatchException {

		// checking for organization is corporate or agency
		Organization org = organizationService.getCurrentOrganization();
		Report report = null;

		switch (reportName) {
		case ReportConstants.CLIENT_STATUS_REPORT:
			report = reportService.overallClientReport();
			if (report != null) {
				Object[] metaData = (Object[]) report.getMetaData();

				// orgType is corporate then - department else client
				if (GlobalConstants.SIGNUP_MODE_CORPORATE.equals(org.getOrgType()))
					metaData[0] = DEPARTMENT_NAME;
				else
					metaData[0] = CLIENT_NAME;

				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Status Report", null);
			}
			break;

		case ReportConstants.CLIENT_RECRUITMENT_STATUS_REPORT:
			report = clientwisePositionAndRecruiterReport(clientNames, reportDropdownDTO, userEmails);
			if (report != null) {
				Object[] metaData = (Object[]) report.getMetaData();

				// orgType is corporate then - department else client
				if (GlobalConstants.SIGNUP_MODE_CORPORATE.equals(org.getOrgType()))
					metaData[0] = DEPARTMENT_NAME;
				else
					metaData[0] = CLIENT_NAME;

				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Report", null);
			}
			break;
		case ReportConstants.CLIENT_POSITION_STATUS_REPORT:
			report = reportService.clientwisePositionCountReport();
			if (report != null) {
				Object[] metaData = (Object[]) report.getMetaData();

				// orgType is corporate then - department else client
				if (GlobalConstants.SIGNUP_MODE_CORPORATE.equals(org.getOrgType()))
					metaData[0] = DEPARTMENT_NAME;
				else
					metaData[0] = CLIENT_NAME;

				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Positionwise Report", null);
			}
			break;
		case ReportConstants.GENERAL_CLIENT_STATUS_STAT:
			report = reportService.overallClientStatus();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Status Mix", null);
			}
			break;
		case ReportConstants.GENERAL_POSITION_STATUS_STAT:
			report = reportService.overallPositionStatus();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Position Status", null);
			}
			break;
		case ReportConstants.GENERAL_CANDIDATE_SOURCING_STAT:
			report = reportService.overallCandidateSourcingChannels();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Candidate Sourcing", null);
			}
			break;
		case ReportConstants.GENERAL_ALL_POSITION_SOURCING_STAT:
			report = reportService.getAllPositionSoucingChannelMix();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Overall Position Sourcing", null);
			}
			break;
		case ReportConstants.GENERAL_ALL_POSITION_REJECTION_STAT:
			report = reportService.getAllPositionCandidateRejectionMix();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Overall Position Rejection", null);
			}
			break;
		case ReportConstants.CANDIDATE_POOL_PERIODICALLY_STAT:
			report = reportService.candidateTotalPoolPeriodically();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Candidate Database Timelapse", null);
			}
			break;
		case ReportConstants.MONTHWISE_CANDIDATE_POOL_STAT:
			report = monthwiseCandidatePool(reportDropdownDTO.getTimePeriod());
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Candidate Database Growth", null);
			}
			break;
		case ReportConstants.CANDIDATE_SOURCING_STAT:
			report = candidateOverallSourcingChannelsByTimperiod(reportDropdownDTO);
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Candidate Sourcing", null);
			}
			break;
		case ReportConstants.CANDIDATE_GENDER_STAT:
			report = candidateOverallGenderMixByTimperiod(reportDropdownDTO);
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Candidate Gender Mix", null);
			}
			break;
		case ReportConstants.RECRUITER_CANDIDATE_SOURCING_STAT:
			report = monthwiseCandidatePoolByRecruiters(reportDropdownDTO.getTimePeriod());
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Candidate Sourcing", null);
			}
			break;
		case ReportConstants.RECRUITER_INTERVIEWS_STAT:
			report = monthwiseInterviewsScheduledByRecruiters(reportDropdownDTO.getTimePeriod());
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Interview Scheduled", null);
			}
			break;
		case ReportConstants.RECRUITER_PROFILE_FORWARDED_STAT:
			report = monthwiseProfileForwardedByRecruiters(reportDropdownDTO.getTimePeriod());
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Profile Forwarded", null);
			}
			break;
		case ReportConstants.RECRUITER_POSITION_CLOSED_STAT:
			report = monthwisePositionsClosedByRecruiters(reportDropdownDTO.getTimePeriod());
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Position Closed", null);
			}
			break;
		case ReportConstants.RECRUITER_PIPELINE_STAT:
			report = recruiterswiseCandidateBoardReport(userEmails.get(0), reportDropdownDTO.getTimePeriod());
			if (report != null && report.getReportData() != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Recruiters Pipeline data", null);
			}
			Report newReport = recruiterswiseCandidateSourcingChannels(userEmails.get(0), reportDropdownDTO.getTimePeriod());
			if (newReport != null && newReport.getReportData() != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) newReport.getMetaData(),
						(Object[][]) newReport.getReportData(), "Recruiters Sourcing data", exceltFile);
			}

			break;
		case ReportConstants.POSITIONWISE_STAT:
			report = perPositionSoucingChannelMixByTimePeriod(positionCode, reportDropdownDTO);
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Positionwise sourcing", null);
			}
			Report positionReport = perPositionCandidateRejectionByTimePeriod(positionCode, reportDropdownDTO);
			if (positionReport != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) positionReport.getMetaData(),
						(Object[][]) positionReport.getReportData(), "Positionwise rejection mix", exceltFile);
			}

			break;
		case ReportConstants.POSITION_CANDIDATE_STATUS_REPORT:
			report = positionwiseCandidateStatusReport(reportDropdownDTO);
			if (report != null) {
				Object[] metaData = (Object[]) report.getMetaData();

				// orgType is corporate then - department else client
				if (GlobalConstants.SIGNUP_MODE_CORPORATE.equals(org.getOrgType()))
					metaData[0] = DEPARTMENT_NAME;
				else
					metaData[0] = CLIENT_NAME;

				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Positionwise Candidate Status Report", null);
			}
			break;
		case ReportConstants.PROSPECT_STATUS_REPORT:
			report = reportService.overallProspectStatus();
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Prospect Status", null);
			}
			break;
		case ReportConstants.USER_PROSPECT_POOL_REPORT:
			report = userwiseProspectsPoolByTimperiod(reportDropdownDTO);
			if (report != null) {
				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Prospect By Team", null);
			}
			break;
		case ReportConstants.USERWISE_PROSPECT_REPORT:
			report = userwiseProspectReport(userEmails, reportDropdownDTO);
			if (report != null) {
				/*SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
				Object[][] resultsetData = (Object[][]) report.getReportData();
			    Date date = new Date();
				for (Object[] result : resultsetData) {
					if (result[4] instanceof String) {
						try {
							date = formatter.parse(result[4].toString());
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}else{
						Timestamp stamp =  (Timestamp) result[4];
						date = new Date(stamp.getTime());
					}
					result[4] = formatter.format(date);
				}*/


				exceltFile = importExportService.resultSetToExcelExport((Object[]) report.getMetaData(),
						(Object[][]) report.getReportData(), "Prospect Report", null);
			}
			break;
		}
		return exceltFile;
	}

	/**
	 * @throws RecruizPermissionDeniedException
	 */
	private void hasAppUserAndReportPermission() throws RecruizPermissionDeniedException {

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (!permissionService.hasReportPermission()) {
			throw new RecruizPermissionDeniedException(ErrorHandler.PERMISSION_DENIED);
		}
	}

	@RequestMapping(value = "/recruiter/profile", method = RequestMethod.POST)
	public RestResponse getRecuriterProfile(@RequestParam Long clientId, @RequestParam(required = false) String status,
			@RequestParam String hrEmail, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		try {
			Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			Date startDate = dateArray[0];
			Date endDate = dateArray[1];

			User user = userService.getUserByEmail(hrEmail);

			Map<String, Object> responseObject = queryService.getRecruiterProfileCount(clientId, user, startDate, endDate, status);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, responseObject);
			return response;
		} catch (RecruizException rex) {
			return new RestResponse(false, rex.getMessage(), rex.getErrConstant());
		} catch (Exception ex) {
			return new RestResponse(false, ErrorHandler.RECRUITEMT_PROFILE_ERROR_OCCURED, ErrorHandler.RECRUITMENT_PROFILE_ERROR);
		}
	}



	@RequestMapping(value = "/position/downloadPerformanceReportAsExcel", method = RequestMethod.POST)
	public void getPerformanceReportAsExcel(@RequestParam Long clientId, @RequestParam(required = false) String status,
			@RequestParam String hrEmail, @RequestBody ReportDropdownDTO reportDropdownDTO,HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		User user = userService.getUserByEmail(hrEmail);

		//	Map<String, Object> responseObject = queryService.getRecruiterProfileCount(clientId, user, startDate, endDate, status);

		File excelFile = reportService.getPerformanceReportAsExcel(clientId, user, startDate, endDate, status);
		writeExcelReport(response, excelFile);

	}



	@RequestMapping(value = "/position/downloadOutstandingPositionAsExcel", method = RequestMethod.POST)
	public void downloadOutstandingPositionAsExcel(@RequestParam(required = false) String clientName,
			@RequestParam(required = false) String clientId, @RequestParam(required = false) String status,
			@RequestParam(required = false) String positionName, @RequestBody ReportDropdownDTO reportDropdownDTO,HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval == 0) {
			return ;
		}

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		List<Client> clients = new ArrayList<>();
		if (null == clientName || clientName.trim().isEmpty()) {
			clients.addAll(clientService.findAll());
		} else {
			List<String> clientNames= Arrays.asList(clientName.split("\\s*,\\s*"));
			List<String> clientNameList = new ArrayList<>();
			for (String clientNameString : clientNames) {
				clientNameList.add(clientNameString.replaceAll("\'",""));
			}
			clients.addAll(clientService.getClientByNameIn(clientNameList));
		}

		List<String> positionCodes = new ArrayList<>();
		if(positionName!=null)
			positionCodes= Arrays.asList(positionName.split("\\s*,\\s*"));

		File report = reportService.downloadOutstandingPositionAsExcel(clients, startDate, endDate,positionCodes);

		writeExcelReport(response, report);
	}




	//@author - Sajin (added to show custom statuses in Recruiter performance table)
	@RequestMapping(value = "/recruiter/profile/custom", method = RequestMethod.POST)
	public RestResponse getRecuriterProfileCustom(@RequestParam Long clientId, @RequestParam(required = false) String status,
			@RequestParam String hrEmail, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		try {
			Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			Date startDate = dateArray[0];
			Date endDate = dateArray[1];

			User user = userService.getUserByEmail(hrEmail);

			Map<String, Object> responseObject = queryService.getRecruiterProfileCountCustom(clientId, user, startDate, endDate, status);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, responseObject);
			return response;
		} catch (RecruizException rex) {
			return new RestResponse(false, rex.getMessage(), rex.getErrConstant());
		} catch (Exception ex) {
			return new RestResponse(false, ErrorHandler.RECRUITEMT_PROFILE_ERROR_OCCURED, ErrorHandler.RECRUITMENT_PROFILE_ERROR);
		}
	}

	@RequestMapping(value = "/recruiter/stat", method = RequestMethod.POST)
	public RestResponse getRecuriterProfileStat(@RequestParam String hrEmail, @RequestParam(required = false) String clientName,
			@RequestParam(required = false) String clientId, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		try {
			User user = userService.getUserByEmail(hrEmail);

			Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());

			Date startDate = dateArray[0];
			Date endDate = dateArray[1];

			Map<String, Object> responseObject = queryService.getRecruiterEntiryStat(user, clientName, clientId, startDate, endDate);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, responseObject);
			return response;
		} catch (RecruizException rex) {
			return new RestResponse(false, rex.getMessage(), rex.getErrConstant());
		} catch (Exception ex) {
			return new RestResponse(false, ErrorHandler.RECRUITEMT_PROFILE_ERROR_OCCURED, ErrorHandler.RECRUITMENT_PROFILE_ERROR);
		}
	}

	@RequestMapping(value = "/candidate/performance", method = RequestMethod.POST)
	public RestResponse getPerformaceReportCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder, @RequestParam String hrEmail,
			@RequestParam(required = false) String clientName, @RequestParam(required = false) String clientId,
			@RequestParam(required = false) String status, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		User user = userService.getUserByEmail(hrEmail);

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());

		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		List<Long> candidateIds = queryService.getPerformaceReportCandidate(user, clientName, clientId, status, startDate, endDate);

		Page<Candidate> candidateList = candidateService.getAllCandidate(candidateIds,
				pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));

		// Need for lazy loading
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		}
		candidateService.attachCurrentPosition(candidateList.getContent());

		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, candidateList);

		return candidateResponse;
	}

	@RequestMapping(value = "/pipeline/all/excel", method = RequestMethod.GET)
	public void getPerformaceReportCandidate(HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		File excelFile = reportService.getBoardReport();
		writeExcelReport(response, excelFile);
	}

	@RequestMapping(value = "/client/position/excel", method = RequestMethod.GET)
	public void getClientPositionReport(HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		File excelFile = reportService.getClientPositionReport();
		writeExcelReport(response, excelFile);
	}


	@RequestMapping(value = "/client/position/saveCustomReportTimePeriod", method = RequestMethod.GET)
	public void saveCustomReportTimePeriod(@RequestParam("reportTimePeriod") String reportTimePeriod,HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		reportService.saveCustomReportTimePeriod(reportTimePeriod);
		/*if(status){
			response.setHeader("report-status", "true");
			return;
		}else{
			response.setHeader("report-status", "false");
			return;
		}*/
	}

	@RequestMapping(value = "/client/position/checkReportStatus", method = RequestMethod.GET)
	public RestResponse checkAllStageAllStatusReportStatus(HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		File excelFile = reportService.getPositionAllStageAllStatusExcelReport(response);
		User user = userRepository.findOneByEmail(userService.getLoggedInUserEmail());

		if(user!=null){
			if(user.getReporttimeperiod()==null){
				return new RestResponse(RestResponse.FAILED, "null");
			}
		}

		if(excelFile==null){
			return new RestResponse(RestResponse.FAILED, "false");
		}

		return new RestResponse(RestResponse.SUCCESS, excelFile.toPath().getFileName().toString());
	}


	@RequestMapping(value = "/client/position/getReportTimePeriodByDefault", method = RequestMethod.GET)
	public RestResponse getReportTimePeriodByDefault(HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		User user = userRepository.findOneByEmail(userService.getLoggedInUserEmail());
		if(user!=null){
			String timeperiod = user.getReporttimeperiod();
			if(timeperiod!=null)
				return new RestResponse(RestResponse.SUCCESS, user.getReporttimeperiod());	
		}
		return new RestResponse(RestResponse.FAILED, "false");
	}


	@RequestMapping(value = "/client/position/allstagestatus", method = RequestMethod.POST)
	public void getPositionAllStageAllStatus(HttpServletResponse response) throws RecruizException, InvalidFormatException, IOException {

		File excelFile = reportService.getPositionAllStageAllStatusExcelReport(response);
		writeExcelReport(response, excelFile);
	}




	@RequestMapping(value = "/position/status/overall", method = RequestMethod.POST)
	public RestResponse getOverAllPositionStatus(@RequestParam(required = false) String clientName,
			@RequestParam(required = false) String clientId, @RequestParam(required = false) String status,
			@RequestParam(required = false) String positionName, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval == 0) {
			return new RestResponse(false, "Not Implemented", "not_implemented");
		}

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		List<Client> clients = new ArrayList<>();
		if (null == clientName || clientName.trim().isEmpty()) {
			clients.addAll(clientService.findAll());
		} else {
			List<String> clientNames= Arrays.asList(clientName.split("\\s*,\\s*"));
			List<String> clientNameList = new ArrayList<>();
			for (String clientNameString : clientNames) {
				clientNameList.add(clientNameString.replaceAll("\'",""));
			}
			clients.addAll(clientService.getClientByNameIn(clientNameList));
		}

		List<String> positionCodes = new ArrayList<>();
		if(positionName!=null)
			positionCodes= Arrays.asList(positionName.split("\\s*,\\s*"));

		Map<String, Object> report = reportService.getOverAllPositionStatus(clients, startDate, endDate,positionCodes);

		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);
		return candidateResponse;
	}

	@RequestMapping(value = "/resource/request/outstanding", method = RequestMethod.POST)
	public RestResponse getResourceRequestsOutstanding(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval <= 0) {
			return new RestResponse(false, "Not Implemented", "not_implemented");
		}
		List<Object> report = reportService.getResourceRequestsOutstanding(interval);
		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);

		return candidateResponse;
	}

	@RequestMapping(value = "/position/hiring/pattern", method = RequestMethod.POST)
	public RestResponse getHiringPatternReport(@RequestParam(required = false) String clientName,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval == 0) {
			return null;
		}

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		if (interval <= 0) {
			return new RestResponse(false, "Not Implemented", "not_implemented");
		}

		List<Object> report = reportService.getHiringPatternReport(reportDropdownDTO, clientName, interval,startDate, endDate );

		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);
		return candidateResponse;
	}


	private int getTimePeriod(String timePeriod) {
		int interval = 0;

		// from start will return data for last 2 years max
		if (timePeriod.equalsIgnoreCase("fromStart")) {
			return 24;
		} else if (timePeriod.equalsIgnoreCase(PerformanceReportTimePeriod.Last_Month.name())) {
			return 1;
		} else if (timePeriod.equalsIgnoreCase(PerformanceReportTimePeriod.Last_3_Months.name())) {
			return 3;
		} else if (timePeriod.equalsIgnoreCase(PerformanceReportTimePeriod.Last_6_Months.name())) {
			return 6;
		} else if (timePeriod.equalsIgnoreCase(PerformanceReportTimePeriod.Last_12_Months.name())) {
			return 12;
		} else if (timePeriod.equalsIgnoreCase(PerformanceReportTimePeriod.Custom.name())) {
			return -1;
		}
		return interval;
	}

	@RequestMapping(value = "/position/request/clients", method = RequestMethod.POST)
	public RestResponse getPositionRequestsByClient(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval <= 0) {
			return new RestResponse(false, "Not Implemented", "not_implemented");
		}

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		Map<String, Object> report = reportService.getPositionRequestsByClient(interval, startDate, endDate);
		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);

		return candidateResponse;
	}

	//Sajin

	@RequestMapping(value = "/allreport/excel", method = RequestMethod.POST)
	public void getExcelReports(HttpServletResponse response, @RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(value = "reportName") String reportName, @RequestParam(value = "clientName", required = false) String clientName,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "userEmail", required = false) String userEmail) throws RecruizException, InvalidFormatException, IOException {


		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		//		if (interval == 0) {
		//			return new RestResponse(false, "Not Implemented", "not_implemented");
		//		}

		Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Date startDate = dateArray[0];
		Date endDate = dateArray[1];

		if (reportName.equalsIgnoreCase("generalpositionopening"))
		{
			File report = reportService.getExcelPositionRequestsByClient(interval, startDate, endDate);
			writeExcelReport(response, report);


		} else if (reportName.equalsIgnoreCase("generalhiringpattern"))
		{
			File report = reportService.getExcelHiringPatternReport(clientName, interval); 
			writeExcelReport(response, report);

		} else if (reportName.equalsIgnoreCase("clientstatus"))
		{
			File report = reportService.getExcelClientStatusReport(interval); 
			writeExcelReport(response, report);


		} else if (reportName.equalsIgnoreCase("clientrecruitmentstatusreport"))
		{
			File report = reportService.getExcelClientStatusReport(interval); 
			writeExcelReport(response, report);

		}  


	}


	@RequestMapping(value = "/client/status/report", method = RequestMethod.POST)
	public RestResponse getClientStatusReport(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval <= 0) {
			return new RestResponse(false, "Not Implemented", "not_implemented");
		}

		List<Map<String, Object>> report = reportService.getClientStatusReport(interval);
		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);
		return candidateResponse;
	}

	@RequestMapping(value = "/client/position/opened/graph", method = RequestMethod.POST)
	public RestResponse getClientOpenedPositionGraph(@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {

		int interval = getTimePeriod(reportDropdownDTO.getTimePeriod());
		if (interval <= 0) {
			return new RestResponse(false, "Graph not available for this range", "not_implemented");
		}

		List<Map<String, Object>> report = reportService.getClientStatusReport(interval);
		RestResponse candidateResponse = new RestResponse(RestResponse.SUCCESS, report);
		return candidateResponse;
	}

	private Date[] getMonthsFromInterval(int interval) {
		Date[] dates = new Date[2];

		DateTime dateTime = new DateTime();
		dateTime.minusMonths(interval);

		Date startDate = dateTime.toDate();
		startDate.setDate(1);
		Date endDate = dateTime.toDate();
		endDate.setDate(30);

		dates[0] = startDate;
		dates[1] = endDate;

		return dates;
	}

	@RequestMapping(value = "/team/performance", method = RequestMethod.POST)
	public RestResponse getTeamPerformanceReport(@RequestParam Long teamId, @RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {

		try {
			Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			Date startDate = dateArray[0];
			Date endDate = dateArray[1];
			List<Long> teamIds = new ArrayList<>();
			Team team = teamService.findOne(teamId);
			teamIds = getAllChildrenTeamIds(team, teamIds);

			Map<String, Object> responseObject = queryService.getPositionStatMapForTeam(teamIds, startDate, endDate);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, responseObject);
			return response;
		} catch (RecruizException rex) {
			return new RestResponse(false, rex.getMessage(), rex.getErrConstant());
		} catch (Exception ex) {
			ex.printStackTrace();
			return new RestResponse(false, ErrorHandler.RECRUITEMT_PROFILE_ERROR_OCCURED, ErrorHandler.RECRUITMENT_PROFILE_ERROR);
		}
	}

	@RequestMapping(value = "/team/stat", method = RequestMethod.POST)
	public RestResponse getRecuriterProfileStat(@RequestParam Long teamId, @RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {

		try {

			Date[] dateArray = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());

			Date startDate = dateArray[0];
			Date endDate = dateArray[1];
			List<Long> teamIds = new ArrayList<>();
			teamIds = getAllChildrenTeamIds(teamService.findOne(teamId), teamIds);

			List<String> teamMemberEmails = new ArrayList<>();
			teamMemberEmails = getAllChildrenTeamMemberEmails(teamService.findOne(teamId), teamMemberEmails);

			Map<String, Object> responseObject = queryService.getTeamEntityStat(teamIds, teamMemberEmails, startDate, endDate);
			Long candidateCount = candidateService.getCountByOwnerLikeAndDatebetween(StringUtils.commaSeparate(teamMemberEmails), startDate,
					endDate);
			responseObject.put("candidateCount", candidateCount);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, responseObject);
			return response;
		} catch (RecruizException rex) {
			return new RestResponse(false, rex.getMessage(), rex.getErrConstant());
		} catch (Exception ex) {
			ex.printStackTrace();
			return new RestResponse(false, ErrorHandler.RECRUITEMT_PROFILE_ERROR_OCCURED, ErrorHandler.RECRUITMENT_PROFILE_ERROR);
		}
	}

	/**
	 * @param team
	 * @param teamIds
	 * @return
	 */
	private List<Long> getAllChildrenTeamIds(Team team, List<Long> teamIds) {
		if (team.getChildren() != null && !team.getChildren().isEmpty()) {
			for (Team subTeam : team.getChildren()) {
				getAllChildrenTeamIds(subTeam, teamIds);
			}
		} else {
			teamIds.add(team.getId());
		}
		return teamIds;
	}

	private List<String> getAllChildrenTeamMemberEmails(Team team, List<String> teamMemberEmails) {
		if (team.getChildren() != null && !team.getChildren().isEmpty()) {
			for (Team subTeam : team.getChildren()) {
				getAllChildrenTeamMemberEmails(subTeam, teamMemberEmails);
			}
		} else {
			team.getMembers().forEach(member -> teamMemberEmails.add(member.getUser().getEmail()));
		}
		return teamMemberEmails;
	}

	@RequestMapping(value = "/team/teamware/req/performance", method = RequestMethod.POST)
	public RestResponse getTeamwareReqPref(@RequestParam Long teamId, @RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {
		Map<String, Object> reportMap = new HashMap<>();

		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, null, null, null, null, date[0], date[1]);
			reportMap.put("csvString", csvLines);
			reportMap.put("listData", report);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new RestResponse(true, reportMap);
	}

	@RequestMapping(value = "/team/teamware/pipeline", method = RequestMethod.POST)
	public RestResponse getTeamwarePipelineReport(@RequestParam Long teamId, @RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException {
		Map<String, Object> reportMap = new HashMap<>();

		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, "pipeline", null, null, null, date[0],
					date[1]);
			reportMap.put("csvString", csvLines);
			reportMap.put("listData", report);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new RestResponse(true, reportMap);
	}

	@RequestMapping(value = "/team/teamware/biz/analysis", method = RequestMethod.POST)
	public RestResponse getTeamwareBizAnalysisReport(@RequestParam Long teamId, @RequestParam(required = false) List<Long> clientIds,
			@RequestParam(required = false) String location, @RequestParam(required = false) String vertical,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {
		Map<String, Object> reportMap = new HashMap<>();

		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			if (null == location) {
				location = "%";
			} else {
				location = "%" + location + "%";
			}

			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, "BizAnalysis", clientIds, location,
					vertical, date[0], date[1]);
			reportMap.put("csvString", csvLines);
			reportMap.put("listData", report);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new RestResponse(true, reportMap);
	}

	@RequestMapping(value = "/teamware/resume/submission/excel", method = RequestMethod.POST)
	public void getTeamwareResumeSubmissionReport(HttpServletResponse response, @RequestParam List<Long> teamIds,
			@RequestParam(required = false) String vertical, @RequestBody ReportDropdownDTO reportDropdownDTO)
					throws RecruizException, InvalidFormatException, IOException {

		teamIds = teamService.getAllTeamsIdsForCurrentUser();
		Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Map<String, Object> result = reportService.getTeamwareResumeSubmissionReportForTeam(teamIds, vertical, date[0], date[1], "'pivot");
		File excelFile = (File) result.get("excel");
		writeExcelReport(response, excelFile);
	}

	@RequestMapping(value = "/teamware/resume/submission/json", method = RequestMethod.POST)
	public RestResponse getTeamwareResumeSubmissionJson(@RequestParam List<Long> teamIds, @RequestParam(required = false) String vertical,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException, InvalidFormatException, IOException {
		teamIds = teamService.getAllTeamsIdsForCurrentUser();
		Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Map<String, Object> result = reportService.getTeamwareResumeSubmissionReportForTeam(teamIds, vertical, date[0], date[1], "json");
		RestResponse restResponse = new RestResponse(true, result.get("pivotData"));
		return restResponse;
	}

	//@ClientA
	private File getCSVForRecPrefReport(List<String> records) {
		String csvPath = System.getProperty("java.io.tmpdir") + File.separator + TenantContextHolder.getTenant() + File.separator
				+ System.currentTimeMillis() + File.separator + System.currentTimeMillis() + ".csv";
		File csvFile = null;
		try {
			csvFile = FileUtils.createTempFileCopy(csvPath);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvPath), "UTF-8"));
			for (String line : records) {
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (UnsupportedEncodingException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return csvFile;
	}

	@RequestMapping(value = "/teamware/pref/report", method = RequestMethod.POST)
	public RestResponse getTeamwarePrefReport(HttpServletResponse response, @RequestParam Long teamIds,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException, InvalidFormatException, IOException {

		Map<String, Object> reportMap = new HashMap<>();

		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<PrefTrend> report = reportService.getTeamwarePrefTrendReport(teamIds, csvLines, null, null, null, date[0], date[1]);
			reportMap.put("csvString", csvLines);
			reportMap.put("listData", report);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new RestResponse(true, reportMap);

	}

	@RequestMapping(value = "/teamware/client/analysis", method = RequestMethod.POST)
	public RestResponse getTeamwareClientAnalysisReport(@RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(required = false) List<Long> clientIds, @RequestParam(required = false) List<String> vertical,
			@RequestParam(required = false) String location, @RequestParam(required = false) List<String> status)
					throws RecruizException, InvalidFormatException, IOException {

		Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());

		Map<String, Object> reportMap = reportService.getClientAnalysisReportForTeamware(date[0], date[1], clientIds, vertical, location,
				status);
		return new RestResponse(true, reportMap);
	}

	@RequestMapping(value = "/team/teamware/biz/analysis/excel", method = RequestMethod.POST)
	public void getTeamwareBizAnalysisExcelReport(HttpServletResponse response, @RequestParam Long teamId,
			@RequestParam(required = false) List<Long> clientIds, @RequestParam(required = false) String location,
			@RequestParam(required = false) String vertical, @RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {
		Map<String, Object> reportMap = new HashMap<>();
		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			if (null == location) {
				location = "%";
			} else {
				location = "%" + location + "%";
			}

			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, "BizAnalysis", clientIds, location,
					vertical, date[0], date[1]);
			reportMap.put("csvString", csvLines);
			reportMap.put("listData", report);

			File excelFile = reportService.createExcelFile(csvLines, "BizAnalysis");
			writeExcelReport(response, excelFile);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/team/teamware/req/performance/excel", method = RequestMethod.POST)
	public void getTeamwareReqPref(HttpServletResponse response, @RequestParam Long teamId,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {
		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, null, null, null, null, date[0], date[1]);
			File excelFile = reportService.createExcelFile(csvLines, "Req Pref");
			writeExcelReport(response, excelFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/team/teamware/pipeline/excel", method = RequestMethod.POST)
	public void getTeamwarePipelineExcelReport(HttpServletResponse response, @RequestParam Long teamId,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException {
		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<RecPrefDTO> report = reportService.getRecPrefReportForTeamware(teamId, csvLines, "pipeline", null, null, null, date[0],
					date[1]);
			File excelFile = reportService.createExcelFile(csvLines, "Pipeline");
			writeExcelReport(response, excelFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// for Pref Trends
	@RequestMapping(value = "/teamware/pref/report/excel", method = RequestMethod.POST)
	public void getTeamwarePrefTrendExcelReport(HttpServletResponse response, @RequestParam Long teamId,
			@RequestBody ReportDropdownDTO reportDropdownDTO) throws RecruizException, InvalidFormatException, IOException {

		try {
			Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
					reportDropdownDTO.getEndDate());
			List<String> csvLines = new ArrayList<>();
			List<PrefTrend> report = reportService.getTeamwarePrefTrendReport(teamId, csvLines, null, null, null, date[0], date[1]);
			File excelFile = reportService.createExcelFile(csvLines, "Pref Trend");
			writeExcelReport(response, excelFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/teamware/client/analysis/excel", method = RequestMethod.POST)
	public void getTeamwareClientAnalysisExcelReport(HttpServletResponse response, @RequestBody ReportDropdownDTO reportDropdownDTO,
			@RequestParam(required = false) List<Long> clientIds, @RequestParam(required = false) List<String> vertical,
			@RequestParam(required = false) String location, @RequestParam(required = false) List<String> status)
					throws RecruizException, InvalidFormatException, IOException {

		Date[] date = reportService.calculateTimePeriod(reportDropdownDTO.getTimePeriod(), reportDropdownDTO.getStartDate(),
				reportDropdownDTO.getEndDate());
		Map<String, Object> reportMap = reportService.getClientAnalysisReportForTeamware(date[0], date[1], clientIds, vertical, location,
				status);
		List<String> csvLines = (List<String>) reportMap.get("csvString");
		File excelFile = reportService.createExcelFile(csvLines, "Client Analysis");
		writeExcelReport(response, excelFile);
	}


	@RequestMapping(value = "/position/{positionCode}/recruiter/getCandidatesListInEachRecruiter", method = RequestMethod.GET)
	public RestResponse getCandidatesListInEachRecruiter(@PathVariable("positionCode") String positionCode)
			throws RecruizException, TypeMismatchException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetCandidatesListInEachRecruiter.name());

		return reportService.getCandidatesListInEachRecruiter(positionCode);
	}


	@RequestMapping(value = "/recruiter/custom/getCustomReportOfEachRecruiter", method = RequestMethod.POST)
	public RestResponse getCustomReportOfEachRecruiter(@RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException, TypeMismatchException, ParseException {


		if(reportDropdownDTO.getTimePeriod()!=null && !reportDropdownDTO.getTimePeriod().equalsIgnoreCase("")){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setEndDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			
			 Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
	
			
		}

		return reportService.getCustomReportOfEachRecruiter(reportDropdownDTO);
	}


	@RequestMapping(value = "/client/custom/getCustomReportOfEachClient", method = RequestMethod.POST)
	public RestResponse getCustomReportOfEachClient(@RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException, TypeMismatchException {

		if(reportDropdownDTO.getTimePeriod()!=null && !reportDropdownDTO.getTimePeriod().equalsIgnoreCase("")){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setStartDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
		}

		return reportService.getCustomReportOfEachClient(reportDropdownDTO);
	}

	
	
	
	
	@RequestMapping(value = "/recruiter/custom/getExcelCustomReportOfEachRecruiter", method = RequestMethod.POST)
	public void getExcelCustomReportOfEachRecruiter(HttpServletResponse response,@RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException, TypeMismatchException, ParseException, IOException {


		if(reportDropdownDTO.getTimePeriod()!=null && !reportDropdownDTO.getTimePeriod().equalsIgnoreCase("")){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setEndDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			
			 Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
	
			
		}

		File excelFile = reportService.createExcelFileForEachRecruiter(reportDropdownDTO);
		writeExcelReport(response, excelFile);
		
	}
	
	
	
	
	@RequestMapping(value = "/client/custom/getExcelCustomReportOfEachClient", method = RequestMethod.POST)
	public void getExcelCustomReportOfEachClient(HttpServletResponse response,@RequestBody ReportDropdownDTO reportDropdownDTO)
			throws RecruizException, TypeMismatchException, IOException {

		if(reportDropdownDTO.getTimePeriod()!=null && !reportDropdownDTO.getTimePeriod().equalsIgnoreCase("")){

			Calendar cal = Calendar.getInstance();
			Date enddate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			Date startdate = cal.getTime();
			reportDropdownDTO.setStartDate(startdate);
			reportDropdownDTO.setStartDate(enddate);
		}else if(reportDropdownDTO.getStartDate()!=null && reportDropdownDTO.getEndDate()!=null){
			Calendar c = Calendar.getInstance();
			 c.setTime(reportDropdownDTO.getStartDate());
			 c.add(Calendar.DATE, 1);
			 c.set(Calendar.HOUR_OF_DAY, 0);
			 
			 reportDropdownDTO.setStartDate(c.getTime());
			
			 Calendar cal = Calendar.getInstance();
			 cal.setTime(reportDropdownDTO.getEndDate());
			 cal.add(Calendar.DATE, 1);
			 cal.set(Calendar.HOUR_OF_DAY, 23);
			 
			 reportDropdownDTO.setEndDate(cal.getTime());
		}

		File excelFile = reportService.createExcelFileForEachClient(reportDropdownDTO);
		writeExcelReport(response, excelFile);
	}
	
	
	
	
}
