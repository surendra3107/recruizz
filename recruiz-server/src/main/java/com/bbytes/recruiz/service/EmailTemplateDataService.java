package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Campaign;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.EmailTemplateData;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterWorkflow;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.EmailTemplateDataRepository;
import com.bbytes.recruiz.rest.dto.models.InterviewPanelDTO;
import com.bbytes.recruiz.rest.dto.models.InterviewScheduleDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterApprovalDTO;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.VelocityTemplateUtils;

@Service
public class EmailTemplateDataService extends AbstractService<EmailTemplateData, Long> {

	private static final Logger logger = LoggerFactory.getLogger(EmailTemplateDataService.class);

	private EmailTemplateDataRepository emailTemplateDataRepository;

	@Autowired
	public EmailTemplateDataService(EmailTemplateDataRepository emailTemplateDataRepository) {
		super(emailTemplateDataRepository);
		this.emailTemplateDataRepository = emailTemplateDataRepository;
	}

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private VelocityEngine templateEngine;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private InterviewPanelService interviewPanelService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private DateTimeService dateTimeService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private ImportExportService importExportService;

	@Autowired
	private OfferLetterWorkFlowService offerLetterWorkFlowService;

	@Value("${logo.recruiz}")
	private String logoPath;

	@Value("${social.linkedin.link}")
	private String linkedIn_link;

	@Value("${social.facebook.link}")
	private String facebook_link;

	@Value("${social.twitter.link}")
	private String twitter_link;

	@Value("${social.linkedin.icon}")
	private String linkedIn_icon;

	@Value("${social.facebook.icon}")
	private String facebook_icon;

	@Value("${social.twitter.icon}")
	private String twiter_icon;

	@Value("${copyright}")
	private String Copyright;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${allowed.external.user.links.validity}")
	private String extendTokenForDays;

	// private String dbTemplateFolderPath = "app-email-templates";

	@Value("${agency.db.template.path}")
	private String agencyDBTemplateFolderPath;

	@Value("${corp.db.template.path}")
	private String corpDBTemplateFolderPath;

	private String orgWebsite;

	private String orgName;

