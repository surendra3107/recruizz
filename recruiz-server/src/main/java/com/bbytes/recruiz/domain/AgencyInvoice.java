package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "agency_invoice")
@Data
@EqualsAndHashCode(callSuper = false,exclude = {"candidateInvoices","agencyInvoicePaymentHistories","taxDetails","taxRelatedDetails"})
@ToString(exclude = {"candidateInvoices","agencyInvoicePaymentHistories","taxDetails","taxRelatedDetails"})
@NoArgsConstructor
public class AgencyInvoice{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	// this id will come from UI
	@Column(name = "invoice_id",unique = true)
	private long invoiceId;
	
	// invoice number is unique .. but not seted as db level
	@Column(name ="invoice_number")
	private String invoiceNumber;

	@Column(name = "client_name")
	private String clientName;
	
	@Column(name = "client_id")
	private Long clientId;
	
	@Column(name = "creation_date")
	private Date creationDate;

	@Column(name = "modification_date")
	private Date modificationDate;

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getModificationDate() {
		return modificationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Column(name = "invoice_status")
	private String invoiceStatus;
	
	@Column(name = "due_date")
	private Date dueDate;

	@Column(name = "currency")
	private String currency;

	@Column(name = "sub_total")
	private double amount;

	@Column(name = "discount")
	private double discount;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "invoice_tax_details")
	@MapKeyColumn(name="name")
    @Column(name="value")
	Map<String, Double> taxDetails = new HashMap<String, Double>();
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "invoice_tax_related_details")
	@MapKeyColumn(name="tax_name")
    @Column(name="number")
	Map<String, String> taxRelatedDetails = new HashMap<String, String>();
	

	@Column(name = "total_amount")
	private double totalAmount;
	
	@Column(name = "total_amount_after_discount")
	private double totalAmountAfterDiscount;

	@Column(name = "payment_received")
	private double paymentReceived;

	@Column(name = "payment_received_date")
	private Date paymentReceivedDate;

	@Transient
	private double pendingAmount;

	@Transient
	private int delayDay;

	@Column(name = "total_amount_in_words")
	private String totalAmountInWords;

	@Column(name = "invoice_raised_by")
	private String informationFilledByUser;
	
	// registered agency details
	
	//@Column(name = "gstin")
	//private String gstin;
	
	//@Column(name = "pan")
	//private String pan;
	
	@Column(name = "org_name")
	private String organizationName;
	
	@Column(name = "org_address_line_1",columnDefinition = "longtext")
	private String organization_address_1;
	
	@Column(name = "org_address_line_2",columnDefinition = "longtext")
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
	
	// Billing person information
	
	@Column(name = "bill_client_name")
	private String billClientName;
	
	@Column(name = "bill_contact_name")
	private String billContactName;
	
	@Column(name = "bill_address_line_1",columnDefinition = "longtext")
	private String bill_address_1;
	
	@Column(name = "bill_address_line_2",columnDefinition = "longtext")
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
	
	@Column(name = "note",columnDefinition = "longtext")
	private String note;
	
	
	@OneToMany(mappedBy = "agencyInvoice", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	Set<CandidateInvoice> candidateInvoices = new HashSet<CandidateInvoice>();
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	Set<AgencyInvoicePaymentHistory> agencyInvoicePaymentHistories = new HashSet<AgencyInvoicePaymentHistory>();
	
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getPaymentReceivedDate() {
		return paymentReceivedDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setPaymentReceivedDate(Date paymentReceivedDate) {
		this.paymentReceivedDate = paymentReceivedDate;
	}

	public AgencyInvoice(String clientName,
			String invoiceStatus, Date dueDate, String currency) {
		super();
		this.clientName = clientName;
		//this.postionName = postionName;
		//this.candidateName = candidateName;
		//this.offeredDate = offeredDate;
		this.invoiceStatus = invoiceStatus;
		this.dueDate = dueDate;
		this.currency = currency;
	}

}
