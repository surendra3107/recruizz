package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.domain.AbstractEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "servetel_agent")
@ToString
public class ServetelAgent extends AbstractEntity{
	
	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "organization_id")
	private String organizationId;

	@Column(name = "agentName")
	private String agentName;
	
	@Column(name = "mobile")
	private String mobile;

	@Column(name = "agentId")
	private String agentId;
	
	@Column(name = "userId")
	private Long userId;
	
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

}
