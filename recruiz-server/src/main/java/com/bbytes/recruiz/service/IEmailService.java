package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.exception.RecruizException;

public interface IEmailService {

	/**
	 * Method is used to send email
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param template
	 */

	public void sendEmail(List<String> emailList, Map<String, Object> emailBody, String subject, String template)
			throws RecruizException;

	/**
	 * Method is used to send email
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param template
	 */

	public void sendEmail(List<String> emailList, String emailBody, String subject) throws RecruizException;;

	/**
	 * Method is used to send email
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param emailBodyIsHTML
	 */
	public void sendEmail(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML)
			throws RecruizException;
	
	/**
	 * Method is used to send email
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param emailBodyIsHTML
	 */
	public void sendEmailForExternalApi(List<String> emailList, String emailBody, String subject, boolean emailBodyIsHTML)
			throws RecruizException;

	/**
	 * Send email using template
	 *
	 * @param template
	 * @param emailList
	 * @param subject
	 * @param emailBodyMap
	 * @throws RecruizException
	 */
	public void sendEmail(final String template, List<String> emailList, String subject,
			Map<String, Object> emailBodyMap) throws RecruizException;

	/**
	 * Method is used to send calendar invitation
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param template
	 * @param fileName
	 * @param selectedFiles 
	 * @throws MessagingException
	 * @throws IOException
	 * @throws RecruizException
	 */

	public void sendCalenderInvite(List<String> emailList, String body, String subject, String fileName,
			String fromUserEmail,List<String> ccList, List<String> selectedFiles) throws MessagingException, IOException, RecruizException;

	void sendCandidateCalenderInvite(List<String> emailList, Map<String, Object> emailBody, String subject,
			String template, String fileName, String fromUserEmail, List<String> selectedFiles)
			throws MessagingException, IOException, RecruizException;

	public void sendEmailWithAtttachment(List<String> emailList, String body, String subject, File attachmentFile,
			String fromUserEmail) throws MessagingException, IOException, RecruizException;

	/**
	 * for sending email with schedule and resume and jd attachment
	 *
	 * @param emailList
	 * @param emailBody
	 * @param subject
	 * @param fileName
	 * @param resumePath
	 * @param candidateFiles 
	 * @throws MessagingException
	 * @throws IOException
	 */
	void sendCalenderInvite(List<String> emailList, String emailBody, String subject, String fileName,
			String resumePath, File jdFile, String fromEmail,List<String> ccList, List<String> candidateFiles) throws MessagingException, IOException, RecruizException;

	// send email with from user param
	public void sendEmail(List<String> emailList, String emailTemplate, String subject, boolean emailBodyIsHTML,
			String loggedInUserEmail) throws RecruizException;

	// this method should be used where we are send bulk email, this will check
	// for email usage and accordingly throw exception
	public MailgunSendResponse sendBulkEmail(List<String> emailList, String body, String subject,
			List<File> attachmentFiles, String fromUserEmail,List<String> ccList) throws MessagingException, IOException, RecruizException;

	public void sendEmailFromEmailClient(List<String> emailList, List<String> ccList, List<String> bccList,
			String messageBody, String subject,List<File> fileList) throws RecruizException;

}
