package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class CandidateCurrentPositionDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;

	private String clientName;

	private String positionName;

	private String currentStatus;

	private String roundName;

	private Date sourceOnDate;

	private boolean isInterviewScheduled = false;

	private String clientId;

	private String positionId;
}
