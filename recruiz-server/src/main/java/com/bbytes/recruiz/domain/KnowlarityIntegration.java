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
@Entity(name = "knowlarity_integration")
@ToString
public class KnowlarityIntegration extends AbstractEntity{

	
	private static final long serialVersionUID = -7085546768563616061L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "organization_id")
	private String organization_id;

	@Column(name = "sr_number")
	private String sr_number;

	@Column(name = "caller_id")
	private String caller_id;

	@Column(name = "authorization_key")
	private String authorization_key;

	@Column(name = "xApi_key")
	private String xApi_key;

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
