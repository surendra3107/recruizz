
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
public class RchilliJDCertifications {

    @JsonProperty("Preferred")
    public List<Object> preferred = null;
    
    @JsonProperty("Required")
    public List<Object> required = null;

}
