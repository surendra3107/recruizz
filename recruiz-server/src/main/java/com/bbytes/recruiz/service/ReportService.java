package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.CustomRounds;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterForCandidate;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.DefaultRounds;
import com.bbytes.recruiz.enums.PerformanceReportTimePeriod;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ClientRepository;
import com.bbytes.recruiz.repository.InterviewScheduleRepository;
import com.bbytes.recruiz.repository.OfferLetterForCandidateRepository;
import com.bbytes.recruiz.repository.PositionCandidateDataRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.repository.UserRepository;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateDuplicateDTO;
import com.bbytes.recruiz.rest.dto.models.CustomRecruiterAndClientDTO;
import com.bbytes.recruiz.rest.dto.models.FinalCustomRecruiterAndClientDTO;
import com.bbytes.recruiz.rest.dto.models.OfferCandidateRepotDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterPositionDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterReportDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterStatusWiseReport;
import com.bbytes.recruiz.rest.dto.models.RecruiterPerformanceReportDTO;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.rest.dto.models.ReportDropdownDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.ResultSetData;
import com.bbytes.recruiz.rest.dto.models.WordCloudDTO;
import com.bbytes.recruiz.rest.dto.models.teamware_report.PrefTrend;
import com.bbytes.recruiz.rest.dto.models.teamware_report.RecPrefDTO;
import com.bbytes.recruiz.rest.dto.models.teamware_report.ResumeSubmissionDTO;
import com.bbytes.recruiz.utils.ArrayUtil;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TeamwareConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fdsapi.ResultSetConverter;
import com.google.common.base.Joiner;
import com.google.inject.internal.Lists;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;

/**
 * Return the report Data
 *
 * @author Akshay
 *
 */
@Service
public class ReportService {

	public final Logger logger = LoggerFactory.getLogger(ReportService.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private VelocityEngine templateEngine;

	@Autowired
	private InterviewScheduleRepository interviewScheduleRepository;

	@Autowired
	PositionCandidateDataRepository positionCandidateDataRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;


	@Autowired
	private PositionCandidateDataService positionCandidateDataService;

	@Autowired
	private PositionService positionService;

	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CustomRoundService customRoundService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private CustomFieldService customFieldService;

	@Autowired
	private QueryService queryService;

	@Autowired
	private PositionRepository positionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService;

	@Autowired
	OfferLetterForCandidateRepository offerLetterForCandidateRepository;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Return report object of overall candidate sourcing channel mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallCandidateSourcingChannels() throws RecruizException {
		String template = "overall_candidate_sourcing_channels.vm";
		String sql = getSQL(template, null);
		Report report = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Candidate", "Sourcing channel mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		report.setTitle("Candidate database channel mix");
		return report;
	}

	/**
	 * Return report object of overall position status mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallPositionStatus() throws RecruizException {
		String template = "overall_position_status.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("onHoldStatus", Status.OnHold.toString());
		templateModel.put("closedStatus", Status.Closed.toString());
		templateModel.put("stopSourcingStatus", Status.StopSourcing.toString());
		String sql = getSQL(template, templateModel);
		Report report = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Position", "Status mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		report.setTitle("Position status mix");
		return report;
	}

	/**
	 * Return report object of overall client status mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallClientStatus() throws RecruizException {
		String template = "overall_client_status.vm";
		String sql = getSQL(template, null);
		Report report = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Client", "Status mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		report.setTitle("status mix");
		return report;
	}

	/**
	 * Return report object of overall client-position-candidate total count
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallClientPositionCandidateCount() throws RecruizException {

		final Map<String, Object> countMap = new LinkedHashMap<String, Object>();

		String template = "overall_client_position_candidate_count.vm";
		String sql = getSQL(template, null);
		ResultSetData resultSetData = jdbcTemplate.query(sql, new ResultSetExtractor<ResultSetData>() {

			@Override
			public ResultSetData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter rsc = new ResultSetConverter(rs);
				ResultSetData resultSetData = new ResultSetData(rsc);
				return resultSetData;
			}

		});

		Report report = new Report();
		report.setMetaData(resultSetData.getColumns());
		report.setReportData(resultSetData.getData());

		countMap.put(resultSetData.getColumns()[0], resultSetData.getColumnToRowData()[0][0]);
		countMap.put(resultSetData.getColumns()[1], resultSetData.getColumnToRowData()[1][0]);
		countMap.put(resultSetData.getColumns()[2], resultSetData.getColumnToRowData()[2][0]);

		report.setDetails(countMap);

		report.setTitle("Total Count");
		return report;
	}

	/**
	 * Return report object of overall client report
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallClientReport() throws RecruizException {

		String template = "overall_client_report.vm";
		String sql = getSQL(template, null);

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

		report.setTitle("Overall Client Report");
		return report;
	}

	/**
	 * Return report object of overall client position and recruitment team report
	 *
	 * @param clientName
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	public Report clientwisePositionAndRecruiterReport(List<String> clientNames, Date startDate, Date endDate,
			List<String> userEmails) throws RecruizException {

		String template = null;
		if (clientNames.get(0).equals("all")) {
			template = "overall_client_position_and_recruiter_report.vm";
		} else {
			template = "clientwise_position_and_recruiter_report.vm";
		}

		String teamFilter = "";

		if (permissionService.isSuperAdmin()) {
			// List<Long> allTeamIds = teamService.getAllTeamIds();
			// if (allTeamIds == null)
			// allTeamIds = new ArrayList<>();

			// Set<Long> teamIds = new HashSet<>(allTeamIds);
			// if (teamIds != null && !teamIds.isEmpty()) {
			// String teamIdsInCondition =
			// org.apache.commons.lang3.StringUtils.join(teamIds, ',');
			// teamFilter = teamFilter + " where team_id in ( " +
			// teamIdsInCondition + " ) ";
			// }

			List<String> allEmails = userService.findAllEmails();
			if (userEmails == null || userEmails.isEmpty()) {
				userEmails = new ArrayList<>();
				for (String email : allEmails) {
					userEmails.add("'" + email + "'");
				}
			}

		}

		// if (userEmails == null || userEmails.isEmpty() ||
		// !permissionService.isSuperAdmin()) {
		// Set<Long> teamIds =
		// teamService.getAllTeamsIdForUser(userService.getLoggedInUserObject());
		// if (teamIds != null && !teamIds.isEmpty()) {
		// String teamIdsInCondition =
		// org.apache.commons.lang3.StringUtils.join(teamIds, ',');
		// teamFilter = teamFilter + " where team_id in ( " + teamIdsInCondition
		// + " ) ";
		// }
		// }

		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("offeredStatus", BoardStatus.Offered.toString());
		templateModel.put("joinedStatus", BoardStatus.Joined.toString());
		templateModel.put("employeeStatus", BoardStatus.Employee.toString());
		templateModel.put("rejectedStatus", BoardStatus.Rejected.toString());
		templateModel.put("clientName", StringUtils.commaSeparate(clientNames));
		templateModel.put("userEmail", StringUtils.commaSeparate(userEmails));
		templateModel.put("teamFilter", teamFilter);

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

		report.setTitle("Recruitment Status");
		return report;
	}

	/**
	 * Return report object of average time to close the position
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallAverageTimeToClosePosition() throws RecruizException {

		final Map<String, Object> map = new LinkedHashMap<String, Object>();

		String template = "average_time_to_close_position.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionStatus", Status.Closed.toString());
		String sql = getSQL(template, templateModel);
		ResultSetData resultSetData = jdbcTemplate.query(sql, new ResultSetExtractor<ResultSetData>() {

			@Override
			public ResultSetData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter rsc = new ResultSetConverter(rs);
				ResultSetData resultSetData = new ResultSetData(rsc);
				return resultSetData;
			}

		});

		Report report = new Report();
		report.setMetaData(resultSetData.getColumns());
		report.setReportData(resultSetData.getData());

		if (Integer.parseInt(resultSetData.getColumnToRowData()[0][0].toString()) > 1)
			map.put("average_time", resultSetData.getColumnToRowData()[0][0] + " days");
		else
			map.put("average_time", resultSetData.getColumnToRowData()[0][0] + " day");
		report.setDetails(map);

		report.setTitle("Average time to close position");
		return report;
	}

	/**
	 * Return report object of per position sourcing channels by given time period
	 * period
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report perPositionSoucingChannelMixByTimePeriod(Date startDate, Date endDate, String positionCode)
			throws RecruizException {
		String template = "per_position_sourcing_channels_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// if positionCode = 'all' then query will execute for all position, if
		// else condition added in above query(vm) template
		templateModel.put("positionCode", positionCode);
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "All Positions", "Sourcing channel mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Sourcing channel mix");
		return metric;
	}

	/**
	 * Return report object of average time to close the per position by given time
	 * period
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report perPositionAvgTimeToCloseByTimePeriod(Date startDate, Date endDate, String positionCode)
			throws RecruizException {

		final Map<String, Object> map = new LinkedHashMap<String, Object>();

		String template = "per_position_average_time_to_close_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionStatus", Status.Closed.toString());
		// if positionCode = 'all' then query will execute for all position, if
		// else condition added in above query(vm) template
		templateModel.put("positionCode", positionCode);
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		ResultSetData resultSetData = jdbcTemplate.query(sql, new ResultSetExtractor<ResultSetData>() {

			@Override
			public ResultSetData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter rsc = new ResultSetConverter(rs);
				ResultSetData resultSetData = new ResultSetData(rsc);
				return resultSetData;
			}

		});

		Report report = new Report();
		report.setMetaData(resultSetData.getColumns());
		report.setReportData(resultSetData.getData());

		if (Integer.parseInt(resultSetData.getColumnToRowData()[0][0].toString()) > 1)
			map.put("average_time", resultSetData.getColumnToRowData()[0][0] + " days");
		else
			map.put("average_time", resultSetData.getColumnToRowData()[0][0] + " day");
		report.setDetails(map);

		report.setTitle("Average time to close");
		return report;
	}

	/**
	 * Return report object of per position candidate rejection by given time period
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report perPositionCandidateRejectionByTimePeriod(Date startDate, Date endDate, String positionCode)
			throws RecruizException {
		String template = "per_position_candidate_rejection_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// if positionCode = 'all' then query will execute for all position, if
		// else condition added in above query(vm) template
		templateModel.put("positionCode", positionCode);
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "All Positions", "Candidate rejection mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Rejection mix");
		return metric;
	}

	/**
	 * Return report object of overall positionwise candidate status report
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	public Report positionwiseCandidateStatusReport(Date startDate, Date endDate) throws RecruizException {

		String timeZone = DateUtil.getTimeZoneOffsetValue(userService.getLoggedInUserTimeZone());

		String template = "positionwise_candidate_status_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("timeZone", timeZone);
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

		report.setTitle("Candidate status report");
		return report;
	}

	/**
	 * Return report object of overall client position and recruitment team report
	 *
	 * @return
	 * @throws RecruizException
	 * @throws TypeMismatchException
	 */
	public Report recruiterswiseCandidateBoardReport(String userEmail, String timePeriod)
			throws RecruizException, TypeMismatchException {

		String template = "recruiterswise_candidate_board_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		templateModel.put("userEmail", userEmail);
		templateModel.put("offeredStatus", BoardStatus.Offered.toString());
		templateModel.put("joinedStatus", BoardStatus.Joined.toString());
		templateModel.put("rejectedStatus", BoardStatus.Rejected.toString());
		String sql = getSQL(template, templateModel);

		ResultSetData resultSetData = jdbcTemplate.query(sql, new ResultSetExtractor<ResultSetData>() {

			@Override
			public ResultSetData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter rsc = new ResultSetConverter(rs);
				ResultSetData resultSetData = new ResultSetData(rsc);
				return resultSetData;
			}

		});

		String dataColumnName = resultSetData.getColumns()[0];
		String seriesColumnName = resultSetData.getColumns()[1];
		String labelColumnName = resultSetData.getColumns()[2];
		Object[] series = null;
		Object[] labels = null;
		DataTable dataTable = null;
		if (resultSetData.getColumnToRowData() != null) {
			series = getUniqueColumnData(resultSetData, seriesColumnName);
			labels = getUniqueColumnData(resultSetData, labelColumnName);
		}
		int labelIndex = findColumnIndex(resultSetData, labelColumnName);
		int seriesIndex = findColumnIndex(resultSetData, seriesColumnName);

		dataTable = getSeriesData(resultSetData, dataColumnName, series, seriesIndex, labels, labelIndex,
				"Recruiter Name");

		Report report = new Report();
		report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		report.setXLabel(labelColumnName);
		report.setYLabel(dataColumnName);
		report.setMetaData(resultSetData.getColumns());
		report.setReportData(resultSetData.getData());

		report.setTitle("Pipeline Statistics");
		return report;
	}

	/**
	 * Return report object of recruiters wise count report
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report recruiterswiseCountReport(String userEmail, String timePeriod) throws RecruizException {

		final Map<String, Object> countMap = new LinkedHashMap<String, Object>();

		String template = "recruiterswise_count_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		templateModel.put("userEmail", userEmail);
		String sql = getSQL(template, templateModel);
		ResultSetData resultSetData = jdbcTemplate.query(sql, new ResultSetExtractor<ResultSetData>() {

			@Override
			public ResultSetData extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter rsc = new ResultSetConverter(rs);
				ResultSetData resultSetData = new ResultSetData(rsc);
				return resultSetData;
			}

		});

		Report report = new Report();
		report.setMetaData(resultSetData.getColumns());
		report.setReportData(resultSetData.getData());

		countMap.put(resultSetData.getColumns()[0], resultSetData.getColumnToRowData()[0]);
		countMap.put(resultSetData.getColumns()[1], resultSetData.getColumnToRowData()[1]);
		countMap.put(resultSetData.getColumns()[2], resultSetData.getColumnToRowData()[2]);

		report.setDetails(countMap);

		report.setTitle("Pipeline Statistics");
		return report;
	}

	/**
	 * Return report object of candidate overall sourcing channels by timeperiod
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report recruiterswiseCandidateSourcingChannels(String userEmail, String timePeriod) throws RecruizException {
		String template = "recruiterswise_candidate_sourcing_channels.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		templateModel.put("userEmail", userEmail);
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Candidate", "Sourcing Channels Mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Sourcing channels mix");
		return metric;
	}

	/**
	 * Return report object of client wise position count report
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report clientwisePositionCountReport() throws RecruizException {

		String template = "clientwise_position_count_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("closedStatus", Status.Closed.toString());
		templateModel.put("activeStatus", Status.Active.toString());
		templateModel.put("onHoldStatus", Status.OnHold.toString());
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

		report.setTitle("Clientwise Position Count Report");
		return report;
	}

	/**
	 * Return report object of all position's sourcing channels
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report getAllPositionSoucingChannelMix() throws RecruizException {
		String template = "all_position_sourcing_channels.vm";
		String sql = getSQL(template, null);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "All Positions", "Sourcing channel mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Sourcing channel mix (All positions)");
		return metric;
	}

	/**
	 * Return report object of all position's candidate rejection mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report getAllPositionCandidateRejectionMix() throws RecruizException {
		String template = "all_position_candidate_rejection_mix.vm";
		String sql = getSQL(template, null);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "All Positions", "Candidate rejection mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Rejection mix (All positions)");
		return metric;
	}

	/**
	 * Return report object of candidate pool periodically
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report candidateTotalPoolPeriodically() throws RecruizException {
		String template = "total_candidate_pool_periodically.vm";
		String sql = getSQL(template, null);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Time Period", "Candidate Count");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate data base - Timelapse");
		return metric;
	}

	/**
	 * Return report object of candidate overall sourcing channels by timeperiod
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report candidateOverallSourcingChannelsByTimperiod(Date startDate, Date endDate) throws RecruizException {
		String template = "candidate_overall_sourcing_channel_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Channel", "Candidate Count");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate database - Sourcing channel mix");
		return metric;
	}



	public Report candidateOverallSourcingChannelsByTimperiodWithOutCustom(String timeperiod) throws RecruizException {

		String template = null;

		if(timeperiod.equalsIgnoreCase("CURDATE()")){
			template = "candidate_overall_sourcing_channel_by_timeperiod_without_custom_today.vm";
		}else{
			template = "candidate_overall_sourcing_channel_by_timeperiod_without_custom.vm";
		}

		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("timePeriod", timeperiod);
		//	templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Channel", "Candidate Count");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate database - Sourcing channel mix");
		return metric;
	}




	/**
	 * Return report object of candidate overall gender mix by timeperiod
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report candidateOverallGenderMixByTimperiod(Date startDate, Date endDate) throws RecruizException {
		String template = "candidate_overall_gender_mix_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Candidate", "Gender Mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate database - Gender mix");
		return metric;
	}



	public Report candidateOverallGenderMixByTimperiod(String timeperiod) throws RecruizException {
		String template = null;
		if(timeperiod.equalsIgnoreCase("CURDATE()")){
			template = "candidate_overall_gender_mix_by_timeperiod_without_custom_today.vm";
		}else{
			template = "candidate_overall_gender_mix_by_timeperiod_without_custom.vm";
		}

		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("timePeriod", timeperiod);
		//	templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Candidate", "Gender Mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate database - Gender mix");
		return metric;
	}



	/**
	 * Return report object of candidate pool per month or year wise
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report monthwiseCandidatePool(String timePeriod) throws RecruizException {
		String template = "monthwise_overall_candidate_pool.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty()) {
			PerformanceReportTimePeriod performanceReportTimePeriod = getPreformanceTimePeriod(timePeriod);
			templateModel.put("timePeriod", performanceReportTimePeriod.getInterval());
		}
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, report.getXLabel(), report.getYLabel());
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate database - Growth");
		return metric;
	}

	public PerformanceReportTimePeriod getPreformanceTimePeriod(String timePeriod) {
		PerformanceReportTimePeriod performanceReportTimePeriod = null;

		if (timePeriod != null) {
			if (timePeriod.equalsIgnoreCase("1 Month"))
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_Month;
			else if (timePeriod.equalsIgnoreCase("3 Month"))
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_3_Months;
			else if (timePeriod.equalsIgnoreCase("6 Month"))
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_6_Months;
			else if (timePeriod.equalsIgnoreCase("1 Year"))
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_12_Months;
			else if (timePeriod.equalsIgnoreCase("12 Month"))
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_12_Months;
		}

		if (performanceReportTimePeriod == null) {
			if (timePeriod == null)
				performanceReportTimePeriod = PerformanceReportTimePeriod.Last_Month;
			else
				performanceReportTimePeriod = PerformanceReportTimePeriod.valueOf(timePeriod);
		} else {
			timePeriod = performanceReportTimePeriod.toString();
		}

		return performanceReportTimePeriod;
	}

	public Date[] calculateTimePeriod(String timePeriod, Date startDate, Date endDate) throws RecruizException {
		PerformanceReportTimePeriod performanceReportTimePeriod = getPreformanceTimePeriod(timePeriod);
		timePeriod = performanceReportTimePeriod.toString();

		Integer timePeriodValue;
		Date[] startEndDates = new Date[2];
		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriodValue = PerformanceReportTimePeriod.Last_Month.getDays();
		} else {
			timePeriodValue = PerformanceReportTimePeriod.valueOf(timePeriod).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (PerformanceReportTimePeriod.Custom.equals(performanceReportTimePeriod)) {
			if (null == startDate || null == endDate) {
				// throw new RecruizException(ErrorHandler.DATE_NOT_PRESENT,
				// ErrorHandler.NO_DATE);
				startEndDates = DateTimeUtils
						.getStartDateEndDateWithDayStart(PerformanceReportTimePeriod.Last_Month.getDays());
			}
			if (startDate != null && endDate != null) {
				startEndDates[0] = startDate;
				startEndDates[1] = new DateTime(endDate.getTime()).plusDays(1).withTimeAtStartOfDay().toDate();
			}
		} else {

			if (timePeriodValue > -1)
				startEndDates = DateTimeUtils.getStartDateEndDateWithDayStart(timePeriodValue);
			else {
				timePeriodValue = PerformanceReportTimePeriod.valueOf(timePeriod).getHours();
				startEndDates = DateTimeUtils.getStartDateEndDateByHours(timePeriodValue);
			}
		}

		return startEndDates;
	}

	/**
	 * Return report object of month wise candidate pool by recruiters
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report monthwiseCandidatePoolByRecruiters(String timePeriod) throws RecruizException {
		String template = "monthwise_candidate_pool_by_recruiters.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, report.getXLabel(), report.getYLabel());
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Candidate sourcing by recruiter");
		return metric;
	}

	/**
	 * Return report object of month wise interviews scheduled by recruiters
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report monthwiseInterviewsScheduledByRecruiters(String timePeriod) throws RecruizException {
		String template = "monthwise_interview_scheduled_by_recruiters.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, report.getXLabel(), report.getYLabel());
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Interview scheduled by recruiter");
		return metric;
	}

	/**
	 * Return report object of month wise positions closed by recruiters
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report monthwisePositionsClosedByRecruiters(String timePeriod) throws RecruizException {
		String template = "monthwise_positions_closed_by_recruiters.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, report.getXLabel(), report.getYLabel());
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Positions closed by recruiter");
		return metric;
	}

	/**
	 * Return report object of month wise profile forwarded by recruiters
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report monthwiseProfileForwardedByRecruiters(String timePeriod) throws RecruizException {
		String template = "monthwise_profile_forwarded_by_recruiters.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		// get report from the start, time period value is 'fromStart'other
		// wise get last 3 month - timeperiod = 3 MONTH, query(vm) template
		// checking if else condition
		if (timePeriod != null && !timePeriod.isEmpty())
			templateModel.put("timePeriod", timePeriod);
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, report.getXLabel(), report.getYLabel());
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Profiles forwarded by recruiter");
		return metric;
	}

	/**
	 * Return resultset for candidate key skill word cloud data
	 *
	 * @return
	 */
	public List<WordCloudDTO> getCandidateKeySkillWordCloud() {
		final List<WordCloudDTO> wordCloudList = new ArrayList<WordCloudDTO>();

		String template = "candidate_keyskills_word_cloud.vm";
		String sql = getSQL(template, null);

		List<WordCloudDTO> resultSet = jdbcTemplate.query(sql, new ResultSetExtractor<List<WordCloudDTO>>() {
			@Override
			public List<WordCloudDTO> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter resultSet = new ResultSetConverter(rs);

				for (int i = 0; i < resultSet.getRowCount(); i++) {

					WordCloudDTO wordCloudDTO = new WordCloudDTO();
					wordCloudDTO.setText((String) resultSet.getResultSet()[i][0]);
					wordCloudDTO.setWeight((Long) resultSet.getResultSet()[i][1]);

					wordCloudList.add(wordCloudDTO);
				}
				return wordCloudList;

			}
		});

