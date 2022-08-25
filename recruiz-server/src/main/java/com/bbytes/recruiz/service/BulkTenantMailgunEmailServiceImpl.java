package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.mailgun.api.ResponseCallback;
import com.bbytes.mailgun.client.MailgunClient;
import com.bbytes.mailgun.model.MailMessage;
import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.mailgun.util.MailMessageBuilder;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.ErrorHandler;

public class BulkTenantMailgunEmailServiceImpl extends MailgunEmailServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(BulkTenantMailgunEmailServiceImpl.class);

	public BulkTenantMailgunEmailServiceImpl(String mailgunAPIKey, String mailgunDomain) {
		client = MailgunClient.create(mailgunAPIKey);
		mailOperations = client.mailOperations(mailgunDomain);
	}

	/**
	 * Send simple email using html template.
	 * 
	 * @throws RecruizException
	 */
	@Override
	public void sendEmail(List<String> emailList, Map<String, Object> emailBody, String subject, String template) throws RecruizException {
		String emailHTMLContent = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, template, "UTF-8", emailBody);
		sendEmail(emailList, emailHTMLContent, subject);
	}

	/**
	 * Send Simple email.
	 * 
	 * @throws RecruizException
	 */
	@Override
	public void sendEmail(List<String> emailList, String emailBody, String subject) throws RecruizException {
		sendEmail(emailList, emailBody, subject, true);
	}

	@Override
	public void sendEmail(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML) throws RecruizException {
		sendEmail(emailList, fromEmail, emailBody, subject, emailBodyIsHTML);

	}

	/**
	 * Overloaded method of send email to initialize the fromEmail variable
	 * 
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param emailBodyIsHTML
	 * @param fromEmail
	 */
	public void sendEmail(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML, String fromUserEmail) {
		try {

			if (fromUserEmail != null && !fromUserEmail.isEmpty()) {
				sendEmail(emailList, fromUserEmail, emailBody, subject, emailBodyIsHTML);
			} else {
				sendEmail(emailList, fromEmail, emailBody, subject, emailBodyIsHTML);
			}

		} catch (RecruizException e) {
			logger.error(e.getErrConstant(), e);
		}

	}

	public void sendEmail(final List<String> emailList, String fromEmailID, String emailBody, String subject, boolean emailBodyIsHTML)
			throws RecruizException {

		try {
			String[] recipients = emailList.toArray(new String[emailList.size()]);
			ResponseCallback<MailgunSendResponse> mailRequestCallback = new ResponseCallback<MailgunSendResponse>() {

				@Override
				public void onSuccess(MailgunSendResponse response) {
					// updating the email count under on success call back
					orgConfigService.updateEmailCount(emailList.size());
					if (!response.isOk())
						logger.error("Mailgun email send failed " + response.getMessage());
					else
						logger.info("Mailgun email send successful");
				}

				@Override
				public void onFailure(Throwable ex) {
					logger.error("Mailgun email send failed " + ex);

				}
			};

			if (emailBodyIsHTML) {
				mailOperations.sendHtmlMailAsync(fromEmailID, recipients, subject, emailBody, mailRequestCallback);
			} else {
				mailOperations.sendTextMailAsync(fromEmailID, recipients, subject, emailBody, mailRequestCallback);
			}
			// add to email activity table
			try {
				// emailActivityService.saveEmail(emailBody, subject,
				// StringUtils.commaSeparate(emailList), null,
				// fromEmailID);
			} catch (Exception ex) {
				logger.warn(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			throw new RecruizException(e);
		}
	}

	/**
	 * Send calendar invitation by email.
	 */
	@Override
	public void sendCalenderInvite(final List<String> emailList, String emailBody, String subject, String fileName, String fromUserEmail,
			final List<String> ccList, List<String> selectedFiles) throws RecruizException, MessagingException, IOException {

		String[] recipients = emailList.toArray(new String[emailList.size()]);
		MailMessage message = null;
		if (fromUserEmail != null && !fromUserEmail.isEmpty()) {
			if (fileName != null && !fileName.isEmpty()) {
				message = createMessage(emailBody, subject, fileName, fromUserEmail, recipients);
			} else {
				message = MailMessageBuilder.create().html(emailBody).subject(subject).to(recipients).from(fromUserEmail).build();
			}
		} else {
			if (fileName != null && !fileName.isEmpty()) {
				message = createMessage(emailBody, subject, fileName, fromEmail, recipients);
			} else {
				File attachmentFile = getFileFromPath(fileName);
				if (attachmentFile != null && attachmentFile.exists()) {
					message = MailMessageBuilder.create().addAttachment(attachmentFile).html(emailBody).subject(subject).to(recipients)
							.from(fromEmail).build();
				} else {
					message = MailMessageBuilder.create().html(emailBody).subject(subject).to(recipients).from(fromEmail).build();
				}
			}
		}

		if (null != ccList && !ccList.isEmpty()) {
			String[] cc = ccList.toArray(new String[ccList.size()]);
			message.addCc(cc);
		}

		mailOperations.sendMailAsync(message, new ResponseCallback<MailgunSendResponse>() {
			@Override
			public void onSuccess(MailgunSendResponse response) {
				// updating the email count under on success call back
				orgConfigService.updateEmailCount(emailList.size());

				if (!response.isOk()) {
					logger.error("Mailgun email send failed" + response.getMessage());
				} else {
					logger.info("Mailgun email send successful");
					// add to email activity table
					try {
						// emailActivityService.saveEmail(emailBody, subject,
						// StringUtils.commaSeparate(emailList),
						// ccList,fromUserEmail);
					} catch (Exception ex) {
						logger.warn(ex.getMessage(), ex);
					}
				}

			}

			@Override
			public void onFailure(Throwable ex) {
				logger.error("Mailgun email send failed" + ex);

			}
		});
	}

	private MailMessage createMessage(String emailBody, String subject, String fileName, String fromUserEmail, String[] recipients) {
		MailMessage message;
		File attachmentFile = getFileFromPath(fileName);

		if (attachmentFile != null && attachmentFile.exists()) {
			message = MailMessageBuilder.create().addAttachment(attachmentFile).html(emailBody).subject(subject).to(recipients)
					.from(fromUserEmail).build();
		} else {
			message = MailMessageBuilder.create().html(emailBody).subject(subject).to(recipients).from(fromUserEmail).build();
		}
		return message;
	}

	/**
	 * Send calendar invitation by email.
	 */
	@Override
	public void sendCandidateCalenderInvite(final List<String> emailList, Map<String, Object> emailBody, String subject, String template,
			String fileName, String fromUserEmail, List<String> selectedFiles) throws RecruizException, MessagingException, IOException {

		String[] recipients = emailList.toArray(new String[emailList.size()]);
		MailMessage message = null;

		File attachmentFile = getFileFromPath(fileName);

		if (fromUserEmail != null && !fromUserEmail.isEmpty()) {
			if (attachmentFile != null) {
				message = MailMessageBuilder.create().addAttachment(attachmentFile).html(template).subject(subject).to(recipients)
						.from(fromUserEmail).build();
			} else {
				message = MailMessageBuilder.create().html(template).subject(subject).to(recipients).from(fromUserEmail).build();
			}

		} else {

			if (attachmentFile != null) {
				message = MailMessageBuilder.create().addAttachment(attachmentFile).html(template).subject(subject).to(recipients)
						.from(fromEmail).build();
			} else {
				message = MailMessageBuilder.create().html(template).subject(subject).to(recipients).from(fromEmail).build();
			}

		}

		mailOperations.sendMailAsync(message, new ResponseCallback<MailgunSendResponse>() {

			@Override
			public void onSuccess(MailgunSendResponse response) {
				// updating the email count under on success call back
				orgConfigService.updateEmailCount(emailList.size());

				if (!response.isOk()) {
					logger.error("Mailgun email send failed" + response.getMessage());
				} else {
					logger.info("Mailgun email send successful");
					// add to email activity table
					try {
						// emailActivityService.saveEmail(template, subject,
						// StringUtils.commaSeparate(emailList), null,
						// fromUserEmail);
					} catch (Exception ex) {
						logger.warn(ex.getMessage(), ex);
					}

				}

			}

			@Override
			public void onFailure(Throwable ex) {
				logger.error("Mailgun email send failed" + ex);

			}
		});

	}

	/**
	 * Send calendar invitation by email.
	 * 
	 * @throws RecruizException
	 */
	@Override
	public void sendEmailWithAtttachment(final List<String> emailList, String emailBody, String subject, File attachmentFile,
			String fromUserEmail) throws MessagingException, IOException, RecruizException {

		if (checkAppSettingsService.isEmailUsageLimitExceeded(emailList.size())) {
			throw new RecruizException(ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);
		}

		String[] recipients = emailList.toArray(new String[emailList.size()]);
		MailMessage message = null;

		// replacing from email value if user has passed from email value
		if (fromUserEmail != null && !fromUserEmail.isEmpty()) {
			message = MailMessageBuilder.create().addAttachment(attachmentFile).html(emailBody).subject(subject).to(recipients)
					.from(fromUserEmail).build();
		} else {
			message = MailMessageBuilder.create().addAttachment(attachmentFile).html(emailBody).subject(subject).to(recipients)
					.from(fromEmail).build();
		}

		mailOperations.sendMailAsync(message, new ResponseCallback<MailgunSendResponse>() {
			@Override
			public void onSuccess(MailgunSendResponse response) {
				// updating the email count under on success call back
				orgConfigService.updateBulkEmailCount(emailList.size());

				if (!response.isOk()) {
					logger.error("Mailgun email send failed" + response.getMessage());
				} else {
					logger.info("Mailgun email send successful");
					// add to email activity table
					try {
						// emailActivityService.saveEmail(emailBody, subject,
						// StringUtils.commaSeparate(emailList), null,
						// fromUserEmail);
					} catch (Exception ex) {
						logger.warn(ex.getMessage(), ex);
					}
				}
			}

			@Override
			public void onFailure(Throwable ex) {
				logger.error("Mailgun email send failed" + ex);

			}
		});
	}

	/**
	 * Send calendar invitation by email.
	 * 
	 * @throws RecruizException
	 */
	@Override
	public void sendCalenderInvite(final List<String> emailList, String emailBody, String subject, String fileName, String resumePath,
			File jdFile, String fromUserEmail, final List<String> ccList, List<String> selectedFiles) throws MessagingException, IOException, RecruizException {

		String[] recipients = emailList.toArray(new String[emailList.size()]);
		MailMessage message = null;

		// adding files to attachment file list
		List<File> attachmentFiles = new ArrayList<File>();

		if (resumePath != null && !resumePath.isEmpty()) {
			File resumeFile = getFileFromPath(resumePath);
			if (resumeFile != null && resumeFile.exists()) {
				attachmentFiles.add(resumeFile);
			}
		}
		if (null != jdFile && jdFile.exists()) {
			attachmentFiles.add(jdFile);
		}

		if (fileName != null && !fileName.isEmpty()) {
			File icsFile = getFileFromPath(fileName);
			if (icsFile != null && icsFile.exists()) {
				attachmentFiles.add(icsFile);
			}
		}

		File[] files = attachmentFiles.toArray(new File[attachmentFiles.size()]);
		if (fromUserEmail != null && !fromUserEmail.isEmpty()) {
			message = MailMessageBuilder.create().addAttachments(files).html(emailBody).subject(subject).to(recipients).from(fromUserEmail)
					.build();
		} else {
			message = MailMessageBuilder.create().addAttachments(files).html(emailBody).subject(subject).to(recipients).from(fromEmail)
					.build();
		}

		if (null != ccList && !ccList.isEmpty()) {
			String[] cc = ccList.toArray(new String[ccList.size()]);
			message.addCc(cc);
		}

		mailOperations.sendMailAsync(message, new ResponseCallback<MailgunSendResponse>() {

			@Override
			public void onSuccess(MailgunSendResponse response) {
				// updating the email count under on success call back
				orgConfigService.updateEmailCount(emailList.size());

				if (!response.isOk()) {
					logger.error("Mailgun email send failed" + response.getMessage());
				} else {
					logger.info("Mailgun email send successful");
					// add to email activity table
					try {
						// emailActivityService.saveEmail(emailBody, subject,
						// StringUtils.commaSeparate(emailList), null,
						// fromUserEmail);
					} catch (Exception ex) {
						logger.warn(ex.getMessage(), ex);
					}
				}
			}

			@Override
			public void onFailure(Throwable ex) {
				logger.error("Mailgun email send failed" + ex);
			}
		});
	}

	public File getFileFromPath(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return null;
		}
		File attachmentFile = null;
		if (fileName.startsWith("http")) {
			attachmentFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), fileName);
		} else {
			attachmentFile = new File(fileName);
		}
		return attachmentFile;
	}

	/**
	 * to be called when sending bulk Email, this will throw a recruiz exception
	 * if the limit is increased.
	 */
	@Override
	public MailgunSendResponse sendBulkEmail(final List<String> emailList, String body, String subject, List<File> attachmentFiles,
			String fromUserEmail, List<String> ccList) throws MessagingException, IOException, RecruizException {

		if (checkAppSettingsService.isEmailUsageLimitExceeded(emailList.size())) {
			throw new RecruizException(ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);
		}

		logger.error(" Email_Lists  = "+emailList);
		logger.error(" subject  = "+subject);
		logger.error(" fromUserEmail  = "+fromUserEmail);
		logger.error(" ccList  = "+ccList);
		
		if (fromUserEmail == null || fromUserEmail.isEmpty()) {
			fromUserEmail = fromEmail;
			
			logger.error(" fromUserEmail getting from properties   = "+fromUserEmail);
		}

		
		
		String[] recipients = emailList.toArray(new String[emailList.size()]);
		MailMessage message = null;

		
		logger.error(" recipients  = "+recipients);
		
		if (attachmentFiles != null && !attachmentFiles.isEmpty()) {
			File[] files = attachmentFiles.toArray(new File[attachmentFiles.size()]);
			message = MailMessageBuilder.create().addAttachments(files).html(body).subject(subject).to(recipients).from(fromUserEmail)
					.build();
		} else {
			message = MailMessageBuilder.create().html(body).subject(subject).to(recipients).from(fromUserEmail).build();
		}

		if (null != ccList && !ccList.isEmpty()) {
			List<String> ccListFinal = new ArrayList<>();
			for (String cc : ccList) {
				if (cc != null && !cc.trim().isEmpty())
					ccListFinal.add(cc);
			}
			message.addCc(ccListFinal);
		}

		logger.error(" message Json  = "+message);
		
		MailgunSendResponse mailResponse = mailOperations.sendMail(message);
		try {
			// emailActivityService.saveEmail(body, subject,
			// StringUtils.commaSeparate(emailList), ccList, fromUserEmail);
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
		return mailResponse;

	}

	@Override
	public void sendEmailFromEmailClient(List<String> emailList, List<String> ccList, List<String> bccList, String messageBody,
			String subject, List<File> fileList) throws RecruizException {
		// not to be implemented

	}
}
