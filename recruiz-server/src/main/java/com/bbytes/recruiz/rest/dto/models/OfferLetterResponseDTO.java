package com.bbytes.recruiz.rest.dto.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class OfferLetterResponseDTO {

	List<OfferLetterFormulaDTO> annualCtc;
	
	List<OfferLetterFormulaDTO> monthlyCtc;
	
	List<OfferLetterFormulaDTO> annualDeduction;
	
	List<OfferLetterFormulaDTO> monthlyDeduction;
	
	int totalCtc;
	
	boolean templateWithFormula;
	
	String designation;
	
	Date doj;
	
	String joiningBonusCost;
	
	long approvalId;
	
	String monthlyCtcValue;
	
	String annaullyCtcValue;
	
	long id;
	
	long candidateId;
	
	String fileName;
	
	String filePath;
}
