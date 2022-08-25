package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.exception.RecruizException;

public class TestFileConversionService extends RecruizBaseApplicationTests {

	@Autowired
	private FileFormatConversionService fileFormatConversionService;

	@Test
	public void testResumeParser() throws InterruptedException, IOException, RecruizException, ParseException {
		File resumeFile = new File("src/test/resources/testfiles/sample-resume-1.pdf");
		Assert.assertNotNull("File read error", resumeFile);
		String result = fileFormatConversionService.convert(resumeFile.getPath(), "html");
		Assert.assertNotNull("File conversion failed", result);
		Thread.sleep(3000);
	}

}
