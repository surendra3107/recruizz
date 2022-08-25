package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "candidate" })
@ToString(exclude = { "candidate" })
@NoArgsConstructor
@Entity(name = "candidate_education_details")
public class CandidateEducationDetails extends AbstractEntity {

	private static final long serialVersionUID = -7693985832182768830L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long cid;

	@Column
	private String board="N/A";
	
	@Column
	private String state="N/A";
	
	@Column
	private String degree="N/A";
	
	@Column
	private String college="N/A"; // this will store college and university
	
	@Column
	private String passingYear="N/A";
	
	@Column
	private Double passingPercent;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	@ManyToOne
	private Candidate candidate;


}