package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "org_tax_details")
public class OrganizationTaxDetails extends AbstractEntity {

	private static final long serialVersionUID = -7877149233118896615L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "tax_name", unique = true)
	private String taxName;

	@Column(name = "tax_value")
	private String taxValue;

	public OrganizationTaxDetails(String taxName, String taxValue) {
		this.taxName = taxName;
		this.taxValue = taxValue;
	}
	
	

}
