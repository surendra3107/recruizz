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
@Entity(name = "sixth_sense_city")
@NamedQuery(name = "SixthSenseCity.cityWithBaseDTO", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO(city.code, city.name,city.groupLabel)"
		+ "FROM sixth_sense_city city")
public class SixthSenseCity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String name;

	@Column
	private String code;

	@Column(name = "group_label")
	private String groupLabel;

}
