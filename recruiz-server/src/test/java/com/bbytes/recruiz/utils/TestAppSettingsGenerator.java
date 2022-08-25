package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbytes.recruiz.exception.RecruizException;

public class TestAppSettingsGenerator {

		public Logger logger = LoggerFactory.getLogger(TestAppSettingsGenerator.class);
	
	@Test
	public void testGenerateSettings() throws InterruptedException, IOException, RecruizException, ParseException {

		String settings = AppSettingsGenerator.generateAndSaveSettings("Beyond bytes");
		logger.error(settings);
		System.out.println(settings);
	}
}
