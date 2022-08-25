package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class PointsDTO implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3582621844806956448L;

	String roundName;
	
	String Category;

	String point;

	
}
