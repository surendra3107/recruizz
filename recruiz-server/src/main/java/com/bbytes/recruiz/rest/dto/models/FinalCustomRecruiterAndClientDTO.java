package com.bbytes.recruiz.rest.dto.models;

import java.util.List;

import lombok.Data;

@Data
public class FinalCustomRecruiterAndClientDTO {

	List<CustomRecruiterAndClientDTO> responseData;
	List<String> headerData;
}
