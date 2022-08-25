package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

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
@EqualsAndHashCode(callSuper = false, exclude = { "file"})
@ToString(exclude = { "file" })
@NoArgsConstructor 
@Entity(name = "task_schedule")
@EntityListeners({ AbstractEntityListener.class })
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class TaskSchedule extends AbstractEntity {

	private static final long serialVersionUID = -921935795141114211L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private Date dueAt = DateTime.now().toDate();

	private Date startAt = DateTime.now().toDate();

	@Column(columnDefinition = "longtext")
	private String notes;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String taskEventId;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EventAttendee> attendee = new HashSet<EventAttendee>();

	private String taskCreaterEmail;

	private String taskCreaterName;

	@Column(columnDefinition = "longtext")
	private String taskSubject;

	@Column(columnDefinition = "longtext")
	private String taskNoteContent;

	private String templateName;

	@NotAudited
	@OneToMany(mappedBy = "taskSchedule", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<InterviewFile> file = new HashSet<InterviewFile>();

	private boolean active = true;

	@NotAudited
	@OneToOne(mappedBy = "taskSchedule", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REMOVE }, orphanRemoval = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private TaskItem taskItem;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDueAt() {
		return dueAt;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDueAt(Date startsAt) {
		this.dueAt = startsAt;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getStartsAt() {
		return startAt;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setStartsAt(Date endsAt) {
		this.startAt = endsAt;
	}
	
	public void setFile(Set<InterviewFile> files) {     
	    this.file.clear();
	    if (files != null) {        
	        this.file.addAll(files);
	    }        
	}
}
