package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "offer_letter_for_candidates")
public class OfferLetterForCandidate extends AbstractEntity{

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "candidate_id")
	private long candidate_id;
	
	@Column(name = "approvalId")
	private long approvalId;
	
	@Column(name = "offerTemplate_id")
	private long offerTemplate_id;
	
	@Column(columnDefinition = "mediumtext")
	private String textValues;
	
	@Column(columnDefinition = "mediumtext")
	private String annuallyDeductionValues;
	
	@Column(columnDefinition = "mediumtext")
	private String monthlyDeductionValues;
	
	@Column(columnDefinition = "mediumtext")
	private String annuallyCtcValues;
	
	@Column(columnDefinition = "mediumtext")
	private String monthlyCtcValues;

	@Column(name = "monthlyGrossTotal")
	private String monthlyGrossTotal;
	
	@Column(name = "annuallyGrossTotal")
	private String annuallyGrossTotal;
	
	@Column(name = "annuallyDeductionsTotal")
	private String annuallyDeductionsTotal;
	
	@Column(name = "monthlyDeductionsTotal")
	private String monthlyDeductionsTotal;
	
	@Column(name = "annuallyCtcTotal")
	private String annuallyCtcTotal;
	
	@Column(name = "monthlyCtcTotal")
	private String monthlyCtcTotal;
	
	@Column(name = "finalOfferLetterPath")
	private String finalOfferLetterPath;
	
	@Column(name = "joiningBonusAmount")
	private String joiningBonusAmount;
	
	@Column(name = "finalMonthlyCtc")
	private String finalMonthlyCtc;
	
	@Column(name = "finalAnnaullyCtc")
	private String finalAnnaullyCtc;
	
	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;
	
}
