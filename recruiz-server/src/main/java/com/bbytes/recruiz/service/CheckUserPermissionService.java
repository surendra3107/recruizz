package com.bbytes.recruiz.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.VendorPermission;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;

@Service
public class CheckUserPermissionService {

	@Autowired
	private UserService userService;

	public Set<Permission> getLoggedInUserPermission() {
		UserRole loggedInuserRole = userService.getLoggedInUserObject().getUserRole();
		return loggedInuserRole.getPermissions();
	}

	public boolean hasOrgAdminPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.SUPER_ADMIN))
				return true;
		}
		return false;
	}

	public boolean hasITAdminPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.IT_ADMIN))
				return true;
		}
		return false;
	}

	public boolean hasNormalRole() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.NORMAL))
				return true;
		}
		return false;
	}

	public boolean hasAddEditClientPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADD_EDIT_CLIENT))
				return true;
		}
		return false;
	}

	public boolean hasDeleteClientPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.DELETE_CLIENT))
				return true;
		}
		return false;
	}

	public boolean hasAddEditPositionPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADD_EDIT_POSITION))
				return true;
		}
		return false;

	}

	public boolean hasDeletePositionPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.DELETE_POSITION))
				return true;
		}
		return false;
	}

	public boolean hasAddEditCandidatePermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADD_EDIT_CANDIDATE))
				return true;
		}
		return false;
	}

	public boolean hasDeleteCandidatePermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.DELETE_CANDIDATE))
				return true;
		}
		return false;
	}

	public boolean hasAddEditUserPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADD_EDIT_USER))
				return true;
		}
		return false;
	}

	public boolean hasDeleteUserPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.DELETE_USER))
				return true;
		}
		return false;
	}

	public boolean hasAddEditUserRolesPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADD_EDIT_USER_ROLES))
				return true;
		}
		return false;
	}

	public boolean hasDeleteUserRolesPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.DELETE_USER_ROLES))
				return true;
		}
		return false;
	}

	public boolean hasViewEditBoardPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.VIEW_EDIT_BOARD))
				return true;
		}
		return false;
	}

	public boolean belongsToHrExecGroup(UserRole userRole) {
		for (Permission permission : userRole.getPermissions()) {
			if (permission.getPermissionName().contains(PermissionConstant.MANAGER_SETTING)
					|| permission.getPermissionName().contains(PermissionConstant.SUPER_ADMIN)) {
				return false;
			}
		}
		return true;
	}

	public boolean belongsToHrManagerGroup(UserRole userRole) {
		for (Permission permission : userRole.getPermissions()) {
			if (permission.getPermissionName().contains(PermissionConstant.MANAGER_SETTING)
					|| permission.getPermissionName().contains(PermissionConstant.SUPER_ADMIN)) {
				return true;
			}

		}
		return false;
	}

	public boolean hasGlobalEditPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.GLOBAL_EDIT))
				return true;
		}
		return false;
	}

	public boolean hasReportPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.VIEW_REPORTS))
				return true;
		}
		return false;
	}

	public boolean hasViewAllCandidatesPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.VIEW_All_CANDIDATES))
				return true;
		}
		return false;
	}
	
	public boolean hasViewAllProspectPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.VIEW_All_PROSPECTS))
				return true;
		}
		return false;
	}

	public boolean hasGlobalDeletePermission() {
		if (hasOrgAdminPermission())
			return true;
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.GLOBAL_DELETE))
				return true;
		}
		return false;
	}

	public boolean hasAdminSettingPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.ADMIN_SETTING))
				return true;
		}
		return false;
	}

	public boolean isVendorUser() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(VendorPermission.CandidateDetails.getDisplayName())
					|| permission.getPermissionName().contains(VendorPermission.PositionDetails.getDisplayName())
					|| permission.getPermissionName().contains(VendorPermission.ViewBoard.getDisplayName()))
				return true;
		}
		return false;
	}

	/**
	 * To check user type is app
	 * 
	 * @return
	 */
	public boolean isUserTypeApp() {
		if (null != userService.getLoggedInUserObject() && GlobalConstants.USER_TYPE_APP.equalsIgnoreCase(userService.getLoggedInUserObject().getUserType()))
			return true;
		return false;
	}

	/**
	 * To check user type is vendor
	 * 
	 * @return
	 */
	public boolean isUserTypeVendor() {
		if (GlobalConstants.USER_TYPE_VENDOR.equalsIgnoreCase(userService.getLoggedInUserObject().getUserType()))
			return true;
		return false;
	}

	public boolean isSuperAdmin() {
		for (Permission permission : getLoggedInUserPermission()) {
			if (permission.getPermissionName().contains(PermissionConstant.SUPER_ADMIN))
				return true;
		}
		return false;
	}
	
	public boolean isDeptHead() {
	    if(userService.getLoggedInUserObject().getUserRole().getRoleName().equalsIgnoreCase(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE)) {
		return true;
	    }
		
		return false;
	}

	public boolean hasCampaignPermission() {
		for (Permission permission : getLoggedInUserPermission()) {
			// if no permission campaign assign for super admin then directly checking
			// super admin permission
			if (permission.getPermissionName().contains(PermissionConstant.CAMPAIGN_FUNCTION))
				return true;
			else if (isSuperAdmin())
				return true;
		}
		return false;
	}

}
