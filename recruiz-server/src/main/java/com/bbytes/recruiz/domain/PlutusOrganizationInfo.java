package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Entity
public class PlutusOrganizationInfo extends AbstractEntity {

	private static final long serialVersionUID = -3539280972273001023L;

	@Id
	@Column(name = "org_id", unique = true)
	private String orgId;

	@Column(name = "org_name", unique = true)
	private String orgName;
	
	@Column
	private String orgType;

	@Column
	private Date updatedDate;

	@Column
	private String subscriptionId;

	@Column
	private String subscriptionKey;

	// stripe payment gateway accn id
	@Column
	private String stripeAccnId;
	
	@Column
	private String planId;
	
	@Column
	private String orgEmail;
	
	@Column
	private String planName;
	
	@Column
	private String subscriptionMode;

}
