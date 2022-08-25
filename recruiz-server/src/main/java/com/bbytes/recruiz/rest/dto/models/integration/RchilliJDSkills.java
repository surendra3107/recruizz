
package com.bbytes.recruiz.rest.dto.models.integration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RchilliJDSkills {

    @JsonProperty("Preferred")
    public List<RchilliJDPreferredSkill> preferred = null;
    
    @JsonProperty("Required")
    public List<RchilliJDRequiredSkill> required = null;

}
