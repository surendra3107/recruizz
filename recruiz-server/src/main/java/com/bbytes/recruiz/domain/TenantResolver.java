package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Part of tenant management DB .It stores the link between tenant id and user
 * email. This is used to resolve tenant id given email
 * 
 * @author Thanneer
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity(name = "tenant_resolver")
public class TenantResolver extends AbstractEntity {

	private static final long serialVersionUID = -111935115141119911L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	@Column(name = "organization_name", nullable = false)
	private String orgName;

}
