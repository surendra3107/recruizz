package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.EmployeeFile;
import com.bbytes.recruiz.domain.OnBoardingDetails;
import com.bbytes.recruiz.domain.OnBoardingDetailsAdmin;
import com.bbytes.recruiz.domain.OnBoardingDetailsComments;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.domain.OnBoardingTemplate;
import com.bbytes.recruiz.enums.OnBoardingCategory;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.EmployeeService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.OnBoardingCategoryService;
import com.bbytes.recruiz.service.OnBoardingCommentsService;
import com.bbytes.recruiz.service.OnBoardingDetailsAdminService;
import com.bbytes.recruiz.service.OnBoardingDetailsService;
import com.bbytes.recruiz.service.OnBoardingTemplateService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Client Controller
 *
 */
@RestController
public class OnBoardingController {

    private static final Logger logger = LoggerFactory.getLogger(OnBoardingController.class);

    @Autowired
    private OnBoardingCategoryService subCategoryService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private OnBoardingDetailsService onBoardingDetailsService;

    @Autowired
    private OnBoardingDetailsAdminService adminOnBoardingSerivice;

    @Autowired
    private UserService userService;

    @Autowired
    private OnBoardingCommentsService commentService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private UploadFileService uploadFileService;
    
    @Autowired
    private FileService fileService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;
    
    
    @Autowired
    private OnBoardingTemplateService onBoardingTemplateService;

    // get all sub categories
    @RequestMapping(value = "/api/v1/onboarding/sub/category/all", method = RequestMethod.GET)
    public RestResponse getAllSubCategory() throws RecruizException {

	LinkedHashMap<String, Object> subCategoryMap = new LinkedHashMap<>();
	subCategoryMap.put(OnBoardingCategory.BeforeJoining.name(),
		subCategoryService.getSubCategoryByCategory(OnBoardingCategory.BeforeJoining.name()));
	subCategoryMap.put(OnBoardingCategory.AfterJoining.name(),
		subCategoryService.getSubCategoryByCategory(OnBoardingCategory.AfterJoining.name()));

	return new RestResponse(RestResponse.SUCCESS, subCategoryMap);
    }

    // to add or update a sub category
    @RequestMapping(value = "/api/v1/onboarding/sub/category/add", method = RequestMethod.POST)
    public RestResponse addUpdateSubCategory(@RequestBody List<OnBoardingSubCategory> subCategories)
	    throws RecruizException {
	
/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddSubCategory.name());*/

	if (null != subCategories && !subCategories.isEmpty()) {
	    subCategories = subCategoryService.manageSubCategory(subCategories);
	    return new RestResponse(RestResponse.SUCCESS, subCategories);
	}

