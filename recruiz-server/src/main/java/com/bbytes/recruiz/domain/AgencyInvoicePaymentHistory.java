package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "agency_invoice_payment_history")
@EntityListeners({ AbstractEntityListener.class })
@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
public class AgencyInvoicePaymentHistory extends AbstractEntity  implements Comparable<AgencyInvoicePaymentHistory>{

	private static final long serialVersionUID = -4503562452248093724L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "received_amount")
    private double recivedAmount;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_received_by")
    private String paymentReceivedBy;

    // @ManyToOne(fetch = FetchType.LAZY)
    // private AgencyInvoice agencyInvoice;

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getPaymentDate() {
	return paymentDate;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setPaymentDate(Date paymentDate) {
	this.paymentDate = paymentDate;
    }

    public AgencyInvoicePaymentHistory(double totalAmount, double recivedAmount, String currency) {
	super();
	this.totalAmount = totalAmount;
	this.recivedAmount = recivedAmount;
	this.paymentDate = new Date();
	this.currency = currency;
    }

    public AgencyInvoicePaymentHistory(double totalAmount, String currency) {
	super();
	this.totalAmount = totalAmount;
	this.paymentDate = new Date();
	this.currency = currency;
    }

	@Override
	public int compareTo(AgencyInvoicePaymentHistory agencyInvoicePaymentHistory) {
		return (int) (this.id - agencyInvoicePaymentHistory.getId());
	}
    
}
