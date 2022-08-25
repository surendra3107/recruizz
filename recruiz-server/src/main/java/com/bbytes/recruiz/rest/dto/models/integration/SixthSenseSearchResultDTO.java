
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.bbytes.recruiz.integration.sixth.sense.SixthSenseCustomDeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
public class SixthSenseSearchResultDTO implements Serializable {
	private static final long serialVersionUID = 3405453481086685165L;

	@JsonProperty("hits")
	public long hits;

	@JsonProperty("src")
	public String source;

	@JsonProperty("results")
	@JsonDeserialize(using = SixthSenseCustomDeSerializer.class)
	public Object resultsRawFormat;

	public List<SixthSenseResultDTO> resultData = null;

	public String resultMessage;

	@SuppressWarnings("unchecked")
	public List<SixthSenseResultDTO> getResultData() {
		if (getResultsRawFormat() != null && getResultsRawFormat() instanceof Collection) {
			return (List<SixthSenseResultDTO>) getResultsRawFormat();
		}
		return null;
	}

	public String getResultMessage() {
		if (getResultsRawFormat() != null && getResultsRawFormat() instanceof String) {
			return (String) getResultsRawFormat();
		}
		return null;
	}

}
