package com.bbytes.recruiz.auth.storage.emails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.bbytes.recruiz.rest.dto.models.email.AttachmentDTO;
import com.bbytes.recruiz.utils.GlobalConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = { "attachments" })
@Document
public class EmailData implements Serializable {
	
	private static final long serialVersionUID = -462298551626365070L;

	@Id
	private String id;
	
	@Field
    @Indexed(dropDups=true, expireAfterSeconds=GlobalConstants.EmailDeleteTimeInSec)
    Date createdDate = new Date();
	
	@Field
	private Long uid;
	
	@Field
	private String messageId;

	@Field
	private String[] senderString;

	@Field
	private String[] recieverString;
	
	private List<Map<String,String>> sender;

	private List<Map<String,String>> reciever;

	@Field
	private String subject;

	@Field	
	private Date sentDate;

	@Field
	private Date recievedDate;

	@Field
	private String contentType;

	@Field
	private Boolean hasAttachment;

	// below two field will have same message when it becomes a conversation
	// thread
	@Field
	private String inReplyToId;
	
	@Field
	private String referencesId;

	@Field
	private List<AttachmentDTO> attachments = new ArrayList<>();

	@Field
	private Boolean statusFlag;

	@Field
	private String textContent;

	@Field
	private String htmlContent;
	
	//private @Indexed Address[] to;
	@Field
	private String cc;
	
	@Field
	private String originalSender;
	
	@Field
	private String recieverEmail;
	
	@Field
	private String folderName;
	
	public List<Map<String,String>> getSender(){
		return getAddressMap(this.senderString);
	}
	
	public List<Map<String,String>> getReciever(){
		return getAddressMap(this.recieverString);
	}
	
	private List<Map<String,String>> getAddressMap(String[] allRecipients) {
		List<Map<String,String>> imapAddress = new ArrayList<>();
		
		if (null == allRecipients || allRecipients.length <= 0) {
			return null;
		}

		for (int i = 0; i < allRecipients.length; i++) {
			Map<String,String> addressMap = new HashMap<>();
			String address = allRecipients[i];
			String[] nameEmailArray = StringUtils.splitByWholeSeparator(address, "<");
			
			if(nameEmailArray.length == 1){
				addressMap.put("email", nameEmailArray[0].replaceAll("<", "").replaceAll(">", ""));
			}else if(nameEmailArray.length > 1){
				addressMap.put("name", nameEmailArray[0]);
				addressMap.put("email", nameEmailArray[1].replaceAll("<", "").replaceAll(">", ""));
			}
			imapAddress.add(addressMap);
		}
		return imapAddress;
	}
	
}

