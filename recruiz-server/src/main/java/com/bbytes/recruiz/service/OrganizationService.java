package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterDetails;
import com.bbytes.recruiz.domain.OfferLetterForCandidate;
import com.bbytes.recruiz.domain.OfferLetterTemplateVariables;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.SelectedCustomField;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.OfferLetterVariable;
import com.bbytes.recruiz.enums.UserType;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.OfferLetterDetailsRepository;
import com.bbytes.recruiz.repository.OfferLetterForCandidateRepository;
import com.bbytes.recruiz.repository.OfferLetterVariableRepository;
import com.bbytes.recruiz.repository.OrganizationRepository;
import com.bbytes.recruiz.repository.SelectedCustomReportsRepository;
import com.bbytes.recruiz.rest.dto.models.CandidateApplyFormFieldsDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterFormulaDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterResponseDTO;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.utils.AppSettingsGenerator;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.PermissionConstant;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class OrganizationService extends AbstractService<Organization, String> {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

	private OrganizationRepository organizationRepository;

	@Autowired
	private FileFormatConversionService fileFormatConversionService;

	@Autowired
	private SelectedCustomReportsRepository selectedCustomReportsRepository;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	OfferLetterForCandidateRepository OfferLetterForCandidateRepository;

	@Autowired
	OrganizationService organizationService;

	@Autowired
	OfferLetterDetailsRepository offerLetterDetailsRepository;

	@Autowired
	OfferLetterVariableRepository offerLetterVariableRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Autowired
	private RecruizPlutusClientService recruizPlutusClientService;

	@Value("${base.url}")
	private String baseUrl;

	@Autowired
	private InvoiceSettingsService invoiceSettingsService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService;

	@Autowired
	public OrganizationService(OrganizationRepository organizationRepository) {
		super(organizationRepository);
		this.organizationRepository = organizationRepository;
	}

	public Organization findByOrgId(String orgId) {
		return organizationRepository.findByOrgId(orgId);
	}

	@Transactional
	public Organization getOrgInfo() {
		return userService.getLoggedInUserObject().getOrganization();
	}


	public Organization getCurrentOrganization() {
		return organizationRepository.findByOrgId(TenantContextHolder.getTenant());
	}

	public String getOrgUrl(Organization org) {
		if (org.getLogoUrlPath() != null && !org.getLogoUrlPath().isEmpty()) {
			String url = baseUrl + "/pubset/" + org.getLogoUrlPath();
			return url;
		}
		return null;
	}

	public boolean orgIdExist(String orgName) {
		boolean state = organizationRepository.findByOrgName(orgName) == null ? false : true;
		return state;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createOrganizationWithSocialUser(String orgID, String orgName, String email, String orgType)
			throws RecruizException {
		try {

			Organization organization = createOrg(orgID, orgName, orgType);

			// Saving this organization as a user
			User user = userService.createForSocialSignUp(email, orgName, organization,
					userRoleService.getRolesByName(GlobalConstants.SUPER_ADMIN_USER_ROLE));

			// making entry in tenant_resolver and tenant_mgmt user table
			tenantResolverService.saveTenantResolverForUser(user);
		} catch (Exception re) {
			re.printStackTrace();
			throw new RecruizException(re);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createOrganizationWithUser(String orgID, String orgName, String email, String password, String orgType,
			String timeZone, String locale, String mobile) throws RecruizException {

		try {

			Organization organization = createOrg(orgID, orgName, orgType);

			// Saving this user as organization admin
			User user = userService.create(email, orgName, password, organization,
					userRoleService.getRolesByName(GlobalConstants.SUPER_ADMIN_USER_ROLE), timeZone, locale,
					UserType.APP.getDisplayName(), mobile);

			// making entry in tenant_resolver and tenant_mgmt user table
			tenantResolverService.saveTenantResolverForUser(user);

		} catch (Exception re) {
			throw new RecruizException(re);
		}
	}

	@Transactional
	private Organization createOrg(String orgID, String orgName, String orgType)
			throws RecruizException, IOException, ParseException {
		Organization organization = saveOrganizationToDB(orgID, orgName, orgType);

		/***************************
		 * Org Admin User Role created here
		 *******************/
		Permission permission = new Permission(PermissionConstant.SUPER_ADMIN);
		List<Permission> orgAdminPermissionList = getPermissionListForHRManager();
		orgAdminPermissionList.add(new Permission(PermissionConstant.ADMIN_SETTING));
		orgAdminPermissionList.add(permission);

		UserRole organizationRole = new UserRole();
		organizationRole.getPermissions().addAll(orgAdminPermissionList);
		organizationRole.setRoleName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
		if (userRoleService.getRolesByName(organizationRole.getRoleName()) == null)
			userRoleService.save(organizationRole);

		/***************************
		 * Normal User Role created here
		 *******************/
		permission = new Permission(PermissionConstant.NORMAL);
		UserRole noramlRole = new UserRole();
		noramlRole = new UserRole();
		noramlRole.getPermissions().add(permission);
		noramlRole.setRoleName(GlobalConstants.NORMAL_USER_ROLE);
		if (userRoleService.getRolesByName(noramlRole.getRoleName()) == null)
			userRoleService.save(noramlRole);

		/***************************
		 * HR Executive Role created here
		 *******************/
		permission = new Permission();
		List<Permission> hrPermissionList = new ArrayList<Permission>();
		hrPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_CANDIDATE));
		hrPermissionList.add(new Permission(PermissionConstant.DELETE_CANDIDATE));
		hrPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_CLIENT));
		hrPermissionList.add(new Permission(PermissionConstant.DELETE_CLIENT));
		hrPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_POSITION));
		hrPermissionList.add(new Permission(PermissionConstant.DELETE_POSITION));
		hrPermissionList.add(new Permission(PermissionConstant.VIEW_EDIT_BOARD));
		hrPermissionList.add(new Permission(PermissionConstant.VIEW_All_CANDIDATES));

		UserRole hrRole = new UserRole();
		hrRole.getPermissions().addAll(hrPermissionList);
		hrRole.setRoleName(GlobalConstants.HR_EXEC_USER_ROLE);
		if (userRoleService.getRolesByName(hrRole.getRoleName()) == null)
			userRoleService.save(hrRole);

		/***************************
		 * HR Manager Role created here
		 *******************/
		permission = new Permission();
		List<Permission> hrManagerPermissionList = getPermissionListForHRManager();

		UserRole hrManagerRole = new UserRole();
		hrManagerRole.getPermissions().addAll(hrManagerPermissionList);
		hrManagerRole.setRoleName(GlobalConstants.HR_MANAGER_USER_ROLE);
		if (userRoleService.getRolesByName(hrManagerRole.getRoleName()) == null)
			userRoleService.save(hrManagerRole);

		/***************************
		 * Department Head Role created here
		 *******************/
		List<Permission> deptartmentHeadPermissions = new ArrayList<Permission>();
		deptartmentHeadPermissions.add(new Permission(PermissionConstant.ADD_EDIT_POSITION));

		UserRole departmentHead = new UserRole();
		departmentHead.getPermissions().addAll(deptartmentHeadPermissions);
		departmentHead.setRoleName(GlobalConstants.DEPARTMENT_HEAD_USER_ROLE);
		if (userRoleService.getRolesByName(departmentHead.getRoleName()) == null)
			userRoleService.save(departmentHead);

		/***************************
		 * IT admin Role created here
		 *******************/
		List<Permission> itAdminPermissions = new ArrayList<Permission>();
		itAdminPermissions.add(new Permission(PermissionConstant.ADD_EDIT_USER));
		itAdminPermissions.add(new Permission(PermissionConstant.ADD_EDIT_USER_ROLES));
		itAdminPermissions.add(new Permission(PermissionConstant.DELETE_USER_ROLES));
		itAdminPermissions.add(new Permission(PermissionConstant.DELETE_USER));
		itAdminPermissions.add(new Permission(PermissionConstant.IT_ADMIN));
		itAdminPermissions.add(new Permission(PermissionConstant.ADMIN_SETTING));

		UserRole itAdmin = new UserRole();
		itAdmin.getPermissions().addAll(itAdminPermissions);
		itAdmin.setRoleName(GlobalConstants.IT_ADMIN_USER_ROLE);
		if (userRoleService.getRolesByName(itAdmin.getRoleName()) == null)
			userRoleService.save(itAdmin);

		return organization;
	}

	// get permission list for HRManager
	private List<Permission> getPermissionListForHRManager() {
		List<Permission> hrManagerPermissionList = new ArrayList<Permission>();
		hrManagerPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_CANDIDATE));
		hrManagerPermissionList.add(new Permission(PermissionConstant.DELETE_CANDIDATE));
		hrManagerPermissionList.add(new Permission(PermissionConstant.VIEW_All_CANDIDATES));
		hrManagerPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_CLIENT));
		hrManagerPermissionList.add(new Permission(PermissionConstant.DELETE_CLIENT));
		hrManagerPermissionList.add(new Permission(PermissionConstant.ADD_EDIT_POSITION));
		hrManagerPermissionList.add(new Permission(PermissionConstant.DELETE_POSITION));
		hrManagerPermissionList.add(new Permission(PermissionConstant.VIEW_EDIT_BOARD));
		hrManagerPermissionList.add(new Permission(PermissionConstant.VIEW_All_PROSPECTS));
		hrManagerPermissionList.add(new Permission(PermissionConstant.GLOBAL_EDIT));
		hrManagerPermissionList.add(new Permission(PermissionConstant.GLOBAL_DELETE));
		hrManagerPermissionList.add(new Permission(PermissionConstant.MANAGER_SETTING));
		hrManagerPermissionList.add(new Permission(PermissionConstant.VIEW_REPORTS));
		hrManagerPermissionList.add(new Permission(PermissionConstant.CAMPAIGN_FUNCTION));
		hrManagerPermissionList.add(new Permission(PermissionConstant.GENERATE_INVOICE));
		hrManagerPermissionList.add(new Permission(PermissionConstant.CAREER_SITE));
		hrManagerPermissionList.add(new Permission(PermissionConstant.VIEW_All_CANDIDATES));
		return hrManagerPermissionList;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private Organization saveOrganizationToDB(String orgID, String orgName, String orgType)
			throws IOException, ParseException {
		/*** Add configuration info to org ****/
		OrganizationConfiguration config = new OrganizationConfiguration();
		config.setSettingInfo(AppSettingsGenerator.generateAndSaveSettings(orgID));

		Organization organization = new Organization();
		organization.setOrgId(orgID);
		organization.setOrgName(orgName);
		organization.setOrgType(orgType);
		organization.setOrganizationConfiguration(config);
		organization = save(organization);
		return organization;
	}

	@Transactional
	public void deleteOrganization(Organization org) {
		organizationRepository.delete(org);
	}

	@Transactional
	public boolean updateOrganization(String orgID, OrganizationUserDTO orgDTO, MultipartFile file)
			throws RecruizException, IOException {
		Organization org = organizationRepository.findByOrgId(orgID);
		if (org != null) {
			org.setOrgName(orgDTO.getOrgName());
			// org.setLogoUrlPath((orgDTO.getLogoUrlPath()));
			org.setWebsiteUrl(orgDTO.getWebsiteUrl());
			org.setFacebookUrl(orgDTO.getFacebookUrl());
			org.setTwitterUrl(orgDTO.getTwitterUrl());
			org.setGoogleUrl(orgDTO.getGoogleUrl());
			org.setLinkedInUrl(orgDTO.getLinkedInUrl());
			org.setSlackUrl(orgDTO.getSlackUrl());
			org.setGitHubUrl(orgDTO.getGitHubUrl());
			org.setHipChatUrl(orgDTO.getHipChatUrl());
			org.setBitBucketUrl(orgDTO.getBitBucketUrl());
			org.setGstId(orgDTO.getGstNo());
			org.setTaxRegistrationId(orgDTO.getTaxRegistrationNo());
			org.setAddress(orgDTO.getAddress());
			org.setState(orgDTO.getState());
			org.setPanNo(orgDTO.getPanNo());
			org.setGstId(orgDTO.getGstNo());
			org.setAddressL1(orgDTO.getAddressL1());
			org.setAddressL2(orgDTO.getAddressL2());
			org.setCity(orgDTO.getCity());
			org.setCountry(orgDTO.getCountry());
			org.setPhone(orgDTO.getPhone());
			org.setPincode(orgDTO.getPincode());
			org.setCandidateModificationDays(orgDTO.getCandidateNodificationDate());

			// String imageEcodedString = orgDTO.getImageByteString();
			if (file != null && !file.isEmpty()) {
				byte[] imgArray = file.getBytes(); // Base64.decode(imageEcodedString);

				if (imgArray != null && imgArray.length > 0) {
					File tenantFolderPath = new File(publicFolder + "/" + TenantContextHolder.getTenant());

					if (!tenantFolderPath.exists())
						FileUtils.forceMkdir(tenantFolderPath);

					File logoFile = new File(tenantFolderPath + "/" + orgDTO.getFileName());

					FileUtils.writeByteArrayToFile(logoFile, imgArray);
					String logoPath = logoFile.getAbsolutePath().replace(publicFolder, "");
					// String logoPath =
					// logoFile.getAbsolutePath().substring(index,
					// logoFile.getAbsolutePath().length());
					org.setLogoUrlPath(logoPath);
				}
			}
			save(org);
			InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			}

			/*
			 * Map<String, String> taxRelatedDetails = new HashMap<String,
			 * String>(); if (orgDTO.getGstNo() != null) {
			 * taxRelatedDetails.put("GSTIN", orgDTO.getGstNo()); } if
			 * (orgDTO.getPanNo() != null) { taxRelatedDetails.put("PAN",
			 * orgDTO.getPanNo()); }
			 * 
			 * if (!taxRelatedDetails.isEmpty()) {
			 * invoiceSettings.setTaxRelatedDetails(taxRelatedDetails); }
			 */
			invoiceSettings.setOrganizationName(orgDTO.getOrgName());
			invoiceSettings.setOrganization_address_1(orgDTO.getAddressL1());
			invoiceSettings.setOrganization_address_2(orgDTO.getAddressL2());
			invoiceSettings.setOrganizationCity(orgDTO.getCity());
			invoiceSettings.setOrganizationState(orgDTO.getState());
			invoiceSettings.setOrganizationCountry(orgDTO.getCountry());
			invoiceSettings.setOrganizationPin(orgDTO.getPincode());
			invoiceSettings.setOrganizationPhone(orgDTO.getPhone());
			invoiceSettingsService.save(invoiceSettings);

			/*
			 * if (taxService.isTaxNameExist("GSTIN")) { Tax taxGstInFromDb =
			 * taxService.getByTaxName("GSTIN"); if (orgDTO.getGstNo() != null
			 * && !orgDTO.getGstNo().isEmpty()) {
			 * taxGstInFromDb.setTaxNumber(orgDTO.getGstNo());
			 * taxService.save(taxGstInFromDb); } } else { if (orgDTO.getGstNo()
			 * != null && !orgDTO.getGstNo().isEmpty()) { Tax gstTax = new
			 * Tax(); gstTax.setTaxName("GSTIN");
			 * gstTax.setTaxNumber(orgDTO.getGstNo()); taxService.save(gstTax);
			 * } }
			 * 
			 * if (taxService.isTaxNameExist("PAN")) { Tax taxPanFromDb =
			 * taxService.getByTaxName("PAN"); if (orgDTO.getPanNo() != null &&
			 * !orgDTO.getPanNo().isEmpty()) {
			 * taxPanFromDb.setTaxNumber(orgDTO.getPanNo());
			 * taxService.save(taxPanFromDb); } } else { if (orgDTO.getPanNo()
			 * != null && !orgDTO.getPanNo().isEmpty()) { Tax panTax = new
			 * Tax(); panTax.setTaxName("PAN");
			 * panTax.setTaxNumber(orgDTO.getPanNo()); taxService.save(panTax);
			 * } }
			 */

			return true;
		}
		throw new RecruizException("Organization doesn't exists", ErrorHandler.org_update_failed);
	}

	/**
	 * This method is used to set mark for delete organization, it will delete
	 * complete org after provided number of days
	 * 
	 * @author Akshay
	 * 
	 * @param markForDeleteState
	 * @param orgId
	 * @param hours
	 * @throws RecruizWarnException
	 * @throws PlutusClientException
	 */
	@Transactional(rollbackFor = { PlutusClientException.class })
	public Organization markforDeleteOrganization(boolean markForDeleteState, String orgId, int hours)
			throws RecruizWarnException, PlutusClientException {
		if (!checkUserPermission.hasAdminSettingPermission())
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		Organization organization = null;
		organization = organizationRepository.findByOrgId(orgId);
		if (organization != null) {
			// markForDeleteState=true means set organization as mark for delete
			organization.setMarkForDelete(markForDeleteState);
			if (markForDeleteState)
				organization.setMarkForDeleteDate(DateTime.now().plusHours(hours).toDate());
			else
				organization.setMarkForDeleteDate(null);

			ResponseEntity<RestResponse> response = recruizPlutusClientService.markForDeleteOrganization(organization);
			if (response.getBody().isSuccess())
				organization = organizationRepository.save(organization);
			else
				throw new RecruizWarnException(ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS,
						ErrorHandler.ORGANIZATION_NOT_FOUND);

		} else {
			throw new RecruizWarnException(ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS,
					ErrorHandler.ORGANIZATION_NOT_FOUND);
		}
		return organization;
	}

	/**
	 * @param markForDeleteState
	 * @param orgId
	 * @param hours
	 * @return
	 * @throws RecruizWarnException
	 */
	@Transactional
	public Organization setMarkDeleteOrgnization(boolean markForDeleteState, int hours) throws RecruizWarnException {
		Organization organization = null;
		organization = this.getCurrentOrganization();
		if (organization != null) {
			// markForDeleteState=true means set organization as mark for delete
			organization.setMarkForDelete(markForDeleteState);
			if (markForDeleteState)
				organization.setMarkForDeleteDate(DateTime.now().plusHours(hours).toDate());
			else
				organization.setMarkForDeleteDate(null);
			organization = organizationRepository.save(organization);

		} else {
			throw new RecruizWarnException(ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS,
					ErrorHandler.ORGANIZATION_NOT_FOUND);
		}
		return organization;
	}

	/**
	 * This method is used to disable the organization
	 * 
	 * @param disableStatus
	 * @param orgId
	 * @return
	 * @throws RecruizWarnException
	 */
	@Transactional
	public Organization disableOrgnization(boolean disableStatus, String disableReason) throws RecruizWarnException {
		Organization organization = null;
		organization = this.getCurrentOrganization();
		if (organization != null) {
			organization.setDisableStatus(disableStatus);
			// if account is disable then storing reason for the same
			if (disableStatus)
				organization.setDisableReason(disableReason);
			else
				organization.setDisableReason(null);
			organization = organizationRepository.save(organization);
		} else {
			throw new RecruizWarnException(ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS,
					ErrorHandler.ORGANIZATION_NOT_FOUND);
		}
		return organization;
	}

	/**
	 * to update organization with given settings
	 * 
	 * @param settings
	 * @param tenant
	 * @return
	 */
	@Transactional
	public Map<String, Object> updateOrganizationSettings(String settings) {

		Map<String, Object> configSettingMap = new HashMap<String, Object>();
		try {
			Organization org = organizationRepository.findByOrgId(TenantContextHolder.getTenant());
			OrganizationConfiguration config = org.getOrganizationConfiguration();
			config.setSettingInfo(settings);
			organizationRepository.save(org);

			configSettingMap.put("Organization", org.getOrgName());
			configSettingMap.put("settings", settings);
			configSettingMap.put("updatedDate", new Date());
		} catch (Exception e) {
			configSettingMap.put("errorMessage", e.getMessage());
			configSettingMap.put("error", ErrorHandler.NO_ORG_ID);
			configSettingMap.put("updatedDate", new Date());
			logger.error(e.getMessage(), e);
		}
		return configSettingMap;
	}

	@Transactional
	public boolean activateAccount() {
		try {
			List<User> users = userService.findAll();
			if (users != null && users.size() > 1) {
				return false;
			} else if (users != null) {
				User firstUser = users.get(0);
				firstUser.setJoinedStatus(true);
				userService.save(firstUser);
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Transactional
	public boolean deactivateAccount() {
		try {
			Organization org = organizationRepository.findByOrgId(TenantContextHolder.getTenant());
			org.setDisableStatus(true);
			organizationRepository.save(org);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Transactional(readOnly = true)
	public boolean isAgency() {
		if (getCurrentOrganization().getOrgType().equalsIgnoreCase(GlobalConstants.ORG_TYPE_AGENCY))
			return true;

		return false;
	}

	/****************************************
	 * To update the address of organization*
	 ****************************************
	 * @param orgID
	 * @param orgDTO
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	@Transactional
	public boolean updateOrganizationAddress(String orgID, OrganizationUserDTO orgDTO)
			throws RecruizException, IOException {
		Organization org = organizationRepository.findByOrgId(orgID);
		if (org != null) {
			org.setState(orgDTO.getState());
			org.setAddressL1(orgDTO.getAddressL1());
			org.setAddressL2(orgDTO.getAddressL2());
			org.setCity(orgDTO.getCity());
			org.setCountry(orgDTO.getCountry());
			org.setPhone(orgDTO.getPhone());
			org.setPincode(orgDTO.getPincode());
			save(org);
			// Adding entry in InvoiceSetting while update the organization
			// Address
			InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			}
			invoiceSettings.setOrganization_address_1(orgDTO.getAddressL1());
			invoiceSettings.setOrganization_address_2(orgDTO.getAddressL2());
			invoiceSettings.setOrganizationCity(orgDTO.getCity());
			invoiceSettings.setOrganizationState(orgDTO.getState());
			invoiceSettings.setOrganizationCountry(orgDTO.getCountry());
			invoiceSettings.setOrganizationPin(orgDTO.getPincode());
			invoiceSettings.setOrganizationPhone(orgDTO.getPhone());
			invoiceSettingsService.save(invoiceSettings);
		}
		return true;
	}


	@Transactional(readOnly=true)
	public void sendHybridUsageStat() throws RecruizException {

		Long candidateCount = candidateService.count();
		Long totalUserCount = userService.count();
		Long disabledUserCount = userService.getUserCountByAccountStatus(false);
		Long joinedUserCount = userService.getUserCountByJoinedStatus(true);
		Long clientCount = clientService.count();
		Long positionCount = positionService.count();

		String template = "email-template-hybrid-stat.html";
		Map<String,Object> emailBody = new HashMap<>();
		emailBody.put("totalUser", totalUserCount);
		emailBody.put("joinedUser", joinedUserCount);
		emailBody.put("disabledUser", disabledUserCount);
		emailBody.put("candidate", candidateCount);
		emailBody.put("position", positionCount);
		emailBody.put("client", clientCount);
		emailBody.put("orgName", getCurrentOrganization().getOrgName());



		String templateString = emailTemplateDataService.getHtmlContentFromFile(emailBody, template);
		String renderedTemplateString = emailTemplateDataService.getMasterTemplateWithoutButton(templateString);
		List<String> notifyTo =  new ArrayList<>();
		notifyTo.add("recruiz@beyondbytes.co.in");
		//	    notifyTo.add("sourav@beyondbytes.co.in");

		emailService.sendEmail(notifyTo, renderedTemplateString, "Usage Stat For " + TenantContextHolder.getTenant());


	}

	public RestResponse uploadOfferLetterAndReadVaraibles(File offerFile, String offerTemplateName) {


		OfferLetterDetails offerDetails = offerLetterDetailsRepository.findByTemplateName(offerTemplateName);

		if(offerDetails!=null){
			if(offerDetails.getAnnuallyCtcFormula()==null && offerDetails.getAnnuallyDeductionsFormula()==null && 
					offerDetails.getMonthlyCtcFormula()==null && offerDetails.getMonthlyDeductionsFormula()==null){

				offerLetterDetailsRepository.delete(offerDetails.getOfferLetterId());
			}else{
				return new RestResponse(false, null, "Duplicate Template name, Try with different template name");
			}

		}

		RestResponse response = null; 
		if(offerFile!=null){
			response = readAndgetVariablesFromFile(offerFile, offerTemplateName); 
		}
		return response;	
	}

	private RestResponse readAndgetVariablesFromFile(File offerFile, String offerTemplateName) {

		OfferLetterDetails response = null;
		LinkedHashSet<String>  textList = new LinkedHashSet<String>();
		OfferLetterTemplateVariables templateVariable = null;

		//String addSpaceTextVariables = ""; 
		//	int x=0;

		XWPFDocument doc;
		try {
			doc = new XWPFDocument(
					OPCPackage.open(offerFile));

			for (XWPFParagraph p : doc.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {
					for (XWPFRun r : runs) {
						String text = r.getText(0);
						templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.text.toString());
						if (text != null && templateVariable!=null) {
							String varaible = StringUtils.substringBetween(text, templateVariable.getStartTag(), templateVariable.getEndTag());
							if(varaible!=null)
								textList.add(varaible);
							/*	}else if(text.trim().equalsIgnoreCase(templateVariable.getStartTag())){
								x = 1;
							}else if(text.trim().equalsIgnoreCase(templateVariable.getEndTag())){
								x = 0;
								addSpaceTextVariables = addSpaceTextVariables + text.trim();
								String varaibleData = StringUtils.substringBetween(addSpaceTextVariables, templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaibleData!=null)
									textList.add(varaibleData);
								addSpaceTextVariables = "";
							}

							if(x==1){
								addSpaceTextVariables = addSpaceTextVariables + text.trim();
							}*/
						}
					}
				}
			}

			List<XWPFTable> table = doc.getTables();
			response = readWordDocumentTables(table, textList, templateVariable,offerFile, offerTemplateName);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new RestResponse(RestResponse.SUCCESS, response);
	}



	private OfferLetterDetails readWordDocumentTables(List<XWPFTable> table, LinkedHashSet<String> textList, OfferLetterTemplateVariables templateVariable, File offerFile, String offerTemplateName) throws IOException{

		LinkedHashSet<String>  componentList = new LinkedHashSet<String>();
		LinkedHashSet<String>  deductionList = new LinkedHashSet<String>();
		LinkedHashSet<String>  calculationList = new LinkedHashSet<String>();
		String monthlyGross = null;
		String annuallyGross = null;
		String annuallyDeductions = null;
		String monthlyDeductions = null;
		String annuallyCtc = null;
		String monthlyCtc = null;
		String finalMonthlyCtc = null;
		String finalAnnuallyCtc = null;

		for (XWPFTable xwpfTable : table) {
			List<XWPFTableRow> row = xwpfTable.getRows();
			for (XWPFTableRow xwpfTableRow : row) {
				List<XWPFTableCell> cell = xwpfTableRow.getTableCells();
				for (XWPFTableCell xwpfTableCell : cell) {
					if(xwpfTableCell!=null)
					{
						if (xwpfTableCell.getText() != null) {

							// Text Variables
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.text.toString());
							if(templateVariable!=null){
								String varaibles = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaibles!=null)
									textList.add(varaibles);
							}

							// Component Variables
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.component.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									componentList.add(varaible);
							}

							// Deduction Variables
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.deduction.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									deductionList.add(varaible);
							}

							// Calculation Variables
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.calculation.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									calculationList.add(varaible);
							}

							// monthlyGross variable
							OfferLetterTemplateVariables templateMonGross = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyGross.toString());
							if(templateMonGross!=null){
								String varaibleMGRoss = StringUtils.substringBetween(xwpfTableCell.getText(),templateMonGross.getStartTag(), templateMonGross.getEndTag());
								if(varaibleMGRoss!=null)
									monthlyGross = varaibleMGRoss;
							}

							// annuallyGross variable
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyGross.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									annuallyGross = varaible;
							}

							// annuallyDeductions variable
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyDeductions.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									annuallyDeductions = varaible;
							}

							// monthlyDeductions variable
							OfferLetterTemplateVariables templateMonDed = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyDeductions.toString());
							if(templateMonDed!=null){
								String varaibleMonDed = StringUtils.substringBetween(xwpfTableCell.getText(),templateMonDed.getStartTag(), templateMonDed.getEndTag());
								if(varaibleMonDed!=null)
									monthlyDeductions = varaibleMonDed;
							}

							// annuallyCtc variable
							templateVariable = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyCtc.toString());
							if(templateVariable!=null){
								String varaible = StringUtils.substringBetween(xwpfTableCell.getText(),templateVariable.getStartTag(), templateVariable.getEndTag());
								if(varaible!=null)
									annuallyCtc = varaible;
							}

							// monthlyCtc variable
							OfferLetterTemplateVariables templateMonCtc = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyCtc.toString());
							if(templateMonCtc!=null){
								String varaibleMonCtc = StringUtils.substringBetween(xwpfTableCell.getText(),templateMonCtc.getStartTag(), templateMonCtc.getEndTag());
								if(varaibleMonCtc!=null)
									monthlyCtc = varaibleMonCtc;
							}

							// finalMonthlyCtc variable
							OfferLetterTemplateVariables templateFinalMCtc = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.finalCostMonthlyCtc.toString());
							if(templateFinalMCtc!=null){
								String varaibleFinalCtc = StringUtils.substringBetween(xwpfTableCell.getText(),templateFinalMCtc.getStartTag(), templateFinalMCtc.getEndTag());
								if(varaibleFinalCtc!=null)
									finalMonthlyCtc = varaibleFinalCtc;
							}

							// finalAnnuallyCtc variable
							OfferLetterTemplateVariables templateFinalACtc = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.finalCostAnnuallyCtc.toString());
							if(templateFinalACtc!=null){
								String varaibleFinalCtc = StringUtils.substringBetween(xwpfTableCell.getText(),templateFinalACtc.getStartTag(), templateFinalACtc.getEndTag());
								if(varaibleFinalCtc!=null)
									finalAnnuallyCtc = varaibleFinalCtc;
							}
						}

					}
				}
			}
		}
		return saveTemplateVariableDetailsInDatabase(textList,componentList,deductionList,calculationList,monthlyGross,
				annuallyGross,annuallyDeductions,monthlyDeductions,annuallyCtc,monthlyCtc,offerFile, offerTemplateName,finalMonthlyCtc,finalAnnuallyCtc);
	}


	private OfferLetterDetails saveTemplateVariableDetailsInDatabase(LinkedHashSet<String> textList,
			LinkedHashSet<String> componentList, LinkedHashSet<String> deductionList,
			LinkedHashSet<String> calculationList, String monthlyGross, String annuallyGross, String annuallyDeductions,
			String monthlyDeductions, String annuallyCtc, String monthlyCtc, File offerFile, String offerTemplateName, String finalMonthlyCtc, String finalAnnuallyCtc) throws IOException {

		OfferLetterDetails response = new OfferLetterDetails();
		String textVariable="";
		String componentVariable="";
		String deductionVariable="";
		String calculationVariable="";

		int i=0;
		for (String variable : textList) {
			if(i==0){
				textVariable = variable;
				i=1;
			}else{
				textVariable = textVariable+","+variable;
			}
		}

		i=0;
		for (String variable : componentList) {
			if(i==0){
				componentVariable = variable;
				i=1;
			}else{
				componentVariable = componentVariable+","+variable;
			}
		}

		i=0;
		for (String variable : deductionList) {
			if(i==0){
				deductionVariable = variable;
				i=1;
			}else{
				deductionVariable = deductionVariable+","+variable;
			}
		}

		i=0;
		for (String variable : calculationList) {
			if(i==0){
				calculationVariable = variable;
				i=1;
			}else{
				calculationVariable = calculationVariable+","+variable;
			}
		}

		response.setText(textVariable);
		response.setComponent(componentVariable);
		response.setDeduction(deductionVariable);
		response.setCalculation(calculationVariable);
		response.setMonthlyGross(monthlyGross);
		response.setAnnuallyGross(annuallyGross);
		response.setMonthlyDeductions(monthlyDeductions);
		response.setAnnuallyDeductions(annuallyDeductions);
		response.setAnnuallyCtc(annuallyCtc);
		response.setMonthlyCtc(monthlyCtc);
		response.setTemplateName(offerTemplateName);
		response.setCreationDate(new Date());
		response.setModificationDate(new Date());
		response.setFinalAnnaullyCtc(finalAnnuallyCtc);
		response.setFinalMonthlyCtc(finalMonthlyCtc);

		saveOfferLetterTemplate(response,offerFile, offerTemplateName);

		return offerLetterDetailsRepository.save(response);
	}

	private void saveOfferLetterTemplate(OfferLetterDetails response, File offerFile, String offerTemplateName) throws IOException {

		XWPFDocument document = new XWPFDocument();
		File excelFolder = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_Template"
				);

		if (!excelFolder.exists()){
			excelFolder.mkdirs();
		}

		File emptyDocxFile = new File(excelFolder, offerTemplateName);		  
		FileOutputStream out = new FileOutputStream (emptyDocxFile);  
		document.write(out);
		out.close();

		FileUtils.copyFile(offerFile, emptyDocxFile);
		response.setTemplatePath(emptyDocxFile.getPath());
	}


	//monthly and annaully
	public RestResponse saveOfferLetterTemplateFormula(String monthCostToCompany, String annualCostToCompany, List<String> annualComponent, List<String> monthlyComponent,
			List<String> annualDeduction, List<String> monthlyDeduction, long offerTemplateId, boolean template_with_formula) {

		if(annualComponent==null || annualComponent.size()<1 || monthlyComponent==null || monthlyComponent.size()<1 
				|| annualDeduction==null ||annualDeduction.size()<1 || monthlyDeduction==null || monthlyDeduction.size()<1)
			return new RestResponse(RestResponse.FAILED, null, "Some List is null or empty");

		RestResponse response = null;

		try{

			OfferLetterDetails offerLetterDetails = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offerLetterDetails==null)
				return new RestResponse(RestResponse.FAILED, null, "OfferLetter Template Details Not Found !! ");


			if(!template_with_formula){
				offerLetterDetails.setAnnaullyCtcValue(annualCostToCompany);
				offerLetterDetails.setMonthlyCtcValue(monthCostToCompany);
			}

			String roundsCalculationFields = checkRoundsMethodInAllLIst(monthlyComponent,monthlyDeduction, offerLetterDetails.getComponent(), offerLetterDetails.getDeduction());

			String monthlyDeductionsFormula = "";
			String annuallyDeductionsFormula = "";
			String monthlyCtcFormula = "";
			String annuallyCtcFormula = "";

			int i=0;
			for (String Acomp : annualComponent) {

				if(i==0 && Acomp.contains("Annual")){
					annuallyCtcFormula = "null";
					i=1;
				}else if(i==0 && !Acomp.contains("Annual")){
					annuallyCtcFormula = Acomp;
					i=1;
				}else if(Acomp.contains("Annual")){
					annuallyCtcFormula = annuallyCtcFormula+";"+"null";
				}else if(!Acomp.contains("Annual")){
					annuallyCtcFormula = annuallyCtcFormula+";"+Acomp;
				}

			}

			i=0;
			for (String Mcomp : monthlyComponent) {
				if(i==0 && Mcomp.contains("Monthly")){
					monthlyCtcFormula = "null";
					i=1;
				}else if(i==0 && !Mcomp.contains("Monthly")){
					monthlyCtcFormula = Mcomp;
					i=1;
				}else if(Mcomp.contains("Monthly")){
					monthlyCtcFormula = monthlyCtcFormula+";"+"null";
				}else if(!Mcomp.contains("Monthly")){
					monthlyCtcFormula = monthlyCtcFormula+";"+Mcomp;
				}
			}

			i=0;
			for (String Adeduct : annualDeduction) {
				if(i==0 && Adeduct.contains("Annual")){
					annuallyDeductionsFormula = "null";
					i=1;
				}else if(i==0 && !Adeduct.contains("Annual")){
					annuallyDeductionsFormula = Adeduct;
					i=1;
				}else if(Adeduct.contains("Annual")){
					annuallyDeductionsFormula = annuallyDeductionsFormula+";"+"null";
				}else if(!Adeduct.contains("Annual")){
					annuallyDeductionsFormula = annuallyDeductionsFormula+";"+Adeduct;
				}
			}

			i=0;
			for (String Mdeduct : monthlyDeduction) {
				if(i==0 && Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = "null";
					i=1;
				}else if(i==0 && !Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = Mdeduct;
					i=1;
				}else if(Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = monthlyDeductionsFormula+";"+"null";
				}else if(!Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = monthlyDeductionsFormula+";"+Mdeduct;
				}
			}


			offerLetterDetails.setAnnuallyCtcFormula(annuallyCtcFormula);
			offerLetterDetails.setMonthlyCtcFormula(monthlyCtcFormula);
			offerLetterDetails.setAnnuallyDeductionsFormula(annuallyDeductionsFormula);
			offerLetterDetails.setMonthlyDeductionsFormula(monthlyDeductionsFormula);
			offerLetterDetails.setTemplate_with_formula(template_with_formula);
			offerLetterDetails.setRoundFormulaList(roundsCalculationFields);

			offerLetterDetailsRepository.save(offerLetterDetails);

			response = new RestResponse(RestResponse.SUCCESS, null, "OfferLetter Template Formula saved successfully !! ");
		}catch(Exception e){
			response = new RestResponse(RestResponse.FAILED, null, "Internal server error !! ");
		}
		return response;
	}



	/*	//save according to monthly
	public RestResponse saveOfferLetterTemplateMonthlyFormula(List<String> annualComponent, List<String> monthlyComponent,
			List<String> annualDeduction, List<String> monthlyDeduction, long offerTemplateId, boolean template_with_formula) {

		if(annualComponent==null || annualComponent.size()<1 || monthlyComponent==null || monthlyComponent.size()<1 
				|| annualDeduction==null ||annualDeduction.size()<1 || monthlyDeduction==null || monthlyDeduction.size()<1)
			return new RestResponse(RestResponse.FAILED, null, "Some List is null or empty");

		RestResponse response = null;

		try{

			OfferLetterDetails offerLetterDetails = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offerLetterDetails==null)
				return new RestResponse(RestResponse.FAILED, null, "OfferLetter Template Details Not Found !! ");

			String monthlyDeductionsFormula = "";
			String monthlyCtcFormula = "";
			String annuallyCtcFormula = "";

			int i=0;
			for (String Acomp : annualComponent) {

				if(i==0 && Acomp.contains("Annual")){
					annuallyCtcFormula = "null";
					i=1;
				}else if(i==0 && !Acomp.contains("Annual")){
					annuallyCtcFormula = Acomp;
					i=1;
				}else if(Acomp.contains("Annual")){
					annuallyCtcFormula = annuallyCtcFormula+";"+"null";
				}else if(!Acomp.contains("Annual")){
					annuallyCtcFormula = annuallyCtcFormula+";"+Acomp;
				}

			}

			i=0;
			for (String Mcomp : monthlyComponent) {
				if(i==0 && Mcomp.contains("Monthly")){
					monthlyCtcFormula = "null";
					i=1;
				}else if(i==0 && !Mcomp.contains("Monthly")){
					monthlyCtcFormula = Mcomp;
					i=1;
				}else if(Mcomp.contains("Monthly")){
					monthlyCtcFormula = monthlyCtcFormula+";"+"null";
				}else if(!Mcomp.contains("Monthly")){
					monthlyCtcFormula = monthlyCtcFormula+";"+Mcomp;
				}
			}

			i=0;
			for (String Mdeduct : monthlyDeduction) {
				if(i==0 && Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = "null";
					i=1;
				}else if(i==0 && !Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = Mdeduct;
					i=1;
				}else if(Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = monthlyDeductionsFormula+";"+"null";
				}else if(!Mdeduct.contains("Monthly")){
					monthlyDeductionsFormula = monthlyDeductionsFormula+";"+Mdeduct;
				}
			}


			offerLetterDetails.setAnnuallyCtcFormula(annuallyCtcFormula);
			offerLetterDetails.setMonthlyCtcFormula(monthlyCtcFormula);
			offerLetterDetails.setMonthlyDeductionsFormula(monthlyDeductionsFormula);
			offerLetterDetails.setTemplate_with_formula(template_with_formula);

			offerLetterDetailsRepository.save(offerLetterDetails);

			response = new RestResponse(RestResponse.SUCCESS, null, "OfferLetter Template Formula saved successfully !! ");
		}catch(Exception e){
			response = new RestResponse(RestResponse.FAILED, null, "Internal server error !! ");
		}
		return response;
	}


	 */




	private String checkRoundsMethodInAllLIst(List<String> monthlyComponent,List<String> monthlyDeduction, String component, String deduction) {

		String roundMethodList = "";

		String[] componentList = component.split(",");
		String[] deductionList = deduction.split(",");
		int i=0;

		if(monthlyComponent.size()==componentList.length){

			for (int x=0;x<monthlyComponent.size();x++) {
				if(monthlyComponent.get(x).contains("Round")){

					if(i==0){
						roundMethodList = roundMethodList + componentList[x]+":"+monthlyComponent.get(x).split(",")[1]; 
						i=1;
					}else{
						roundMethodList = roundMethodList +";"+ componentList[x]+":"+monthlyComponent.get(x).split(",")[1];
					}

					monthlyComponent.set(x, monthlyComponent.get(x).split(",")[0].trim());
				}
			}
		}


		if(monthlyDeduction.size()==deductionList.length){
			i=0;
			for (int x=0;x<monthlyDeduction.size();x++) {
				if(monthlyDeduction.get(x).contains("Round")){
					if(i==0 && roundMethodList.equalsIgnoreCase("")){
						roundMethodList = roundMethodList + deductionList[x]+":"+monthlyDeduction.get(x).split(",")[1];
						i=1;
					}else{
						roundMethodList = roundMethodList +";"+ deductionList[x]+":"+monthlyDeduction.get(x).split(",")[1];
					}

					monthlyDeduction.set(x, monthlyDeduction.get(x).split(",")[0].trim());
				}
			}
		}



		return roundMethodList;
	}




	public RestResponse getOfferTemplateList() {

		List<OfferLetterDetails> templateList = new ArrayList<>();
		List<OfferLetterDetails> list = offerLetterDetailsRepository.findAll();

		for (OfferLetterDetails offerLetterDetails : list) {

			if(offerLetterDetails.getAnnuallyCtcFormula()!=null && offerLetterDetails.getAnnuallyDeductionsFormula()!=null){
				templateList.add(offerLetterDetails);
			}
		}


		return new RestResponse(RestResponse.SUCCESS, templateList);
	}


	public RestResponse deleteOfferTemplateById(long offerTemplateId) {

		try{
			offerLetterDetailsRepository.delete(offerTemplateId);
		}catch(Exception e){
			e.printStackTrace();
			return	new RestResponse(RestResponse.FAILED, null, "Something went wrong");
		}

		return new RestResponse(RestResponse.SUCCESS, null, "Template deleted successfully !!");
	}

	public RestResponse getOfferTemplateById(long offerTemplateId, String positionId, String candidateId) {
		OfferLetterDetails offerDetails= null;
		OfferLetterResponseDTO response = null;
		try{

			offerDetails = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offerDetails==null)
				return	new RestResponse(RestResponse.FAILED, null, "Not Found any details of template ");

			if(offerDetails!=null){
				response = createlistIdAndvalue(offerDetails);
				response.setTemplateWithFormula(offerDetails.getTemplate_with_formula());
			}

			if(positionId!=null && candidateId!=null){

				List<OfferLetterApprovals> offerLetterApproval = offerLetterApprovalsService.getApprovalDetailsByPositionIdAndCandidateId(Long.parseLong(positionId),Long.parseLong(candidateId));

				OfferLetterApprovals offerLetterApprovals = null;
				for (OfferLetterApprovals approvals : offerLetterApproval) {
					if(offerLetterApprovals==null){
						offerLetterApprovals = approvals;
					}else{
						if(approvals.getCreationDate().after(offerLetterApprovals.getCreationDate()))
							offerLetterApprovals = approvals;
					}
				}

				Organization org = organizationService.getOrgInfo();
				if (org == null) {
					return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
				}

				if(org.getCheckRolloutOfferletter()==null || org.getCheckRolloutOfferletter().equalsIgnoreCase("false")){

					if(offerLetterApprovals==null || !offerLetterApprovals.getApproval_status().equalsIgnoreCase(GlobalConstants.ACCEPTED))
						return	new RestResponse(RestResponse.FAILED, null, "OfferLetter Approval Required");
				}

				if(offerLetterApprovals!=null){
					response.setDesignation(offerLetterApprovals.getDesignation());
					response.setDoj(offerLetterApprovals.getDoj());
					response.setJoiningBonusCost(offerLetterApprovals.getJoiningBonusCost());
					response.setApprovalId(offerLetterApprovals.getId());

					if(offerLetterApprovals.getCompensationAnnual()!=null && !offerLetterApprovals.getCompensationAnnual().trim().equalsIgnoreCase("") && !offerLetterApprovals.getCompensationAnnual().isEmpty()){
						response.getAnnualCtc().get(0).setFormulaValue(offerLetterApprovals.getCompensationAnnual());
					}

				}

				//	response.getAnnualCtc().get(0).setFormulaValue("1100000");
				/*	response.setDesignation("Sr Java developer");
				response.setDoj(new Date());
				response.setJoiningBonusCost("500");*/

			}

		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return	new RestResponse(RestResponse.FAILED, null, "Something went wrong");
		}

		return new RestResponse(RestResponse.SUCCESS, response, "get offer template details successfully !!");

	}


	private OfferLetterResponseDTO createlistIdAndvalue(OfferLetterDetails offerDetails) {
		OfferLetterResponseDTO res = new OfferLetterResponseDTO();

		List<OfferLetterFormulaDTO> annualCtc = new ArrayList<>();

		List<OfferLetterFormulaDTO> monthlyCtc = new ArrayList<>();

		List<OfferLetterFormulaDTO> annualDeduction = new ArrayList<>();

		List<OfferLetterFormulaDTO> monthlyDeduction = new ArrayList<>();


		String[] component = offerDetails.getComponent().split(",");
		String[] deduction = offerDetails.getDeduction().split(",");

		String[] annualCtcFormula = offerDetails.getAnnuallyCtcFormula().split(";");
		String[] monthlyCtcFormula = offerDetails.getMonthlyCtcFormula().split(";");
		String[] annualDeductionFormula = offerDetails.getAnnuallyDeductionsFormula().split(";");
		String[] monthlyDeductionFormula = offerDetails.getMonthlyDeductionsFormula().split(";");

		int totalCtc = 0; 


		// for ctc we are going to add annual ctc and monthly ctc manually for UI
		if(offerDetails.getTemplate_with_formula()){

			OfferLetterFormulaDTO annualDtoctc = new OfferLetterFormulaDTO();
			annualDtoctc.setId("Annual "+"CTC");
			annualDtoctc.setValue("0");
			annualCtc.add(annualDtoctc);

			OfferLetterFormulaDTO monthlyDtoctc = new OfferLetterFormulaDTO();

			monthlyDtoctc.setId("Monthly "+"CTC");
			monthlyDtoctc.setValue("");
			monthlyCtc.add(monthlyDtoctc);
		}else{
			res.setAnnaullyCtcValue(offerDetails.getAnnaullyCtcValue());
			res.setMonthlyCtcValue(offerDetails.getMonthlyCtcValue());
		}

		for(int i = 0; i< component.length; i++){

			OfferLetterFormulaDTO annualDto = new OfferLetterFormulaDTO();

			annualDto.setId("Annual "+component[i]);
			annualDto.setValue(annualCtcFormula[i]);
			annualCtc.add(annualDto);
			if(!offerDetails.getTemplate_with_formula())
				totalCtc = totalCtc + Integer.parseInt(annualCtcFormula[i]);

			OfferLetterFormulaDTO monthlyDto = new OfferLetterFormulaDTO();

			monthlyDto.setId("Monthly "+component[i]);
			monthlyDto.setValue(monthlyCtcFormula[i]);

			if(offerDetails.getRoundFormulaList()!=null && !offerDetails.getRoundFormulaList().trim().equalsIgnoreCase("")){
				String[] roundFormulaList = offerDetails.getRoundFormulaList().split(";");

				for (String round : roundFormulaList) {

					if(component[i].trim().equalsIgnoreCase(round.split(":")[0].trim())){
						monthlyDto.setValue(monthlyCtcFormula[i]+","+round.split(":")[1]);
					}

				}

			}

			monthlyCtc.add(monthlyDto);
		}


		for(int i = 0; i< deduction.length; i++){

			OfferLetterFormulaDTO annualDto = new OfferLetterFormulaDTO();

			annualDto.setId("Annual "+deduction[i]);
			annualDto.setValue(annualDeductionFormula[i]);
			annualDeduction.add(annualDto);

			if(!offerDetails.getTemplate_with_formula())
				totalCtc = totalCtc + Integer.parseInt(annualDeductionFormula[i]);

			OfferLetterFormulaDTO monthlyDto = new OfferLetterFormulaDTO();

			monthlyDto.setId("Monthly "+deduction[i]);
			monthlyDto.setValue(monthlyDeductionFormula[i]);

			if(offerDetails.getRoundFormulaList()!=null && !offerDetails.getRoundFormulaList().trim().equalsIgnoreCase("")){
				String[] roundFormulaList = offerDetails.getRoundFormulaList().split(";");

				for (String round : roundFormulaList) {

					if(deduction[i].trim().equalsIgnoreCase(round.split(":")[0].trim())){
						monthlyDto.setValue(monthlyDeductionFormula[i]+","+round.split(":")[1]);
					}

				}

			}

			monthlyDeduction.add(monthlyDto);

		}

		res.setAnnualCtc(annualCtc);
		res.setMonthlyCtc(monthlyCtc);
		res.setAnnualDeduction(annualDeduction);
		res.setMonthlyDeduction(monthlyDeduction);

		for (OfferLetterFormulaDTO offerLetterFormulaDTO : annualCtc) {

			if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Annual CTC")){
				try{
					//if(offerLetterFormulaDTO.getValue()!=null && !offerLetterFormulaDTO.getValue().equals("") && !offerLetterFormulaDTO.getValue().isEmpty())
					totalCtc = Integer.parseInt(offerLetterFormulaDTO.getValue());
				}catch(Exception e){

				}
			}
		}

		if(!offerDetails.getTemplate_with_formula()){
			totalCtc = Integer.parseInt(offerDetails.getAnnaullyCtcValue());
			res.setAnnaullyCtcValue(offerDetails.getAnnaullyCtcValue());
			res.setMonthlyCtcValue(offerDetails.getMonthlyCtcValue());
		}



		res.setTotalCtc(totalCtc);

		return res;
	}

	public RestResponse selectOfferTemplateById(long offerTemplateId) {

		try{

			if(offerTemplateId==0){
				List<OfferLetterDetails> offerTemplateList = offerLetterDetailsRepository.findAll();
				for (OfferLetterDetails offerLetter : offerTemplateList) {
					if(offerLetter.getSelected_status()){
						offerLetter.setSelected_status(false);
						offerLetterDetailsRepository.save(offerLetter);
					}
				}

				return new RestResponse(RestResponse.SUCCESS, null, "set status false successfully !!");

			}



			OfferLetterDetails offer = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offer==null)
				return new RestResponse(RestResponse.FAILED, null, "Not found any record !!");



			List<OfferLetterDetails> offerTemplateList = offerLetterDetailsRepository.findAll();

			if(offerTemplateList!=null && offerTemplateList.size()>0){
				for (OfferLetterDetails offerLetter : offerTemplateList) {
					if(offerLetter.getSelected_status()){
						offerLetter.setSelected_status(false);
						offerLetterDetailsRepository.save(offerLetter);
					}
				}

				offer.setSelected_status(true);
				offerLetterDetailsRepository.save(offer);

			}

			return new RestResponse(RestResponse.SUCCESS, null, "set status true successfully !!");
		}catch(Exception e){
			e.printStackTrace();
			return	new RestResponse(RestResponse.FAILED, null, "Something went wrong");
		}
	}

	public RestResponse generateOfferLetterForPreview(long positionCode, List<String> textData, List<String> annualComponent,
			List<String> monthlyComponent, List<String> annualDeduction, List<String> monthlyDeduction,
			long offerTemplateId, long candidateId, String joiningBonusAmount) {

		try{

			String firstName = textData.get(textData.size()-2);
			String lastName = textData.get(textData.size()-1);
			textData.remove(textData.size()-1);
			textData.remove(textData.size()-1);


			if(joiningBonusAmount!=null && !joiningBonusAmount.trim().equalsIgnoreCase("") && !joiningBonusAmount.trim().equalsIgnoreCase("0")){
				textData.add(joiningBonusAmount);
				textData.set(5, String.valueOf(Integer.parseInt(textData.get(5))+ Integer.parseInt(joiningBonusAmount)));
				textData.add(textData.get(1));textData.add(lastName);

				textData.set(1, firstName);

			}else{
				textData.add(textData.get(1));textData.add(lastName);
				textData.set(1, firstName);
				textData.add("0");
			}
			OfferLetterDetails offer = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offer==null)
				return new RestResponse(RestResponse.FAILED, null, "Not found any record !!");

			int annualCTC = Integer.parseInt(annualComponent.get(0)); // Using this value for make finalAnnualCtc value round only 
			monthlyComponent.remove(0);
			annualComponent.remove(0);

			int annuallyGrossTotal = 0;
			int monthlyGrossTotal = 0;
			int annuallyDeductionTotal = 0;
			int monthlyDeductionTotal = 0;
			int annuallyCTCTotal = 0;
			int monthlyCTCTotal = 0;
			int finalMonthlyCtc = 0;
			int finalAnaullyCtc = 0;

			for (String data : annualComponent) {
				int value = Integer.parseInt(data);
				annuallyGrossTotal = annuallyGrossTotal+value;
			}

			for (String data : monthlyComponent) {
				int value = Integer.parseInt(data);
				monthlyGrossTotal = monthlyGrossTotal + value;
			}

			for (String data : annualDeduction) {
				int value = Integer.parseInt(data);
				annuallyDeductionTotal = annuallyDeductionTotal + value;
			}

			for (String data : monthlyDeduction) {
				int value = Integer.parseInt(data);
				monthlyDeductionTotal = monthlyDeductionTotal + value;
			}

			annuallyCTCTotal = annualCTC;//annuallyGrossTotal + annuallyDeductionTotal;
			monthlyCTCTotal = monthlyGrossTotal + monthlyDeductionTotal;

			//	finalCtcValueRound(annuallyCTCTotal,annualCTC);

			if(joiningBonusAmount!=null){
				finalMonthlyCtc = monthlyCTCTotal + (Integer.valueOf(joiningBonusAmount))/12;
				finalAnaullyCtc = annuallyCTCTotal + Integer.valueOf(joiningBonusAmount);
			}else{
				finalMonthlyCtc = monthlyCTCTotal ;
				finalAnaullyCtc = annuallyCTCTotal; 
			}


			String[] component = offer.getComponent().split(",");
			String[] deduction = offer.getDeduction().split(",");
			String[] textValue = offer.getText().split(",");

			OfferLetterTemplateVariables txt  = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.text.toString());
			OfferLetterTemplateVariables com  = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.component.toString());
			OfferLetterTemplateVariables calcu  = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.calculation.toString());
			OfferLetterTemplateVariables ded  = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.deduction.toString());
			OfferLetterTemplateVariables monG = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyGross.toString());
			OfferLetterTemplateVariables annG = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyGross.toString());
			OfferLetterTemplateVariables monD = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyDeductions.toString());
			OfferLetterTemplateVariables annD = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyDeductions.toString());
			OfferLetterTemplateVariables monC = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.monthlyCtc.toString());
			OfferLetterTemplateVariables annC = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.annuallyCtc.toString());
			OfferLetterTemplateVariables finalMCtc = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.finalCostMonthlyCtc.toString());
			OfferLetterTemplateVariables finalACtc = offerLetterVariableRepository.getByVariableName(OfferLetterVariable.finalCostAnnuallyCtc.toString());

			XWPFDocument doc = new XWPFDocument(
					OPCPackage.open(offer.getTemplatePath()));
			for (XWPFParagraph p : doc.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {
					for (XWPFRun r : runs) {
						String text = r.getText(0);
						System.out.println(text);
						if (text != null) {

							//Text
							for(int i = 0; i< textValue.length; i++){

								if(txt!=null && textData.size()>=textValue.length){
									String find1 = txt.getStartTag()+textValue[i]+txt.getEndTag();
									String replace1 = textData.get(i);
									text = text.replace(find1, replace1);
								}								
							}


							//Component
							for(int i = 0; i< component.length; i++){

								if(com!=null){
									// for annually_Component
									String find1 = calcu.getStartTag()+component[i]+calcu.getEndTag();
									String replace1 = annualComponent.get(i);
									text = text.replace(find1, "₹  "+replace1);

									//for monthly_Component
									String find2 = com.getStartTag()+component[i]+com.getEndTag();
									String replace2 = monthlyComponent.get(i);
									text = text.replace(find2, "₹  "+replace2);
								}								
							}


							//Deduction
							for(int i = 0; i< deduction.length; i++){

								if(ded!=null){
									// for annually_Deduction
									String find1 = calcu.getStartTag()+deduction[i]+calcu.getEndTag();
									String replace1 = annualDeduction.get(i);
									text = text.replace(find1, "₹  "+replace1);

									//for monthly_Deduction
									String find2 = ded.getStartTag()+deduction[i]+ded.getEndTag();
									String replace2 = monthlyDeduction.get(i);
									text = text.replace(find2, "₹  "+replace2);
								}								
							}

							//annuallyGross
							String aG = annG.getStartTag() + offer.getAnnuallyGross() + annG.getEndTag();
							text = text.replace(aG, "₹  "+String.valueOf(annuallyGrossTotal));

							//monthlyGross
							String mG = monG.getStartTag() + offer.getMonthlyGross() + monG.getEndTag();
							text = text.replace(mG, "₹  "+String.valueOf(monthlyGrossTotal));

							//annuallyDeduction
							String aD = annD.getStartTag() + offer.getAnnuallyDeductions() + annD.getEndTag();
							text = text.replace(aD, "₹  "+String.valueOf(annuallyDeductionTotal));

							//monthlyDeduction
							String mD = monD.getStartTag() + offer.getMonthlyDeductions() + monD.getEndTag();
							text = text.replace(mD, "₹  "+String.valueOf(monthlyDeductionTotal));

							//annuallyCTCTotal
							String aC = annC.getStartTag() + offer.getAnnuallyCtc() + annC.getEndTag();
							text = text.replace(aC, "₹  "+String.valueOf(annuallyCTCTotal));

							//monthlyCTCTotal
							String mC = monC.getStartTag() + offer.getMonthlyCtc() + monC.getEndTag();
							text = text.replace(mC, "₹  "+String.valueOf(monthlyCTCTotal));


							r.setText(text, 0);
						}
					}
				}
			}


			for (XWPFTable tbl : doc.getTables()) {
				for (XWPFTableRow row : tbl.getRows()) {
					for (XWPFTableCell cell : row.getTableCells()) {
						for (XWPFParagraph p : cell.getParagraphs()) {
							int k=0;
							for (XWPFRun r : p.getRuns()) {
								if(k==0){
									String text="";
									//	for(XWPFRun rA : p.getRuns()){
									text = text + r.getText(0);	
									//}

									System.out.println(text);
									//Text
									for(int i = 0; i< textValue.length; i++){

										if(txt!=null && textData.size()>=textValue.length){
											// for annually_Component
											String find1 = txt.getStartTag()+textValue[i]+txt.getEndTag();
											String replace1 = textData.get(i);
											text = text.replace(find1, replace1);
										}								
									}

									/*	for(int i = 0; i< textValue.length; i++){

										if(txt!=null && textData.size()>=textValue.length){
											String find1 = txt.getStartTag()+textValue[i]+txt.getEndTag();
											String replace1 = textData.get(i);
											text = text.replace(find1, replace1);
										}								
									}
									 */

									//Component
									for(int i = 0; i< component.length; i++){

										if(com!=null){
											// for annually_Component
											String find1 = calcu.getStartTag()+component[i]+calcu.getEndTag();
											String replace1 = annualComponent.get(i);
											text = text.replace(find1, "₹  "+replace1);

											//for monthly_Component
											String find2 = com.getStartTag()+component[i]+com.getEndTag();
											String replace2 = monthlyComponent.get(i);
											text = text.replace(find2, "₹  "+replace2);
										}								
									}


									//Deduction
									for(int i = 0; i< deduction.length; i++){

										if(ded!=null){
											// for annually_Deduction
											String find1 = calcu.getStartTag()+deduction[i]+calcu.getEndTag();
											String replace1 = annualDeduction.get(i);
											text = text.replace(find1, "₹  "+replace1);

											//for monthly_Deduction
											String find2 = ded.getStartTag()+deduction[i]+ded.getEndTag();
											String replace2 = monthlyDeduction.get(i);
											text = text.replace(find2, "₹  "+replace2);
										}								
									}

									//annuallyGross
									String aG = annG.getStartTag() + offer.getAnnuallyGross() + annG.getEndTag();
									text = text.replace(aG, "₹  "+String.valueOf(annuallyGrossTotal));

									//monthlyGross
									String mG = monG.getStartTag() + offer.getMonthlyGross() + monG.getEndTag();
									text = text.replace(mG, "₹  "+String.valueOf(monthlyGrossTotal));

									//annuallyDeduction
									String aD = annD.getStartTag() + offer.getAnnuallyDeductions() + annD.getEndTag();
									text = text.replace(aD, "₹  "+String.valueOf(annuallyDeductionTotal));

									//monthlyDeduction
									String mD = monD.getStartTag() + offer.getMonthlyDeductions() + monD.getEndTag();
									text = text.replace(mD, "₹  "+String.valueOf(monthlyDeductionTotal));

									//annuallyCTCTotal
									String aC = annC.getStartTag() + offer.getAnnuallyCtc() + annC.getEndTag();
									text = text.replace(aC, "₹  "+String.valueOf(annuallyCTCTotal));

									//monthlyCTCTotal
									String mC = monC.getStartTag() + offer.getMonthlyCtc() + monC.getEndTag();
									text = text.replace(mC, "₹  "+String.valueOf(monthlyCTCTotal));


									//FinalMonthlyCtc
									String finMCtc = finalMCtc.getStartTag() + offer.getFinalMonthlyCtc() + finalMCtc.getEndTag();
									text = text.replace(finMCtc, "₹  "+String.valueOf(finalMonthlyCtc));

									//FinalAnnaullyCtc
									String finACtc = finalACtc.getStartTag() + offer.getFinalAnnaullyCtc() + finalACtc.getEndTag();
									text = text.replace(finACtc, "₹  "+String.valueOf(finalAnaullyCtc));

									text = text.replace("Computation of CTC", "Computation of CTC 2020");
									r.setText(text,0);
									k=1;

								}else{
									r.setText("",0);
								}
							}
						}
					}
				}
			}

			Candidate candidate = candidateService.findOne(candidateId);
			Organization org = getOrgInfo();

			File fileFolder = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_For_Candidate"
					+ File.separator +positionCode+ File.separator +candidateId+ File.separator + "Preview");

			if (!fileFolder.exists()){
				fileFolder.mkdirs();
			}else{
				FileUtils.deleteDirectory(fileFolder);
				fileFolder.mkdirs();
			}


			doc.write(new FileOutputStream(fileFolder.getPath() + File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".docx"));

			File inputFile = new File(fileFolder.getPath() + File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".docx");
			File outputFile = new File(fileFolder.getPath() + File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".pdf");
			fileFormatConversionService.convert(inputFile, outputFile);

			return new RestResponse(RestResponse.SUCCESS, null, "offer letter generated successfully  !!");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
	}

	private String finalCtcValueRound(int finalAnaullyCtc, int annualCTC) {

		int ctc = annualCTC - 10;

		if(finalAnaullyCtc >= ctc && finalAnaullyCtc <= annualCTC){
			finalAnaullyCtc = annualCTC;
		}

		return finalAnaullyCtc+"";
	}

	public void sendFileResponse(HttpServletResponse response, long candidateId, String positionCode) {

		try{

			Candidate candidate = candidateService.findOne(candidateId);
			Organization org = getOrgInfo();

			File file = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_For_Candidate"
					+ File.separator +positionCode+ File.separator +candidateId+ File.separator + "Preview"+ File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".pdf");

			logger.error("File path download : ========" + candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_For_Candidate"
					+ File.separator +positionCode+ File.separator +candidateId+ File.separator + "Preview"+ File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".pdf");

			Path filePath = file.toPath();

			String name = filePath.getFileName().toString();
			logger.debug("File requested for download : " + name);

			if (filePath.toFile() == null || !filePath.toFile().exists()) {
				return;
			}

			String mimeType = URLConnection.guessContentTypeFromName(filePath.getFileName().toString());
			if (mimeType == null) {
				mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			}

			response.setContentType(mimeType);
			response.setHeader("Content-Disposition",
					String.format("inline; filename=\"" + filePath.getFileName().toString() + "\""));

			response.setContentLength((int) filePath.toFile().length());
			Files.copy(filePath, response.getOutputStream());
			System.out.println(response.toString());
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
	}

	public RestResponse saveFinalOfferLetterForCandidate(long approvalID, long positionCode, List<String> text, List<String> annualComponent,
			List<String> monthlyComponent, List<String> annualDeduction, List<String> monthlyDeduction,
			long offerTemplateId, long candidateId, String joiningBonusAmount) {

		OfferLetterForCandidate offerCandidate = new OfferLetterForCandidate();

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}


		OfferLetterApprovals offerLetterApprovals = offerLetterApprovalsService.findOne(approvalID);

		if(offerLetterApprovals==null && org.getCheckRolloutOfferletter()!=null && org.getCheckRolloutOfferletter().trim().equalsIgnoreCase("false"))
			return new RestResponse(RestResponse.FAILED, null, "Not found any Approval record for Candidate!!");

		try{

			if(joiningBonusAmount!=null && !joiningBonusAmount.trim().equalsIgnoreCase("")&& !joiningBonusAmount.trim().equalsIgnoreCase("0")){
				text.add(joiningBonusAmount);
				text.set(5, String.valueOf(Integer.parseInt(text.get(5))+ Integer.parseInt(joiningBonusAmount)));
			}else{
				text.add("0");
			}

			Candidate candidate = candidateService.findOne(candidateId);

			if(candidate==null)
				return new RestResponse(RestResponse.FAILED, null, "Not found any record of Candidate!!");

			OfferLetterDetails offer = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offer==null)
				return new RestResponse(RestResponse.FAILED, null, "Not found any record !!");

			int annuallyGrossTotal = 0;
			int monthlyGrossTotal = 0;
			int annuallyDeductionTotal = 0;
			int monthlyDeductionTotal = 0;
			int annuallyCTCTotal = 0;
			int monthlyCTCTotal = 0;
			int finalMonthlyCtc = 0;
			int finalAnaullyCtc = 0;

			String textValues = "";
			String annuallyDeductionValues = "";
			String monthlyDeductionValues = "";
			String annuallyCtcValues = "";
			String monthlyCtcValues = "";

			for (int i=0;i<text.size(); i++ ) {

				if(i==0){
					textValues = textValues+text.get(i);
				}else{
					textValues = textValues+";"+text.get(i);
				}

			}

			for (int i=0;i<annualComponent.size(); i++ ) {
				int value = Integer.parseInt(annualComponent.get(i));
				annuallyGrossTotal = annuallyGrossTotal+value;

				if(i==0){
					annuallyCtcValues = annuallyCtcValues+annualComponent.get(i);
				}else{
					annuallyCtcValues = annuallyCtcValues+";"+annualComponent.get(i);
				}

			}

			for (int i=0;i<monthlyComponent.size(); i++ ) {
				int value = Integer.parseInt(monthlyComponent.get(i));
				monthlyGrossTotal = monthlyGrossTotal + value;

				if(i==0){
					monthlyCtcValues = monthlyCtcValues+monthlyComponent.get(i);
				}else{
					monthlyCtcValues = monthlyCtcValues+";"+monthlyComponent.get(i);
				}

			}

			for (int i=0;i<annualDeduction.size(); i++ ) {

				int value = Integer.parseInt(annualDeduction.get(i));
				annuallyDeductionTotal = annuallyDeductionTotal + value;

				if(i==0){
					annuallyDeductionValues = annuallyDeductionValues+annualDeduction.get(i);
				}else{
					annuallyDeductionValues = annuallyDeductionValues+";"+annualDeduction.get(i);
				}
			}

			for (int i=0;i<monthlyDeduction.size(); i++ ) {
				int value = Integer.parseInt(monthlyDeduction.get(i));
				monthlyDeductionTotal = monthlyDeductionTotal + value;

				if(i==0){
					monthlyDeductionValues = monthlyDeductionValues+monthlyDeduction.get(i);
				}else{
					monthlyDeductionValues = monthlyDeductionValues+";"+monthlyDeduction.get(i);
				}

			}

			annuallyCTCTotal = annuallyGrossTotal + annuallyDeductionTotal;
			monthlyCTCTotal = monthlyGrossTotal + monthlyDeductionTotal;
			if(joiningBonusAmount!=null){
				finalMonthlyCtc =  monthlyCTCTotal + Integer.valueOf(joiningBonusAmount); 
				finalAnaullyCtc =  annuallyCTCTotal + Integer.valueOf(joiningBonusAmount);
			}else{
				finalMonthlyCtc =  monthlyCTCTotal;
				finalAnaullyCtc =  annuallyCTCTotal;
			}
			offerCandidate.setOfferTemplate_id(offerTemplateId);
			offerCandidate.setCandidate_id(candidateId);
			offerCandidate.setTextValues(textValues);
			offerCandidate.setAnnuallyCtcValues(annuallyCtcValues);
			offerCandidate.setMonthlyCtcValues(monthlyCtcValues);
			offerCandidate.setAnnuallyDeductionValues(annuallyDeductionValues);
			offerCandidate.setMonthlyDeductionValues(monthlyDeductionValues);
			offerCandidate.setAnnuallyCtcTotal(String.valueOf(annuallyCTCTotal));
			offerCandidate.setMonthlyCtcTotal(String.valueOf(monthlyCTCTotal));
			offerCandidate.setAnnuallyDeductionsTotal(String.valueOf(annuallyDeductionTotal));
			offerCandidate.setMonthlyDeductionsTotal(String.valueOf(monthlyDeductionTotal));
			offerCandidate.setAnnuallyGrossTotal(String.valueOf(annuallyGrossTotal));
			offerCandidate.setMonthlyGrossTotal(String.valueOf(monthlyGrossTotal));
			offerCandidate.setJoiningBonusAmount(joiningBonusAmount);
			offerCandidate.setFinalAnnaullyCtc(String.valueOf(finalAnaullyCtc));
			offerCandidate.setFinalMonthlyCtc(String.valueOf(finalMonthlyCtc));
			offerCandidate.setApprovalId(approvalID);

			File previewFolder = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_For_Candidate"
					+ File.separator +positionCode+ File.separator +candidateId+ File.separator + "Preview");

			if (!previewFolder.exists()){
				generateOfferLetterForPreview(positionCode,text, annualComponent,
						monthlyComponent, annualDeduction,  monthlyDeduction, offerTemplateId, candidateId,joiningBonusAmount);
			}


			File OfferletterFolder = new File(candidateFolderPath + File.separator + TenantContextHolder.getTenant() +  File.separator + "Offer_Letter_For_Candidate"
					+ File.separator +positionCode+ File.separator +candidateId+ File.separator + "offerLetter");


			if (!OfferletterFolder.exists()){
				OfferletterFolder.mkdirs();
			}


			File inputFile = new File(previewFolder.getPath() + File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".docx");
			File outputFile = new File(OfferletterFolder.getPath() + File.separator + "offerletter_"+candidate.getFullName()+"_"+org.getOrgId()+".pdf");
			fileFormatConversionService.convert(inputFile, outputFile);

			offerCandidate.setFinalOfferLetterPath(outputFile.getPath());
			OfferLetterForCandidateRepository.save(offerCandidate);
			candidate.setGeneratedOfferLetter(true);
			candidateService.save(candidate);

			return new RestResponse(RestResponse.SUCCESS, null, "save offer letter Candidate Successfully  !!");
		}catch(Exception e){
			logger.error("Error comes when generating offer letter = "+e);
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}

	}

	public RestResponse getListOfOfferLetter(long candidateId) {

		try{
			List<OfferLetterForCandidate> offerLetterList = OfferLetterForCandidateRepository.getListOfOfferLetterByCandidateId(candidateId);

			/*OfferLetterResponseDTO data = new OfferLetterResponseDTO();
			if(offerLetterList.size()>0 && offerLetterList.get(0).getFinalOfferLetterPath()!=null){
				String name = offerLetterList.get(0).getFinalOfferLetterPath().split("offerLetter")[1];
				name = name.replace("\\", "");
				data.setId(offerLetterList.get(0).getId());
				data.setFileName(name);
				data.setFilePath(offerLetterList.get(0).getFinalOfferLetterPath());
				data.setCandidateId(candidateId);
			}*/
			return new RestResponse(RestResponse.SUCCESS, offerLetterList, "get List offer_letter of Candidate Successfully  !!");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
	}

	public RestResponse deleteOfferLetterByCandidateId(long candidateId, String filePath) {

		try{
			Candidate candidate = candidateService.findOne(candidateId);

			if(candidate==null)
				return new RestResponse(RestResponse.FAILED, null, "Not found any record of Candidate!!");

			OfferLetterForCandidate offerLetter = OfferLetterForCandidateRepository.deleteOfferLetterByCandidateId(candidateId, filePath);

			if(offerLetter!=null){
				if(offerLetter.getFinalOfferLetterPath()!=null){
					Files.deleteIfExists(Paths.get(offerLetter.getFinalOfferLetterPath()));

					OfferLetterForCandidateRepository.delete(offerLetter.getId()); 
				}
				candidate.setGeneratedOfferLetter(false);
				candidateService.save(candidate);
			}
			return new RestResponse(RestResponse.SUCCESS, null, "offer_letter deleted Successfully  !!");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}

	}

	public RestResponse calculationOfTemplateFormula(List<OfferLetterFormulaDTO> dataList, long offerTemplateId) {

		if(dataList==null || dataList.equals("") || dataList.isEmpty() || dataList.size()==0)
			return new RestResponse(RestResponse.FAILED, null, "Required formula value  !!");

		List<OfferLetterFormulaDTO> valueList = new ArrayList<>();		
		List<OfferLetterFormulaDTO> annformula = new ArrayList<>();
		List<OfferLetterFormulaDTO> monformula = new ArrayList<>();
		List<OfferLetterFormulaDTO> noValueList = new ArrayList<>();

		try{

			String annualCTCVAlue = null;
			//check if annaual ctcvalue is 0, then return annaual value required

			for (OfferLetterFormulaDTO offerLetterFormulaDTO : dataList) {
				if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Annual CTC")){
					if(offerLetterFormulaDTO.getValue().trim().equalsIgnoreCase("") || offerLetterFormulaDTO.getValue().trim().equalsIgnoreCase("0"))
						return new RestResponse(RestResponse.FAILED, null, "Required Annual Ctc Value  !!");
					else
						annualCTCVAlue = offerLetterFormulaDTO.getValue();
				}else{
					if(!offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly CTC") /*&& !offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly BONUS")*/){

						offerLetterFormulaDTO.setValue("");
					}
				}



				/*if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly BONUS")){

					if(!offerLetterFormulaDTO.getValue().equalsIgnoreCase("") && offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly "+offerLetterFormulaDTO.getFormula()))

				}*/


			}


			for (OfferLetterFormulaDTO offerLetterFormulaDTO : dataList) {

				if(!offerLetterFormulaDTO.getFormula().equalsIgnoreCase("")){

					if(offerLetterFormulaDTO.getFormula().contains(","))
						offerLetterFormulaDTO.setFormula(offerLetterFormulaDTO.getFormula().split(",")[0]);

					if(offerLetterFormulaDTO.getValue().equals("") && (offerLetterFormulaDTO.getId().equalsIgnoreCase("Annual "+offerLetterFormulaDTO.getFormula()) || offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly "+offerLetterFormulaDTO.getFormula()))){
						noValueList.add(offerLetterFormulaDTO);
					}else if(!offerLetterFormulaDTO.getValue().equalsIgnoreCase("") && (offerLetterFormulaDTO.getId().equalsIgnoreCase("Annual "+offerLetterFormulaDTO.getFormula()) || offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly "+offerLetterFormulaDTO.getFormula()))){
						valueList.add(offerLetterFormulaDTO);
					}else if(offerLetterFormulaDTO.getId().split(" ")[0].trim().equalsIgnoreCase("Annual")){
						annformula.add(offerLetterFormulaDTO);
					}else{

						// this if condition is hardcore for intelliswift only, if we need to make dynamic , we need to change it
						// if we change in future we keep only else condition not if				
						if(annualCTCVAlue!=null && offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly BASIC")){
							String replaceData = String.valueOf(Integer.parseInt(annualCTCVAlue)/50000) ;
							offerLetterFormulaDTO.setFormula(offerLetterFormulaDTO.getFormula().replace("CTC / 50000", replaceData));
							monformula.add(offerLetterFormulaDTO);
						}else{
							monformula.add(offerLetterFormulaDTO);	
						}
					}


				}else if(!offerLetterFormulaDTO.getValue().equalsIgnoreCase("") && offerLetterFormulaDTO.getFormula().equalsIgnoreCase("")){
					valueList.add(offerLetterFormulaDTO);
				}			

			}     

			// add manually annual ctc in value list
			valueList.add(dataList.get(0));


			// first calculate annaual formula
			//		calculateFormulaOneByOne(annformula,valueList);

			// second calculate monthly formula
			calculateFormulaOneByOne(monformula, valueList);


			List<OfferLetterFormulaDTO> pendingMonthly = new ArrayList<>(); 
			String forIntelliswitBasicValue = null;

			for (OfferLetterFormulaDTO offerLetterFormulaDTO2 : valueList) {
				if(offerLetterFormulaDTO2.getId().split(" ")[0].trim().equalsIgnoreCase("Monthly") && offerLetterFormulaDTO2.getValue().trim().equalsIgnoreCase("")){
					pendingMonthly.add(offerLetterFormulaDTO2);
				}

				if(offerLetterFormulaDTO2.getId().equalsIgnoreCase("Monthly BASIC")){
					forIntelliswitBasicValue = offerLetterFormulaDTO2.getValue();
				}


			}


			// (start code) This is hardcode code for Intelliswift client only 
			Organization org = organizationService.getOrgInfo();
			if (org != null && (org.getOrgId().equalsIgnoreCase("Intelliswift___Bangalore") || org.getOrgId().equalsIgnoreCase("Template") 
					|| org.getOrgId().equalsIgnoreCase("ABC_Corp") )&& forIntelliswitBasicValue!=null) {

				int basicValue = Integer.parseInt(forIntelliswitBasicValue);


				for (OfferLetterFormulaDTO offerLetterFormulaDTO : valueList) {

					if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly PF")){

						if(basicValue>=15000){
							offerLetterFormulaDTO.setValue("1950");
						}else{
							double basicDouble= basicValue;
							String val = String.valueOf(basicDouble*13/100);
							val = this.round(val);
							offerLetterFormulaDTO.setValue(val);
						}
					}

					if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Monthly BONUS")){

						if(basicValue<=9904){
							double basicDouble= basicValue;
							String val = String.valueOf(basicDouble*833/10000);
							val = this.round(val);
							offerLetterFormulaDTO.setValue(val);
						}else if(basicValue>=9905 && basicValue<=21000){
							offerLetterFormulaDTO.setValue("825");
						}else{
							offerLetterFormulaDTO.setValue("0");
						}
					}	

				}

			}	


			// (end code) This is hardcode code for Intelliswift client only 






			calculateFormulaOneByOne(pendingMonthly, valueList);


			/*			List<OfferLetterFormulaDTO> annualValueCalculateDTO = new ArrayList<>();

			for (OfferLetterFormulaDTO noValueDTO : noValueList) {

				for (OfferLetterFormulaDTO data : valueList) {

					if(!data.getValue().trim().equalsIgnoreCase("") && !noValueDTO.getId().split(" ")[0].trim().equalsIgnoreCase(data.getId().split(" ")[0].trim()) && noValueDTO.getId().split(" ")[1].trim().equalsIgnoreCase(data.getId().split(" ")[1].trim())){

						noValueDTO.setValue(String.valueOf(Integer.parseInt(data.getValue())*12));
						annualValueCalculateDTO.add(noValueDTO);
					}

				}	


			}

			valueList.addAll(annualValueCalculateDTO);
			 */			

			/*	List<OfferLetterFormulaDTO> setAnnualValueByMonthly = new ArrayList<>(); 

			for (OfferLetterFormulaDTO offerLetterFormulaDTO2 : valueList) {
				if(offerLetterFormulaDTO2.getId().split(" ")[0].trim().equalsIgnoreCase("Monthly") && !offerLetterFormulaDTO2.getValue().trim().equalsIgnoreCase("")){
					setAnnualValueByMonthly.add(offerLetterFormulaDTO2);
				}

			}

			for (OfferLetterFormulaDTO dTO2 : valueList) {

				for (OfferLetterFormulaDTO offer : setAnnualValueByMonthly) {

					if(dTO2.getId().split(" ")[0].trim().equalsIgnoreCase("Annual") && dTO2.getId().split(" ")[1].trim().equalsIgnoreCase(offer.getId().split(" ")[1].trim())){
						dTO2.setValue(String.valueOf(Integer.parseInt(offer.getValue())*12));
					}

				}

			}
			 */	


			annformula = new ArrayList<>();
			//check monthly value's once again
			if(noValueList.size()>0){

				//valueList.addAll(noValueList);

				for (OfferLetterFormulaDTO offerDTO : noValueList) {

					if(offerDTO.getId().split(" ")[0].trim().equalsIgnoreCase("Monthly")){

						for(OfferLetterFormulaDTO checkOnces : valueList) {

							if(checkOnces.getId().equalsIgnoreCase("Annual "+offerDTO.getFormula())){

								if(!checkOnces.getValue().equalsIgnoreCase("")){
									offerDTO.setValue(checkOnces.getValue());
									annformula.add(offerDTO);
								}else{
									annformula.add(offerDTO);
								}
							}
						}


					}else{
						valueList.add(offerDTO);
					}

				}

			}

			if(annformula.size()>0)
				valueList.addAll(annformula);



			OfferLetterDetails offerLetterDetails = offerLetterDetailsRepository.findOne(offerTemplateId);

			if(offerLetterDetails!=null && offerLetterDetails.getRoundFormulaList()!=null && !offerLetterDetails.getRoundFormulaList().trim().equalsIgnoreCase("")){

				for (OfferLetterFormulaDTO offerLetterFormulaDTO : valueList) {

					if(offerLetterFormulaDTO.getId().split(" ")[0].trim().equalsIgnoreCase("Monthly")){

						String[] roundFormulaList = offerLetterDetails.getRoundFormulaList().split(";");

						for (String round : roundFormulaList) {

							if(offerLetterFormulaDTO.getId().split(" ")[1].trim().equalsIgnoreCase(round.split(":")[0].trim()) && !offerLetterFormulaDTO.getValue().trim().equalsIgnoreCase("") && !offerLetterFormulaDTO.getValue().trim().isEmpty()){

								if(round.split(":")[1].trim().contains("RoundUp")){
									offerLetterFormulaDTO.setValue(roundUp(offerLetterFormulaDTO.getValue(), Integer.parseInt(round.split(":")[1].split(" ")[1].trim())));
								}else if(round.split(":")[1].trim().contains("RoundDown")){
									offerLetterFormulaDTO.setValue(roundDown(offerLetterFormulaDTO.getValue(), Integer.parseInt(round.split(":")[1].split(" ")[1].trim())));
								}else{
									offerLetterFormulaDTO.setValue(round(offerLetterFormulaDTO.getValue()));
								}


							}

						}

					}

					if(offerLetterFormulaDTO.getValue()!=null && !offerLetterFormulaDTO.getValue().trim().equalsIgnoreCase("") && !offerLetterFormulaDTO.getValue().trim().isEmpty())
						offerLetterFormulaDTO.setValue(String.valueOf((int) Double.parseDouble(offerLetterFormulaDTO.getValue())));

				}

			}


			List<OfferLetterFormulaDTO> setAnnualValueByMonthly = new ArrayList<>(); 

			for (OfferLetterFormulaDTO offerLetterFormulaDTO2 : valueList) {
				if(offerLetterFormulaDTO2.getId().split(" ")[0].trim().equalsIgnoreCase("Monthly") && !offerLetterFormulaDTO2.getValue().trim().equalsIgnoreCase("")){
					setAnnualValueByMonthly.add(offerLetterFormulaDTO2);
				}

			}




			for (OfferLetterFormulaDTO dTO2 : valueList) {

				for (OfferLetterFormulaDTO offer : setAnnualValueByMonthly) {

					if(!offer.getValue().trim().equalsIgnoreCase("") && dTO2.getId().split(" ")[0].trim().equalsIgnoreCase("Annual") && dTO2.getId().split(" ")[1].trim().equalsIgnoreCase(offer.getId().split(" ")[1].trim())){
						if(!dTO2.getId().equalsIgnoreCase("Annual CTC"))
							dTO2.setValue(String.valueOf(((int) Double.parseDouble(offer.getValue()))*12));
					}

				}

			}


		}catch(Exception e){
			logger.error("error occured "+e);
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}



		OfferLetterFormulaDTO totalCtcData = new OfferLetterFormulaDTO();
		String totalCtc = "";

		for (OfferLetterFormulaDTO offerLetterFormulaDTO : valueList) {

			if(offerLetterFormulaDTO.getId().equalsIgnoreCase("Annual CTC")){
				try{
					//if(offerLetterFormulaDTO.getValue()!=null && !offerLetterFormulaDTO.getValue().equals("") && !offerLetterFormulaDTO.getValue().isEmpty())
					totalCtc = offerLetterFormulaDTO.getValue();
				}catch(Exception e){

				}
			}
			if(!offerLetterFormulaDTO.getValue().trim().equalsIgnoreCase("") && !offerLetterFormulaDTO.getValue().isEmpty())
				offerLetterFormulaDTO.setValue(String.valueOf(((int) Double.parseDouble(offerLetterFormulaDTO.getValue()))));
		}

		totalCtcData.setId("Total CTC Amount");
		totalCtcData.setValue(String.valueOf(totalCtc));

		valueList.add(totalCtcData);
		return new RestResponse(RestResponse.SUCCESS, valueList, "Calculated formula's successfuly !!");
	}



	public static List<OfferLetterFormulaDTO> calculateFormulaOneByOne(List<OfferLetterFormulaDTO> formulaList, List<OfferLetterFormulaDTO> valueList){

		for (int j=0; j< formulaList.size();j++) {

			String formula = formulaList.get(j).getFormula();

			/*if(formula.contains(".")) {
				formula = calculatePointExistInFormula(formula);
			}*/

			double finalVaraibleValue = 0;
			String splitValue ="";

			List<String> valueArray = new ArrayList<>();
			List<String> opratorArray = new ArrayList<>();

			char[] formulaArray = formula.toCharArray(); 

			for (char c : formulaArray) {

				if(c=='*'){
					if(splitValue.equals("")){
						String[] first = formula.split("\\*",2);
						valueArray.add(first[0]);
						opratorArray.add("*");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("\\*",2);
						valueArray.add(first[0]);
						opratorArray.add("*");
						splitValue = first[1];
					}

				} else if(c=='/'){

					if(splitValue.equals("")){
						String[] first = formula.split("/",2);
						valueArray.add(first[0]);
						opratorArray.add("/");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("/",2);
						valueArray.add(first[0]);
						opratorArray.add("/");
						splitValue = first[1];
					}

				} else if(c=='+'){

					if(splitValue.equals("")){
						String[] first = formula.split("\\+",2);
						valueArray.add(first[0]);
						opratorArray.add("+");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("\\+",2);
						valueArray.add(first[0]);
						opratorArray.add("+");
						splitValue = first[1];
					}

				} else if(c=='-'){

					if(splitValue.equals("")){
						String[] first = formula.split("-",2);
						valueArray.add(first[0]);
						opratorArray.add("-");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("-",2);
						valueArray.add(first[0]);
						opratorArray.add("-");
						splitValue = first[1];
					}
				}


			}						

			if(!splitValue.equalsIgnoreCase(""))
				valueArray.add(splitValue);

			int getcorrectValue = 0;  // if value is 1 , than it is not correct
			double varaibleValue = 0;
			for(int i=0;i<valueArray.size();i++){


				int varaibleIntValue = getVaraibleValue(valueArray.get(i), valueList);

				if(valueArray.get(i).contains(".")){

					String withoutSpace = valueArray.get(i).replaceAll("\\s","");
					varaibleValue = Double.parseDouble(withoutSpace);
				}else if(varaibleValue == -1){
					getcorrectValue = 1;
					continue;
				}else{
					varaibleValue = varaibleIntValue;
				}


				if(finalVaraibleValue==0){
					finalVaraibleValue = finalVaraibleValue + varaibleValue;
				}else{

					String operatorValue = opratorArray.get(i-1);

					if(operatorValue.equalsIgnoreCase("*")){

						finalVaraibleValue = finalVaraibleValue * varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("/")){

						finalVaraibleValue = finalVaraibleValue / varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("+")){

						finalVaraibleValue = finalVaraibleValue + varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("-")){

						finalVaraibleValue = finalVaraibleValue - varaibleValue;

					}

				}


			}

			if(valueArray.size()==0)
				finalVaraibleValue = Integer.parseInt(formula);

			if(getcorrectValue==0){
				OfferLetterFormulaDTO dtoValues = new OfferLetterFormulaDTO();
				dtoValues.setId(formulaList.get(j).getId());
				dtoValues.setValue(round(Double.toString(finalVaraibleValue)));
				valueList.add(dtoValues);
			}else{
				valueList.add(formulaList.get(j));
			}

		}

		return valueList;

	}


	public static int getVaraibleValue(String valueArray, List<OfferLetterFormulaDTO> values){

		try{
			int intValue = 0;
			int k =0;int p = 0; 
			if(StringUtils.isNumeric(valueArray.trim())){

				intValue = Integer.parseInt(valueArray.trim());
				k =1; p=1;
			}else{

				for (OfferLetterFormulaDTO value : values) {	
					String data = value.getId().split(" ")[1].trim();

					if(data.equalsIgnoreCase(valueArray.trim())){
						if(valueArray.trim().equalsIgnoreCase("CTC") && !value.getId().equalsIgnoreCase("Annual CTC"))
							continue;

						k=1;
						if(value.getValue()!=null && !value.getValue().equals("") && !value.getValue().isEmpty()){
							p=1;
							//	intValue = Integer.parseInt(value.getValue());
							intValue = (int) Double.parseDouble(round(value.getValue()));
						}

					}		
				}

			}


			if(k==0 || p==0){
				return -1;
			}

			return intValue;

		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}

	}


	private static String calculatePointExistInFormula(String formula) {

		formula = formula.trim();
		String trimValueFormula = formula.replaceAll("\\s", "");
		String concatValueAfterPoint = "";
		String[] spilted = formula.split("\\.");
		String getPointValue = spilted[1].trim();
		char[] formulaArray = getPointValue.toCharArray(); 
		int i=0;
		for (char c : formulaArray) {

			if(c==' '){
				continue;
			}

			if(c=='*' || c=='/' || c=='+' || c=='-'){
				break;
			}
			concatValueAfterPoint = concatValueAfterPoint + c;
			i=i+1;
		}

		getPointValue = concatValueAfterPoint + "/1";

		for(int j=0;j<i;j++){
			getPointValue = getPointValue + "0";
		}

		String finalformula = trimValueFormula.replace("."+concatValueAfterPoint, getPointValue);

		return finalformula;
	}

	public RestResponse saveSelectedCustomReports(List<String> textArray) {

		String reportNameArray = "";
		try{
			for (int j=0;j<textArray.size();j++) {

				if(j==0){
					reportNameArray = reportNameArray + textArray.get(j);
				}else{
					reportNameArray = reportNameArray + "," +textArray.get(j);
				}	
			}

			List<SelectedCustomField> customReportList = selectedCustomReportsRepository.findAll();

			if(customReportList!=null && customReportList.size()>0){
				customReportList.get(0).setTextValues(reportNameArray);
				selectedCustomReportsRepository.save(customReportList.get(0));
			}else{
				SelectedCustomField data = new SelectedCustomField();
				data.setTextValues(reportNameArray);
				selectedCustomReportsRepository.save(data);
			}
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}

		return new RestResponse(RestResponse.SUCCESS, null, "save selected custom report successfuly !!");
	}

	public RestResponse getSelectedCustomReportList() {

		try{
			List<SelectedCustomField> customReportList = selectedCustomReportsRepository.findAll();
			if(customReportList!=null && customReportList.size()>0){
				String data = customReportList.get(0).getTextValues();
				String[] res = data.split(",");
				return new RestResponse(RestResponse.SUCCESS, res, "save selected custom report successfuly !!");
			}else{
				return new RestResponse(RestResponse.FAILED, null, "Not found Result !!");
			}
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
	}

	public RestResponse changeDuplicateCheckStatus(String status, Organization org) {
		try{
			org.setDuplicateCheck(status);
			organizationRepository.save(org);
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
		return new RestResponse(RestResponse.SUCCESS, null, "change duplicateCamdidateValue successfuly !!");
	}

	public RestResponse changeMandatoryDocsCheckStatus(String status, Organization org, String mandatoryDocs) {

		try{
			org.setMandatoryDocs(mandatoryDocs);
			org.setDocumentsCheck(status);
			organizationRepository.save(org);
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
		return new RestResponse(RestResponse.SUCCESS, null, "change duplicateCamdidateValue successfuly !!");
	}

	public RestResponse changeCheckRollOutOfferLetterStatus(String status, Organization org) {

		try{
			org.setCheckRolloutOfferletter(status);
			organizationRepository.save(org);
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
		return new RestResponse(RestResponse.SUCCESS, null, "change CheckRollOutOfferLetterStatus successfuly !!");
	}


	public String roundDown(String number, int numberIndex) {

		BigDecimal rawValue = new BigDecimal(number);
		int value = rawValue.setScale(numberIndex, RoundingMode.DOWN).intValue();

		return String.valueOf(value);
	}

	public String roundUp(String number, int numberIndex) {

		BigDecimal rawValue = new BigDecimal(number);
		int value = rawValue.setScale(numberIndex, RoundingMode.UP).intValue();

		return String.valueOf(value);
	}

	public static String round(String number) {
		int value = (int) Math.round(Double.valueOf(number));
		return String.valueOf(value);
	}

	public RestResponse madantoryDocsLeft(String candidateId) {


		try{
			RoundCandidateDTO candidateDTO = new RoundCandidateDTO();
			Organization org = organizationService.getOrgInfo();
			if (org != null && org.getDocumentsCheck()!=null && org.getDocumentsCheck().equalsIgnoreCase("Yes")){ 

				String docList = org.getMandatoryDocs();

				List<CandidateFile> files = candidateFileService.getCandidateFile(String.valueOf(candidateId));
				Set<String> canFiles = new HashSet<>();
				Set<String> listFiles = new HashSet<>();

				if(docList==null){
					listFiles.add(FileType.PAN_CARD.getDisplayName());listFiles.add(FileType.AADHAR_CARD.getDisplayName());listFiles.add(FileType.UPDATED_RESUME.getDisplayName());listFiles.add(FileType.TENTH_EDU_DOC.getDisplayName());
					listFiles.add(FileType.TWELETH_EDU_DOC.getDisplayName());listFiles.add(FileType.DEGREE_DOC.getDisplayName());listFiles.add(FileType.APPOINTMENT_LETTER.getDisplayName());listFiles.add(FileType.SALARY_SLIPS.getDisplayName());
					listFiles.add(FileType.RELIEVING_LETTER.getDisplayName());listFiles.add(FileType.PASSPORT_PHOTOGRAPH.getDisplayName());listFiles.add(FileType.STATEMENT_CHEQUE.getDisplayName());listFiles.add(FileType.ADDRESS_PROOF.getDisplayName());
				}else{
					if(docList.contains(",")){

						String[] docs = docList.split(",");
						listFiles.addAll(new HashSet<>(Arrays.asList(docs)));
					}else{
						listFiles.add(docList);
					}
				}
				for (CandidateFile file : files) {
					System.out.println(file.getFileType());
					for (String doc : listFiles) {
						if(file.getFileType().equalsIgnoreCase(doc.trim()))
							canFiles.add(doc);
					}
				}

				if(listFiles.size()>canFiles.size()){

					listFiles.removeAll(canFiles);
					candidateDTO.setPendingDocs(listFiles);
					candidateDTO.setMandatoryDocAvailable(false);

				}else{

					candidateDTO.setMandatoryDocAvailable(true);
					//	candidateDTO.setPendingDocs(new HashSet<>());
				}


			}

			return new RestResponse(RestResponse.SUCCESS, candidateDTO);

		}catch(Exception e){
			e.printStackTrace();

			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}


	}

	public RestResponse savePositionApplyFormFields(List<CandidateApplyFormFieldsDTO> fields,Organization org) {
		try{

			if(fields.size()>0){

				String id ="";
				String value ="";
				int i = 0;
				for (CandidateApplyFormFieldsDTO candidateApplyFormFieldsDTO : fields) {

					if(i==0){
						id = id + candidateApplyFormFieldsDTO.getId();
						value = value + candidateApplyFormFieldsDTO.getIsMandatory();

						i=1;
					}else{
						id = id +","+candidateApplyFormFieldsDTO.getId();
						value = value +","+ candidateApplyFormFieldsDTO.getIsMandatory();
					}

				}

				org.setCandidateFormFields(id);
				org.setCandidateMandatoryFields(value);
				organizationRepository.save(org);
			}

		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}
		return new RestResponse(RestResponse.SUCCESS, null, "change savePositionApplyFormFields successfuly !!");

	}

	public RestResponse getPositionApplyFormFields(Organization org) {

		try{
			List<CandidateApplyFormFieldsDTO> listData = new ArrayList<>();

			String fields = org.getCandidateFormFields();
			String mandFields = org.getCandidateMandatoryFields();

			if(fields.contains(",")){

				String[] listFields = fields.split(",");
				String[] listManFields = mandFields.split(",");

				for(int i=0;i<listFields.length;i++){
					CandidateApplyFormFieldsDTO dto = new CandidateApplyFormFieldsDTO();

					dto.setId(listFields[i]);
					dto.setIsMandatory(listManFields[i]);

					listData.add(dto);
				}

			}else{
				CandidateApplyFormFieldsDTO dto = new CandidateApplyFormFieldsDTO();

				dto.setId(fields);
				dto.setIsMandatory(mandFields);

				listData.add(dto);
			}

			return new RestResponse(RestResponse.SUCCESS, listData, " success !!");

		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, null, "Internal sever error  !!");
		}


	}



}
