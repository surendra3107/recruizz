package com.bbytes.recruiz.service;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

import com.bbytes.recruiz.utils.AppSettingsGenerator;

public class AppSettingsGeneratorTest {

	@Test
	public void test() throws IOException, ParseException{
		AppSettingsGenerator.generateAndSaveSettings("BBytes");
	}
}
