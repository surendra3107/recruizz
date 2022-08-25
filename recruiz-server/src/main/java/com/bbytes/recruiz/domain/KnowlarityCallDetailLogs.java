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
@Entity(name = "knowlarity_call_detail_logs")
@ToString
public class KnowlarityCallDetailLogs extends AbstractEntity{

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "knowlarityCallDetails_Id")
	private long knowlarityCallDetails_Id;

	@Column(name = "posting_time")
	private String posting_time;
	
	@Column(name = "hangup_time")
	private String hangup_time;

	@Column(name = "outcall_pickup_time")
	private String outcall_pickup_time;
	
	@Column(name = "rec_timetaken")
	private String rec_timetaken;

	@Column(name = "recordingurl_system")
	private String recordingurl_system;

	@Column(name = "status")
	private String status;

	@Column(name = "field1")
	private String field1;

	@Column(name = "field2")
	private String field2;

	@Column(name = "field3")
	private String field3;

	@Column(name = "field4")
	private String field4;
	
	@Column(name = "field5")
	private String field5;
	
}
