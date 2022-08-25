package com.bbytes.recruiz.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionFolderLink;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FolderType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.FolderRepository;
import com.bbytes.recruiz.repository.PositionFolderRepository;

@Service
public class PositionFolderService extends AbstractService<PositionFolderLink, Long> {

	private static final Logger logger = LoggerFactory.getLogger(PositionFolderService.class);

	private PositionFolderRepository positionFolderRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private PositionService positionService;

	@Autowired
	private UserService userService;

	@Autowired
	public PositionFolderService(PositionFolderRepository positionFolderRepository) {
		super(positionFolderRepository);
		this.positionFolderRepository = positionFolderRepository;
	}

	@Transactional
	public PositionFolderLink addPositionToFolder(Position position, String folderDisplayName) {
		return addPositionToFolder(position, folderDisplayName, true);
	}

	@Transactional
	public PositionFolderLink addPositionToFolder(String positionCode, String folderDisplayName, boolean createFolderIfMissing)
			throws RecruizException {
		Position position = positionService.getOneByPositionCode(positionCode);
		if (position == null)
			throw new RecruizException("Position missing with code : " + positionCode);

		return addPositionToFolder(position, folderDisplayName, createFolderIfMissing);
	}

	@Transactional
	public PositionFolderLink addPositionToFolder(Position position, String folderDisplayName, boolean createFolderIfMissing) {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			if (createFolderIfMissing) {
				folder = new Folder();
				folder.setDisplayName(folderDisplayName);
				folder.addUserToFolder(userService.getLoggedInUserObject());
				folder = folderRepository.save(folder);
				logger.info("Created missing candidate folder : " + folder.getDisplayName());
			} else {
				throw new EntityNotFoundException("Folder with display name " + folderDisplayName + " not found ");
			}
		}

		return addPositionToFolder(position, folder);
	}

	@Transactional
	public PositionFolderLink addPositionToFolder(Position position, Folder folder) {
		PositionFolderLink positionFolder = new PositionFolderLink();
		positionFolder.setAddedDate(new Date());
		positionFolder.setPosition(position);
		positionFolder.setFolder(folder);
		return this.positionFolderRepository.save(positionFolder);
	}

	@Transactional(readOnly = true)
	public List<PositionFolderLink> findByPositionOrderByAddedDateDesc(Position position) {
		return positionFolderRepository.findByPositionOrderByAddedDateDesc(position);
	}

	@Transactional(readOnly = true)
	public List<PositionFolderLink> findByFolderOrderByAddedDateDesc(String folderName) {
		Folder folder = folderRepository.findByDisplayName(folderName);
		return findByFolderOrderByAddedDateDesc(folder);
	}
	
	@Transactional(readOnly = true)
	public List<PositionFolderLink> findByFolderOrderByAddedDateDesc(Folder folder) {
		return positionFolderRepository.findByFolderOrderByAddedDateDesc(folder);
	}

	@Transactional(readOnly = true)
	public PositionFolderLink findByPositionAndFolder(Position position, Folder folder) {
		return positionFolderRepository.findByPositionAndFolder(position, folder);
	}

	@Transactional(readOnly = true)
	public PositionFolderLink findByPositionAndFolder(String positionCode, String folderName) {
		Position position = positionService.getOneByPositionCode(positionCode);
		Folder folder = folderRepository.findByDisplayName(folderName);
		return positionFolderRepository.findByPositionAndFolder(position, folder);
	}

	@Transactional(readOnly = true)
	public long countByFolder(Folder folder) {
		return positionFolderRepository.countByFolder(folder);
	}

	@Transactional(readOnly = true)
	public long countByPosition(Position position) {
		return positionFolderRepository.countByPosition(position);
	}

	@Transactional(readOnly = true)
	public Collection<Folder> findPositionFolderForCurrentUser() {
		Set<User> users = new HashSet<>();
		users.add(userService.getLoggedInUserObject());
		Collection<Folder> folders = folderRepository.findByFolderTypeAndSharedUserListIn(FolderType.POSITION_FOLDER, users);
		return folders;
	}

}
