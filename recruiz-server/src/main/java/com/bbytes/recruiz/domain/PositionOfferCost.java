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
@Entity(name = "position_offer_cost")
public class PositionOfferCost extends AbstractEntity {

	private static final long serialVersionUID = -7085546768563616061L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@Column(name = "position_id")
	private long position_id;

	@Column(name = "billRate")
	private String billRate;

	@Column(name = "billHours")
	private String billHours;

	@Column(name = "billingDate")
	private Date billingDate;

	@Column(name = "projectDuration")
	private String projectDuration;

	@Column(name = "oneTimeCost")
	private String oneTimeCost;

	@Column(name = "headHunting")
	private String headHunting;
	
	@Column(name = "status")
	private boolean status;

	@Column(name = "field1")
	private String field1;

	@Column(name = "field2")
	private String field2;

	@Column(name = "field3")
	private String field3;

	@Column(name = "field4")
	private String field4;


}
