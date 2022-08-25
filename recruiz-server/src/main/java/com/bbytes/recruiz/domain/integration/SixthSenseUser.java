package com.bbytes.recruiz.domain.integration;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.bbytes.recruiz.domain.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity(name = "sixth_sense_user")
@EqualsAndHashCode(callSuper = false, exclude = { "user" })
@ToString(exclude = { "user" })
public class SixthSenseUser {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "user_name", unique = true, nullable = false)
	private String userName;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "sources")
	private String sources;

	@Column(name = "usage_type")
	private String usageType;
	
	@Column(name = "login_user_email")
	private String loggedUserEmail;
	
	@Column(name = "captcha_status")
	private String captchaStatus;

	@Column(name = "captchaSession")
	private String captchaSession;

	
	@Column(name = "view_count", nullable = false)
	private int viewCount = 0;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
}
