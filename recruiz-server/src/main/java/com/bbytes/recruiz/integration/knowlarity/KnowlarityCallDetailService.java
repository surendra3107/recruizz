package com.bbytes.recruiz.integration.knowlarity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.KnowlarityCallDetails;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class KnowlarityCallDetailService extends AbstractService<KnowlarityCallDetails, Long> {

	@Autowired
	KnowlarityCallDetailRepository knowlarityCallDetailRepository;
	
	@Autowired
	public KnowlarityCallDetailService(KnowlarityCallDetailRepository knowlarityCallDetailRepository) {
		super(knowlarityCallDetailRepository);
		this.knowlarityCallDetailRepository = knowlarityCallDetailRepository;
	}

	public List<KnowlarityCallDetails> findByPendingCalllogstatus() {
	
		return knowlarityCallDetailRepository.findByPendingCalllogstatus("pending");
		
	}
	
}
