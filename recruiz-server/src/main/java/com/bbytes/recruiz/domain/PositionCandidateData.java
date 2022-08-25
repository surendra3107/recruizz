package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "position_candidate_data")
public class PositionCandidateData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "position_id")
	private long positionId;
	
	@Column(name = "client_id")
	private long clientId;
	
	@Column(name = "candidate_id")
	private long candidateId;
	
	@Column(name = "logged_user_id")
	private long loggedUserId;
	
	@Column(name = "from_status")
	private String fromStatus;
	
	@Column(name = "to_status")
	private String toStatus;
	
	@Column(name = "from_stage")
	private String fromStage;
	
	@Column(name = "to_stage")
	private String toStage;
	
	@Column(name = "modification_date")
	private Date ModificationTimestamp;
	
	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;

}
