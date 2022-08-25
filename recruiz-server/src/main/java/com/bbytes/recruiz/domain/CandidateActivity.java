package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.joda.time.DateTime;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "candidate_activity")
@EntityListeners({AbstractEntityListener.class })
public class CandidateActivity extends AbstractEntity {

	private static final long serialVersionUID = 4502748737300132321L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private Date whatTime = DateTime.now().toDate();

	private String who;
	
	@Column(columnDefinition = "text")
	private String what;
	
	@Column
	private String offerApprovalId;
	
	@Column
	private String knowlarityCallDetailId;

	@Column
	private String ivr_integration;
	
	private String candidateId;
	
	private String type;
	

    @JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getWhatTime() {
		return whatTime;
	}

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setWhatTime(Date whatTime) {
		this.whatTime = whatTime;
	}
    
    public CandidateActivity(String who, String what,String candidateId,String type){
    	this.who = who;
    	this.what = what;
    	this.candidateId = candidateId;
    	this.type = type;
    }

}