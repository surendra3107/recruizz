package com.bbytes.recruiz.repository;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FolderType;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	Folder findByDisplayName(String folderDisplayName);
	
	Set<Folder> findDistinctByDisplayNameIn(Collection<String> folderDisplayNames);

	Set<Folder> findByFolderType(FolderType folderType);
	
	Set<Folder> findBySharedUserListIn(Collection<User> users);
	
	Set<Folder> findByOwner(String ownerEmail);

	Set<Folder> findByFolderTypeAndSharedUserListIn(FolderType folderType,Collection<User> users);
	
	@Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM folder f WHERE f.displayName = :folderName")
	boolean existsByFolderName(@Param("folderName") String folderName);
}
