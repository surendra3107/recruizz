
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
public class RchilliJDPreferredDemographic {

    @JsonProperty("Nationality")
    public String nationality;
    @JsonProperty("Visa")
    public String visa;
    @JsonProperty("AgeLimit")
    public String ageLimit;
    @JsonProperty("Others")
    public String others;

}
