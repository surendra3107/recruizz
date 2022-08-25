package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class CandidateProfileDTO implements Serializable {

    private static final long serialVersionUID = 909228243372068381L;

    private Set<String> interviewerEmails = new HashSet<String>();

    private List<String> ccEmails = new ArrayList<>();

    private List<Map<String, String>> roundCandidateData = new ArrayList<Map<String, String>>();

    private String notes = "N/A";

    private String positionCode;

    private String subject;

    private List<String> roundCandidateIds = new ArrayList<String>();

    private String maskedResume = "n";

    private String feedbackQueSetId;

    private boolean attchedResume = false;

    private boolean ignoreFeedback = false;
    
    private String excelAttachmentPath;
    
    private Boolean isJDAttached = false;
    
    private Boolean maskedCtc = true;

}
