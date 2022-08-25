package com.bbytes.recruiz.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bbytes.recruiz.enums.PerformanceReportTimePeriod;
import com.bbytes.recruiz.exception.RecruizException;

public class DateUtil {

	public final static String DATE_FORMAT = "dd/MM/YYYY";
	
	public final static String DATE_FORMAT_MONTH = "dd-MMM-YYYY";

	public final static String DATE_TIME_FORMAT = "E, dd MMM yyyy HH:mm:ss Z";

	public static final String DATE_TIME = "dd-M-yyyy hh:mm a";

	public static final String[] DATE_FORMAT_LIST = { "yyyy-MM-dd", "yyyy-dd-MM", "dd-MM-yyyy", "MM-dd-yyyy",
			"dd-MM-yy", "MM-dd-yy", "yyyy/MM/dd", "yyyy/dd/MM", "dd/MM/yyyy", "MM/dd/yyyy", "dd/MM/yy", "MM/dd/yy",
			"yyyy:MM:dd", "yyyyMMdd", "dd-MMM-yy", "MMM-dd-yy", "d/M/yyyy", "M/d/yyyy", "yy/MM/dd", "yy-MM-dd",
			"d-M-yyyy", "M-d-yyyy", "d-M-yy", "M-d-yy", "d/M/yy", "M/d/yy", "dd-MMM-yyyy", "MMM-dd-yyyy", "dd/MMM/yyyy",
			"MMM/dd/yyyy", "dd/MMM/yy", "MMM/dd/yy", "dd MMM yy", "dd MMM yyyy" };

	public static Date parseDateTime(String timeStamp) {
		if (timeStamp == null)
			return null;

		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
		DateTime dateTime = fmt.parseDateTime(timeStamp);
		return dateTime.toDate();
	}

	public static Date parseDate(String date) {
		if (date == null || date.isEmpty())
			return null;
		// checking all possible date formats for date string
		for (String parse : DATE_FORMAT_LIST) {
			try {
				DateTimeFormatter fmt = DateTimeFormat.forPattern(parse);
				DateTime dateTime = fmt.parseDateTime(date);
				return dateTime.toDate();
			} catch (IllegalArgumentException iae) {

			}
		}
		return null;
	}

	public static boolean isDate(String text) {
		if (text == null || text.isEmpty())
			return false;
		for (String parse : DATE_FORMAT_LIST) {
			try {
				DateTimeFormatter dtf = DateTimeFormat.forPattern(parse);
				dtf.parseDateTime(text);
				return true;
			} catch (IllegalArgumentException iae) {
			}
		}
		return false;
	}

	public static String formateDate(Date date) {

		if (date == null)
			return null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
		return simpleDateFormat.format(date);
	}
	
	public static String formateDate(Date date,String dateFormat) {

		if (date == null)
			return null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		return simpleDateFormat.format(date);
	}

	public static String formateDateAndTime(Date date) {
		if (date == null)
			return null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME);
		return simpleDateFormat.format(date);
	}

	public static int getDifferenceDateDay(Date past, Date today) {
		LocalDate todaysDate = new DateTime(today).toLocalDate();
		LocalDate pastDate = new DateTime(past).toLocalDate();
		return Days.daysBetween(pastDate, todaysDate).getDays();
	}

	public static Date[] calculateTimePeriod(String timePeriod, Date startDate, Date endDate) throws RecruizException {

		Integer timePeriodValue;
		Date[] startEndDates = new Date[2];
		// if timeperiod is not provided from UI it will set as 'Last Month'
		if (timePeriod == null || timePeriod.isEmpty()) {
			timePeriodValue = PerformanceReportTimePeriod.Last_12_Months.getDays();
		} else {
			timePeriodValue = PerformanceReportTimePeriod.valueOf(timePeriod).getDays();
		}
		// custom time period - directing taking startDate and endDate from
		// request parameter and no of days for custom would be -1
		if (PerformanceReportTimePeriod.Custom.equals(timePeriod)) {
			if (null == startDate || null == endDate) {
				throw new RecruizException(ErrorHandler.DATE_NOT_PRESENT, ErrorHandler.NO_DATE);
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

	public static String getTimeZoneOffsetValue(String timeZoneString) {
		ZoneId zone = ZoneId.of(timeZoneString);
		LocalDateTime dt = LocalDateTime.now();
		ZonedDateTime zdt = dt.atZone(zone);
		ZoneOffset zos = zdt.getOffset();

		// replace Z to +00:00
		String offset = zos.getId().replaceAll("Z", "+00:00");
		return offset;
	}

}