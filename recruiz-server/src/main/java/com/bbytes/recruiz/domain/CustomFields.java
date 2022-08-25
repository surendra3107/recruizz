package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.enums.CustomFieldDataType;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.CustomFieldDBEventListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "CustomFields")
@EntityListeners({ AbstractEntityListener.class,CustomFieldDBEventListener.class })
public class CustomFields extends AbstractEntity {

	private static final long serialVersionUID = 2059711191818268950L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CustomFieldEntityType entityType;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CustomFieldDataType dataType;

	@Column
	private String dropDownValues;

	public List<String> getDropDownValueList() {
		if (dropDownValues != null && !dropDownValues.isEmpty())
			return Arrays.asList(dropDownValues.split("\\s*,\\s*"));
		else
			return new ArrayList<>();
	}

	public void setDropDownValueList(List<String> dropDownValueList) {
		dropDownValues = String.join(",", dropDownValueList);
	}

}