	return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_CATEGORY_SENT, ErrorHandler.NO_DATA_FOUND);
    }

    // to delete a sub category
    @RequestMapping(value = "/api/v1/onboarding/sub/category/delete/{catId}", method = RequestMethod.DELETE)
    public RestResponse deleteSubCategory(@PathVariable Long catId) throws RecruizException {

/*	  // making entry to usage stat table
			tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteSubCategory.name());*/
		    
	
	RestResponse response = null;
	try {
	    subCategoryService.delete(catId);
	    response = new RestResponse(true, SuccessHandler.SUB_CATEGORY_DELETED);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETING_SUB_CATEGORY_FAILED,
		    ErrorHandler.DELETE_FAILED);
	}
	return response;
    }

    // to add multiple on board details to candidate
    @RequestMapping(value = "/api/v1/employee/onboarding/{eid}", method = RequestMethod.POST)
    public RestResponse addUpdateOnBoardDetails(@RequestBody List<OnBoardingDetails> onBoardingDetails,
	    @PathVariable Long eid) throws RecruizException {

	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateOnBoardingDetails.name());
	
	if (null != onBoardingDetails && !onBoardingDetails.isEmpty()) {
	    Employee emp = employeeService.findOne(eid);
	    for (OnBoardingDetails onBoardingDetail : onBoardingDetails) {
		try {
		    onBoardingDetail.setEid(emp);
		    onBoardingDetail.setOwner(userService.getLoggedInUserEmail());
		    onBoardingDetail.setCompletedStatus(false);
		    onBoardingDetail = onBoardingDetailsService.addOnBoardingActivity(onBoardingDetail);
		} catch (Exception ex) {
		    logger.error(ex.getMessage(), ex);
		}
	    }
	    return new RestResponse(RestResponse.SUCCESS, onBoardingDetails);
	} else {
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ON_BOARD_DETAILS_SENT,
		    ErrorHandler.NO_DATA_FOUND);
	}
    }

    // to delete a onboardDetails
    @RequestMapping(value = "/api/v1/onboarding/details/delete/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteOnBoardDetails(@PathVariable Long id) throws RecruizException {
	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteSubCategory.name());
	
	RestResponse response = null;
	try {
	    onBoardingDetailsService.delete(id);
	    response = new RestResponse(true, SuccessHandler.DELETE_SUCCESS);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETING_ON_BOARD_DETAILS_FAILED,
		    ErrorHandler.DELETE_FAILED);
	}
	return response;
    }

    // to change status of onboard details
    @RequestMapping(value = "/api/v1/onboarding/details/status/{id}/{status}", method = RequestMethod.PUT)
    public RestResponse changeOnBoardDetailsStatus(@PathVariable Long id, @PathVariable Boolean status)
	    throws RecruizException {
	
/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteSubCategory.name());*/
	
	RestResponse response = null;
	try {
	    onBoardingDetailsService.changeCompletedStatus(id, status);
	    response = new RestResponse(true, SuccessHandler.STATUS_CHANGE_SUCCESS);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.STATUS_CHANGE_ON_BOARD_DETAILS_FAILED,
		    ErrorHandler.REASON_UPDATE_FAILED);
	}
	return response;
    }

    // add update admin onboarding template
    @RequestMapping(value = "/api/v1/admin/onboarding", method = RequestMethod.POST)
    public RestResponse addUpdateAdminOnBoardDetails(@RequestBody List<OnBoardingDetailsAdmin> onBoardingDetailsAdmin,
	    @RequestParam(value = "templateName", required = false) String templateName) throws RecruizException {

/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateAdminOnBoardDetails.name());*/
	
	if (null != onBoardingDetailsAdmin && !onBoardingDetailsAdmin.isEmpty()) {
	    for (OnBoardingDetailsAdmin onBoardingDetail : onBoardingDetailsAdmin) {
		try {
		    OnBoardingSubCategory existingSubCategory = subCategoryService
			    .getSubCategoryByCategoryAndSubCategoryName(onBoardingDetail.getOnboardCategory(),
				    onBoardingDetail.getSubCategoryName());
		    if (null == existingSubCategory) {
			existingSubCategory = new OnBoardingSubCategory();
			existingSubCategory.setOnboardCategory(onBoardingDetail.getOnboardCategory());
			existingSubCategory.setSubCategoryName(onBoardingDetail.getSubCategoryName());
			existingSubCategory.setCompositeKey(System.currentTimeMillis() + "");
			subCategoryService.save(existingSubCategory);
		    }
		    onBoardingDetail.setOwner(userService.getLoggedInUserEmail());
		    onBoardingDetail = adminOnBoardingSerivice.addOnBoardingActivity(onBoardingDetail);
		  
		    // adding to template if template name is passed
		    if (onBoardingDetail.getId() > 0 && null != templateName && !templateName.trim().isEmpty()) {
			try {
			    List<Long> taskIds = new ArrayList<>();
			    taskIds.add(onBoardingDetail.getId());
			    OnBoardingTemplate template = onBoardingTemplateService.getTemplateByName(templateName);
			    if (null != template) {
				onBoardingTemplateService.editTemplate(templateName, taskIds, null, template.getId());
			    }
			} catch (Exception ex) {
			    logger.warn(ex.getMessage(), ex);
			}
		    }

		    
		} catch (Exception ex) {
		    logger.error(ex.getMessage(), ex);
		}
	    }
	    return new RestResponse(RestResponse.SUCCESS, onBoardingDetailsAdmin);
	} else {
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_ON_BOARD_DETAILS_SENT,
		    ErrorHandler.NO_DATA_FOUND);
	}
    }

    // to delete a onboardDetails
    @RequestMapping(value = "/api/v1/onboarding/admin/details/delete/{id}", method = RequestMethod.DELETE)
    public RestResponse deletedminOnBoardDetails(@PathVariable Long id) throws RecruizException {
	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateAdminOnBoardDetails.name());
	
	
	RestResponse response = null;
	try {
	    OnBoardingDetailsAdmin task = adminOnBoardingSerivice.findOne(id);
	    List<OnBoardingTemplate> templates =  onBoardingTemplateService.findAll();
	    if(null != templates && !templates.isEmpty()) {
		for (OnBoardingTemplate onBoardingTemplate : templates) {
		    if(onBoardingTemplate.getTasks().contains(task)) {
			onBoardingTemplate.getTasks().remove(task);
			onBoardingTemplateService.save(onBoardingTemplate);
		    }
		}
	    }
	    adminOnBoardingSerivice.delete(task);
	    response = new RestResponse(true, SuccessHandler.DELETE_SUCCESS);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETING_ON_BOARD_DETAILS_FAILED,
		    ErrorHandler.DELETE_FAILED);
	}
	return response;
    }

    // add a comment on a activity
    @RequestMapping(value = "/api/v1/onboarding/{onid}/comment", method = RequestMethod.POST)
    public RestResponse addCommentOnActivity(@PathVariable Long onid, @RequestBody OnBoardingDetailsComments comment)
	    throws RecruizException {
	
/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateAdminOnBoardDetails.name());*/
	
	
	RestResponse response = null;
	try {
	    OnBoardingDetails activity = onBoardingDetailsService.findOne(onid);
	    comment.setCommentedBy(userService.getLoggedInUserEmail());
	    comment.setOnBoardingDetails(activity);
	    activity.getComments().add(comment);
	    commentService.save(comment);
	    onBoardingDetailsService.save(activity);

	    response = new RestResponse(true, activity);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.ADDING_COMMENT_FAILED,
		    ErrorHandler.ADDING_COMMENT_ERROR);
	}
	return response;
    }

    // edit a comment on a activity
    @RequestMapping(value = "/api/v1/onboarding/comment/edit/{cmntID}", method = RequestMethod.PUT)
    public RestResponse editComment(@PathVariable Long cmntID, @RequestParam String comment) throws RecruizException {
	
/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateAdminOnBoardDetails.name());*/
	
	
	RestResponse response = null;
	try {
	    OnBoardingDetailsComments commentDetails = commentService.findOne(cmntID);
	    if (!commentDetails.getCommentedBy().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
		return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
	    }
	    commentDetails.setComment(comment);
	    commentService.save(commentDetails);

	    response = new RestResponse(true, commentDetails);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.EDITING_COMMENT_FAILED,
		    ErrorHandler.EDITING_COMMENT_ERROR);
	}
	return response;
    }

    // add a comment on a activity
    @RequestMapping(value = "/api/v1/onboarding/comment/delete/{cmntID}", method = RequestMethod.DELETE)
    public RestResponse deleteComment(@PathVariable Long cmntID) throws RecruizException {

/*	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddUpdateAdminOnBoardDetails.name());*/
	
	
	RestResponse response = null;
	try {
	    OnBoardingDetailsComments commentDetails = commentService.findOne(cmntID);
	    if (!commentDetails.getCommentedBy().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
		return new RestResponse(false, ErrorHandler.NOT_ALLOWED, ErrorHandler.NOT_PERMITTED);
	    }
	    OnBoardingDetails onBoardingDetails = commentDetails.getOnBoardingDetails();
	    onBoardingDetails.getComments().remove(commentDetails);
	    onBoardingDetailsService.save(onBoardingDetails);
	    commentDetails.setOnBoardingDetails(null);
	    commentService.save(commentDetails);
	//    commentService.delete(commentDetails);
	    response = new RestResponse(true, SuccessHandler.DELETE_SUCCESS);

	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETING_COMMENT_FAILED,
		    ErrorHandler.DELETING_COMMENT_ERROR);
	}
	return response;
    }

    // to get employee on boarding activity
    @RequestMapping(value = "/api/v1/onboarding/activity/list/{eid}/{state}", method = RequestMethod.GET)
    public RestResponse getAllTaskForEmployee(@PathVariable Long eid, @PathVariable String state)
	    throws RecruizException {
	RestResponse response = null;
	try {
	    Map<String, Object> listMap = onBoardingDetailsService.getGroupedMapOfOnBoardingActivity(eid, state);
	    response = new RestResponse(true, listMap);
	} catch (Exception ex) {
	    response = new RestResponse(false, ErrorHandler.FETCHING_ACTIVITY_FAILED,
		    ErrorHandler.FAILED_TO_FETCH_CATEGORY);
	}
	return response;
    }

    // get all onboarding activity for admin
    @RequestMapping(value = "/api/v1/admin/onboarding/activity/list", method = RequestMethod.GET)
    public RestResponse getEmployeeTaskToAssign() throws RecruizException {
	RestResponse response = null;
	try {
	    LinkedHashMap<String, Object> listMap = adminOnBoardingSerivice.getGroupedAdminOnBoardingDetails();
	    response = new RestResponse(true, listMap);
	} catch (Exception ex) {
	    response = new RestResponse(false, ErrorHandler.FETCHING_ACTIVITY_FAILED,
		    ErrorHandler.FAILED_TO_FETCH_CATEGORY);
	}

	return response;
    }

    // to get all on boarding templates
    @RequestMapping(value = "/api/v1/onboarding/templates/all", method = RequestMethod.GET)
    public RestResponse getAllTemplates() throws Exception {

	Map<String, Object> data = onBoardingTemplateService.getGroupedTemplateOnBoardingDetails();
	RestResponse response = new RestResponse(true, data);
	return response;
    }

    // to add on boarding templates
    @RequestMapping(value = "/api/v1/onboarding/templates/add", method = RequestMethod.POST)
    public RestResponse addTemplates(@RequestParam String templateName, @RequestParam List<Long> taskIds)
	    throws Exception {
	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddOnBoardingTemplate.name());
	
	
	RestResponse response = null;
	try {
	    if(templateName.equalsIgnoreCase("Master Template")) {
		return new RestResponse(false, ErrorHandler.FAILED_TO_ADD_TEMPLATE,
			    ErrorHandler.FAILED_ADDING_TEMPLATES);
	    }
	    OnBoardingTemplate existingTemplate = onBoardingTemplateService.getTemplateByName(templateName);
	    if (null != existingTemplate) {
		return new RestResponse(false, ErrorHandler.TEMPLATE_EXISTS_WITH_NAME, ErrorHandler.TEMPLATE_EXISTS);
	    }

	    onBoardingTemplateService.addTemplate(templateName, taskIds);
	    response = new RestResponse(true, SuccessHandler.TEMPLATE_ADDED_SUCCESSFULLY);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	    response = new RestResponse(false, ErrorHandler.FAILED_TO_ADD_TEMPLATE,
		    ErrorHandler.FAILED_ADDING_TEMPLATES);
	}
	return response;
    }

    // to add on boarding templates
    @RequestMapping(value = "/api/v1/onboarding/templates/edit/{tid}", method = RequestMethod.POST)
    public RestResponse editTemplates(@PathVariable Long tid, @RequestParam String templateName,
	    @RequestParam(value = "taskIds", required = false) List<Long> taskIds,
	    @RequestParam(value = "removedTaskIds", required = false) List<Long> removedTaskIds) throws Exception {
	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.EditOnBoardingTemplate.name());
	
	
	RestResponse response = null;
	try {
	    onBoardingTemplateService.editTemplate(templateName, taskIds, removedTaskIds, tid);
	    response = new RestResponse(true, SuccessHandler.TEMPLATE_EDITED_SUCCESSFULLY);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	    response = new RestResponse(false, ErrorHandler.FAILED_TO_EDIT_TEMPLATE,
		    ErrorHandler.FAILED_EDITING_TEMPLATES);
	}
	return response;
    }

    // to delete template
    @RequestMapping(value = "/api/v1/onboarding/templates/delete", method = RequestMethod.DELETE)
    public RestResponse deleteTemplates(@RequestParam String templateName) throws Exception {
	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteOnBoardingTemplate.name());
	
	
	RestResponse response = null;
	try {
	    OnBoardingTemplate template = onBoardingTemplateService.getTemplateByName(templateName);
	    if(null == template) {
		return new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_TEMPLATE,
			    ErrorHandler.FAILED_DELETEING_TEMPLATES);
	    }
	    template.setTasks(null);
	    onBoardingTemplateService.delete(template);
	    response = new RestResponse(true, SuccessHandler.TEMPLATE_DELETED_SUCCESSFULLY);
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	    response = new RestResponse(false, ErrorHandler.FAILED_TO_DELETE_TEMPLATE,
		    ErrorHandler.FAILED_DELETEING_TEMPLATES);
	}
	return response;
    }


    @RequestMapping(value = "/api/v1/employee/onboarding/{eid}/edit", method = RequestMethod.POST)
    public RestResponse editOnBoardDetails(@RequestPart("json") @Valid OnBoardingDetails onBoardingDetail,
	    @PathVariable Long eid,@RequestPart(value = "file", required = false) MultipartFile file,
		@RequestParam("fileName") String fileName, @RequestParam("fileType") String fileType) throws RecruizException {

	
	  // making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.EditEmployeeOnBoardingDetails.name());
	
	
	    Employee emp = employeeService.findOne(eid);
		try {
		    OnBoardingDetails existingDetails = onBoardingDetailsService.findOne(onBoardingDetail.getId());
		  
		    onBoardingDetail.setEid(emp);
		    onBoardingDetail.setOwner(userService.getLoggedInUserEmail());
		    onBoardingDetail.setCompletedStatus(false);
		    onBoardingDetail.setComments(existingDetails.getComments());
		    onBoardingDetail.setEnrolledPeopleEmails(StringUtils.commaSeparate(onBoardingDetail.getEnrolledPeoples()));
		    onBoardingDetail = onBoardingDetailsService.addOnBoardingActivity(onBoardingDetail);
		    
		    // copy file to employee folder
		    uploadEmloyeeFile(file,fileName,fileType,emp);
		    
		} catch (Exception ex) {
		    logger.error(ex.getMessage(), ex);
		    return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_EDITING_TASK,ErrorHandler.FAILED_TASK_EDITING);
		}
	    
	    return new RestResponse(RestResponse.SUCCESS, onBoardingDetail);
	
    }

    private void uploadEmloyeeFile(MultipartFile file, String fileName, String fileType, Employee emp) throws IOException, IllegalStateException, RecruizWarnException {
	
	if (fileName == null || fileName.isEmpty() || fileType == null || fileType.isEmpty() || emp == null)
	    return ;


	if (fileName != null && !fileName.isEmpty()) {
	    fileName = StringUtils.cleanFileName(fileName);
	}

	String employeeFolder = uploadFileService.createFolderStructureForEmployee(emp.getId() + "");
	File fileToUpload = new File(employeeFolder + File.separator + fileName);
	if (fileToUpload.exists()) {
	    return; 
	}

	File tmpFile = fileService.multipartToFile(file);
	Files.copy(tmpFile.toPath(), fileToUpload.toPath());

	EmployeeFile employeeFile = new EmployeeFile();
	employeeFile.setEid(emp.getId() + "");
	employeeFile.setFileName(fileName);
	employeeFile.setFilePath(fileToUpload.getPath());
	employeeFile.setFileType(fileType);

	emp.getFiles().add(employeeFile);
	employeeService.save(emp);

	if (null != emp.getFiles() && !emp.getFiles().isEmpty()) {
	    emp.getFiles().size();
	}
	
    }
    
    // to get all on boarding templates
    @RequestMapping(value = "/api/v1/onboarding/templates/comments/{taskId}/all", method = RequestMethod.GET)
    public RestResponse getTaskComments(@PathVariable Long taskId) throws Exception {

	OnBoardingDetails details = onBoardingDetailsService.findOne(taskId);
	if(null != details.getComments() && !details.getComments().isEmpty()) {
	    details.getComments().size();
	}
	RestResponse response = new RestResponse(true, details.getComments());
	return response;
    }
    
    
}
