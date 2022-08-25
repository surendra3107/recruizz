
package com.bbytes.recruiz.rest.dto.models.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RchilliJDJobLocation {

    @JsonProperty("Location")
    public String location;
    
    @JsonProperty("City")
    public String city;
    
    @JsonProperty("State")
    public String state;
    
    @JsonProperty("Country")
    public String country;
    
    @JsonProperty("IsoCountryCode")
    public String isoCountryCode;
    
    @JsonProperty("ZipCode")
    public String zipCode;

}
