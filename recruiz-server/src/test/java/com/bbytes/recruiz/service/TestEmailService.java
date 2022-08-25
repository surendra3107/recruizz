package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.exception.RecruizException;

public class TestEmailService extends RecruizBaseApplicationTests {

	@Autowired
	private IEmailService emailService;

	@Test
	public void testSendEmail() throws InterruptedException, RecruizException {
		List<String> emailList = new ArrayList<String>();
		emailList.add("tm@beyondbytes.co.in");

		emailService.sendEmail(emailList, "Recruiz test body", "Recruiz test subject - " + UUID.randomUUID());

		Thread.sleep(3000);
	}
}
