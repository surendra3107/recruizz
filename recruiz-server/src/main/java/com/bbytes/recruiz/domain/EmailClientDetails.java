package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "user" })
@ToString(exclude = { "user" })
@NoArgsConstructor
@Entity(name = "email_client_details")
public class EmailClientDetails extends AbstractEntity {

	private static final long serialVersionUID = 6074929127978760718L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String emailClientName;

	@Column(nullable = false)
	private String emailId;

	@JsonProperty(access=Access.WRITE_ONLY)
	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String imapServerUrl;

	@Column(nullable = false)
	private String smtpServerUrl;

	@Column(nullable = false)
	private String imapServerPort;

	@Column(nullable = false)
	private String smtpServerPort;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private String recruizEmail;
	
	@Column(nullable = false)
	private Boolean markedDefault = false;

	@Column
	private Long lastMaxUid;
	
	@Column
	private Date emailFetchStartDate;
	
	@Column
	private Date emailFetchEndDate;
	
	@Column
	private Date lastFetchedStartDate ;
	
	@Column
	private Date lastFetchedEndDate ;

	public void setPassword(String password) {
		String encryptedPassword = EncryptKeyUtils.getEncryptedKey(password);
		this.password = encryptedPassword;
	}

	public String getPassword() {
		return EncryptKeyUtils.getDecryptedKey(this.password);
	}

	public void setEmailId(String email) {
		String encryptedEmail = EncryptKeyUtils.getEncryptedKey(email);
		this.emailId = encryptedEmail;
	}

	public String getEmailId() {
		return EncryptKeyUtils.getDecryptedKey(this.emailId);
	}

	@Transient
	@JsonSerialize
	@JsonDeserialize
	private Long successCount;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private Long failedCount;
	
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private Long yetToProcessCount;

	@Transient
	@JsonSerialize
	@JsonDeserialize
	private Long totalCount;

	@Transient
	@JsonSerialize
	@JsonDeserialize
	private String status;

	
	
}
