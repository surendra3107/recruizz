package com.bbytes.recruiz.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.Metrics;
import com.bbytes.recruiz.rest.dto.models.ResultSetData;
import com.bbytes.recruiz.utils.ArrayUtil;
import com.fdsapi.ResultSetConverter;
import com.google.inject.internal.Lists;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.render.JsonRenderer;

/**
 * Return the metric Data that gives for google chart
 * https://github.com/angular-google-chart/angular-google-chart
 * 
 * @author - Akshay
 */
@Service
public class MetricsService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private VelocityEngine templateEngine;

	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public Metrics getOverallCandidateGenderMix() throws RecruizException {
		String template = "candidate_overall_gender_mix_sql.vm";
		String sql = getSQL(template, null);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				try {
					DataTable dataTable = getDataTable(result, "Candidate", "Candidate Gender Mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Gender mix");
		return metric;
	}

	public Metrics getDaysSincePostionOpened(String status) throws RecruizException {
		String template = "no_of_days_since_position_opened_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("status", status);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);

				try {
					DataTable dataTable = getDataTable(result, "Position", "Opened Since");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position open since");
		return metric;
	}

	public Metrics perPositionDaysSincePostionOpened(String positionCode) throws RecruizException {
		String template = "per_position_no_of_days_since_position_opened_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {
					DataTable dataTable = getDataTable(result, "Position", "Opened Since");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position open since");
		return metric;
	}

	public Metrics clientwiseDaysSincePostionOpened(String clientId) throws RecruizException {

		String template = "clientwise_no_of_days_since_position_opened_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {
					DataTable dataTable = getDataTable(result, "Position", "Opened Since");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position open since");
		return metric;
	}

	public Metrics getDaysSincePostionClosed(String status) throws RecruizException {
		String template = "no_of_days_since_position_closed_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("status", status);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);

				try {
					DataTable dataTable = getDataTable(result, "Position", "Closed Since");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position closed since");
		return metric;
	}

	public Metrics perPositionDaysSincePostionClosed(String positionCode) throws RecruizException {
		String template = "per_position_no_of_days_since_position_closed_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {
					String label = null;
					if (Integer.parseInt(result.getResultSet()[0][1].toString()) < 0)
						label = "Days since closure";
					else
						label = "Days to closure";
					DataTable dataTable = getDataTable(result, "Position", label);
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position closure deadline");
		return metric;
	}

	public Metrics clientwiseDaysSincePostionClosed(String clientId) throws RecruizException {
		String template = "clientwise_no_of_days_since_position_closed_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {
					DataTable dataTable = getDataTable(result, "Position", "Closed Since");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Position closure deadline");
		return metric;
	}

	public Metrics perPositionGenderMix(String positionCode) throws RecruizException {

		String template = "per_position_gender_mix_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				try {
					DataTable dataTable = getDataTable(result, "Position", "Gender Mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
					metric.setYLabel(result.getMetaData()[1]);
					metric.setXLabel(result.getMetaData()[0]);
					metric.setMetaData(result.getMetaData());
					metric.setRawData(result.getResultSet());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return metric;
			}
		});

		metric.setTitle("Gender Mix");
		return metric;
	}

	public Metrics perPositionSourcedbyRecruiterMix(String positionCode) throws RecruizException {

		String template = "per_postion_candidate_sourced_by_recruiter.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				try {
					DataTable dataTable = getDataTable(result, "Position", "Candidate sourced by recruiter");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
					metric.setYLabel(result.getMetaData()[1]);
					metric.setXLabel(result.getMetaData()[0]);
					metric.setMetaData(result.getMetaData());
					metric.setRawData(result.getResultSet());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return metric;
			}
		});

		metric.setTitle("Candidate sourced by recruiter");
		return metric;
	}

	public Metrics overallPositionSourcedbyRecruiterMix() throws RecruizException, TypeMismatchException {
		String template = "position_overall_candidate_sourced_by_recruiter.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
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

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);

		metric.setTitle("Candidate sourcedby recruiter");
		return metric;
	}

	public Metrics clientwiseCandidateSourcedbyRecruiterMix(String clientId)
			throws RecruizException, TypeMismatchException {
		String template = "clientwise_overall_candidate_sourced_by_recruiter.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
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

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);
		metric.setMetaData(resultSetData.getColumns());
		metric.setRawData(resultSetData.getData());

		metric.setTitle("Candidate sourced by recruiter");
		return metric;
	}

	public Metrics perPositionInterviewSchedule(String positionCode) throws RecruizException {

		String template = "per_postion_interview_scheduled.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				try {
					DataTable dataTable = getDataTable(result, "Position", "Interview count");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
					metric.setYLabel(result.getMetaData()[1]);
					metric.setXLabel(result.getMetaData()[0]);
					metric.setMetaData(result.getMetaData());
					metric.setRawData(result.getResultSet());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return metric;
			}
		});

		metric.setTitle("Interview count");
		return metric;
	}

	public Metrics perPositionCandidateSourced(Date startDate, Date endDate, String aggrType, String positionCode)
			throws RecruizException, TypeMismatchException {
		String template = null;
		if (aggrType.equals("day")) {
			template = "per_day_per_position_candidate_sourced_sql.vm";
		} else {
			template = "per_month_per_position_candidate_sourced_sql.vm";
		}
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {

			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				try {
					DataTable dataTable = getDataTable(result, "Position", "New candidates sourced");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
					metric.setYLabel(result.getMetaData()[1]);
					metric.setXLabel(result.getMetaData()[0]);
					metric.setMetaData(result.getMetaData());
					metric.setRawData(result.getResultSet());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return metric;
			}
		});

		metric.setTitle("New candidates sourced");
		return metric;
	}

	public Metrics overallPositionCandidateSourced(Date startDate, Date endDate, String aggrType)
			throws RecruizException, TypeMismatchException {
		String template = null;
		if (aggrType.equals("day")) {
			template = "per_day_overall_position_candidate_sourced_sql.vm";
		} else {
			template = "per_month_overall_position_candidate_sourced_sql.vm";
		}
		Map<String, Object> templateModel = new HashMap<String, Object>();
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);

		metric.setTitle("New candidate sourced");
		return metric;
	}

	public Metrics clientwisePositionCandidateSourced(Date startDate, Date endDate, String aggrType, String clientId)
			throws RecruizException, TypeMismatchException {
		String template = null;
		if (aggrType.equals("day")) {
			template = "clientwise_per_day_candidate_sourced_sql.vm";
		} else {
			template = "clientwise_per_month_candidate_sourced_sql.vm";
		}
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("startDate", new java.sql.Timestamp(startDate.getTime()));
		templateModel.put("endDate", new java.sql.Timestamp(endDate.getTime()));
		templateModel.put("clientId", clientId);
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);
		metric.setMetaData(resultSetData.getColumns());
		metric.setRawData(resultSetData.getData());

		metric.setTitle("New candidate sourced");
		return metric;
	}

	public Metrics overallPositionGenderMix() throws RecruizException, TypeMismatchException {
		String template = "position_overall_gender_mix_sql.vm";
		String sql = getSQL(template, null);
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);

		metric.setTitle("Gender mix");
		return metric;
	}

	public Metrics clientwiseGenderMix(String clientId) throws RecruizException, TypeMismatchException {
		String template = "clientwise_overall_gender_mix_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);
		metric.setMetaData(resultSetData.getColumns());
		metric.setRawData(resultSetData.getData());

		metric.setTitle("Gender mix");
		return metric;
	}

	public Metrics overallPositionInterviewSchedule(String positionStatus)
			throws RecruizException, TypeMismatchException {
		String template = "position_overall_interview_scheduled_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("status", positionStatus);
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);

		metric.setTitle("Interview count");
		return metric;
	}

	public Metrics clientwisePositionInterviewSchedule(String clientId) throws RecruizException, TypeMismatchException {
		String template = "clientwise_overall_interview_scheduled_sql.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
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
				"Position Name");

		Metrics metric = new Metrics();
		metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
		metric.setXLabel(labelColumnName);
		metric.setYLabel(dataColumnName);
		metric.setMetaData(resultSetData.getColumns());
		metric.setRawData(resultSetData.getData());

		metric.setTitle("Interview count");
		return metric;
	}

	public Metrics overallPositionSoucingChannelMix() throws RecruizException {
		String template = "position_overall_sourcing_channels.vm";
		String sql = getSQL(template, null);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);

				try {

					DataTable dataTable = getDataTable(result, "Position", "Sourcing channel mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Sourcing channel mix");
		return metric;
	}

	public Metrics clientwiseSoucingChannelMix(String clientId) throws RecruizException {
		String template = "clientwise_overall_sourcing_channels.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Position", "Sourcing channel mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Sourcing channel mix");
		return metric;
	}

	public Metrics perPositionSoucingChannelMix(String positionCode) throws RecruizException {
		String template = "per_postion_sourcing_channels.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Position", "Sourcing channel mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Sourcing channel mix");
		return metric;
	}

	public Metrics overallPositionCandidateRejectionMix() throws RecruizException {
		String template = "position_overall_candidate_rejection_reason.vm";
		String sql = getSQL(template, null);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);

				try {

					DataTable dataTable = getDataTable(result, "Position", "Candidate rejection mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Candidate Rejection Mix");
		return metric;
	}

	public Metrics clientwiseCandidateRejectionMix(String clientId) throws RecruizException {
		String template = "clientwise_overall_candidate_rejection_reason.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("clientId", clientId);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Position", "Candidate rejection mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Candidate rejection mix");
		return metric;
	}

	public Metrics perPositionCandadidateRejectionMix(String positionCode) throws RecruizException {
		String template = "per_postion_candidate_rejection_reason.vm";
		Map<String, Object> templateModel = new HashMap<String, Object>();
		templateModel.put("positionCode", positionCode);
		String sql = getSQL(template, templateModel);
		Metrics metric = jdbcTemplate.query(sql, new ResultSetExtractor<Metrics>() {
			@Override
			public Metrics extractData(ResultSet rs) throws SQLException, DataAccessException {
				ResultSetConverter result = new ResultSetConverter(rs);
				Metrics metric = new Metrics();
				metric.setYLabel(result.getMetaData()[1]);
				metric.setXLabel(result.getMetaData()[0]);
				metric.setMetaData(result.getMetaData());
				metric.setRawData(result.getResultSet());

				try {

					DataTable dataTable = getDataTable(result, "Position", "Candidate rejection mix");
					metric.setChartData(JsonRenderer.renderDataTable(dataTable, true, true).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return metric;
			}
		});

		metric.setTitle("Candidate rejection mix");
		return metric;
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
		String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
				"query-templates/metrics-query/" + template, "UTF-8", model);
		return sql;
	}

}
