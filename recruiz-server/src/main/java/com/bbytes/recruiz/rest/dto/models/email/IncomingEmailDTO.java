package com.bbytes.recruiz.rest.dto.models.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;

import lombok.Data;

@Data
public class IncomingEmailDTO implements Serializable {

	private static final long serialVersionUID = -462298551626365070L;

	private Long uid;
	
	private String messageId;

	private Address[] sender;

	private Address[] reciever;

	private String subject;

	private Date sentDate;

	private Date recievedDate;

	private String contentType;

	private Boolean hasAttachment;

	// below two field will have same message when it becomes a conversation
	// thread
	private String inReplyToId;
	private String referencesId;

	private List<AttachmentDTO> attachments = new ArrayList<>();

	private Boolean statusFlag;

	private List<IncomingEmailDTO> conversationThreadMessages = new ArrayList<>();

	private String textContent;

	private String htmlContent;
	
	private String to;
	
	private String cc;
	
	private String originalSender;
}
