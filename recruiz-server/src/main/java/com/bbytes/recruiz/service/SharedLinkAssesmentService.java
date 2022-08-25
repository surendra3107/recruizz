package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.SharedLinkAnalytics;
import com.bbytes.recruiz.repository.SharedLinkAnalyticsRepository;

@Service
public class SharedLinkAssesmentService extends AbstractService<SharedLinkAnalytics, Long> {

	private SharedLinkAnalyticsRepository sharedLinkAnalyticsRepository;

	@Autowired
	public SharedLinkAssesmentService(SharedLinkAnalyticsRepository sharedLinkAnalyticsRepository) {
		super(sharedLinkAnalyticsRepository);
		this.sharedLinkAnalyticsRepository = sharedLinkAnalyticsRepository;
	}

	@Transactional(readOnly = true)
	public List<SharedLinkAnalytics> getByEventTypeAndPlatform(String eventType, String platform) {
		return sharedLinkAnalyticsRepository.findByEventTypeAndPlatform(eventType, platform);
	}

	@Transactional(readOnly = true)
	public List<SharedLinkAnalytics> getByEventTypeAndPlatform(String eventType, String platform, String positionCode) {
		return sharedLinkAnalyticsRepository.findByEventTypeAndPlatformAndPositionCode(eventType, platform,
				positionCode);
	}

}
