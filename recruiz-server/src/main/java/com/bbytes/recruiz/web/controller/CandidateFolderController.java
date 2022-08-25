package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFolderLink;
import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.FolderDTO;
import com.bbytes.recruiz.rest.dto.models.FolderRequestDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserDTO;
import com.bbytes.recruiz.service.CandidateFolderService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Search Controller, It will communicate with elastic search service.
 * 
 * @author akshay
 *
 */
@RestController
public class CandidateFolderController {

	private static final Logger logger = LoggerFactory.getLogger(CandidateFolderController.class);

	@Autowired
	private CandidateFolderService candidateFolderService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	/**
	 * Create new folder in DB
	 * 
	 * @param folderDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder", method = RequestMethod.POST)
	public RestResponse createCandidateFolder(@RequestBody FolderDTO folderDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.CreateCandidateFolder.name());

		try {
			Folder folderFromDB = candidateFolderService.addCandidateFolder(folderDTO.getFolderName(), folderDTO.getFolderDesc());
			FolderDTO folderDTOFromDB = dataModelToDTOConversionService.convertFolder(folderFromDB);
			return new RestResponse(RestResponse.SUCCESS, folderDTOFromDB);
		} catch (EntityExistsException eee) {
			return new RestResponse(RestResponse.FAILED, eee.getMessage(), "candidate_folder_name_already_exists");
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, "Candidate folder creation failed", "candidate_folder_creation_failed");
		}
	}

	/**
	 * Update folder in db
	 * 
	 * @param folderDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder", method = RequestMethod.PUT)
	public RestResponse updadeCandidateFolder(@RequestBody FolderDTO folderDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UpdateCandidateFolder.name());

		try {
			Folder folder = candidateFolderService.updateCandidateFolder(folderDTO);
			FolderDTO folderDTOUpdated = dataModelToDTOConversionService.convertFolder(folder);
			return new RestResponse(RestResponse.SUCCESS, folderDTOUpdated);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "candidate_folder_update_failed");
		}
	}

	/**
	 * Add candidate to folder , it creates the link
	 * 
	 * @param folderRequestDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/add", method = RequestMethod.POST)
	public RestResponse addCandidatesToFolder(@RequestBody(required = true) FolderRequestDTO folderRequestDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddCandidateToFolder.name());

		try {
			candidateFolderService.addCandidatesToFolder(folderRequestDTO.getCandidateIds(), folderRequestDTO.getFolderName());
		} catch (RecruizException e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "candidates_add_to_folder_failed");
		}

		return new RestResponse(RestResponse.SUCCESS, "Candidates added to folder");
	}

	/**
	 * Remove the candidate to folder link
	 * 
	 * @param folderRequestDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/remove", method = RequestMethod.POST)
	public RestResponse removeCandidateFromFolder(@RequestBody(required = true) FolderRequestDTO folderRequestDTO) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.RemoveCandidateFromFolder.name());

		Collection<CandidateFolderLink> candidateFolderLinks = candidateFolderService
				.findByCandidatesAndFolder(folderRequestDTO.getCandidateIds(), folderRequestDTO.getFolderName());

		if (candidateFolderLinks == null)
			return new RestResponse(RestResponse.FAILED, "Candidates not found in folder", "candidates_remove_from_folder_failed");

		candidateFolderService.delete(candidateFolderLinks);

		return new RestResponse(RestResponse.SUCCESS, "Candidates removed from folder");
	}

	/**
	 * Add candidates to folders
	 * 
	 * @param folderRequestDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/folders/add", method = RequestMethod.POST)
	public RestResponse addCandidatesToFolders(@RequestBody(required = true) FolderRequestDTO folderRequestDTO) {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.AddCandidateToFolder.name());
		try {
			if (folderRequestDTO.getCandidateId() == null) {
				candidateFolderService.addCandidatesToFolders(folderRequestDTO.getCandidateIds(), folderRequestDTO.getFolderNames());
			} else {
				candidateFolderService.addCandidateToFolders(folderRequestDTO.getCandidateId(), folderRequestDTO.getFolderNames());
			}
		} catch (RecruizException e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "candidate_add_to_folders_failed");
		}

		return new RestResponse(RestResponse.SUCCESS, "Candidate added to folders");
	}

	/**
	 * Add user to user share lis for the given folder
	 * 
	 * @param folderRequestDTO
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/user/share", method = RequestMethod.POST)
	public RestResponse shareFolderWithUsers(@RequestBody(required = true) FolderRequestDTO folderRequestDTO) {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ShareFolderWithUser.name());*/

