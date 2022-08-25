package com.bbytes.recruiz.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
@MappedSuperclass
@EntityListeners(AbstractEntityListener.class)
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 824825366833336263L;

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
}