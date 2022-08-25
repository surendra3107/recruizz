package com.bbytes.recruiz.integration.sixth.sense;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.integration.SixthSenseUserUsage;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseUserUsageService extends AbstractService<SixthSenseUserUsage, Long> {

	private SixthSenseUserUsageRepository userUsageRepository;

	@Autowired
	public SixthSenseUserUsageService(SixthSenseUserUsageRepository userUsageRepository) {
		super(userUsageRepository);
		this.userUsageRepository = userUsageRepository;
	}

	@Transactional(readOnly = true)
	public SixthSenseUserUsage findBetweenDates(String email, Date startDate, Date endDate) {
		return userUsageRepository.findTOP1ByEmailAndDateTimeBetween(email, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public List<SixthSenseUserUsage> findAllBetweenDates(String email, Date startDate, Date endDate) {
		return userUsageRepository.findByEmailAndDateTimeBetween(email, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public int getViewCountBetweenDates(String email, Date startDate, Date endDate) {

		Integer count = userUsageRepository.getViewCountByEmailAndBetweenDates(email, startDate, endDate);

		return count == null ? 0 : count;
	}
}
