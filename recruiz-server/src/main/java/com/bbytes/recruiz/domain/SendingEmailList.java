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
@Entity(name = "sending_email_id_list")
@ToString
public class SendingEmailList extends AbstractEntity{
	
	private static final long serialVersionUID = -7085546768563616061L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "email_id")
	private String email_id;

	@Column(name = "list_type")
	private String list_type;

	@Column(name = "status")
	private String status;


}
