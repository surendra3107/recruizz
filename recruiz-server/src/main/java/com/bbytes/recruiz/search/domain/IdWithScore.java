package com.bbytes.recruiz.search.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdWithScore {

	private Long id;

	private Float searchScore;

	public IdWithScore(Long id, Float searchScore) {
		this.id = id;
		this.searchScore = searchScore;
	}
	
}
