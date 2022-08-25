package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "forward_profile")
public class ForwardProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length=1000)
	private String subject;

	@Column(columnDefinition="text")
	private String body;


	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "profile_reciever", joinColumns = { @JoinColumn(name = "id") })
	private Set<String> profileReciever = new HashSet<String>();
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "forwarded_candidate", joinColumns = { @JoinColumn(name = "id") })
	private Set<String> roundCandidateId = new HashSet<String>();

	private String emailFrom;

	private Date date = new Date();
	
	@Column(length=1000)
	private String attachmentLink;
	
	@Column
	private String positionCode;
}
