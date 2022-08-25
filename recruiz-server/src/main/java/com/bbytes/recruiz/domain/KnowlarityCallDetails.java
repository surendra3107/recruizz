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
@NoArgsConstructor
@Entity(name = "knowlarity_call_details")
@ToString
public class KnowlarityCallDetails extends AbstractEntity{

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "candidateId")
	private long candidateId;
	
	@Column(name = "candidateName")
	private String candidateName;

	@Column(name = "candidateEmail")
	private String candidateEmail;

	@Column(name = "candidateMobile")
	private String candidateMobile;

	@Column(name = "agentMobile")
	private String agentMobile;
	
	@Column(name = "agentEmail")
	private String agentEmail;

	@Column(name = "call_id")
	private String call_id;

	@Column(name = "status")
	private String status;

	@Column(name = "call_logs_status")
	private String call_logs_status;

	@Column(name = "field1")
	private String field1;

	@Column(name = "field2")
	private String field2;

	@Column(name = "field3")
	private String field3;

	@Column(name = "field4")
	private String field4;

}
