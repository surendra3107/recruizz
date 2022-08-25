package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseDTO implements Serializable {

	private static final long serialVersionUID = -7751461039510283289L;

	String id;

	String value;
	
	String type;

	public BaseDTO(String id, String value) {
		super();
		this.id = id;
		this.value = value;
	}

}
