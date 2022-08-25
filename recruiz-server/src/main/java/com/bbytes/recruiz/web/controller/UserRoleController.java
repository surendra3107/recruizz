package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.RolePermission;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.AssignRoleDTO;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.PermissionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.UserRoleDTO;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.UserRoleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class UserRoleController {

    private static Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private DataModelToDTOConversionService dataModelToDTOConversionService;

    @Autowired
    private CheckUserPermissionService checkUserPermissionService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    @RequestMapping(value = "/api/v1/userrole", method = RequestMethod.POST)
    public RestResponse addUserRoles(@RequestParam("roleName") String roleName) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(), UsageActionType.AddRole.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (roleName == null || roleName.isEmpty())
	    throw new RecruizWarnException("Role is empty", "Invlid role name");

	if (userRoleService.getRolesByName(roleName) != null)
	    throw new RecruizWarnException("Role Exists", "Invlid role name");

	userRoleService.addUserRoles(roleName, null);

	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS, userRoleService.getRolesByName(roleName),
		SuccessHandler.USER_ROLE_ADDED);
	return roleResponse;
    }

    /**
     * API used to assign the role to users.
     * 
     * @param assignRoleDTO
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/userrole/change", method = RequestMethod.PUT)
    public RestResponse changeRole(@RequestBody AssignRoleDTO assignRoleDTO) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ChangeRole.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	RestResponse roleResponse = null;

	if (assignRoleDTO.getRoleId() == null)
	    throw new RecruizWarnException(ErrorHandler.ROLE_NOT_EXIST, ErrorHandler.INVALID_ROLE);

	try {

	    long roleId = Long.parseLong(assignRoleDTO.getRoleId());
	    if (userRoleService.getRoleById(roleId) == null)
		return new RestResponse(false, ErrorHandler.ROLE_NOT_EXIST, ErrorHandler.INVALID_ROLE);

	    if (assignRoleDTO.getUserEmailList() != null && !assignRoleDTO.getUserEmailList().isEmpty()) {
		String roleName = userRoleService.getRoleById(roleId).getRoleName();
		for (String email : assignRoleDTO.getUserEmailList()) {
		    userService.assignUserRole(email, roleName);
		}
		roleResponse = new RestResponse(RestResponse.SUCCESS, SuccessHandler.ROLES_CHANGED);
	    } else
		roleResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.NO_CHANGED);
	} catch (Exception ex) {
	    logger.warn("*********" + ex.getMessage() + "*************", ex);
	    roleResponse = new RestResponse(false, ErrorHandler.CAN_NOT_CHANGE_ROLE_FOR_INACTIVE_USER,
		    ErrorHandler.NO_ROLE_CHANGE_FOR_INACTIVE_USER);
	}
	return roleResponse;
    }

    @RequestMapping(value = "/api/v1/userRoles/getAllUserRoles", method = RequestMethod.GET)
    public RestResponse getAllUserRoles() throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllRoles.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	List<UserRole> userRoles = userService.getAllRoles();
	UserRole vendor = userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE);
	UserRole normal = userRoleService.getRolesByName("Normal");
	userRoles.remove(normal);
	userRoles.remove(vendor);
	Map<String, Object> roleMap = dataModelToDTOConversionService.getAllRoles(userRoles);
	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS, roleMap, SuccessHandler.USER_ROLES);
	return roleResponse;
    }

    /*
     * This api will change the existing role to Normal, Make sure user has
     * agreed to this option
     */
    @RequestMapping(value = "/api/v1/userrole", method = RequestMethod.DELETE)
    public RestResponse deleteUserRoles(@RequestParam("id") String id) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteRole.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (id == null || id.isEmpty())
	    return null;
	long roleId = Long.parseLong(id);
	String roleName = userRoleService.getRoleById(roleId).getRoleName();

	if (roleName.equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE))
	    throw new RecruizWarnException(ErrorHandler.ROLE_NOT_DELETABLE, ErrorHandler.CAN_NOT_DELETE_ROLE);

	if (roleName.equalsIgnoreCase(GlobalConstants.NORMAL_USER_ROLE))
	    throw new RecruizWarnException(ErrorHandler.ROLE_NOT_DELETABLE, ErrorHandler.CAN_NOT_DELETE_ROLE);

	if (roleName.equalsIgnoreCase(GlobalConstants.IT_ADMIN_USER_ROLE))
	    throw new RecruizWarnException(ErrorHandler.ROLE_NOT_DELETABLE, ErrorHandler.CAN_NOT_DELETE_ROLE);

	UserRole updatableRole = userRoleService.getRolesByName(GlobalConstants.NORMAL_USER_ROLE);

	if (updatableRole == null) {
	    Permission permission = new Permission(PermissionConstant.NORMAL);
	    updatableRole = new UserRole();
	    updatableRole.getPermissions().add(permission);
	    updatableRole.setRoleName("Normal");
	    userRoleService.save(updatableRole);
	}

	UserRole deletableRole = userRoleService.getRolesByName(roleName);
	Set<User> userList = userService.getAllUserByRole(deletableRole);
	if (!userList.isEmpty()) {
	    for (User user : userList) {
		user.setUserRole(updatableRole);
	    }
	}

	userRoleService.deleteRolesByRoleName(roleId);
	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.ROLE_DELETED, null);
	return roleResponse;
    }

    @RequestMapping(value = "/api/v1/userRoles/getRoleByName", method = RequestMethod.POST)
    public RestResponse getUserRoleByRoleName(@RequestParam String roleName) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetRoleByName.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (roleName == null || roleName.isEmpty())
	    return null;
	UserRole role = userRoleService.getRolesByName(roleName);
	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS, role, roleName + "Details Fetched");
	return roleResponse;
    }

    @RequestMapping(value = "/api/v1/userRoles/getAllPermission/all", method = RequestMethod.GET)
    public RestResponse getAllPermission() throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllPermission.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	List<PermissionDTO> permissions = getPermissionList();

	RestResponse rolePermission = new RestResponse(RestResponse.SUCCESS, permissions, null);
	return rolePermission;
    }

    private List<PermissionDTO> getPermissionList() {
	List<PermissionDTO> permissions = new ArrayList<PermissionDTO>();
	permissions.add(new PermissionDTO(PermissionConstant.ADD_EDIT_CLIENT));
	permissions.add(new PermissionDTO(PermissionConstant.DELETE_CLIENT));
	permissions.add(new PermissionDTO(PermissionConstant.ADD_EDIT_POSITION));
	permissions.add(new PermissionDTO(PermissionConstant.DELETE_POSITION));
	permissions.add(new PermissionDTO(PermissionConstant.ADD_EDIT_CANDIDATE));
	permissions.add(new PermissionDTO(PermissionConstant.DELETE_CANDIDATE));
	permissions.add(new PermissionDTO(PermissionConstant.VIEW_All_CANDIDATES));
	permissions.add(new PermissionDTO(PermissionConstant.VIEW_All_PROSPECTS));
	// permissions.add(new PermissionDTO(PermissionConstant.ADD_EDIT_USER));
	// permissions.add(new PermissionDTO(PermissionConstant.DELETE_USER));
	// permissions.add(new
	// PermissionDTO(PermissionConstant.ADD_EDIT_USER_ROLES));
	// permissions.add(new
	// PermissionDTO(PermissionConstant.DELETE_USER_ROLES));
	permissions.add(new PermissionDTO(PermissionConstant.VIEW_EDIT_BOARD));
	permissions.add(new PermissionDTO(PermissionConstant.MANAGER_SETTING));
	permissions.add(new PermissionDTO(PermissionConstant.GLOBAL_EDIT));
	permissions.add(new PermissionDTO(PermissionConstant.GLOBAL_DELETE));
	permissions.add(new PermissionDTO(PermissionConstant.ADMIN_SETTING));
	permissions.add(new PermissionDTO(PermissionConstant.DISABLE_RESUME_DOWNLOAD));
	permissions.add(new PermissionDTO(PermissionConstant.ADD_EDIT_PROSPECTS));
	permissions.add(new PermissionDTO(PermissionConstant.VIEW_REPORTS));
	permissions.add(new PermissionDTO(PermissionConstant.GENERATE_INVOICE));
	permissions.add(new PermissionDTO(PermissionConstant.CAREER_SITE));
	permissions.add(new PermissionDTO(PermissionConstant.CAMPAIGN_FUNCTION));
	permissions.add(new PermissionDTO(PermissionConstant.EMAIL_CLIENT));
	permissions.add(new PermissionDTO(PermissionConstant.CAREER_PAGE));

	return permissions;
    }

    @RequestMapping(value = "/api/v1/userRoles/renameRoleName", method = RequestMethod.POST)
    public RestResponse renameRoleName(@RequestParam("roleName") String roleName, @RequestParam("id") String id)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ChangeRoleName.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (roleName == null || roleName.isEmpty() || id == null || id.isEmpty())
	    return null;
	long roleId = Long.parseLong(id);
	UserRole updatedRole = userRoleService.updateUserRole(roleName, roleId);
	RestResponse role = new RestResponse(RestResponse.SUCCESS, updatedRole, null);
	return role;
    }

    @RequestMapping(value = "/api/v1/userRoles/changeRolePermissions", method = RequestMethod.POST)
    public RestResponse changeRolePermissions(@RequestBody UserRoleDTO userRoleDTO,
	    @RequestParam("assignRole") String assignRole, @RequestParam("allAssign") String allAssign)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.ChangeRolePermission.name());
