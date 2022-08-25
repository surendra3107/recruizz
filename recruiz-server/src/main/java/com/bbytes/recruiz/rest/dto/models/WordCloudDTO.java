package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class WordCloudDTO implements Serializable {

	private static final long serialVersionUID = -7751461039510283289L;

	String text;

	long weight;

}
