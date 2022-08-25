package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Input sixth sense user object for CRUD operation
 * 
 * @author akshay
 *
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SixthSenseDeleteUserDTO implements Serializable {

	private static final long serialVersionUID = -6116364643249538856L;

	// sixth sense users list
	@JsonProperty("users")
	private List<String> users = new ArrayList<String>();

}
