
package com.bbytes.recruiz.rest.dto.models.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonRootName(value = "JobProfile")
public class RchilliJDJobProfile {

    @JsonProperty("Title")
    public String title;
    
    @JsonProperty("Alias")
    public String alias;
    
    @JsonProperty("RelatedSkills")
    public String relatedSkills;

}
