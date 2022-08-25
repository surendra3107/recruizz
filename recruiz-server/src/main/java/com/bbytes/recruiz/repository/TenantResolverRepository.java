package com.bbytes.recruiz.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.PlutusOrgDTO;
import com.bbytes.recruiz.service.PasswordHashService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Repository
public class TenantResolverRepository {

	private static final Logger logger = LoggerFactory.getLogger(TenantResolverRepository.class);

	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("tenant-mgmt")
	private DataSource tenantMgMtDataSource;

	@Autowired
	private PasswordHashService passwordHashService;

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(tenantMgMtDataSource);
	}

	public TenantResolver findByEmailAndOrgId(String email, String orgID) {

		MessageFormat query = new MessageFormat(
				"SELECT distinct * FROM tenant_resolver where email = ''{0}'' AND org_id = ''{1}''");

		TenantResolver tenantResolver = null;
		try {
			String sql = query.format(new Object[] { email, orgID });
			tenantResolver = jdbcTemplate.queryForObject(sql,
					new BeanPropertyRowMapper<TenantResolver>(TenantResolver.class));
		} catch (DataAccessException e) {
			// logger.error(e.getMessage(), e);
		}

		return tenantResolver;
	}

	public Integer countByOrgId(String orgId) {

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM tenant_resolver where LOWER(org_id) = LOWER(?) ", Integer.class, orgId);

		return count;
	}

	public TenantResolver findOneByUserId(Long userId) {
		MessageFormat query = new MessageFormat("SELECT * FROM tenant_resolver where user_id = {0}");

		TenantResolver tenantResolver = null;
		try {
			String sql = query.format(new Object[] { userId });
			tenantResolver = jdbcTemplate.queryForObject(sql,
					new BeanPropertyRowMapper<TenantResolver>(TenantResolver.class));
		} catch (DataAccessException e) {
			// do nothing
		}

		return tenantResolver;
	}

	public User findOneByEmail(String email) {
		User user = null;
		try {
			String sql = "SELECT * FROM user where email = '" + email + "'";

			user = jdbcTemplate.queryForObject(sql, new RowMapper<User>() {

				@Override
				public User mapRow(ResultSet rs, int rowNum) throws SQLException {
					User user = new User();
					user.setUserId(rs.getLong("user_id"));
					user.setName(rs.getString("name"));
					user.setEmail(rs.getString("email"));
					user.setPassword(rs.getString("password"));
					user.setUserType(rs.getString("user_type"));
					user.setLoggedOn(rs.getTimestamp("last_logged_on_time"));
					user.setTimezone(rs.getString("timezone"));
					return user;
				}

			});
		} catch (DataAccessException e) {
			// do nothing
		}

		return user;
	}

	public void deleteAll() {
		String sql = "DELETE FROM tenant_resolver";
		jdbcTemplate.execute(sql);
	}

	public void delete(TenantResolver tenantResolver) {
		if (tenantResolver == null)
			return;

		String sql = MessageFormat.format("DELETE FROM tenant_resolver where id = {0}", tenantResolver.getId());
		jdbcTemplate.execute(sql);
	}

	public void deleteDB(String orgId) {
		if (orgId != null && !orgId.isEmpty()) {
			String sql = "DROP DATABASE " + orgId + ";";
			jdbcTemplate.execute(sql);
		}

	}

	public void delete(List<TenantResolver> tenantResolverList) {
		if (tenantResolverList == null || tenantResolverList.size() == 0)
			return;

		List<Long> ids = new ArrayList<Long>();
		for (TenantResolver tenantResolver : tenantResolverList) {
			ids.add(tenantResolver.getId());
		}

		String sql = MessageFormat.format("DELETE FROM tenant_resolver where id IN (:ids)", ids);
		jdbcTemplate.execute(sql);
	}

	public boolean save(TenantResolver tenantResolver) {

		if (tenantResolver == null)
			return false;

		logger.debug("Initiating save tenant resolver for user and org {}", tenantResolver.getUserId(),
				tenantResolver.getOrgId());
		String insertSql = "INSERT INTO tenant_resolver(`id`,`creation_date`,`modification_date`,`email`,`org_id`,`user_id`,`organization_name`) VALUES(?,?,?,?,?,?,?)";
		String updateSql = "UPDATE tenant_resolver SET `modification_date` = ?,`email` = ?,`org_id` = ?,`user_id` = ?  WHERE `id` = ?";
		int rowCount = 0;

		rowCount = jdbcTemplate.update(insertSql,
				new Object[] { tenantResolver.getId(), DateTime.now().toDate(), DateTime.now().toDate(),
						tenantResolver.getEmail(), tenantResolver.getOrgId(), tenantResolver.getUserId(),
						tenantResolver.getOrgName() });

		if (rowCount > 0) {
			logger.debug("Success saving data to tenant resolver for user and org {} ", tenantResolver.getUserId(),
					tenantResolver.getOrgId());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	// using jdbc template to save user to tenant management database
	public boolean save(User user) {

		if (user == null)
			return false;

		String insertSql = "INSERT INTO `user` (`email`, `name`, `password`, `timezone`,`organization_org_id`,`user_type`,`vendor_id`,`mobile`) VALUES (?,?,?,?,?,?,?,?)";
		int rowCount = 0;
		rowCount = jdbcTemplate.update(insertSql, new Object[] { user.getEmail(), user.getName(), user.getPassword(),
				user.getTimezone(), null, user.getUserType(), user.getVendorId(), user.getMobile() });

		if (rowCount > 0) {
			logger.debug("Success saving data to tenant resolver for user and org {} ", user.getName(),
					user.getEmail());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	// using jdbc template to save sixth sense user to tenant management
	// database
	public boolean saveSixthSenseUser(SixthSenseUser user) {

		if (user == null)
			return false;

		String insertSql = "INSERT INTO `sixth_sense_user` (`login_user_email`,`captcha_status`,`user_name`, `password`, `user_id`, `usage_type`,`view_count`, `sources`) VALUES (?,?,?,?,?,?,?,?)";
		int rowCount = 0;
		rowCount = jdbcTemplate.update(insertSql, new Object[] { user.getLoggedUserEmail(),user.getCaptchaStatus(),user.getUserName(), user.getPassword(),
				user.getUser().getUserId(), user.getUsageType(), user.getViewCount(), user.getSources() });

		if (rowCount > 0) {
			logger.debug("Successfully saving data to tenant management for sixth sense user", user.getUserName());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	// using jdbc template to save sixth sense user to tenant management
	// database
	public boolean updateSixthSenseUser(SixthSenseUser user) {

		if (user == null)
			return false;

		String updateSql = "UPDATE `sixth_sense_user` SET `login_user_email`=?,`captcha_status`=?,`password`=?, `usage_type`=?, `view_count`=?, `sources`=?  WHERE user_name = ?";
		int rowCount = 0;
		rowCount = jdbcTemplate.update(updateSql, new Object[] {user.getLoggedUserEmail(),user.getCaptchaStatus(), user.getPassword(), user.getUsageType(),
				user.getViewCount(), user.getSources(), user.getUserName() });

		if (rowCount > 0) {
			logger.debug("Successfully updating data to tenant management for sixth sense user", user.getUserName());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	public void deleteSixthSenseUser(SixthSenseUser user) {
		if (user == null)
			return;

		String sql = "DELETE FROM sixth_sense_user where user_name = '" + user.getUserName() + "'";
		jdbcTemplate.execute(sql);
	}

	// using jdbc template to save user to tenant resover database
	public boolean update(User user) {

		if (user == null)
			return false;

		String updateSQL = "UPDATE user set name=? ,password =? ,timezone=? , organization_org_id =? where email = ?";

		int rowCount = 0;
		rowCount = jdbcTemplate.update(updateSQL,
				new Object[] { user.getName(), user.getPassword(), user.getTimezone(), null, user.getEmail() });

		if (rowCount > 0) {
			logger.debug("Success updating data to tenant resolver for user and org {} ", user.getName(),
					user.getEmail());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	// using jdbc template to save user to tenant resover database
	public boolean savePreSignUpUser(User user) throws RecruizException {

		if (user == null)
			return false;
		if (userAlreadyPreSignedUp(user.getEmail()))
			throw new RecruizWarnException(ErrorHandler.ALREADY_SIGNED_UP, ErrorHandler.SIGN_UP_FAILED);

		String insertSql = "INSERT INTO `presignup` (`email`) VALUES (?)";
		int rowCount = 0;
		rowCount = jdbcTemplate.update(insertSql, new Object[] { user.getEmail() });

		if (rowCount > 0) {
			logger.debug("Success saving data to pre signup table for user and org {} ", user.getEmail());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	public SixthSenseUser findSixthSenseUserByUserName(String userName) {

		String query = "SELECT DISTINCT * FROM sixth_sense_user where user_name = ?";

		// In Spring 2.5, BeanPropertyRowMapper maps row’s column value to a
		// property by matching their names e.g property ‘email’ will match
		// to column name ‘email’ or with underscores ‘email’
		try {
			SixthSenseUser sixthSenseUser = jdbcTemplate.queryForObject(query, new Object[] { userName },
					new BeanPropertyRowMapper<SixthSenseUser>(SixthSenseUser.class));

			return sixthSenseUser;
		} catch (Exception e) {
			return null;
		}
	}

	public List<String> findDistinctOrgIds() {
		List<String> orgIds = new ArrayList<String>();

		try {
			String sql = "SELECT distinct org_id FROM tenant_resolver";
			orgIds = jdbcTemplate.queryForList(sql, String.class);
		} catch (DataAccessException e) {
			// do nothing
		}
		return orgIds;
	}

	// this method will return list of org_id against the email id provided
	public List<String> findOrgIdListForUser(String email) {
		List<String> orgIds = new ArrayList<String>();

		try {
			String sql = "SELECT distinct org_id FROM tenant_resolver where email = '" + email + "'";
			orgIds = jdbcTemplate.queryForList(sql, String.class);
		} catch (DataAccessException e) {
			// do nothing
		}

		return orgIds;
	}

	// getting all user from tenant management database
	public List<User> getUserList() {

		String query = "SELECT DISTINCT * FROM user";

		List<User> userList = jdbcTemplate.query(query, new BeanPropertyRowMapper<User>(User.class));

		return userList;
	}

	// this method will check if user is already registered
	public boolean userExists(String email) {
		Integer cnt = jdbcTemplate.queryForObject("SELECT count(*) FROM user WHERE email = ? ", Integer.class, email);
		return cnt != null && cnt > 0;
	}

	// this method will check if vendor is already registered
	public boolean vendorExists(String email) {
		Integer cnt = jdbcTemplate.queryForObject("SELECT count(*) FROM user WHERE email = ? AND user_type = ?",
				Integer.class, email, GlobalConstants.USER_TYPE_VENDOR);
		return cnt != null && cnt > 0;
	}

	public boolean userAlreadyPreSignedUp(String email) {
		Integer cnt = jdbcTemplate.queryForObject("SELECT count(*) FROM presignup WHERE email = ? ", Integer.class,
				email);
		return cnt != null && cnt > 0;
	}

	public Map<String, String> getOrganizationIdAndName(List<String> ids) {
		if(ids==null || ids.isEmpty())
			return new HashMap<>();
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", ids);

		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

		Map<String, String> result = template.query(
				"SELECT distinct org_id,organization_name FROM tenant_resolver WHERE org_id IN (:ids) ", parameters,
				new ResultSetExtractor<Map<String, String>>() {
					@Override
					public Map<String, String> extractData(final ResultSet rs)
							throws SQLException, DataAccessException {
						final HashMap<String, String> mapRet = new HashMap<String, String>();
						while (rs.next()) {
							mapRet.put(rs.getString("org_id"), rs.getString("organization_name"));
						}
						return mapRet;
					}
				});

		return result;

	}

	public User findUserByEmail(String emailId) {
		MessageFormat query = new MessageFormat("SELECT distinct * FROM user where email = ''{0}''");

		User user = null;
		try {
			String sql = query.format(new Object[] { emailId });
			user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<User>(User.class));
		} catch (DataAccessException e) {
			// do nothing
		}
		return user;
	}

	public boolean changePassword(String emailId, String password) {

		String pass = passwordHashService.encodePassword(password);

		String sqlUpdate = "UPDATE user set password=? where email=?";
		int count = jdbcTemplate.update(sqlUpdate, pass, emailId);

		if (count > 0)
			return true;
		else
			return false;
	}

	// this method will return tenant id from tenant resolver
	public String getTenantId(String orgId) {
		String tenantId = null;

		List<String> orgIds = new ArrayList<String>();

		try {
			String sql = "SELECT distinct org_id FROM tenant_resolver where LOWER(org_id) = LOWER('" + orgId + "')";
			orgIds = jdbcTemplate.queryForList(sql, String.class);
		} catch (DataAccessException e) {
			// do nothing
		}
		if (orgIds != null && orgIds.size() > 0) {
			tenantId = orgIds.get(0);
		}
		return tenantId;
	}

	public int getTenantsCount(String email) {

		int tenantCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tenant_resolver where email = ? ",
				Integer.class, email);

		return tenantCount;
	}

	public void deleteTenantResolver(String email, String orgId) {

		String sql = MessageFormat.format("DELETE  FROM tenant_resolver WHERE email = ''{0}'' AND org_id = ''{1}''",
				email, orgId);
		jdbcTemplate.execute(sql);
	}

	public void deleteUserFromTenantResolver(String email) {

		String sql = MessageFormat.format("DELETE  FROM user WHERE email = ''{0}''", email);
		jdbcTemplate.execute(sql);
	}

	/**
	 * insert into plustus org info after signup
	 */
	public boolean savePlustusOrgInfo(PlutusOrgDTO plutusOrgDTO) {

		if (plutusOrgDTO == null)
			return false;

		logger.debug("Initiating save plutus org info org {}", plutusOrgDTO.getOrgName());
		String insertSql = "INSERT INTO `plutusorganizationinfo`(`org_id`,`creation_date`,`modification_date`,`org_name`,`stripeAccnId`,`subscriptionId`,`subscriptionKey`,`updatedDate`,`planName`)VALUES(?,?,?,?,?,?,?,?,?)";
		int rowCount = 0;

		rowCount = jdbcTemplate.update(insertSql,
				new Object[] { plutusOrgDTO.getOrgId(), DateTime.now().toDate(), DateTime.now().toDate(),
						plutusOrgDTO.getOrgName(), plutusOrgDTO.getStripeAccnId(), plutusOrgDTO.getSubscryptionId(),
						plutusOrgDTO.getSubscryptionKey(), plutusOrgDTO.getUpdatedDate(), plutusOrgDTO.getPlanName() });

		if (rowCount > 0) {
			logger.debug("Success saving data to tenant resolver for orgId and org {} ", plutusOrgDTO.getOrgName(),
					plutusOrgDTO.getOrgId());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	/**
	 * insert into plustus org info after signup
	 */
	public boolean updatePlutusOrgInfo(PlutusOrgDTO plutusOrgDTO) {

		if (plutusOrgDTO == null)
			return false;

		logger.debug("Initiating update plutus org info org {}", plutusOrgDTO.getOrgName());
		String updateSql = "UPDATE `plutusorganizationinfo` SET `stripeAccnId` = ?,`orgType` = ?,`modification_date` = ?,`subscriptionId` = ?,`subscriptionKey` = ?,`updatedDate` = ?,`planId`=?,`planName`=?,`subscriptionMode`=? WHERE `org_id` = ?";
		int rowCount = 0;

		rowCount = jdbcTemplate.update(updateSql,
				new Object[] { plutusOrgDTO.getStripeAccnId(), plutusOrgDTO.getOrgType(), DateTime.now().toDate(),
						plutusOrgDTO.getSubscryptionId(), plutusOrgDTO.getSubscryptionKey(),
						plutusOrgDTO.getUpdatedDate(), plutusOrgDTO.getPlanId(), plutusOrgDTO.getPlanName(),
						plutusOrgDTO.getSubscriptionMode(), plutusOrgDTO.getOrgId() });

		if (rowCount > 0) {
			logger.debug("Success saving data to tenant resolver for org and orgId {} ", plutusOrgDTO.getOrgName(),
					plutusOrgDTO.getOrgId());
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	/**
	 * to get plutus org info stored in tenant mgmt db
	 * 
	 * @param orgId
	 * @return
	 */
	public PlutusOrganizationInfo findPlutusOrgInfo(String orgId) {
		PlutusOrganizationInfo orgInfo;
		try {
			String sql = "SELECT * FROM plutusorganizationinfo where org_id = '" + orgId + "'";

			orgInfo = jdbcTemplate.queryForObject(sql, new RowMapper<PlutusOrganizationInfo>() {

				@Override
				public PlutusOrganizationInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
					PlutusOrganizationInfo orgInfo = new PlutusOrganizationInfo();
					orgInfo.setCreationDate(rs.getTimestamp("creation_date"));
					orgInfo.setModificationDate(rs.getTimestamp("modification_date"));
					orgInfo.setOrgEmail(rs.getString("orgEmail"));
					orgInfo.setOrgId(rs.getString("org_id"));
					orgInfo.setOrgName(rs.getString("org_name"));
					orgInfo.setPlanId(rs.getString("planId"));
					orgInfo.setStripeAccnId(rs.getString("stripeAccnId"));
					orgInfo.setSubscriptionId(rs.getString("subscriptionId"));
					orgInfo.setSubscriptionKey(rs.getString("subscriptionKey"));
					orgInfo.setUpdatedDate(rs.getTimestamp("updatedDate"));
					orgInfo.setPlanName(rs.getString("planName"));
					orgInfo.setSubscriptionMode(rs.getString("subscriptionMode"));
					return orgInfo;
				}
			});
		} catch (DataAccessException e) {
			logger.debug(e.getMessage(), e);
			return null;
		}
		return orgInfo;

	}

	/**
	 * to update recruiz plan id
	 * 
	 * @param orgId
	 * @param planID
	 * @return
	 */
	public boolean updateRecuizPlanId(String orgId, String planID, String planName, String subscriptionMode) {
		String updateSql = "UPDATE `plutusorganizationinfo` SET `planId` = ?,`modification_date` = ?,`updatedDate` = ?,`planName`=?,`subscriptionMode`=? WHERE `org_id` = ?";
		int rowCount = 0;

		rowCount = jdbcTemplate.update(updateSql, new Object[] { planID, DateTime.now().toDate(),
				DateTime.now().toDate(), planName, subscriptionMode, orgId });

		if (rowCount > 0) {
			logger.debug("Success updating plan id for orgId {} ", orgId);
		}

		if (rowCount > 0)
			return true;
		else
			return false;
	}

	// this method will check if mobile is already registered
	public boolean userMobilenumberExists(String mobileNumber) {
		Integer cnt = jdbcTemplate.queryForObject("SELECT count(*) FROM user WHERE mobile = ?", Integer.class,
				mobileNumber);
		boolean status = cnt != null && cnt > 0;
		return status;
	}

	public User findOneUserFromTenant(String orgId) {
		User user = null;
		try {
			String sql = "SELECT distinct(*) FROM tenant_resolver where org_id = '" + orgId + "'";

			user = jdbcTemplate.queryForObject(sql, new RowMapper<User>() {

				@Override
				public User mapRow(ResultSet rs, int rowNum) throws SQLException {
					User user = new User();
					user.setEmail(rs.getString("email"));
					return user;
				}

			});
		} catch (DataAccessException e) {
			// do nothing
		}

		return user;
	}

	public boolean changeOrgName(String tenant, String orgNewName) {
		String updateSql = "UPDATE `tenant_resolver` SET `organization_name` = ? WHERE `org_id` = ?";
		int rowCount = 0;

		rowCount = jdbcTemplate.update(updateSql, new Object[] { orgNewName, tenant });

		if (rowCount > 0) {
			logger.debug("Success updating organization name for orgId {} ", tenant);
		}

		if (rowCount > 0)
			return true;
		else
			return false;

	}

	/**
	 * to save tenant id and message id map for campaign sent email
	 * 
	 * @param tenant
	 * @param messageId
	 */
	public boolean saveTenantAndMessageIdMap(String tenant, String messageId) {

		String insertSql = "INSERT INTO `campaign_email_tenant_map`(`tenantId`,`emailMessageId`)VALUES(?,?)";
		int rowCount = 0;
		rowCount = jdbcTemplate.update(insertSql, new Object[] { tenant, messageId });
		if (rowCount > 0)
			return true;
		else
			return false;

	}

	/**
	 * fetch and return the tenant from campaign_email_tenant_map table for the
	 * given message id
	 * 
	 * @param messageId
	 * @return
	 */
	public String getTenantForMessageId(String messageId) {
		try {
			String sql = "SELECT distinct tenantId FROM campaign_email_tenant_map where LOWER(emailMessageId) = LOWER('"
					+ messageId + "')";
			String tenantId = jdbcTemplate.queryForObject(sql, String.class);
			return tenantId;
		} catch (DataAccessException e) {
			// do nothing
		}
		return null;
	}

}
