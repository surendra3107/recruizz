package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.OrganizationConfiguration;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CareerPortalSource;
import com.bbytes.recruiz.enums.Communication;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.Gender;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.integration.servetel.ServetelCallDetailReponseDto;
import com.bbytes.recruiz.integration.servetel.ServetelService;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateApplyFormFieldsDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterFormulaDTO;
import com.bbytes.recruiz.rest.dto.models.OrgannizationPermissionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.CustomFieldService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PasswordHashService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UniqueIdentifierGeneratorService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.AppSettingsGenerator;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class OrganizationController {

	private static Logger logger = LoggerFactory.getLogger(OrganizationController.class);

	@Autowired
	private FileService fileService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private PasswordHashService passwordHashService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private UserService userService;

	@Autowired
	private ServetelService servetelService;
	
	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private UniqueIdentifierGeneratorService uniqueIdentifierGeneratorService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private CustomFieldService customFieldService;

	@Value("${base.url}")
	private String baseUrl;

	@RequestMapping(value = "/api/v1/organization/settings", method = RequestMethod.PUT)
	public RestResponse updateSettings(@RequestParam("orgId") String orgId)
			throws IOException, RecruizException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UpdateOrganizationSetting.name());

		Organization org = organizationService.findByOrgId(orgId);
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		OrganizationConfiguration config = org.getOrganizationConfiguration();
		if (config == null) {
			config = new OrganizationConfiguration();
		}
		config.setSettingInfo(AppSettingsGenerator.generateAndSaveSettings(orgId));
		org.setOrganizationConfiguration(config);
		organizationService.save(org);

		return new RestResponse(SuccessHandler.SUCCESS, SuccessHandler.ORGANIZATION_SEETINGS_UPDATED);
	}

	@RequestMapping(value = "/api/v1/organization", method = RequestMethod.GET)
	public RestResponse getOrganizationInfo() {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetOrganizationDetails.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}
		// Organization org =
		// organizationService.findByOrgId(TenantContextHolder.getTenant());
		if (org.getLogoUrlPath() != null && !org.getLogoUrlPath().isEmpty()) {
			String url = baseUrl + "/pubset/" + org.getLogoUrlPath();
			org.setOrgLogoUrl(url);
		}

		org.setOrganizationEmail(uniqueIdentifierGeneratorService.generateUniqueResumeEmailForOrganization());
		return new RestResponse(RestResponse.SUCCESS, org);
	}

	@RequestMapping(value = "/api/v1/organization/name", method = RequestMethod.GET)
	public RestResponse getOrganizationDisplayName() {
		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}
		return new RestResponse(RestResponse.SUCCESS, org.getOrgName());
	}

	/**
	 * The token is used by external apps like recruiz career site
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/organization/authtoken", method = RequestMethod.GET)
	public RestResponse getOrganizationLevelAuthToken() {

		/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetOrgLevelAuthToken.name());*/

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		String orgAPIToken = null;
		try {
			orgAPIToken = tokenAuthenticationProvider.getOrganizationApiToken();
		} catch (RecruizException e) {
			logger.error(e.getMessage(), e);
		}

		if (orgAPIToken == null) {
			return new RestResponse(false, "Organization api key generation failed",
					ErrorHandler.ORG_API_TOKEN_CREATE_FAILED);
		}

		return new RestResponse(RestResponse.SUCCESS, orgAPIToken);
	}

	/**
	 *
	 * markForDeleteOrganization method is used to set mark for delete
	 * organization (Complete organization related database will be deleted
	 * after no of days with running thread logic)
	 *
	 * @author - Akshay
	 * @param markForDeleteState
	 * @param days
	 * @return
	 * @throws RecruizWarnException
	 * @throws PlutusClientException
	 */
	@RequestMapping(value = "/api/v1/organization/markdelete", method = RequestMethod.DELETE)
	public RestResponse markForDeleteOrganization(@RequestParam(value = "markForDeleteState") String markForDeleteState,
			@RequestParam(value = "days") int days, @RequestParam(value = "password") String password)
					throws RecruizWarnException, PlutusClientException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.MarkForDeleteOrganization.name());

		int hours;

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User loggedInUser = userService.getLoggedInUserObject();
		User tenantDBUser = tenantResolverService.findUserByEmail(loggedInUser.getEmail());
		if (!(passwordHashService.passwordMatches(password, tenantDBUser.getPassword())))
			throw new RecruizWarnException(GlobalConstants.PASSWORD_WRONG, ErrorHandler.CURRENT_PASSWORD_INCORRECT);
		if (days == 0) {
			// user passes 0 no of days from UI then system will set 3 days by
			// default
			hours = GlobalConstants.DEFAULT_MARK_FOR_DELETE_DAYS * 24;
		} else {
			hours = days * 24;
		}
		Organization organization = organizationService.markforDeleteOrganization(
				Boolean.parseBoolean(markForDeleteState), loggedInUser.getOrganization().getOrgId(), hours);

		logger.debug("Organization with tenant id  '" + loggedInUser.getOrganization().getOrgId()
				+ "' is marked for delete");
		RestResponse reponse = new RestResponse(RestResponse.SUCCESS, organization);

		return reponse;
	}

	@RequestMapping(value = "/api/v1/organization/dummy/data/status", method = RequestMethod.PUT)
	public RestResponse getDummyDataStatus(@RequestParam String dummyDataStatus)
			throws RecruizWarnException, PlutusClientException {

		Organization org = organizationService.getCurrentOrganization();
		org.setDummyDataStatus(dummyDataStatus);
		organizationService.save(org);

		return new RestResponse(RestResponse.SUCCESS, org);
	}

	@RequestMapping(value = "/api/v1/fields/custom/add", method = RequestMethod.POST)
	public RestResponse addCustomFields(@RequestBody List<CustomFields> customFields) throws Exception {
		List<CustomFields> addedCustomFields = customFieldService.save(customFields);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, addedCustomFields, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/fields/custom/delete/{id}/{type}", method = RequestMethod.DELETE)
	public RestResponse deleteCustomFields(@PathVariable Long id, @PathVariable String type) throws Exception {
		customFieldService.delete(id);
		List<CustomFields> customFields = customFieldService.getAllFieldsByEntity(CustomFieldEntityType.valueOf(type));
		RestResponse response = new RestResponse(RestResponse.SUCCESS, customFields, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/fields/custom/edit/{id}", method = RequestMethod.PUT)
	public RestResponse editCustomField(@PathVariable Long id, @RequestBody CustomFields fields) throws Exception {

		fields.setId(id);
		fields = customFieldService.save(fields);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, fields, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/fields/custom/all/{type}", method = RequestMethod.GET)
	public RestResponse getAllCustomField(@PathVariable String type) throws Exception {
		List<CustomFields> fields = customFieldService.getAllFieldsByEntity(CustomFieldEntityType.valueOf(type));
		RestResponse response = new RestResponse(RestResponse.SUCCESS, fields, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/organization/settings/enable/custom/report", method = RequestMethod.PUT)
	public RestResponse updateSettings(@RequestParam Boolean enabled)
			throws IOException, RecruizException, ParseException {

		Organization org = organizationService.findByOrgId(TenantContextHolder.getTenant());
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		OrganizationConfiguration config = org.getOrganizationConfiguration();
		if (config == null) {
			config = new OrganizationConfiguration();
		}
		config.setCustomReportEnabled(enabled);
		org.setOrganizationConfiguration(config);
		organizationService.save(org);

		return new RestResponse(true, org);
	}

	@RequestMapping(value = "/api/v1/organization/setting", method = RequestMethod.GET)
	public RestResponse getOrganizationSetting() {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}
		OrganizationConfiguration conf = org.getOrganizationConfiguration();

		return new RestResponse(RestResponse.SUCCESS, conf);
	}


	@RequestMapping(value = "/api/v1/offer/getOfferTemplateList", method = RequestMethod.GET)
	public RestResponse getOfferTemplateList() {

		return organizationService.getOfferTemplateList();
	}


	@RequestMapping(value = "/api/v1/offer/deleteOfferTemplateById", method = RequestMethod.GET)
	public RestResponse deleteOfferTemplateById(@RequestParam  long offerTemplateId) {

		return organizationService.deleteOfferTemplateById(offerTemplateId);
	}

	@RequestMapping(value = "/api/v1/offer/getOfferTemplateById", method = RequestMethod.GET)
	public RestResponse getOfferTemplateById(@RequestParam  long offerTemplateId,@RequestParam(value = "positionId", required=false) String positionId,@RequestParam(value = "candidateId", required=false) String candidateId) {

		return organizationService.getOfferTemplateById(offerTemplateId,positionId,candidateId);
	}


	@RequestMapping(value = "/api/v1/offer/selectOfferTemplateById", method = RequestMethod.GET)
	public RestResponse selectOfferTemplateById(@RequestParam  long offerTemplateId) {

		return organizationService.selectOfferTemplateById(offerTemplateId);
	}


	@RequestMapping(value = "/api/v1/org/offer/uploadCandidateOfferTemplate", method = RequestMethod.POST)
	public RestResponse uploadCandidateOfferTemplate(@RequestPart("file") MultipartFile file, @RequestParam  String offerTemplateName)
			throws IllegalStateException, RecruizException, IOException {


		if(offerTemplateName==null || offerTemplateName.equals("") || offerTemplateName.isEmpty())
			return new RestResponse(RestResponse.FAILED, null, "offerTemplateName is required !! ");
		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.UploadCandidateOfferLetter.name());

		File offerFile = fileService.multipartToFile(file);       

		return organizationService.uploadOfferLetterAndReadVaraibles(offerFile,offerTemplateName);
	}


	@RequestMapping(value = "/api/v1/org/offer/saveOfferLetterTemplateFormula", method = RequestMethod.GET)
	public RestResponse saveOfferLetterTemplateFormula(@RequestParam List<String> annualComponent, @RequestParam List<String> monthlyComponent, @RequestParam List<String> annualDeduction,
			@RequestParam List<String> monthlyDeduction,@RequestParam  String monthCostToCompany,@RequestParam String annualCostToCompany,@RequestParam  long offerTemplateId, @RequestParam  boolean template_with_formula)
					throws IllegalStateException, RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.saveOfferLetterTemplateFormula.name());

		return organizationService.saveOfferLetterTemplateFormula(monthCostToCompany,annualCostToCompany,annualComponent, monthlyComponent, annualDeduction, monthlyDeduction, offerTemplateId, template_with_formula);
		//return organizationService.saveOfferLetterTemplateMonthlyFormula(annualComponent, monthlyComponent, annualDeduction, monthlyDeduction, offerTemplateId, template_with_formula);
	}


	/*@RequestMapping(value = "/api/v1/org/offer/monthly/saveOfferLetterTemplateFormula", method = RequestMethod.GET)
	public RestResponse saveOfferLetterTemplateMonthlyFormula(@RequestParam List<String> annualComponent, @RequestParam List<String> monthlyComponent, @RequestParam List<String> annualDeduction,
			@RequestParam List<String> monthlyDeduction,@RequestParam  long offerTemplateId, @RequestParam  boolean template_with_formula)
					throws IllegalStateException, RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.saveOfferLetterTemplateFormula.name());

		return organizationService.saveOfferLetterTemplateMonthlyFormula(annualComponent, monthlyComponent, annualDeduction, monthlyDeduction, offerTemplateId, template_with_formula);

	}*/



	@RequestMapping(value = "/api/v1/org/offer/generateOfferLetterForPreview", method = RequestMethod.POST)
	public RestResponse generateOfferLetterForPreview(@RequestParam long positionCode,@RequestParam List<String> text, @RequestParam List<String> annualComponent, @RequestParam List<String> monthlyComponent, @RequestParam List<String> annualDeduction,
			@RequestParam List<String> monthlyDeduction,@RequestParam  long offerTemplateId, @RequestParam  long candidateId,@RequestParam(value = "joiningBonusAmount", required=false) String joiningBonusAmount)
					throws IllegalStateException, RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.generateOfferLetterForPreview.name());

		return organizationService.generateOfferLetterForPreview(positionCode,text, annualComponent, monthlyComponent, annualDeduction, monthlyDeduction, offerTemplateId, candidateId,joiningBonusAmount);

	}


	@RequestMapping(value = "/api/v1/org/offer/file/viewOfferLetterForPreview", method = RequestMethod.GET)
	public void viewOfferLetterForPreview(HttpServletResponse response, @RequestParam  long candidateId,@RequestParam String positionId)
			throws IllegalStateException, RecruizException, IOException {

		organizationService.sendFileResponse(response,candidateId,positionId);

	}


	@RequestMapping(value = "/api/v1/org/offer/file/getOfferLetterListByCandidateId", method = RequestMethod.GET)
	public RestResponse getOfferLetterListByCandidateId(HttpServletResponse response, @RequestParam  long candidateId)
			throws IllegalStateException, RecruizException, IOException {

		return organizationService.getListOfOfferLetter(candidateId);

	}

	@RequestMapping(value = "/api/v1/org/offer/file/deleteOfferLetterByCandidateId", method = RequestMethod.GET)
	public RestResponse deleteOfferLetterByCandidateId(@RequestParam  long candidateId,@RequestParam  String filePath)
			throws IllegalStateException, RecruizException, IOException {

		return organizationService.deleteOfferLetterByCandidateId(candidateId, filePath);

	}


	@RequestMapping(value = "/api/v1/org/offer/saveAndGenerateOfferLetterForCandidate", method = RequestMethod.POST)
	public RestResponse saveFinalOfferLetterForCandidate(@RequestParam long approvalId,@RequestParam long positionCode,@RequestParam List<String> textArray, @RequestParam List<String> annualComponent, @RequestParam List<String> monthlyComponent, @RequestParam List<String> annualDeduction,
			@RequestParam List<String> monthlyDeduction,@RequestParam  long offerTemplateId, @RequestParam  long candidateId,@RequestParam(value = "joiningBonusAmount", required=false) String joiningBonusAmount)
					throws IllegalStateException, RecruizException, IOException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.saveFinalOfferLetterForCandidate.name());

		return organizationService.saveFinalOfferLetterForCandidate(approvalId,positionCode,textArray, annualComponent, monthlyComponent, annualDeduction, monthlyDeduction, offerTemplateId, candidateId,joiningBonusAmount);

	}


	@RequestMapping(value = "/api/v1/org/offer/calculationOfTemplateFormula", method = RequestMethod.POST)
	public RestResponse calculationOfTemplateFormula(@RequestBody  List<OfferLetterFormulaDTO> dataList,@RequestParam  long offerTemplateId)
			throws IllegalStateException, RecruizException, IOException {   

		return organizationService.calculationOfTemplateFormula(dataList,offerTemplateId);

	}


	@RequestMapping(value = "/api/v1/custom/Reports/selectedCustomReports", method = RequestMethod.GET)
	public RestResponse saveSelectedCustomReports(@RequestParam List<String> textArray)
			throws IllegalStateException, RecruizException, IOException {   

		return organizationService.saveSelectedCustomReports(textArray);

	}


	@RequestMapping(value = "/api/v1/custom/Reports/getSelectedCustomReportList", method = RequestMethod.GET)
	public RestResponse getSelectedCustomReportList()
			throws IllegalStateException, RecruizException, IOException {   

		return organizationService.getSelectedCustomReportList();

	}


	@RequestMapping(value = "/api/v1/duplicate/candidate/check/changeDuplicateCheckStatus", method = RequestMethod.GET)
	public RestResponse changeDuplicateCheckStatus(@RequestParam String status)
			throws IllegalStateException, RecruizException, IOException {   

		if(status==null || status.equals("") || status.isEmpty())
			return new RestResponse(false, null, "staus value required !");

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.changeDuplicateCheckStatus(status,org);

	}


	@RequestMapping(value = "/api/v1/Docs/candidate/check/changeMandatoryDocsCheckStatus", method = RequestMethod.GET)
	public RestResponse changeMandatoryDocsCheckStatus(@RequestParam String status,@RequestParam(value = "mandatoryDocs", required=false) String mandatoryDocs)
			throws IllegalStateException, RecruizException, IOException {   

		if(status==null || status.equals("") || status.isEmpty())
			return new RestResponse(false, null, "staus value required !");

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.changeMandatoryDocsCheckStatus(status,org,mandatoryDocs);

	}


	@RequestMapping(value = "/api/v1/Docs/candidate/check/mandatoryDocsList", method = RequestMethod.GET)
	public RestResponse mandatoryDocsList()throws IllegalStateException, RecruizException, IOException {   

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		OrgannizationPermissionDTO dto = new OrgannizationPermissionDTO();

		dto.setCheckRolloutOfferletter(org.getCheckRolloutOfferletter());
		dto.setDocumentsCheck(org.getDocumentsCheck());
		dto.setDuplicateCheck(org.getDocumentsCheck());
		dto.setMandatoryDocs(org.getMandatoryDocs());

		return new RestResponse(RestResponse.SUCCESS, dto);

	}


	@RequestMapping(value = "/api/v1/duplicate/candidate/check/changeCheckRollOutOfferLetterStatus", method = RequestMethod.GET)
	public RestResponse changeCheckRollOutOfferLetterStatus(@RequestParam String status)
			throws IllegalStateException, RecruizException, IOException {   

		if(status==null || status.equals("") || status.isEmpty())
			return new RestResponse(false, null, "staus value required !");

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.changeCheckRollOutOfferLetterStatus(status,org);

	}



	@RequestMapping(value = "/api/v1/position/offerRollout/check/madantoryDocsLeft", method = RequestMethod.GET)
	public RestResponse madantoryDocsLeft(@RequestParam String candidateId)
			throws IllegalStateException, RecruizException, IOException {   

		if(candidateId==null || candidateId.equals("") || candidateId.isEmpty())
			return new RestResponse(false, null, "candidateId value required !");


		return organizationService.madantoryDocsLeft(candidateId);

	}




	@RequestMapping(value = "/api/v1/position/apply/candidate/savePositionApplyFormFields", method = RequestMethod.POST)
	public RestResponse savePositionApplyFormFields(@RequestBody List<CandidateApplyFormFieldsDTO> fields)
			throws IllegalStateException, RecruizException, IOException {   

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.savePositionApplyFormFields(fields,org);

	}


	@RequestMapping(value = "/auth/position/apply/candidate/getPositionApplyFormFields", method = RequestMethod.GET)
	public RestResponse getPositionApplyFormFields(@RequestParam String orgName)
			throws IllegalStateException, RecruizException, IOException {   


		if (orgName == null || orgName.trim().equalsIgnoreCase("")) {
			return new RestResponse(false,"","orgName Required !");
		}

		TenantContextHolder.setTenant(orgName);

		Organization org = organizationService.findByOrgId(orgName);
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.getPositionApplyFormFields(org);

	}


	@RequestMapping(value = "/api/v1/position/apply/candidate/getPositionApplyFormFields", method = RequestMethod.GET)
	public RestResponse getPositionApplyFormField()
			throws IllegalStateException, RecruizException, IOException {   


		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		return organizationService.getPositionApplyFormFields(org);

	}



	@RequestMapping(value = "/auth/position/apply/candidate/fields", method = RequestMethod.GET)
	public RestResponse getCandidateFileds(@RequestParam String orgName) {


		if (orgName == null || orgName.trim().equalsIgnoreCase("")) {
			return new RestResponse(false,"","orgName Required !");
		}

		TenantContextHolder.setTenant(orgName);

		List<BaseDTO> fieldMap = new ArrayList<BaseDTO>();
		Field[] fields = Candidate.class.getDeclaredFields();
		if (null != fields && fields.length != 0) {
			for (Field field : fields) {
				BaseDTO baseDTO = new BaseDTO();

				String fieldValue = candidateService.getCandidateFiledName(field.getName());
				//		System.out.println(field.getType().getCanonicalName()+" 1= " +field.getType().getName()+" 2= "+field.getType().getSimpleName()+" 3= "+field.getType().getTypeName()+" 4= "+field.getType().toGenericString()+" 5= "+field.getType().toString());
				if (null != fieldValue && !fieldValue.trim().isEmpty()) {

					baseDTO.setId(field.getName());
					baseDTO.setValue(fieldValue); 
					baseDTO.setType(field.getType().getSimpleName());

					if(field.getName().equalsIgnoreCase("dob")  || field.getName().equalsIgnoreCase("keySkills")){

						if(field.getName().equalsIgnoreCase("dob")){
							baseDTO.setId("dob_date");
						}

						if(field.getName().equalsIgnoreCase("keySkills")){
							baseDTO.setId("keySkill");
						}
					}

					fieldMap.add(baseDTO);
				}

			}
		}

		List<CustomFields> customFields = customFieldService.getAllFieldsByEntity(CustomFieldEntityType.Candidate);
		if (null != customFields && !customFields.isEmpty()) {
			for (CustomFields customField : customFields) {
				BaseDTO baseDTO = new BaseDTO();
				baseDTO.setId(customField.getName());
				baseDTO.setValue(customField.getName());
				fieldMap.add(baseDTO);
			}
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, fieldMap);
		return rangeResponse;
	}


	@RequestMapping(value = "/auth/position/apply/fields/custom/all/{type}", method = RequestMethod.GET)
	public RestResponse getAllCustomFieldForPositionApply(@RequestParam String orgName,@PathVariable String type) throws Exception {

		if (orgName == null || orgName.trim().equalsIgnoreCase("")) {
			return new RestResponse(false,"","orgName Required !");
		}

		TenantContextHolder.setTenant(orgName);

		List<CustomFields> fields = customFieldService.getAllFieldsByEntity(CustomFieldEntityType.valueOf(type));
		RestResponse response = new RestResponse(RestResponse.SUCCESS, fields, null);
		return response;
	}


	@RequestMapping(value = "/auth/position/apply/jobtype", method = RequestMethod.GET)
	public RestResponse getJobTypeDropdownList() {

		List<BaseDTO> jobTypeList = new ArrayList<BaseDTO>();
		for (EmploymentType jobType : EmploymentType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(jobType.name());
			baseDTO.setValue(jobType.getDisplayName());
			jobTypeList.add(baseDTO);
		}

		RestResponse jobTypeResponse = new RestResponse(RestResponse.SUCCESS, jobTypeList);

		return jobTypeResponse;
	}



	@RequestMapping(value = "/auth/position/apply/gender", method = RequestMethod.GET)
	public RestResponse getGenderDropdownList() {

		List<BaseDTO> genderList = new ArrayList<BaseDTO>();
		for (Gender gender : Gender.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(gender.name());
			baseDTO.setValue(gender.name());
			genderList.add(baseDTO);
		}

		RestResponse genderResponse = new RestResponse(RestResponse.SUCCESS, genderList);

		return genderResponse;
	}



	@RequestMapping(value = "/auth/position/apply/communication", method = RequestMethod.GET)
	public RestResponse getCommunicationDropdownList() {

		List<BaseDTO> communicationList = new ArrayList<BaseDTO>();
		for (Communication communication : Communication.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(communication.name());
			baseDTO.setValue(communication.name());
			communicationList.add(baseDTO);
		}

		RestResponse communicationResponse = new RestResponse(RestResponse.SUCCESS, communicationList);

		return communicationResponse;
	}


	@RequestMapping(value = "/auth/position/apply/source", method = RequestMethod.GET)
	public RestResponse getCandidateSource() {

		List<BaseDTO> sourceList = new ArrayList<BaseDTO>();
		for (Source source : Source.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(source.name());
			baseDTO.setValue(source.getDisplayName());
			sourceList.add(baseDTO);
		}

		RestResponse sourceResponse = new RestResponse(RestResponse.SUCCESS, sourceList);

		return sourceResponse;
	}

	@RequestMapping(value = "/auth/position/apply/careerportal/sourcedetails", method = RequestMethod.GET)
	public RestResponse getCareerPortalSourceDetails() {

		List<BaseDTO> sourceDetailsList = new ArrayList<BaseDTO>();
		for (CareerPortalSource careerPortal : CareerPortalSource.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(careerPortal.name());
			baseDTO.setValue(careerPortal.getDisplayName());
			sourceDetailsList.add(baseDTO);
		}

		RestResponse sourceResponse = new RestResponse(RestResponse.SUCCESS, sourceDetailsList);

		return sourceResponse;
	}

	
	@RequestMapping(value = "/api/v1/organization/IVR/integration", method = RequestMethod.GET)
	public RestResponse setIVRCallingIntegration(@RequestParam String ivr) {

		Organization org = organizationService.getOrgInfo();
		if (org == null) {
			return new RestResponse(false, ErrorHandler.ORGANIZATION_DOES_NOT_EXISTS, ErrorHandler.NO_ORG_REGISTERED);
		}

		try{
			org.setIvrCallingIntegration(ivr);
			organizationService.save(org);
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, "Internal server error");
		}
		return new RestResponse(RestResponse.SUCCESS, "");
	}


	@RequestMapping(value = "/auth/servetel/call/getCallActivityDetails", method = RequestMethod.POST)
	public void getCallActivityDetails(@RequestBody ServetelCallDetailReponseDto dto) {

		logger.error("call getCallActivityDetails successfully ****************** = "+dto.toString());
		
		servetelService.updateCallActivityDetails(dto);
		
	}


}
