package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class CandidateRatingDTO implements Serializable {

	private static final long serialVersionUID = 909228643372062381L;

	private Long candidateRatingQuestionId;

	private String ratingQuestion;

	private Double ratingScore;

	private Long candidateId;

	private String candidateEmail;

	private String candidateName;

}
