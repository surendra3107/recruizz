package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.PositionFile;

public interface PositionFileRepository extends JpaRepository<PositionFile, Long>{

	@Query(value = "select * from position_file where position_id = ?1", nativeQuery = true)
	List<PositionFile> getPositionFilesByPositionId(String pid);

	@Query(value = "select * from position_file where storageMode = 'aws'", nativeQuery = true)
	List<PositionFile> getPositionFileByStorageModeAWS();

}
