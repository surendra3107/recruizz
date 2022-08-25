package com.bbytes.recruiz.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class SchedularServiceTest extends RecruizBaseApplicationTests {

	@Autowired
	private InterviewScheduleService interviewScheduleService;
	
	String tenantId = "Beyond_bytes";
	
	
	@Test
	public void sendReminderForInterviewSchedule() throws RecruizException, SQLException, IOException, ParseException {
		TenantContextHolder.setTenant(tenantId);
		
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
			interviewScheduleService.sendInterviewScheduleReminder();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
