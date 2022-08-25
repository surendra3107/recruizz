package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class RoundDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String roundName;

	private String roundType;

	private String roundId;
}
