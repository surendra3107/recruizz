package com.bbytes.recruiz.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;

import org.apache.commons.io.FileUtils;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.UploadFileService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false,exclude = { "attachments", "htmlBody", "textBody" })
@ToString(exclude = { "attachments", "htmlBody", "textBody" })
@NoArgsConstructor
@Entity(name = "email_message_model")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
//@EntityListeners({ AbstractEntityListener.class })
public class EmailMessageModel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long mid;
	
	@Column
	private String id;

	@Column(unique = true)
	private String messageId;

	@Column
	private String fromEmail;

	@Column
	private String toEmail;

	private String cc;
	
	private String subject;
	
	//@Column(name="textBody",columnDefinition="longtext")
	@Lob
	@Column(name="textBody",length = 1000000)
	private byte[] textBody;
	
	//@Column(name="htmlBody",columnDefinition="longtext")
	@Lob
	@Column(name="htmlBody",length = 1000000)
	private byte[] htmlBody;
	
	private String inReplyTo;
	private String domain;
	private String messageURL;
	
	//@Column(name="messageHeader",columnDefinition="longtext")
	@Lob
	@Column(name="messageHeader",length = 1000000)    
	private byte[] messageHeader;
	
	private boolean recieved = false;
	private Date timestamp;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "email_attachements", joinColumns = { @JoinColumn(name = "mid") })
	private Set<String> attachments = new HashSet<String>();

	public String addAttachment(File file,UploadFileService fileService) throws RecruizException, IOException {
		String fileUrl =fileService.uploadFileToLocalServer(FileUtils.readFileToByteArray(file),file.getName(),FileType.EMAIL_ATTACHMENT.getDisplayName(),getMessageId()); 
		attachments.add(fileUrl);
		return fileUrl;
	}

	public String addAttachment(InputStream fsStream, String fileName, String contentType, UploadFileService fileService) throws RecruizException, IOException  {
		String fileUrl = fileService.uploadFileToLocalServer(fsStream,fileName,FileType.EMAIL_ATTACHMENT.getDisplayName(),getMessageId());
		attachments.add(fileUrl);
		return fileUrl;
	}

	/**
	 * @return the htmlBody
	 */
	public String getHtmlBody() {
		String htmlBodyString = new String(htmlBody, StandardCharsets.UTF_8);
		return htmlBodyString;
	}

	/**
	 * @param htmlBodyString
	 *            the htmlBody to set
	 */
	public void setHtmlBody(String htmlBodyString) {
		
		if (htmlBodyString != null && !htmlBodyString.isEmpty()) {
			byte[] bodyblob = htmlBodyString.getBytes(StandardCharsets.UTF_8);
			this.htmlBody = bodyblob;
		}
		
		//this.htmlBody = StringUtils.compress(htmlBody);
	}

	/**
	 * @return the textBody
	 */
	public String getTextBody() {
		String textBodyString = new String(textBody, StandardCharsets.UTF_8);
		return textBodyString;
	}

	/**
	 * @param textBodyString
	 *            the textBody to set
	 */
	public void setTextBody(String textBodyString) {
		if (textBodyString != null && !textBodyString.isEmpty()) {
			byte[] locationBlob = textBodyString.getBytes(StandardCharsets.UTF_8);
			this.textBody = locationBlob;
		}
	}
	
	/**
	 * set message header in blob format
	 * @param messageHeader
	 */
	public void setMessageHeader(String messageHeader) {
		if (messageHeader != null && !messageHeader.isEmpty()) {
			byte[] messageHeaderBlob = messageHeader.getBytes(StandardCharsets.UTF_8);
			this.messageHeader = messageHeaderBlob;
		}
	}
	
	/**
	 * get message header in string format
	 * @return
	 */
	public String getMessageHeader() {
		String messageHeaderString = new String(messageHeader, StandardCharsets.UTF_8);
		return messageHeaderString;
	}
}
