package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "eid" })
@ToString(exclude = { "eid" })
@NoArgsConstructor
@Entity(name = "onboarding_details")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class OnBoardingDetails extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "sub_category_name", nullable = false)
	private String subCategoryName;

	@Column(name = "onboard_category", nullable = false)
	private String onboardCategory;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description", nullable = false, columnDefinition = "longtext")
	private String description;

	@Column(name = "schedule_date")
	private Date scheduleDate;

	@Column(name = "completed_status")
	private Boolean completedStatus = false;

	@Column
	private String owner;

	@Column
	private String state = "new";

	@Column(name = "enrolled_people_email")
	private String enrolledPeopleEmails;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	private Employee eid;

	@Transient
	@JsonSerialize
	@JsonDeserialize
	private List<String> enrolledPeoples;

	public List<String> getEnrolledPeoples(){
	    if(this.enrolledPeopleEmails != null && !this.enrolledPeopleEmails.isEmpty()) {
		return StringUtils.commaSeparateStringToList(this.getEnrolledPeopleEmails());
	    }
	    return this.enrolledPeoples;
	}
	
	// coming from UI to added
	@Transient
	@JsonSerialize
	@JsonDeserialize
	private Boolean selected;

	@NotAudited
	@OneToMany(fetch = FetchType.LAZY)
	private Set<OnBoardingDetailsComments> comments = new HashSet<>();

}
