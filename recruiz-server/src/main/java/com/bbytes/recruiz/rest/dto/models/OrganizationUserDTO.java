package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class OrganizationUserDTO implements Serializable {

    private static final long serialVersionUID = -8818990214359974910L;

    private String orgName;

    private String name;

    private String email;

    private String password;

    private String orgID;

    private String signUpMode;

    private String timezone = "Asia/Kolkata";

    private String locale = "en";

    private String logoUrlPath;

    private String websiteUrl;

    private String facebookUrl;

    private String twitterUrl;

    private String googleUrl;

    private String linkedInUrl;

    private String slackUrl;

    private String gitHubUrl;

    private String hipChatUrl;

    private String bitBucketUrl;

    private String imageByteString;

    private String fileName;

    private String recruizPlanId;

    private String mobile;

    private String gstNo;

    private String taxRegistrationNo;
    
    private String panNo;

    private String address;

    private String addressL1;

    private String addressL2;

    private String city;

    private String pincode;

    private String country;

    private String state;

    private String phone;
    
    private Long candidateNodificationDate;

}
