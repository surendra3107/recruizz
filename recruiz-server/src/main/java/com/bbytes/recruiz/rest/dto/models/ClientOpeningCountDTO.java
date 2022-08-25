package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import com.bbytes.recruiz.domain.Client;

import lombok.Data;

@Data
public class ClientOpeningCountDTO implements Serializable {

	private static final long serialVersionUID = 909228243372068381L;

	private Client client;

	private Long totalPosition;
	
	private Long totalOpenings;
	
	private float searchScore;

	public ClientOpeningCountDTO(Client client, Long totalPosition) {
		super();
		this.client = client;
		this.totalPosition = totalPosition;
	}

}
