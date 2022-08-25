package com.bbytes.recruiz.search.domain;

import java.util.ArrayList;
import java.util.List;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;

import lombok.Data;

@Data
public class GlobalSearchResult {

	List<Candidate> candidates = new ArrayList<>();

	List<Position> positions = new ArrayList<>();

	List<ClientOpeningCountDTO> clients = new ArrayList<>();

	public GlobalSearchResult() {
	}
	
	public GlobalSearchResult(List<ClientOpeningCountDTO> clients, List<Position> positions, List<Candidate> candidates) {
		this(clients, positions);
		this.candidates = candidates;
	}

	public GlobalSearchResult(List<ClientOpeningCountDTO> clients, List<Position> positions) {
		this.clients = clients;
		this.positions = positions;
	}

	public String toString() {
		return " Client total : " + this.clients.size() + " Position total : " + this.positions.size() + " Candidates total : "
				+ this.candidates.size();
	}
}
