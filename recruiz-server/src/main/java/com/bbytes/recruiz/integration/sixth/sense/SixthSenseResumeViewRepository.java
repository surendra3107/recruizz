package com.bbytes.recruiz.integration.sixth.sense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.integration.SixthSenseResumeView;

public interface SixthSenseResumeViewRepository extends JpaRepository<SixthSenseResumeView, String> {

	SixthSenseResumeView findByResumeId(String resumeId);

	@Query(value = "select DATEDIFF(NOW(),view_on_date) from sixth_sense_resume_view where resume_id = ?1", nativeQuery = true)
	Integer getDateDiffenceOfViewResume(String resumeId);
}
