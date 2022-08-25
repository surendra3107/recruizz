package com.bbytes.recruiz.service;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FolderType;
import com.bbytes.recruiz.repository.FolderRepository;

@Service
public class FolderService extends AbstractService<Folder, Long> {

	private FolderRepository folderRepository;

	@Autowired
	public FolderService(FolderRepository folderRepository) {
		super(folderRepository);
		this.folderRepository = folderRepository;
	}

	@Transactional(readOnly = true)
	public Folder findByDisplayName(String folderDisplayName) {
		return folderRepository.findByDisplayName(folderDisplayName);
	}

	@Transactional(readOnly = true)
	public Set<Folder> findDistinctByDisplayNameIn(Collection<String> folderDisplayNames) {
		return folderRepository.findDistinctByDisplayNameIn(folderDisplayNames);
	}

	@Transactional(readOnly = true)
	public Set<Folder> findByOwner(String ownerEmail) {
		return folderRepository.findByOwner(ownerEmail);
	}

	@Transactional(readOnly = true)
	public Set<Folder> findByFolderType(FolderType folderType) {
		return folderRepository.findByFolderType(folderType);
	}

	@Transactional(readOnly = true)
	public Set<Folder> findBySharedUserListIn(Collection<User> users) {
		return folderRepository.findBySharedUserListIn(users);
	}

	@Transactional(readOnly = true)
	public Set<Folder> findByFolderTypeAndSharedUserListIn(FolderType folderType, Collection<User> users) {
		return folderRepository.findByFolderTypeAndSharedUserListIn(folderType, users);
	}

	@Transactional(readOnly = true)
	public boolean existsByFolderName(@Param("folderName") String folderName) {
		return folderRepository.existsByFolderName(folderName);
	}

}
