package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.utils.EncryptKeyUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author sourav
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "org_config")
public class OrganizationConfiguration {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "licence", columnDefinition = "longtext")
	private String lic;

	@Column(name = "setting_info", columnDefinition = "longtext")
	private String settingInfo;

	@Column(name = "last_verified")
	private String lastVerified = EncryptKeyUtils.getEncryptedKey(new Date().getTime() + "");

	// to store the number of emails used by the organization
	private int emailUsed;

	// to store email Used by bulk service
	private int bulkEmailUsed;

	private Boolean customReportEnabled = false;
}
