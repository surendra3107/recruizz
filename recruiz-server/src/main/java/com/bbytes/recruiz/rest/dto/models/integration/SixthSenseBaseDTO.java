package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import com.bbytes.recruiz.rest.dto.models.BaseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class SixthSenseBaseDTO extends BaseDTO implements Serializable {

	private static final long serialVersionUID = 8661857060359126684L;

	String parentId;

	String groupLabel;

	public SixthSenseBaseDTO(String id, String value, String groupLabel) {
		super(id, value);
		this.groupLabel = groupLabel;
	}

	public SixthSenseBaseDTO(String id, String value, String groupLabel, String parentId) {
		super(id, value);
		this.groupLabel = groupLabel;
		this.parentId = parentId;
	}

}
