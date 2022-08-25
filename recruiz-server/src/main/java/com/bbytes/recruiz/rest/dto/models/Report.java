package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Report implements Serializable {

	private static final long serialVersionUID = -4283248621429252655L;

	private String title;

	private String xLabel;

	private String yLabel;

	private String chartData;

	private Object details;

	private Object reportData;

	private Object metaData;
	
	private List<String> vendors;
}
