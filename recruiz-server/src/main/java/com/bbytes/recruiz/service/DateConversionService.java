package com.bbytes.recruiz.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DateConversionService {

	@Autowired
	private UserService userService;
	
	public String getConvertedDateTime(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(userService.getLoggedInUserObject().getTimezone()));
		String result=sdf.format(date); 
		
		return result;
	}
}
