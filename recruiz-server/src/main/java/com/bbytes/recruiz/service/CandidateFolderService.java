package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.FolderType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.repository.CandidateFolderRepository;
import com.bbytes.recruiz.repository.FolderRepository;
import com.bbytes.recruiz.rest.dto.models.FolderDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
public class CandidateFolderService extends AbstractService<CandidateFolderLink, Long> {

	private static final Logger logger = LoggerFactory.getLogger(CandidateFolderService.class);

	private CandidateFolderRepository candidateFolderRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private UserService userService;

	@Autowired
	public CandidateFolderService(CandidateFolderRepository candidateFolderRepository) {
		super(candidateFolderRepository);
		this.candidateFolderRepository = candidateFolderRepository;
	}

	@Transactional
	public CandidateFolderLink addCandidateToFolder(Candidate candidate, String folderDisplayName, String folderDesc)
			throws RecruizException {
		return addCandidateToFolder(candidate, folderDisplayName);
	}

	@Transactional
	public CandidateFolderLink addCandidateToFolder(Long candidateId, String folderDisplayName) throws RecruizException {
		Candidate candidate = candidateService.findOne(candidateId);
		if (candidate == null)
			throw new RecruizException("Candidate missing with Id : " + candidateId);

		return addCandidateToFolder(candidate, folderDisplayName);
	}

	@Transactional
	public Collection<CandidateFolderLink> addCandidatesToFolder(Collection<Long> candidateIds, String folderDisplayName)
			throws RecruizException {
		Set<Candidate> candidates = Sets.newHashSet(candidateService.findAll(candidateIds));
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			throw new EntityNotFoundException("Folder with display name " + folderDisplayName + " not found");
		}
		Collection<Candidate> existingCandidates = candidateFolderRepository.findCandidateByFolder(folder.getId());
		candidates.removeAll(existingCandidates);

