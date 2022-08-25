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
@Entity(name = "sixth_sense_ug_degree_specialization")
@NamedQuery(name = "SixthSenseUGDegreeSpecialization.degreeSpecWithBaseDTO", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO(spec.code, spec.name, spec.groupLabel, spec.degreeCode)"
		+ "FROM sixth_sense_ug_degree_specialization spec")
public class SixthSenseUGDegreeSpecialization {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String name;

	@Column
	private String code;
	
	@Column(name = "group_label")
	private String groupLabel;

	@Column(name = "degree_code")
	private String degreeCode;

}