		return resultSet;
	}

	/**
	 * Return report object of overall prospect status mix
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report overallProspectStatus() {
		String template = "overall_prospect_status.vm";
		String sql = getSQL(template, null);
		Report report = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Prospect", "Status mix");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		report.setTitle("Prospect status mix");
		return report;
	}

	/**
	 * Return report object of user wise total number of prospects by timeperiod
	 *
	 * @return
	 * @throws RecruizException
	 */
	public Report userWiseProspectsPoolByTimperiod(Date startDate, Date endDate) throws RecruizException {
		String template = "userwise_prospects_pool_by_timeperiod.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		String sql = getSQL(template, templateModel);
		Report metric = jdbcTemplate.query(sql, new ResultSetExtractor<Report>() {
			@Override
			public Report extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Report report = new Report();
				report.setYLabel(result.getMetaData()[1]);
				report.setXLabel(result.getMetaData()[0]);
				report.setMetaData(result.getMetaData());
				report.setReportData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Prospect", "Total Prospects");
					report.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return report;
			}
		});

		metric.setTitle("Prospect - By Team");
		return metric;
	}

	/**
	 * Return report object of prospect report by team member
	 *
	 * @param userEmail
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	public Report userWiseProspectReport(String userEmail, Date startDate, Date endDate) throws RecruizException {

		String template = null;
		template = "userwise_prospects_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("userEmail", userEmail);
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

		report.setTitle("Prospect Report");
		return report;
	}

	/**
	 * Return report object of invoice
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws RecruizException
	 */
	public Report invoiceReport(String clientName, String invoiceStatus, Date startDate, Date endDate)
			throws RecruizException {

		String timeZone = DateUtil.getTimeZoneOffsetValue(userService.getLoggedInUserTimeZone());

		String template = null;
		template = "invoice_report.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("clientName", clientName);
		templateModel.put("invoiceStatus", invoiceStatus);
		templateModel.put("timeZone", timeZone);
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

		report.setTitle("Invoice Report");
		return report;
	}

	/**
	 * Return dataTable Converted chart data for single
	 * series/pie-chart/area-chart/line-chart
	 *
	 * @param resultSet
	 * @param xBarLabel
	 * @param yBarLabel
	 * @return
	 * @throws TypeMismatchException
	 */
	private DataTable getDataTable(ResultSetConverter resultSet, String xBarLabel, String yBarLabel)
			throws TypeMismatchException {

		DataTable dataTable = new DataTable();
		ColumnDescription columnDescription1 = new ColumnDescription("col0", ValueType.TEXT, xBarLabel);
		ColumnDescription columnDescription2 = new ColumnDescription("col1", ValueType.NUMBER, yBarLabel);
		dataTable.addColumn(columnDescription1);
		dataTable.addColumn(columnDescription2);
		List<TableRow> rows = Lists.newArrayList();
		for (int i = 0; i < resultSet.getRowCount(); i++) {
			TableRow row = new TableRow();
			row.addCell(new TableCell((String) resultSet.getResultSet()[i][0]));
			row.addCell(new TableCell((Long) resultSet.getResultSet()[i][1]));
			rows.add(row);
		}
		dataTable.addRows(rows);

		return dataTable;
	}

	private int findColumnIndex(ResultSetData resultSetData, String seriesColumnName) {
		String[] columns = resultSetData.getColumns();
		for (int i = 0; i < columns.length; i++) {
			if (seriesColumnName.equals(columns[i]))
				return i;
		}
		return -1;
	}

	/**
	 * Return metrics data for series
	 *
	 * @param resultSetData
	 * @param dataColumnName
	 * @param series
	 * @param seriesIndex
	 * @param labels
	 * @param labelIndex
	 * @return
	 * @throws TypeMismatchException
	 */
	private DataTable getSeriesData(ResultSetData resultSetData, String dataColumnName, Object[] series,
			int seriesIndex, Object[] labels, int labelIndex, String xBarLabel) throws TypeMismatchException {

		Object[][] data = resultSetData.getData();
		int columnIndex = resultSetData.getColumnIndex(dataColumnName);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		DataTable dataTable = new DataTable();
		List<TableRow> rows = Lists.newArrayList();
		ColumnDescription columnDescription = new ColumnDescription("col0", ValueType.TEXT, xBarLabel);
		dataTable.addColumn(columnDescription);
		for (int i = 0; i < resultSetData.getRowCount(); i++) {
			resultMap.put(data[i][seriesIndex] + ":" + data[i][labelIndex], data[i][columnIndex]);
		}
		int index = 0;
		if (series != null && labels != null) {
			for (int i = 0; i < series.length; i++) {
				ColumnDescription columnDesc = new ColumnDescription("col" + ++index, ValueType.NUMBER,
						(String) series[i]);
				dataTable.addColumn(columnDesc);
			}
			for (int i = 0; i < labels.length; i++) {
				TableRow row = new TableRow();
				row.addCell(new TableCell((String) labels[i]));
				for (int j = 0; j < series.length; j++) {

					if (resultMap.get(series[j] + ":" + labels[i]) != null) {
						row.addCell(new TableCell((Long) resultMap.get(series[j] + ":" + labels[i])));
					} else {
						row.addCell(new TableCell((Long) 0L));
					}
				}
				rows.add(row);
			}
		} else {
			ColumnDescription columnDesc = new ColumnDescription("col1", ValueType.NUMBER, xBarLabel);
			dataTable.addColumn(columnDesc);
		}

		dataTable.addRows(rows);

		return dataTable;
	}

	private Object[] getUniqueColumnData(ResultSetData result, String columnName) {
		int columnIndex = result.getColumnIndex(columnName);

		Object[] data = result.getColumnData(columnIndex);
		data = ArrayUtil.removeDuplicate(data);
		return data;
	}

	private String getSQL(String template, Map<String, Object> model) {
		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/report-query/" + template, "UTF-8", model);
		return sql;
	}

	// to get all candidate count board status wise for a given HR and position
	// and time frame from round candidate
	public Object getCandidateCountStatusWise(Date startDate, Date endDate, String hrEmail, String positionCode)
			throws RecruizException {
		String template = "all_status_count_sql.vm";

		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		templateVariableMap.put("positionCode", positionCode);
		templateVariableMap.put("sourcedBy", hrEmail);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/recruiter-profile-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		countResponse.put("pcode", positionCode);
		countResponse.put("status", positionService.getPositionByCode(positionCode).getStatus());
		// logger.error(sql);
		return countResponse;
	}



	// to get all candidate count board status wise for a given HR and position
	// and time frame from round candidate
	public Object getCandidateCountStatusWiseRecruiterProgressReport(Date startDate, Date endDate, String hrEmail, String positionCode)
			throws RecruizException {
		String template = "all_status_count_sql.vm";

		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		templateVariableMap.put("positionCode", positionCode);
		templateVariableMap.put("sourcedBy", hrEmail);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/recruiter-profile-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);

		addCustomStatusCandidatesInReport(countResponse, startDate, endDate, hrEmail, positionCode);

		countResponse.put("pcode", positionCode);
		countResponse.put("status", positionService.getPositionByCode(positionCode).getStatus());
		// logger.error(sql);
		return countResponse;
	}


	// to get all candidate count board status wise for a given HR and position
	// and time frame from round candidate
	public Object getCandidateCountStatusWiseCustom(Date startDate, Date endDate, String hrEmail, String positionCode)
			throws RecruizException {
		String template = "custom_all_status_count_sql.vm";

		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		templateVariableMap.put("positionCode", positionCode);
		templateVariableMap.put("sourcedBy", hrEmail);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/recruiter-profile-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		countResponse.put("pcode", positionCode);
		countResponse.put("status", positionService.getPositionByCode(positionCode).getStatus());
		// logger.error(sql);
		return countResponse;
	}

	public Map<String, Object> getStatForRecruitmentHr(String hrEmail, String hrId, String clientName, String clientId,
			Date startDate, Date endDate, String positionCodes) throws RecruizException {

		String template = "related_entity_count.vm";
		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		if (clientName != null && !clientName.trim().isEmpty() && clientId != null && !clientId.trim().isEmpty()) {
			template = "related_entity_count_client_wise.vm";
			templateVariableMap.put("clientName", clientName);
			templateVariableMap.put("clientId", clientId);

			if (!positionCodes.isEmpty()) {
				templateVariableMap.put("positionCode", positionCodes);
			} else {
				templateVariableMap.put("positionCode", "'nill'");
			}
		}

		templateVariableMap.put("hrEmail", hrEmail);
		templateVariableMap.put("hrId", hrId);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/recruiter-profile-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		return countResponse;
	}

	// query for employee dashboard
	public Map<String, Object> getEmployeeDashBoardCount() throws RecruizException {

		String template = "emplpoye_dashboard_count.vm";
		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/employee-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		return countResponse;
	}


	public void saveCustomReportTimePeriod(String reportTimePeriod){
		User user = userRepository.findOneByEmail(userService.getLoggedInUserEmail());
		user.setReporttimeperiod(reportTimePeriod);
		userRepository.save(user);
	}


	public File getPositionAllStageAllStatusExcelReport(HttpServletResponse response) throws InvalidFormatException, IOException, RecruizException {
		File file =null;
		User user = userRepository.findOneByEmail(userService.getLoggedInUserEmail());

		File folder = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "reports" 
				+ File.separator + "All_Status_And_Stages"+ File.separator + user.getUserId()+ File.separator);
		File[] listOfFiles = folder.listFiles();

		if(listOfFiles==null)
			return file;

		String fileName =null;
		for (File fileData : listOfFiles) {
			if (fileData.isFile()) {
				fileName = fileData.getName();
			}
		}


		if(fileName!=null){
			response.setHeader("file-name", fileName);
			String todayExcel = candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "reports"
					+ File.separator + "All_Status_And_Stages"+ File.separator + user.getUserId()+ File.separator + fileName;

			file = new File(todayExcel);
		}

		return file;
	}


	/*	@Async
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

			Set<Position> positions = new HashSet<>();
		positions.addAll(positionList);

			Map<String, String> statusMap = getBoardStatusList();

			Map<String, String> stageMap = new LinkedHashMap<>();
			stageMap.put(DefaultRounds.Sourcing.getDisplayName(), DefaultRounds.Sourcing.getDisplayName());
			List<CustomRounds> customRounds = customRoundService.findAll();
			if (null != customRounds && !customRounds.isEmpty()) {
				for (CustomRounds customRound : customRounds) {
					stageMap.put(customRound.getName(), customRound.getName());
				}
			}


	 * int sheetColumnSize = 5000 + statusMap.size(); int sheetRowSize =
	 * positions.size() + 5000;


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
	 */
	// custom board report given for a client
	public File getBoardReport() throws InvalidFormatException, IOException {
		List<Position> positionList = positionService.findAll();

		Set<Position> positions = new HashSet<>();
		positions.addAll(positionList);

		Map<String, String> statusMap = getBoardStatusList();

		int sheetColumnSize = 23 + statusMap.size();
		Object[][] data = new Object[positions.size()][sheetColumnSize];
		String[] header = new String[sheetColumnSize];
		header[0] = "Srl No.";
		header[1] = "Job Received Date";
		header[2] = "Client";

		header[3] = "Vertical/Cluster";
		header[4] = "Account / End Client"; // + end client spoc

		header[5] = "Account/Hiring Manager/RMG";
		header[6] = "Team";
		header[7] = "Requirement Details";
		header[8] = "Exp";
		header[9] = "Location";
		header[10] = "No. of posns";
		header[11] = "Requirement Status";
		header[12] = "# of CVs Sub";
		int i = 12;
		for (Entry<String, String> entry : statusMap.entrySet()) {
			header[++i] = entry.getValue();
		}
		header[++i] = "CVs / Posn";
		header[++i] = "Int / CV";
		header[++i] = "Sels / Int";
		header[++i] = "Joined / Sels";
		header[++i] = "Joined / Posns";
		int row = 0;
		for (Position pos : positions) {
			for (; row < positionList.size(); row++) {
				Position position = positionList.get(row);
				data[row][0] = row + 1;
				data[row][1] = DateUtil.formateDate(position.getOpenedDate(), DateUtil.DATE_FORMAT_MONTH);
				data[row][2] = position.getClient().getClientName();

				if (position.getVerticalCluster() != null) {
					data[row][3] = position.getVerticalCluster();
				} else {
					data[row][3] = "NA";
				}

				if (position.getEndClient() != null) {
					data[row][4] = position.getEndClient();
				} else {
					data[row][4] = "NA";
				}

				if (position.getHiringManager() != null) {
					data[row][5] = position.getHiringManager();
				} else {
					data[row][5] = "NA";
				}

				if (position.getTeam() != null) {
					data[row][6] = position.getTeam().getTeamName();
				} else {
					data[row][6] = "NA";
				}

				data[row][7] = position.getTitle();
				data[row][8] = position.getExperienceRange();
				data[row][9] = position.getLocation();
				data[row][10] = position.getTotalPosition();
				data[row][11] = position.getStatus();
				data[row][12] = roundCandidateService.getCountByPositionCode(position.getPositionCode());
				int columnIndex = 12;
				for (Entry<String, String> entry : statusMap.entrySet()) {
					List<String> statusIn = new ArrayList<>();
					statusIn.add(entry.getKey());
					statusIn.add(entry.getValue());
					data[row][++columnIndex] = roundCandidateService
							.getCountByPositionCodeAndStatusIn(position.getPositionCode(), statusIn);
				}
				List<String> status = new ArrayList<>();

				data[row][++columnIndex] = roundCandidateService.getCountByPositionCode(position.getPositionCode())
						+ "/" + position.getTotalPosition();
				data[row][++columnIndex] = interviewScheduleService
						.getInterviewCountByPosition(position.getPositionCode()) + "/"
						+ roundCandidateService.getCountByPositionCode(position.getPositionCode()); // "Int
				// /
				// CV";
				status.add(BoardStatus.Selected.getDisplayName());
				Long selectedCount = roundCandidateService.getCountByPositionCodeAndStatusIn(position.getPositionCode(),
						status);
				data[row][++columnIndex] = selectedCount + "/"
						+ interviewScheduleService.getInterviewCountByPosition(position.getPositionCode()); // "Sels
				// /
				// Int";

				// status cleared to add new Status
				status.clear();
				status.add(BoardStatus.Joined.getDisplayName());
				Long joinedCount = roundCandidateService.getCountByPositionCodeAndStatusIn(position.getPositionCode(),
						status);
				data[row][++columnIndex] = joinedCount + "/" + selectedCount; // "Joined
				// /
				// Sels";
				data[row][++columnIndex] = joinedCount + "/" + position.getTotalPosition(); // "Joined
				// /
				// Posns";
			}
		}

		File excelFile = importExportService.resultSetToExcelExport(header, data, System.currentTimeMillis() + "",
				null);
		return excelFile;
	}

	public File getClientPositionReport() throws InvalidFormatException, IOException, RecruizException {
		List<Position> positionList = positionService.findAll();

		Set<Position> positions = new HashSet<>();
		positions.addAll(positionList);

		Map<String, String> statusMap = new LinkedHashMap<>();
		statusMap.put(DefaultRounds.Sourcing.getDisplayName(), DefaultRounds.Sourcing.getDisplayName());
		List<CustomRounds> customRounds = customRoundService.findAll();
		if (null != customRounds && !customRounds.isEmpty()) {
			for (CustomRounds customRound : customRounds) {
				statusMap.put(customRound.getName(), customRound.getName());
			}
		}

		int sheetColumnSize = 13;
		Object[][] data = new Object[positions.size()][];
		String[] header = new String[sheetColumnSize];
		header[0] = "Srl No.";
		header[1] = "Job Received Date";
		header[2] = "Client";

		header[3] = "Vertical/Cluster";
		header[4] = "Account / End Client"; // + end client spoc

		header[5] = "Account/Hiring Manager/RMG";
		header[6] = "Team";
		header[7] = "Requirement Details";
		header[8] = "Exp";
		header[9] = "Location";
		header[10] = "No. of posns";
		header[11] = "Requirement Status";
		header[12] = "Total";
		// int i = 12;
		// for (Entry<String, String> entry : statusMap.entrySet()) {
		// header[++i] = entry.getValue();
		// }

		int row = 0;
		// for (Position pos : positions) {

		for (; row < positionList.size(); row++) {
			List<Round> allRounds = roundService.getRoundsByBoardPositionCode(positionList.get(row).getBoard());
			int columns = allRounds.size();
			data[row] = new Object[columns + sheetColumnSize];
			Position position = positionList.get(row);
			data[row][0] = row + 1;
			data[row][1] = DateUtil.formateDate(position.getOpenedDate(), DateUtil.DATE_FORMAT_MONTH);
			data[row][2] = position.getClient().getClientName();

			if (position.getVerticalCluster() != null) {
				data[row][3] = position.getVerticalCluster();
			} else {
				data[row][3] = "NA";
			}

			if (position.getEndClient() != null) {
				data[row][4] = position.getEndClient();
			} else {
				data[row][4] = "NA";
			}

			if (position.getHiringManager() != null) {
				data[row][5] = position.getHiringManager();
			} else {
				data[row][5] = "NA";
			}

			if (position.getTeam() != null) {
				data[row][6] = position.getTeam().getTeamName();
			} else {
				data[row][6] = "NA";
			}

			data[row][7] = position.getTitle();
			data[row][8] = position.getExperienceRange();
			data[row][9] = position.getLocation();
			data[row][10] = position.getTotalPosition();
			data[row][11] = position.getStatus();
			data[row][12] = roundCandidateService.getCountByPositionCode(position.getPositionCode());
			int columnIndex = 12;

			for (Round allRound : allRounds) {
				// Round round =
				// roundService.getRoundByBoardAndName(position.getBoard(),
				// entry.getKey());
				Long count = roundCandidateService.getCountByPositionCodeAndRound(position.getPositionCode(), allRound);
				data[row][++columnIndex] = allRound.getRoundName() + " - " + count;
			}
			// Posns";
		}
		// }

		File excelFile = importExportService.resultSetToExcelExport(header, data, System.currentTimeMillis() + "",
				null);
		return excelFile;
	}

	// to get client status report
	public List<Map<String, Object>> getClientStatusReport(Integer intervalInMonths) {

		List<Map<String, Object>> report = new LinkedList<>();
		List<Client> allClients = clientService.findAll();
		if (null == allClients || allClients.isEmpty()) {
			return report;
		}

		for (Client client : allClients) {
			Map<String, Object> reportMap = new LinkedHashMap<>();
			reportMap.put("name", client.getClientName());
			reportMap.put("location", client.getClientLocation());
			reportMap.put("createdDate", DateUtil.formateDate(client.getCreationDate(), DateUtil.DATE_FORMAT_MONTH));
			reportMap.put("modificationDate",
					DateUtil.formateDate(client.getModificationDate(), DateUtil.DATE_FORMAT_MONTH));

			reportMap.put("totalPositions",
					positionService.getPositionCountByClientAndDateInterval(client.getId(), intervalInMonths));
			reportMap.put("totalOpenings",
					positionService.getTotalPositionsOpeiningByClientAndDateInterval(client.getId(), intervalInMonths));
			reportMap.put("active", positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.Active.toString(), Status.Active.getDisplayName()), intervalInMonths));
			reportMap.put("closed", positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.Closed.toString(), Status.Closed.getDisplayName()), intervalInMonths));
			reportMap.put("onhold", positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.OnHold.toString(), Status.OnHold.getDisplayName()), intervalInMonths));
			reportMap.put("stoppedsourcing",
					positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
							Arrays.asList(Status.StopSourcing.toString(), Status.StopSourcing.getDisplayName()),
							intervalInMonths));
			report.add(reportMap);
		}

		return report;
	}

	// to get position/opening requests by client
	public Map<String, Object> getPositionRequestsByClient(Integer intervalInMonths, Date startDate, Date endDate)
			throws RecruizException {

		// interval will work on position created not on update
		Map<String, Object> responsMap = new HashMap<>();

		List<Object> reportMap = new LinkedList<>();

		List<Team> teamList = teamService.getAllTeamsForCurrentUser();
		List<String> postionlist = new ArrayList<>();
		List<Position> allpostions = new ArrayList<>();
		Map<String, Object> graphResponse = new HashMap<>();

		if (teamList != null || permissionService.isSuperAdmin()) {

			if (permissionService.isSuperAdmin()) {

				// allpostions = positionService.getAllPosition();
				allpostions = positionService.getAllPositionsByModifiationDateBetween(startDate, endDate);
				if (allpostions != null) {

					for (Position eachpostion : allpostions) {

						postionlist.add(eachpostion.getPositionCode());
					}
				}

			} else {

				try{
					postionlist = positionService.getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(
							userService.getLoggedInUserEmail(), userService.getLoggedInUserObject(), teamList, startDate,
							endDate);

				}catch(Exception e){
					Date endDateNew = new Date();
					postionlist = positionService.getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(
							userService.getLoggedInUserEmail(), userService.getLoggedInUserObject(), teamList, startDate,
							endDateNew);
				}
			}

			if (postionlist != null && !postionlist.isEmpty()) {

				int totalOpening = 0;

				for (String eachpostion : postionlist) {
					Map<String, Object> positionMap = new LinkedHashMap<>();
					Position position = positionService.getPositionByCode(eachpostion);

					positionMap.put("name", position.getClient().getClientName());
					positionMap.put("position", position.getTitle());
					positionMap.put("opening", position.getTotalPosition());
					positionMap.put("pcode", position.getPositionCode());
					reportMap.add(positionMap);
					totalOpening += position.getTotalPosition();
					graphResponse.put(position.getClient().getClientName(), totalOpening);

				}

			}

		}

		//		List<Client> allClients = clientService.findAll();
		//		if (null == allClients || allClients.isEmpty()) {
		//			return responsMap;
		//		}
		//
		//		for (Client client : allClients) {
		//			List<Position> positions = positionService.getPositionByClientAndDateInterval(client, startDate, endDate);
		//			int totalOpening = 0;
		//			for (Position position : positions) {
		//				Map<String, Object> positionMap = new LinkedHashMap<>();
		//				positionMap.put("name", client.getClientName());
		//				positionMap.put("position", position.getTitle());
		//				positionMap.put("opening", position.getTotalPosition());
		//				positionMap.put("pcode", position.getPositionCode());
		//				reportMap.add(positionMap);
		//				totalOpening += position.getTotalPosition();
		//			}
		//			graphResponse.put(client.getClientName(), totalOpening);
		//		}

		responsMap.put("table", reportMap);
		responsMap.put("graph", graphResponse);

		return responsMap;
	}

	// @Sajin
	// to get position/opening requests by client (Excel Report)
	public File getExcelPositionRequestsByClient(Integer intervalInMonths, Date startDate, Date endDate)
			throws RecruizException, InvalidFormatException, IOException {

		// Excel file details

		int datarowCount = 0;
		int sheetColumnSize = 10;
		int sheetRowSize = 5000;
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client";
		header[2] = "Position/Requisition";
		header[3] = "No of Openings";

		List<Client> allClients = clientService.findAll();
		if (null == allClients || allClients.isEmpty()) {
			return null;
		}

		for (Client client : allClients) {
			List<Position> positions = positionService.getPositionByClientAndDateInterval(client, startDate, endDate);
			for (Position position : positions) {
				data[datarowCount][0] = datarowCount + 1;
				data[datarowCount][1] = client.getClientName();
				data[datarowCount][2] = position.getTitle();
				data[datarowCount][3] = position.getTotalPosition();
				datarowCount++;
			}
		}

		String fullfileName = "ClientPositionVsOpening";

		File excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;
	}

	// @Sajin
	// to get excel download of hiring pattern report
	public File getExcelHiringPatternReport(String clientName, Integer intervalInMonths)
			throws RecruizException, InvalidFormatException, IOException {

		// Excel file details
		int datarowCount = 0;
		int sheetColumnSize = 6;
		int sheetRowSize = 5000;
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client";
		header[2] = "Position/Requisition";
		header[3] = "No of Openings";
		header[4] = "No of Candidates";
		header[5] = "No of Joinees";

		List<Client> clients = new ArrayList<>();
		if (null == clientName || clientName.trim().isEmpty() || clientName.equalsIgnoreCase("all")) {
			clients.addAll(clientService.findAll());
		} else {
			if (clientName.startsWith("'") && clientName.endsWith("'")) {
				clientName = clientName.replaceAll("'", "");
			}
			List<String> cnames = StringUtils.commaSeparateStringToList(clientName);
			for (String name : cnames) {
				Client client = clientService.getClient(name);
				if (null == client) {
					throw new RecruizException(ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.CLIENT_NOT_FOUND);
				}
				clients.add(client);
			}

		}

		List<String> joinedstatuses = new ArrayList<>();
		joinedstatuses.add(BoardStatus.Joined.getDisplayName());
		joinedstatuses.add(BoardStatus.Employee.getDisplayName());

		for (Client client : clients) {
			Set<Position> positions = client.getPositions();
			for (Position position : positions) {

				data[datarowCount][0] = datarowCount + 1;
				data[datarowCount][1] = client.getClientName();
				data[datarowCount][2] = position.getTitle();
				data[datarowCount][3] = position.getTotalPosition();
				data[datarowCount][4] = roundCandidateService.getCountByPositionCode(position.getPositionCode());
				data[datarowCount][5] = roundCandidateService
						.getCountByPositionCodeAndStatusIn(position.getPositionCode(), joinedstatuses);

				datarowCount++;
			}
		}
		String fullfileName = "Hiring Pattern Report";

		File excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;
	}

	// @Sajin
	// to get client status report in excel
	public File getExcelClientStatusReport(Integer intervalInMonths) throws InvalidFormatException, IOException {

		// Excel file details
		int datarowCount = 0;
		int sheetColumnSize = 20;
		int sheetRowSize = 5000;
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client";
		header[2] = "Location";
		header[3] = "Added on";
		header[4] = "Last modified on";
		header[5] = "Total positions";
		header[6] = "No of Openings";
		header[7] = "Active";
		header[8] = "Closed";
		header[9] = "On-hold";
		header[10] = "Stop Sourcing";

		List<Client> allClients = clientService.findAll();
		if (null == allClients || allClients.isEmpty()) {
			return null;
		}

		for (Client client : allClients) {

			data[datarowCount][0] = datarowCount + 1;
			data[datarowCount][1] = client.getClientName();
			data[datarowCount][2] = client.getClientLocation();
			data[datarowCount][3] = DateUtil.formateDate(client.getCreationDate());
			data[datarowCount][4] = DateUtil.formateDate(client.getModificationDate());
			data[datarowCount][5] = positionService.getPositionCountByClientAndDateInterval(client.getId(),
					intervalInMonths);
			data[datarowCount][6] = positionService.getTotalPositionsOpeiningByClientAndDateInterval(client.getId(),
					intervalInMonths);
			data[datarowCount][7] = positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.Active.toString(), Status.Active.getDisplayName()), intervalInMonths);
			data[datarowCount][8] = positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.Closed.toString(), Status.Closed.getDisplayName()), intervalInMonths);
			data[datarowCount][9] = positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.OnHold.toString(), Status.OnHold.getDisplayName()), intervalInMonths);
			data[datarowCount][10] = positionService.getTotalPositionsByStatusesClientAndDateInterval(client.getId(),
					Arrays.asList(Status.StopSourcing.toString(), Status.StopSourcing.getDisplayName()),
					intervalInMonths);
			datarowCount++;

		}

		String fullfileName = "ClientStatus";

		File excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;

	}

	// to get position/opening requests by client
	public List<Object> getHiringPatternReport(ReportDropdownDTO reportDTO, String clientName, Integer intervalInMonths,
			Date startdate, Date enddate) throws RecruizException {

		Set<String> clientNameList = new LinkedHashSet<>(); 
		List<Object> mapList = new LinkedList<>();
		List<String> status = new ArrayList<>();
		status.add(BoardStatus.Joined.getDisplayName());
		// List<Team> teamList = teamService.getAllTeamsForCurrentUser();

		List<Team> teamList = teamService.getAllDirectTeamsForUser(userService.getLoggedInUserObject());
		List<String> postionlist = new ArrayList<>();
		List<Position> allpostions = new ArrayList<>();
		Set<String> set = new LinkedHashSet<>(); 

		// @sajin - This has been changed to take care of Clients/Position specific to
		// the logged in user
		// Since clients are not tagged to users/teams, first we loop through the
		// positions to get the positions
		// that the user is owner of or part of. Then the client list is obtained from
		// this.
		if (teamList != null || permissionService.isSuperAdmin()) {

			if (permissionService.isSuperAdmin()) {

				// allpostions = positionService.getAllPosition();
				allpostions = positionService.getAllPositionsByModifiationDateBetween(startdate, enddate);
				if (allpostions != null) {

					for (Position eachpostion : allpostions) {

						postionlist.add(eachpostion.getPositionCode());
					}
				}

			} else {

				postionlist = positionService.getPositionCodesForOwnerOrHrExecutivesInOrTeamIn(
						userService.getLoggedInUserEmail(), userService.getLoggedInUserObject(), teamList, startdate,
						enddate);

			}
		}

		set.addAll(postionlist); 
		postionlist.clear(); 
		postionlist.addAll(set); 

		if (postionlist != null) {
			for (String eachpositioncode : postionlist) {

				clientNameList.add(positionService.getPositionByCode(eachpositioncode).getClient().getClientName());
			}
		}

		for (String cliName : clientNameList) {
			Client client = clientRepository.findByClientName(cliName);

			if(client!=null){
				Set<Position> positions = client.getPositions();
				for (Position position : positions) {
					Map<String, Object> reportMap = new LinkedHashMap<>();
					reportMap.put("name", client.getClientName());
					reportMap.put("position", position.getTitle());
					reportMap.put("opening", position.getTotalPosition());
					reportMap.put("candidate", roundCandidateService.getCountByPositionCode(position.getPositionCode()));
					reportMap.put("joinees",
							roundCandidateService.getCountByPositionCodeAndStatusIn(position.getPositionCode(), status));
					mapList.add(reportMap);
				}
			}
		}

		//		List<Client> clients = new ArrayList<>();
		//		
		//
		//		if (null == clientName || clientName.trim().isEmpty()) {
		//			clients = clientService.getClientbyDates(startdate, enddate);
		//		} else {
		//			if (clientName.startsWith("'") && clientName.endsWith("'")) {
		//				clientName = clientName.replaceAll("'", "");
		//			}
		//			List<String> cnames = StringUtils.commaSeparateStringToList(clientName);
		//			for (String name : cnames) {
		//				Client client = clientService.getClient(name);
		//				if (null == client) {
		//					throw new RecruizException(ErrorHandler.CLIENT_NOT_EXISTS, ErrorHandler.CLIENT_NOT_FOUND);
		//				}
		//				clients.add(client);
		//			}
		//
		//		}
		//
		//		List<Object> mapList = new LinkedList<>();
		//
		//		List<String> status = new ArrayList<>();
		//		status.add(BoardStatus.Joined.getDisplayName());
		//
		//		for (Client client : clients) {
		//			Set<Position> positions = client.getPositions();
		//			for (Position position : positions) {
		//				Map<String, Object> reportMap = new LinkedHashMap<>();
		//				reportMap.put("name", client.getClientName());
		//				reportMap.put("position", position.getTitle());
		//				reportMap.put("opening", position.getTotalPosition());
		//				reportMap.put("candidate", roundCandidateService.getCountByPositionCode(position.getPositionCode()));
		//				reportMap.put("joinees",
		//						roundCandidateService.getCountByPositionCodeAndStatusIn(position.getPositionCode(), status));
		//				mapList.add(reportMap);
		//			}
		//		}
		return mapList;
	}

	// to get position/opening requests by client
	public List<Object> getResourceRequestsOutstanding(Integer intervalInMonths) {
		List<Object> reportMapList = new LinkedList<>();

		List<Client> allClients = clientService.findAll();
		if (null == allClients || allClients.isEmpty()) {
			return reportMapList;
		}
		try{
			for (Client client : allClients) {
				Map<String, Object> reportMap = new LinkedHashMap<>();
				reportMap.put("name", client.getClientName());
				reportMap.put("location", client.getClientLocation());

				List<Position> list = positionService.getAllPositionByClient(client);
				int count=0;

				for (Position position : list) {
					if(position.getStatus().equalsIgnoreCase("Active"))
						count++;
				}


				reportMap.put("TotalOpenedPosition", count);
				reportMap.put("PositionclosedPastDate", positionService.getCountForPastCloseByDate(client.getId()));
				reportMap.put("lessThan30Days", positionService.getCountForLessThan1MonthClosureDate(client.getId()));
				reportMap.put("between30To60Days", positionService.getCountFor1To2MonthClosureDate(client.getId()));
				reportMap.put("moreThan60Days", positionService.getCountForMoreThan2MonthClosureDate(client.getId()));
				reportMapList.add(reportMap);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return reportMapList;
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

	// to get position/opening requests by client
	public Map<String, Object> getOverAllPositionStatus(List<Client> allClients, Date startDate, Date endDate,
			List<String> positionCodes) {

		Map<String, Object> reportMap = new LinkedHashMap<>();

		List<Object> allClnt = new ArrayList<>();

		Set<String> status = new HashSet<>();
		status.add(BoardStatus.Joined.getDisplayName());
		Long totalForwarded = 0L;
		Long totalShortlisted = 0L;
		Long totalInterview = 0L;
		Long totalSelected = 0L;
		Long totalOffered = 0L;
		Long totalJoined = 0L;
		for (Client client : allClients) {

			List<Position> positions = new ArrayList<>();
			if (positionCodes != null && !positionCodes.isEmpty()) {
				positions = positionService.getPositionByClientAndPositionCodeInAndDateInterval(client, positionCodes,
						startDate, endDate);
			} else {
				positions = positionService.getPositionByClientAndDateInterval(client, startDate, endDate);
			}

			if (null == positions || positions.isEmpty()) {
				continue;
			}

			for (int pCount = 0; pCount < positions.size(); pCount++) {
				Position position = positions.get(pCount);
				Map<String, Object> data = new LinkedHashMap<>();
				Long joined = roundCandidateService.getCountByPositionAndStatusAndDateRange(position.getPositionCode(),
						BoardStatus.Joined.getDisplayName(), startDate, endDate);

				totalJoined += joined;

				Long offered = roundCandidateService.getCountByPositionAndStatusAndDateRange(position.getPositionCode(),
						BoardStatus.Offered.getDisplayName(), startDate, endDate);
				totalOffered += offered;

				Long selected = roundCandidateService.getCountByPositionAndStatusAndDateRange(
						position.getPositionCode(), BoardStatus.Selected.getDisplayName(), startDate, endDate);
				totalSelected += selected;

				Long forwarded = feedbackService.getForwardProfileCountForPosition(position.getTitle(),
						client.getClientName(), startDate, endDate);
				totalForwarded += forwarded;

				Long interviews = interviewScheduleService
						.getInterviewCountByPositionForDateRange(position.getPositionCode(), startDate, endDate);
				totalInterview += interviews;

				data.put("name", client.getClientName());
				data.put("clientLocation", client.getClientLocation());
				data.put("position", position.getTitle());
				data.put("opening", position.getTotalPosition());
				data.put("forwarded", forwarded);
				data.put("shortlisted", 0);
				data.put("interviews", interviews);
				data.put("selected", selected);
				data.put("offered", offered);
				data.put("joined", joined);
				allClnt.add(data);
			}
		}

		Map<String, String> headerData = new LinkedHashMap<>();
		headerData.put("forwardVsShortlist", totalForwarded + "/" + totalShortlisted);
		headerData.put("interviewVsSelected", totalInterview + "/" + totalSelected);
		headerData.put("selectedVsOffered", totalSelected + "/" + totalOffered);
		headerData.put("offeredVsJoined", totalOffered + "/" + totalJoined);

		reportMap.put("headerData", headerData);
		reportMap.put("bodyData", allClnt);

		return reportMap;
	}

	// to get client status report
	public Map<String, Object> getClientStatusReport(Date startDate, Date endDate) {
		Map<String, Object> reportMap = new LinkedHashMap<>();

		return reportMap;
	}

	public Map<String, Object> getCandidateCountStatusWiseForTeam(Date startDate, Date endDate, String positionCode)
			throws RecruizException {
		String template = "all_status_count_sql.vm";

		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		templateVariableMap.put("positionCode", positionCode);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/team-performance-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		countResponse.put("pcode", positionCode);
		countResponse.put("status", positionService.getPositionByCode(positionCode).getStatus());
		// logger.error(sql);
		return countResponse;
	}

	public Map<String, Object> getStatForTeam(List<Long> teamIds, String hrEmail, Date startDate, Date endDate,
			String positionCodes) throws RecruizException {

		String ids = Joiner.on(",").join(teamIds);
		String template = "related_entity_count.vm";
		Map<String, Object> templateVariableMap = new HashMap<String, Object>();
		templateVariableMap.put("pcodes", positionCodes);
		templateVariableMap.put("hrEmail", hrEmail);
		templateVariableMap.put("teamIds", ids);
		templateVariableMap.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateVariableMap.put("endDate", new java.sql.Timestamp(endDate.getTime()));

		@SuppressWarnings("deprecation")
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/team-performance-query/" + template, "UTF-8", templateVariableMap);

		Map<String, Object> countResponse = jdbcTemplate.queryForMap(sql);
		return countResponse;
	}

	public List<RecPrefDTO> getRecPrefReportForTeamware(Long teamId, List<String> csvLines, String reportType,
			List<Long> clients, String location, String vertical, Date startDate, Date endDate)
					throws RecruizException {
		Team team = teamService.findOne(teamId);
		if (null == team) {
			throw new RecruizException(ErrorHandler.TEAM_NOT_AVAILABLE, ErrorHandler.NO_TEAM_FOUND);
		}

		List<RecPrefDTO> reports = new LinkedList<>();

		if (null == reportType || reportType.trim().isEmpty()) {
			addRecPrefHeader(csvLines);
		} else if (reportType.equalsIgnoreCase("Pipeline")) {
			addPipelineHeader(csvLines);
		} else if (reportType.equalsIgnoreCase("BizAnalysis")) {
			addBizAnalysisHeader(csvLines);
		}

		
		logger.error(" Team details = "+team.toString());
		logger.error(" Team childrens details = "+team.getChildren().toString());
		
		if (team.getChildren() != null && !team.getChildren().isEmpty()) {
			for (Team level1 : team.getChildren()) {
				if (level1.getChildren() != null && !level1.getChildren().isEmpty()) {
					for (Team level2 : level1.getChildren()) {
						if (level2.getChildren() != null && !level2.getChildren().isEmpty()) {
							for (Team level3 : level2.getChildren()) {
								if (level3.getChildren() != null && !level3.getChildren().isEmpty()) {
									for (Team level4 : level3.getChildren()) {
										if (level4.getChildren() == null || level4.getChildren().isEmpty()) {
											
											logger.error(" level 4 Team details = "+level4.toString());
											
											csvLines = getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport(
													level1.getTeamName(), level2.getTeamName(), level3.getTeamName(),
													level4.getTeamName(), level4, reports, csvLines, reportType,
													vertical, location, clients, startDate, endDate);
										}
									}
								} else {
									
									logger.error(" level 3 Team details = "+level3.toString());
									
									csvLines = getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport(team.getTeamName(),
											level1.getTeamName(), level2.getTeamName(), level3.getTeamName(), level3,
											reports, csvLines, reportType, vertical, location, clients, startDate,
											endDate);
								}
							}
						} else {
							logger.error(" level 2 Team details = "+level2.toString());
							
							csvLines = getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport("NA", team.getTeamName(),
									level1.getTeamName(), level2.getTeamName(), level2, reports, csvLines, reportType,
									vertical, location, clients, startDate, endDate);
						}
					}
				} else {
					
					logger.error(" level 1 Team details = "+level1.toString());
					
					csvLines = getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport("NA", "NA", team.getTeamName(),
							level1.getTeamName(), level1, reports, csvLines, reportType, vertical, location, clients,
							startDate, endDate);
				}
			}
		} else {
			
			logger.error(" else Team details = "+team.toString());
			
			csvLines = getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport("NA", "NA", "NA", team.getTeamName(), team,
					reports, csvLines, reportType, vertical, location, clients, startDate, endDate);
		}

		return reports;
	}

	private List<String> getTeamwareReqPrefOrPipelineReportOrBizAnalysisReport(String level1, String level2,
			String level3, String level4, Team team, List<RecPrefDTO> reports, List<String> csvLines, String reportType,
			String vertical, String location, List<Long> clients, Date startDate, Date endDate)
					throws RecruizException {
		if (null == reportType || reportType.trim().isEmpty()) {

			return getRecPrefData(level1, level2, level3, level4, team, reports, csvLines, startDate, endDate);

		} else if (reportType.equalsIgnoreCase("Pipeline")) {
			return getTeamwarePipelineReport(level1, level2, level3, level4, team, reports, csvLines, startDate,
					endDate);
		} else if (reportType.equalsIgnoreCase("BizAnalysis")) {
			return getBizAnalysis(level1, level2, level3, level4, team, reports, csvLines, clients, location, vertical,
					startDate, endDate);
		}
		return csvLines;
	}

	public List<PrefTrend> getTeamwarePrefTrendReport(Long teamId, List<String> csvLines, List<Long> clients,
			String location, String vertical, Date startDate, Date endDate) throws RecruizException {

		Team team = teamService.findOne(teamId);
		if (null == team) {
			throw new RecruizException(ErrorHandler.TEAM_NOT_AVAILABLE, ErrorHandler.NO_TEAM_FOUND);
		}

		List<PrefTrend> reports = new LinkedList<>();
		addPerformanceTrendsHeader(csvLines);

		if (team.getChildren() != null && !team.getChildren().isEmpty()) {
			for (Team level1 : team.getChildren()) {
				if (level1.getChildren() != null && !level1.getChildren().isEmpty()) {
					for (Team level2 : level1.getChildren()) {
						if (level2.getChildren() != null && !level2.getChildren().isEmpty()) {
							for (Team level3 : level2.getChildren()) {
								if (level3.getChildren() != null && !level3.getChildren().isEmpty()) {
									for (Team level4 : level3.getChildren()) {
										if (level4.getChildren() == null || level4.getChildren().isEmpty()) {
											csvLines = getPrefReportForTeamware(level1.getTeamName(),
													level2.getTeamName(), level3.getTeamName(), level4.getTeamName(),
													level4, reports, csvLines, startDate, endDate);
										}
									}
								} else {
									csvLines = getPrefReportForTeamware(team.getTeamName(), level1.getTeamName(),
											level2.getTeamName(), level3.getTeamName(), level3, reports, csvLines,
											startDate, endDate);
								}
							}
						} else {
							csvLines = getPrefReportForTeamware("NA", team.getTeamName(), level1.getTeamName(),
									level2.getTeamName(), level2, reports, csvLines, startDate, endDate);
						}
					}
				} else {
					csvLines = getPrefReportForTeamware("NA", "NA", team.getTeamName(), level1.getTeamName(), level1,
							reports, csvLines, startDate, endDate);
				}
			}
		} else {
			csvLines = getPrefReportForTeamware("NA", "NA", "NA", team.getTeamName(), team, reports, csvLines,
					startDate, endDate);
		}

		return reports;

	}

	private String addPerformanceTrendsHeader(List<String> csvLines) {

		final String lineDelim = "|";

		StringBuilder headerLine = new StringBuilder();
		headerLine.append("SBU Head").append(lineDelim);
		headerLine.append("SBU").append(lineDelim);
		headerLine.append("DM").append(lineDelim);
		headerLine.append("DL/Team").append(lineDelim);
		headerLine.append("Recruiter").append(lineDelim);
		headerLine.append("Year").append(lineDelim);
		headerLine.append("Month").append(lineDelim);
		headerLine.append("Week").append(lineDelim);
		headerLine.append("Req").append(lineDelim).append("# Posns").append(lineDelim).append("# CVs Parsed")
		.append(lineDelim).append("CVs Per Day").append(lineDelim).append("# CVs Cleared by Recr (L1)")
		.append(lineDelim);
		headerLine.append("# CVs Cleared by Scr/DL (L2)").append(lineDelim).append("# CVs Tech cleared")
		.append(lineDelim).append("# of Client Subm").append(lineDelim);
		headerLine.append("# Interviewed").append(lineDelim).append("# Selects").append(lineDelim).append("# Offered")
		.append(lineDelim).append("# Joined").append(lineDelim);
		headerLine.append("# CV Client Rej").append(lineDelim).append("Interview Rejects").append(lineDelim)
		.append("Interview Noshows").append(lineDelim).append("Joinee No Show").append(lineDelim);
		headerLine.append("L2 / L1").append(lineDelim).append("Interview Attended / Interview Scheduled")
		.append(lineDelim).append("CVs Sub / L1").append(lineDelim).append("Joined / CVs Sub").append(lineDelim)
		.append("CVs Sub / Posn").append(lineDelim);
		headerLine.append("1st Lvl Int / CV").append(lineDelim).append("Sels / Int").append(lineDelim)
		.append("Offer / Sels").append(lineDelim);
		headerLine.append("Joined / Offer").append(lineDelim).append("Joined / Posns").append(lineDelim);

		csvLines.add(headerLine.toString());
		return lineDelim;
	}

	// @Teamware-PerformanceTrendReport
	public List<String> getPrefReportForTeamware(String level1, String level2, String level3, String level4, Team team,
			List<PrefTrend> reports, List<String> csvLines, Date start, Date end) throws RecruizException {

		final String lineDelim = "|";

		List<String> interviewedStatuses = getInterviwedStatuses();
		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewRejectStatuses = getinterviewRejectStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();

		List<Integer> years = positionService.getYearsForGivenDateRange(start, end);
		for (Integer year : years) {
			List<String> months = positionService.getMonthsForGiveYear(year, start, end);
			for (String month : months) {
				List<Integer> weeks = positionService.getWeeksForGivenYearAndMonth(month, year, start, end);
				for (Integer weekNo : weeks) {
					// DateTime weekStartDate = new DateTime().withWeekOfWeekyear(weekNo);
					// DateTime weekEndDate = new DateTime().withWeekOfWeekyear(weekNo + 1);

					DateTime weekStartDate = null;
					DateTime weekEndDate = null;

					if (weekNo != 0) {
						weekStartDate = new DateTime().withWeekOfWeekyear(weekNo);

					} else {
						continue;
					}

					if (weekNo < 52) {
						weekEndDate = new DateTime().withWeekOfWeekyear(weekNo + 1);
					} else {
						weekEndDate = new DateTime().withWeekOfWeekyear(1);
					}

					String week = "week " + weekNo;
					// Long sourcedCvs =
					// roundCandidateService.getCountByPositionCodes(positionCodes);
					// Long cvPerdayForWeek = sourcedCvs.longValue() / 5;

					if (team.getMembers() != null && !team.getMembers().isEmpty()) {
						for (TeamMember member : team.getMembers()) {

							PrefTrend recPref = new PrefTrend();
							StringBuilder line = new StringBuilder();

							String userEmail = member.getUser().getEmail();

							List<String> positionCodes = positionService.getPositionCodesForOwnerOrHrExecutivesIn(
									userEmail, member.getUser(), weekStartDate.toDate(), weekEndDate.toDate());

							if (null == positionCodes || positionCodes.isEmpty()) {
								continue;
							}

							recPref.setSbu(level1);
							line.append(level1).append(lineDelim);

							recPref.setDm(level2);
							line.append(level2).append(lineDelim);

							recPref.setDl(level3);
							line.append(level3).append(lineDelim);

							recPref.setTeam(level4);
							line.append(level4).append(lineDelim);

							recPref.setRecruiter(member.getUser().getName() + "/" + userEmail);
							line.append(recPref.getRecruiter()).append(lineDelim);

							recPref.setYear(year);
							line.append(year).append(lineDelim);

							recPref.setMonth(month);
							line.append(month).append(lineDelim);

							line.append(week).append(lineDelim);
							recPref.setWeek(week);

							// Total Position count
							recPref.setReqs(positionCodes.size());
							line.append(positionCodes.size()).append(lineDelim);

							// Total Opening count
							recPref.setPosns(positionService.getTotalOpenPositionByPositionCodes(positionCodes));
							line.append(recPref.getPosns()).append(lineDelim);

							// Total CVs added to position
							recPref.setCvsParsed(roundCandidateService.getCountByPositionCodesAndSourcedByAndDateRange(
									positionCodes, userEmail, weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getCvsParsed()).append(lineDelim);

							// CVs added per day by the recruiter
							Long cvPerdayForWeek = 0L;
							if (recPref.getCvsParsed() != null) {
								cvPerdayForWeek = ((Long) recPref.getCvsParsed()) / 7;
							}
							recPref.setCvsClearedPerDay(cvPerdayForWeek);
							line.append(cvPerdayForWeek).append(lineDelim);

							// Total CVs in Cleared by L1 status
							recPref.setCvsClearedByL1(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.CvClearedByL1, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getCvsClearedByL1()).append(lineDelim);

							// Total CVs in CV sent to Client status
							roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
									TeamwareConstants.CVSenttoClient, userEmail, weekStartDate.toDate(),
									weekEndDate.toDate());

							// Total CVs in Cleared by L2 status
							recPref.setCvsClearedByL2(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.CvClearedByL2, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getCvsClearedByL2()).append(lineDelim);

							// Total of Tech cleared status
							recPref.setCvsTechCleared(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.CvsTechCleared, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getCvsTechCleared()).append(lineDelim);

							// @sajin - Falling back to counting Client submission count based on Status and
							// not based on Feedback count
							recPref.setClientSubmission(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.CVSenttoClient, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getClientSubmission()).append(lineDelim);

							// recPref.setClientSubmission(feedbackService.getFeedbackCountByPositionCode(positionCodes));
							// line.append(recPref.getClientSubmission()).append(lineDelim);

							// total interviewed
							// Long totalInterviewed = interviewScheduleService
							// .getInterviewCountByPositionInAndHr(positionCodes, userEmail);
							// recPref.setInterviewed(totalInterviewed);
							// line.append(totalInterviewed).append(lineDelim);

							// Total Interviewed
							recPref.setInterviewed(
									roundCandidateService.getCountByPositionCodesAndStatusInAndOnwerAndDateRange(
											positionCodes, interviewedStatuses, userEmail, weekStartDate.toDate(),
											weekEndDate.toDate()));
							line.append(recPref.getInterviewed()).append(lineDelim);

							// Total Selected
							recPref.setSelected(roundCandidateService
									.getCountByPositionCodesAndStatusInAndOnwerAndDateRange(positionCodes,
											selectStatuses, userEmail, weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getSelected()).append(lineDelim);

							// Total Offered
							recPref.setOffered(roundCandidateService
									.getCountByPositionCodesAndStatusInAndOnwerAndDateRange(positionCodes,
											offeredStatuses, userEmail, weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getOffered()).append(lineDelim);

							// Total Joined
							// Changed to consider JoinedbyHR also in the query.
							String joinedByHr = userEmail;
							recPref.setJoined(roundCandidateService
									.getCountByPositionCodesAndStatusOrJoinedByHrAndOwnerAndDateRange(positionCodes,
											BoardStatus.Joined.name(), userEmail, joinedByHr, weekStartDate.toDate(),
											weekEndDate.toDate()));
							line.append(recPref.getJoined()).append(lineDelim);

							recPref.setCvsClientRejected(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.ClientRejected, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getCvsClientRejected()).append(lineDelim);

							recPref.setInterviewRejected(
									roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(
											positionCodes, interviewRejectStatuses, userEmail, weekStartDate.toDate(),
											weekEndDate.toDate()));
							line.append(recPref.getInterviewRejected()).append(lineDelim);

							recPref.setInterviewNoShow(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.InterviewNoShow, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getInterviewNoShow()).append(lineDelim);

							recPref.setJoineeNoShow(
									roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
											positionCodes, TeamwareConstants.JoineeNoShow, userEmail,
											weekStartDate.toDate(), weekEndDate.toDate()));
							line.append(recPref.getJoineeNoShow()).append(lineDelim);

							recPref.setL2VsL1(ratio(recPref.getCvsClearedByL2(), recPref.getCvsClearedByL1()));
							line.append(recPref.getL2VsL1()).append(lineDelim);

							// @sajin - Changes to use status instead of feedback count for CVs Submitted.
							recPref.setCvsSubVsL1(ratio(recPref.getClientSubmission(), recPref.getCvsClearedByL1()));
							line.append(recPref.getCvsSubVsL1()).append(lineDelim);

							//							recPref.setCvsSubVsL1(ratio(feedbackService.getFeedbackCountByPositionCode(positionCodes),
							//									recPref.getCvsClearedByL1()));
							//							line.append(recPref.getCvsSubVsL1()).append(lineDelim);

							// @sajin - Changes to use status instead of feedback count for CVs Submitted.
							recPref.setJoinedVsCvsSub(ratio(recPref.getJoined(), recPref.getClientSubmission()));
							line.append(recPref.getJoinedVsCvsSub()).append(lineDelim);

							recPref.setCvsSubVsPosns(ratio(recPref.getClientSubmission(), recPref.getPosns()));
							line.append(recPref.getCvsSubVsPosns()).append(lineDelim);

							recPref.setLvl1interVsCvs(ratio(recPref.getInterviewed(), recPref.getClientSubmission()));
							line.append(recPref.getLvl1interVsCvs()).append(lineDelim);

							recPref.setInterviewVsScheduled(ratio(recPref.getInterviewed(),
									roundCandidateService.getCountByPositionCodesAndStatusInAndOnwerAndDateRange(
											positionCodes, interviewScheduled, userEmail, weekStartDate.toDate(),
											weekEndDate.toDate())));
							line.append(recPref.getInterviewVsScheduled()).append(lineDelim);

							recPref.setSelectedVsInt(ratio(recPref.getSelected(), recPref.getInterviewed()));
							line.append(recPref.getSelectedVsInt()).append(lineDelim);

							recPref.setOfferVsSelected(ratio(recPref.getOffered(), recPref.getSelected()));
							line.append(recPref.getOfferVsSelected()).append(lineDelim);

							recPref.setJoinedVsOffer(ratio(recPref.getJoined(), recPref.getOffered()));
							line.append(recPref.getJoinedVsOffer()).append(lineDelim);

							recPref.setJoinedVsPosns(ratio(recPref.getJoined(), recPref.getPosns()));
							line.append(recPref.getJoinedVsPosns());

							csvLines.add(line.toString());
							reports.add(recPref);
						}
					}

				}
			}
		}

		return csvLines;
	}

	// @Teamware-RecruiterPerformanceReport
	private List<String> getRecPrefDataOld(String level1, String level2, String level3, String level4, Team team,
			List<RecPrefDTO> reports, List<String> csvLines, Date startDate, Date endDate) {
		final String lineDelim = "|";

		List<Position> positions = positionService.getPositionByTeamAndStatusClosedAndDateRange(team, startDate,
				endDate);
		if (null == positions || positions.isEmpty()) {
			return csvLines;
		}

		if (team.getMembers() != null && !team.getMembers().isEmpty()) {
			// for (TeamMember member : team.getMembers()) {
			// String userEmail = member.getUser().getEmail();
			for (Position position : positions) {
				for (User user : position.getHrExecutives()) {
					String userEmail = user.getEmail();
					StringBuilder line = new StringBuilder();
					String positionCode = position.getPositionCode();
					String clientName = positionService.getClientNameFromPositioncode(positionCode);

					List<String> lstPositioncode = new ArrayList<>();
					lstPositioncode.add(position.getPositionCode());

					// Board board = position.getBoard();
					RecPrefDTO recPref = new RecPrefDTO();
					recPref.setSbu(level1);
					line.append(level1).append(lineDelim);

					recPref.setDm(level2);
					line.append(level2).append(lineDelim);

					recPref.setDl(level3);
					line.append(level3).append(lineDelim);

					recPref.setTeam(level4);
					line.append(level4).append(lineDelim);

					recPref.setRecruiter(user.getName() + "/" + userEmail);
					line.append(recPref.getRecruiter()).append(lineDelim);

					recPref.setClient(clientName);
					line.append(recPref.getClient()).append(lineDelim);

					recPref.setLocation(position.getLocation());
					line.append(recPref.getLocation()).append(lineDelim);

					recPref.setVertical(position.getVerticalCluster());
					line.append(recPref.getVertical()).append(lineDelim);

					recPref.setReqs(position.getTitle());
					line.append(recPref.getReqs()).append(lineDelim);

					recPref.setRequirementType(position.getCustomField().get("Requirement Type") == null ? "NA"
							: position.getCustomField().get("Requirement Type"));
					line.append(recPref.getRequirementType()).append(lineDelim);

					recPref.setPosns(position.getTotalPosition());
					line.append(recPref.getPosns()).append(lineDelim);

					recPref.setCvParsed(
							roundCandidateService.getCountByPositionCodeAndSourcedByAndModificationDateBetween(
									positionCode, userEmail, startDate, endDate) + "");
					line.append(recPref.getCvParsed()).append(lineDelim);

					recPref.setCvClearedbyL1(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.CvClearedByL1, userEmail, startDate,
							endDate));
					line.append(recPref.getCvClearedbyL1()).append(lineDelim);

					recPref.setCvClearedbyL2(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCvClearedbyL2()).append(lineDelim);

					recPref.setCvClearedbyL2Awaiting(roundCandidateService
							.getCountByPositionCodeAndStatusAndOwnerAndDateRange(position.getPositionCode(),
									TeamwareConstants.CvClearedByL2Awaiting, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCvClearedbyL2Awaiting()).append(lineDelim);

					recPref.setCvTechCleared(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.CvsTechCleared, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCvTechCleared()).append(lineDelim);

					recPref.setCvForwarded(feedbackService.getForwardProfileCountForPositionByUser(position.getTitle(),
							position.getClient().getClientName(), userEmail));
					line.append(recPref.getCvForwarded()).append(lineDelim);

					recPref.setAwCvUpd(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.AwaitingCvUpdate, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getAwCvUpd()).append(lineDelim);

					recPref.setInt1(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview1, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getInt1()).append(lineDelim);

					recPref.setInt2(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview2, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getInt2()).append(lineDelim);

					recPref.setInt3(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview3, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getInt3()).append(lineDelim);

					recPref.setInt4(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview4, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getInt4()).append(lineDelim);

					recPref.setFinalInt(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.FinalRound, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getFinalInt()).append(lineDelim);

					recPref.setAwOff(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.AwaitingOffer, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getFinalInt()).append(lineDelim);

					recPref.setAwOffAcc(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.AwaitingOfferAcceptance, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getAwOffAcc()).append(lineDelim);

					recPref.setAwJng(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.AwaitingJoining, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getAwJng()).append(lineDelim);

					recPref.setJoined(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), BoardStatus.Joined.name(), userEmail, startDate, endDate) + "");
					line.append(recPref.getJoined()).append(lineDelim);

					recPref.setCvRejected(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), BoardStatus.Rejected.name(), userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCvRejected()).append(lineDelim);

					recPref.setCvOnHold(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), BoardStatus.OnHold.name(), userEmail, startDate, endDate) + "");
					line.append(recPref.getCvOnHold()).append(lineDelim);

					recPref.setCvDuplicate(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), BoardStatus.Duplicate.name(), userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCvDuplicate()).append(lineDelim);

					recPref.setRejLvl1(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview1Reject, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getRejLvl1()).append(lineDelim);

					recPref.setRejLvl2(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview2Reject, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getRejLvl2()).append(lineDelim);

					recPref.setRejLvl3(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview3Reject, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getRejLvl3()).append(lineDelim);

					recPref.setRejLvl4(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.Interview4Reject, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getRejLvl4()).append(lineDelim);

					recPref.setRejLvlFinal(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.FinalRoundReject, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getRejLvlFinal()).append(lineDelim);

					recPref.setIntNoShow(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.InterviewNoShow, userEmail, startDate,
							endDate) + "");
					line.append(recPref.getIntNoShow()).append(lineDelim);

					recPref.setCndDropByUs(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.DroppedByUs, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getCndDropByUs()).append(lineDelim);

					recPref.setOfrOnHold(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.OfferOnHold, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getOfrOnHold()).append(lineDelim);

					recPref.setJoinNoShow(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.JoineeNoShow, userEmail, startDate, endDate)
							+ "");
					line.append(recPref.getJoinNoShow()).append(lineDelim);

					// Check here...

					recPref.setL2Vsl1(ratio(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
							position.getPositionCode(), TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate),
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), TeamwareConstants.CvClearedByL1, userEmail, startDate,
									endDate)));
					line.append(recPref.getL2Vsl1()).append(lineDelim);

					recPref.setCvsSubVsl1(ratio(
							feedbackService.getForwardProfileCountForPositionByUser(position.getTitle(),
									position.getClient().getClientName(), userEmail),
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), TeamwareConstants.CvClearedByL1, userEmail, startDate,
									endDate)));
					line.append(recPref.getCvsSubVsl1()).append(lineDelim);

					recPref.setJoinedVscvSub(ratio(
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Joined.name(), userEmail, startDate,
									endDate),
							feedbackService.getForwardProfileCountForPositionByUser(position.getTitle(),
									position.getClient().getClientName(), userEmail)));
					line.append(recPref.getJoinedVscvSub()).append(lineDelim);

					recPref.setCvSubVsPosns(ratio(feedbackService.getForwardProfileCountForPositionByUser(
							position.getTitle(), position.getClient().getClientName(), userEmail), recPref.getPosns()));
					line.append(recPref.getCvSubVsPosns()).append(lineDelim);

					recPref.setInt1vsSourced(ratio(
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), TeamwareConstants.Interview1, userEmail, startDate,
									endDate),
							roundCandidateService.getCountByPositionCodeAndSourcedBy(positionCode, userEmail)));
					line.append(recPref.getInt1vsSourced()).append(lineDelim);

					recPref.setSelectedVsScheduled(ratio(
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Selected.name(), userEmail, startDate,
									endDate),
							interviewScheduleService.getInterviewCountByPositionAndHr(positionCode, userEmail)));
					line.append(recPref.getSelectedVsScheduled()).append(lineDelim);

					List<String> offeredStatuses = new ArrayList<>();
					offeredStatuses.add(BoardStatus.Offered.name());
					offeredStatuses.add(TeamwareConstants.AwaitingOfferAcceptance);

					recPref.setOfferVsSelected(ratio(
							roundCandidateService.countByPositionCodeAndStatusInAndSourcedBy(position.getPositionCode(),
									offeredStatuses, userEmail),
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Selected.name(), userEmail, startDate,
									endDate)));
					line.append(recPref.getOfferVsSelected()).append(lineDelim);

					recPref.setJoinedVsOffer(ratio(
							roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Joined.name(), userEmail, startDate,
									endDate),
							roundCandidateService.countByPositionCodeAndStatusInAndSourcedBy(position.getPositionCode(),
									offeredStatuses, userEmail)));
					line.append(recPref.getJoinedVsOffer()).append(lineDelim);

					logger.error("setJoinedVsPosns : "
							+ roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Joined.name(), userEmail, startDate,
									endDate)
							+ " / " + positionService.getTotalOpenPositionByPositionCodes(lstPositioncode));

					recPref.setJoinedVsPosns(
							ratio(roundCandidateService.getCountByPositionCodeAndStatusAndOwnerAndDateRange(
									position.getPositionCode(), BoardStatus.Joined.name(), userEmail, startDate,
									endDate), positionService.getTotalOpenPositionByPositionCodes(lstPositioncode)));
					line.append(recPref.getJoinedVsPosns());
					csvLines.add(line.toString());
					reports.add(recPref);
				}
			}
		}
		return csvLines;

	}

	// @Teamware-RecruiterPerformanceReport - New (Original)
	private List<String> getRecPrefDataOriginal(String level1, String level2, String level3, String level4, Team team,
			List<RecPrefDTO> reports, List<String> csvLines, Date startDate, Date endDate) {
		final String lineDelim = "|";

		// List<Position> positions =
		// positionService.getPositionByTeamAndStatusActive(team);
		List<Long> teamIds = new ArrayList<>();
		List<String> positionNames = new ArrayList<>();

		teamIds.add(team.getId());
		List<String> positionCodes = positionService.getPositionByTeamAndStatusClosedAndDateRangeNativeQuery(team,
				startDate, endDate);
		if (null == positionCodes || positionCodes.isEmpty()) {
			return csvLines;
		}

		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();

		if (team.getMembers() != null && !team.getMembers().isEmpty()) {
			for (TeamMember member : team.getMembers()) {
				String userEmail = member.getUser().getEmail();

				positionCodes = positionService.getPositionByTeamAndStatusClosedAndDateRangeNativeQuery(team, startDate,
						endDate);

				if (null == positionCodes || positionCodes.isEmpty()) {
					continue;
				}

				positionNames = positionService.getPositionNamesfromPositionCodes(positionCodes);

				// for (Position position : positions) {
				StringBuilder line = new StringBuilder();
				// String clientName =
				// positionService.getClientNameFromPcode(positionCode);
				// Board board = position.getBoard();
				RecPrefDTO recPref = new RecPrefDTO();
				recPref.setSbu(level1);
				line.append(level1).append(lineDelim);

				recPref.setDm(level2);
				line.append(level2).append(lineDelim);

				recPref.setDl(level3);
				line.append(level3).append(lineDelim);

				recPref.setTeam(level4);
				line.append(level4).append(lineDelim);

				recPref.setRecruiter(member.getUser().getName() + "/" + userEmail);
				line.append(recPref.getRecruiter()).append(lineDelim);

				// Number of Positions
				recPref.setReqs(positionCodes.size());
				line.append(recPref.getReqs()).append(lineDelim);

				// Number of Openings for these positions
				recPref.setPosns(positionService.getTotalOpenPositionByPositionCodes(positionCodes));
				line.append(recPref.getPosns()).append(lineDelim);

				// Number of CV sourced by the recruiter to these positions
				recPref.setCvParsed(roundCandidateService.getCountByPositionCodesAndSourcedByAndDateRange(positionCodes,
						userEmail, startDate, endDate));
				line.append(recPref.getCvParsed()).append(lineDelim);

				// Number of profiles in clearedbyL1 status
				recPref.setCvClearedbyL1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL1()).append(lineDelim);

				// Number of profiles in clearedbyL2 status
				recPref.setCvClearedbyL2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2()).append(lineDelim);

				// Number of profiles in ClearedByL2Awaiting status
				recPref.setCvClearedbyL2Awaiting(
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								TeamwareConstants.CvClearedByL2Awaiting, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2Awaiting()).append(lineDelim);

				// Number of profiles in TechCleared status
				recPref.setCvTechCleared(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvsTechCleared, userEmail, startDate, endDate));
				line.append(recPref.getCvTechCleared()).append(lineDelim);

				// Number of profiles in PresubmissionRejected status
				recPref.setPreSubmReject(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.PreSubmisionRejected, userEmail, startDate, endDate));
				line.append(recPref.getPreSubmReject()).append(lineDelim);

				// CVs sent to client
				//				Long count = 0L;
				//				for (String positionCode : positionCodes) {
				//					Position position = positionService.getOneByPositionCode(positionCode);
				//					count = count + feedbackService.getForwardProfileCountForPositionByUserAndDateRange(
				//							position.getTitle(), position.getClient().getClientName(), userEmail, startDate, endDate);
				//				}
				//				recPref.setCvForwarded(count);
				//				line.append(recPref.getCvForwarded()).append(lineDelim);

				// CVs sent to client
				recPref.setCvForwarded(roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(
						positionCodes, cvSenttoClient, userEmail, startDate, endDate));
				line.append(recPref.getCvForwarded()).append(lineDelim);

				// Number of profiles in AwaitingCVUpdate status
				recPref.setAwCvUpd(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingCvUpdate, userEmail, startDate, endDate));
				line.append(recPref.getAwCvUpd()).append(lineDelim);

				// Number of profiles in Interview1 status
				recPref.setInt1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview1, userEmail, startDate, endDate));
				line.append(recPref.getInt1()).append(lineDelim);

				// Number of profiles in Interview2 status
				recPref.setInt2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview2, userEmail, startDate, endDate));
				line.append(recPref.getInt2()).append(lineDelim);

				// Number of profiles in Interview3 status
				recPref.setInt3(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview3, userEmail, startDate, endDate));
				line.append(recPref.getInt3()).append(lineDelim);

				// Number of profiles in Interview4 status
				recPref.setInt4(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview4, userEmail, startDate, endDate));
				line.append(recPref.getInt4()).append(lineDelim);

				// Number of profiles in FinalInterview status
				recPref.setFinalInt(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.FinalRound, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				// Number of profiles in AwaitingOffer status
				recPref.setAwOff(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingOffer, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				// Number of profiles in AwaitingOfferAcceptance status
				recPref.setAwOffAcc(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingOfferAcceptance, userEmail, startDate, endDate));
				line.append(recPref.getAwOffAcc()).append(lineDelim);

				// Number of profiles in AwaitingJoining status
				recPref.setAwJng(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingJoining, userEmail, startDate, endDate));
				line.append(recPref.getAwJng()).append(lineDelim);

				// Number of profiles in Joined status
				recPref.setJoined(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.Joined.name(), userEmail, startDate, endDate));
				line.append(recPref.getJoined()).append(lineDelim);

				// Number of profiles in Rejected status
				recPref.setCvRejected(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.Rejected.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvRejected()).append(lineDelim);

				// Number of profiles in OnHold status
				recPref.setCvOnHold(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.OnHold.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvOnHold()).append(lineDelim);

				// Number of profiles in Duplicate status
				recPref.setCvDuplicate(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.Duplicate.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvDuplicate()).append(lineDelim);

				// Number of profiles in Interview1Reject status
				recPref.setRejLvl1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview1Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl1()).append(lineDelim);

				// Number of profiles in Interview2Reject status
				recPref.setRejLvl2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview2Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl2()).append(lineDelim);

				// Number of profiles in Interview3Reject status
				recPref.setRejLvl3(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview3Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl3()).append(lineDelim);

				// Number of profiles in Interview4Reject status
				recPref.setRejLvl4(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview4Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl4()).append(lineDelim);

				// Number of profiles in FinalRoundReject status
				recPref.setRejLvlFinal(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.FinalRoundReject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvlFinal()).append(lineDelim);

				// Number of profiles in InterviewNoShow status
				recPref.setIntNoShow(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.InterviewNoShow, userEmail, startDate, endDate));
				line.append(recPref.getIntNoShow()).append(lineDelim);

				// Number of profiles in DroppedbyUS status
				recPref.setCndDropByUs(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.DroppedByUs, userEmail, startDate, endDate));
				line.append(recPref.getCndDropByUs()).append(lineDelim);

				// Number of profiles in OfferOnHold status
				recPref.setOfrOnHold(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.OfferOnHold, userEmail, startDate, endDate));
				line.append(recPref.getOfrOnHold()).append(lineDelim);

				// Number of profiles in JoineeNoShow status
				recPref.setJoinNoShow(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.JoineeNoShow, userEmail, startDate, endDate));
				line.append(recPref.getJoinNoShow()).append(lineDelim);

				// Ratio

				// L1/l2
				recPref.setL2Vsl1(ratio(
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate),
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate)));
				line.append(recPref.getL2Vsl1()).append(lineDelim);

				// L1/l2
				recPref.setCvsSubVsl1(ratio(recPref.getCvForwarded(),
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate)));
				line.append(recPref.getCvsSubVsl1()).append(lineDelim);

				// Joined / CV Sent to Client
				recPref.setJoinedVscvSub(
						ratio(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								BoardStatus.Joined.name(), userEmail, startDate, endDate), recPref.getCvForwarded()));
				line.append(recPref.getJoinedVscvSub()).append(lineDelim);

				// CVs sent to Client / Number of Positions
				recPref.setCvSubVsPosns(ratio(recPref.getCvForwarded(), recPref.getPosns()));
				line.append(recPref.getCvSubVsPosns()).append(lineDelim);

				// Interview1 / CVs Sourced
				recPref.setInt1vsSourced(ratio(recPref.getInt1(), recPref.getCvParsed()));
				line.append(recPref.getInt1vsSourced()).append(lineDelim);

				// Selected / Scheduled
				recPref.setSelectedVsScheduled(ratio(
						roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(positionCodes,
								selectStatuses, userEmail, startDate, endDate),
						roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(positionCodes,
								interviewScheduled, userEmail, startDate, endDate)));
				line.append(recPref.getSelectedVsScheduled()).append(lineDelim);

				// Select / Offered
				recPref.setOfferVsSelected(ratio(
						roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(positionCodes,
								offeredStatuses, userEmail, startDate, endDate),
						roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(positionCodes,
								selectStatuses, userEmail, startDate, endDate)));
				line.append(recPref.getOfferVsSelected()).append(lineDelim);

				// Joined / Offered
				recPref.setJoinedVsOffer(ratio(
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								BoardStatus.Joined.name(), userEmail, startDate, endDate),
						roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(positionCodes,
								offeredStatuses, userEmail, startDate, endDate)));
				line.append(recPref.getJoinedVsOffer()).append(lineDelim);

				// Joined / Positions
				recPref.setJoinedVsPosns(ratio(
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								BoardStatus.Joined.name(), userEmail, startDate, endDate),
						positionService.getTotalOpenPositionByPositionCodes(positionCodes)));
				line.append(recPref.getJoinedVsPosns());

				csvLines.add(line.toString());
				reports.add(recPref);

			}
		}
		return csvLines;
	}

	// @Teamware-RecruiterPerformanceReport - Modified
	private List<String> getRecPrefData(String level1, String level2, String level3, String level4, Team team,
			List<RecPrefDTO> reports, List<String> csvLines, Date startDate, Date endDate) {
		final String lineDelim = "|";

		// List<Position> positions =
		// positionService.getPositionByTeamAndStatusActive(team);
		List<Long> teamIds = new ArrayList<>();
		List<String> positionNames = new ArrayList<>();

		teamIds.add(team.getId());
		List<Long> positionIds = positionService.getPositionIdsByTeamAndStatusClosedAndDateRange(team, startDate, endDate);
		if (null == positionIds || positionIds.isEmpty()) {
			return csvLines;
		}

		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();

		if (team.getMembers() != null && !team.getMembers().isEmpty()) {
			for (TeamMember member : team.getMembers()) {
				String userEmail = member.getUser().getEmail();

				positionIds = positionService.getPositionIdsByTeamAndStatusClosedAndDateRange(team, startDate,
						endDate);

				if (null == positionIds || positionIds.isEmpty()) {
					continue;
				}

				List<String> positionCodes = positionService.getPositionCodesforPostionIds (positionIds);


				positionNames = positionService.getPositionNamesfromPositionIDs(positionIds);

				// for (Position position : positions) {
				StringBuilder line = new StringBuilder();
				// String clientName =
				// positionService.getClientNameFromPcode(positionCode);
				// Board board = position.getBoard();
				RecPrefDTO recPref = new RecPrefDTO();
				recPref.setSbu(level1);
				line.append(level1).append(lineDelim);

				recPref.setDm(level2);
				line.append(level2).append(lineDelim);

				recPref.setDl(level3);
				line.append(level3).append(lineDelim);

				recPref.setTeam(level4);
				line.append(level4).append(lineDelim);

				recPref.setRecruiter(member.getUser().getName() + "/" + userEmail);
				line.append(recPref.getRecruiter()).append(lineDelim);

				// Number of Positions
				recPref.setReqs(positionIds.size());
				line.append(recPref.getReqs()).append(lineDelim);

				// Number of Openings for these positions
				recPref.setPosns(positionService.getTotalOpenPositionByPositionIDs(positionIds));
				line.append(recPref.getPosns()).append(lineDelim);

				// Number of CV sourced by the recruiter to these positions
				recPref.setCvParsed(roundCandidateService.getCountByPositionCodesAndSourcedByAndDateRange(positionCodes,
						userEmail, startDate, endDate));
				line.append(recPref.getCvParsed()).append(lineDelim);

				// Number of profiles in clearedbyL1 status
				recPref.setCvClearedbyL1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL1()).append(lineDelim);

				// Number of profiles in clearedbyL2 status
				recPref.setCvClearedbyL2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2()).append(lineDelim);

				// Number of profiles in ClearedByL2Awaiting status
				recPref.setCvClearedbyL2Awaiting(
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								TeamwareConstants.CvClearedByL2Awaiting, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2Awaiting()).append(lineDelim);

				// Number of profiles in TechCleared status
				recPref.setCvTechCleared(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvsTechCleared, userEmail, startDate, endDate));
				line.append(recPref.getCvTechCleared()).append(lineDelim);

				// Number of profiles in PresubmissionRejected status
				recPref.setPreSubmReject(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.PreSubmisionRejected, userEmail, startDate, endDate));
				line.append(recPref.getPreSubmReject()).append(lineDelim);

				// CVs sent to client
				//				Long count = 0L;
				//				for (String positionCode : positionCodes) {
				//					Position position = positionService.getOneByPositionCode(positionCode);
				//					count = count + feedbackService.getForwardProfileCountForPositionByUserAndDateRange(
				//							position.getTitle(), position.getClient().getClientName(), userEmail, startDate, endDate);
				//				}
				//				recPref.setCvForwarded(count);
				//				line.append(recPref.getCvForwarded()).append(lineDelim);

				// CVs sent to client
				recPref.setCvForwarded(positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(
						positionIds, cvSenttoClient, userEmail, startDate, endDate));
				line.append(recPref.getCvForwarded()).append(lineDelim);

				// Number of profiles in AwaitingCVUpdate status
				recPref.setAwCvUpd(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingCvUpdate, userEmail, startDate, endDate));
				line.append(recPref.getAwCvUpd()).append(lineDelim);

				// Number of profiles in Interview1 status
				recPref.setInt1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview1, userEmail, startDate, endDate));
				line.append(recPref.getInt1()).append(lineDelim);

				// Number of profiles in Interview2 status
				recPref.setInt2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview2, userEmail, startDate, endDate));
				line.append(recPref.getInt2()).append(lineDelim);

				// Number of profiles in Interview3 status
				recPref.setInt3(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview3, userEmail, startDate, endDate));
				line.append(recPref.getInt3()).append(lineDelim);

				// Number of profiles in Interview4 status
				recPref.setInt4(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview4, userEmail, startDate, endDate));
				line.append(recPref.getInt4()).append(lineDelim);

				// Number of profiles in FinalInterview status
				recPref.setFinalInt(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.FinalRound, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				// Number of profiles in AwaitingOffer status
				recPref.setAwOff(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingOffer, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				// Number of profiles in AwaitingOfferAcceptance status
				recPref.setAwOffAcc(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingOfferAcceptance, userEmail, startDate, endDate));
				line.append(recPref.getAwOffAcc()).append(lineDelim);

				// Number of profiles in AwaitingJoining status
				recPref.setAwJng(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingJoining, userEmail, startDate, endDate));
				line.append(recPref.getAwJng()).append(lineDelim);

				// Number of profiles in Joined status
				recPref.setJoined(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.Joined.name(), userEmail, startDate, endDate));
				line.append(recPref.getJoined()).append(lineDelim);

				// Number of profiles in Rejected status
				recPref.setCvRejected(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.Rejected.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvRejected()).append(lineDelim);

				// Number of profiles in OnHold status
				recPref.setCvOnHold(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.OnHold.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvOnHold()).append(lineDelim);

				// Number of profiles in Duplicate status
				recPref.setCvDuplicate(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.Duplicate.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvDuplicate()).append(lineDelim);

				// Number of profiles in Interview1Reject status
				recPref.setRejLvl1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview1Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl1()).append(lineDelim);

				// Number of profiles in Interview2Reject status
				recPref.setRejLvl2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview2Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl2()).append(lineDelim);

				// Number of profiles in Interview3Reject status
				recPref.setRejLvl3(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview3Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl3()).append(lineDelim);

				// Number of profiles in Interview4Reject status
				recPref.setRejLvl4(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview4Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl4()).append(lineDelim);

				// Number of profiles in FinalRoundReject status
				recPref.setRejLvlFinal(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.FinalRoundReject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvlFinal()).append(lineDelim);

				// Number of profiles in InterviewNoShow status
				recPref.setIntNoShow(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.InterviewNoShow, userEmail, startDate, endDate));
				line.append(recPref.getIntNoShow()).append(lineDelim);

				// Number of profiles in DroppedbyUS status
				recPref.setCndDropByUs(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.DroppedByUs, userEmail, startDate, endDate));
				line.append(recPref.getCndDropByUs()).append(lineDelim);

				// Number of profiles in OfferOnHold status
				recPref.setOfrOnHold(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.OfferOnHold, userEmail, startDate, endDate));
				line.append(recPref.getOfrOnHold()).append(lineDelim);

				// Number of profiles in JoineeNoShow status
				recPref.setJoinNoShow(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.JoineeNoShow, userEmail, startDate, endDate));
				line.append(recPref.getJoinNoShow()).append(lineDelim);

				// Ratio

				// L1/l2
				recPref.setL2Vsl1(ratio(
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate),
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate)));
				line.append(recPref.getL2Vsl1()).append(lineDelim);

				// L1/l2
				recPref.setCvsSubVsl1(ratio(recPref.getCvForwarded(),
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate)));
				line.append(recPref.getCvsSubVsl1()).append(lineDelim);

				// Joined / CV Sent to Client
				recPref.setJoinedVscvSub(
						ratio(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								BoardStatus.Joined.name(), userEmail, startDate, endDate), recPref.getCvForwarded()));
				line.append(recPref.getJoinedVscvSub()).append(lineDelim);

				// CVs sent to Client / Number of Positions
				recPref.setCvSubVsPosns(ratio(recPref.getCvForwarded(), recPref.getPosns()));
				line.append(recPref.getCvSubVsPosns()).append(lineDelim);

				// Interview1 / CVs Sourced
				recPref.setInt1vsSourced(ratio(recPref.getInt1(), recPref.getCvParsed()));
				line.append(recPref.getInt1vsSourced()).append(lineDelim);

				// Selected / Scheduled
				recPref.setSelectedVsScheduled(ratio(
						positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIds,
								selectStatuses, userEmail, startDate, endDate),
						positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIds,
								interviewScheduled, userEmail, startDate, endDate)));
				line.append(recPref.getSelectedVsScheduled()).append(lineDelim);

				// Select / Offered
				recPref.setOfferVsSelected(ratio(
						positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIds,
								offeredStatuses, userEmail, startDate, endDate),
						positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIds,
								selectStatuses, userEmail, startDate, endDate)));
				line.append(recPref.getOfferVsSelected()).append(lineDelim);

				// Joined / Offered
				recPref.setJoinedVsOffer(ratio(
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								BoardStatus.Joined.name(), userEmail, startDate, endDate),
						positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(positionIds,
								offeredStatuses, userEmail, startDate, endDate)));
				line.append(recPref.getJoinedVsOffer()).append(lineDelim);

				// Joined / Positions
				recPref.setJoinedVsPosns(ratio(
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								BoardStatus.Joined.name(), userEmail, startDate, endDate),
						positionService.getTotalOpenPositionByPositionIDs(positionIds)));
				line.append(recPref.getJoinedVsPosns());

				csvLines.add(line.toString());
				reports.add(recPref);

			}
		}
		return csvLines;
	}

	public Double ratio(Object obj1, Object obj2) {
		if (obj1 instanceof Long && obj2 instanceof Long && (Long) obj2 != 0L) {
			logger.error(((Long) obj1).doubleValue() + " / " + ((Long) obj2).doubleValue());
			Double result = ((Long) obj1).doubleValue() / ((Long) obj2).doubleValue();
			if (result != null && result != 0D)
				return Math.round(result * 100.0) / 100.0;
		}
		return 0D;
	}

	private String addRecPrefHeader(List<String> csvLines) {
		final String lineDelim = "|";

		StringBuilder headerLine = new StringBuilder();
		headerLine.append("SBU Head").append(lineDelim).append("SBU").append(lineDelim).append("DM").append(lineDelim)
		.append("DL/Team").append(lineDelim).append("Recruiter Name").append(lineDelim).append("# Requirements")
		.append(lineDelim).append("# Positions").append(lineDelim).append("# CVs Parsed").append(lineDelim)
		.append("# CVs Cleared by Recr (L1)").append(lineDelim).append("# CVs Cleared by Scr/DL (L2)")
		.append(lineDelim).append("# CVs Awaiting L2 Clearance").append(lineDelim).append("# CVs Tech cleared")
		.append(lineDelim).append("# CVs Tech cleared").append(lineDelim).append("# of Client Subm, forwarded")
		.append(lineDelim).append("# Aw CV Upd").append(lineDelim).append("# Interview 1").append(lineDelim)
		.append("# Interview 2").append(lineDelim).append("# Interview 3").append(lineDelim)
		.append("# Interview 4").append(lineDelim).append("# Final Int").append(lineDelim).append("Aw Off")
		.append(lineDelim).append("Aw Off Acc").append(lineDelim).append("Aw Jng").append(lineDelim)
		.append("Joined").append(lineDelim).append("CV Rej").append(lineDelim).append("CV on Hold")
		.append(lineDelim).append("CV Duplicate").append(lineDelim).append("1st Lvl Rej").append(lineDelim)
		.append("2nd Lvl Rej").append(lineDelim).append("3rd Lvl Rej").append(lineDelim).append("4th Lvl Rej")
		.append(lineDelim).append("Final Lvl Rej").append(lineDelim).append("Int No Show").append(lineDelim)
		.append("Cand Dropped by us").append(lineDelim).append("Offer on Hold").append(lineDelim)
		.append("Joinee No Show").append(lineDelim).append("L2 / L1").append(lineDelim).append("CVs Sub / L1")
		.append(lineDelim).append("Joined / CVs Sub").append(lineDelim).append("CVs Sub / Posn")
		.append(lineDelim).append("1st Lvl Int / CV").append(lineDelim).append("Sels / Int").append(lineDelim)
		.append("Offer / Sels").append(lineDelim).append("Joined / Offer").append(lineDelim)
		.append("Joined / Posns").append(lineDelim);

		csvLines.add(headerLine.toString());
		return lineDelim;
	}

	// @Teamware-PipelineReport - Original
	private List<String> getTeamwarePipelineReportOriginal (String level1, String level2, String level3, String level4,
			Team team, List<RecPrefDTO> reports, List<String> csvLines, Date startDate, Date endDate) {
		final String lineDelim = "|";

		// List<Position> positions =
		// positionService.getPositionByTeamAndStatusActive(team);
		List<Long> teamIds = new ArrayList<>();
		teamIds.add(team.getId());
		List<String> positionCodes = positionService.getPositionsCodeByTeamIdsAndStatusAndDateRange(teamIds,
				Status.Active.toString(), startDate, endDate);
		if (null == positionCodes || positionCodes.isEmpty()) {
			return csvLines;
		}

		List<String> rejectstatus = new ArrayList<>();

		if (null == rejectstatus || rejectstatus.isEmpty()) {
			rejectstatus.add(TeamwareConstants.CVReject);
			rejectstatus.add(BoardStatus.Rejected.name());

		}

		if (team.getMembers() != null && !team.getMembers().isEmpty()) {
			for (TeamMember member : team.getMembers()) {
				String userEmail = member.getUser().getEmail();
				positionCodes = positionService.getPositionCodesForOwnerOrHrExecutivesIn(userEmail, member.getUser(),
						startDate, endDate);
				if (null == positionCodes || positionCodes.isEmpty()) {
					continue;
				}
				// for (Position position : positions) {
				StringBuilder line = new StringBuilder();
				// String clientName =
				// positionService.getClientNameFromPcode(positionCode);
				// Board board = position.getBoard();
				RecPrefDTO recPref = new RecPrefDTO();
				recPref.setSbu(level1);
				line.append(level1).append(lineDelim);

				recPref.setDm(level2);
				line.append(level2).append(lineDelim);

				recPref.setDl(level3);
				line.append(level3).append(lineDelim);

				recPref.setTeam(level4);
				line.append(level4).append(lineDelim);

				recPref.setRecruiter(member.getUser().getName() + "/" + userEmail);
				line.append(recPref.getRecruiter()).append(lineDelim);

				recPref.setReqs(positionCodes.size());
				line.append(recPref.getReqs()).append(lineDelim);

				recPref.setPosns(positionService.getTotalOpenPositionByPositionCodes(positionCodes));
				line.append(recPref.getPosns()).append(lineDelim);

				// @sajin - Added another column to get total candidates sourced by recruiter
				recPref.setCvSourced(candidateService.getCountByOwnerAndDatebetween(userEmail, startDate, endDate));
				line.append(recPref.getCvSourced()).append(lineDelim);

				recPref.setCvParsed(roundCandidateService.getCountByPositionCodesAndSourcedByAndDateRange(positionCodes,
						userEmail, startDate, endDate));
				line.append(recPref.getCvParsed()).append(lineDelim);

				recPref.setCvClearedbyL1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL1()).append(lineDelim);

				recPref.setCvClearedbyL2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2()).append(lineDelim);

				recPref.setCvClearedbyL2Awaiting(
						roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(positionCodes,
								TeamwareConstants.CvClearedByL2Awaiting, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2Awaiting()).append(lineDelim);

				recPref.setCvTechCleared(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.CvsTechCleared, userEmail, startDate, endDate));
				line.append(recPref.getCvTechCleared()).append(lineDelim);

				recPref.setPreSubmReject(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.PreSubmisionRejected, userEmail, startDate, endDate));
				line.append(recPref.getPreSubmReject()).append(lineDelim);

				// CV Sent to client is a total
				List<String> cvSenttoClient = getCVsSenttoClient();
				recPref.setCvForwarded((roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(
						positionCodes, cvSenttoClient, userEmail, startDate, endDate)));
				line.append(recPref.getCvForwarded()).append(lineDelim);

				//				Long count = 0L;
				//				for (String positionCode : positionCodes) {
				//					Position position = positionService.getOneByPositionCode(positionCode);
				//					count = count + feedbackService.getForwardProfileCountForPositionByUserAndDateRange(
				//							position.getTitle(), position.getClient().getClientName(), userEmail, startDate, endDate);
				//				}
				//				recPref.setCvForwarded(count);
				//				line.append(recPref.getCvForwarded()).append(lineDelim);

				recPref.setAwCvUpd(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingCvUpdate, userEmail, startDate, endDate));
				line.append(recPref.getAwCvUpd()).append(lineDelim);

				// New status
				recPref.setInt1Scheduled(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview1Scheduled, userEmail, startDate, endDate));
				line.append(recPref.getInt1Scheduled()).append(lineDelim);

				recPref.setInt1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview1, userEmail, startDate, endDate));
				line.append(recPref.getInt1()).append(lineDelim);

				recPref.setInt2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview2, userEmail, startDate, endDate));
				line.append(recPref.getInt2()).append(lineDelim);

				recPref.setInt3(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview3, userEmail, startDate, endDate));
				line.append(recPref.getInt3()).append(lineDelim);

				recPref.setInt4(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview4, userEmail, startDate, endDate));
				line.append(recPref.getInt4()).append(lineDelim);

				recPref.setFinalInt(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.FinalRound, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				recPref.setAwOff(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingOffer, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				recPref.setAwOffAcc(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingOfferAcceptance, userEmail, startDate, endDate));
				line.append(recPref.getAwOffAcc()).append(lineDelim);

				recPref.setAwJng(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.AwaitingJoining, userEmail, startDate, endDate));
				line.append(recPref.getAwJng()).append(lineDelim);

				recPref.setJoined(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.Joined.name(), userEmail, startDate, endDate));
				line.append(recPref.getJoined()).append(lineDelim);

				recPref.setCvRejected(roundCandidateService.getCountByPositionCodesAndStatusesAndOwnerAndDateRange(
						positionCodes, rejectstatus, userEmail, startDate, endDate));
				line.append(recPref.getCvRejected()).append(lineDelim);

				// @Sajin - Considering 2 reject statuses for Reject count
				recPref.setCvOnHold(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.OnHold.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvOnHold()).append(lineDelim);

				recPref.setCvDuplicate(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, BoardStatus.Duplicate.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvDuplicate()).append(lineDelim);

				recPref.setRejLvl1(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview1Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl1()).append(lineDelim);

				recPref.setRejLvl2(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview2Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl2()).append(lineDelim);

				recPref.setRejLvl3(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview3Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl3()).append(lineDelim);

				recPref.setRejLvl4(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.Interview4Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl4()).append(lineDelim);

				recPref.setRejLvlFinal(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.FinalRoundReject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvlFinal()).append(lineDelim);

				// New status - Select Drop
				recPref.setSelectDrop(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.SelectDrop, userEmail, startDate, endDate));
				line.append(recPref.getSelectDrop()).append(lineDelim);

				recPref.setIntNoShow(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.InterviewNoShow, userEmail, startDate, endDate));
				line.append(recPref.getIntNoShow()).append(lineDelim);

				recPref.setCndDropByUs(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.DroppedByUs, userEmail, startDate, endDate));
				line.append(recPref.getCndDropByUs()).append(lineDelim);

				recPref.setOfrOnHold(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.OfferOnHold, userEmail, startDate, endDate));
				line.append(recPref.getOfrOnHold()).append(lineDelim);

				recPref.setJoinNoShow(roundCandidateService.getCountByPositionCodesAndStatusAndOwnerAndDateRange(
						positionCodes, TeamwareConstants.JoineeNoShow, userEmail, startDate, endDate));
				line.append(recPref.getJoinNoShow()).append(lineDelim);

				csvLines.add(line.toString());
				reports.add(recPref);
				// }
			}
		}
		return csvLines;
	}

	// @Teamware-PipelineReport - Modified
	private List<String> getTeamwarePipelineReport(String level1, String level2, String level3, String level4,
			Team team, List<RecPrefDTO> reports, List<String> csvLines, Date startDate, Date endDate) {
		final String lineDelim = "|";

		// List<Position> positions =
		// positionService.getPositionByTeamAndStatusActive(team);
		List<Long> teamIds = new ArrayList<>();
		teamIds.add(team.getId());

		//Get Active Positions based on TeamIds and Date Range
		//Date range is applied on Modification date or Creation date
		//List<String> positionCodes = positionService.getPositionsCodeByTeamIdsAndStatusAndDateRange(teamIds,
		//		Status.Active.toString(), startDate, endDate);

		//Getting ids instead of codes, since positionCandidateDataService is storing only ids and not codes.
		List<Long> positionIds = positionService.getIDsByTeamIdsAndStatusAndDateRange(teamIds,
				Status.Active.toString(), startDate, endDate);


		if (null == positionIds || positionIds.isEmpty()) {
			return csvLines;
		}

		List<String> rejectstatus = new ArrayList<>();

		if (null == rejectstatus || rejectstatus.isEmpty()) {
			rejectstatus.add(TeamwareConstants.CVReject);
			rejectstatus.add(BoardStatus.Rejected.name());

		}

		
		
		
		if (team.getMembers() != null && !team.getMembers().isEmpty()) {
			
			String teamMemeberDetails = "";
			
			for (TeamMember member : team.getMembers()) {
				teamMemeberDetails = teamMemeberDetails + " "+ member.getUser().getEmail();
			}
			
			logger.error("teamMemebers emails = "+teamMemeberDetails+"   Team Name = "+team.getTeamName());
			
			
			for (TeamMember member : team.getMembers()) {
				String userEmail = member.getUser().getEmail();
				positionIds = positionService.getActivePositionIdsForOwnerOrHrExecutivesIn(userEmail, member.getUser(),
						startDate, endDate);

				logger.error("Position Ids = "+positionIds+"  Member Mail_id = "+userEmail);
				
				if (null == positionIds || positionIds.isEmpty()) {
					continue;
				}

				List<String> positionCodes = positionService.getPositionCodesforPostionIds (positionIds);

				// for (Position position : positions) {
				StringBuilder line = new StringBuilder();
				// String clientName =
				// positionService.getClientNameFromPcode(positionCode);
				// Board board = position.getBoard();
				RecPrefDTO recPref = new RecPrefDTO();
				recPref.setSbu(level1);
				line.append(level1).append(lineDelim);

				recPref.setDm(level2);
				line.append(level2).append(lineDelim);

				recPref.setDl(level3);
				line.append(level3).append(lineDelim);

				recPref.setTeam(level4);
				line.append(level4).append(lineDelim);

				recPref.setRecruiter(member.getUser().getName() + "/" + userEmail);
				line.append(recPref.getRecruiter()).append(lineDelim);

				recPref.setReqs(positionIds.size());
				line.append(recPref.getReqs()).append(lineDelim);

				recPref.setPosns(positionService.getTotalOpenPositionByPositionIDs(positionIds));
				line.append(recPref.getPosns()).append(lineDelim);

				//Added another column to get total candidates sourced by recruiter
				recPref.setCvSourced(candidateService.getCountByOwnerAndDatebetween(userEmail, startDate, endDate));
				line.append(recPref.getCvSourced()).append(lineDelim);

				//Out of the sourced candidates, this gives the count of candidates added to the position
				recPref.setCvParsed(roundCandidateService.getCountByPositionCodesAndSourcedByAndDateRange(positionCodes,
						userEmail, startDate, endDate));
				line.append(recPref.getCvParsed()).append(lineDelim);


				///////////////////////
				//Find the data from PositionCandidateData Repo

				recPref.setCvClearedbyL1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvClearedByL1, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL1()).append(lineDelim);

				logger.error("Teamware - Pipeline Report" +TeamwareConstants.CvClearedByL1 +" " +recPref.getCvClearedbyL1());


				recPref.setCvClearedbyL2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvClearedByL2, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2()).append(lineDelim);

				logger.error("Teamware - Pipeline Report" +TeamwareConstants.CvClearedByL1 +" " +recPref.getCvClearedbyL2());

				recPref.setCvClearedbyL2Awaiting(
						positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(positionIds,
								TeamwareConstants.CvClearedByL2Awaiting, userEmail, startDate, endDate));
				line.append(recPref.getCvClearedbyL2Awaiting()).append(lineDelim);

				recPref.setCvTechCleared(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.CvsTechCleared, userEmail, startDate, endDate));
				line.append(recPref.getCvTechCleared()).append(lineDelim);

				recPref.setPreSubmReject(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.PreSubmisionRejected, userEmail, startDate, endDate));
				line.append(recPref.getPreSubmReject()).append(lineDelim);

				// CV Sent to client is a total
				List<String> cvSenttoClient = getCVsSenttoClient();
				recPref.setCvForwarded((positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(
						positionIds, cvSenttoClient, userEmail, startDate, endDate)));
				line.append(recPref.getCvForwarded()).append(lineDelim);

				//				Long count = 0L;
				//				for (String positionCode : positionCodes) {
				//					Position position = positionService.getOneByPositionCode(positionCode);
				//					count = count + feedbackService.getForwardProfileCountForPositionByUserAndDateRange(
				//							position.getTitle(), position.getClient().getClientName(), userEmail, startDate, endDate);
				//				}
				//				recPref.setCvForwarded(count);
				//				line.append(recPref.getCvForwarded()).append(lineDelim);

				recPref.setAwCvUpd(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingCvUpdate, userEmail, startDate, endDate));
				line.append(recPref.getAwCvUpd()).append(lineDelim);

				// New status
				recPref.setInt1Scheduled(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview1Scheduled, userEmail, startDate, endDate));
				line.append(recPref.getInt1Scheduled()).append(lineDelim);

				recPref.setInt1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview1, userEmail, startDate, endDate));
				line.append(recPref.getInt1()).append(lineDelim);

				recPref.setInt2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview2, userEmail, startDate, endDate));
				line.append(recPref.getInt2()).append(lineDelim);

				recPref.setInt3(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview3, userEmail, startDate, endDate));
				line.append(recPref.getInt3()).append(lineDelim);

				recPref.setInt4(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview4, userEmail, startDate, endDate));
				line.append(recPref.getInt4()).append(lineDelim);

				recPref.setFinalInt(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.FinalRound, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				recPref.setAwOff(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingOffer, userEmail, startDate, endDate));
				line.append(recPref.getFinalInt()).append(lineDelim);

				recPref.setAwOffAcc(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingOfferAcceptance, userEmail, startDate, endDate));
				line.append(recPref.getAwOffAcc()).append(lineDelim);

				recPref.setAwJng(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.AwaitingJoining, userEmail, startDate, endDate));
				line.append(recPref.getAwJng()).append(lineDelim);

				recPref.setJoined(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.Joined.name(), userEmail, startDate, endDate));
				line.append(recPref.getJoined()).append(lineDelim);

				recPref.setCvRejected(positionCandidateDataService.getCountByPositionIDsAndStatusesAndOwnerAndDateRange(
						positionIds, rejectstatus, userEmail, startDate, endDate));
				line.append(recPref.getCvRejected()).append(lineDelim);

				// @Sajin - Considering 2 reject statuses for Reject count
				recPref.setCvOnHold(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.OnHold.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvOnHold()).append(lineDelim);

				recPref.setCvDuplicate(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, BoardStatus.Duplicate.name(), userEmail, startDate, endDate));
				line.append(recPref.getCvDuplicate()).append(lineDelim);

				recPref.setRejLvl1(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview1Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl1()).append(lineDelim);

				recPref.setRejLvl2(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview2Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl2()).append(lineDelim);

				recPref.setRejLvl3(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview3Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl3()).append(lineDelim);

				recPref.setRejLvl4(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.Interview4Reject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvl4()).append(lineDelim);

				recPref.setRejLvlFinal(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.FinalRoundReject, userEmail, startDate, endDate));
				line.append(recPref.getRejLvlFinal()).append(lineDelim);

				// New status - Select Drop
				recPref.setSelectDrop(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.SelectDrop, userEmail, startDate, endDate));
				line.append(recPref.getSelectDrop()).append(lineDelim);

				recPref.setIntNoShow(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.InterviewNoShow, userEmail, startDate, endDate));
				line.append(recPref.getIntNoShow()).append(lineDelim);

				recPref.setCndDropByUs(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.DroppedByUs, userEmail, startDate, endDate));
				line.append(recPref.getCndDropByUs()).append(lineDelim);

				recPref.setOfrOnHold(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.OfferOnHold, userEmail, startDate, endDate));
				line.append(recPref.getOfrOnHold()).append(lineDelim);

				recPref.setJoinNoShow(positionCandidateDataService.getCountByPositionIdsAndStatusAndOwnerAndDateRange(
						positionIds, TeamwareConstants.JoineeNoShow, userEmail, startDate, endDate));
				line.append(recPref.getJoinNoShow()).append(lineDelim);

				csvLines.add(line.toString());
				reports.add(recPref);
				// }
			}
		}
		return csvLines;
	}

	private String addPipelineHeader(List<String> csvLines) {
		final String lineDelim = "|";

		StringBuilder headerLine = new StringBuilder();
		headerLine.append("SBU Head").append(lineDelim).append("SBU").append(lineDelim).append("DM").append(lineDelim)
		.append("DL/Team").append(lineDelim).append("Recruiter").append(lineDelim).append("# Req")
		.append(lineDelim).append("# Posns").append(lineDelim).append("# CVs Sourced").append(lineDelim)
		.append("# CVs Parsed").append(lineDelim).append("# CVs Cleared by Recr (L1)").append(lineDelim)
		.append("# CVs Cleared by Scr/DL (L2)").append(lineDelim).append("# CVs Awaiting L2 Clearance")
		.append(lineDelim).append("# CVs Tech cleared").append(lineDelim).append("Pre Subm Reject")
		.append(lineDelim).append("# of Client Subm").append(lineDelim).append("Aw CV Upd").append(lineDelim)
		.append("Int-1 Scheduled").append(lineDelim).append("Int-1 FBP").append(lineDelim).append("Int-2")
		.append(lineDelim).append("Int-3").append(lineDelim).append("Int-4").append(lineDelim)
		.append("Final Int").append(lineDelim).append("Aw Off").append(lineDelim).append("Aw Off Acc")
		.append(lineDelim).append("Aw Jng").append(lineDelim).append("Joined").append(lineDelim)
		.append("CV Rej").append(lineDelim).append("CV on Hold").append(lineDelim).append("CV Duplicate")
		.append(lineDelim).append("1st Lvl Rej").append(lineDelim).append("2nd Lvl Rej").append(lineDelim)
		.append("3rd Lvl Rej").append(lineDelim).append("4th Lvl Rej").append(lineDelim).append("Final Lvl Rej")
		.append(lineDelim).append("Select drop").append(lineDelim).append("Int No Show").append(lineDelim)
		.append("Cand Dropped by us").append(lineDelim).append("Offer on Hold").append(lineDelim)
		.append("Joinee No Show").append(lineDelim);

		csvLines.add(headerLine.toString());
		return lineDelim;
	}

	// @Teamware- BizAnalysis - Original
	private List<String> getBizAnalysisOriginal(String level1, String level2, String level3, String level4, Team team,
			List<RecPrefDTO> reports, List<String> csvLines, List<Long> clients, String location, String vertical,
			Date startDate, Date endDate) throws RecruizException {

		final String lineDelim = "|";

		List<String> pcodes = positionService.getPositionCodeByTeamAndVerticalAndLocationAndClients(team, vertical,
				location, clients, startDate, endDate);

		if (null == pcodes || pcodes.isEmpty()) {
			return csvLines;
		}

		//		List<String> offeredStatuses = new ArrayList<>();
		//		offeredStatuses.add(BoardStatus.Offered.name());
		//		offeredStatuses.add(TeamwareConstants.AwaitingOfferAcceptance);
		//
		//		List<String> joinedStatuses = new ArrayList<>();
		//		joinedStatuses.add(BoardStatus.Joined.name());
		//		joinedStatuses.add(BoardStatus.Employee.name());
		//
		//		List<String> rejectstatus = new ArrayList<>();
		//
		//		if (null == rejectstatus || rejectstatus.isEmpty()) {
		//			rejectstatus.add(TeamwareConstants.CVReject);
		//			rejectstatus.add(BoardStatus.Rejected.name());
		//
		//		}

		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();
		List<String> rejectstatus = getrejectStatuses();
		List<String> joinedStatuses = getjoinedStatuses();

		for (String pcode : pcodes) {
			String clientName = positionService.getClientNameFromPositioncode(pcode);
			StringBuilder line = new StringBuilder();
			Position position = positionService.getPositionByCode(pcode);
			String positionstatus = position.getStatus();
			String positiontitle = position.getTitle();

			List<String> lstPositioncode = new ArrayList<>();
			lstPositioncode.add(pcode);

			// Board board = position.getBoard();
			RecPrefDTO recPref = new RecPrefDTO();
			recPref.setSbu(level1);
			line.append(level1).append(lineDelim);

			recPref.setDm(level2);
			line.append(level2).append(lineDelim);

			recPref.setDl(level3);
			line.append(level3).append(lineDelim);

			recPref.setTeam(level4);
			line.append(level4).append(lineDelim);

			// Client
			recPref.setClient(clientName);
			line.append(recPref.getClient()).append(lineDelim);

			// Position Title
			recPref.setReqs(positiontitle);
			line.append(recPref.getReqs()).append(lineDelim);

			//Position Status
			recPref.setStatus(positionstatus);
			line.append(recPref.getStatus()).append(lineDelim);

			// No of Positions
			recPref.setPosns(position.getTotalPosition() + "");
			line.append(recPref.getPosns()).append(lineDelim);

			// Req ID
			recPref.setReqId(position.getRequisitionId() == null ? "NA" : position.getRequisitionId());
			line.append(recPref.getReqId()).append(lineDelim);

			// Vertical
			recPref.setVertical(position.getVerticalCluster());
			line.append(recPref.getVertical()).append(lineDelim);

			// Requirement Type (Custom Field)
			recPref.setRequirementType(position.getCustomField().get("Requirement Type") == null ? "NA"
					: position.getCustomField().get("Requirement Type"));
			line.append(recPref.getRequirementType()).append(lineDelim);

			// Location
			String str_location = "";
			if (position.getLocation() != null || !position.getLocation().isEmpty()) {
				str_location = position.getLocation().toString();
				str_location = str_location.replace('|', '/');

			}

			recPref.setLocation(position.getLocation());
			line.append(str_location).append(lineDelim);

			// System.out.println("Position: " +positiontitle +" Location: " +str_location
			// +" Vertical:" +recPref.getVertical());

			// Creation Date
			recPref.setCreationDate(DateUtil.formateDate(position.getCreationDate()));
			line.append(recPref.getCreationDate()).append(lineDelim);

			recPref.setExp(position.getExperienceRange() == null ? "NA" : position.getExperienceRange());
			line.append(recPref.getExp()).append(lineDelim);

			// recPref.setJobLocation(position.getLocation());
			// line.append(recPref.getJobLocation()).append(lineDelim);

			recPref.setEndClient(position.getEndClient() == null ? "NA" : position.getEndClient());
			line.append(recPref.getEndClient()).append(lineDelim);

			//Hiring Manager
			recPref.setHiringManager(position.getHiringManager() == null ? "NA" : position.getHiringManager());
			line.append(recPref.getHiringManager()).append(lineDelim);

			//Internal SPOC
			recPref.setSpoc(position.getSpoc() == null ? "NA" : position.getSpoc());
			line.append(recPref.getSpoc()).append(lineDelim);

			// @sajin - Removed Recruiz default position type. Using only custom Requirement
			// type
			// recPref.setReqType(position.getType() == null ? "NA" : position.getType());
			// line.append(recPref.getReqType()).append(lineDelim);

			recPref.setCvForwarded(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
					cvSenttoClient, startDate, endDate));
			line.append(recPref.getCvForwarded()).append(lineDelim);

			recPref.setAwCvUpd(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.AwaitingCvUpdate, startDate, endDate) + "");
			line.append(recPref.getAwCvUpd()).append(lineDelim);

			// New status
			recPref.setInt1Scheduled(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview1Scheduled, startDate, endDate));
			line.append(recPref.getInt1Scheduled()).append(lineDelim);

			recPref.setInt1(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview1, startDate, endDate) + "");
			line.append(recPref.getInt1()).append(lineDelim);

			recPref.setInt2(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview2, startDate, endDate) + "");
			line.append(recPref.getInt2()).append(lineDelim);

			recPref.setInt3(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview3, startDate, endDate) + "");
			line.append(recPref.getInt3()).append(lineDelim);

			recPref.setInt4(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview4, startDate, endDate) + "");
			line.append(recPref.getInt4()).append(lineDelim);

			recPref.setFinalInt(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.FinalRound, startDate, endDate) + "");
			line.append(recPref.getFinalInt()).append(lineDelim);

			recPref.setAwOff(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.AwaitingOffer, startDate, endDate) + "");
			line.append(recPref.getFinalInt()).append(lineDelim);

			recPref.setAwOffAcc(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.AwaitingOfferAcceptance, startDate, endDate) + "");
			line.append(recPref.getAwOffAcc()).append(lineDelim);

			recPref.setAwJng(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.AwaitingJoining, startDate, endDate) + "");
			line.append(recPref.getAwJng()).append(lineDelim);

			recPref.setJoined(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					BoardStatus.Joined.name(), startDate, endDate) + "");
			line.append(recPref.getJoined()).append(lineDelim);

			//			recPref.setCvRejected(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
			//					BoardStatus.Rejected.name(), startDate, endDate) + "");

			// @Sajin - Considering 2 reject statuses for Reject count
			recPref.setCvRejected(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode, rejectstatus,
					startDate, endDate));

			line.append(recPref.getCvRejected()).append(lineDelim);

			recPref.setCvOnHold(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					BoardStatus.OnHold.name(), startDate, endDate) + "");
			line.append(recPref.getCvOnHold()).append(lineDelim);

			recPref.setCvDuplicate(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					BoardStatus.Duplicate.name(), startDate, endDate) + "");
			line.append(recPref.getCvDuplicate()).append(lineDelim);

			recPref.setRejLvl1(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview1Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl1()).append(lineDelim);

			recPref.setRejLvl2(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview2Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl2()).append(lineDelim);

			recPref.setRejLvl3(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview3Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl3()).append(lineDelim);

			recPref.setRejLvl4(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.Interview4Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl4()).append(lineDelim);

			recPref.setRejLvlFinal(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.FinalRoundReject, startDate, endDate) + "");
			line.append(recPref.getRejLvlFinal()).append(lineDelim);

			// New status - Select Drop
			recPref.setSelectDrop(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.SelectDrop, startDate, endDate));
			line.append(recPref.getSelectDrop()).append(lineDelim);

			recPref.setIntNoShow(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.InterviewNoShow, startDate, endDate) + "");
			line.append(recPref.getIntNoShow()).append(lineDelim);

			recPref.setCndDropByUs(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.DroppedByUs, startDate, endDate) + "");
			line.append(recPref.getCndDropByUs()).append(lineDelim);

			recPref.setOfrOnHold(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.OfferOnHold, startDate, endDate) + "");
			line.append(recPref.getOfrOnHold()).append(lineDelim);

			recPref.setJoinNoShow(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
					TeamwareConstants.JoineeNoShow, startDate, endDate) + "");
			line.append(recPref.getJoinNoShow()).append(lineDelim);

			// Ratios
			recPref.setJoinedVscvSub(ratio(
					roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode, BoardStatus.Joined.name(),
							startDate, endDate),
					feedbackService.getForwardProfileCountForPositionAndDateRange(positiontitle, clientName, startDate,
							endDate)));
			line.append(recPref.getJoinedVscvSub()).append(lineDelim);

			Long positioncount = 0L;
			String str_poscount = (String) recPref.getPosns();
			if (str_poscount != null) {
				positioncount = Long.parseLong(str_poscount);
			}

			//CVs forwarded / Number of Openings
			recPref.setCvSubVsPosns(ratio(recPref.getCvForwarded(),
					positionService.getTotalOpenPositionByPositionCodes(lstPositioncode)));
			line.append(recPref.getCvSubVsPosns()).append(lineDelim);

			//Interview 1 / Number of CVs added to position
			recPref.setInt1vsSourced(ratio(
					roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
							TeamwareConstants.Interview1, startDate, endDate),
					roundCandidateService.getCountByPositionCodeAndDateRange(pcode, startDate, endDate)));
			line.append(recPref.getInt1vsSourced()).append(lineDelim);

			// // this data is not available
			// recPref.setIntAttndVsScheduled("0");
			// line.append(recPref.getIntAttndVsScheduled()).append(lineDelim);

			//Select / InterviewScheduled
			recPref.setSelectedVsScheduled(ratio(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
					selectStatuses, startDate, endDate),
					roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
							interviewScheduled, startDate, endDate)));
			line.append(recPref.getSelectedVsScheduled()).append(lineDelim);

			//Offered / Selected
			recPref.setOfferVsSelected(ratio(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
					offeredStatuses, startDate, endDate),
					roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
							selectStatuses, startDate, endDate)));
			line.append(recPref.getOfferVsSelected()).append(lineDelim);

			//Joined / Offered
			recPref.setJoinedVsOffer(ratio(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
					joinedStatuses, startDate, endDate),
					roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
							offeredStatuses, startDate, endDate)));
			line.append(recPref.getJoinedVsOffer()).append(lineDelim);

			//Joined / Total Openings
			recPref.setJoinedVsPosns(
					ratio(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
							joinedStatuses, startDate, endDate), positionService.getTotalOpenPositionByPositionCodes(lstPositioncode)));
			line.append(recPref.getJoinedVsPosns());

			csvLines.add(line.toString());
			reports.add(recPref);
		}

		return csvLines;
	}

	// @Teamware- BizAnalysis - Modified
	private List<String> getBizAnalysis(String level1, String level2, String level3, String level4, Team team,
			List<RecPrefDTO> reports, List<String> csvLines, List<Long> clients, String location, String vertical,
			Date startDate, Date endDate) throws RecruizException {

		final String lineDelim = "|";

		//List<String> pcodes = positionService.getPositionCodeByTeamAndVerticalAndLocationAndClients(team, vertical,
		//		location, clients, startDate, endDate);

		List<BigInteger> positionIDs = positionService.getPositionIDsByTeamAndVerticalAndLocationAndClients(team, vertical,
				location, clients, startDate, endDate);

		if (null == positionIDs || positionIDs.isEmpty()) {
			return csvLines;
		}


		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();
		List<String> rejectstatus = getrejectStatuses();
		List<String> joinedStatuses = getjoinedStatuses();

		for (BigInteger pid : positionIDs) {
			String clientName = positionService.getClientNameFromPositionId(pid);
			StringBuilder line = new StringBuilder();
			long longValuePid = pid.longValue();
			Position position = positionService.getOneByPositionID(longValuePid);
			String positionstatus = position.getStatus();
			String positiontitle = position.getTitle();

			List<BigInteger> lstPositionIds = new ArrayList<>();
			lstPositionIds.add(pid);

			// Board board = position.getBoard();
			RecPrefDTO recPref = new RecPrefDTO();
			recPref.setSbu(level1);
			line.append(level1).append(lineDelim);

			recPref.setDm(level2);
			line.append(level2).append(lineDelim);

			recPref.setDl(level3);
			line.append(level3).append(lineDelim);

			recPref.setTeam(level4);
			line.append(level4).append(lineDelim);

			// Client
			recPref.setClient(clientName);
			line.append(recPref.getClient()).append(lineDelim);

			// Position Title
			recPref.setReqs(positiontitle);
			line.append(recPref.getReqs()).append(lineDelim);

			//Position Status
			recPref.setStatus(positionstatus);
			line.append(recPref.getStatus()).append(lineDelim);

			// No of Positions
			recPref.setPosns(position.getTotalPosition() + "");
			line.append(recPref.getPosns()).append(lineDelim);

			// Req ID
			recPref.setReqId(position.getRequisitionId() == null ? "NA" : position.getRequisitionId());
			line.append(recPref.getReqId()).append(lineDelim);

			// Vertical
			recPref.setVertical(position.getVerticalCluster());
			line.append(recPref.getVertical()).append(lineDelim);

			// Requirement Type (Custom Field)
			recPref.setRequirementType(position.getCustomField().get("Requirement Type") == null ? "NA"
					: position.getCustomField().get("Requirement Type"));
			line.append(recPref.getRequirementType()).append(lineDelim);

			// Location
			String str_location = "";
			if (position.getLocation() != null || !position.getLocation().isEmpty()) {
				str_location = position.getLocation().toString();
				str_location = str_location.replace('|', '/');

			}

			recPref.setLocation(position.getLocation());
			line.append(str_location).append(lineDelim);

			// System.out.println("Position: " +positiontitle +" Location: " +str_location
			// +" Vertical:" +recPref.getVertical());

			// Creation Date
			recPref.setCreationDate(DateUtil.formateDate(position.getCreationDate()));
			line.append(recPref.getCreationDate()).append(lineDelim);

			recPref.setExp(position.getExperienceRange() == null ? "NA" : position.getExperienceRange());
			line.append(recPref.getExp()).append(lineDelim);

			// recPref.setJobLocation(position.getLocation());
			// line.append(recPref.getJobLocation()).append(lineDelim);

			recPref.setEndClient(position.getEndClient() == null ? "NA" : position.getEndClient());
			line.append(recPref.getEndClient()).append(lineDelim);

			//Hiring Manager
			recPref.setHiringManager(position.getHiringManager() == null ? "NA" : position.getHiringManager());
			line.append(recPref.getHiringManager()).append(lineDelim);

			//Internal SPOC
			recPref.setSpoc(position.getSpoc() == null ? "NA" : position.getSpoc());
			line.append(recPref.getSpoc()).append(lineDelim);

			// @sajin - Removed Recruiz default position type. Using only custom Requirement
			// type
			// recPref.setReqType(position.getType() == null ? "NA" : position.getType());
			// line.append(recPref.getReqType()).append(lineDelim);

			/*recPref.setCvForwarded(roundCandidateService.countByPositionCodeAndStatusInAndDateRange(pcode,
						cvSenttoClient, startDate, endDate));
				line.append(recPref.getCvForwarded()).append(lineDelim);*/

			/////
			recPref.setCvForwarded (positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(
					pid,cvSenttoClient, startDate, endDate));
			line.append(recPref.getCvForwarded()).append(lineDelim);

			////


			recPref.setAwCvUpd(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.AwaitingCvUpdate, startDate, endDate) + "");
			line.append(recPref.getAwCvUpd()).append(lineDelim);

			// New status
			recPref.setInt1Scheduled(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview1Scheduled, startDate, endDate));
			line.append(recPref.getInt1Scheduled()).append(lineDelim);

			recPref.setInt1(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview1, startDate, endDate) + "");
			line.append(recPref.getInt1()).append(lineDelim);

			recPref.setInt2(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview2, startDate, endDate) + "");
			line.append(recPref.getInt2()).append(lineDelim);

			recPref.setInt3(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview3, startDate, endDate) + "");
			line.append(recPref.getInt3()).append(lineDelim);

			recPref.setInt4(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview4, startDate, endDate) + "");
			line.append(recPref.getInt4()).append(lineDelim);

			recPref.setFinalInt(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.FinalRound, startDate, endDate) + "");
			line.append(recPref.getFinalInt()).append(lineDelim);

			recPref.setAwOff(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.AwaitingOffer, startDate, endDate) + "");
			line.append(recPref.getFinalInt()).append(lineDelim);

			recPref.setAwOffAcc(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.AwaitingOfferAcceptance, startDate, endDate) + "");
			line.append(recPref.getAwOffAcc()).append(lineDelim);

			recPref.setAwJng(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.AwaitingJoining, startDate, endDate) + "");
			line.append(recPref.getAwJng()).append(lineDelim);

			recPref.setJoined(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					BoardStatus.Joined.name(), startDate, endDate) + "");
			line.append(recPref.getJoined()).append(lineDelim);

			//			recPref.setCvRejected(roundCandidateService.getCountByPositionCodeAndStatusAndDateRange(pcode,
			//					BoardStatus.Rejected.name(), startDate, endDate) + "");

			// @Sajin - Considering 2 reject statuses for Reject count
			recPref.setCvRejected(positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid, rejectstatus,
					startDate, endDate));

			line.append(recPref.getCvRejected()).append(lineDelim);

			recPref.setCvOnHold(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					BoardStatus.OnHold.name(), startDate, endDate) + "");
			line.append(recPref.getCvOnHold()).append(lineDelim);

			recPref.setCvDuplicate(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					BoardStatus.Duplicate.name(), startDate, endDate) + "");
			line.append(recPref.getCvDuplicate()).append(lineDelim);

			recPref.setRejLvl1(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview1Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl1()).append(lineDelim);

			recPref.setRejLvl2(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview2Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl2()).append(lineDelim);

			recPref.setRejLvl3(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview3Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl3()).append(lineDelim);

			recPref.setRejLvl4(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.Interview4Reject, startDate, endDate) + "");
			line.append(recPref.getRejLvl4()).append(lineDelim);

			recPref.setRejLvlFinal(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.FinalRoundReject, startDate, endDate) + "");
			line.append(recPref.getRejLvlFinal()).append(lineDelim);

			// New status - Select Drop
			recPref.setSelectDrop(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.SelectDrop, startDate, endDate));
			line.append(recPref.getSelectDrop()).append(lineDelim);

			recPref.setIntNoShow(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.InterviewNoShow, startDate, endDate) + "");
			line.append(recPref.getIntNoShow()).append(lineDelim);

			recPref.setCndDropByUs(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.DroppedByUs, startDate, endDate) + "");
			line.append(recPref.getCndDropByUs()).append(lineDelim);

			recPref.setOfrOnHold(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.OfferOnHold, startDate, endDate) + "");
			line.append(recPref.getOfrOnHold()).append(lineDelim);

			recPref.setJoinNoShow(positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
					TeamwareConstants.JoineeNoShow, startDate, endDate) + "");
			line.append(recPref.getJoinNoShow()).append(lineDelim);

			// Ratios
			recPref.setJoinedVscvSub(ratio(
					positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid, BoardStatus.Joined.name(),
							startDate, endDate),
					feedbackService.getForwardProfileCountForPositionAndDateRange(positiontitle, clientName, startDate,
							endDate)));
			line.append(recPref.getJoinedVscvSub()).append(lineDelim);

			Long positioncount = 0L;
			String str_poscount = (String) recPref.getPosns();
			if (str_poscount != null) {
				positioncount = Long.parseLong(str_poscount);
			}

			//CVs forwarded / Number of Openings
			recPref.setCvSubVsPosns(ratio(recPref.getCvForwarded(),
					positionService.getTotalOpenPositionByPositionIDsBigInt(lstPositionIds)));
			line.append(recPref.getCvSubVsPosns()).append(lineDelim);

			//Interview 1 / Number of CVs added to position
			recPref.setInt1vsSourced(ratio(
					positionCandidateDataService.getCountByPositionIdAndStatusAndDateRange(pid,
							TeamwareConstants.Interview1, startDate, endDate),
					positionCandidateDataService.getCountByPositionIdAndDateRange(pid, startDate, endDate)));
			line.append(recPref.getInt1vsSourced()).append(lineDelim);

			// // this data is not available
			// recPref.setIntAttndVsScheduled("0");
			// line.append(recPref.getIntAttndVsScheduled()).append(lineDelim);

			//Select / InterviewScheduled
			recPref.setSelectedVsScheduled(ratio(positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
					selectStatuses, startDate, endDate),
					positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
							interviewScheduled, startDate, endDate)));
			line.append(recPref.getSelectedVsScheduled()).append(lineDelim);

			//Offered / Selected
			recPref.setOfferVsSelected(ratio(positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
					offeredStatuses, startDate, endDate),
					positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
							selectStatuses, startDate, endDate)));
			line.append(recPref.getOfferVsSelected()).append(lineDelim);

			//Joined / Offered
			recPref.setJoinedVsOffer(ratio(positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
					joinedStatuses, startDate, endDate),
					positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
							offeredStatuses, startDate, endDate)));
			line.append(recPref.getJoinedVsOffer()).append(lineDelim);

			//Joined / Total Openings
			recPref.setJoinedVsPosns(
					ratio(positionCandidateDataService.getcountByPositionIdAndStatusesInAndDateRange(pid,
							joinedStatuses, startDate, endDate), positionService.getTotalOpenPositionByPositionIDsBigInt(lstPositionIds)));
			line.append(recPref.getJoinedVsPosns());

			csvLines.add(line.toString());
			reports.add(recPref);
		}

		return csvLines;
	}

	private String addBizAnalysisHeader(List<String> csvLines) {
		final String lineDelim = "|";

		StringBuilder headerLine = new StringBuilder();
		headerLine.append("SBU Head").append(lineDelim).append("SBU").append(lineDelim).append("DM").append(lineDelim)
		.append("DL/Team").append(lineDelim);
		headerLine.append("Client").append(lineDelim).append("Position").append(lineDelim).append("Status").append(lineDelim).append("# Posns")
		.append(lineDelim);
		headerLine.append("Req ID").append(lineDelim).append("Vertical").append(lineDelim).append("Recruitment type")
		.append(lineDelim).append("Location").append(lineDelim).append("Job Received Date").append(lineDelim);
		headerLine.append("Exp").append(lineDelim).append("Account / End Client").append(lineDelim)
		.append("Account/Hiring Manager/RMG").append(lineDelim).append("Spoc").append(lineDelim);
		headerLine.append("# of Client Subm").append(lineDelim).append("Aw CV Upd").append(lineDelim)
		.append("Int-1 Scheduled").append(lineDelim).append("Int-1 FBP").append(lineDelim);
		headerLine.append("Int-2").append(lineDelim).append("Int-3").append(lineDelim).append("Int-4").append(lineDelim)
		.append("Final Int").append(lineDelim);
		headerLine.append("Aw Off").append(lineDelim).append("Aw Off Acc").append(lineDelim).append("Aw Jng")
		.append(lineDelim).append("Joined").append(lineDelim);
		headerLine.append("CV Rej").append(lineDelim).append("CV on Hold").append(lineDelim).append("CV Duplicate")
		.append(lineDelim).append("1st Lvl Rej").append(lineDelim);
		headerLine.append("2nd Lvl Rej").append(lineDelim).append("3rd Lvl Rej").append(lineDelim).append("4th Lvl Rej")
		.append(lineDelim).append("Final Lvl Rej").append(lineDelim).append("Select drop").append(lineDelim);
		headerLine.append("Int No Show").append(lineDelim).append("Candidate Dropped by us").append(lineDelim)
		.append("Offer on Hold").append(lineDelim).append("Joinee No Show").append(lineDelim);
		headerLine.append("Joined / CVs Sub").append(lineDelim).append("CVs Sub / Posn").append(lineDelim)
		.append("1st Lvl Int / CV").append(lineDelim);
		headerLine.append("Sels / Int").append(lineDelim).append("Offer / Sels").append(lineDelim)
		.append("Joined / Offer").append(lineDelim).append("Joined / Posns").append(lineDelim);

		csvLines.add(headerLine.toString());
		return lineDelim;
	}

	// @Teamware-ResumeSubmissionReport
	public Map<String, Object> getTeamwareResumeSubmissionReportForTeam(List<Long> teamIds, String vertical,
			Date startDate, Date enddate, String reportType)
					throws InvalidFormatException, IOException, RecruizException {
		File excelFile = null;

		Map<String, Object> response = new HashMap<>();
		List<String> positionCodes = new ArrayList<>();
		if (null != vertical && !vertical.trim().isEmpty()) {
			positionCodes = positionService.getPositionsCodeByTeamIdsAndVertical(teamIds, vertical);
		} else {
			positionCodes = positionService.getPositionsCodeByTeamIds(teamIds);
		}

		List<ResumeSubmissionDTO> records = new ArrayList<>();
		if (null != positionCodes && !positionCodes.isEmpty()) {
			int rowsCount = 0;
			for (String pcode : positionCodes) {
				Long count = roundCandidateService.getCandidateCountByPositionAndDateBetween(pcode, startDate, enddate);
				rowsCount = rowsCount + count.intValue();
			}

			Object[] header = new Object[29];

			Object[][] data = new Object[rowsCount + 1][29];
			header[0] = "Sr No";
			header[1] = "Account/Hiring Manager/RMG";
			header[2] = "SPOC";
			header[3] = "Resume Sourced date";
			header[4] = "Submission Date to Client";
			header[5] = "Submission Time";
			header[6] = "Client";
			header[7] = "Requisition ID";
			header[8] = "Name of Candidate";
			header[9] = "Position/ Skill";
			header[10] = "Client Status / current status in pipeline";
			header[11] = "Remarks";
			header[12] = "Recruiter";
			header[13] = "Screener";
			header[14] = "1st round Date";
			header[15] = "2nd round Date";
			header[16] = "3rd round Date";
			header[17] = "Final Round Date";
			header[18] = "Offer date";
			header[19] = "DOJ";
			header[20] = "Current Org";
			header[21] = "Exp";
			header[22] = "Email";
			header[23] = "Phone #";
			header[24] = "Notice Period";
			header[25] = "Candidates Current Location";
			header[26] = "Preferred Location";
			header[27] = "CCTC";
			header[28] = "ECTC";

			int count = 0;
			for (String pcode : positionCodes) {
				List<RoundCandidate> sourcedCandidate = roundCandidateService
						.getCandidateByPositionAndDateBetween(pcode, startDate, enddate);
				if (null != sourcedCandidate && !sourcedCandidate.isEmpty()) {
					Position position = positionService.getPositionByCode(pcode);
					for (RoundCandidate roundCandidate : sourcedCandidate) {
						ResumeSubmissionDTO record = new ResumeSubmissionDTO();
						Long roundcandidateId = roundCandidate.getId();
						Object[] candidateDetails = (Object[]) candidateService
								.getCandidateDetailsFromRoundCandidateId(roundcandidateId);

						data[count][0] = count + 1;
						record.setSrlNo(count + 1);

						//Hiring Manager
						data[count][1] = position.getHiringManager() == null ? "NA" : position.getHiringManager();
						record.setHiringmanager(data[count][1]);

						//Spoc
						data[count][2] = position.getSpoc() == null ? "NA" : position.getSpoc();
						record.setSpoc(data[count][2]);

						data[count][3] = DateUtil.formateDate(roundCandidate.getCreationDate());
						record.setResumeSharedDate(data[count][3]);

						data[count][4] = feedbackService
								.getTopFeedbackByRoundCandidateForForwardType(roundcandidateId) == null
								? "NA"
										: DateUtil.formateDate(feedbackService
												.getTopFeedbackByRoundCandidateForForwardType(roundcandidateId)
												.getCreationDate(), "dd-mm-yyyy");
						record.setSubmissionDate(data[count][4]);

						data[count][5] = feedbackService
								.getTopFeedbackByRoundCandidateForForwardType(roundcandidateId) == null
								? "NA"
										: DateUtil.formateDate(feedbackService
												.getTopFeedbackByRoundCandidateForForwardType(roundcandidateId)
												.getCreationDate(), "hh:mm");
						record.setSubmisssionTime(data[count][5]);


						data[count][6] = position.getClient().getClientName() == null ? "NA" : position.getClient().getClientName();
						record.setClientName(data[count][6]);

						data[count][7] = position.getRequisitionId() == null ? "NA" : position.getRequisitionId();
						record.setReqId(data[count][7]);

						data[count][8] = candidateDetails[1];
						record.setCandidateName(data[count][8]);

						position.getReqSkillSet().size();
						data[count][9] = position.getTitle() + " / "
								+ StringUtils.commaSeparate(position.getReqSkillSet());
						record.setPositionSkills(data[count][9]);

						data[count][10] = roundCandidate.getStatus();
						record.setPipelineStatus(data[count][10]);

						// latest note from candidate
						String strlastNote = candidateService.getLatestNoteForCandidate(candidateDetails[0] + "");

						String strlastNote1 = strlastNote.trim();
						String strlastNote2 = strlastNote1.replace("</p>", "");
						String strlastNote3 = strlastNote2.replace("<p>", "");
						String strlastNote4 = strlastNote3.replace("&nbsp;", "");
						String strlastNote5 = strlastNote4.trim();
						data[count][11] = strlastNote5;
						record.setRemarks(data[count][11]);

						data[count][12] = roundCandidate.getSourcedBy();
						record.setRecruiter(data[count][12]);

						data[count][13] = position.getScreener() == null ? "NA" : position.getScreener();
						record.setScreener(data[count][13]);

						InterviewSchedule interview1 = interviewScheduleService
								.getScheduleByPositionCodeRoundNameAndCandidateEmail(pcode, TeamwareConstants.Round1,
										roundCandidate.getCandidate().getEmail());
						data[count][14] = interview1 == null ? "NA"
								: DateUtil.formateDateAndTime(interview1.getStartsAt());
						record.setRound1Date(data[count][14]);

						InterviewSchedule interview2 = interviewScheduleService
								.getScheduleByPositionCodeRoundNameAndCandidateEmail(pcode, TeamwareConstants.Round2,
										roundCandidate.getCandidate().getEmail());
						data[count][15] = interview2 == null ? "NA"
								: DateUtil.formateDateAndTime(interview2.getStartsAt());
						record.setRound2Date(data[count][15]);

						InterviewSchedule interview3 = interviewScheduleService
								.getScheduleByPositionCodeRoundNameAndCandidateEmail(pcode, TeamwareConstants.Round3,
										roundCandidate.getCandidate().getEmail());
						data[count][16] = interview3 == null ? "NA"
								: DateUtil.formateDateAndTime(interview3.getStartsAt());
						record.setRound3Date(data[count][16]);

						InterviewSchedule finalRound = interviewScheduleService
								.getScheduleByPositionCodeRoundNameAndCandidateEmail(pcode,
										TeamwareConstants.FinalRoundInterview,
										roundCandidate.getCandidate().getEmail());
						data[count][17] = finalRound == null ? "NA"
								: DateUtil.formateDateAndTime(finalRound.getStartsAt());
						record.setFinalRoundDate(data[count][17]);

						data[count][18] = roundCandidate.getOfferDate() == null ? "NA"
								: DateUtil.formateDate(roundCandidate.getOfferDate());
						record.setOfferDate(data[count][18]);

						data[count][19] = roundCandidate.getJoinedDate() == null ? "NA"
								: DateUtil.formateDate(roundCandidate.getJoinedDate());
						record.setDoj(data[count][19]);

						data[count][20] = candidateDetails[4] == null ? "NA" : candidateDetails[4];
						record.setCurrentOrg(data[count][20]);

						data[count][21] = candidateDetails[9];
						record.setExp(data[count][21]);

						data[count][22] = candidateDetails[3];
						record.setEmail(data[count][22]);

						data[count][23] = candidateDetails[2] == null ? "NA" : candidateDetails[2];
						record.setPhone(data[count][23]);

						data[count][24] = candidateDetails[5];
						record.setNoticePeriod(data[count][24]);

						data[count][25] = candidateDetails[6] == null ? "NA" : candidateDetails[6];
						record.setCandCurrentLocation(data[count][25]);

						data[count][26] = candidateDetails[10] == null ? "NA" : candidateDetails[10];
						record.setPrefLocation(data[count][26]);

						data[count][27] = candidateDetails[7];
						record.setCctc(data[count][27]);

						data[count][28] = candidateDetails[8];
						record.setExpectedCTC(data[count][28]);

						records.add(record);
						count++;
					}

				}
			}

			if (reportType == "json") {
				Map<String, Object> result = new HashMap<>();
				result.put("listData", records);
				response.put("pivotData", result);
			} else {
				excelFile = importExportService.resultSetToExcelExport(header, data, System.currentTimeMillis() + "",
						null);
				response.put("excel", excelFile);
			}
		}
		return response;
	}

	// @Teamware-ClientAnalysisReport
	public Map<String, Object> getClientAnalysisReportForTeamware(Date start, Date end, List<Long> clientIds,
			List<String> vertical, String location, List<String> status) throws RecruizException {

		if (null == status || status.isEmpty()) {
			status = new ArrayList<>();
			status.add(Status.Active.name());
			status.add(Status.OnHold.name());
			status.add(Status.Closed.name());
			status.add(Status.StopSourcing.name());
		}

		Map<String, Object> report = new HashMap<>();
		List<String> csvLines = new ArrayList<>();
		List<PrefTrend> reportList = new ArrayList<>();
		final String lineDelim = "|";

		StringBuilder headerLine = new StringBuilder();
		headerLine.append("Year").append(lineDelim);
		headerLine.append("Month").append(lineDelim);
		headerLine.append("Week").append(lineDelim);
		headerLine.append("# Reqs").append(lineDelim).append("# Posns").append(lineDelim).append("# CVs Added")
		.append(lineDelim).append("# of Client Subm").append(lineDelim);
		headerLine.append("# Interviewed").append(lineDelim).append("# Selects").append(lineDelim).append("# Offered")
		.append(lineDelim).append("# Joined").append(lineDelim);
		headerLine.append("# CV Client Rej").append(lineDelim).append("Interview Rejects").append(lineDelim)
		.append("Interview Noshows").append(lineDelim).append("Joinee No Show").append(lineDelim);
		headerLine.append("Joined / CVs Sub").append(lineDelim).append("CVs Sub / Posn").append(lineDelim);
		headerLine.append("1st Lvl Int / CV").append(lineDelim).append("Interview Attended / Interview Scheduled")
		.append(lineDelim).append("Sels / Int").append(lineDelim).append("Offer / Sels").append(lineDelim);
		headerLine.append("Joined / Offer").append(lineDelim).append("Joined / Posns").append(lineDelim);

		csvLines.add(headerLine.toString());
		User user = userService.getLoggedInUserObject();
		List<User> users = new ArrayList<>();
		users.add(user);

		List<String> interviewedStatuses = getInterviwedStatuses();
		List<String> selectStatuses = getselectStatuses();
		List<String> offeredStatuses = getofferedStatuses();
		List<String> interviewRejectStatuses = getinterviewRejectStatuses();
		List<String> interviewScheduled = getinterviewScheduled();
		List<String> cvSenttoClient = getCVsSenttoClient();

		List<Integer> years = positionService.getYearsForGivenDateRange(start, end);
		for (Integer year : years) {
			List<String> months = positionService.getMonthsForGiveYear(year, start, end);
			for (String month : months) {
				List<Integer> weeks = positionService.getWeeksForGivenYearAndMonth(month, year, start, end);
				for (Integer weekNo : weeks) {

					DateTime weekStartDate = null;
					DateTime weekEndDate = null;

					if (weekNo != 0) {
						weekStartDate = new DateTime().withWeekOfWeekyear(weekNo);

					} else {
						continue;
					}

					if (weekNo < 52) {
						weekEndDate = new DateTime().withWeekOfWeekyear(weekNo + 1);
					} else {
						weekEndDate = new DateTime().withWeekOfWeekyear(1);
					}

					List<String> positionCodes = positionService.getPositionCodesByClientAndVerticalAndLocationAndWeek(
							clientIds, vertical, location, weekNo, year, status, user.getEmail(),
							teamService.getAllTeamsIdsForCurrentUser(), user.getUserId());
					if (null == positionCodes || positionCodes.isEmpty()) {
						continue;
					}

					// recPref.setSbu(getPositionTeamSBU(position.getTeam()));
					// line.append(recPref.getSbu()).append(lineDelim);

					String week = "week " + weekNo;

					PrefTrend recPref = new PrefTrend();

					StringBuilder line = new StringBuilder();

					recPref.setYear(year);
					line.append(year).append(lineDelim);

					recPref.setMonth(month);
					line.append(month).append(lineDelim);

					line.append(week).append(lineDelim);
					recPref.setWeek(week);

					recPref.setReqs(positionCodes.size());
					line.append(positionCodes.size()).append(lineDelim);

					recPref.setPosns(positionService.getTotalOpenPositionByPositionCodes(positionCodes));
					line.append(recPref.getPosns()).append(lineDelim);

					recPref.setCvsParsed(roundCandidateService.getCountByPositionCodes(positionCodes));
					line.append(recPref.getCvsParsed()).append(lineDelim);

					// @sajin - Falling back to counting Client submission count based on Status and
					// not based on Feedback count
					recPref.setClientSubmission(roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(
							positionCodes, cvSenttoClient, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getClientSubmission()).append(lineDelim);

					// recPref.setClientSubmission(feedbackService.getFeedbackCountByPositionCodeAndDateRange(
					// positionCodes, weekStartDate.toDate(), weekEndDate.toDate()));
					// line.append(recPref.getClientSubmission()).append(lineDelim);

					// Long totalInterviewed =
					// interviewScheduleService.getInterviewCountByPositionCodes(positionCodes);
					// recPref.setInterviewed(totalInterviewed);
					// line.append(totalInterviewed).append(lineDelim);

					// Total Interviewed
					recPref.setInterviewed(roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(
							positionCodes, interviewedStatuses, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getInterviewed()).append(lineDelim);

					// Total Selected
					recPref.setSelected(roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(
							positionCodes, selectStatuses, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getSelected()).append(lineDelim);

					// Interview Rejects
					recPref.setInterviewRejected(roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(
							positionCodes, interviewRejectStatuses, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getInterviewRejected()).append(lineDelim);

					// Total Offered
					recPref.setOffered(roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(
							positionCodes, offeredStatuses, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getOffered()).append(lineDelim);

					// Individual status counts
					recPref.setJoined(roundCandidateService.getCountByPositionCodesAndStatusAndDateRange(positionCodes,
							BoardStatus.Joined.name(), weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getJoined()).append(lineDelim);

					recPref.setCvsClientRejected(
							roundCandidateService.getCountByPositionCodesAndStatusAndDateRange(positionCodes,
									TeamwareConstants.ClientRejected, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getCvsClientRejected()).append(lineDelim);

					recPref.setInterviewNoShow(
							roundCandidateService.getCountByPositionCodesAndStatusAndDateRange(positionCodes,
									TeamwareConstants.InterviewNoShow, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getInterviewNoShow()).append(lineDelim);

					recPref.setJoineeNoShow(
							roundCandidateService.getCountByPositionCodesAndStatusAndDateRange(positionCodes,
									TeamwareConstants.JoineeNoShow, weekStartDate.toDate(), weekEndDate.toDate()));
					line.append(recPref.getJoineeNoShow()).append(lineDelim);

					// Ratios
					recPref.setJoinedVsCvsSub(ratio(recPref.getJoined(), recPref.getClientSubmission()));
					line.append(recPref.getJoinedVsCvsSub()).append(lineDelim);

					recPref.setCvsSubVsPosns(ratio(recPref.getClientSubmission(), recPref.getPosns()));
					line.append(recPref.getCvsSubVsPosns()).append(lineDelim);

					recPref.setLvl1interVsCvs(ratio(recPref.getInterviewed(), recPref.getClientSubmission()));
					line.append(recPref.getLvl1interVsCvs()).append(lineDelim);

					recPref.setInterviewVsScheduled(ratio(recPref.getInterviewed(),
							roundCandidateService.getCountByPositionCodesAndStatusInAndDateRange(positionCodes,
									interviewScheduled, weekStartDate.toDate(), weekEndDate.toDate())));
					line.append(recPref.getInterviewVsScheduled()).append(lineDelim);

					recPref.setSelectedVsInt(ratio(recPref.getSelected(), recPref.getInterviewed()));
					line.append(recPref.getSelectedVsInt()).append(lineDelim);

					recPref.setOfferVsSelected(ratio(recPref.getOffered(), recPref.getSelected()));
					line.append(recPref.getOfferVsSelected()).append(lineDelim);

					recPref.setJoinedVsOffer(ratio(recPref.getJoined(), recPref.getOffered()));
					line.append(recPref.getJoinedVsOffer()).append(lineDelim);

					recPref.setJoinedVsPosns(ratio(recPref.getJoined(), recPref.getPosns()));
					line.append(recPref.getJoinedVsPosns());

					csvLines.add(line.toString());
					reportList.add(recPref);
				}
			}
		}
		report.put("csvString", csvLines);
		report.put("listData", reportList);
		return report;
	}

	// List<Integer> weeks = positionService.getWeeksForGicenDateRange(new
	// java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()));

	// for (Integer weekNo : weeks) {
	//
	// }

	// }

	private String getPositionTeamSBU(Team team) {
		String sbu = "";
		if (null == team) {
			return "NA";
		}
		if (team.getParent() == null) {
			return team.getTeamName();
		}
		getPositionTeamSBU(team.getParent());
		return sbu;
	}

	// to create excel file from csv string
	public File createExcelFile(List<String> csvLines, String fileName) throws IOException {
		String tempFolderPath = System.getProperty("java.io.tmpdir");
		File excelFile = new File(tempFolderPath + File.separator + fileName + ".xlsx");

		XSSFWorkbook workbook = new XSSFWorkbook();
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(fileName);
		for (int i = 0; i < csvLines.size(); i++) {
			XSSFRow row = sheet.createRow(i);
			ArrayList<String> arrElement = new ArrayList<String>(Arrays.asList(csvLines.get(i).split("\\|")));
			CellStyle cellStyle = workbook.createCellStyle();
			for (int j = 0; j < arrElement.size(); j++) {
				XSSFCell cell = row.createCell(j);
				try {
					if (NumberUtils.isNumber(arrElement.get(j))) {
						cell.setCellValue(Double.parseDouble(arrElement.get(j)));
					} else {
						cell.setCellValue(arrElement.get(j));
					}
				} catch (Exception ex) {
					cell.setCellValue(arrElement.get(j));
				}

				if (i == 0) {
					cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
					cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
					cell.setCellStyle(cellStyle);
				}
			}
		}
		// Write the workbook in file system
		FileOutputStream out = new FileOutputStream(excelFile);
		workbook.write(out);
		out.close();
		return excelFile;
	}

	public File getPerformanceReportAsExcel(Long clientId, User hrExecutive, Date startDate, Date endDate,
			String status) throws InvalidFormatException, IOException {
		File file = null;
		Client client = null;
		if (clientId == 0) {

			file = getClientPositionStatMapList(hrExecutive, startDate, endDate, client, status);

		} else {
			client = clientService.findOne(clientId);
			file = getClientPositionStatSingle(hrExecutive, startDate, endDate, client, status);
		}
		return file;
	}

	@SuppressWarnings("unchecked")
	private File getClientPositionStatMapList(User hrExecutive, Date startDate, Date endDate, Client client,
			String status) throws InvalidFormatException, IOException {

		List<Long> hrClients = clientService.getClientIdsForHrExecutive(hrExecutive);
		int totalrows = 0;
		for (Long cid : hrClients) {
			client = clientService.findOne(cid);
			Set<User> positionHrs = new HashSet<>();
			positionHrs.add(hrExecutive);

			List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, positionHrs, status);
			totalrows = totalrows + hrPositions.size();
		}

		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 17;
		int sheetRowSize = totalrows;
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client Name";
		header[2] = "Position/Requisition";
		header[3] = "Status";
		header[4] = "Total";
		header[5] = "Yet To Process";
		header[6] = "In Progress";
		header[7] = "Approved";
		header[8] = "Rejected";
		header[9] = "On Hold";
		header[10] = "Duplicate";
		header[11] = "Dropped Out";
		header[12] = "Not Interested";
		header[13] = "Offered";
		header[14] = "Offer Accepted";
		header[15] = "Offer Declined";
		header[16] = "Joined";

		int row = 0;

		for (Long cid : hrClients) {

			client = clientService.findOne(cid);
			Set<User> positionHrs = new HashSet<>();
			positionHrs.add(hrExecutive);

			List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, positionHrs, status);

			if (hrPositions != null && !hrPositions.isEmpty()) {
				for (Position position : hrPositions) {

					try {
						Map<String, Object> countResponse = (Map<String, Object>) getCandidateCountStatusWise(startDate,
								endDate, hrExecutive.getEmail(), position.getPositionCode());

						data[row][0] = datarowCount + 1;
						data[row][1] = client.getClientName();
						data[row][2] = position.getTitle();
						data[row][3] = getMapdataInString(countResponse.get("status"));
						data[row][4] = getMapdataInString(countResponse.get("total"));
						data[row][5] = getMapdataInString(countResponse.get("Yet To Process"));
						data[row][6] = getMapdataInString(countResponse.get("In Progress"));
						data[row][7] = getMapdataInString(countResponse.get("Approved"));
						data[row][8] = getMapdataInString(countResponse.get("Rejected"));
						data[row][9] = getMapdataInString(countResponse.get("On Hold"));
						data[row][10] = getMapdataInString(countResponse.get("Duplicate"));
						data[row][11] = getMapdataInString(countResponse.get("Dropped Out"));
						data[row][12] = getMapdataInString(countResponse.get("Not Interested"));
						data[row][13] = getMapdataInString(countResponse.get("Offered"));
						data[row][14] = getMapdataInString(countResponse.get("Offer Accepted"));
						data[row][15] = getMapdataInString(countResponse.get("Offer Declined"));
						data[row][16] = getMapdataInString(countResponse.get("Joined"));

						row++;
						datarowCount++;
					} catch (RecruizException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		String startDate1 = formatter.format(startDate);
		String endDate1 = formatter.format(endDate);

		String fullfileName = hrExecutive.getName() + "_" + startDate1 + "_" + "to_" + endDate1;

		excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;
	}

	@SuppressWarnings("unchecked")
	private File getClientPositionStatSingle(User hrExecutive, Date startDate, Date endDate, Client client,
			String status) throws InvalidFormatException, IOException {

		Set<User> positionHrs = new HashSet<>();
		positionHrs.add(hrExecutive);

		List<Position> hrPositions = positionService.getAllPositionByClientAndHrIn(client, positionHrs, status);
		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 17;
		int sheetRowSize = hrPositions.size();
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client Name";
		header[2] = "Position/Requisition";
		header[3] = "Status";
		header[4] = "Total";
		header[5] = "Yet To Process";
		header[6] = "In Progress";
		header[7] = "Approved";
		header[8] = "Rejected";
		header[9] = "On Hold";
		header[10] = "Duplicate";
		header[11] = "Dropped Out";
		header[12] = "Not Interested";
		header[13] = "Offered";
		header[14] = "Offer Accepted";
		header[15] = "Offer Declined";
		header[16] = "Joined";

		int row = 0;
		if (hrPositions != null && !hrPositions.isEmpty()) {
			for (Position position : hrPositions) {

				try {
					Map<String, Object> countResponse = (Map<String, Object>) getCandidateCountStatusWise(startDate,
							endDate, hrExecutive.getEmail(), position.getPositionCode());

					data[row][0] = datarowCount + 1;
					data[row][1] = client.getClientName();
					data[row][2] = position.getTitle();
					data[row][3] = getMapdataInString(countResponse.get("status"));
					data[row][4] = getMapdataInString(countResponse.get("total"));
					data[row][5] = getMapdataInString(countResponse.get("Yet To Process"));
					data[row][6] = getMapdataInString(countResponse.get("In Progress"));
					data[row][7] = getMapdataInString(countResponse.get("Approved"));
					data[row][8] = getMapdataInString(countResponse.get("Rejected"));
					data[row][9] = getMapdataInString(countResponse.get("On Hold"));
					data[row][10] = getMapdataInString(countResponse.get("Duplicate"));
					data[row][11] = getMapdataInString(countResponse.get("Dropped Out"));
					data[row][12] = getMapdataInString(countResponse.get("Not Interested"));
					data[row][13] = getMapdataInString(countResponse.get("Offered"));
					data[row][14] = getMapdataInString(countResponse.get("Offer Accepted"));
					data[row][15] = getMapdataInString(countResponse.get("Offer Declined"));
					data[row][16] = getMapdataInString(countResponse.get("Joined"));

					row++;
					datarowCount++;
				} catch (RecruizException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		String startDate1 = formatter.format(startDate);
		String endDate1 = formatter.format(endDate);

		String fullfileName = hrExecutive.getName() + "_" + startDate1 + "_" + "to_" + endDate1;

		excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;

	}

	String getMapdataInString(Object dataRecord) {
		if (dataRecord == null) {
			return "0";
		} else {
			return dataRecord.toString();
		}
	}

	public File downloadOutstandingPositionAsExcel(List<Client> allClients, Date startDate, Date endDate,
			List<String> positionCodes) throws InvalidFormatException, IOException {

		int totalRowSize = 0;
		for (Client client : allClients) {
			List<Position> positions = new ArrayList<>();
			if (positionCodes != null && !positionCodes.isEmpty()) {
				positions = positionService.getPositionByClientAndPositionCodeInAndDateInterval(client, positionCodes,
						startDate, endDate);
			} else {
				positions = positionService.getPositionByClientAndDateInterval(client, startDate, endDate);
			}

			if (null == positions || positions.isEmpty()) {
				continue;
			}
			totalRowSize = totalRowSize + positions.size();
		}

		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 11;
		int sheetRowSize = totalRowSize;
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		// Header
		header[0] = "Sl No";
		header[1] = "Client_Name";
		header[2] = "Client_Location";
		header[3] = "Position/Requisition";
		header[4] = "Total_Opening";
		header[5] = "Forwarded";
		header[6] = "Shortlisted";
		header[7] = "Interviews";
		header[8] = "Selected";
		header[9] = "Offered";
		header[10] = "Joined";

		int row = 0;
		Set<String> status = new HashSet<>();
		status.add(BoardStatus.Joined.getDisplayName());
		Long totalForwarded = 0L;
		Long totalShortlisted = 0L;
		Long totalInterview = 0L;
		Long totalSelected = 0L;
		Long totalOffered = 0L;
		Long totalJoined = 0L;
		for (Client client : allClients) {

			List<Position> positions = new ArrayList<>();
			if (positionCodes != null && !positionCodes.isEmpty()) {
				positions = positionService.getPositionByClientAndPositionCodeInAndDateInterval(client, positionCodes,
						startDate, endDate);
			} else {
				positions = positionService.getPositionByClientAndDateInterval(client, startDate, endDate);
			}

			if (null == positions || positions.isEmpty()) {
				continue;
			}

			for (int pCount = 0; pCount < positions.size(); pCount++) {
				Position position = positions.get(pCount);

				Long joined = roundCandidateService.getCountByPositionAndStatusAndDateRange(position.getPositionCode(),
						BoardStatus.Joined.getDisplayName(), startDate, endDate);

				totalJoined += joined;

				Long offered = roundCandidateService.getCountByPositionAndStatusAndDateRange(position.getPositionCode(),
						BoardStatus.Offered.getDisplayName(), startDate, endDate);
				totalOffered += offered;

				Long selected = roundCandidateService.getCountByPositionAndStatusAndDateRange(
						position.getPositionCode(), BoardStatus.Selected.getDisplayName(), startDate, endDate);
				totalSelected += selected;

				Long forwarded = feedbackService.getForwardProfileCountForPosition(position.getTitle(),
						client.getClientName(), startDate, endDate);
				totalForwarded += forwarded;

				Long interviews = interviewScheduleService
						.getInterviewCountByPositionForDateRange(position.getPositionCode(), startDate, endDate);
				totalInterview += interviews;

				try {
					data[row][0] = datarowCount + 1;
					data[row][1] = client.getClientName();
					data[row][2] = client.getClientLocation();
					data[row][3] = position.getTitle();
					data[row][4] = getMapdataInString(position.getTotalPosition());
					data[row][5] = getMapdataInString(forwarded);
					data[row][6] = 0;
					data[row][7] = getMapdataInString(interviews);
					data[row][8] = getMapdataInString(selected);
					data[row][9] = getMapdataInString(offered);
					data[row][10] = getMapdataInString(joined);
					row++;
					datarowCount++;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
		String startDate1 = formatter.format(startDate);
		String endDate1 = formatter.format(endDate);

		String fullfileName = "OutstandingPositionRequest" + "_" + startDate1 + "_" + "to_" + endDate1;

		excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		return excelFile;

	}

	private List<String> getInterviwedStatuses() {

		// Interviewed statuses
		List<String> interviewedStatuses = new ArrayList<>();
		interviewedStatuses.add(TeamwareConstants.Interview1);
		interviewedStatuses.add(TeamwareConstants.Interview2);
		interviewedStatuses.add(TeamwareConstants.Interview3);
		interviewedStatuses.add(TeamwareConstants.Interview4);
		interviewedStatuses.add(TeamwareConstants.FinalRoundInterview);
		interviewedStatuses.add(TeamwareConstants.AwaitingOffer);
		interviewedStatuses.add(TeamwareConstants.AwaitingOfferAcceptance);
		interviewedStatuses.add(TeamwareConstants.AwaitingJoining);
		interviewedStatuses.add(BoardStatus.Joined.name());
		interviewedStatuses.add(TeamwareConstants.Interview1Reject);
		interviewedStatuses.add(TeamwareConstants.Interview2Reject);
		interviewedStatuses.add(TeamwareConstants.Interview3Reject);
		interviewedStatuses.add(TeamwareConstants.Interview4Reject);
		interviewedStatuses.add(TeamwareConstants.FinalRoundReject);
		interviewedStatuses.add(TeamwareConstants.SelectDrop);
		interviewedStatuses.add(TeamwareConstants.OfferOnHold);
		interviewedStatuses.add(TeamwareConstants.JoineeNoShow);

		return interviewedStatuses;
	}

	private List<String> getselectStatuses() {

		// Select statuses
		List<String> selectStatuses = new ArrayList<>();
		selectStatuses.add(TeamwareConstants.AwaitingOffer);
		selectStatuses.add(TeamwareConstants.AwaitingOfferAcceptance);
		selectStatuses.add(TeamwareConstants.AwaitingJoining);
		selectStatuses.add(BoardStatus.Joined.name());
		selectStatuses.add(TeamwareConstants.SelectDrop);
		selectStatuses.add(TeamwareConstants.OfferOnHold);
		selectStatuses.add(TeamwareConstants.JoineeNoShow);

		return selectStatuses;
	}

	private List<String> getofferedStatuses() {

		// Select statuses
		List<String> offeredStatuses = new ArrayList<>();
		offeredStatuses.add(TeamwareConstants.AwaitingOfferAcceptance);
		offeredStatuses.add(TeamwareConstants.AwaitingJoining);
		offeredStatuses.add(BoardStatus.Joined.name());
		offeredStatuses.add(TeamwareConstants.OfferOnHold);
		offeredStatuses.add(TeamwareConstants.JoineeNoShow);

		return offeredStatuses;
	}

	private List<String> getinterviewRejectStatuses() {

		// Interview Reject statuses
		List<String> interviewRejectStatuses = new ArrayList<>();
		interviewRejectStatuses.add(TeamwareConstants.Interview1Reject);
		interviewRejectStatuses.add(TeamwareConstants.Interview2Reject);
		interviewRejectStatuses.add(TeamwareConstants.Interview3Reject);
		interviewRejectStatuses.add(TeamwareConstants.Interview4Reject);
		interviewRejectStatuses.add(TeamwareConstants.InterviewReject);
		interviewRejectStatuses.add(TeamwareConstants.FinalRoundReject);

		return interviewRejectStatuses;
	}

	private List<String> getCVsSenttoClient() {

		// CV sent to client
		List<String> cvSenttoClient = new ArrayList<>();
		cvSenttoClient.add(TeamwareConstants.AwaitingCvUpdate);
		cvSenttoClient.add(TeamwareConstants.Interview1);
		cvSenttoClient.add(TeamwareConstants.Interview2);
		cvSenttoClient.add(TeamwareConstants.Interview3);
		cvSenttoClient.add(TeamwareConstants.Interview4);
		cvSenttoClient.add(TeamwareConstants.FinalRoundInterview);
		cvSenttoClient.add(TeamwareConstants.AwaitingOffer);
		cvSenttoClient.add(TeamwareConstants.AwaitingOfferAcceptance);
		cvSenttoClient.add(TeamwareConstants.AwaitingJoining);
		cvSenttoClient.add(BoardStatus.Joined.name());
		cvSenttoClient.add(TeamwareConstants.Interview1Reject);
		cvSenttoClient.add(TeamwareConstants.Interview2Reject);
		cvSenttoClient.add(TeamwareConstants.Interview3Reject);
		cvSenttoClient.add(TeamwareConstants.Interview4Reject);
		cvSenttoClient.add(TeamwareConstants.InterviewReject);
		cvSenttoClient.add(TeamwareConstants.FinalRoundReject);
		cvSenttoClient.add(TeamwareConstants.SelectDrop);
		cvSenttoClient.add(TeamwareConstants.OfferOnHold);
		cvSenttoClient.add(TeamwareConstants.JoineeNoShow);
		cvSenttoClient.add(TeamwareConstants.InterviewNoShow);
		cvSenttoClient.add(TeamwareConstants.CVReject);
		cvSenttoClient.add(TeamwareConstants.ClientRejected);
		cvSenttoClient.add(BoardStatus.Rejected.name());
		cvSenttoClient.add(BoardStatus.Duplicate.name());
		cvSenttoClient.add(BoardStatus.OnHold.name());

		return cvSenttoClient;
	}

	private List<String> getinterviewScheduled() {

		List<String> interviewScheduled = getInterviwedStatuses();
		interviewScheduled.add(TeamwareConstants.InterviewNoShow);

		return interviewScheduled;

	}

	private List<String> getrejectStatuses() {

		List<String> rejectstatuses = new ArrayList<>();
		rejectstatuses.add(TeamwareConstants.CVReject);
		rejectstatuses.add(TeamwareConstants.ClientRejected);
		rejectstatuses.add(BoardStatus.Rejected.name());

		return rejectstatuses;
	}

	private List<String> getjoinedStatuses() {

		List<String> joinedstatuses = new ArrayList<>();
		joinedstatuses.add(BoardStatus.Joined.name());
		joinedstatuses.add(BoardStatus.Employee.name());
		return joinedstatuses;
	}

	public RestResponse getCandidatesListInEachRecruiter(String positionCode) {

		List<BaseDTO> statusList = new ArrayList<BaseDTO>();
		List<RecruiterPerformanceReportDTO> recruiterData = new ArrayList<>();
		try {

			for (BoardStatus status : BoardStatus.values()) {
				if(status!=null){
					BaseDTO baseDTO = new BaseDTO();
					baseDTO.setId(status.name());
					baseDTO.setValue(status.getDisplayName());
					statusList.add(baseDTO);
				}
			}

			List<BoardCustomStatus> customStatus = boardCustomStatusService.findAll();
			if (null != customStatus && !customStatus.isEmpty()) {
				for (BoardCustomStatus boardCustomStatus : customStatus) {
					BaseDTO baseDTO = new BaseDTO();
					baseDTO.setId(boardCustomStatus.getStatusKey());
					baseDTO.setValue(boardCustomStatus.getStatusName());
					statusList.add(baseDTO);
				}
			}

			Set<String> recruitersEmail =  roundCandidateService.getRecruitersEmailByPositionCode(positionCode);

			if(recruitersEmail==null)
				return new RestResponse(RestResponse.SUCCESS, recruiterData, "Not found any data");

			for (String email : recruitersEmail) {

				if(email==null)
					continue;

				List<BaseDTO> candidateRecord = new ArrayList<BaseDTO>();
				RecruiterPerformanceReportDTO reportDto = new RecruiterPerformanceReportDTO();
				List<RoundCandidate> roundCandidateList = roundCandidateService.getRoundCandidateByPositionCodeAndSourcedBy(positionCode,email);

				if(roundCandidateList==null)
					continue;

				Integer [] arr = new Integer[statusList.size()];
				ArrayList<Integer> myList= new ArrayList<>(Arrays.asList(arr));
				Collections.fill(myList, 0);//fills all entries with 0

				for(int i=0; i<roundCandidateList.size(); i++){

					for (int k=0; k < statusList.size(); k++) {

						if(roundCandidateList.get(i).getStatus().equalsIgnoreCase(statusList.get(k).getId())){

							int value = myList.get(k).intValue();
							int newValue = value + 1;
							myList.set(k, newValue);
						}

					}

				}


				User user = userRepository.findOneByEmail(email);
				reportDto.setRecruiterName(user.getName());
				reportDto.setRecruiterEmail(user.getEmail());
				reportDto.setCandidatesSourced(String.valueOf(roundCandidateList.size()));

				for (int k=0; k < statusList.size(); k++) {
					BaseDTO  dto= new BaseDTO();
					dto.setId(statusList.get(k).getValue());
					dto.setValue(String.valueOf(myList.get(k).intValue()));
					candidateRecord.add(dto);
				}

				reportDto.setStatusCandidates(candidateRecord);

				recruiterData.add(reportDto);
			}


		} catch (Exception e) {
			logger.error(e.getMessage()+"Error Occured in list "+e.toString());
			return new RestResponse(RestResponse.FAILED, null, "Internal server error");
		}


		return new RestResponse(RestResponse.SUCCESS, recruiterData);
	}


	private void addCustomStatusCandidatesInReport(Map<String, Object> countResponse, Date startDate, Date endDate,
			String hrEmail, String positionCode) {


		List<RoundCandidate> roundCandidateList = roundCandidateService.getRoundCandidateByPositionCodeAndSourcedBy(positionCode,hrEmail,startDate,endDate);
		List<BoardCustomStatus> customStatus = boardCustomStatusService.findAll();
		List<RoundCandidate> customList = new ArrayList<>();

		for (RoundCandidate roundCandidate : roundCandidateList) {

			for (BoardCustomStatus boardCustomStatus : customStatus) {

				if(roundCandidate.getStatus().equalsIgnoreCase(boardCustomStatus.getStatusKey())){
					customList.add(roundCandidate);
				}
			}

		}

		if(customStatus.size()==0)
			return;





		Integer [] arr = new Integer[customStatus.size()];
		ArrayList<Integer> myList= new ArrayList<>(Arrays.asList(arr));
		Collections.fill(myList, 0);//fills all entries with 0

		for(int i=0; i<roundCandidateList.size(); i++){

			for (int k=0; k < customStatus.size(); k++) {

				if(roundCandidateList.get(i).getStatus().equalsIgnoreCase(customStatus.get(k).getStatusKey())){

					int value = myList.get(k).intValue();
					int newValue = value + 1;
					myList.set(k, newValue);
				}

			}

		}


		for (int k=0; k < customStatus.size(); k++) {

			countResponse.put(customStatus.get(k).getStatusKey(), myList.get(k));
		}

	}

	public RestResponse getCustomReportOfEachRecruiter(ReportDropdownDTO reportDropdownDTO) {


		if(reportDropdownDTO.getStartDate()==null || reportDropdownDTO.getEndDate()==null)
			return new RestResponse(RestResponse.FAILED, null, "Start Date and End Date Both are required !!");
		FinalCustomRecruiterAndClientDTO response = new FinalCustomRecruiterAndClientDTO();
		List<CustomRecruiterAndClientDTO> responseData = new ArrayList<>();
		List<String> headerData = new ArrayList<>();
		List<User> users = new ArrayList<>();
		List<User> allUsers = userRepository.findAll();

		for (User user : allUsers) {

			if(user.getAccountStatus())
				users.add(user);
		}

		try{		 
			String clientSubmission = "Client Submission";
			String interview = "Interview";
			String feedbackPending = "FeedbackPending";
			String round1select = "Round1select";
			String round2select = "Round2select";
			String round1reject = "Round1reject";
			String round2reject = "Round2reject";
			String selected = BoardStatus.Selected.getDisplayName();
			String rejected = BoardStatus.Rejected.getDisplayName();

			List<String> statusList = new ArrayList<>();
			statusList.add(feedbackPending);
			statusList.add(round1select);
			statusList.add(round2select);
			statusList.add(round1reject);
			statusList.add(round2reject);
			statusList.add(selected);
			statusList.add(rejected);

			Date startDate = reportDropdownDTO.getStartDate();
			Date endDate = reportDropdownDTO.getEndDate();

			if(users==null || users.size()==0)
				return new RestResponse(RestResponse.FAILED, null, "No Data Found !!");

			for (User user : users) {

				/*	Set<BigInteger> clientSubmissions = positionCandidateDataRepository.getCandidatesByUserAndToStage("InProgress",user.getUserId(), clientSubmission, startDate, endDate);
				Set<Long> clientInterviewsScheduled = findTodaysScheduleByOwner(startDate, endDate, user.getEmail()); //interviewScheduleService.findTodaysScheduleByOwner( startDate, endDate, user.getEmail());	 
				Set<Long> clientInterviewsHappen = roundCandidateService.findCandidateIdsBySourcebyAndStatusBetweenDate(user.getEmail(), statusList,startDate, endDate);
				Set<Long>  selectedCandidate = roundCandidateService.findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(user.getEmail(),selected ,startDate, endDate);*/


				Set<BigInteger> clientSubmissions = positionCandidateDataRepository.getCandidatesByUserAndToStage("InProgress",user.getUserId(), clientSubmission, startDate, endDate);
				Set<Long> clientInterviewsScheduled = findTodaysScheduleByOwner(startDate, endDate, user.getEmail()); //interviewScheduleService.findTodaysScheduleByOwner( startDate, endDate, user.getEmail());	 
				Set<Long> clientInterviewsHappen = positionCandidateDataService.findCandidateIdsBySourcebyAndStatusBetweenDate(interview,user.getUserId(), statusList,startDate, endDate);
				Set<Long>  selectedCandidate = positionCandidateDataService.findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(interview,user.getUserId(),selected ,startDate, endDate);


				CustomRecruiterAndClientDTO recruiterRes  = new CustomRecruiterAndClientDTO();

				recruiterRes.setRecruiterName(user.getName());
				recruiterRes.setClientSubmissions(String.valueOf(clientSubmissions.size()));
				recruiterRes.setClientInterviewsScheduled(String.valueOf(clientInterviewsScheduled.size()));
				recruiterRes.setClientInterviewsHappen(String.valueOf(clientInterviewsHappen.size()));
				recruiterRes.setSelected(String.valueOf(selectedCandidate.size()));

				responseData.add(recruiterRes);				
			}

			headerData.add("Recruiter Name");headerData.add("Client Submissions");headerData.add("Client Interviews Scheduled");
			headerData.add("Client Interviews Happened");headerData.add("Selected");

			response.setHeaderData(headerData);
			response.setResponseData(responseData);

		}catch(Exception e){
			logger.error(e.getMessage()+"Error Occured in list "+e.toString());
			return new RestResponse(RestResponse.FAILED, null, "Internal server error");
		}

		return new RestResponse(RestResponse.SUCCESS, response, "Get Recruiter Report Data Successfully !!");
	}

	private Set<Long> findTodaysScheduleByOwner(Date startDate, Date endDate, String email) {

		Set<Long> linkedHashSet = new LinkedHashSet<>();
		List<InterviewSchedule> schedules =  interviewScheduleRepository.findTodaysScheduleByOwner(startDate, endDate,email);

		if(schedules!=null && schedules.size()>0){
			for (InterviewSchedule interviewSchedule : schedules) {
				linkedHashSet.add(interviewSchedule.getId());
			}
		}

		return linkedHashSet;
	}

	public RestResponse getCustomReportOfEachClient(ReportDropdownDTO reportDropdownDTO) {


		if(reportDropdownDTO.getStartDate()==null || reportDropdownDTO.getEndDate()==null)
			return new RestResponse(RestResponse.FAILED, null, "Start Date and End Date Both are required !!");

		FinalCustomRecruiterAndClientDTO response = new FinalCustomRecruiterAndClientDTO();
		List<CustomRecruiterAndClientDTO> responseData = new ArrayList<>();
		List<String> headerData = new ArrayList<>();
		List<Client> clients = new ArrayList<>();
		List<Client> allClients = clientRepository.findAll();

		for (Client client : allClients) {

			if(client.getStatus().equalsIgnoreCase("Active"))
				clients.add(client);
		}

		String clientSubmission = "Client Submission";
		String interview = "Interview";
		String feedbackPending = "FeedbackPending";
		String round1select = "Round1select";
		String round2select = "Round2select";
		String round1reject = "Round1reject";
		String round2reject = "Round2reject";
		String selected = BoardStatus.Selected.getDisplayName();
		String rejected = BoardStatus.Rejected.getDisplayName();

		List<String> statusList = new ArrayList<>();
		statusList.add(feedbackPending);
		statusList.add(round1select);
		statusList.add(round2select);
		statusList.add(round1reject);
		statusList.add(round2reject);
		statusList.add(selected);
		statusList.add(rejected);


		if(clients==null || clients.size()==0)
			return new RestResponse(RestResponse.FAILED, null, "No Data Found !!");
		try{

			Date startDate = reportDropdownDTO.getStartDate();
			Date endDate = reportDropdownDTO.getEndDate();

			for (Client client : clients) {
				List<Long> positionNameList = new ArrayList<>();
				Set<BigInteger> clientSubmissions = positionCandidateDataRepository.getCandidatesByClientAndToStage("InProgress",client.getId(), clientSubmission, startDate, endDate);
				Set<Long> clientInterviewsScheduled = interviewScheduleRepository.findTodaysScheduleByClient(startDate, endDate, client.getClientName()); 

				List<Position> positionList = positionRepository.findByClient(client);
				for (Position position : positionList) {
					positionNameList.add(position.getId());
				}

				Set<Long> clientInterviewsHappen = new LinkedHashSet<>();
				Set<Long> selectedCandidate = new LinkedHashSet<>();

				if(positionNameList.size()>0)
					clientInterviewsHappen = positionCandidateDataService.findCandidateIdsByClientsbyAndStatusBetweenDate(positionNameList, statusList,startDate, endDate,interview);

				if(positionNameList.size()>0)
					selectedCandidate = positionCandidateDataService.findCandidateIdsByClientbyAndSelectedStatusBetweenDate(positionNameList,selected ,startDate, endDate,interview);
				CustomRecruiterAndClientDTO recruiterRes  = new CustomRecruiterAndClientDTO();

				recruiterRes.setClientName(client.getClientName());
				recruiterRes.setClientSubmissions(String.valueOf(clientSubmissions.size()));
				recruiterRes.setClientInterviewsScheduled(String.valueOf(clientInterviewsScheduled.size()));
				recruiterRes.setClientInterviewsHappen(String.valueOf(clientInterviewsHappen.size()));
				recruiterRes.setSelected(String.valueOf(selectedCandidate.size()));

				responseData.add(recruiterRes);	
			}

			headerData.add("Client Name");headerData.add("Client Submissions");headerData.add("Client Interviews Scheduled");
			headerData.add("Client Interviews Happened");headerData.add("Selected");

			response.setHeaderData(headerData);
			response.setResponseData(responseData);

		}catch(Exception e){
			logger.error(e.getMessage()+"Error Occured in list "+e.toString());
			return new RestResponse(RestResponse.FAILED, null, "Internal server error");
		}
		return new RestResponse(RestResponse.SUCCESS, response, "Get Recruiter Report Data Successfully !!");
	}

	public File createExcelFileForEachRecruiter(ReportDropdownDTO reportDropdownDTO) {

		List<User> users = new ArrayList<>();
		List<User> allUsers = userRepository.findAll();

		for (User user : allUsers) {

			if(user.getAccountStatus())
				users.add(user);
		}
		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 6;
		int sheetRowSize = users.size();
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];
		try{		 
			String clientSubmission = "Client Submission";
			String interview = "Interview";
			String feedbackPending = "FeedbackPending";
			String round1select = "Round1select";
			String round2select = "Round2select";
			String round1reject = "Round1reject";
			String round2reject = "Round2reject";
			String selected = BoardStatus.Selected.getDisplayName();
			String rejected = BoardStatus.Rejected.getDisplayName();

			List<String> statusList = new ArrayList<>();
			statusList.add(feedbackPending);
			statusList.add(round1select);
			statusList.add(round2select);
			statusList.add(round1reject);
			statusList.add(round2reject);
			statusList.add(selected);
			statusList.add(rejected);

			Date startDate = reportDropdownDTO.getStartDate();
			Date endDate = reportDropdownDTO.getEndDate();

			// Header
			header[0] = "Sl No";
			header[1] = "Recruiter Name";
			header[2] = "Client Submissions";
			header[3] = "Client Interviews Scheduled";
			header[4] = "Client Interviews Happened";
			header[5] = "Selected";

			int row = 0;

			for (User user : users) {

				Set<BigInteger> clientSubmissions = positionCandidateDataRepository.getCandidatesByUserAndToStage("InProgress",user.getUserId(), clientSubmission, startDate, endDate);
				Set<Long> clientInterviewsScheduled = findTodaysScheduleByOwner(startDate, endDate, user.getEmail()); //interviewScheduleService.findTodaysScheduleByOwner( startDate, endDate, user.getEmail());	 
				Set<Long> clientInterviewsHappen = positionCandidateDataService.findCandidateIdsBySourcebyAndStatusBetweenDate(interview,user.getUserId(), statusList,startDate, endDate);
				Set<Long>  selectedCandidate = positionCandidateDataService.findCandidateIdsBySourcebyAndSelectedStatusBetweenDate(interview,user.getUserId(),selected ,startDate, endDate);

				data[row][0] = datarowCount + 1;
				data[row][1] = user.getName();
				data[row][2] = String.valueOf(clientSubmissions.size());
				data[row][3] = String.valueOf(clientInterviewsScheduled.size());
				data[row][4] = String.valueOf(clientInterviewsHappen.size());
				data[row][5] = String.valueOf(selectedCandidate.size());
				row++;
				datarowCount++;

			}


			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
			String startDate1 = formatter.format(startDate);
			String endDate1 = formatter.format(endDate);

			String fullfileName = "RecruiterProductivity" + "_" + startDate1 + "_" + "to_" + endDate1;

			excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);


		}catch(Exception e){
			e.printStackTrace();
		}

		return excelFile;
	}

	public File createExcelFileForEachClient(ReportDropdownDTO reportDropdownDTO) {


		List<Client> clients = new ArrayList<>();
		List<Client> allClients = clientRepository.findAll();

		for (Client client : allClients) {

			if(client.getStatus().equalsIgnoreCase("Active"))
				clients.add(client);
		}
		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 6;
		int sheetRowSize = clients.size();
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];
		try{		 
			String clientSubmission = "Client Submission";
			String interview = "Interview";
			String feedbackPending = "FeedbackPending";
			String round1select = "Round1select";
			String round2select = "Round2select";
			String round1reject = "Round1reject";
			String round2reject = "Round2reject";
			String selected = BoardStatus.Selected.getDisplayName();
			String rejected = BoardStatus.Rejected.getDisplayName();

			List<String> statusList = new ArrayList<>();
			statusList.add(feedbackPending);
			statusList.add(round1select);
			statusList.add(round2select);
			statusList.add(round1reject);
			statusList.add(round2reject);
			statusList.add(selected);
			statusList.add(rejected);

			Date startDate = reportDropdownDTO.getStartDate();
			Date endDate = reportDropdownDTO.getEndDate();

			// Header
			header[0] = "Sl No";
			header[1] = "Client Name";
			header[2] = "Client Submissions";
			header[3] = "Client Interviews Scheduled";
			header[4] = "Client Interviews Happened";
			header[5] = "Selected";

			int row = 0;

			for (Client client : clients) {

				List<Long> positionNameList = new ArrayList<>();
				Set<BigInteger> clientSubmissions = positionCandidateDataRepository.getCandidatesByClientAndToStage("InProgress",client.getId(), clientSubmission, startDate, endDate);
				Set<Long> clientInterviewsScheduled = interviewScheduleRepository.findTodaysScheduleByClient(startDate, endDate, client.getClientName()); 

				List<Position> positionList = positionRepository.findByClient(client);
				for (Position position : positionList) {
					positionNameList.add(position.getId());
				}

				Set<Long> clientInterviewsHappen = new LinkedHashSet<>();
				Set<Long> selectedCandidate = new LinkedHashSet<>();

				if(positionNameList.size()>0)
					clientInterviewsHappen = positionCandidateDataService.findCandidateIdsByClientsbyAndStatusBetweenDate(positionNameList, statusList,startDate, endDate,interview);

				if(positionNameList.size()>0)
					selectedCandidate = positionCandidateDataService.findCandidateIdsByClientbyAndSelectedStatusBetweenDate(positionNameList,selected ,startDate, endDate,interview);

				data[row][0] = datarowCount + 1;
				data[row][1] = client.getClientName();
				data[row][2] = String.valueOf(clientSubmissions.size());
				data[row][3] = String.valueOf(clientInterviewsScheduled.size());
				data[row][4] = String.valueOf(clientInterviewsHappen.size());
				data[row][5] = String.valueOf(selectedCandidate.size());
				row++;
				datarowCount++;

			}


			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
			String startDate1 = formatter.format(startDate);
			String endDate1 = formatter.format(endDate);

			String fullfileName = "ClientProductivity" + "_" + startDate1 + "_" + "to_" + endDate1;

			excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);


		}catch(Exception e){
			e.printStackTrace();
		}

		return excelFile;
	}


	public File getClientStatusReportExcel(int interval) {

		List<Map<String, Object>> report = getClientStatusReport(interval);

		File excelFile = null;
		int datarowCount = 0;
		int sheetColumnSize = 11;
		int sheetRowSize = report.size();
		Object[][] data = new Object[sheetRowSize][sheetColumnSize];
		String[] header = new String[sheetColumnSize];

		try{
			// Header
			header[0] = "Sl No";
			header[1] = "Client Name";
			header[2] = "Added Date";
			header[3] = "Last Modified";
			header[4] = "No Of Positions";
			header[5] = "Total Positions";
			header[6] = "Active";
			header[7] = "Closed";
			header[8] = "On-hold";
			header[9] = "Stop Sourcing";
			header[10] = "Location";

			int row = 0;

			for (Map<String, Object> dataArray : report) {



				data[row][0] = String.valueOf(row+1);
				data[row][1] = String.valueOf(dataArray.get("name"));
				data[row][2] = String.valueOf(dataArray.get("createdDate"));
				data[row][3] = String.valueOf(dataArray.get("modificationDate"));
				data[row][4] = String.valueOf(dataArray.get("totalPositions"));
				data[row][5] = String.valueOf(dataArray.get("totalOpenings"));
				data[row][6] = String.valueOf(dataArray.get("active"));
				data[row][7] = String.valueOf(dataArray.get("closed"));
				data[row][8] = String.valueOf(dataArray.get("onhold"));
				data[row][9] = String.valueOf(dataArray.get("stoppedsourcing"));
				data[row][10] = String.valueOf(dataArray.get("location"));

				row++;
				datarowCount++;

			}

			String fullfileName = "Client_Status_Report" + "_"+interval+" "+"Months"+"_Before ";

			excelFile = importExportService.resultSetToExcelExport(header, data, fullfileName, null);

		}catch(Exception e){
			e.printStackTrace();
		}

		return excelFile;
	}

	public RestResponse positionwiseCandidateOfferLetterStatusReport(Date startReportDate, Date endReportDate) {

		String userRole = userService.getLoggedInUserObject().getUserRole().getRoleName();

		Set<String> positionNameList = new HashSet<>();
		OfferLetterReportDTO reportDto = new OfferLetterReportDTO();
		List<OfferLetterPositionDTO> positionDtoList = new ArrayList<>();

		int totalRolloutCount = 0;
		int totalPositionCount = 0;
		int totalApprovedCount = 0;
		int totalRejectedCount = 0;
		int totalPendingCount = 0;
		int totalOfferLetterGeneration = 0;

		List<OfferLetterApprovals>	totalRollout = new ArrayList<>();
		try{
			if(userRole.equalsIgnoreCase("Super Admin")){
				totalRollout = offerLetterApprovalsService.findAllByDate(startReportDate,endReportDate);							
			}else{
				totalRollout = offerLetterApprovalsService.getApprovalListByRequestSenderAndDate(userService.getLoggedInUserObject().getUserId(),startReportDate,endReportDate);
			}

			totalRolloutCount = totalRollout.size();

			for (OfferLetterApprovals offerLetterApprovals : totalRollout) {

				positionNameList.add(offerLetterApprovals.getPosition_code());

				if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.ACCEPTED)){
					totalApprovedCount = totalApprovedCount + 1;
				}else if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.REJECTED)){
					totalRejectedCount = totalRejectedCount + 1;
				}else if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.PENDING_STATUS)){
					totalPendingCount = totalPendingCount + 1;
				}

				OfferLetterForCandidate offerLetterForCandidate = offerLetterForCandidateRepository.findDetailsByApprovalId(offerLetterApprovals.getId());
				if(offerLetterForCandidate!=null)
					totalOfferLetterGeneration = totalOfferLetterGeneration + 1;

			}

			totalPositionCount = positionNameList.size();


			for (String positionCode : positionNameList) {

				OfferLetterPositionDTO positionReport = new OfferLetterPositionDTO();
				List<OfferCandidateRepotDTO> candidateList = new ArrayList<>();

				int approvedCount = 0;
				int rejectedCount = 0;
				int pendingCount = 0;
				int offerLetterGenerationCount = 0;

				List<OfferLetterApprovals> rollout = new ArrayList<>();

				if(userRole.equalsIgnoreCase("Super Admin")){
					rollout = offerLetterApprovalsService.findAllByPositionCode(startReportDate,endReportDate,positionCode);							
				}else{
					rollout = offerLetterApprovalsService.findAllByPositionCode(userService.getLoggedInUserObject().getUserId(),startReportDate,endReportDate,positionCode);
				}




				Position position = positionService.getPositionByCode(positionCode);

				for (OfferLetterApprovals offerLetterApprovals : rollout) {

					if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.ACCEPTED)){
						approvedCount = approvedCount + 1;
					}else if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.REJECTED)){
						rejectedCount = rejectedCount + 1;
					}else if(offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.PENDING_STATUS)){
						pendingCount = pendingCount + 1;
					}

					OfferLetterForCandidate offerLetterForCandidate = offerLetterForCandidateRepository.findDetailsByApprovalId(offerLetterApprovals.getId());
					if(offerLetterForCandidate!=null)
						offerLetterGenerationCount = offerLetterGenerationCount + 1;

					Candidate candidate = candidateService.findOne(offerLetterApprovals.getCandidate_id());

					OfferCandidateRepotDTO canData = new OfferCandidateRepotDTO();

					canData.setCandidateEmail(candidate.getEmail());
					canData.setCandidateId(candidate.getCid());
					canData.setCandidateName(candidate.getFullName());
					canData.setRejectionReason(offerLetterApprovals.getReject_reason());
					canData.setOfferStatus(offerLetterApprovals.getApproval_status());

					if(offerLetterForCandidate!=null && offerLetterForCandidate.getField1()!=null && offerLetterForCandidate.getField1().trim().equalsIgnoreCase("yes"))
						canData.setIsOfferLetterSent(offerLetterForCandidate.getField1());
					else
						canData.setIsOfferLetterSent("no");

					candidateList.add(canData);
				}

				positionReport.setApprovedCount(approvedCount);
				positionReport.setPendingCount(pendingCount);
				positionReport.setPositionId(position.getId());
				positionReport.setPositionName(position.getTitle());
				positionReport.setRejectedCount(rejectedCount);
				positionReport.setOfferLetterGenerationCount(offerLetterGenerationCount);
				positionReport.setCandidateList(candidateList);

				positionDtoList.add(positionReport);





			}

			reportDto.setOfferLetterPositionReport(positionDtoList);
			reportDto.setTotalAcceptOfferLetter(totalApprovedCount);
			reportDto.setTotalPendingOfferLetter(totalPendingCount);
			reportDto.setTotalPositionCount(totalPositionCount);
			reportDto.setTotalRejectOfferLetter(totalRejectedCount);
			reportDto.setTotalRolloutCount(totalRolloutCount);
			reportDto.setTotalOfferLetterGeneration(totalOfferLetterGeneration);

			return new RestResponse(RestResponse.SUCCESS, reportDto, "Success");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal server error");
		}

	}



	public RestResponse offerLetterStatusWiseLinkReport(Date startReportDate, Date endReportDate, String status,Pageable pageable) {

		String userRole = userService.getLoggedInUserObject().getUserRole().getRoleName();

		ArrayList<OfferLetterStatusWiseReport> reportData = new ArrayList<>();

		List<OfferLetterApprovals>	totalRollout = new ArrayList<>();
		try{
			if(userRole.equalsIgnoreCase("Super Admin")){
				if(status.equalsIgnoreCase("rollout") || status.equalsIgnoreCase("offerLetterGenerated")){
					totalRollout = offerLetterApprovalsService.findAllByDate(startReportDate,endReportDate);	
				}else{
					totalRollout = offerLetterApprovalsService.findAllByDateAndStatus(startReportDate,endReportDate,status);
				}
			}else{
				if(status.equalsIgnoreCase("rollout") || status.equalsIgnoreCase("offerLetterGenerated")){
					totalRollout = offerLetterApprovalsService.getApprovalListByRequestSenderAndDate(userService.getLoggedInUserObject().getUserId(),startReportDate,endReportDate);
				}else{
					totalRollout = offerLetterApprovalsService.getApprovalListByRequestSenderAndDateAndStatus(userService.getLoggedInUserObject().getUserId(),startReportDate,endReportDate,status);
				}
			}

			for (OfferLetterApprovals offerLetterApprovals : totalRollout) {

				if(status.equalsIgnoreCase("offerLetterGenerated"))	{
					OfferLetterForCandidate offerLetterForCandidate = offerLetterForCandidateRepository.findDetailsByApprovalId(offerLetterApprovals.getId());
					if(offerLetterForCandidate!=null){
						
						OfferLetterStatusWiseReport report = new OfferLetterStatusWiseReport();
						CandidateDuplicateDTO candidate = new CandidateDuplicateDTO();
						candidate = candidate.copy(candidateService.getCandidateById(offerLetterApprovals.getCandidate_id()));
						Position position = positionService.getOneByPositionID(offerLetterApprovals.getPosition_id());
						String approval_status = offerLetterApprovals.getApproval_status();
						
						report.setApprovalStatus(approval_status);
						report.setCandidate(candidate);
						report.setPosition(position);
						report.setApproval_id(offerLetterApprovals.getId());
						
						reportData.add(report);
					}
						
				}else{
					
					OfferLetterStatusWiseReport report = new OfferLetterStatusWiseReport();
					CandidateDuplicateDTO candidate = new CandidateDuplicateDTO();
					candidate = candidate.copy(candidateService.getCandidateById(offerLetterApprovals.getCandidate_id()));
					Position position = positionService.getOneByPositionID(offerLetterApprovals.getPosition_id());
					String approval_status = offerLetterApprovals.getApproval_status();
					
					report.setApprovalStatus(approval_status);
					report.setCandidate(candidate);
					report.setPosition(position);
					report.setApproval_id(offerLetterApprovals.getId());
					
					reportData.add(report);
				}
			}

			int start = pageable.getOffset();
			int end = (start + pageable.getPageSize()) > reportData.size() ? reportData.size() : (start + pageable.getPageSize());
			final Page<OfferLetterStatusWiseReport> reportList = new PageImpl<OfferLetterStatusWiseReport>(reportData.subList(start, end), pageable,
					reportData.size());	
			
			return new RestResponse(RestResponse.SUCCESS, reportList, "Success");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal server error");
		}

	}



}