		return addCandidatesToGivenFolder(Lists.newArrayList(candidates), folder);
	}

	@Transactional(readOnly = true)
	public Collection<Candidate> getExistingCandidatesInFolder(String folderDisplayName) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			throw new RecruizException("Folder with display name " + folderDisplayName + " not found");
		}
		Collection<Candidate> existingCandidates = candidateFolderRepository.findCandidateByFolder(folder.getId());

		return existingCandidates;
	}

	@Transactional(readOnly = true)
	public Collection<User> getSharedUsersInFolder(String folderDisplayName) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			throw new RecruizException("Folder with display name " + folderDisplayName + " not found");
		}
		String ownerEmail = folder.getOwner();
		Collection<User> sharedUsersWithoutOwner = new ArrayList<>();
		for (User user : folder.getSharedUserList()) {
			if (!user.getEmail().equalsIgnoreCase(ownerEmail))
				sharedUsersWithoutOwner.add(user);
		}

		return sharedUsersWithoutOwner;

	}

	@Transactional
	public Collection<CandidateFolderLink> addCandidateToFolders(Long candidateId, Collection<String> folderDisplayNames)
			throws RecruizException {
		Set<Folder> folders = folderRepository.findDistinctByDisplayNameIn(folderDisplayNames);
		Candidate candidate = candidateService.findOne(candidateId);
		if (candidate == null) {
			throw new RecruizException("Candidate with id " + candidate + " not found");
		}

		return addCandidateToGivenFolders(candidate, folders);
	}

	@Transactional
	public Collection<CandidateFolderLink> addCandidatesToFolders(Collection<Long> candidateIds, Collection<String> folderDisplayNames)
			throws RecruizException {
		Collection<CandidateFolderLink> result = new ArrayList<>();
		Set<Candidate> candidates = Sets.newHashSet(candidateService.findAll(candidateIds));
		Set<Folder> folders = folderRepository.findDistinctByDisplayNameIn(folderDisplayNames);
		if (folders == null || candidates == null) {
			throw new EntityNotFoundException("Folders or candidates missing");
		}

		for (Candidate candidate : candidates) {
			Collection<CandidateFolderLink> candidateFolderLinks = addCandidateToGivenFolders(candidate, folders);
			result.addAll(candidateFolderLinks);
		}

		return result;
	}

	@Transactional
	public CandidateFolderLink addCandidateToFolder(Candidate candidate, String folderDisplayName) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			throw new RecruizException("Folder with display name " + folderDisplayName + " not found");
		}

		return addCandidateToGivenFolder(candidate, folder);
	}

	@Transactional(readOnly = true)
	public Folder getByCandidateFolderName(String folderDisplayName) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			throw new RecruizException("Folder with display name " + folderDisplayName + " not found");
		}

		return folder;
	}

	@Transactional
	public void deleteByCandidateFolderName(String folderDisplayName) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);

		if (folder == null) {
			throw new RecruizException("Folder with display name " + folderDisplayName + " not found");
		}

		candidateFolderRepository.deleteByFolder(folder);
		folderRepository.delete(folder);
	}

	@Transactional
	public Folder addCandidateFolder(String folderDisplayName, String folderDesc) throws RecruizException {
		Folder folder = folderRepository.findByDisplayName(folderDisplayName);
		if (folder == null) {
			folder = new Folder();
			folder.setDisplayName(folderDisplayName);
			folder.setDesc(folderDesc);

			User owner = userService.getLoggedInUserObject();
			folder.setOwner(owner.getEmail());
			folder.addUserToFolder(owner);

			folder = folderRepository.save(folder);
			logger.info("Created missing candidate folder : " + folder.getDisplayName());
		} else {
			throw new EntityExistsException("Folder with name '" + folderDisplayName + "' exist");
		}

		return folder;
	}

	@Transactional
	public Folder updateCandidateFolder(FolderDTO folderDTO) throws RecruizException {
		if (folderDTO == null)
			return null;

		Folder folderFromDB = folderRepository.findOne(folderDTO.getId());
		if (folderFromDB != null) {
			folderFromDB.setDisplayName(folderDTO.getFolderName());
			folderFromDB.setDesc(folderDTO.getFolderDesc());
			folderFromDB.setFolderPublic(folderDTO.isFolderPublic());
			folderFromDB.setFolderType(FolderType.valueOf(folderDTO.getFolderType()));

			folderFromDB = folderRepository.save(folderFromDB);
			logger.info("Updated candidate folder : " + folderFromDB.getDisplayName());
		} else {
			throw new RecruizException("Folder with id " + folderDTO.getId() + "  not found");
		}

		return folderFromDB;
	}

	@Transactional
	public CandidateFolderLink addCandidateToGivenFolder(Candidate candidate, Folder folder) {
		List<Candidate> candidates = new ArrayList<>();
		candidates.add(candidate);
		Iterable<CandidateFolderLink> candidateFolderLinks = addCandidatesToGivenFolder(candidates, folder);
		if (candidateFolderLinks.iterator().hasNext()) {
			return candidateFolderLinks.iterator().next();
		}
		return null;
	}

	@Transactional
	public Collection<CandidateFolderLink> addCandidatesToGivenFolder(Collection<Candidate> candidates, Folder folder) {
		List<CandidateFolderLink> candidateFolderLinks = new ArrayList<>();
		for (Candidate candidate : candidates) {
			CandidateFolderLink candidateFolder = new CandidateFolderLink();
			candidateFolder.setAddedDate(new Date());
			candidateFolder.setCandidate(candidate);
			candidateFolder.setFolder(folder);
			candidateFolder.setAddedByUserEmail(userService.getLoggedInUserEmail());
			candidateFolder.setAddedDate(DateTime.now().toDate());
			candidateFolderLinks.add(candidateFolder);
		}

		return Lists.newArrayList(this.candidateFolderRepository.save(candidateFolderLinks));
	}

	@Transactional
	public Collection<CandidateFolderLink> addCandidateToGivenFolders(Candidate candidate, Collection<Folder> folders) {
		List<CandidateFolderLink> candidateFolderLinks = new ArrayList<>();
		for (Folder folder : folders) {
			if (!candidateFolderRepository.existsByCandidateAndFolder(candidate.getCid(), folder.getId())) {
				CandidateFolderLink candidateFolder = new CandidateFolderLink();
				candidateFolder.setAddedDate(new Date());
				candidateFolder.setCandidate(candidate);
				candidateFolder.setFolder(folder);
				candidateFolder.setAddedByUserEmail(userService.getLoggedInUserEmail());
				candidateFolder.setAddedDate(DateTime.now().toDate());
				candidateFolderLinks.add(candidateFolder);
			}

		}

		return Lists.newArrayList(this.candidateFolderRepository.save(candidateFolderLinks));
	}

	@Transactional
	public Folder addUsersToFolder(Collection<String> userEmails, String folderName) throws RecruizException {
		Collection<User> users = userService.getUsersByEmails(userEmails);
		Folder folder = getByCandidateFolderName(folderName);
		folder.addUsersToFolder(users);
		return folderRepository.save(folder);
	}

	@Transactional
	public Folder removeUsersFromFolder(Collection<String> userEmails, String folderName) throws RecruizException {
		Collection<User> usersToRemove = userService.getUsersByEmails(userEmails);
		Folder folder = getByCandidateFolderName(folderName);
		folder.getSharedUserList().removeAll(usersToRemove);
		return folderRepository.save(folder);
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> findByCandidateOrderByAddedDateDesc(Candidate candidate) {
		return this.candidateFolderRepository.findByCandidateOrderByAddedDateDesc(candidate);
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> findByFolderOrderByAddedDateDesc(String folderName) {
		Folder folder = folderRepository.findByDisplayName(folderName);
		return findByFolderOrderByAddedDateDesc(folder);
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> findByFolderAndOwner(String folderName, String ownerEmail) {
		return candidateFolderRepository.findByFolderAndOwner(folderName, ownerEmail);
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> getCandidateListForCurrentUserAndFolder(String folderName) throws RecruizException {
		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizPermissionDeniedException(ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Collection<CandidateFolderLink> candidateFolderLinks;

		// checking user has view all candidate permission
		if (checkUserPermission.isSuperAdmin() || checkUserPermission.hasViewAllCandidatesPermission()) {
			candidateFolderLinks = findByFolderOrderByAddedDateDesc(folderName);
		} else {
			String loggedInUserEmail = userService.getLoggedInUserEmail();
			candidateFolderLinks = findByFolderAndOwner(folderName, loggedInUserEmail);
		}

		return candidateFolderLinks;
	}

	@Transactional(readOnly = true)
	public Collection<User> getUserForFolder(String folderName) {
		Folder folder = folderRepository.findByDisplayName(folderName);
		return folder.getSharedUserList();
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> findByFolderOrderByAddedDateDesc(Folder folder) {
		return this.candidateFolderRepository.findByFolderOrderByAddedDateDesc(folder);
	}

	@Transactional(readOnly = true)
	public CandidateFolderLink findByCandidateAndFolder(Candidate candidate, Folder folder) {
		return this.candidateFolderRepository.findByCandidateAndFolder(candidate, folder);
	}

	@Transactional(readOnly = true)
	public Collection<CandidateFolderLink> findByCandidatesAndFolder(Collection<Long> candidateIds, String folderName) {
		Iterable<Candidate> candidates = candidateService.findAll(candidateIds);
		Folder folder = folderRepository.findByDisplayName(folderName);
		return this.candidateFolderRepository.findByCandidateInAndFolder(candidates, folder);
	}

	@Transactional(readOnly = true)
	public long countByFolder(Folder folder) {
		return this.candidateFolderRepository.countByFolder(folder);
	}

	@Transactional(readOnly = true)
	public long countByCandidate(Candidate candidate) {
		return this.candidateFolderRepository.countByCandidate(candidate);
	}

	@Transactional(readOnly = true)
	public boolean isfolderExistByName(String folderName) {
		return this.folderRepository.existsByFolderName(folderName.toLowerCase());
	}

	@Transactional(readOnly = true)
	public Set<Folder> findCandidateFolderForCurrentUser() {
		List<User> users = new ArrayList<>();
		users.add(userService.getLoggedInUserObject());
		Set<Folder> folders = folderRepository.findByFolderTypeAndSharedUserListIn(FolderType.CANDIDATE_FOLDER, users);
		// get count of candidates
		for (Folder folder : folders) {
			folder.getCandidateCount();
		}
		return folders;
	}

}
