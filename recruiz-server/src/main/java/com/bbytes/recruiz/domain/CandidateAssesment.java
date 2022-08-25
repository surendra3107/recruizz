package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity(name = "candidate_assesment")
@EntityListeners({ AbstractEntityListener.class })
public class CandidateAssesment extends AbstractEntity {
	
	private static final long serialVersionUID = -7057456523750540228L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String candidateEmailId;
	
	@Column
	private String testId;
	
	@Column
	private String positionCode;
	
	@Column
	private String totalScore;
	
	@Column
	private String resultScore;
	
	@Column
	private double totalScoreDouble;
	
	@Column
	private double resultScoreDouble;
	
	@Column
	private String status;
	
}
