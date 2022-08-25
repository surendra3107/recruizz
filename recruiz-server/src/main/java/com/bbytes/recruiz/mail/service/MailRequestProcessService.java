package com.bbytes.recruiz.mail.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.EmailMessageModel;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.ExternalUserService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.IResumeParserService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Service
public class MailRequestProcessService extends AbstractMailgunService {

	private static final Logger logger = LoggerFactory.getLogger(MailRequestProcessService.class);

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private IResumeParserService parserService;

	@Autowired
	private ExternalUserService externalUserService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	public void processAndSaveMessageRequest(HttpServletRequest request) throws RecruizException {

		// logger.error("Reached to process request");

		// TODO this if condition was locking every connection to db for every
		// email sent so commented the code
		// TODO this if condition is called before tenant id is set so it will
		// never work properly
		// if
		// (!emailMessageModelService.messageExists(request.getParameter(MailConstant.MAIL_PARAM_MESSAGE_ID)))
		// {

		// Example of recruizmail emails
		// inspirar_tech_systems_pvt_ltd@recruizmail.com
		// inspirar_tech_systems_pvt_ltd.j_31@recruizmail.com

		String tenant, positionCode;
		String emailId_to = request.getParameter(MailConstant.MAIL_PARAM_TO);

		int sepraterIndex_to = emailId_to.indexOf(".");
		int atIndex_to = emailId_to.indexOf("@");

		if (atIndex_to > sepraterIndex_to) {
			tenant = emailId_to.substring(0, sepraterIndex_to).trim();
			positionCode = emailId_to.substring(sepraterIndex_to + 1, atIndex_to).trim();
			logger.error("In To block - " +"tenant: " +tenant);
			logger.error("In To block - " +"position code: "+positionCode);
		} else {
			tenant = emailId_to.substring(0, atIndex_to).trim();
			positionCode = null;
		}

		// Try getting from recipient or cc or bcc if tenant has not been decoded from
		// To emailID
		if (tenant == null || tenant == "") {
			String email_recipient = request.getParameter(MailConstant.MAIL_PARAM_RECEPIENT);
			if (email_recipient != null || email_recipient != "") {
				int sepraterIndex_recipient = email_recipient.indexOf(".");
				int atIndex_recipient = email_recipient.indexOf("@");

				if (atIndex_recipient > sepraterIndex_recipient) {
					tenant = email_recipient.substring(0, sepraterIndex_recipient).trim();
					positionCode = email_recipient.substring(sepraterIndex_recipient + 1, atIndex_recipient).trim();
					logger.error("In Recepient block - " +"tenant: " +tenant);
					logger.error("In Recepient block - " +"position code: "+positionCode);
				} else {
					tenant = email_recipient.substring(0, atIndex_recipient).trim();
					positionCode = null;
				}
			}

		}

		if (tenant == null || tenant == "") {

			String emailId_cc = request.getParameter(MailConstant.MAIL_PARAM_CC);
			if (emailId_cc != null || emailId_cc != "") {
				int sepraterIndex_cc = emailId_cc.indexOf(".");
				int atIndex_cc = emailId_cc.indexOf("@");

				if (atIndex_cc > sepraterIndex_cc) {
					tenant = emailId_cc.substring(0, sepraterIndex_cc).trim();
					positionCode = emailId_cc.substring(sepraterIndex_cc + 1, atIndex_cc).trim();
					logger.error("In CC block - " +"tenant: " +tenant);
					logger.error("In CC block - " +"position code: "+positionCode);
				} else {
					tenant = emailId_cc.substring(0, atIndex_cc).trim();
					positionCode = null;
				}

			}

		}

		if (tenant == null || tenant == "") {

			String emailId_bcc = request.getParameter(MailConstant.MAIL_PARAM_BCC);
			if (emailId_bcc != null || emailId_bcc != "") {
				int sepraterIndex_bcc = emailId_bcc.indexOf(".");
				int atIndex_bcc = emailId_bcc.indexOf("@");

				if (atIndex_bcc > sepraterIndex_bcc) {
					tenant = emailId_bcc.substring(0, sepraterIndex_bcc).trim();
					positionCode = emailId_bcc.substring(sepraterIndex_bcc + 1, atIndex_bcc).trim();
					logger.error("In BCC block - " +"tenant: " +tenant);
					logger.error("In BCC block - " +"position code: "+positionCode);
				} else {
					tenant = emailId_bcc.substring(0, atIndex_bcc).trim();
					positionCode = null;
				}

			}

		}

		logger.error("Organization name is " + tenant);
		logger.error("Position code is " + positionCode);
		// setting the tenant here

		// removing special chars from begining of tenant added by outlook
		tenant = tenant.trim();
		while (!Character.isLetterOrDigit(tenant.charAt(0))) {
			tenant = tenant.substring(1, tenant.length());
		}
		if (!tenantResolverService.isTenantValid(tenant)) {
			logger.error("TEnant resolver not valid " + tenant);
			return;
		}

		String tenantId = tenantResolverService.getTenant(tenant);
		TenantContextHolder.setTenant(tenantId);
		logger.error("tenant from resolver" + tenantId);
		// logger.error("Tenant Set");

		EmailMessageModel message = new EmailMessageModel();
		message.setFromEmail(request.getParameter(MailConstant.MAIL_PARAM_FROM));
		message.setToEmail(request.getParameter(MailConstant.MAIL_PARAM_TO));
		message.setCc(request.getParameter(MailConstant.MAIL_PARAM_CC));
		message.setSubject(request.getParameter(MailConstant.MAIL_PARAM_SUBJECT));
		message.setHtmlBody(request.getParameter(MailConstant.MAIL_PARAM_HTML_BODY));
		message.setMessageId(request.getParameter(MailConstant.MAIL_PARAM_MESSAGE_ID));
		message.setTextBody(request.getParameter(MailConstant.MAIL_PARAM_TEXT_BODY));

		// message.setTimestamp(DateUtil.parse(request.getParameter(MailConstant.MAIL_PARAM_DATE)));
		message.setInReplyTo(request.getParameter(MailConstant.MAIL_PARAM_IN_REPLY_TO));
		message.setDomain(request.getParameter(MailConstant.MAIL_PARAM_DOMAIN));
		message.setMessageHeader(request.getParameter(MailConstant.MAIL_PARAM_HEADER));
		message.setMessageURL(request.getParameter(MailConstant.MAIL_PARAM_MESSAGE_URL));
		message.setRecieved(StringUtils.yesNoBoolean(request.getParameter(MailConstant.MAIL_PARAM_INCOMING_YES_NO)));

		try {
			String attachmentsJson = request.getParameter(MailConstant.MAIL_PARAM_ATTACHMENTS);
			if (attachmentsJson != null && !attachmentsJson.trim().isEmpty()) {
				List<MailgunMailAttachment> attachments = new ObjectMapper().readValue(attachmentsJson,
						TypeFactory.defaultInstance().constructCollectionType(List.class, MailgunMailAttachment.class));
				Set<Map<String, Object>> retrievedAttchments = retrieveAttachment(message, attachments);
				// this method will process retrieved attachments
				processRetrievedAttachments(retrievedAttchments, positionCode, message.getFromEmail());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// TODO
		// long text issue is there, will come back to this issue later so
		// commenting this code to save message

		// message = emailMessageModelService.save(message);
		// if (message.getMid() > 0) {
		// logger.info("Email message saved successfully : " + message);
		// }
		// }
	}

	/**
	 * This method will retrieve all attachment and will return a List of map of
	 * object which will contain candidate and file info
	 * 
	 * @param message
	 * @param attachments
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private Set<Map<String, Object>> retrieveAttachment(final EmailMessageModel message,
			List<MailgunMailAttachment> attachments) throws IOException, Exception {

		final Set<Map<String, Object>> retrievedAttchments = new HashSet<>();

		for (final MailgunMailAttachment mailAttachment : attachments) {

			client.restClient().execute(mailAttachment.getUrl(), HttpMethod.GET, new RequestCallback() {

				@Override
				public void doWithRequest(ClientHttpRequest request) throws IOException {
					request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
				}
			}, new ResponseExtractor<Void>() {

				@Override
				public Void extractData(ClientHttpResponse response) throws IOException {
					try {
						Map<String, Object> attachmentFilesMap = new HashMap<>();
						InputStream fsStream = response.getBody();
						String filePath = message.addAttachment(fsStream, mailAttachment.getName(),
								mailAttachment.getContentType(), uploadFileService);
						logger.info("Processing attachment " + mailAttachment.getName());

						Candidate candidate = parserService.parseResumeForExternalUser(filePath,
								mailAttachment.getName());

						attachmentFilesMap.put("filePath", filePath);
						attachmentFilesMap.put("fileName", mailAttachment.getName());
						attachmentFilesMap.put("contentType", mailAttachment.getContentType());
						attachmentFilesMap.put("candidate", candidate);

						retrievedAttchments.add(attachmentFilesMap);

					} catch (RecruizException e) {
						logger.error(e.getMessage(), e);
					}
					return null;
				}
			});
		}
		return retrievedAttchments;
	}

	/**
	 * This method will process all the retrieved attachment and will send email to
	 * sender with the details of processed files
	 * 
	 * @param retrievedAttchments
	 * @param positionCode
	 * @param fromEmail
	 * @throws RecruizException
	 * @throws RecruizWarnException
	 * @throws IOException
	 */
	@Transactional
	private void processRetrievedAttachments(Set<Map<String, Object>> retrievedAttchments, String positionCode,
			String fromEmail) throws RecruizException, RecruizWarnException, IOException {

		List<String> processedCandidate = new ArrayList<>();
		File resumeFile = null;

		if (retrievedAttchments != null && !retrievedAttchments.isEmpty()) {
			for (Map<String, Object> map : retrievedAttchments) {
				try {
					String attachmentFilePath = (String) map.get("filePath");
					String fileName = (String) map.get("fileName");
					Candidate candidate = (Candidate) map.get("candidate");

					resumeFile = FileUtils.createTempFileCopy(attachmentFilePath);
					if (resumeFile == null || !resumeFile.exists())
						continue;

					if (candidate.getFullName() == null || candidate.getFullName().trim().isEmpty()) {
						candidate.setFullName("Name Missing");
					}

					// if the file has no email then it will be return by
					// throwing exception
					if ((candidate == null) || (candidate.getEmail() == null || candidate.getEmail().isEmpty())) {
						processedCandidate.add("<br/>Cannot process candidate information, File name : " + fileName);
					} else {

						// add candidate to db here
						if (positionCode != null && !positionCode.isEmpty()) {
							fromEmail = fromEmail.trim();
							String owner = null;
							try {
								owner = fromEmail.substring(fromEmail.indexOf("<") + 1, fromEmail.length() - 1);
							} catch (Exception ex) {
							}
							externalUserService.addCandidateFromExternalSource(candidate.getFullName(),
									candidate.getMobile(), candidate.getEmail(), resumeFile, positionCode,
									"Email Share", "", "", fromEmail, owner);

							// adding processed candidate to list to send email
							processedCandidate.add("<br/>#File : " + fileName + " #Candidate Details : "
									+ candidate.getFullName() + "(" + candidate.getEmail() + ")");

							// making entry to analytics
							externalUserService.addtoSharedLinkAnalytics(positionCode, fromEmail,
									GlobalConstants.AnalyticsEvenTypeApplied, "Email Shared");

						} else {
							if (candidateService.isCandidateExists(candidate.getEmail())) {
								processedCandidate.add(
										"<br/>#File : " + fileName + " #Candidate Details : " + candidate.getFullName()
												+ "(" + candidate.getEmail() + ") already exists in database");
							} else {
								candidateService.setDefaultValues(candidate);
								candidate.setOwner(fromEmail);
								candidate.setSource(GlobalConstants.EMAIL_SHARE);
								candidate.setSourceDetails(fromEmail);
								candidate.setSourcedOnDate(new Date());

								logger.error("Before saving checking tenant, Tenant is : "
										+ TenantContextHolder.getTenant());

								candidateService.save(candidate);
								uploadFileService.createFolderStructureForCandidate(folderPath,
										candidate.getCid() + "");
								String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, fileName,
										"resume", candidate.getCid() + "");
								String pdfFilePath = fileService.convert(serverPath);
								candidate.setResumeLink(pdfFilePath);
								candidateService.save(candidate);
								// adding original resume to candidate files
								candidateService.uploadCandidateFiles(serverPath, fileName,
										FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "",
										pdfFilePath);
								// adding candidate activity
								candidateActivityService.addActivity("Sourced via email share", fromEmail,
										candidate.getCid() + "", CandidateActivityType.Added.getDisplayName());
								// adding processed candidate to list to send
								// email
								processedCandidate.add("<br/>#File : " + fileName + " #Candidate Details : "
										+ candidate.getFullName() + "(" + candidate.getEmail() + ")");

								// making entry to analytics
								externalUserService.addtoSharedLinkAnalytics(positionCode, fromEmail,
										GlobalConstants.AnalyticsEvenTypeApplied, "Email Shared");

								// masking resume
								try {
									Map<String, String> maskedResumeFiles = candidateService
											.maskResume(candidate.getCid());
									if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
										candidateService.attachMaskedResumeToCadidate(maskedResumeFiles,
												candidate.getCid());
									}
								} catch (Exception ex) {
									logger.warn("\n\n\n*******Failed to mask resume*********", ex);
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					processedCandidate.add("<br/>Can not process candidate, File name : " + (String) map.get("fileName")
							+ " -> " + ex.toString());
				} finally {
					if (resumeFile != null && resumeFile.exists()) {
						resumeFile.delete();
					}
				}
			}
		}

		// sending reply email here
		List<String> emailList = new ArrayList<>();
		emailList.add(fromEmail);

		if (processedCandidate == null || processedCandidate.isEmpty()) {
			String templateData = "Hi, </br></br>candidate resume sent to <b>Recruiz</b> failed to process.";
			emailService.sendEmail(emailList, templateData, "Auto Reply : Candidate Processing Failed", true);
		} else {
			String renderedReplyEmailTeplate = emailTemplateDataService
					.getRenderedTemplateForResumeEmailReply(positionCode, processedCandidate);
			emailService.sendEmail(emailList, renderedReplyEmailTeplate,
					"Auto Reply : Candidate Processed " + new Date().toLocaleString(), true);
		}
	}
}
