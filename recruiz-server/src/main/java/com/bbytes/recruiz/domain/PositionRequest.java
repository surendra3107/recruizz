package com.bbytes.recruiz.domain;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;

import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.PositionRequestDBEventListener;
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
@EqualsAndHashCode(callSuper = false, exclude = { "educationalQualification", "reqSkillSet", "goodSkillSet",
		"location" })
@ToString(exclude = { "educationalQualification", "reqSkillSet", "goodSkillSet", "location" })
@NoArgsConstructor
@Entity(name = "position_request")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ PositionRequestDBEventListener.class, AbstractEntityListener.class })
public class PositionRequest extends AbstractEntity {

	private static final long serialVersionUID = 8312488899817804938L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private String clientName;

	@Column(nullable = false)
	private String title;

	// to allow character of different encoding using lob type
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(length = 100000)
	private byte[] location;

	@Column(nullable = false)
	private int totalPosition;

	@Column(nullable = false)
	private Date openedDate = DateTime.now().toDate();

	@Column(nullable = false)
	private Date closeByDate;

	@Column(length = 1000)
	private String positionUrl;

	private String positionCode;

	@ElementCollection
	@CollectionTable(name = "position_request_good_skill_set", joinColumns = @JoinColumn(name = "PositionRequest_id", referencedColumnName = "id"))
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> goodSkillSet;

	@ElementCollection
	@CollectionTable(name = "position_request_req_skill_set", joinColumns = @JoinColumn(name = "PositionRequest_id", referencedColumnName = "id"))
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> reqSkillSet;

	@ElementCollection
	@CollectionTable(name = "position_request_educationa_qualification", joinColumns = @JoinColumn(name = "PositionRequest_id", referencedColumnName = "id"))
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> educationalQualification;

	@Column(nullable = false)
	private String experienceRange;

	@Column(nullable = false)
	private String type; // Payroll or onContract basis

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String typeDisplayName; // Payroll or onContract basis

	@Column(nullable = false)
	private boolean remoteWork;

	@Column(nullable = false)
	private double maxSal;

	@Column(nullable = false)
	private double minSal;

	private String salUnit;

	@Column(nullable = false)
	private String industry;

	@Column(nullable = false)
	private String functionalArea;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(length = 100000)
	private byte[] notes;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(length = 100000)
	private byte[] description;

	@Column(name = "requested_by_name")
	private String requestedByName;

	@Column(name = "requested_by_email")
	private String requestedByEmail;

	@Column(name = "requested_by_phone")
	private String requestedByPhone;

	@Column(nullable = false)
	private String status = PositionRequestStatus.Pending.toString();

	@Column(length = 1000)
	private String jdPath;

	@Column
	private long positionId;

	@Column
	private Date processedDate;

	@ElementCollection
	@CollectionTable(name = "position_request_notes", joinColumns = @JoinColumn(name = "PositionRequest_id", referencedColumnName = "id"))
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> positionRequestNotes;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String minExp;

	@Transient
	@JsonProperty(access = Access.READ_WRITE)
	private String maxExp;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getOpenedDate() {
		return openedDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setOpenedDate(Date openedDate) {
		this.openedDate = openedDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCloseByDate() {
		return closeByDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCloseByDate(Date closeByDate) {
		this.closeByDate = closeByDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getProcessedDate() {
		return processedDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}

	public String getExperienceRange() {

		if (minExp == null || maxExp == null) {
			return this.experienceRange;
		}

		String experienceRange = minExp.trim() + "-" + maxExp.trim() + " Years";
		return experienceRange;
	}

	// changing location String to blob
	public void setLocation(String locationString) {
		if (locationString != null && !locationString.isEmpty()) {
			byte[] locationBlob = locationString.getBytes(StandardCharsets.UTF_8);
			location = locationBlob;
		}
	}

	// changing location blob to String
	public String getLocation() {
		String locationString = new String(location, StandardCharsets.UTF_8);
		return locationString;
	}

	// changing notes String to blob
	public void setNotes(String notesString) {
		if (notesString != null && !notesString.isEmpty()) {
			byte[] notesBlob = notesString.getBytes(StandardCharsets.UTF_8);
			notes = notesBlob;
		}
	}

	// changing notes blob to String
	public String getNotes() {
		if (notes != null && notes.length > 0) {
			String notesString = new String(notes, StandardCharsets.UTF_8);
			return notesString;
		}
		return null;
	}

	// changing notes String to blob
	public void setDescription(String descriptionString) {
		if (descriptionString != null && !descriptionString.isEmpty()) {
			byte[] descriptionBlob = descriptionString.getBytes(StandardCharsets.UTF_8);
			description = descriptionBlob;
		}
	}

	// changing notes blob to String
	public String getDescription() {
		if (description != null && description.length > 0) {
			String descriptionString = new String(description, StandardCharsets.UTF_8);
			return descriptionString;
		}
		return null;

	}
}
