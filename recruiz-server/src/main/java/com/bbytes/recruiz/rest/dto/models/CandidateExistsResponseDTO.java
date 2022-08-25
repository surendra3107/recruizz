package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import com.bbytes.recruiz.domain.Candidate;

import lombok.Data;

@Data
public class CandidateExistsResponseDTO implements Serializable {

    private static final long serialVersionUID = -2190129400799015471L;

    private Long cid;

    private Candidate oldCandidate;

    private Candidate newCandidate;

    private String newFilePath;
    
    private boolean candidateExists = true;

}
