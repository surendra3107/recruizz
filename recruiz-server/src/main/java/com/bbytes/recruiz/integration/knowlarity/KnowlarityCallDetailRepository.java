package com.bbytes.recruiz.integration.knowlarity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.KnowlarityCallDetails;

public interface KnowlarityCallDetailRepository extends JpaRepository<KnowlarityCallDetails, Long>{

	@Query(value ="select * from knowlarity_call_details k where k.call_logs_status=?1", nativeQuery = true)
	List<KnowlarityCallDetails> findByPendingCalllogstatus(String string);

}
