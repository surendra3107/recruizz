package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.SharedLinkAnalytics;

public interface SharedLinkAnalyticsRepository extends JpaRepository<SharedLinkAnalytics, Long> {

	List<SharedLinkAnalytics> findByEventTypeAndPlatform(String eventype, String platform);

	List<SharedLinkAnalytics> findByEventTypeAndPlatformAndPositionCode(String eventype, String platform,
			String positionCode);

}
