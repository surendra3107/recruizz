package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "selected_custom_reports")
public class SelectedCustomField extends AbstractEntity {

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(columnDefinition = "mediumtext")
	private String textValues;

	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;
	
}
