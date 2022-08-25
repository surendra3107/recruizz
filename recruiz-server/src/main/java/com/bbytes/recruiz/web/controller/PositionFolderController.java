package com.bbytes.recruiz.web.controller;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.PositionFolderLink;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.PositionFolderService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Search Controller, It will communicate with elastic search service.
 * 
 * @author akshay
 *
 */
@RestController
public class PositionFolderController {

    private static final Logger logger = LoggerFactory.getLogger(PositionFolderController.class);

    @Autowired
    private PositionFolderService positionFolderService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/api/v1/folder/position/add", method = RequestMethod.POST)
    public RestResponse addPostionToFolder(@RequestParam(value = "folderName", required = true) String folderName,
	    @RequestParam(value = "positionCode", required = true) String positionCode) {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddPositionToFolder.name());

	try {
	    PositionFolderLink positionFolderLink = positionFolderService.findByPositionAndFolder(positionCode,
		    folderName);
	    if (positionFolderLink == null) {
		positionFolderService.addPositionToFolder(positionCode, folderName, true);
	    }

	} catch (RecruizException e) {
	    logger.warn(e.getMessage(), e);
	    return new RestResponse(RestResponse.FAILED, e.getMessage(), "position_add_to_folder_failed");
	}

	return new RestResponse(RestResponse.SUCCESS, "Position added to folder");

    }

    @RequestMapping(value = "/api/v1/folder/position/remove", method = RequestMethod.DELETE)
    public RestResponse removePostionFromFolder(@RequestParam(value = "folderName", required = true) String folderName,
	    @RequestParam(value = "positionCode", required = true) String positionCode) {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.RemovePositionFromFolder.name());

	PositionFolderLink positionFolderLink = positionFolderService.findByPositionAndFolder(positionCode, folderName);

	if (positionFolderLink == null)
	    return new RestResponse(RestResponse.FAILED, "Position not found in folder",
		    "position_remove_from_folder_failed");

	positionFolderService.delete(positionFolderLink);

	return new RestResponse(RestResponse.SUCCESS, "Position removed from folder");

    }

    @RequestMapping(value = "/api/v1/folder/position/folderlist", method = RequestMethod.GET)
    public RestResponse listPositionFoldersForCurrentUser() {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetUserPositionFolders.name());*/

	Collection<Folder> folders = positionFolderService.findPositionFolderForCurrentUser();
	return new RestResponse(RestResponse.SUCCESS, folders);
    }

    @RequestMapping(value = "/api/v1/folder/position/positionlist", method = RequestMethod.GET)
    public RestResponse listPositionInFolder(@RequestParam(value = "folderName", required = true) String folderName) {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllPositionFolder.name());

	List<PositionFolderLink> positionFolderLinks = positionFolderService
		.findByFolderOrderByAddedDateDesc(folderName);
	return new RestResponse(RestResponse.SUCCESS, positionFolderLinks);
    }

}
