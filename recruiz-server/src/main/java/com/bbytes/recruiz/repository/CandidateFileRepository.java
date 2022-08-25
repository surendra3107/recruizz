package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.CandidateFile;

//@JaversSpringDataAuditable
public interface CandidateFileRepository extends JpaRepository<CandidateFile, Long> {

	CandidateFile findOneByFilePath(String path);

	List<CandidateFile> findByFilePath(String filePath);

	List<CandidateFile> findByCandidateId(String candidateId);

	List<CandidateFile> findByFileTypeAndCandidateId(String filetype, String candidateId);

	List<CandidateFile> findByCandidateIdAndFilePathStartsWith(String candidateId, String filePathPrefix);
	
	List<CandidateFile> findByCandidateIdAndFileName(String cid,String fileName);

	@Query(value = "select * from candidate_file where storageMode = 'aws'", nativeQuery = true)
	List<CandidateFile> getCandidateFileByStorageModeAWS();
}
