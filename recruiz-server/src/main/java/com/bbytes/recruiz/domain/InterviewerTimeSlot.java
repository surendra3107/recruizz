package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.utils.TimePickerDateDeSerializer;
import com.bbytes.recruiz.utils.TimePickerDateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "clientInterviewerPanel" })
@NoArgsConstructor
@Entity(name = "time_slot")
// @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class,
// property = "@CyclicReferenceId")
public class InterviewerTimeSlot extends AbstractEntity {

	private static final long serialVersionUID = 6271178087092776234L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JsonSerialize(using = TimePickerDateSerializer.class)
	@JsonDeserialize(using = TimePickerDateDeSerializer.class)
	private Date startTime;

	@JsonSerialize(using = TimePickerDateSerializer.class)
	@JsonDeserialize(using = TimePickerDateDeSerializer.class)
	private Date endTime;

	public InterviewerTimeSlot(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@ManyToOne
	@JsonProperty(access = Access.WRITE_ONLY)
	private ClientInterviewerPanel clientInterviewerPanel;

	@Override
	public String toString() {
		return "Id: " + this.getId() + ", StartTime: " + this.getStartTime() + ", EndTime: " + this.getEndTime();
	}
}
