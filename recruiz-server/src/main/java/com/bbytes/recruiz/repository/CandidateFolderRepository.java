package com.bbytes.recruiz.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.Folder;

public interface CandidateFolderRepository extends JpaRepository<CandidateFolderLink, Long> {

	Collection<CandidateFolderLink> findByCandidateOrderByAddedDateDesc(Candidate candidate);

	Collection<CandidateFolderLink> findByFolderOrderByAddedDateDesc(Folder folder);

	@Query("SELECT cfl.candidate FROM candidate_folder cfl WHERE cfl.folder.id = :folderId ")
	Collection<Candidate> findCandidateByFolder(@Param("folderId") Long folderId);

	void deleteByFolder(Folder folder);

	CandidateFolderLink findByCandidateAndFolder(Candidate candidate, Folder folder);

	@Query("SELECT cfl FROM candidate_folder cfl WHERE cfl.folder.displayName = :folderName AND cfl.candidate.owner = :ownerEmail")
	Collection<CandidateFolderLink> findByFolderAndOwner(@Param("folderName") String folderName,
			@Param("ownerEmail") String ownerEmail);

	@Query("SELECT CASE WHEN COUNT(cfl) > 0 THEN true ELSE false END FROM candidate_folder cfl  WHERE cfl.candidate.id = :candidateId AND cfl.folder.id = :folderId")
	boolean existsByCandidateAndFolder(@Param("candidateId") Long candidateId, @Param("folderId") Long folderId);

	Collection<CandidateFolderLink> findByCandidateInAndFolder(Iterable<Candidate> candidates, Folder folder);

	long countByFolder(Folder folder);

	long countByCandidate(Candidate candidate);

}
