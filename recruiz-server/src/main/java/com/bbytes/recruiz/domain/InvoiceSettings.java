package com.bbytes.recruiz.domain;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * basically this table keep information while generating 1st Invoice for 2nd
 * invoice , take information from this table and in adminSettings if update
 * then it immediately reflect in this table.
 *
 */
@Entity(name = "invoice_settings")
@EntityListeners({ AbstractEntityListener.class })
@Data
@EqualsAndHashCode(callSuper = false , exclude= {"taxDetails","taxRelatedDetails"})
@ToString(exclude = {"taxDetails","taxRelatedDetails"})
@NoArgsConstructor
public class InvoiceSettings extends AbstractEntity{

	private static final long serialVersionUID = 3992610390707104751L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	// registered agency details
	//@Column(name = "gstin")
	//private String gstin;

	//@Column(name = "pan")
	//private String pan;

	@Column(name = "org_name")
	private String organizationName;

	@Column(name = "org_address_line_1", columnDefinition = "longtext")
	private String organization_address_1;

	@Column(name = "org_address_line_2", columnDefinition = "longtext")
	private String organization_address_2;

	@Column(name = "org_city")
	private String organizationCity;

	@Column(name = "org_state")
	private String organizationState;

	@Column(name = "org_country")
	private String organizationCountry;

	@Column(name = "org_pin_code")
	private String organizationPin;

	@Column(name = "org_phone")
	private String organizationPhone;

	// organization account details
	@Column(name = "cheque_payable")
	private String chequePayable;

	@Column(name = "org_account_name")
	private String organizationAccountName;

	@Column(name = "org_account_number")
	private String organizationAccountNumber;

	@Column(name = "org_bank_name")
	private String organizationBankName;

	@Column(name = "org_bank_branch_name")
	private String organizationBankBranchName;

	@Column(name = "org_bank_ifsc")
	private String organizationBankIfsc;

	@Column(name = "note", columnDefinition = "longtext")
	private String note;

	// Billing person information

	@Column(name = "bill_client_name")
	private String billClientName;

	@Column(name = "bill_contact_name")
	private String billContactName;

	@Column(name = "bill_address_1", columnDefinition = "longtext")
	private String bill_address_1;

	@Column(name = "bill_address_2", columnDefinition = "longtext")
	private String bill_address_2;

	@Column(name = "bill_city")
	private String billCity;

	@Column(name = "bill_state")
	private String billState;

	@Column(name = "bill_country")
	private String billCountry;

	@Column(name = "bill_pin_code")
	private String billPin;

	@Column(name = "bill_phone")
	private String billPhone;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "invoice_settings_tax_details")
	@MapKeyColumn(name="name")
    @Column(name="value")
	Map<String, Double> taxDetails = new HashMap<String, Double>(); 
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "invoice_settings_tax_related_details")
	@MapKeyColumn(name="tax_name")
    @Column(name="number")
	Map<String, String> taxRelatedDetails = new HashMap<String, String>();
}
