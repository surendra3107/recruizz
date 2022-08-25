package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;

import lombok.Data;

@Data
public class ScheduleInterviewDTO implements Serializable {

	private static final long serialVersionUID = 3913449225724878503L;

	private long id;

	private String interview_datails;

	private String date;

	private Position position;

	private Round round;

	private Set<ClientInterviewerPanel> interviewer;

	private List<String> candidateEmail;

	private String hrExecutiveEmail;

	private String timeSlot;

	private String status; // should have active/cancelled

	private boolean candidateAccepted;

	private boolean interviewerAccepted;
}
