package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.StringUtils;

/**
 * This Service class uses SMTP protocol using Java Mail Sender to send email's.
 *
 */

@Service("SmtpMailService")
public class SMTPEmailServiceImpl extends AbstractEmailService {

    private static final Logger logger = LoggerFactory.getLogger(SMTPEmailServiceImpl.class);

    @Autowired
    private VelocityEngine templateEngine;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailActivityService emailActivityService;

    @Autowired
    private EmailAccountDetailService emailClientDetailService;

    private String fromEmail;

    private JavaMailSender getJavaMailSenderClient() {

    	logger.error("getJavaMailSenderClient() line no 70");
	EmailClientDetails userEmailClients = emailClientDetailService
		.getDefaultEmailAccount(userService.getLoggedInUserObject());
	
	logger.error("getJavaMailSenderClient() line no 73"+userEmailClients);

	if (null == userEmailClients) {
	    return null;
	}

	EmailClientDetails clientDetails = userEmailClients;
	fromEmail = clientDetails.getEmailId();

	String host = clientDetails.getSmtpServerUrl(); // userService.getLoggedInUserObject().getHost();
	if (host == null || host.isEmpty()) {
	    return null;
	}
	final String username = clientDetails.getEmailId();
	final String password = clientDetails.getPassword();
	// String protocol = userService.getLoggedInUserObject().getProtocol();
	int port = Integer.parseInt(clientDetails.getSmtpServerPort());

	// sets SMTP server properties
	Properties properties = new Properties();
	properties.put("mail.smtp.host", host);
	properties.put("mail.smtp.port", port);
	properties.put("mail.smtp.auth", "true");
	properties.put("mail.smtp.starttls.enable", "true");
	properties.put("mail.user", username);
	properties.put("mail.password", password);

	// creates a new session with an authenticator
	Authenticator auth = new Authenticator() {
	    public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	    }
	};
	Session session = Session.getInstance(properties, auth);

	JavaMailSenderImpl sender = new JavaMailSenderImpl();
	sender.setHost(host);
	sender.setDefaultEncoding("UTF-8");
	sender.setUsername(username);
	sender.setPassword(password);
	// sender.setProtocol(protocol);
	sender.setPort(port);
	sender.setDefaultFileTypeMap(sender.getDefaultFileTypeMap());
	sender.setSession(session);
	sender.setJavaMailProperties(properties);

