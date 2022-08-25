package com.bbytes.recruiz.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "client", "interviewerTimeSlots" })
@ToString(exclude = { "client", "interviewerTimeSlots" })
@NoArgsConstructor
@Entity(name = "interview_panel")
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "email", "client_id" }) })
public class ClientInterviewerPanel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	private String mobile;

	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Client client;

	@ManyToMany(mappedBy = "interviewers")
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<Position> positions;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "clientInterviewerPanel", orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<InterviewerTimeSlot> interviewerTimeSlots;

	public void addInterviewerTimeSlot(InterviewerTimeSlot timeSlot) {

		timeSlot.setClientInterviewerPanel(this);
		if (getInterviewerTimeSlots() != null) {
			getInterviewerTimeSlots().add(timeSlot);
		} else {
			interviewerTimeSlots = new HashSet<InterviewerTimeSlot>();
			interviewerTimeSlots.add(timeSlot);
		}
	}

	public void addInterviewerTimeSlot(Collection<InterviewerTimeSlot> timeSlots) {
		if (timeSlots == null)
			return;

		for (InterviewerTimeSlot slot : timeSlots) {
			addInterviewerTimeSlot(slot);
		}
	}
}
