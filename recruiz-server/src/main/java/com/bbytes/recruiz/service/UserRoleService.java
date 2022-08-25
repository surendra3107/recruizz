package com.bbytes.recruiz.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.UserRolesRepository;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class UserRoleService extends AbstractService<UserRole, Long> {

	private UserRolesRepository userRolesRepository;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	public UserRoleService(UserRolesRepository userRolesRepository) {
		super(userRolesRepository);
		this.userRolesRepository = userRolesRepository;
	}

	/**
	 * This method will return the UserRole object for the roleName provided as
	 * argument
	 * 
	 * @throws RecruizException
	 * 
	 */

	@Transactional(readOnly = true)
	public UserRole getRolesByName(String roleName) throws RecruizException {
		return userRolesRepository.findOneByRoleName(roleName);
	}

	@Transactional(readOnly = true)
	public UserRole getRoleById(long id) throws RecruizException {
		return userRolesRepository.findOneById(id);
	}

	@Transactional(readOnly = true)
	public List<UserRole> getAllRoles() throws RecruizException {
		return userRolesRepository.findAll();
	}

	@Transactional
	public void addUserRoles(String roleName, Set<Permission> permissions) throws RecruizException {

		UserRole userRoles = new UserRole();
		userRoles.setRoleName(roleName);
		userRoles.setPermissions(permissions);
		saveAndFlush(userRoles);
	}

	@Transactional
	public void addPermissionsToRole(String roleName, Set<Permission> permissions) throws RecruizException {
		Permission permission = null;
		for (Permission perm : permissions) {
			permission = new Permission(perm.getPermissionName());
			permission.setPermissionName(perm.getPermissionName());
			UserRole userRole = userRolesRepository.findOneByRoleName(roleName);
			userRole.getPermissions().add(permission);
			save(userRole);
		}
	}

	@Transactional
	public void removeRolePermissions(String roleName, Set<Permission> permissions) throws RecruizException {
		UserRole userRoles = userRolesRepository.findOneByRoleName(roleName);
		Set<Permission> roleExistingPermissions = userRoles.getPermissions();
		roleExistingPermissions.removeAll(permissions);
		userRoles.setPermissions(roleExistingPermissions);
		save(userRoles);
	}

	@Transactional
	public void deleteRolesByRoleName(long roleId) throws RecruizException {
		if (!checkUserPermission.hasOrgAdminPermission() && !checkUserPermission.hasAddEditUserRolesPermission()
				&& !checkUserPermission.hasAdminSettingPermission())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		UserRole roleToDelete = userRolesRepository.findOneById(roleId);
		userRolesRepository.delete(roleToDelete);
	}

	@Transactional(readOnly = true)
	public boolean isRoleExists(String roleName) {
		return userRolesRepository.findOneByRoleName(roleName) == null ? false : true;
	}

	public boolean hasPermission(Set<Permission> loggedInUserPermissionList, String permissionName) {
		boolean state = false;
		for (Permission permission : loggedInUserPermissionList) {
			if (permission.getPermissionName().equalsIgnoreCase(permissionName))
				return true;
			else
				state = false;
		}
		return state;
	}

	@Transactional
	public UserRole updateUserRole(String roleName, long id) throws RecruizException {
		if (!checkUserPermission.hasOrgAdminPermission() && !checkUserPermission.hasAddEditUserRolesPermission()
				&& !checkUserPermission.hasAdminSettingPermission())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		UserRole roleToUpdate = userRolesRepository.findOneById(id);

		if (roleToUpdate != null && roleToUpdate.getRoleName().equalsIgnoreCase(roleName))
			return roleToUpdate;

		if (userRolesRepository.findOneByRoleName(roleName) != null)
			throw new RecruizWarnException(ErrorHandler.ROLE_NAME_EXISTS, ErrorHandler.RENAMING_FAILED);

		if (roleName.equalsIgnoreCase("Organization Admin") || roleName.equalsIgnoreCase("Normal")
				|| roleName.equalsIgnoreCase(GlobalConstants.IT_ADMIN_USER_ROLE))
			throw new RecruizWarnException(ErrorHandler.CANNOT_RENAME_ROLE, ErrorHandler.RENAMING_FAILED);

		if (roleToUpdate.getRoleName().equalsIgnoreCase(GlobalConstants.SUPER_ADMIN_USER_ROLE)
				|| roleToUpdate.getRoleName().equalsIgnoreCase(GlobalConstants.NORMAL_USER_ROLE)
				|| roleToUpdate.getRoleName().equalsIgnoreCase(GlobalConstants.IT_ADMIN_USER_ROLE))
			throw new RecruizWarnException(ErrorHandler.CANNOT_RENAME_ROLE, ErrorHandler.RENAMING_FAILED);

		roleToUpdate.setRoleName(roleName);
		roleToUpdate = save(roleToUpdate);
		return roleToUpdate;
	}

}
