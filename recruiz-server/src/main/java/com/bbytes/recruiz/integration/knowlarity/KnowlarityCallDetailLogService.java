package com.bbytes.recruiz.integration.knowlarity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.KnowlarityCallDetailLogs;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class KnowlarityCallDetailLogService extends AbstractService<KnowlarityCallDetailLogs, Long> {

	@Autowired
	KnowlarityCallDetailLogRepository knowlarityCallDetailLogRepository;
	
	@Autowired
	public KnowlarityCallDetailLogService(KnowlarityCallDetailLogRepository knowlarityCallDetailLogRepository) {
		super(knowlarityCallDetailLogRepository);
		this.knowlarityCallDetailLogRepository = knowlarityCallDetailLogRepository;
	}

	public KnowlarityCallDetailLogs getByKnowlarityCallDetailId(long id) {
		// TODO Auto-generated method stub
		return knowlarityCallDetailLogRepository.getByKnowlarityCallDetailId(id);
	}
	
}
