package com.bbytes.recruiz.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.InterviewerTimeSlot;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class InterviewerTimeSlotRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	InterviewerTimeSlotRepository interviewerTimeSlotRepo;

	private String tenantName = "acme";

	@Before
	public void setup() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addTimeSlot() throws ParseException {
		Date statTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 01:00 pm");
		Date endTime1 = new SimpleDateFormat(GlobalConstants.DATE_TIME_FORMAT).parse("21-05-2016 02:00 pm");

		InterviewerTimeSlot interviewerTimeSlot = new InterviewerTimeSlot();
		interviewerTimeSlot.setStartTime(statTime1);
		interviewerTimeSlot.setEndTime(endTime1);
		interviewerTimeSlotRepo.save(interviewerTimeSlot);
	}
}
