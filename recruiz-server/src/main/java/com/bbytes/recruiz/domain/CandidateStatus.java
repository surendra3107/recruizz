package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "candidate_status")
@EntityListeners({ AbstractEntityListener.class })
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"candidate", "client", "position" })
@ToString(exclude = {"candidate", "client", "position" })
@NoArgsConstructor
public class CandidateStatus extends AbstractEntity {

	private static final long serialVersionUID = 5077093217586341208L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Client client;

	@ManyToOne(fetch = FetchType.LAZY)
	private Position position;

	@ManyToOne(fetch = FetchType.LAZY)
	private Candidate candidate;

	private String status;

	private Date statusChangedDate;

	private Boolean current;

	private Date joiningDate;

	@Column(columnDefinition = "longtext")
	private String notes;

	private Boolean onBoarded;

	//@OneToMany(mappedBy = "candidateStatusId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	//private Set<AgencyInvoice> agencyInvoices = new HashSet<AgencyInvoice>();

	
	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getJoiningDate() {
		return joiningDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setJoiningDate(Date joiningDate) {
		this.joiningDate = joiningDate;
	}

}
