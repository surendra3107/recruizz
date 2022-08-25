package com.bbytes.recruiz.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.User;

@Service
public class DateTimeService {

	private static Logger logger = LoggerFactory.getLogger(DateTimeService.class);

	@Autowired
	private UserService userService;

	/**
	 * to format datetime object as per user time zone
	 * 
	 * @param dateTime
	 * @param userEmail
	 * @return
	 */
	public String getUserTimezoneDateString(DateTime dateTime, String userEmail) {
		SimpleDateFormat formatter = formatDate(userEmail);
		String formatedDate = formatter.format(dateTime);
		return formatedDate;
	}

	/**
	 * to format date object as per user time zone
	 * 
	 * @param date
	 * @param userEmail
	 * @return
	 */
	public String getUserTimezoneDateString(Date date, String userEmail) {
		SimpleDateFormat formatter = formatDate(userEmail);
		String formatedDate = formatter.format(date);
		return formatedDate;
	}

	private SimpleDateFormat formatDate(String userEmail) {
		String userTimeZone = "";
		User user = userService.getUserByEmail(userEmail);
		if (user != null) {
			userTimeZone = user.getTimezone();
		}

		TimeZone timeZone = TimeZone.getDefault();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss z");
		if (userTimeZone != null && !userTimeZone.isEmpty()) {
			timeZone = TimeZone.getTimeZone(user.getTimezone());
			formatter.setTimeZone(timeZone);
		} else {
			formatter.setTimeZone(timeZone);
		}
		return formatter;
	}

}
