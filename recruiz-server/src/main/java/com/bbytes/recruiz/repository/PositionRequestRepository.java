package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.PositionRequest;

//@JaversSpringDataAuditable
public interface PositionRequestRepository extends JpaRepository<PositionRequest, Long> {

	List<PositionRequest> findByStatus(String status);

	List<PositionRequest> findByRequestedByEmail(String email);

	List<PositionRequest> findByIdInOrderById(List<Long> ids);

	List<PositionRequest> findByClientNameAndIdInOrderById(String clientName, List<Long> ids);

	List<PositionRequest> findByIdIn(List<Long> ids, Sort sort);

	List<PositionRequest> findByClientNameAndIdIn(String clientName, List<Long> ids, Sort sort);

	Page<PositionRequest> findByStatus(String status, Pageable pageable);

	Page<PositionRequest> findByClientName(String clientName, Pageable pageable);
	
	Page<PositionRequest> findByClientNameAndStatusIn(String clientName,Set<String> statusIn, Pageable pageable);
	
	Page<PositionRequest> findByStatusIn(Set<String> statusIn, Pageable pageable);

	Page<PositionRequest> findByStatusAndClientName(String status, String clientName, Pageable pageable);

	Page<PositionRequest> findByRequestedByEmail(String requesterEmail, Pageable pageable);
	
	Page<PositionRequest> findByRequestedByEmailAndStatusIn(String requesterEmail,Set<String> statusIn, Pageable pageable);

	PositionRequest findByPositionCodeAndRequestedByEmail(String positionCode, String deptHeadEmail);

	PositionRequest findByPositionId(long positionId);

}
