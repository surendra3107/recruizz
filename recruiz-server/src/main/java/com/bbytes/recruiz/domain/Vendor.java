package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "organization" })
@ToString(exclude = { "organization" })
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Entity(name = "vendor")
public class Vendor {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(unique = true, nullable = false)
	private String name;

	private String email;

	private String type;

	private String phone;

	private Boolean status = true;

	@Column(columnDefinition = "text")
	private String address;

	@Column
	private String isInterviewSchedule;
	
	@ManyToOne
	private Organization organization;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String activationStatus;

	public static String PENDING = "Pending";
	public static String ACTIVE = "Active";
	public static String DISABLED = "Disabled";

}
