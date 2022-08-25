package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;

@RestController
public class CustomerFeedbackController {

	@Autowired
	private OrganizationService orgService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private UserService userService;

	@Value("${org.signup.notify.email.list:#{null}}")
	private String feedbackNotifyList;

	/**
	 * API used to get all candidates for sourcing apart from board.
	 * 
	 * @param boardId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/customer/feedback", method = RequestMethod.POST)
	public RestResponse sendCustomerFeedback(
			@RequestParam(value = "feedbackType", required = false) String feedbackType,
			@RequestParam(value = "feedbackMsg", required = false) String feedbackMsg) throws RecruizException {

		String orgName = orgService.getCurrentOrganization().getOrgName();
		String emailSubject = "Feedback from " + orgName;

		feedbackMsg ="<p>We received a feedback from recruiz application</p>"
				+ "<p>User : " + userService.getLoggedInUserName() + "</p>"
						+ "<p>Email : " + userService.getLoggedInUserEmail() + "</p>"
								+ "<p>Organization : " + orgName + "</p>"
										+ "<p>Feedback Category : " + feedbackType + "</p>"
												+ "<p>Feeback Message :</p><br />" + feedbackMsg; 

		String renderedFeedback = emailTemplateDataService.getMasterTemplateWithoutButton(feedbackMsg);

		List<String> emailList = Arrays.asList(feedbackNotifyList.split("\\s*,\\s*"));

		emailService.sendEmail(emailList, renderedFeedback, emailSubject, true, userService.getLoggedInUserEmail());

		String replyMessage ="<p>Hi, " + userService.getLoggedInUserName() + "</p>"
				+ "<p>We appreciate your feedback on Recruiz.</p>"
				+ "<p>Your feedback is forwarded to the right team and you will hear back from them shortly.</p>"
				+ "<p>Thank you and have a great day!</p>"; 
				
		String renderedReplyMessage = emailTemplateDataService.getMasterTemplateWithoutButton(replyMessage);
		String replySubject = "Thank you for providing your feedback!";
		List<String> replyEmail = new ArrayList<>();
		replyEmail.add(userService.getLoggedInUserEmail());

		emailService.sendEmail(replyEmail, renderedReplyMessage, replySubject);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, GlobalConstants.THANKS_FOR_FEEDBACK, null);
		return response;
	}
}
