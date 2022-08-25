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
@Entity(name = "org_bank_info")
public class OrganizationBankInfo extends AbstractEntity {
	
	private static final long serialVersionUID = -7303946352377039772L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false)
	private String bankName;
	
	private String accountName;
	
	private String branch;
	
	@Column(nullable = false, unique = true)
	private String accountNumber;
	
	@Column(name = "ifsc_code")
	private String ifscCode;
	
	@Column(nullable = false)
	private String addedBy;
	
	private Boolean defaultBankDetails = false;
	
	public OrganizationBankInfo(String bankName, String branch, String accountNumber, String ifscCode) {
		
		this.bankName = bankName;
		this.branch = branch;
		this.accountNumber = accountNumber;
		this.ifscCode = ifscCode;
	}
	
	

}
