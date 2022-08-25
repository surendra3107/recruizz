package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.ParserCount;
import com.bbytes.recruiz.repository.ParserCountRepository;

@Service
public class ParserCountService extends AbstractService<ParserCount, Long> {

	private ParserCountRepository parserCountRepository;

	@Autowired
	public ParserCountService(ParserCountRepository parserCountRepository) {
		super(parserCountRepository);
		this.parserCountRepository = parserCountRepository;
	}

	
	public long getMaxId() {
		String maxID = parserCountRepository.findMaxId();
		if (maxID == null)
			return 0;

		return Long.parseLong(maxID);
	}

}
