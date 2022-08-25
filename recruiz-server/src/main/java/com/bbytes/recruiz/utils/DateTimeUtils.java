package com.bbytes.recruiz.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtils {
	
	public static final int MONDAY = 1;

	public static final int SUNDAY = 7;

	public static int longTimeDifference(long startCal, long endCal) {
		long diff = endCal - startCal;
		int min = (int) (diff / (1000 * 60 * 60));
		return min;
	}

	public static Date getDateFromString(String date) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MMM-yyyy");
		return formatter.parseDateTime(date).toDate();
	}

	public static Date getDate(String date, String formatPattern) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(formatPattern);
		return formatter.parseDateTime(date).toDate();
	}

	public static String getDateAsString(Date date, String pattern) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
		return formatter.print(new DateTime(date));
	}

	public static String getDateTimeAsString(Date date) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM d, yyyy hh:mm a");
		return formatter.print(new DateTime(date));
	}

	public static Date getCurrentDate() throws ParseException {
		return DateTime.now().toDate();
	}

	/**
	 * This will return array of dates today and date where noOfDays provided.
	 * (Ex. noOfDays is 7 then it will give today and today minus 7 days)
	 * 
	 * @param noOfDays
	 * @return
	 */
	public static Date[] getStartDateEndDate(Integer noOfDays) {
		Date[] startEndDates = new Date[2];

		// start date set
		startEndDates[0] = new DateTime(new Date()).minusDays(noOfDays).withTimeAtStartOfDay().toDate();

		// end date set
		startEndDates[1] = DateTime.now().toDate();

		return startEndDates;
	}

	public static Date[] getStartDateEndDateWithDayStart(Integer noOfDays) {
		Date[] startEndDates = new Date[2];

		// start date set
		startEndDates[0] = new DateTime(new Date()).minusDays(noOfDays).withTimeAtStartOfDay().toDate();

		// end date set
		startEndDates[1] = DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate();

		return startEndDates;
	}
	
	public static Date[] getStartDateEndDateByHours(Integer noOfHours) {
		Date[] startEndDates = new Date[2];

		// start date set
		startEndDates[0] = new DateTime(new Date()).minusHours(noOfHours).toDate();

		// end date set
		startEndDates[1] = DateTime.now().toDate();

		return startEndDates;
	}

	public static String formatDateToWithTimeZone(Date date, String timeZone) {
		// null check
		if (date == null)
			return null;
		// create SimpleDateFormat object with input format
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy hh:mm a");

		// default system timezone if passed null or empty
		if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
			timeZone = Calendar.getInstance().getTimeZone().getID();
		}
		// set timezone to SimpleDateFormat
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		// return Date in required format with timezone as String
		return sdf.format(date);
	}
}