		try {
			candidateFolderService.addUsersToFolder(folderRequestDTO.getUserEmails(), folderRequestDTO.getFolderName());
		} catch (RecruizException e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "candidate_folder_share_failed");
		}

		return new RestResponse(RestResponse.SUCCESS, "Candidate folder shared with users successfully");
	}

	@RequestMapping(value = "/api/v1/folder/candidate/user/unshare", method = RequestMethod.POST)
	public RestResponse unshareFolderWithUsers(@RequestBody(required = true) FolderRequestDTO folderRequestDTO) {
		// making entry to usage stat table
/*		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.UnshareFolderWithUser.name());*/
		try {
			candidateFolderService.removeUsersFromFolder(folderRequestDTO.getUserEmails(), folderRequestDTO.getFolderName());
		} catch (RecruizException e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, e.getMessage(), "candidate_folder_unshare_failed");
		}

		return new RestResponse(RestResponse.SUCCESS, "Candidate folder unshared with users successfully");
	}

	/**
	 * Delete candidate folder api, it removes the candidate to folder link and
	 * removes the folder for DB
	 * 
	 * @param folderName
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate", method = RequestMethod.DELETE)
	public RestResponse deleteCandidateFolder(@RequestParam(value = "folderName", required = true) String folderName) {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.DeleteCandidateFolder.name());
		try {
			candidateFolderService.deleteByCandidateFolderName(folderName);
			return new RestResponse(RestResponse.SUCCESS, "Candidate folder deleted");
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, "Candidate folder deletion failed", "candidate_folder_delete_failed");
		}
	}

	/**
	 * Check if folder name exist
	 * 
	 * @param folderName
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/{folderName}/exist", method = RequestMethod.GET)
	public RestResponse folderNameExist(@PathVariable("folderName") String folderName) {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.CheckFolderExists.name());

		Boolean exist = candidateFolderService.isfolderExistByName(folderName);
		return new RestResponse(RestResponse.SUCCESS, exist);
	}

	/**
	 * Get the list of candidate folder for current logged in user
	 * 
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/folderlist", method = RequestMethod.GET)
	public RestResponse listCandidateFoldersForCurrentUser() {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ListCandidateFolderForCurrentUser.name());

		Set<Folder> folders = candidateFolderService.findCandidateFolderForCurrentUser();
		Collection<FolderDTO> folderDTOs = dataModelToDTOConversionService.convertFolders(folders);
		return new RestResponse(RestResponse.SUCCESS, folderDTOs);

	}

	/**
	 * Get the folder candidate list
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/candidatelist", method = RequestMethod.GET)
	public RestResponse listCandidateInFolder(@RequestParam(value = "folderName", required = true) String folderName)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetFolderCandidate.name());

		Collection<CandidateFolderLink> candidateFolderLinks = candidateFolderService.getCandidateListForCurrentUserAndFolder(folderName);

		for (CandidateFolderLink candidateFolderLink : candidateFolderLinks) {
			if (null != candidateFolderLink.getCandidate()) {
				candidateService.attachCurrentPosition(candidateFolderLink.getCandidate());
			}
		}

		return new RestResponse(RestResponse.SUCCESS, candidateFolderLinks);
	}

	/**
	 * Get the shared user list for the folder
	 * 
	 * @param folderName
	 * @return
	 */
	@RequestMapping(value = "/api/v1/folder/candidate/userlist", method = RequestMethod.GET)
	public RestResponse listUsersInFolder(@RequestParam(value = "folderName", required = true) String folderName) {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.ListUsersInFolder.name());

		Collection<User> folderUsers;
		try {
			folderUsers = candidateFolderService.getSharedUsersInFolder(folderName);
			List<UserDTO> userDTOs = dataModelToDTOConversionService.convertUsers(folderUsers);
			return new RestResponse(RestResponse.SUCCESS, userDTOs);
		} catch (RecruizException e) {
			logger.error(e.getMessage());
			return new RestResponse(RestResponse.FAILED, "Error while fetching user list for folder", "user_folder_fetch_exception");
		}

	}

	/**
	 * Get the list of candidate to be added to the folder , we remove the
	 * folder that is already added to thsi list
	 * 
	 * @param folderName
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/folder/list/candidate", method = RequestMethod.GET)
	public RestResponse getAllCandidate(@RequestParam(value = "folderName", required = true) String folderName,
			@RequestParam(value = "searchText", required = false) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllFolderCandidate.name());

		try {
			Collection<Candidate> existingCandidates = candidateFolderService.getExistingCandidatesInFolder(folderName);
			List<Long> candidateCids = new ArrayList<>();
			for (Candidate candidate : existingCandidates) {
				candidateCids.add(candidate.getCid());
			}
			Page<Candidate> candidateDbList;
			if (searchText != null && !searchText.isEmpty()) {
				candidateDbList = candidateService.searchCandidateListForCurrentUser(searchText, pageNo, sortField, sortOrder,candidateCids);
			} else {
				candidateDbList = candidateService.getCandidateListForCurrentUserAndNotInList(pageNo, sortField, sortOrder, candidateCids);
			}

			RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, candidateDbList);
			return candidateAddResponse;

		} catch (RecruizPermissionDeniedException e) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

	}

	/**
	 * Get the user list that has to be added to the folder for sharing purpose
	 * 
	 * @param folderName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/folder/list/user", method = RequestMethod.GET)
	public RestResponse getAllUsers(@RequestParam(value = "folderName", required = true) String folderName) throws Exception {
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetAllFolderUser.name());
		try {
			Collection<User> existingUsers = candidateFolderService.getUserForFolder(folderName);
			List<User> usersFromDB = userService.getAllActiveAppAndNonPendingUsers();

			if (existingUsers != null)
				usersFromDB.removeAll(existingUsers);

			List<UserDTO> userDTOs = dataModelToDTOConversionService.convertUsers(usersFromDB);
			return new RestResponse(RestResponse.SUCCESS, userDTOs);

		} catch (Exception e) {
			logger.error(e.getMessage());
			return new RestResponse(RestResponse.FAILED, "Error while fetching user from db for share", ErrorHandler.USER_NOT_FOUND);
		}

	}

}
