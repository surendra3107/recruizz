package com.bbytes.recruiz.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TenantResolverRepository;

@Service
public class TenantResolverService {

	private static Logger logger = LoggerFactory.getLogger(TenantResolverService.class);

	@Autowired
	private SpringProfileService springProfileService;

	@Autowired
	private TenantResolverRepository tenantResolverRepository;

	public List<String> findTenantIdListForUserEmail(String email) {
		if (springProfileService.isEnterpriseMode())
			return null;

		List<String> tenantIds = tenantResolverRepository.findOrgIdListForUser(email);

		if (tenantIds == null || tenantIds.isEmpty()) {
			throw new UsernameNotFoundException("User not found with email '" + email + "'");
		}

		return tenantIds;
	}

	public boolean doesTenantResolverExistForUser(User user) {
		if (user == null || user.getOrganization().getOrgId() == null)
			throw new IllegalArgumentException("User or tenantId cannot be null");

		List<String> tenantResolverList = tenantResolverRepository.findOrgIdListForUser(user.getEmail());

		if (tenantResolverList == null || tenantResolverList.isEmpty())
			return false;

		return true;
	}

	public TenantResolver saveTenantResolverForUser(User user) {
		if (user == null || user.getOrganization().getOrgId() == null)
			throw new IllegalArgumentException("User or tenantId cannot be null");

		TenantResolver tenantResolver = new TenantResolver();
		tenantResolver.setEmail(user.getEmail());
		tenantResolver.setOrgId(user.getOrganization().getOrgId());
		tenantResolver.setUserId(user.getUserId());
		tenantResolver.setOrgName(user.getOrganization().getOrgName());

		boolean saved = tenantResolverRepository.save(tenantResolver);
		// saving user details to tenant resolver database for allowing central
		// login
		User dbUser = tenantResolverRepository.findUserByEmail(user.getEmail());
		if (saved && dbUser == null)
			tenantResolverRepository.save(user);

		if (dbUser != null && dbUser.getPassword() == null)
			tenantResolverRepository.update(user);

		logger.debug(String.format("Saved user with email %s successfully", user.getEmail()));
		TenantResolver savedTenantResolver = tenantResolverRepository.findByEmailAndOrgId(tenantResolver.getEmail(),
				user.getOrganization().getOrgId());
		return savedTenantResolver;
	}

	public void saveSixthSenseUser(SixthSenseUser user) throws RecruizException {
		if (user == null || user.getUser() == null)
			throw new RecruizException("Sixth Sense User or recruiz user cannot be null");

		user.setCaptchaStatus("0");
		tenantResolverRepository.saveSixthSenseUser(user);

	}

	public void updateSixthSenseUser(SixthSenseUser user) {
		if (user == null || user.getUserName() == null)
			throw new IllegalArgumentException("Sixth Sense User or recruiz user cannot be null");

		tenantResolverRepository.updateSixthSenseUser(user);
	}
	
	public void deleteSixthSenseUser(SixthSenseUser user) {
		if (user == null || user.getUserName() == null)
			throw new IllegalArgumentException("Sixth Sense User or recruiz user cannot be null");

		tenantResolverRepository.deleteSixthSenseUser(user);

	}

	public boolean saveUserToTenantMgmtDatabase(User user) {
		return tenantResolverRepository.save(user);
	}

	public boolean savePreSignUpUser(User user) throws RecruizException {
		if (user == null)
			throw new IllegalArgumentException("User cannot be null");

		return tenantResolverRepository.savePreSignUpUser(user);
	}

	public void deleteTenantResolverForUserId(Long userId) {
		if (userId == null)
			throw new IllegalArgumentException("User id cannot be null");

		TenantResolver tenantResolver = tenantResolverRepository.findOneByUserId(userId);
		tenantResolverRepository.delete(tenantResolver);

	}

	public void deleteTenantResolverForUser(String email, String orgId) {
		if (email == null)
			throw new IllegalArgumentException("User email cannot be null");

		tenantResolverRepository.delete(tenantResolverRepository.findByEmailAndOrgId(email, orgId));
	}

