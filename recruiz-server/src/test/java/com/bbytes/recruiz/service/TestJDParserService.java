package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.exception.RecruizException;

public class TestJDParserService extends RecruizBaseApplicationTests {

	@Autowired
	private IJobDescriptionParserService jobDescriptionParserService;

	@Test
	public void testJdDocParser() throws InterruptedException, IOException, RecruizException, ParseException {
		File resumeFile = new File("src/test/resources/testfiles/Accounting Assistant Job Summary.docx");
		Assert.assertNotNull("File read error", resumeFile);
		Position position = jobDescriptionParserService.parseJobDescription(resumeFile);
		Assert.assertNotNull(position);
		System.out.println(position);
		Thread.sleep(3000);
	}
	
	@Test
	public void testJdPDFVersion() throws InterruptedException, IOException, RecruizException, ParseException {
		File resumeFile = new File("src/test/resources/testfiles/UX Designer Job Summary.pdf");
		Assert.assertNotNull("File read error", resumeFile);
		Position position = jobDescriptionParserService.parseJobDescription(resumeFile);
		Assert.assertNotNull(position);
		System.out.println(position);
		Thread.sleep(3000);
	}
	
	
	

}
