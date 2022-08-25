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
@Entity(name = "offer_letter_template_variables")
public class OfferLetterTemplateVariables {

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "variableId")
	private long variableId;
	
	@Column(name = "variableName")
	private String variableName;
	
	@Column(name = "startTag")
	private String startTag;
	
	@Column(name = "endTag")
	private String endTag;
	
}
