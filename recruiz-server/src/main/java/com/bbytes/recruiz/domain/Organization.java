package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.enums.CandidateModificationRange;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Organization
 *
 * @author souravkumar
 *
 */

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "user", "organizationConfiguration" })
@ToString(exclude = { "user", "organizationConfiguration" })
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Entity(name = "organization")
public class Organization extends AbstractEntity {

	private static final long serialVersionUID = -5207628002365267277L;

	@Id
	@Column(name = "org_id", unique = true)
	private String orgId;

	@Column(name = "org_name", unique = true)
	private String orgName;

	private String orgType;

	@Column(name = "time_preference")
	private String timePreference;

	@Column(length = 1000)
	private String logoUrlPath;

	@Column(length = 1000)
	private String websiteUrl;

	@Column(length = 1000)
	private String facebookUrl;

	@Column(length = 1000)
	private String twitterUrl;

	@Column(length = 1000)
	private String googleUrl;

	@Column(length = 1000)
	private String linkedInUrl;

	@Column(length = 1000)
	private String slackUrl;

	@Column(length = 1000)
	private String gitHubUrl;

	@Column(length = 1000)
	private String hipChatUrl;

	@Column(length = 1000)
	private String bitBucketUrl;

	@Column(name = "marked_delete")
	private Boolean markForDelete = false;

	@Column(name = "marked_delete_date")
	private Date markForDeleteDate;

	@Column(name = "org_api_token", length = 5000)
	private String organizationAPIToken;

	@Column(name = "disable_status")
	private Boolean disableStatus = false;

	@Column(name = "tax_registration_id")
	private String taxRegistrationId;

	@Column(name = "gst_id")
	private String gstId;

	@Column(name = "pan_no")
	private String panNo;

	@Column(name = "address",columnDefinition="longtext")
	private String address;

	@Column(name = "disable_reason")
	private String disableReason;

	@Column(name = "address_l1",columnDefinition="longtext")
	private String addressL1;

	@Column(name = "address_l2",columnDefinition="longtext")
	private String addressL2;

	@Column(name = "city")
	private String city;

	@Column(name = "pincode")
	private String pincode;

	@Column(name = "country")
	private String country;

	@Column(name = "state")
	private String state;

	@Column(name = "phone")
	private String phone;
	
	@Column(name = "duplicateCheck")
	private String duplicateCheck;
	
	@Column(name = "mandatoryDocs")
	private String mandatoryDocs;
	
	@Column(name = "documentsCheck")
	private String documentsCheck;

	@Column(name = "checkRolloutOfferletter")
	private String checkRolloutOfferletter;
		
	@Column(name = "candidateFormFields")
	private String candidateFormFields;

	@Column(name = "candidateMandatoryFields")
	private String candidateMandatoryFields;

	@Column(name = "ivrCallingIntegration")
	private String ivrCallingIntegration;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String organizationEmail;

	// to be used when sending data to plutus
	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String registeredMobile;


	@NotAudited
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "organization", orphanRemoval = true, cascade = CascadeType.ALL)
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<User> user = new ArrayList<User>();

	@OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
	private OrganizationConfiguration organizationConfiguration;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String orgLogoUrl;

	@Column(name = "candidate_modification_days")
	private Long candidateModificationDays = Long.parseLong(CandidateModificationRange.THIRTY_DAYS.getRangeValue()+"");

	@Column(name="dummy_data_status")
	private String dummyDataStatus ;

	public Organization(String orgId, String orgName) {
		this.orgId = orgId;
		this.orgName = orgName;
	}
}
