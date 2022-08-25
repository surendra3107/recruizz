package com.bbytes.recruiz.domain;

import java.util.ArrayList;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "attendee", "scheduleHrExecutives", "file" })
@ToString(exclude = { "attendee", "scheduleHrExecutives", "file" })
@NoArgsConstructor
@Entity(name = "interview_schedule")
@EntityListeners({ AbstractEntityListener.class })
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class InterviewSchedule extends AbstractEntity implements Comparable<InterviewSchedule> {

	private static final long serialVersionUID = -5056178946614293164L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private Date startsAt = DateTime.now().toDate();

	private Date endsAt = DateTime.now().toDate();

	private String positionCode;

	private String positionName;

	private String roundId;

	private String roundType;

	@Column(length = 1000)
	private String roundName;

	@Column(length = 1000)
	private String clientName;

	@Column(columnDefinition = "longtext")
	private String notes;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String interviewerEventId;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EventAttendee> attendee = new HashSet<EventAttendee>();

	private String candidateEmail;

	private String candidateName;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String candidateEventId;

	private String interviewSchedulerEmail;

	private String interviewSchedulerName;

	@Column(columnDefinition = "longtext")
	private String interviewerTemplateSubject;

	@Column(columnDefinition = "longtext")
	private String interviewerTemplateName;

	@Column(columnDefinition = "longtext")
	private String interviewerTemplateData;

	private String candidateAccepted;

	@Column(length = 1000)
	private String templateName;

	@Column(columnDefinition = "longtext")
	private String templateSubject;

	@Column
	private boolean profileMasked = false;

	@Column
	private boolean feedbackExpected = true;

	// storing feedback share id for custom feedback form
	@Column(name = "feedback_share_id")
	private String levelbarFeedbackShareId;

	// storing feedback share id for custom feedback form
	@Column(name = "feedback_que_set_id")
	private String levelbarFeedbackQueSetId;

	@NotAudited
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JsonProperty(access = Access.WRITE_ONLY)
	@JoinTable(name = "interview_hr", joinColumns = @JoinColumn(name = "Schedule_ID", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "HR_ID", referencedColumnName = "user_id"))
	private Set<User> scheduleHrExecutives;

	@NotAudited
	@OneToMany(mappedBy = "schedule", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<InterviewFile> file = new ArrayList<InterviewFile>();

	@Column
	private boolean active = true;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getStartsAt() {
		return startsAt;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setStartsAt(Date startsAt) {
		this.startsAt = startsAt;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getEndsAt() {
		return endsAt;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setEndsAt(Date endsAt) {
		this.endsAt = endsAt;
	}

	@Override
	public int compareTo(InterviewSchedule schedule) {
		return getStartsAt().compareTo(schedule.getStartsAt());
	}
}
