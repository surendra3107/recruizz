package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class InvoiceInfoRateDTO implements Serializable {

	private static final long serialVersionUID = -7535699225543784890L;

	private String name;

	private String value;

	private String type;

}
