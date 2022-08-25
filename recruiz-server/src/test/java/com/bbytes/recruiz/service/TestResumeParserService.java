package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.exception.RecruizException;

public class TestResumeParserService extends RecruizBaseApplicationTests {

	@Autowired
	private IResumeParserService resumeParserService;

	@Test
	public void testResumeParser() throws InterruptedException, IOException, RecruizException, ParseException {
		File resumeFile = new File("src/test/resources/testfiles/sample-resume-1.pdf");
		Assert.assertNotNull("File read error", resumeFile);
		Candidate candidate = resumeParserService.parseResume(resumeFile);
		Assert.assertNotNull("Candidate name missing", candidate.getFullName());
		System.out.println(candidate);
		Thread.sleep(3000);
	}
	
	@Test
	public void testResumeParserDocVersion() throws InterruptedException, IOException, RecruizException, ParseException {
		File resumeFile = new File("src/test/resources/testfiles/sample-resume-2.doc");
		Assert.assertNotNull("File read error", resumeFile);
		Candidate candidate = resumeParserService.parseResume(resumeFile);
		Assert.assertNotNull("Candidate name missing", candidate.getFullName());
		System.out.println(candidate);
		Thread.sleep(3000);
	}
	
	
	

}
