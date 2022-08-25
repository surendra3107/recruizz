
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
public class RchilliJDSalaryOffered {

    @JsonProperty("MinAmount")
    public String minAmount;
    
    @JsonProperty("MaxAmount")
    public String maxAmount;
    
    @JsonProperty("Currency")
    public String currency;
    
    @JsonProperty("Units")
    public String units;
    
    @JsonProperty("Text")
    public String text;

}
