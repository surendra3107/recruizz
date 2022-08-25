package com.bbytes.recruiz.rest.dto.models;

import com.bbytes.recruiz.domain.AbstractEntity;

import lombok.Data;

@Data
public class CandidateNotesDTO extends AbstractEntity {

	private static final long serialVersionUID = -3021098947855656314L;

	private String addedBy;

	private String notes;

}
