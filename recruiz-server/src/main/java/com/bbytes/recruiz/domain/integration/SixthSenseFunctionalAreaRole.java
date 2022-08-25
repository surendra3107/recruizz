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
@Entity(name = "sixth_sense_functional_area_role")
@NamedQuery(name = "SixthSenseFunctionalAreaRole.roleWithBaseDTO", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO(role.code, role.name, role.groupLabel, role.functionalAreaCode)"
		+ "FROM sixth_sense_functional_area_role role")
public class SixthSenseFunctionalAreaRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String name;

	@Column
	private String code;

	@Column(name = "group_label")
	private String groupLabel;

	@Column(name = "functional_area_code")
	private String functionalAreaCode;

}
