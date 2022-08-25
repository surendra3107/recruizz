package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import com.bbytes.recruiz.exception.RecruizException;

public class TestDateTimeUtils  {

	
	@Test
	public void testDateParse() throws InterruptedException, IOException, RecruizException, ParseException {
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
		DateTime dt = formatter.parseDateTime("21/12/1989");
		Assert.assertNotNull(dt);
		Assert.assertNotNull(DateTimeUtils.getDate("21/12/1989", "dd/MM/yyyy"));
		
	}

}
