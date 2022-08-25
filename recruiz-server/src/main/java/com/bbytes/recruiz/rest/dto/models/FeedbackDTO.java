package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FeedbackDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private String cid; // candidate id

	private String roundId;

	private String positionCode;

	private String feedback = "N/A";

	private String status;
	
	private String feedbackType;	// (forwarded / interview schedule)

	private Map<String, String> points = new HashMap<String, String>();
	
	private String feedbackId;
	
	private List<String> reason;
	
	private String rating;

}
