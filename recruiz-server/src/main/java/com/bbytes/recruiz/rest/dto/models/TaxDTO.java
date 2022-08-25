package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;

import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class TaxDTO implements Serializable {

	private static final long serialVersionUID = -5092270908468442724L;

	private long taxId;

	private String taxName;

	private String taxNumber;

	private Date modificationDate;

	private Date creationDate;

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
}
