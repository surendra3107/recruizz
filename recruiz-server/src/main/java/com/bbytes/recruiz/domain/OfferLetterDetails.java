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
@Entity(name = "offer_letter_details")
public class OfferLetterDetails extends AbstractEntity {

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "offerLetterId")
	private long offerLetterId;
	
	@Column(name = "templateName")
	private String templateName;
	
	@Column
    private Boolean selected_status = false;
	
	@Column
    private Boolean template_with_formula;
	
	@Column(columnDefinition = "mediumtext")
	private String text;
	
	@Column(columnDefinition = "mediumtext")
	private String component;
	
	@Column(columnDefinition = "mediumtext")
	private String deduction;
	
	@Column(columnDefinition = "mediumtext")
	private String calculation;

	@Column(name = "monthlyGross")
	private String monthlyGross;
	
	@Column(name = "annuallyGross")
	private String annuallyGross;
	
	@Column(name = "annuallyDeductions")
	private String annuallyDeductions;
	
	@Column(name = "monthlyDeductions")
	private String monthlyDeductions;
	
	@Column(name = "annuallyCtc")
	private String annuallyCtc;
	
	@Column(name = "monthlyCtc")
	private String monthlyCtc;
	
	@Column(columnDefinition = "mediumtext")
	private String annuallyDeductionsFormula;
	
	@Column(columnDefinition = "mediumtext")
	private String monthlyDeductionsFormula;
	
	@Column(columnDefinition = "mediumtext")
	private String annuallyCtcFormula;
	
	@Column(columnDefinition = "mediumtext")
	private String monthlyCtcFormula;
	
	@Column(name = "templatePath")
	private String templatePath;
	
	@Column(name = "finalOfferLetterPath")
	private String finalOfferLetterPath;
	
	@Column(name = "finalMonthlyCtc")
	private String finalMonthlyCtc;
	
	@Column(name = "finalAnnaullyCtc")
	private String finalAnnaullyCtc;
	
	@Column(name = "roundFormulaList")
	private String roundFormulaList;
	
	@Column(name = "monthlyCtcValue")
	private String monthlyCtcValue;
	
	@Column(name = "annaullyCtcValue")
	private String annaullyCtcValue;
	
	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;
	
}
