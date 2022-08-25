package com.bbytes.recruiz.integration.knowlarity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.KnowlarityCallDetailLogs;

public interface KnowlarityCallDetailLogRepository extends JpaRepository<KnowlarityCallDetailLogs, Long>{

	@Query(value ="select * from knowlarity_call_detail_logs k where k.organization_id=?1", nativeQuery = true)
	List<KnowlarityCallDetailLogs> findByPendingCalllogstatus(String logStatus);

	@Query(value ="select * from knowlarity_call_detail_logs k where k.knowlarityCallDetails_Id=?1", nativeQuery = true)
	KnowlarityCallDetailLogs getByKnowlarityCallDetailId(long id);

}
