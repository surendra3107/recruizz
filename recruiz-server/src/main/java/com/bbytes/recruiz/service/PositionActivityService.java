package com.bbytes.recruiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.repository.PositionActivityRepository;

@Service
public class PositionActivityService extends AbstractService<PositionActivity, Long> {

	private static final Logger logger = LoggerFactory.getLogger(PositionActivityService.class);

	private PositionActivityRepository positionActivityRepository;

	@Autowired
	public PositionActivityService(PositionActivityRepository positionActivityRepository) {
		super(positionActivityRepository);
		this.positionActivityRepository = positionActivityRepository;
	}

	@Transactional
	public void addActivity(PositionActivity positionActivity) {
		positionActivityRepository.save(positionActivity);
	}

	
	public Page<PositionActivity> getPositionActivity(String positionCode,Pageable pageable){
		return positionActivityRepository.findByPositionCode(positionCode, pageable);
	}

	

}