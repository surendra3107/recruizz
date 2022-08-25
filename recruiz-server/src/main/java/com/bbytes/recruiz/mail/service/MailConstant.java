package com.bbytes.recruiz.mail.service;

public class MailConstant {

	public static final String MAIL_PARAM_FROM = "From";

	public static final String MAIL_PARAM_TO = "To";

	public static final String MAIL_PARAM_CC = "Cc";
	
	public static final String MAIL_PARAM_BCC = "Bcc";

	public static final String MAIL_PARAM_RECEPIENT = "recipient";
	
	public static final String MAIL_PARAM_SUBJECT = "subject";

	public static final String MAIL_PARAM_HTML_BODY = "body-html";

	public static final String MAIL_PARAM_TEXT_BODY = "body-plain";

	public static final String MAIL_PARAM_MESSAGE_ID = "Message-Id";

	public static final String MAIL_PARAM_DOMAIN = "domain";

	public static final String MAIL_PARAM_IN_REPLY_TO = "In-Reply-To";

	public static final String MAIL_PARAM_HEADER = "message-headers";

	public static final String MAIL_PARAM_MSG_URL = "message-url";

	public static final String MAIL_PARAM_DATE = "Date";

	public static final String MAIL_PARAM_MESSAGE_URL = "message-url";

	public static final String MAIL_PARAM_INCOMING_YES_NO = "X-Mailgun-Incoming";

	public static final String MAIL_PARAM_TIMESTAMP = "timestamp";

	public static final String MAIL_PARAM_TOKEN = "token";

	public static final String MAIL_PARAM_SIGNATURE = "signature";

	public static final String MAIL_PARAM_ATTACHMENTS = "attachments";

	public static final String REPLY_ROUTE_NAME = "'Reply Route'";

	public static final String REPLY_ROUTE_DESC = "Route all reply mail to given endpoint";
	
	public static final String MAIL_WEBHOOK_URL =  "/public/mail/reply/webhook";
	

}
