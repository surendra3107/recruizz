package com.bbytes.recruiz.domain.integration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity(name = "sixth_sense_functional_area")
@NamedQuery(name = "SixthSenseFunctionalArea.funcAreaWithBaseDTO", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.BaseDTO(farea.code, farea.name)"
		+ "FROM sixth_sense_functional_area farea")
public class SixthSenseFunctionalArea {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String name;

	@Column
	private String code;

}
