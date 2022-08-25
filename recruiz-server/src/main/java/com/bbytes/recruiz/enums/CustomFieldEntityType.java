package com.bbytes.recruiz.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum CustomFieldEntityType {

	@JsonProperty("Client")
	Client,
	@JsonProperty("Position")
	Position,
	@JsonProperty("Candidate")
	Candidate,
	@JsonProperty("Prospects")
	Prospects,
	@JsonProperty("Employees")
	Employees;


	@JsonCreator
    public static CustomFieldEntityType create(String value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        for(CustomFieldEntityType v : values()) {
            if(value.equals(v.toString())) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
