package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity(name = "candidate_rating")
@EntityListeners({ AbstractEntityListener.class })
public class CandidateRating extends AbstractEntity {

	private static final long serialVersionUID = -5626821621517900407L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	private Candidate candidate;
	
	@ManyToOne
	@JoinColumn(name="candidate_rating_question_id")
	private CandidateRatingQuestion candidateRatingQuestion;
	
	@Column(name="rating_score")
	private double ratingScore;
	
}
