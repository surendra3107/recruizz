package com.bbytes.recruiz.web.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.EmailTemplateData;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.EmailTemplateCategory;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.EmailTemplateVariableService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
public class EmailTemplateDataController {

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private EmailTemplateVariableService emailTemplateVariableService;

	/**
	 * 
	 * @param category
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/template/{category}", method = RequestMethod.GET)
	public RestResponse getAllEmailTemplate(@PathVariable("category") String category) throws RecruizException {
		List<EmailTemplateData> allTemplate = emailTemplateDataService.getNameAndSubjectList(category);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, allTemplate, null);
		return addRoundResponse;
	}

	/**
	 * 
	 * @param category
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/admin/template/{category}", method = RequestMethod.GET)
	public RestResponse getAllEmailTemplateForAdmin(@PathVariable("category") String category) throws RecruizException {
		List<EmailTemplateData> allTemplate = emailTemplateDataService.getAllTemplate(category);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, allTemplate, null);
		return addRoundResponse;
	}

	/**
	 * 
	 * @param scheduleDTO
	 * @param templateName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/template/interview/schedule", method = RequestMethod.POST)
	public RestResponse getRenderedTemplate(@RequestBody InterviewScheduleDTO scheduleDTO,
			@RequestParam(value = "templateName", required = false) String templateName)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForInterviewSchedule(templateName,
				scheduleDTO);
		String renderedSubject = emailTemplateDataService.getRenderedSubject(scheduleDTO.getPositionCode(),
				scheduleDTO.getCandidateEmail(), templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/email/rendered", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForEmail(@RequestParam("roundCandidateId") String roundCandidateId,
			@RequestParam(value = "templateName", required = false) String templateName)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForEmail(roundCandidateId, templateName);
		RoundCandidate roundCandidate = roundCandidateService.findOne(Long.parseLong(roundCandidateId));

		String renderedSubject = emailTemplateDataService.getRenderedSubject(roundCandidate.getPositionCode(),
				templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/forward/rendered", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForForwardProfile(@RequestParam("positionCode") String positionCode,
			@RequestParam("templateName") String templateName,
			@RequestParam(value = "candidateEmail", required = false) String candidateEmail,
			@RequestParam(value = "isMasked", required = false) boolean isMasked)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		List<String> candidateEmails = new ArrayList<>();
		if (candidateEmail != null && !candidateEmail.isEmpty()) {
			candidateEmails = StringUtils.commaSeparateStringToList(candidateEmail);
		}

		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForForwardProfile(positionCode,
				templateName, candidateEmails, isMasked);

		String renderedSubject = emailTemplateDataService.getRenderedSubject(positionCode, templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/update/{id}", method = RequestMethod.PUT)
	public RestResponse updateTemplate(@RequestBody EmailTemplateData emailTemplateData, @PathVariable("id") String id, @RequestParam(value = "variableData", required=false) String variableData)
			throws RecruizException, ParseException {

		if (emailTemplateData.getCategory() == null || emailTemplateData.getCategory().isEmpty()
				|| emailTemplateData.getBody() == null || emailTemplateData.getBody().isEmpty()
				|| emailTemplateData.getName() == null || emailTemplateData.getName().isEmpty()
				|| emailTemplateData.getSubject() == null || emailTemplateData.getSubject().isEmpty())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.REQUIRED_FIELD_MISSING,
					ErrorHandler.FIELD_REQUIRED);

		if (!emailTemplateDataService.exists(Long.parseLong(id))) {
			return new RestResponse(RestResponse.FAILED, GlobalConstants.TEMPLATE_NOT_FOUND, ErrorHandler.NO_TEMPLATE);
		}
		EmailTemplateData existingTemplate = emailTemplateDataService.findOne(Long.parseLong(id));
		if (existingTemplate.getCategory().equalsIgnoreCase(EmailTemplateCategory.interview_schedule_hr.name())
				&& !emailTemplateData.getCategory()
						.equalsIgnoreCase(EmailTemplateCategory.interview_schedule_hr.name()))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED_FOR_HR_CATEGORY_UPDATE,
					ErrorHandler.NO_ADD_UPDATE);

		if(variableData==null)
			emailTemplateData.setVariableData("");
		else
			emailTemplateData.setVariableData(variableData);
		
	
		
		existingTemplate = emailTemplateDataService.updateTemplate(emailTemplateData, id);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, existingTemplate, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/preview", method = RequestMethod.POST)
	public RestResponse previewTemplate(@RequestBody EmailTemplateData emailTemplateData)
			throws RecruizException, ParseException {
		EmailTemplateData renderedTemplate = emailTemplateDataService.previewTemplate(emailTemplateData);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedTemplate, null);
		return addRoundResponse;
	}

	/**
	 * 
	 * @param scheduleDTO
	 * @param templateName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/template/interviewer/rendered", method = RequestMethod.POST)
	public RestResponse getRenderedTemplateForInterviewer(@RequestBody InterviewScheduleDTO scheduleDTO,
			@RequestParam(value = "templateName", required = false) String templateName)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}
		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForInterviewSchedule(templateName,
				scheduleDTO);
		String renderedSubject = emailTemplateDataService.getRenderedSubject(scheduleDTO.getPositionCode(),
				scheduleDTO.getCandidateEmail(), templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/all", method = RequestMethod.GET)
	public RestResponse getAllEmailTemplates() throws RecruizException {

		Map<String, Object> templateMap = new java.util.LinkedHashMap<>();
		for (EmailTemplateCategory category : EmailTemplateCategory.values()) {
			List<EmailTemplateData> allTemplate = new LinkedList<>();
			allTemplate = emailTemplateDataService.getNameAndSubjectList(category.name());
			templateMap.put(category.getDisplayName(), allTemplate);
		}

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, templateMap, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/variable/{category}", method = RequestMethod.GET)
	public RestResponse getEmailTemplateVariables(@PathVariable("category") String category) throws RecruizException {

		List<String> templateVariable = new LinkedList<>();
		//templateVariable = emailTemplateVariableService.getTemplateVaribleListByCategory(category);
		
		templateVariable = emailTemplateVariableService.getListofTemplateVariblesByCategory(category);

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, templateVariable, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/add", method = RequestMethod.POST)
	public RestResponse addNewTemplate(@RequestBody EmailTemplateData emailTemplateData, @RequestParam(value = "variableData", required=false) String variableData)
			throws RecruizException, ParseException {

		if (emailTemplateData.getCategory() == null || emailTemplateData.getCategory().isEmpty()
				|| emailTemplateData.getBody() == null || emailTemplateData.getBody().isEmpty()
				|| emailTemplateData.getName() == null || emailTemplateData.getName().isEmpty()
				|| emailTemplateData.getSubject() == null || emailTemplateData.getSubject().isEmpty())
			return new RestResponse(RestResponse.FAILED, ErrorHandler.REQUIRED_FIELD_MISSING,
					ErrorHandler.FIELD_REQUIRED);

//	if(null !=emailTemplateData.getTemplateVariable() && !emailTemplateData.getTemplateVariable().isEmpty()) {
//		for (String variable : emailTemplateData.getTemplateVariable()) {
//		    emailTemplateData.getTemplateVariable() .add(variable);
//		}
//	    }

		if (emailTemplateDataService.getTemplateByName(emailTemplateData.getName()) != null)
			return new RestResponse(RestResponse.FAILED, ErrorHandler.TEMPLATE_EXISTS_WITH_NAME,
					ErrorHandler.TEMPLATE_EXISTS);

		if (emailTemplateData.getCategory().equalsIgnoreCase(EmailTemplateCategory.interview_schedule_hr.name()))
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED_FOR_HR_CATEGORY,
					ErrorHandler.NO_ADD_UPDATE);

		if(variableData!=null && !variableData.equalsIgnoreCase(""))
			emailTemplateData.setVariableData(variableData);
		
		emailTemplateDataService.addTemplate(emailTemplateData);
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, emailTemplateData, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteTemplate(@PathVariable("id") String id) throws RecruizException, ParseException {
		emailTemplateDataService.delete(Long.parseLong(id));
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.TEMPLATE_DELETED, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/restore", method = RequestMethod.PUT)
	public RestResponse deleteTemplate(@RequestParam("templateCategory") String templateCategory,
			@RequestParam("templateName") String templateName) throws RecruizException, ParseException {
		try {
			EmailTemplateData emailTemplate = emailTemplateDataService.restoreTemplate(templateCategory, templateName);
			RestResponse emailTemplateResponse = new RestResponse(RestResponse.SUCCESS, emailTemplate, null);
			return emailTemplateResponse;
		} catch (Exception ex) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.EMAIL_TEMPLATE_RESTORE_FAILED,
					ErrorHandler.TEMPLATE_RESTORE_FAILED);
		}
	}

	@RequestMapping(value = "/api/v1/template/bulk/email/rendered", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForBulkEmail(
			@RequestParam(value = "templateName", required = false) String templateName)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForBulkEmail(templateName);

		String renderedSubject = emailTemplateDataService.getRenderedSubject(null, templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	@RequestMapping(value = "/api/v1/template/campaign/rendered", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForCampaign(
			@RequestParam(value = "templateName", required = false) String templateName,
			@RequestParam(value = "positionCode", required = false) String positionCode)
			throws RecruizException, ParseException {

		if (templateName == null || templateName.isEmpty()) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.PARAMETER_MISSING,
					ErrorHandler.REQUEST_PARAMETER_MISSING);
		}

		String reneredTemplate = emailTemplateDataService.getRenderedTemplateForCampaign(templateName);

		String renderedSubject = emailTemplateDataService.getRenderedSubject(positionCode, templateName);
		Map<String, String> renderedList = new HashMap<String, String>();
		renderedList.put("template", reneredTemplate);
		renderedList.put("subject", renderedSubject);

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, renderedList, null);
		return addRoundResponse;
	}

	// to get rendered template to forward profile
	@RequestMapping(value = "/api/v1/pipeline/candidate/forward/template/{pcode}", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForForwardProfile(@PathVariable("pcode") String pcode,
			@RequestParam List<String> candidateFields, @RequestParam List<Long> roundCandidateIds,
			@RequestParam(required = false) Boolean roundStatus) throws RecruizException {

		RestResponse response = null;
		Map<String, Object> templateData = emailTemplateDataService.getRenderedTemplateForCandidateForwardProfile(pcode,
				candidateFields, roundCandidateIds, roundStatus);
		response = new RestResponse(true, templateData);

		return response;
	}

	@RequestMapping(value = "/api/v1/pipeline/candidate/forward/custom/template/rendered/{pcode}", method = RequestMethod.GET)
	public RestResponse getRenderedTemplateForCustomForwardProfile(@PathVariable("pcode") String pcode,
			@RequestParam List<Long> roundCandidateIds, @RequestParam(required = false) Boolean roundStatus,
			@RequestParam String templateName, @RequestParam String category) throws RecruizException {

		RestResponse response = null;
		EmailTemplateData customTemplate = emailTemplateDataService.getTemplateByNameAndCategory(templateName,
				category);

		if (null == customTemplate) {
			return new RestResponse(false, ErrorHandler.Email_NOT_PRESENT, ErrorHandler.NO_TEMPLATE);
		}
		List<String> candidateFields = new ArrayList<>();
		candidateFields.addAll(customTemplate.getTemplateVariable());
		Map<String, Object> templateData = emailTemplateDataService.getRenderedTemplateForCandidateForwardProfile(pcode,
				candidateFields, roundCandidateIds, roundStatus);
		String body = templateData.get("table").toString();

		String signatureData = emailTemplateDataService.getRenderedProfileSignature();

			customTemplate.setBody(body);
			
			if(customTemplate.getVariableData()!=null && customTemplate.getVariableData().equalsIgnoreCase("${emailSignature}") && !customTemplate.getVariableData().equalsIgnoreCase("") && !customTemplate.getVariableData().isEmpty())
			    customTemplate.setBody(customTemplate.getBody() + "<br>" + signatureData);
		
		templateData.put("body", "");
		templateData.put("template", customTemplate);

		response = new RestResponse(true, templateData);
		return response;
	}

}
