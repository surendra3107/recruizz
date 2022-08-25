package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CalculationDTO implements Serializable{

	private static final long serialVersionUID = -6403733374408987100L;

	private double subAmount;
	
	private double totalAmount;
	
	private double discount;
	
	private double discountPercentValue;
	
	private List<CalculatePercentDTO> calculatePercentDTOs = new ArrayList<CalculatePercentDTO>();
}