*/
	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (userRoleDTO.getRoleName().isEmpty())
	    throw new RecruizWarnException("Role is empty", "Invlid role name");

	/*
	 * if (userRoleService.getRolesByName(userRoleDTO.getRoleName()) ==
	 * null) throw new RecruizException("Role Does not exists",
	 * "Invlid role name");
	 */

	if (assignRole.equalsIgnoreCase("yes") && allAssign.equalsIgnoreCase("no")) {
	    userRoleService.addPermissionsToRole(userRoleDTO.getRoleName(), userRoleDTO.getPermissions());
	} else if (assignRole.equalsIgnoreCase("yes") && allAssign.equalsIgnoreCase("yes")) {
	    for (PermissionDTO permissionDTO : getPermissionList()) {
		userRoleDTO.getPermissions().add(new Permission(permissionDTO.getPermissionName()));
	    }
	    userRoleService.addPermissionsToRole(userRoleDTO.getRoleName(), userRoleDTO.getPermissions());
	} else if (assignRole.equalsIgnoreCase("no") && allAssign.equalsIgnoreCase("no")) {
	    userRoleService.removeRolePermissions(userRoleDTO.getRoleName(), userRoleDTO.getPermissions());
	} else if (assignRole.equalsIgnoreCase("no") && allAssign.equalsIgnoreCase("yes")) {
	    userRoleService.removeRolePermissions(userRoleDTO.getRoleName(), userRoleDTO.getPermissions());
	} else {
	    throw new RecruizException(ErrorHandler.INVALID_REQUEST, ErrorHandler.INVALID_REQUEST);
	}

	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS,
		userRoleService.getRolesByName(userRoleDTO.getRoleName()), null);
	return roleResponse;
    }

    @RequestMapping(value = "/api/v1/userRoles/roles/permissions/all", method = RequestMethod.GET)
    public RestResponse getAllRoleAndPermission() throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllRoleAndPermission.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (!checkUserPermissionService.hasOrgAdminPermission() && !checkUserPermissionService.hasITAdminPermission()
		&& !checkUserPermissionService.hasAdminSettingPermission())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	Map<String, Object> rolePermissionMap = new LinkedHashMap<String, Object>();
	UserRole orgAdmin = userRoleService.getRolesByName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
	UserRole normal = userRoleService.getRolesByName(GlobalConstants.NORMAL_USER_ROLE);
	UserRole vendor = userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE);
	UserRole deptHead = userRoleService.getRolesByName(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE);
	UserRole itAdmin = userRoleService.getRolesByName(GlobalConstants.IT_ADMIN_USER_ROLE);
	List<UserRole> allRoles = userRoleService.getAllRoles();
	allRoles.remove(normal);
	allRoles.remove(orgAdmin);
	allRoles.remove(vendor);
	allRoles.remove(deptHead);
	allRoles.remove(itAdmin);
	// List<PermissionDTO> permissions = getPermissionList();

	LinkedList<BaseDTO> permissions = new LinkedList<BaseDTO>();
	for (RolePermission permission : RolePermission.values()) {
	    if (permission.name().equalsIgnoreCase(RolePermission.AEUSER.name())
		    || permission.name().equalsIgnoreCase(RolePermission.DeleteUser.name())
		    || permission.name().equalsIgnoreCase(RolePermission.AERoles.name())
		    || permission.name().equalsIgnoreCase(RolePermission.DeleteRoles.name())) {
		continue;
	    }
	    BaseDTO baseDTO = new BaseDTO();
	    baseDTO.setId(permission.getPermissionName());
	    baseDTO.setValue(permission.getDisplayText());
	    permissions.add(baseDTO);
	}

	for (UserRole role : allRoles) {
	    for (Permission perm : role.getPermissions()) {
		role.getPermissionList().add(perm.getPermissionName());
	    }
	}

	rolePermissionMap.put("roles", allRoles);
	rolePermissionMap.put("permissions", permissions);

	RestResponse roleResponse = new RestResponse(RestResponse.SUCCESS, rolePermissionMap, null);
	return roleResponse;
    }
}
