package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity(name="prospect_contact_info")
@EqualsAndHashCode(callSuper = false)
@EntityListeners({ AbstractEntityListener.class })
@ToString
public class ProspectContactInfo extends AbstractEntity {

	private static final long serialVersionUID = -1971323132244452514L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 1000)
	private String name;

	@Column(unique = true)
	private String email;

	@Column(unique = true, nullable = false)
	private String mobile;

	@Column
	private String designation;

	@ManyToOne(fetch = FetchType.LAZY)
	private Prospect prospect;

}
