package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class DiscountDTO implements Serializable{
	
	private static final long serialVersionUID = -9062149093072980691L;
	
	private double discount;
	
	private double subTotal;
	
	private double total;
	
	private double discountPercentageValue;

}
