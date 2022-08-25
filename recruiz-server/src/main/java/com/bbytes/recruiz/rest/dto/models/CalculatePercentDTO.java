package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculatePercentDTO implements Serializable {

	
	private static final long serialVersionUID = 5638055593730803648L;

	private double percentValue;
	
	private double percent;
	
	private String taxName;

	public CalculatePercentDTO(double percentValue, double percent, String taxName) {
		this.percentValue = percentValue;
		this.percent = percent;
		this.taxName = taxName;
		
	}
	
	
}
