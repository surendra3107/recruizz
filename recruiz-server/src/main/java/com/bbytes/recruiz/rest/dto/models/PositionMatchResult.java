package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import org.springframework.data.domain.Page;

import com.bbytes.recruiz.domain.Candidate;

import lombok.Data;

@Data
public class PositionMatchResult implements Serializable {

	private static final long serialVersionUID = -4283248621429252655L;

	private Object position;

	private Page<Candidate> candidateList;
}
