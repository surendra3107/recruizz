package com.bbytes.recruiz.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectPosition;

public interface ProspectPositionRepository extends JpaRepository<ProspectPosition, Long>{

	List<ProspectPosition> findByProspect(Prospect prospect);
	
	List<ProspectPosition> findByClientName(String clientName);
	
	Page<ProspectPosition> findByClientName(String clientName ,Pageable pageable);
	
	Page<ProspectPosition> findByStatus(String status,Pageable pageable);
	
	Page<ProspectPosition> findByIsConvertedToClientAndStatus(boolean isConverted,String status,Pageable pageable);
	
	List<ProspectPosition> findByIsConvertedToClientAndStatus(boolean isConverted,String status);
	
	Page<ProspectPosition> findByClientNameAndStatus(String clientName,String status,Pageable pageable);
	
}