	/**
	 * to get all email template
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<EmailTemplateData> getAllTemplate(String category) {
		List<EmailTemplateData> allTemplate = emailTemplateDataRepository.findByCategory(category);
		if (allTemplate != null && !allTemplate.isEmpty()) {
			for (EmailTemplateData emailTemplateData : allTemplate) {
				emailTemplateData.getTemplateVariable().size();
			}
		}
		return allTemplate;
	}

	@Transactional(readOnly = true)
	public EmailTemplateData getTemplateByName(String name) {
		EmailTemplateData emailTemplateData = emailTemplateDataRepository.findByName(name);
		if (null != emailTemplateData && null != emailTemplateData.getTemplateVariable()
				&& !emailTemplateData.getTemplateVariable().isEmpty()) {
			emailTemplateData.getTemplateVariable().size();
		}

		return emailTemplateData;
	}

	@Transactional
	public String getRenderedTemplateForInterviewSchedule(String templateName, InterviewScheduleDTO scheduleDTO)
			throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyMapForInterview(scheduleDTO));
		return renderedTemplate;
	}

	/**
	 *
	 * @param templateName
	 * @param scheduleDTO
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedTemplateForInterviewSchedule(String templateName, String positionCode,
			String candidateEmail, Set<String> interviewerEmails, String roundName, Date scheduleDate)
					throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyForInterviewSchedule(positionCode, candidateEmail, interviewerEmails, roundName,
						scheduleDate));
		return renderedTemplate;
	}

	/**
	 * Get rendered template for interviewer
	 *
	 * @param templateName
	 * @param scheduleDTO
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedTemplateForInterviewer(String templateName, InterviewScheduleDTO scheduleDTO)
			throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyMapForInterview(scheduleDTO));
		return renderedTemplate;
	}

	/**
	 * body for interview schedule
	 *
	 * @param scheduleDTO
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	private Map<String, Object> getEmailBodyMapForInterview(InterviewScheduleDTO scheduleDTO)
			throws ParseException, RecruizException {

		Position position = positionService.getPositionByCode(scheduleDTO.getPositionCode());
		Round round = roundService.findOne(Long.parseLong(scheduleDTO.getRoundId()));
		Candidate candidate = candidateService.getCandidateByEmail(scheduleDTO.getCandidateEmail());
		String hrMobile = userService.getLoggedInUserObject().getMobile();
		if (hrMobile == null)
			hrMobile = "";
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.ROUND_NAME, round.getRoundName());
		emailBody.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());

		if (null != candidate.getMobile() && !candidate.getMobile().trim().isEmpty()) {
			emailBody.put(GlobalConstants.CANDIDATE_MOBILE, candidate.getMobile());
		} else {
			emailBody.put(GlobalConstants.CANDIDATE_MOBILE, "NA");
		}

		emailBody.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		if (organizationService.getCurrentOrganization().getOrgType().equalsIgnoreCase("Agency")) {
			emailBody.put(GlobalConstants.CLIENT_LABEL, "Department");
		} else {
			emailBody.put(GlobalConstants.CLIENT_LABEL, "Client");
		}
		emailBody.put(GlobalConstants.HR_NAME, userService.getLoggedInUserObject().getName());
		emailBody.put(GlobalConstants.HR_MOBILE, hrMobile);

		String startDate = dateTimeService.getUserTimezoneDateString(scheduleDTO.getStartTime(),
				userService.getLoggedInUserEmail());
		String endDate = dateTimeService.getUserTimezoneDateString(scheduleDTO.getEndTime(),
				userService.getLoggedInUserEmail());

		emailBody.put(GlobalConstants.INTERVIEW_SHCEDULE, startDate + " - " + endDate);

		emailBody.put(GlobalConstants.CANDIDATE_KEY_SKILLS, candidate.getKeySkills().toString());
		emailBody.put(GlobalConstants.CANDIDATE_TOTAL_EXPERIENCE, candidate.getTotalExp() + " yrs");
		emailBody.put(GlobalConstants.CANDIDATE_CURRENT_COMPANY, candidate.getCurrentCompany());
		emailBody.put(GlobalConstants.CANDIDATE_CURRENT_LOCATION, candidate.getCurrentLocation());
		emailBody.put(GlobalConstants.CANDIDATE_PREFFERED_LOCATION, candidate.getPreferredLocation());
		emailBody.put(GlobalConstants.CANDIDATE_EMAIL, candidate.getEmail());
		emailBody.put(GlobalConstants.CANDIDATE_CURRENT_DESIGNATION, candidate.getCurrentTitle());

		emailBody.put(GlobalConstants.CANDIDATE_EDUCATIONAL_QUALIFICATION, candidate.getHighestQual());
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());

		// adding interviewers to emailList
		List<String> interviewPanelMember = new ArrayList<>();
		if (scheduleDTO.getInterviewerEmails() != null && !scheduleDTO.getInterviewerEmails().isEmpty()) {
			for (String interviewerEmail : scheduleDTO.getInterviewerEmails()) {
				ClientInterviewerPanel interviewer = interviewPanelService
						.getInterviewerByEmailAndClient(interviewerEmail, position.getClient());
				if (interviewer != null) {
					interviewPanelMember.add(interviewer.getName() + " (" + interviewerEmail + ")");
				}
			}
		}

		if (scheduleDTO.getInterviewerList() != null && !scheduleDTO.getInterviewerList().isEmpty()) {
			for (InterviewPanelDTO interviewer : scheduleDTO.getInterviewerList()) {
				interviewPanelMember.add(interviewer.getName() + " (" + interviewer.getEmail() + ")");
			}
		}

		if (interviewPanelMember != null && !interviewPanelMember.isEmpty()) {
			emailBody.put(GlobalConstants.INTERVIEWER_LIST, StringUtils.commaSeparate(interviewPanelMember));
		} else {
			emailBody.put(GlobalConstants.INTERVIEWER_LIST, "N/A");
		}

		return emailBody;
	}

	/**
	 * body for interview schedule/reschedule
	 *
	 * @param positionCode
	 * @param candidateEmail
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	private Map<String, Object> getEmailBodyForInterviewSchedule(String positionCode, String candidateEmail,
			Set<String> interviewerEmails, String roundName, Date scheduleDate)
					throws ParseException, RecruizException {

		Position position = positionService.getPositionByCode(positionCode);
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		String hrMobile = userService.getLoggedInUserObject().getMobile();
		if (hrMobile == null)
			hrMobile = "";
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());
		emailBody.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());

		if (!organizationService.getCurrentOrganization().getOrgType().equalsIgnoreCase("Agency")) {
			emailBody.put(GlobalConstants.CLIENT_LABEL, "Department");
		} else {
			emailBody.put(GlobalConstants.CLIENT_LABEL, "Client");
		}

		emailBody.put(GlobalConstants.HR_NAME, userService.getLoggedInUserObject().getName());
		emailBody.put(GlobalConstants.HR_MOBILE, hrMobile);
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
		emailBody.put(GlobalConstants.ROUND_NAME, roundName);
		emailBody.put(GlobalConstants.INTERVIEW_SHCEDULE,
				dateTimeService.getUserTimezoneDateString(scheduleDate, userService.getLoggedInUserEmail()));

		if (candidate.getMobile() != null && !candidate.getMobile().isEmpty()) {
			emailBody.put(GlobalConstants.CANDIDATE_MOBILE, candidate.getMobile());
		} else {
			emailBody.put(GlobalConstants.CANDIDATE_MOBILE, "");
		}
		emailBody.put(GlobalConstants.CANDIDATE_EMAIL, candidate.getEmail());

		// adding list of interviewer in map
		if (interviewerEmails != null && !interviewerEmails.isEmpty()) {
			String interviewers = "";
			for (String email : interviewerEmails) {
				ClientInterviewerPanel interviewer = interviewPanelService.getInterviewerByEmailAndClient(email,
						position.getClient());
				if (interviewer != null) {
					interviewers = interviewers + "<br />" + interviewer.getName() + " (" + interviewer.getEmail()
					+ ")";
				}
			}
			emailBody.put(GlobalConstants.INTERVIEWER_LIST, interviewers);
		} else {
			emailBody.put(GlobalConstants.INTERVIEWER_LIST, "N/A");
		}

		return emailBody;
	}

	@Transactional(readOnly = true)
	public List<EmailTemplateData> getNameAndSubjectList(String category) {
		List<EmailTemplateData> allTemplate = emailTemplateDataRepository.findByCategory(category);
		if (allTemplate != null && !allTemplate.isEmpty()) {
			for (EmailTemplateData emailTemplateData : allTemplate) {
				emailTemplateData.getTemplateVariable().size();
				// as per UI request, again passing the email body in response
				// date : 03-feb-2016
				// emailTemplateData.setBody("");
			}
		}
		return allTemplate;
	}

	/**
	 * Get rendered email template for sending emails
	 *
	 * @param candidateRoundId
	 * @param templateName
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedTemplateForEmail(String candidateRoundId, String templateName)
			throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);

		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyForEmail(candidateRoundId));
		return renderedTemplate;
	}

	@Transactional(readOnly = true)
	public String getRenderedTemplateForBulkEmail(String templateName) throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);
		Map<String, Object> emailBodyValueMap = new HashMap<>();
		initEmailBodyDefaultVariables(emailBodyValueMap);
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(), emailBodyValueMap);
		return renderedTemplate;
	}

	/**
	 * get rendered template for cancel interview
	 *
	 * @param candidateRoundId
	 * @param templateName
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedTemplateForCancelInterview(String positionCode, String candidateName, String templateName)
			throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);

		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyForCancelInterview(positionCode, candidateName));
		return renderedTemplate;
	}

	/**
	 * Get rendered email template for sending forward profile
	 *
	 * @param candidateRoundId
	 * @param templateName
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public String getRenderedTemplateForForwardProfile(String positionCode, String templateName,
			List<String> candidateEmail, boolean isMasked) throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);

		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(),
				getEmailBodyForForwardProfile(positionCode, candidateEmail, isMasked));
		return renderedTemplate;
	}

	/**
	 * getting rendered html body for sending emails
	 *
	 * @param candidateRoundId
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	private Map<String, Object> getEmailBodyForEmail(String candidateRoundId) throws ParseException, RecruizException {

		RoundCandidate roundcandidate = roundCandidateService.findOne(Long.parseLong(candidateRoundId));
		Position position = positionService.getPositionByCode(roundcandidate.getPositionCode());
		Candidate candidate = roundcandidate.getCandidate();
		String hrMobile = userService.getLoggedInUserObject().getMobile();
		if (hrMobile == null)
			hrMobile = "";
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());
		emailBody.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		emailBody.put(GlobalConstants.HR_NAME, userService.getLoggedInUserObject().getName());
		emailBody.put(GlobalConstants.HR_MOBILE, hrMobile);
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
		return emailBody;
	}

	/**
	 * getting rendered html body for sending canceel interview
	 *
	 * @param candidateRoundId
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional
	private Map<String, Object> getEmailBodyForCancelInterview(String positionCode, String candidateName)
			throws ParseException, RecruizException {

		Position position = positionService.getPositionByCode(positionCode);
		String hrMobile = userService.getLoggedInUserObject().getMobile();
		if (hrMobile == null)
			hrMobile = "";
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.CANDIDATE_NAME, candidateName);
		emailBody.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		emailBody.put(GlobalConstants.HR_NAME, userService.getLoggedInUserObject().getName());
		emailBody.put(GlobalConstants.HR_MOBILE, hrMobile);
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
		return emailBody;
	}

	/**
	 * getting rendered html body for sending emails
	 *
	 * @param candidateRoundId
	 * @return
	 * @throws ParseException
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	private Map<String, Object> getEmailBodyForForwardProfile(String positionCode, List<String> candidateEmail,
			boolean isMasked) throws ParseException, RecruizException {

		List<Candidate> candidateList = candidateService.getCandidateByEmailIdsIn(candidateEmail);
		for (Candidate candidate : candidateList) {
			if (null == candidate.getMobile() || candidate.getMobile().isEmpty()) {
				candidate.setMobile("N/A");
			}
			if (null == candidate.getFullName() || candidate.getFullName().isEmpty()) {
				candidate.setFullName("N/A");
			}
		}

		Position position = positionService.getPositionByCode(positionCode);
		String hrMobile = userService.getLoggedInUserObject().getMobile();
		if (hrMobile == null)
			hrMobile = "";
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		emailBody.put(GlobalConstants.HR_NAME, userService.getLoggedInUserObject().getName());
		emailBody.put(GlobalConstants.HR_MOBILE, hrMobile);
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
		emailBody.put(GlobalConstants.CANDIDATE_LIST, candidateList);
		emailBody.put(GlobalConstants.IS_MASKED, isMasked);

		return emailBody;
	}

	/**
	 * getting rendered html body for sending emails
	 *
	 * @param candidateRoundId
	 * @return
	 * @throws ParseException9015567815
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedSubject(String positionCode, String candidateEmail, String templateName)
			throws ParseException, RecruizException {

		Map<String, Object> subjectMap = new HashMap<>();
		if (positionCode != null && !positionCode.isEmpty()) {
			Position position = positionService.getPositionByCode(positionCode);
			subjectMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
			subjectMap.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		}

		if (candidateEmail != null && !candidateEmail.isEmpty()) {
			subjectMap.put(GlobalConstants.CANDIDATE_NAME,
					candidateService.getCandidateByEmail(candidateEmail).getFullName());
		} else {
			subjectMap.put(GlobalConstants.CANDIDATE_NAME, " ");
		}

		subjectMap.put("orgName", organizationService.getCurrentOrganization().getOrgName());

		initEmailBodyDefaultVariables(subjectMap);
		String subjectTemplate = emailTemplateDataRepository.findByName(templateName).getSubject();
		String renderedSubject = VelocityTemplateUtils.getTemplateString(subjectTemplate, subjectMap);

		return renderedSubject;
	}

	@Transactional
	public String getRenderedSubject(String positionCode, String templateName) throws ParseException, RecruizException {
		return getRenderedSubject(positionCode, "", templateName);
	}

	@Transactional
	public EmailTemplateData updateTemplate(EmailTemplateData emailTemplateData, String id) {
		isEmailTemplateExists(id);
		EmailTemplateData emailTemplate = emailTemplateDataRepository.findOne(Long.parseLong(id));
		emailTemplate.setBody(emailTemplateData.getBody());
		emailTemplate.setCategory(emailTemplateData.getCategory());
		emailTemplate.setName(emailTemplateData.getName());
		emailTemplate.setSubject(emailTemplateData.getSubject());
		emailTemplate.setTemplateVariable(emailTemplateData.getTemplateVariable());
		emailTemplate.setVariableData(emailTemplateData.getVariableData());
		EmailTemplateData template = emailTemplateDataRepository.save(emailTemplate);
		return template;
	}

	@Transactional(readOnly = true)
	public void isEmailTemplateExists(String id) {
		emailTemplateDataRepository.exists(Long.parseLong(id));
	}

	/**
	 * return previewed template
	 *
	 * @param emailTemplateData
	 * @throws RecruizException
	 * @throws ParseException
	 */
	public EmailTemplateData previewTemplate(EmailTemplateData emailTemplateData)
			throws ParseException, RecruizException {
		String subject = VelocityTemplateUtils.getTemplateString(emailTemplateData.getSubject(), previewObjectMap());
		String body = VelocityTemplateUtils.getTemplateString(emailTemplateData.getBody(), previewObjectMap());
		emailTemplateData.setSubject(subject);
		emailTemplateData.setBody(body);
		return emailTemplateData;
	}

