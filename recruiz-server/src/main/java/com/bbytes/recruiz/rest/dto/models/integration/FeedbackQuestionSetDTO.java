package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;

@Data
public class FeedbackQuestionSetDTO implements Serializable {

	private static final long serialVersionUID = 8255155723804585890L;

	private String id;

	private String title;

	private String orgId;

	private String orgName;

	private String questionSetType;

	private String description;

	private Object tags;

}