	public void deleteTenantResolver(String email, String orgId) {
		if (email == null)
			throw new IllegalArgumentException("User email cannot be null");
		try {
			tenantResolverRepository.deleteTenantResolver(email, orgId);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void deleteUserFromTenantResolver(String email) {
		if (email == null)
			throw new IllegalArgumentException("User email cannot be null");
		try {
			tenantResolverRepository.deleteUserFromTenantResolver(email);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	public User findUserByEmail(String email) {
		User user = tenantResolverRepository.findOneByEmail(email);
		return user;
	}

	public TenantResolver findByEmailAndOrgID(String email, String orgID) {
		TenantResolver tenantResolver = tenantResolverRepository.findByEmailAndOrgId(email, orgID);
		return tenantResolver;
	}

	public void deleteDB(String orgId) {
		try {
			tenantResolverRepository.deleteDB(orgId);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	public boolean userExistsForOrg(String email, String orgID) {
		TenantResolver tenantResolver = tenantResolverRepository.findByEmailAndOrgId(email, orgID);
		if (tenantResolver == null)
			return false;
		return true;
	}

	public boolean emailExist(String email) {
		return tenantResolverRepository.userExists(email);
	}

	public boolean isVendorExists(String email) {
		return tenantResolverRepository.vendorExists(email);
	}

	public boolean organizationExist(String orgId) {
		int count = tenantResolverRepository.countByOrgId(orgId);
		if (count > 0)
			return true;

		return false;
	}

	public int organizationCount() {
		List<String> orgIds = tenantResolverRepository.findDistinctOrgIds();
		if (orgIds != null)
			return orgIds.size();

		return 0;
	}

	public TenantResolver findOneByUserId(Long userId) {
		TenantResolver tenantResolver = tenantResolverRepository.findOneByUserId(userId);
		return tenantResolver;
	}

	/**
	 * Should not be exposed in prod so making it part of dev profile
	 */
	public void deleteAll() {
		if (!springProfileService.isTestMode())
			throw new IllegalAccessError(
					"This method is not allowed in dev or prod mode of the app, only valid in test mode");

		tenantResolverRepository.deleteAll();
	}

	public List<String> findAllTenants() {
		return tenantResolverRepository.findDistinctOrgIds();
	}

	public List<String> findAllTenantsForUserId(String email) {
		return tenantResolverRepository.findOrgIdListForUser(email);
	}

	public Map<String, String> getOrgIdToOrgNameMap(List<String> ids) {
		return tenantResolverRepository.getOrganizationIdAndName(ids);
	}

	public boolean isTenantValid(String tenantId) {

		int count = tenantResolverRepository.countByOrgId(tenantId);
		if (count > 0)
			return true;

		return false;
	}

	public boolean resetPassword(String email, String password) {

		if (tenantResolverRepository.userExists(email)) {
			return tenantResolverRepository.changePassword(email, password);
		} else {
			return false;
		}
	}

	public List<User> getAllUsers() {
		return tenantResolverRepository.getUserList();
	}

	public User getUserByEmail(String email) {
		return tenantResolverRepository.findUserByEmail(email);
	}

	public String getTenant(String orgId) {
		return tenantResolverRepository.getTenantId(orgId);
	}

	public int getTenantsCount(String email) {
		return tenantResolverRepository.getTenantsCount(email);
	}

	public SixthSenseUser findSixthSenseUserByUserName(String email) {
		return tenantResolverRepository.findSixthSenseUserByUserName(email);
	}

	/**
	 * to update plan id in tenant resolvers Plutus Org Info table
	 * 
	 * @param tenant
	 */
	public void updatePlutusOrgPlanId(String tenant, String planId, String planName, String subscriptionMode) {
		tenantResolverRepository.updateRecuizPlanId(tenant, planId, planName, subscriptionMode);
	}

	/**
	 * to get plutus org info stored in tenant mgmt db
	 * 
	 * @param orgId
	 * @return
	 */
	public PlutusOrganizationInfo getPlutusOrgInfo(String orgId) {
		return tenantResolverRepository.findPlutusOrgInfo(orgId);
	}

	/**
	 * to check if any user is registered with the mobile number
	 * 
	 * @param email
	 * @return
	 */
	public boolean isMobileExists(String mobileNumber) {
		return tenantResolverRepository.userMobilenumberExists(mobileNumber);
	}

	public User getOneUserForTenant(String tenant) {
		return tenantResolverRepository.findOneUserFromTenant(tenant);
	}

	public void changeOrgName(String tenant, String orgNewName) {
		tenantResolverRepository.changeOrgName(tenant, orgNewName);
	}

	/**
	 * to store campaign email message id and tenant map
	 * 
	 * @param tenant
	 * @param messageId
	 * @return
	 */
	public boolean saveTenantAndMessageIdMap(String tenant, String messageId) {
		return tenantResolverRepository.saveTenantAndMessageIdMap(tenant, messageId);
	}

	/**
	 * fetch and return the tenant from campaign_email_tenant_map table for the
	 * given message id
	 * 
	 * @param messageId
	 * @return
	 */
	public String getTenantForMessageId(String messageId) {
		return tenantResolverRepository.getTenantForMessageId(messageId);
	}
}
