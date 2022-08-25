package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "tax")
@EntityListeners({ AbstractEntityListener.class })
@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
public class Tax extends AbstractEntity {
	
	private static final long serialVersionUID = -921935115141114211L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "tax_name",nullable = false, unique = true)
	private String taxName;
	
	@Column(name = "tax_number",nullable = false)
	private String taxNumber;
}