	private Map<String, Object> previewObjectMap() throws ParseException, RecruizException {
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POSITION_NAME, "Software Engineer");
		emailBody.put(GlobalConstants.CANDIDATE_NAME, "Mr. Niladri Patiyala");
		emailBody.put(GlobalConstants.CLIENT_NAME, "Beyond Bytes Technologies Pvt Ltd");
		emailBody.put(GlobalConstants.HR_NAME, "Mr. HR Sarma");
		emailBody.put(GlobalConstants.HR_MOBILE, "+91 9123456787");
		emailBody.put(GlobalConstants.EMAIL_SIGNATURE, "email profile signature will come here !");
		return emailBody;
	}

	public String getHtmlContentFromFile(Map<String, Object> emailBody, String template) {
		@SuppressWarnings("deprecation")
		String emailHTMLContent = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, template, "UTF-8",
				emailBody);
		return emailHTMLContent;
	}

	@Transactional(readOnly = true)
	public String getHeaderHTML() {
		Map<String, Object> headerMap = new HashMap<>();
		initEmailBodyDefaultVariables(headerMap);
		String headerHtml = VelocityTemplateUtils
				.getTemplateString(getTemplateByName(GlobalConstants.TEMPLATE_NAME_HEADER).getBody(), headerMap);

		return headerHtml;
	}

	@Transactional(readOnly = true)
	public String getFooterHTML() {
		Map<String, Object> footerMap = new HashMap<>();
		initEmailBodyDefaultVariables(footerMap);

		String footerHTML = VelocityTemplateUtils
				.getTemplateString(getTemplateByName(GlobalConstants.TEMPLATE_NAME_FOOTER).getBody(), footerMap);

		return footerHTML;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWithButton(String template, String buttonLink, String buttonText) {
		String templateName = "Email Master Button";
		Map<String, Object> bodyMap = new HashMap<>();
		String masterTemplateWithButton = getRenderedTemplate(template, buttonLink, buttonText, templateName, bodyMap);
		return masterTemplateWithButton;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWithButtonCampaign(String template, String interestedLink, String buttonText,
			String notInterestedLink) {
		String templateName = "Forward Body Master";
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.NOT_INTERESTED_PARAM_LINK, notInterestedLink);
		String masterTemplateWithButton = getRenderedTemplate(template, interestedLink, buttonText, templateName,
				bodyMap);
		return masterTemplateWithButton;
	}

	public String getRenderedTemplate(String template, String buttonLink, String buttonText, String templateName,
			Map<String, Object> bodyMap) {
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, getHeaderHTML());
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, getFooterHTML());
		bodyMap.put(GlobalConstants.MasterEmailTemplate, template);
		bodyMap.put(GlobalConstants.NOTE_LINK, buttonLink);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_TEXT, buttonText);

		String masterTemplateWithButton = VelocityTemplateUtils
				.getTemplateString(getTemplateByName(templateName).getBody(), bodyMap);
		return masterTemplateWithButton;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWithoutButton(String template) {
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, getHeaderHTML());
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, getFooterHTML());
		bodyMap.put(GlobalConstants.MasterEmailTemplate, template);

		String masterTemplateWithButton = VelocityTemplateUtils
				.getTemplateString(getTemplateByName("Email Master").getBody(), bodyMap);
		return masterTemplateWithButton;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWithoutButtonFullWidth(String template) {
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, getHeaderHTML());
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, getFooterHTML());
		bodyMap.put(GlobalConstants.MasterEmailTemplate, template);

		String masterTemplateWithButton = VelocityTemplateUtils
				.getTemplateString(getTemplateByName("Full Width Master").getBody(), bodyMap);
		return masterTemplateWithButton;
	}

	@Transactional
	public EmailTemplateData addTemplate(EmailTemplateData emailTemplateData) {
		return save(emailTemplateData);
	}

	/** Get template for JD */
	public String getRenderedTemplateForJD(Position position) {
		Map<String, Object> jdTemplateMap = new HashMap<>();
		jdTemplateMap.put("openedDate", position.getOpenedDate());
		jdTemplateMap.put("closeDate", position.getCloseByDate());
		jdTemplateMap.put("positionName", position.getTitle());
		jdTemplateMap.put("clientName", position.getClient().getClientName());
		jdTemplateMap.put("totalOpenings", position.getTotalPosition());
		jdTemplateMap.put("location", position.getLocation());
		jdTemplateMap.put("reqSkillSet", StringUtils.commaSeparate(position.getReqSkillSet()));
		jdTemplateMap.put("goodSkillSet", StringUtils.commaSeparate(position.getGoodSkillSet()));
		jdTemplateMap.put("eduQual", StringUtils.commaSeparate(position.getEducationalQualification()));
		jdTemplateMap.put("jobUrl", position.getPositionUrl());

		if(position.getType()==null || position.getType().equalsIgnoreCase("") || position.getType().isEmpty()){
			jdTemplateMap.put("jobType", "N/A");
		}else{
			jdTemplateMap.put("jobType", position.getType());
		}
		if (position.isRemoteWork()) {
			jdTemplateMap.put("remoteWork", "Yes");
		} else {
			jdTemplateMap.put("remoteWork", "No");
		}

		jdTemplateMap.put("exp", position.getExperienceRange());
		if(position.getIndustry()==null || position.getIndustry().equalsIgnoreCase("") || position.getIndustry().isEmpty()){
			jdTemplateMap.put("industry", "N/A");
		}else{
			jdTemplateMap.put("industry", position.getIndustry());
		}
		//	jdTemplateMap.put("industry", position.getIndustry());
		if(position.getFunctionalArea()==null || position.getFunctionalArea().equalsIgnoreCase("") || position.getFunctionalArea().isEmpty()){
			jdTemplateMap.put("category", "N/A");
		}else{
			jdTemplateMap.put("category", position.getFunctionalArea());
		}
		//	jdTemplateMap.put("category", position.getFunctionalArea());
		jdTemplateMap.put("description", position.getDescription());
		jdTemplateMap.put("hrEmail", userService.getLoggedInUserEmail());

		String jdTemplate = GlobalConstants.JD_EMAIL_TEMPLATE;

		String renderedJdTemplate = getHtmlContentFromFile(jdTemplateMap, jdTemplate);
		return renderedJdTemplate;
	}

	/**
	 * this will initialize all the header and footer variable values either from
	 * organization info or from properties file
	 *
	 * @param emailBodyValueMap
	 */
	@Transactional(readOnly = true)
	public void initEmailBodyDefaultVariables(Map<String, Object> emailBodyValueMap) {
		Organization org = organizationService.getCurrentOrganization();

		if (org == null) {
			return;
		}

		emailBodyValueMap.put("twitter_icon", twiter_icon);
		emailBodyValueMap.put("facebook_icon", facebook_icon);
		emailBodyValueMap.put("linkedIn_icon", linkedIn_icon);
		emailBodyValueMap.put("Copyright", Copyright);

		if (org.getFacebookUrl() != null && !org.getFacebookUrl().isEmpty()) {
			emailBodyValueMap.put("facebook_link", org.getFacebookUrl());
		} else {
			emailBodyValueMap.put("facebook_link", facebook_link);
		}

		if (org.getTwitterUrl() != null && !org.getTwitterUrl().isEmpty()) {
			emailBodyValueMap.put("twitter_link", org.getTwitterUrl());
		} else {
			emailBodyValueMap.put("facebook_link", twitter_link);
		}

		if (org.getLinkedInUrl() != null && !org.getLinkedInUrl().isEmpty()) {
			emailBodyValueMap.put("linkedIn_link", org.getLinkedInUrl());
		} else {
			emailBodyValueMap.put("linkedIn_link", linkedIn_link);
		}

		if (org.getLogoUrlPath() != null && !org.getLogoUrlPath().isEmpty()) {
			String logoUrl = baseUrl + "/pubset/" + org.getLogoUrlPath();
			emailBodyValueMap.put("logo", logoUrl);
		} else {
			emailBodyValueMap.put("logo", logoPath);
		}

		if (org.getWebsiteUrl() != null && !org.getWebsiteUrl().isEmpty()) {
			String websiteUrl = "https://" + baseUrl + "/pubset" + org.getWebsiteUrl();
			emailBodyValueMap.put("orgWebsite", websiteUrl);
		} else {
			String websiteUrl = "www.recruiz.com";
			emailBodyValueMap.put("orgWebsite", websiteUrl);
		}

		if (org.getOrgName() != null && !org.getOrgName().isEmpty()) {
			emailBodyValueMap.put("orgName", org.getOrgName());
		} else {
			emailBodyValueMap.put("orgName", "");
		}

		// adding profile signature here
		emailBodyValueMap.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
	}

	/**
	 * Get template for RESUME EMAIL REPLY
	 *
	 * @throws RecruizException
	 */
	@Transactional
	public String getRenderedTemplateForResumeEmailReply(String positionCode, List<String> candidateList)
			throws RecruizException {
		Map<String, Object> jdTemplateMap = new HashMap<>();
		// Position position = positionService.getPositionByCode(positionCode);
		jdTemplateMap.put("positionName", null);
		jdTemplateMap.put("candidateList", StringUtils.commaSeparate(candidateList));

		String jdTemplate = GlobalConstants.RESUME_REPLY_EMAIL_TEMPLATE;

		String renderedBodyTemplate = getHtmlContentFromFile(jdTemplateMap, jdTemplate);
		String renderedTemplate = getMasterTemplateWithoutButton(renderedBodyTemplate);
		return renderedTemplate;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> addHeaderFooterVariable(Map<String, Object> emailVariableMap) {
		initEmailBodyDefaultVariables(emailVariableMap);
		emailVariableMap.put("header", getHeaderHTML());
		emailVariableMap.put("footer", getFooterHTML());

		return emailVariableMap;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getEmailBodyValueMapForCandidateSourceEmailForHr(Position position, Candidate candidate,
			String source) {

		String link = baseUrl + GlobalConstants.BOARD_URL + position.getPositionCode() + "/";
		String candidateDetailsUrl = baseUrl + GlobalConstants.CANIDATE_DETAILS_URL + candidate.getCid();

		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
		bodyMap.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
		bodyMap.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());
		bodyMap.put(GlobalConstants.CANDIDATE_EMAIL, candidate.getEmail());
		bodyMap.put(GlobalConstants.SOURCE, source);
		bodyMap.put(GlobalConstants.NOTE_LINK, link);
		bodyMap.put(GlobalConstants.CANDIDATE_PROFILE_LINK, candidateDetailsUrl);
		bodyMap.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());
		bodyMap = addHeaderFooterVariable(bodyMap);
		return bodyMap;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getEmailBodyValueMapForPositionAddedEmail(Position position) {

		try {
			User user = userService.getLoggedInUserObject();
			String link = baseUrl + GlobalConstants.POSITION_DETAILS_URL + position.getId();
			Organization org = organizationService.getCurrentOrganization();

			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
			bodyMap.put(GlobalConstants.ORG_NAME, org.getOrgName());
			bodyMap.put(GlobalConstants.HR_NAME, user.getName());
			bodyMap.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
			bodyMap.put(GlobalConstants.EMAIL_SIGNATURE, getRenderedProfileSignature());

			if (user.getMobile() != null && !user.getMobile().isEmpty()) {
				bodyMap.put(GlobalConstants.HR_MOBILE, user.getMobile());
			} else {
				bodyMap.put(GlobalConstants.HR_MOBILE, " ");
			}
			bodyMap.put(GlobalConstants.STATUS, Status.valueOf(position.getStatus()).getDisplayName());
			bodyMap.put(GlobalConstants.NOTE_LINK, link);
			bodyMap = addHeaderFooterVariable(bodyMap);
			return bodyMap;
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			return null;
		}
	}

	@Transactional(readOnly = true)
	public String getRenderedProfileSignature() {
		User user = userService.getLoggedInUserObject();
		if (user != null && user.getProfileSignature() != null && !user.getProfileSignature().isEmpty()) {
			String templateSignature = user.getProfileSignature();
			Map<String, Object> profileSignatureVariableMap = new HashMap<>();

			profileSignatureVariableMap.put(GlobalConstants.HR_EMAIL, user.getEmail());
			profileSignatureVariableMap.put(GlobalConstants.ORG_NAME, user.getOrganization().getOrgName());

			if (user.getName() != null && !user.getName().isEmpty()) {
				profileSignatureVariableMap.put(GlobalConstants.HR_NAME, user.getName());
			} else {
				profileSignatureVariableMap.put(GlobalConstants.HR_NAME, "");
			}

			if (user.getMobile() != null && !user.getMobile().isEmpty()) {
				profileSignatureVariableMap.put(GlobalConstants.HR_MOBILE, user.getMobile());
			} else {
				profileSignatureVariableMap.put(GlobalConstants.HR_MOBILE, "");
			}

			if (user.getOrganization().getWebsiteUrl() != null && !user.getOrganization().getWebsiteUrl().isEmpty()) {
				profileSignatureVariableMap.put(GlobalConstants.ORG_WEBSITE, user.getOrganization().getWebsiteUrl());
			} else {
				profileSignatureVariableMap.put(GlobalConstants.ORG_WEBSITE, "");
			}

			if (user.getDesignation() != null && !user.getDesignation().isEmpty()) {
				profileSignatureVariableMap.put(GlobalConstants.USER_DESIGNATION, user.getDesignation());
			} else {
				profileSignatureVariableMap.put(GlobalConstants.USER_DESIGNATION, "");
			}

			String renderedTemplateSignature = VelocityTemplateUtils.getTemplateString(templateSignature,
					profileSignatureVariableMap);
			return renderedTemplateSignature;
		}
		// returning blank string when email signature is not present
		return " ";
	}

	@Transactional(readOnly = true)
	public EmailTemplateData getTemplateByNameAndCategory(String templateName, String category) {
		return emailTemplateDataRepository.findByNameAndCategory(templateName, category);
	}

	@Transactional
	public EmailTemplateData restoreTemplate(String templateCategory, String templateName) throws IOException {
		String templateSubjectConstant = "__templateSubject__";
		EmailTemplateData emailTemplate = emailTemplateDataRepository.findByNameAndCategory(templateName,
				templateCategory);

		String dbTemplateFolderPath = null;
		if (organizationService.getCurrentOrganization().getOrgType()
				.equalsIgnoreCase(GlobalConstants.ORG_TYPE_AGENCY)) {
			dbTemplateFolderPath = agencyDBTemplateFolderPath;
		} else {
			dbTemplateFolderPath = corpDBTemplateFolderPath;
		}

		if (emailTemplate != null) {
			// Resource res = new ClassPathResource(dbTemplateFolderPath +
			// templateCategory + "/" + templateName);
			File templateDirectory = new File(dbTemplateFolderPath + templateCategory + "/" + templateName);
			Collection<File> files = FileUtils.listFiles(templateDirectory, null, true);
			for (File templateFile : files) {
				long version = 0;
				String templateFileName = templateFile.getName();
				version = Long.parseLong(
						templateFileName.substring(0, templateFileName.indexOf("__")).replace("v", "").trim());
				if (templateFileName.contains(templateSubjectConstant)) {
					String templateSubject = new String(Files.readAllBytes(templateFile.toPath()));
					emailTemplate.setSubject(templateSubject);
					emailTemplate.setSubjectVersion(version);
				} else {
					String templateBody = new String(Files.readAllBytes(templateFile.toPath()));
					emailTemplate.setBody(templateBody);
					emailTemplate.setBodyVersion(version);
				}
				save(emailTemplate);
			}
		}
		return emailTemplate;
	}

	@Transactional(readOnly = true)
	public String getMasterRenderedTemplateForEmailCampaign(Campaign campaign, String candidateEmail,
			String candidateName, String emailLink, String linkButtonTitle, String notInterestedLink)
					throws RecruizException {
		Map<String, Object> templateMapForCampaign = new HashMap<>();

		initEmailBodyDefaultVariables(templateMapForCampaign);

		if (campaign.getPositionCode() != null) {
			Position position = positionService.getPositionByCode(campaign.getPositionCode());
			templateMapForCampaign.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
			templateMapForCampaign.put(GlobalConstants.POSITION_NAME, position.getTitle());
		} else {
			templateMapForCampaign.put(GlobalConstants.CLIENT_NAME, " ");
			templateMapForCampaign.put(GlobalConstants.POSITION_NAME, " ");
		}

		templateMapForCampaign.put(GlobalConstants.CANDIDATE_NAME, candidateName);
		templateMapForCampaign.put(GlobalConstants.CANDIDATE_EMAIL, candidateEmail);

		templateMapForCampaign.put(GlobalConstants.NOTE_LINK, emailLink);
		templateMapForCampaign.put(GlobalConstants.NOT_INTERESTED_PARAM_LINK, notInterestedLink);

		String template = VelocityTemplateUtils.getTemplateString(campaign.getCampaignRenderedTemplate(),
				templateMapForCampaign);
		// getHtmlContentFromFile(templateMapForCampaign,
		// campaign.getCampaignRenderedTemplate());

		String masterTemplate = getMasterTemplateWithButtonCampaign(template, emailLink, linkButtonTitle,
				notInterestedLink);

		return masterTemplate;
	}

	@Transactional(readOnly = true)
	public String getRenderedTemplateForCampaign(String templateName) throws ParseException, RecruizException {
		EmailTemplateData template = getTemplateByName(templateName);
		Map<String, Object> emailBodyValueMap = new HashMap<>();
		initEmailBodyDefaultVariables(emailBodyValueMap);
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(template.getBody(), emailBodyValueMap);
		return renderedTemplate;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWithButtonForForwardProfile(String template, String buttonLink, String buttonText) {
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, getHeaderHTML());
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, getFooterHTML());
		bodyMap.put(GlobalConstants.MasterEmailTemplate, template);
		bodyMap.put(GlobalConstants.NOTE_LINK, buttonLink);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_TEXT, buttonText);
		if (null != userService.getLoggedInUserObject().getProfileSignature()
				&& !userService.getLoggedInUserObject().getProfileSignature().isEmpty()) {
			bodyMap.put("emailSignature", userService.getLoggedInUserObject().getProfileSignature());
		} else {
			bodyMap.put("emailSignature", " ");
		}
		String masterTemplateWithButton = "";
		try {
			masterTemplateWithButton = VelocityTemplateUtils
					.getTemplateString(getTemplateByName("Forward Profile Master Button").getBody(), bodyMap);
		} catch (Exception ex) {
			masterTemplateWithButton = VelocityTemplateUtils
					.getTemplateString(getTemplateByName("Email Master Button").getBody(), bodyMap);

		}
		return masterTemplateWithButton;
	}

	@Transactional(readOnly = true)
	public String getMasterTemplateWith2ButtonForJDShare(String template, String button1txt, String btn1Link,
			String button2txt, String btn2Link) {
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, getHeaderHTML());
		bodyMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, getFooterHTML());
		bodyMap.put(GlobalConstants.MasterEmailTemplate, template);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_1_TEXT, button1txt);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_2_TEXT, button2txt);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_1_LINK, btn1Link);
		bodyMap.put(GlobalConstants.TEMPLATE_BUTTON_2_LINK, btn2Link);

		String masterTemplateWithButton = VelocityTemplateUtils
				.getTemplateString(getTemplateByName("Email Master 2 Button").getBody(), bodyMap);

		return masterTemplateWithButton;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getRenderedTemplateForBoardReport(String positionCode, List<String> candidateFields,
			List<String> statusIn) {
		try {
			Map<String, Object> responseMap = new HashMap<>();

			List<String> statusInSet = new ArrayList<>();
			statusInSet.addAll(statusIn);

			Long totalCandidateCount = roundCandidateService.getCountByPositionCodeAndStatusIn(positionCode,
					statusInSet);

			if (null == totalCandidateCount || totalCandidateCount == 0) {
				throw new RecruizException(ErrorHandler.NO_CANDIDATE_FOUND, ErrorHandler.N0_CANDIDATE);
			}

			Position position = positionService.getPositionByCode(positionCode);
			Board board = position.getBoard();
			Set<Round> boardRound = board.getRounds();

			String templateArray[][] = new String[totalCandidateCount.intValue() + 1][];

			String[] header = new String[candidateFields.size() + 1];

			for (int i = 0; i < header.length - 1; i++) {
				//header[i] = candidateService.getCandidateFiledName(candidateFields.get(i));
				header[i] = candidateFields.get(i);
			}
			header[header.length - 1] = "Current Round/Status";

			// this will be column length
			int candidateInfoLength = candidateFields.size();

			templateArray[0] = new String[candidateInfoLength + 1];
			for (int i = 0; i <= candidateInfoLength; i++) {
				templateArray[0][i] = header[i];
				header[i] = "";
			}

			int roundCount = 0;
			// templateArray[roundCount] = new String[boardRound.size()+1];
			for (Round round : boardRound) {

				Set<RoundCandidate> roundCandidates = round.getCandidates();

				for (RoundCandidate roundCandidate : roundCandidates) {

					if (!statusInSet.contains(roundCandidate.getStatus())) {
						continue;
					}

					templateArray[roundCount + 1] = new String[candidateInfoLength + 1];
					Candidate candidate = roundCandidate.getCandidate();
					int i = 0;
					for (i = 0; i < candidateInfoLength; i++) {
						String value = getCandidateValue(candidateFields.get(i), candidate);
						if (null == value || value.trim().isEmpty()) {
							value = " ";
						}
						templateArray[roundCount + 1][i] = value;
					}
					String status = roundCandidate.getStatus();
					if (null == boardCustomStatusService.getBoardCustomStatusByKey(status)) {
						try {
							status = BoardStatus.valueOf(status).getDisplayName();
						} catch (Exception ex) {
							logger.warn("Status not found");
						}
					} else {
						status = boardCustomStatusService.getBoardCustomStatusByKey(status).getStatusName();
					}
					templateArray[roundCount + 1][i] = round.getRoundName() + " - " + status;

					roundCount++;
				}
			}
			String templateName = "email-template-client-position-report.html";

			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("dataArray", templateArray);
			bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
			String renderedTemplate = getHtmlContentFromFile(bodyMap, templateName);
			String subject = "Progress Report For Opened Position " + position.getTitle();
			try {
				String fileName = StringUtils.cleanFileName(position.getTitle());
				File excelFile = importExportService.resultSetToExcelExport(header, templateArray, fileName, null);
				responseMap.put("excelPath", excelFile.getPath());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

			responseMap.put("body", renderedTemplate);
			responseMap.put("subject", subject);

			return responseMap;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	private String getCandidateValue(String name, Candidate candidate) throws ParseException {

		if (name.equalsIgnoreCase("candidateRandomId")) {
			return candidate.getCandidateRandomId();
		} else if (name.equalsIgnoreCase("fullName")) {
			return candidate.getFullName();
		} else if (name.equalsIgnoreCase("mobile")) {
			return candidate.getMobile();
		} else if (name.equalsIgnoreCase("email")) {
			return candidate.getEmail();
		} else if (name.equalsIgnoreCase("currentCompany")) {
			return candidate.getCurrentCompany();
		} else if (name.equalsIgnoreCase("currentTitle")) {
			return candidate.getCurrentTitle();
		} else if (name.equalsIgnoreCase("currentLocation")) {
			return candidate.getCurrentLocation();
		} else if (name.equalsIgnoreCase("highestQual")) {
			return candidate.getHighestQual();
		} else if (name.equalsIgnoreCase("totalExp")) {
			return candidate.getTotalExp() + " Year(s)";
		} else if (name.equalsIgnoreCase("employmentType")) {
			return candidate.getEmploymentType();
		} else if (name.equalsIgnoreCase("currentCtc")) {
			return candidate.getCurrentCtc() + "";
		} else if (name.equalsIgnoreCase("expectedCtc")) {
			return candidate.getExpectedCtc() + "";
		} else if (name.equalsIgnoreCase("noticeStatus")) {
			if (!candidate.isNoticeStatus()) {
				return candidate.getNoticePeriod() + "";
			}
			return DateTimeUtils.getDateAsString(candidate.getLastWorkingDay(), "dd-MMM-yyyy") + " LWD";

		} else if (name.equalsIgnoreCase("preferredLocation")) {
			return candidate.getPreferredLocation();
		} else if (name.equalsIgnoreCase("keySkills")) {
			return StringUtils.commaSeparate(candidate.getKeySkills());
		} else if (name.equalsIgnoreCase("gender")) {
			return candidate.getGender();
		} else if (name.equalsIgnoreCase("communication")) {
			return candidate.getCommunication();
		} else if (name.equalsIgnoreCase("linkedinProf")) {
			return candidate.getLinkedinProf();
		} else if (name.equalsIgnoreCase("githubProf")) {
			return candidate.getGithubProf();
		} else if (name.equalsIgnoreCase("twitterProf")) {
			return candidate.getTwitterProf();
		} else if (name.equalsIgnoreCase("facebookProf")) {
			return candidate.getFacebookProf();
		} else if (name.equalsIgnoreCase("alternateEmail")) {
			return candidate.getAlternateEmail();
		} else if (name.equalsIgnoreCase("alternateMobile")) {
			return candidate.getAlternateMobile();
		} else if (name.equalsIgnoreCase("nationality")) {
			return candidate.getNationality();
		} else if (name.equalsIgnoreCase("maritalStatus")) {
			return candidate.getMaritalStatus();
		} else if (name.equalsIgnoreCase("previousEmployment")) {
			return candidate.getPreviousEmployment();
		} else if (name.equalsIgnoreCase("address")) {
			return candidate.getAddress();
		} else if (name.equalsIgnoreCase("longestStayInCompany")) {
			Double longestStay = candidate.getLongestStayInCompany();
			if (null == longestStay || longestStay <= 0) {
				return "0 Year";
			}
			Double longestStayInYear = longestStay / 12;

			DecimalFormat dec = new DecimalFormat("#0.0");
			return dec.format(longestStayInYear) + " Year(s)";

		} else if (name.equalsIgnoreCase("averageStayInCompany")) {
			Double avgStay = candidate.getAverageStayInCompany();
			if (null == avgStay || avgStay <= 0) {
				return "0 Year";
			}
			Double avgStayInComp = avgStay / 12;
			DecimalFormat dec = new DecimalFormat("#0.0");
			return dec.format(avgStayInComp) + " Year(s)";

		} else if (name.equalsIgnoreCase("languages")) {
			return candidate.getLanguages();
		} else if (name.equalsIgnoreCase("industry")) {
			return candidate.getIndustry();
		} else if (candidate.getCustomField().containsKey(name)) {
			return candidate.getCustomField().get(name);
		}

		return "";
	}

	private static String getNextDate(Date date, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);

		String date1 = sdf.format(date);

		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(date1));
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
		c.add(Calendar.DATE, 1); // number of days to add
		date1 = sdf.format(c.getTime());

		return date1;
	}

	// to get rendered template for forward profile
	@Transactional(readOnly = true)
	public Map<String, Object> getRenderedTemplateForCandidateForwardProfile(String positionCode,
			List<String> candidateFields, List<Long> roundCandidateIds, Boolean roundStatus) {

		Set<Long> candidateIds = new HashSet<>();
		candidateIds.addAll(roundCandidateIds);


		String checkSLno = candidateFields.get(0);

		if(!checkSLno.equalsIgnoreCase("Sl_No")){

			List<String> newList = new ArrayList<>();
			newList.add("Sl_No");

			for (String string : candidateFields) {
				newList.add(string);
			}
			candidateFields = newList;
		}



		try {
			Map<String, Object> responseMap = new HashMap<>();

			if (null == roundCandidateIds || roundCandidateIds.isEmpty()) {
				throw new RecruizException(ErrorHandler.NO_CANDIDATE_FOUND, ErrorHandler.N0_CANDIDATE);
			}

			Position position = positionService.getPositionByCode(positionCode);

			String templateArray[][] = new String[candidateIds.size() + 1][];
			String[] header = null;
			if (roundStatus) {
				header = new String[candidateFields.size() + 1];
				for (int i = 0; i < header.length - 1; i++) {
					String fieldName = candidateService.getCandidateFiledName(candidateFields.get(i));
					if (null == fieldName) {
						fieldName = candidateFields.get(i);
					}
					header[i] = fieldName;
				}

				if (roundStatus) {
					header[header.length - 1] = "Current Round/Status";
				}
			} else {
				header = new String[candidateFields.size()];
				for (int i = 0; i < candidateFields.size(); i++) {
					String fieldName = candidateService.getCandidateFiledName(candidateFields.get(i));
					if (null == fieldName) {
						fieldName = candidateFields.get(i);
					}
					header[i] = fieldName;
				}
			}

			// this will be column length
			int candidateInfoLength = candidateFields.size();

			if (roundStatus) {
				templateArray[0] = new String[candidateInfoLength + 1];
			} else {
				templateArray[0] = new String[candidateInfoLength];
			}

			int candidateInfoColumnSize = candidateInfoLength - 1;
			if (roundStatus) {
				candidateInfoColumnSize = candidateInfoLength;
			}
			for (int i = 0; i <= candidateInfoColumnSize; i++) {
				templateArray[0][i] = header[i];
				header[i] = "";
			}

			int roundCount = 0;

			List<RoundCandidate> roundCandidates = roundCandidateService.getCandidatedByIdsIin(candidateIds);

			for (RoundCandidate roundCandidate : roundCandidates) {
				templateArray[roundCount + 1] = new String[candidateInfoLength];
				if (roundStatus) {
					templateArray[roundCount + 1] = new String[candidateInfoLength + 1];
				}

				Candidate candidate = roundCandidate.getCandidate();
				int i = 0;
				for (i = 0; i < candidateInfoLength; i++) {

					if(i==0){
						templateArray[roundCount + 1][i] = String.valueOf(roundCount + 1);
						continue;
					}

					String value = getCandidateValue(candidateFields.get(i), candidate);
					if (candidateFields.get(i).equalsIgnoreCase("noticestatus")) {

						if (value.contains("LWD")) {
							String trimString = value.replace("LWD", "");
							trimString = trimString.trim();
							String dateformat = "dd-MMM-yyyy";
							SimpleDateFormat sdt = new SimpleDateFormat(dateformat);
							Date actuldate = sdt.parse(trimString);
							String nextdate = getNextDate(actuldate, dateformat);
							if (null == nextdate || nextdate.trim().isEmpty()) {
								value = " ";
								templateArray[roundCount + 1][i] = nextdate;
							} else {
								nextdate = "LWD - " + nextdate;
								templateArray[roundCount + 1][i] = nextdate;
							}

						} else if (null == value || value.trim().isEmpty() || value.equalsIgnoreCase("0")) {
							value = "N/A";
							templateArray[roundCount + 1][i] = value;

						} else {
							value = value + " days";
							templateArray[roundCount + 1][i] = value;
						}

					} else {

						if (null == value || value.trim().isEmpty()) {						
							value = getPositionValue(candidateFields.get(i), position);						
							if(null == value || value.trim().isEmpty())
								value = "N/A";
						}
						templateArray[roundCount + 1][i] = value;
					}

				}

				String status = roundCandidate.getStatus();
				if (null == boardCustomStatusService.getBoardCustomStatusByKey(status)) {
					try {
						status = BoardStatus.valueOf(status).getDisplayName();
					} catch (Exception ex) {
						logger.warn("Status not found");
					}
				} else {
					status = boardCustomStatusService.getBoardCustomStatusByKey(status).getStatusName();
				}

				if (roundStatus) {
					templateArray[roundCount + 1][i] = roundCandidate.getRound().getRoundName() + " - " + status;
				}

				roundCount++;
			}

			String templateNameTable = "email-template-forward-profile-table.html";

			Map<String, Object> bodyMap = new HashMap<>();
			bodyMap.put("dataArray", templateArray);
			bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
			String renderedTemplateTable = getHtmlContentFromFile(bodyMap, templateNameTable);
			renderedTemplateTable = "<br><br>"+renderedTemplateTable+"<br><br>";
			String subject = "Profile(s) forwarded for " + position.getTitle() + " "
					+ position.getClient().getClientName();

			bodyMap.put("positionTable", renderedTemplateTable);

			String templateName = "email-template-forward-profile.html";
			String renderedTemplate = getHtmlContentFromFile(bodyMap, templateName);

			responseMap.put("table", renderedTemplateTable);

			try {
				String fileName = StringUtils.cleanFileName(position.getTitle());
				File excelFile = importExportService.resultSetToExcelExport(header, templateArray, fileName, null);
				responseMap.put("excelPath", excelFile.getPath());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

			responseMap.put("body", renderedTemplate);
			responseMap.put("subject", subject);

			return responseMap;

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	private String getPositionValue(String name, Position position) throws ParseException {

		if (name.equalsIgnoreCase("Client Name")) {
			return position.getClient().getClientName();
		} else if (name.equalsIgnoreCase("Position Name")) {
			return position.getTitle();
		} else if (name.equalsIgnoreCase("Requisition Id")) {
			return position.getRequisitionId();
		} else if (name.equalsIgnoreCase("Job Location")) {
			return position.getLocation();
		} else if (name.equalsIgnoreCase("Job Description")) {
			return position.getDescription();
		} else if (name.equalsIgnoreCase("Inteview Date")) {
			return DateTimeUtils.getDateAsString(position.getOpenedDate(), "dd-MMM-yyyy");
		} else if (name.equalsIgnoreCase("Skill Set")) {
			return String.join(",", position.getGoodSkillSet());
		} else if (name.equalsIgnoreCase("Education Qualification")) {
			return String.join(",", position.getEducationalQualification());
		} else if (name.equalsIgnoreCase("Hiring Manager")) {
			return position.getHiringManager();
		} else if (name.equalsIgnoreCase("Vertical/Cluster")) {
			return position.getVerticalCluster();
		} else if (name.equalsIgnoreCase("End Client")) {
			return position.getEndClient();
		} else if (name.equalsIgnoreCase("Screener")) {
			return position.getScreener();
		} else if (name.equalsIgnoreCase("Internal Spoc")) {
			return position.getSpoc();
		}

		Map<String,String> dta = position.getCustomField();
		for ( Map.Entry<String, String> entry : dta.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if(name.equalsIgnoreCase(key))
				return value;

		}

		return "";
	}

	public boolean getRenderedTemplateForRequestForApproval(OfferLetterApprovalDTO approvalDTO, String worklowId, long approvalID) {
		List<String> newList = new ArrayList<>();
		newList.add("Client Name");newList.add("Candidate Name");newList.add("Skills");newList.add("Experience");
		/*newList.add("% Hike");*/newList.add("CTC Offered");newList.add("Margin %");newList.add("Bill rate");newList.add("Annual Cost");

		String templateArray[][] = new String[2][];
		String[] header = new String[newList.size()];

		for (int i=0;i<newList.size();i++) {
			header[i] = newList.get(i);
		}

		templateArray[0] = new String[newList.size()];

		for (int i = 0; i < newList.size(); i++) {
			templateArray[0][i] = header[i];
			header[i] = "";
		}

		Position position = positionService.findOne(Long.parseLong(approvalDTO.getPositionId()));
		Candidate candidate = candidateService.findOne(Long.parseLong(approvalDTO.getCandidateId()));
		
		double currentCTC = candidate.getCurrentCtc();
		
		Set<String> skills = candidate.getKeySkills();
		String candidateSkills = "";
		int y = 1;
		for (String skill : skills) {
			if(y==1)
				candidateSkills = skill;
			else
				candidateSkills = candidateSkills + "," + skill;
			y++;

			if(y==3)
				break;
		}

		templateArray[1] = new String[newList.size()];

		templateArray[1][0] = approvalDTO.getClientName();
		templateArray[1][1] = approvalDTO.getCandidateName();
		templateArray[1][2] = candidateSkills;
		templateArray[1][3] = String.valueOf(candidate.getTotalExp());
	//	templateArray[1][4] = "0 %";
		templateArray[1][4] = String.valueOf(approvalDTO.getCompensationAnnual());
		templateArray[1][5] = String.valueOf(approvalDTO.getGrossMarginPercentage())+" %";
		templateArray[1][6] = approvalDTO.getBillRatePerHour();
		templateArray[1][7] = approvalDTO.getBillingProjectDuration();

		String templateNameTable = "email-template-offerletter-approval-table.html";

		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("dataArray", templateArray);
		bodyMap.put(GlobalConstants.POSITION_NAME, approvalDTO.getCandidateName());
		String renderedTemplateTable = getHtmlContentFromFile(bodyMap, templateNameTable);
		renderedTemplateTable = "<br><br>"+renderedTemplateTable+"<br><br>";
		String subject = "Approval Request";

		bodyMap.put("positionTable", renderedTemplateTable);

		String templateName = "email-template-offerletter-approval.html";
		String renderedTemplate = getHtmlContentFromFile(bodyMap, templateName);												

		OfferLetterWorkflow workflow = offerLetterWorkFlowService.findOne(Long.parseLong(worklowId));

		List<String> emailTo = new ArrayList<>();
		emailTo.add(workflow.getApprover_email());

		List<String> emailCC = new ArrayList<>();

		if(workflow.getOther_loop().contains(",")){
			String[] otherLoopList = workflow.getOther_loop().split(",");
			for (String otherloopemail : otherLoopList) {
				emailCC.add(otherloopemail);	
			}
		}else{
			if(workflow.getOther_loop().equalsIgnoreCase("") || workflow.getOther_loop().isEmpty())
				emailCC = null;
			else
				emailCC.add(workflow.getOther_loop());
		}
		try {
			logger.error("send mail for approval request = "+emailTo);
			renderedTemplate = renderedTemplate.replace("${approverName}", workflow.getApprover_name());
			renderedTemplate = renderedTemplate.replace("${candidateName}", candidate.getFullName());
			
			String buttonText = "View Details";
			final String email = userService.getLoggedInUserEmail();
			int linkValidityHour = (int) (Integer.parseInt(extendTokenForDays) * 24);
			final String externalUserAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(email,
					TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour,
					userService.getLoggedInUserObject().getTimezone(), userService.getLoggedInUserObject().getLocale());

			String token = baseUrl + GlobalConstants.OFFER_APPROVAL_PROFILE_URL + GlobalConstants.PASSKEY + externalUserAuthToken
					+ GlobalConstants.ROUND_AND_CANDIDATE_ID + approvalDTO.getCandidateId() + GlobalConstants.POSITION_CODE
					+ position.getPositionCode()+GlobalConstants.OFFER_APPROVAL_ID+String.valueOf(approvalID)+"&approvalMail="+workflow.getApprover_email();
			
			String renderedTemplateTo = getMasterTemplateWithButtonForForwardProfile(renderedTemplate, token, buttonText);
			emailService.sendBulkEmail(emailTo, renderedTemplateTo,subject, null, userService.getLoggedInUserEmail(),null);
			
			
			if(emailCC!=null && emailCC.size()>0){

					String tokenCC = baseUrl + GlobalConstants.OFFER_APPROVAL_PROFILE_URL + GlobalConstants.PASSKEY + externalUserAuthToken
							+ GlobalConstants.ROUND_AND_CANDIDATE_ID + approvalDTO.getCandidateId() + GlobalConstants.POSITION_CODE
							+ position.getPositionCode()+GlobalConstants.OFFER_APPROVAL_ID+String.valueOf(approvalID)+"&approvalMail="+"";
					
					String renderedTemplateCC = getMasterTemplateWithButtonForForwardProfile(renderedTemplate, tokenCC, buttonText);
					emailService.sendBulkEmail(emailCC, renderedTemplateCC,subject, null, userService.getLoggedInUserEmail(),null);
				
			}
			
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}


		return false;
	}

	public void sendMailOfApproverResponse(OfferLetterApprovals offerLetterApproval, boolean isAccepted) throws MessagingException, IOException, RecruizException {
		
		String requestStatus = "";
		if(isAccepted)
			requestStatus = GlobalConstants.ACCEPTED;
		else
			requestStatus = GlobalConstants.REJECTED;
		
		Candidate candidate = candidateService.findOne(offerLetterApproval.getCandidate_id());
		User user = userService.findOne(offerLetterApproval.getRequest_send_from_user());
		OfferLetterWorkflow offerLetterWorkflow = offerLetterWorkFlowService.findOne(offerLetterApproval.getWorkflow_id());
		
		String templateNameTable = "email-template-offerletter-approver-response.html";
		String acceptedLastLine = "Please login to Recruiz to generate the offer letter.";

		
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("requestUser", user.getUsername());
		bodyMap.put("candidateName", candidate.getFullName());
		bodyMap.put("candidateEmail", candidate.getEmail());
		bodyMap.put("status", requestStatus);
		
		if(isAccepted)
			bodyMap.put("acceptedLastLine", acceptedLastLine);
		else
			bodyMap.put("acceptedLastLine", "");
		
		String renderedTemplate = getHtmlContentFromFile(bodyMap, templateNameTable);
		renderedTemplate = "<br><br>"+renderedTemplate+"<br><br>";
		String subject = "Approval Response";
		
		logger.error("mail template content templateNameTable = "+renderedTemplate+" requestuser = "+user+" candidate ="+candidate);
		
		
		
		renderedTemplate = renderedTemplate.replace("${requestUser}", user.getEmail().split("@")[0]);
		renderedTemplate = renderedTemplate.replace("${candidateName}", candidate.getFullName());
		renderedTemplate = renderedTemplate.replace("${status}", requestStatus);
		
		String emailTemplate = getMasterTemplateWithoutButton(renderedTemplate);
		
		List<String> emailList = new ArrayList<String>();
		emailList.add(user.getEmail());
		
		List<String> emailCC = new ArrayList<>();

		if(offerLetterWorkflow.getOther_loop().contains(",")){
			String[] otherLoopList = offerLetterWorkflow.getOther_loop().split(",");
			for (String otherloopemail : otherLoopList) {
				emailCC.add(otherloopemail);	
			}
		}else{
			if(offerLetterWorkflow.getOther_loop().equalsIgnoreCase("") || offerLetterWorkflow.getOther_loop().isEmpty())
				emailCC = null;
			else
				emailCC.add(offerLetterWorkflow.getOther_loop());
		}
		
		
		emailService.sendBulkEmail(emailList, emailTemplate, subject, null,offerLetterWorkflow.getApprover_email(), emailCC);
	}

}
