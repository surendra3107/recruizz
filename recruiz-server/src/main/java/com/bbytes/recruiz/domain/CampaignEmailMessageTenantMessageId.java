package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "campaign_email_tenant_map")
public class CampaignEmailMessageTenantMessageId {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String tenantId;

	@Column(unique = true)
	private String emailMessageId;
}
