package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class RouteModel {

	@Id
	private String id;
	
	@Column(unique = true,length=1000)
	private String mailGunRouteId;

	@Column(length=1000)
	private String mailId;

	@Column(length=1000)
	private String webHookURL;

}
