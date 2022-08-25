package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.UniqueIdentifierGeneratorService;

@Controller
public class UniqueIdentifierGeneratorController {

	private static Logger logger = LoggerFactory.getLogger(UniqueIdentifierGeneratorController.class);

	@Autowired
	private UniqueIdentifierGeneratorService uniqueIdentifierGeneratorService;

	@RequestMapping(value = "/api/v1/generate/position/email/{positionCode}", method = RequestMethod.GET)
	public RestResponse generateEmailForPosition(@PathVariable("positionCode") String positionCode) throws IOException, RecruizException, ParseException {
		String positionEmail = uniqueIdentifierGeneratorService.generateUniqueResumeEmailForPosition(positionCode);
		return new RestResponse(RestResponse.SUCCESS, positionEmail);
	}
}
