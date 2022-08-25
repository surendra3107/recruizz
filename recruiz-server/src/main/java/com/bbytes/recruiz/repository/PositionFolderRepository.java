package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionFolderLink;

public interface PositionFolderRepository extends JpaRepository<PositionFolderLink, Long> {

	List<PositionFolderLink> findByPositionOrderByAddedDateDesc(Position position);

	List<PositionFolderLink> findByFolderOrderByAddedDateDesc(Folder folder);

	PositionFolderLink findByPositionAndFolder(Position position, Folder folder);

	long countByFolder(Folder folder);
	
	long countByPosition(Position position);

}
