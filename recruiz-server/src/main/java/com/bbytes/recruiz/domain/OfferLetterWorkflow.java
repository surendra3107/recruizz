package com.bbytes.recruiz.domain;

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
@Entity(name = "offer_letter_workflow")
public class OfferLetterWorkflow extends AbstractEntity {

	private static final long serialVersionUID = -7085546768563616061L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@Column(name = "workflow_name")
	private String workflow_name;
	
	@Column
    private String lower_margin;
	
	@Column
	private String upper_margin;
	
	@Column(name = "lower_margin_operator")
	private String lower_margin_operator;
	
	@Column(name = "approver_name")
	private String approver_name;
	
	@Column(name = "upper_margin_operator")
	private String upper_margin_operator;
	
	@Column(name = "approver_email")
	private String approver_email;

	@Column(name = "other_loop")
	private String other_loop;
	
	@Column(name = "other_loop_name")
	private String other_loop_name;
	
	@Column(name = "status")
	private Boolean status;
	
	@Column(name = "field1")
	private String field1;
	
	@Column(name = "field2")
	private String field2;
	
	@Column(name = "field3")
	private String field3;
	
	@Column(name = "field4")
	private String field4;
	
}
