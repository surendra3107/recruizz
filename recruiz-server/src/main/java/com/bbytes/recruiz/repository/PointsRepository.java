package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.Points;
import com.bbytes.recruiz.domain.RoundCandidate;

//@JaversSpringDataAuditable
public interface PointsRepository extends JpaRepository<Points, Long> {

	@Query("select AVG(points) from points p where p.positionCode = :positionCode AND p.roundCandidate = :roundCandidate")
	double findAvgPointByCandidateAndPosition(@Param("positionCode") String positionCode,
			@Param("roundCandidate") RoundCandidate roundCandidate);

}
