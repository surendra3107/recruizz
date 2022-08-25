package com.bbytes.recruiz.repository.usage.stat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.ui.velocity.VelocityEngineUtils;

@Repository
public class UsageStatRepository {

    private static final Logger logger = LoggerFactory.getLogger(UsageStatRepository.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("tenant-usage-stat")
    private DataSource tenantUsagePatternDataSource;

    @Autowired
    private VelocityEngine templateEngine;

    @PostConstruct
    public void init() {
	jdbcTemplate = new JdbcTemplate(tenantUsagePatternDataSource);
    }

    // to create a table for given tenantid in usage database
    public void createTanantUsageTable(String tenantId) {
	String createTableStatement = "create table IF NOT EXISTS " + tenantId
		+ " (`user_email` VARCHAR(255) NOT NULL,  `name` VARCHAR(255) NOT NULL,"
		+ "`action` VARCHAR(255) NULL,  `time` DATETIME NULL,  `id` BIGINT(20) NOT NULL,"
		+ "PRIMARY KEY (`user_email`, `id`),`last_hit_gap` BIGINT(20) NOT NULL DEFAULT 0);";
	jdbcTemplate.execute(createTableStatement);
    }

    // to insert into usage table for given tenant
    public synchronized int insertIntoUsageTable(String tenant, String userEmail, String userName, String actionType) {

	String lastHitStatement = "SELECT IFNULL((select time from " + tenant
		+ " where Date(time) = Date(Now()) order by time desc Limit 1),null) time";

	Map<String, Object> val = jdbcTemplate.queryForMap(lastHitStatement);

	Date lastDate = (Date) val.get("time");
	if (lastDate == null) {
	    lastDate = new Date();
	}

	Long diffInSec = (new Date().getTime() - lastDate.getTime()) / 1000;

	String insertStatement = "INSERT INTO " + tenant
		+ " (`user_email`, `name`, `action`, `time`, `id`,`last_hit_gap`) VALUES (?,?,?,?,?,?)";

	int rowCount = jdbcTemplate.update(insertStatement,
		new Object[] { userEmail, userName, actionType, new Date(), System.nanoTime(), diffInSec });
	return rowCount;
    }

public Long toGetLoggedInHourForHourInterval(String tenant, String userEmail, Date startDate,Date endDate) {
	
	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String startTime = sdf.format(startDate);
	String endTime = sdf.format(endDate);
	String getLoggedInTimeInSec = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '" + startTime + "' AND '" + endTime
		+ "' order by time desc Limit 1),(select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '"
		+ startTime + "' AND '" + endTime + "' order by time asc Limit 1))) time_difference_in_sec";
	
	System.out.println(getLoggedInTimeInSec+"\n\n");

	Map<String, Object> val = jdbcTemplate.queryForMap(getLoggedInTimeInSec);

	Long loggedInSec = (Long) val.get("time_difference_in_sec");
	if (loggedInSec == null) {
	    loggedInSec = 0L;
	}

	return loggedInSec.longValue();
    }
    
    
    
    // to get total logged in time in sec
    public Long toGetLoggedInHourForGivenDate(String tenant, String userEmail, Date startDate,Date endDate) {
	
	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String startTime = sdf.format(startDate);
	String endTime = sdf.format(endDate);
	
	String midNightTime = sdf.format(removeTime(endDate));
	
	
	String getLoggedInTimeInSec = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '" + startTime + "' AND '" + midNightTime
		+ "' order by time desc Limit 1),(select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '"
		+ startTime + "' AND '" + midNightTime + "' order by time asc Limit 1))) time_difference_in_sec";

	String getLoggedInTimeInSecday2 = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '" + midNightTime + "' AND '" + endTime
		+ "' order by time desc Limit 1),(select time from " + tenant + " where user_email='"
		+ userEmail + "' AND  time between '"
		+ midNightTime + "' AND '" + endTime + "' order by time asc Limit 1))) time_difference_in_sec";

	
	System.out.println(getLoggedInTimeInSec+"\n\n");
	System.out.println(getLoggedInTimeInSecday2+"\n\n");

	Map<String, Object> val = jdbcTemplate.queryForMap(getLoggedInTimeInSec);
	Map<String, Object> val2 = jdbcTemplate.queryForMap(getLoggedInTimeInSecday2);
	

	Long loggedInSec = (Long) val.get("time_difference_in_sec");
	if (loggedInSec == null) {
	    loggedInSec = 0L;
	}
	
	Long loggedInSec2 = (Long) val2.get("time_difference_in_sec");
	if (loggedInSec2 != null) {
	    loggedInSec = loggedInSec + loggedInSec2;
	}

	return loggedInSec.longValue();
    }

    // to get total logged in time in sec
    public Long toGetTotalIdleTimeForInterval(String tenant, String userEmail, String intervalInMinute,
	    String idleTimeLimit) {

	String template = "user_idle_time_for_interval.vm";
	Map<String, Object> queryData = new HashMap<>();
	queryData.put("tableName", tenant);
	queryData.put("userEmail", userEmail);
	queryData.put("intervalInMinute", intervalInMinute);
	queryData.put("idleTimeout", idleTimeLimit);

	String query = getSQL(template, queryData);

	Map<String, Object> val = jdbcTemplate.queryForMap(query);
	BigDecimal loggedInSec = (BigDecimal) val.get("totalIdleTime");
	if (loggedInSec == null) {
	    return 0L;
	}

	return loggedInSec.longValue();
    }

    // to get total idle time for a day
    public Long getIdleTimeForADay(String tenant, String userEmail, Date startDate,Date endDate, String idleTimeLimit) {

	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String startTime = sdf.format(startDate);
	String endTime = sdf.format(endDate);
	
	String template = "total_idle_time_per_day.vm";
	Map<String, Object> queryData = new HashMap<>();
	queryData.put("tableName", tenant);
	queryData.put("userEmail", userEmail);
	queryData.put("startTime", startTime);
	queryData.put("endTime", endTime);
	queryData.put("idleTimeOut", idleTimeLimit);

	String query = getSQL(template, queryData);
	
	System.out.println("idleTime = " + query);

	Map<String, Object> val = jdbcTemplate.queryForMap(query);
	BigDecimal loggedInSec = (BigDecimal) val.get("totalIdleTime");
	if (loggedInSec == null) {
	    return 0L;
	}

	return loggedInSec.longValue();
    }

    // to get active user count for given date
    public Long getActiveUserCountForGivenDate(String tenant, Date date) {

	String query = "select (select count(distinct user_email) from " + tenant + " where date(time) >= '"
		+ new java.sql.Date(date.getTime()) + "') activeUserCount ";

	Map<String, Object> val = jdbcTemplate.queryForMap(query);

	Long userCount = (Long) val.get("activeUserCount");
	if (userCount == null) {
	    return 0L;
	}

	return userCount.longValue();
    }

    // to get total idle time for all user for a interval
    public Long getTotalIdleForAllUserForInterval(String tenant, String intervalInMinute, String idleTimeLimit) {

	String template = "all_user_idle_time_for_interval.vm";
	Map<String, Object> queryData = new HashMap<>();
	queryData.put("tableName", tenant);
	queryData.put("intervalInMinute", intervalInMinute);
	queryData.put("idleTimeout", idleTimeLimit);

	String query = getSQL(template, queryData);
	System.out.println(query);

	Map<String, Object> val = jdbcTemplate.queryForMap(query);
	BigDecimal loggedInSec = (BigDecimal) val.get("totalIdleTime");
	if (loggedInSec == null) {
	    return 0L;
	}
	return loggedInSec.longValue();
    }

 // to get total logged in time in sec
    public Long toGetLoggedInHourForHourIntervalForAllUser(String tenant, Date date,Date endDate) {

	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String currentTime = sdf.format(date);
	
	String endTime = sdf.format(endDate);

	String getLoggedInTimeInSec = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where "
		+ "  time between '" + currentTime + "' AND '" + endTime
		+ "' order by time desc Limit 1),(select time from " + tenant + " where " + "  time between '"
		+ currentTime + "' AND '" + endTime + "' order by time asc Limit 1))) time_difference_in_sec";

	System.out.println("usage Query :" + getLoggedInTimeInSec);

	Map<String, Object> val = jdbcTemplate.queryForMap(getLoggedInTimeInSec);

	Long loggedInSec = (Long) val.get("time_difference_in_sec");
	if (loggedInSec == null) {
	    return 0L;
	}

	return loggedInSec.longValue();
    }
    
    // to get total logged in time in sec
    public Long toGetLoggedInHourForGivenDateForAllUser(String tenant, Date date,Date endDate) {

	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String currentTime = sdf.format(date);
	
	String lastDate = sdf.format(endDate);
	String midnighttime = sdf.format(removeTime(endDate));

	String getLoggedInTimeInSec = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where "
		+ "  time between '" + currentTime + "' AND '" + midnighttime
		+ "' order by time desc Limit 1),(select time from " + tenant + " where " + "  time between '"
		+ currentTime + "' AND '" + midnighttime + "' order by time asc Limit 1))) time_difference_in_sec";

	System.out.println("usage Query :" + getLoggedInTimeInSec);

	Map<String, Object> val = jdbcTemplate.queryForMap(getLoggedInTimeInSec);

	Long loggedInSec = (Long) val.get("time_difference_in_sec");
	if (loggedInSec == null) {
	    loggedInSec = 0L;
	}

	Long lastLoggedInTime = 0L;

	String getLastLoggedInTimeInSec = "SELECT TIME_TO_SEC(TIMEDIFF((select time from " + tenant + " where "
		+ "  time between '" + midnighttime + "' AND '" + lastDate
		+ "' order by time desc Limit 1),(select time from " + tenant + " where " + "  time between '"
		+ midnighttime + "' AND '" + lastDate + "' order by time asc Limit 1))) time_difference_in_sec";

	System.out.println("usage Query :" + getLastLoggedInTimeInSec);

	Map<String, Object> lastVal = jdbcTemplate.queryForMap(getLastLoggedInTimeInSec);

	lastLoggedInTime = (Long) lastVal.get("time_difference_in_sec");
	if (lastLoggedInTime == null) {
	    lastLoggedInTime = 0L;
	}

	return loggedInSec.longValue() + lastLoggedInTime.longValue();
    }

    // to get total idle time for a day
    public Long getIdleTimeForADayForTenant(String tenant, String idleTimeLimit, String startDate, String endDate) {

	String template = "total_idle_time_per_day_tenant.vm";
	Map<String, Object> queryData = new HashMap<>();
	queryData.put("tableName", tenant);
	queryData.put("startDate", startDate);
	queryData.put("endDate", endDate);
	queryData.put("idleTimeOut", idleTimeLimit);

	String query = getSQL(template, queryData);

	System.out.println("Idle Query :" + query);

	Map<String, Object> val = jdbcTemplate.queryForMap(query);
	BigDecimal loggedInSec = (BigDecimal) val.get("totalIdleTime");
	if (loggedInSec == null) {
	    return 0L;
	}

	return loggedInSec.longValue();
    }

    private String getSQL(String template, Map<String, Object> model) {
	@SuppressWarnings("deprecation")
	String sql = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine,
		"query-templates/usage-query/" + template, "UTF-8", model);
	return sql;
    }

    public Date removeTime(Date date) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	cal.set(Calendar.HOUR_OF_DAY, 0);
	cal.set(Calendar.MINUTE, 0);
	cal.set(Calendar.SECOND, 0);
	cal.set(Calendar.MILLISECOND, 0);
	return cal.getTime();
    }

}
