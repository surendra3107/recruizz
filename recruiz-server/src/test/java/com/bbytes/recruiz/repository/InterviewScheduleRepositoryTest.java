package com.bbytes.recruiz.repository;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;

public class InterviewScheduleRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserRolesRepository userRolesRepository;

	@Autowired
	ClientInterviewPanelRepository clientInterviewPanelRepo;

	@Autowired
	RoundRepository roundRepo;

	@Autowired
	CandidateRepository candidateRepo;

	@Autowired
	PositionRepository positionRepository;

	@Autowired
	InterviewScheduleRepository interviewScheduleRepo;

	private String tenantName = "dummy";



}
