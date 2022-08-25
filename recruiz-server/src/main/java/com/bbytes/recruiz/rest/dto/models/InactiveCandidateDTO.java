package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class InactiveCandidateDTO implements Serializable {

    private static final long serialVersionUID = -219012943789015471L;

    private String clientName;
    private String positionName;
    private Long candidateCount;
    private String pipelineLink;

    public InactiveCandidateDTO(String clientName, String positionName, Long candidateCount, String pipelineLink) {
	this.clientName = clientName;
	this.positionName = positionName;
	this.candidateCount = candidateCount;
	this.pipelineLink = pipelineLink;
    }

}
