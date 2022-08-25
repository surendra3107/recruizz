package com.bbytes.recruiz.integration.sixth.sense;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.integration.SixthSenseUserUsage;

public interface SixthSenseUserUsageRepository extends JpaRepository<SixthSenseUserUsage, Long> {

	SixthSenseUserUsage findTOP1ByEmailAndDateTimeBetween(String email, Date startDate, Date endDate);

	List<SixthSenseUserUsage> findByEmailAndDateTimeBetween(String email, Date startDate, Date endDate);

	@Query(value = "select sum(view_count) from sixth_sense_user_usage where email = ?1 and date_time between ?2 and ?3", nativeQuery = true)
	Integer getViewCountByEmailAndBetweenDates(String email, Date startDate, Date endDate);
}
