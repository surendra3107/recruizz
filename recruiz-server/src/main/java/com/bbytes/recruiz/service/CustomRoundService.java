package com.bbytes.recruiz.service;

import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CustomRounds;
import com.bbytes.recruiz.repository.RoundCustomRepository;

@Service
public class CustomRoundService extends AbstractService<CustomRounds, Long> {

	private RoundCustomRepository customRoundRepository;

	@Autowired
	public CustomRoundService(RoundCustomRepository customRoundRepository) {
		super(customRoundRepository);
		this.customRoundRepository = customRoundRepository;
	}

	@Transactional(readOnly=true)
	public LinkedList<CustomRounds> getAllRounds(){
	    LinkedList<CustomRounds> list = new LinkedList<>();
	    if(null != customRoundRepository.findAll() && !customRoundRepository.findAll().isEmpty()) {
		list.addAll(customRoundRepository.findAll());
	    }
	    return list;
	}


}