	return sender;
    }

    private MimeMessage getMailMessage(List<String> emailList, String emailBody, String subject,
	    boolean emailBodyIsHTML, List<String> ccList) throws MessagingException {
	try {
	    MimeMessage mail = getJavaMailSenderClient().createMimeMessage();

	    String[] recipients = new String[emailList.size()];
	    emailList.toArray(recipients);

	    MimeMessageHelper helper = new MimeMessageHelper(mail, true);
	    helper.setTo(recipients);

	    if (null != ccList && !ccList.isEmpty()) {
		String[] cc = ccList.toArray(new String[ccList.size()]);
		helper.setCc(cc);
	    }

	    helper.setFrom(fromEmail);
	    helper.setSubject(subject);
	    if (emailBodyIsHTML)
		mail.setContent(emailBody, "text/html");
	    else
		helper.setText(emailBody);
	    return mail;
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}
	return null;

    }

    @Override
    public void sendEmail(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML) {
	try {
	    MimeMessage mail = getMailMessage(emailList, emailBody, subject, emailBodyIsHTML, null);
	    if (null != mail) {
		getJavaMailSenderClient().send(mail);
		// add to email activity table
		try {
		    // emailActivityService.saveEmail(emailBody, subject,
		    // StringUtils.commaSeparate(emailList), null,
		    // fromEmail);
		} catch (Exception ex) {
		    logger.warn(ex.getMessage(), ex);
		}
	    }

	} catch (MessagingException e) {
	    logger.error(e.getMessage(), e);
	} catch (MailException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    @Override
    public void sendEmail(List<String> emailList, Map<String, Object> emailBody, String subject, String template)
	    throws RecruizException {
	@SuppressWarnings("deprecation")
	String emailHTMLContent = VelocityEngineUtils.mergeTemplateIntoString(this.templateEngine, template, "UTF-8",
		emailBody);
	sendEmail(emailList, emailHTMLContent, subject);
    }

    @Override
    public void sendEmail(List<String> emailList, String emailBody, String subject) throws RecruizException {
	sendEmail(emailList, emailBody, subject, true);
    }

    @Override
    public void sendCalenderInvite(List<String> emailList, String body, String subject, String fileName,
	    String fromUserEmail, List<String> ccList, List<String> selectedFiles) throws MessagingException, IOException {

	MimeMessage mail = getMailMessage(emailList, body, subject, true, ccList);

	createBodyPartWithFiles(mail, fileName);
	getJavaMailSenderClient().send(mail);
	// add to email activity table
	try {
	    // emailActivityService.saveEmail(body, subject,
	    // StringUtils.commaSeparate(emailList), ccList, fromEmail);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}
    }

    // to create Multipart to attach a file in email
    private void createBodyPartWithFiles(MimeMessage mail, String... fileNames) throws MessagingException, IOException {
	// Create a Multipart
	Multipart multipart = null;

	try {
	    String existingContent = (String) mail.getContent();
	    multipart = new MimeMultipart();

	    MimeBodyPart htmlPart = new MimeBodyPart();
	    htmlPart.setContent(existingContent, "text/html");
	    multipart.addBodyPart(htmlPart);

	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}

	if (null != fileNames && fileNames.length > 0) {
	    for (String fileName : fileNames) {
		if (null != fileName && !fileName.isEmpty()) {
		    File file = new File(FileUtils.getTempDirectory() + "/" + System.currentTimeMillis() + "."
			    + FilenameUtils.getExtension(fileName));
		    if (fileName.startsWith("http")) {
			URL url;
			try {
			    url = new URL(fileName);
			    FileUtils.copyURLToFile(url, file);
			} catch (Exception e) {
			    logger.warn(e.getMessage(), e);
			}

		    } else {
			file = new File(fileName);
		    }

		    if (file.exists()) {
			BodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(file.getPath());

			if (FilenameUtils.getExtension(fileName).endsWith("ics")) {

			    messageBodyPart.setHeader("Content-Class", "urn:content-  classes:calendarmessage");
			    messageBodyPart.setHeader("Content-ID", "calendar_message");

			    messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(
				    Files.readAllBytes(file.toPath()), "text/calendar; charset=UTF-8")));// very
			    // important

			} else {
			    messageBodyPart.setDataHandler(new DataHandler(source));
			    messageBodyPart.setFileName(file.getName());
			}
			multipart.addBodyPart(messageBodyPart);
		    }
		}
	    }

	    // Put parts in message
	    mail.setContent(multipart, "text/html");
	}
    }

    @Override
    public void sendCandidateCalenderInvite(List<String> emailList, Map<String, Object> emailBody, String subject,
	    String template, String fileName, String fromUserEmail, List<String> selectedFiles) throws MessagingException, IOException {
	sendCalenderInvite(emailList, template, subject, fileName, fromUserEmail, null, null);
    }

    @Override
    public void sendEmailWithAtttachment(List<String> emailList, String body, String subject, File attachmentFile,
	    String fromUserEmail) throws MessagingException, IOException {
	sendCalenderInvite(emailList, body, subject, attachmentFile.getPath(), fromUserEmail, null, null);
    }

    @Override
    public void sendCalenderInvite(List<String> emailList, String emailBody, String subject, String fileName,
	    String resumePath, File jdFile, String fromEmail, List<String> ccList, List<String> selectedFiles)
	    throws MessagingException, IOException {

	MimeMessage mail = getMailMessage(emailList, emailBody, subject, true, ccList);

	String jdPath = null;
	if(null != jdFile) {
	    jdPath = jdFile.getPath();
	}
	createBodyPartWithFiles(mail, fileName, resumePath,jdPath);
	getJavaMailSenderClient().send(mail);
	// add to email activity table
	try {
//	    emailActivityService.saveEmail(emailBody, subject, StringUtils.commaSeparate(emailList), ccList, fromEmail);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}

    }

    @Override
    public void sendEmail(List<String> emailList, String emailTemplate, String subject, boolean emailBodyIsHTML,
	    String loggedInUserEmail) {
	try {
	    sendEmail(emailList, emailTemplate, subject);
	} catch (RecruizException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public MailgunSendResponse sendBulkEmail(List<String> emailList, String body, String subject,
	    List<File> attachmentFiles, String fromUserEmail, List<String> ccList)
	    throws MessagingException, IOException, RecruizException {

    	logger.error("SMTPEmailServiceImpl line no 315");
    	
	String[] fileName = null;
	if (null != attachmentFiles && !attachmentFiles.isEmpty()) {
	    fileName = new String[attachmentFiles.size()];
	    for (int i = 0; i < attachmentFiles.size(); i++) {
		fileName[i] = attachmentFiles.get(i).getPath();
	    }
	}

	MimeMessage mail = getMailMessage(emailList, body, subject, true, ccList);
	createBodyPartWithFiles(mail, fileName);
	getJavaMailSenderClient().send(mail);
	// add to email activity table
	try {
	    // emailActivityService.saveEmail(body, subject,
	    // StringUtils.commaSeparate(emailList), ccList, fromEmail);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}
	return null;
    }

    // to send email from user mailbox
    @Override
    public void sendEmailFromEmailClient(List<String> emailList, List<String> ccList, List<String> bccList,
	    String messageBody, String subject, List<File> fileList) throws RecruizException {
	try {
	    MimeMessage mail = getJavaMailSenderClient().createMimeMessage();

	    String[] recipients = new String[emailList.size()];
	    emailList.toArray(recipients);

	    MimeMessageHelper helper = new MimeMessageHelper(mail, true);
	    helper.setTo(recipients);
	    if (null != ccList && !ccList.isEmpty() && !ccList.contains("") && !ccList.contains(null)) {
		String[] ccRecipients = new String[ccList.size()];
		helper.setCc(ccList.toArray(ccRecipients));
	    }
	    if (null != bccList && !bccList.isEmpty() && !bccList.contains("") && !bccList.contains(null)) {
		String[] bccRecipients = new String[bccList.size()];
		helper.setBcc(bccList.toArray(bccRecipients));
	    }

	    helper.setFrom(fromEmail);
	    helper.setSubject(subject);
	    mail.setContent(messageBody, "text/html");
	    if (null != fileList && !fileList.isEmpty()) {
		String[] files = new String[fileList.size()];
		for (int i = 0; i < fileList.size(); i++) {
		    files[i] = fileList.get(i).getPath();
		}
		createBodyPartWithFiles(mail, files);
	    }
	    getJavaMailSenderClient().send(mail);
	    // add to email activity table
	    try {
		// emailActivityService.saveEmail(messageBody, subject,
		// StringUtils.commaSeparate(emailList), ccList,
		// fromEmail);
	    } catch (Exception ex) {
		logger.warn(ex.getMessage(), ex);
	    }
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}

    }

	@Override
	public void sendEmailForExternalApi(List<String> emailList, String emailBody, String subject,
			boolean emailBodyIsHTML) throws RecruizException {
		
		logger.error("call sendEmailForExternalApi() in SMTPEmailServiceImpl"); 
		
	}

}
