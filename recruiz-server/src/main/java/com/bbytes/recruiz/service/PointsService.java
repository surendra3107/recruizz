package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Points;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.repository.PointsRepository;

@Service
public class PointsService extends AbstractService<Points, Long> {

	private PointsRepository pointsRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	public PointsService(PointsRepository pointsRepository) {
		super(pointsRepository);
		this.pointsRepository = pointsRepository;
	}

	public double getPointsAVGByCandidateAndPosition(String positionCode, RoundCandidate candidate) {
		return pointsRepository.findAvgPointByCandidateAndPosition(positionCode, candidate);
	}
}
