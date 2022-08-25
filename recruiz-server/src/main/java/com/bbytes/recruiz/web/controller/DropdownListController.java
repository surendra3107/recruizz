package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.enums.AdvancedSearchTabs;
import com.bbytes.recruiz.enums.AppUsageStatRange;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CampaignType;
import com.bbytes.recruiz.enums.CandidateModificationRange;
import com.bbytes.recruiz.enums.CareerPortalSource;
import com.bbytes.recruiz.enums.CategoryOptions;
import com.bbytes.recruiz.enums.CloseByDateRange;
import com.bbytes.recruiz.enums.Communication;
import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.enums.CustomerFeedbackType;
import com.bbytes.recruiz.enums.EmailClientType;
import com.bbytes.recruiz.enums.EmailTemplateCategory;
import com.bbytes.recruiz.enums.EmployeeStatus;
import com.bbytes.recruiz.enums.EmployeeType;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.ExpectedCTCRange;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.Gender;
import com.bbytes.recruiz.enums.InactiveSinceRange;
import com.bbytes.recruiz.enums.IndustryOptions;
import com.bbytes.recruiz.enums.InvoiceStatus;
import com.bbytes.recruiz.enums.MetricsTimePeriod;
import com.bbytes.recruiz.enums.NoticePeriodRange;
import com.bbytes.recruiz.enums.OnBoardingCategory;
import com.bbytes.recruiz.enums.PositionRequestStatus;
import com.bbytes.recruiz.enums.ProspectActivityType;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.enums.RemoteWork;
import com.bbytes.recruiz.enums.ReportInterval;
import com.bbytes.recruiz.enums.ReportTimePeriod;
import com.bbytes.recruiz.enums.RolePermission;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.TemplateCategory;
import com.bbytes.recruiz.enums.VendorType;
import com.bbytes.recruiz.enums.Vertical;
import com.bbytes.recruiz.enums.ViewUsageType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.BoardCustomStatusService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckAppSettingsService;
import com.bbytes.recruiz.service.CustomFieldService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;

/**
 * Dropdown list Controller for various dropdown list
 *
 * @author akshay
 *
 */

@RestController
@RequestMapping(value = "/api/v1")
public class DropdownListController {

	@Autowired
	private CustomFieldService customFieldService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;

	/**
	 * getJobTypeDropdownList method is used to return job types.
	 *
	 * @return
	 */
	@RequestMapping(value = "/jobtype", method = RequestMethod.GET)
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

	/**
	 * getRemoteWorkDropdownList method is used to return remote working
	 * options.
	 *
	 * @return
	 */
	@RequestMapping(value = "/remotework", method = RequestMethod.GET)
	public RestResponse getRemoteWorkDropdownList() {

		List<BaseDTO> remoteWorkList = new ArrayList<BaseDTO>();
		for (RemoteWork remoteWork : RemoteWork.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(remoteWork.name());
			baseDTO.setValue(String.valueOf(remoteWork.isRemoteValue()));
			remoteWorkList.add(baseDTO);
		}

		RestResponse remoteWorkResponse = new RestResponse(RestResponse.SUCCESS, remoteWorkList);

		return remoteWorkResponse;
	}

	/**
	 * getGenderDropdownList method is used to return gender options.
	 *
	 * @return
	 */
	@RequestMapping(value = "/gender", method = RequestMethod.GET)
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

	/**
	 * getCommunicationDropdownList method is used to return communication
	 * options.
	 *
	 * @return
	 */
	@RequestMapping(value = "/communication", method = RequestMethod.GET)
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

	@RequestMapping(value = "/currency", method = RequestMethod.GET)
	public RestResponse getCurrencyDropdownList() {

		List<BaseDTO> currencyList = new ArrayList<BaseDTO>();
		for (Currency currency : Currency.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(currency.name());
			baseDTO.setValue(currency.getUnicode());
			currencyList.add(baseDTO);
		}

		RestResponse currencyResponse = new RestResponse(RestResponse.SUCCESS, currencyList);

		return currencyResponse;
	}

	@RequestMapping(value = "/board/status", method = RequestMethod.GET)
	public RestResponse getCandidateStatusList() {

		List<BaseDTO> statusList = new ArrayList<BaseDTO>();
		for (BoardStatus status : BoardStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(status.name());
			baseDTO.setValue(status.getDisplayName());
			statusList.add(baseDTO);
		}

		List<BoardCustomStatus> customStatus = boardCustomStatusService.findAll();
		if (null != customStatus && !customStatus.isEmpty()) {
			for (BoardCustomStatus boardCustomStatus : customStatus) {
				BaseDTO baseDTO = new BaseDTO();
				baseDTO.setId(boardCustomStatus.getStatusKey());
				baseDTO.setValue(boardCustomStatus.getStatusName());
				statusList.add(baseDTO);
			}
		}

		RestResponse statusResponse = new RestResponse(RestResponse.SUCCESS, statusList);

		return statusResponse;
	}

	@RequestMapping(value = "/source", method = RequestMethod.GET)
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

	@RequestMapping(value = "/careerportal/sourcedetails", method = RequestMethod.GET)
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

	@RequestMapping(value = "/roundtype", method = RequestMethod.GET)
	public RestResponse getRoundType() {

		List<BaseDTO> roundTypeList = new ArrayList<BaseDTO>();
		for (RoundType roundType : RoundType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(roundType.name());
			baseDTO.setValue(roundType.getDisplayName());
			roundTypeList.add(baseDTO);
		}

		RestResponse roundTypeResponse = new RestResponse(RestResponse.SUCCESS, roundTypeList);

		return roundTypeResponse;
	}

	@RequestMapping(value = "/status/all", method = RequestMethod.GET)
	public RestResponse getStatusForCandidatePositionClient() {

		List<BaseDTO> statusOption = new ArrayList<BaseDTO>();
		for (Status status : Status.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(status.name());
			baseDTO.setValue(status.getDisplayName());
			statusOption.add(baseDTO);
		}

		RestResponse statusResponse = new RestResponse(RestResponse.SUCCESS, statusOption);

		return statusResponse;
	}

	@RequestMapping(value = "/candidate/status/all", method = RequestMethod.GET)
	public RestResponse getCandidateStatus() {

		List<BaseDTO> statusOption = new ArrayList<BaseDTO>();
		for (Status status : Status.values()) {
			if (!Status.StopSourcing.equals(status) && !Status.Closed.equals(status)) {
				BaseDTO baseDTO = new BaseDTO();
				baseDTO.setId(status.name());
				baseDTO.setValue(status.getDisplayName());
				statusOption.add(baseDTO);
			}
		}

		RestResponse statusResponse = new RestResponse(RestResponse.SUCCESS, statusOption);
		return statusResponse;
	}

	@RequestMapping(value = "/closebydate", method = RequestMethod.GET)
	public RestResponse getOptionForCloseByDate() {

		List<BaseDTO> closeByDateList = new ArrayList<BaseDTO>();
		for (CloseByDateRange date : CloseByDateRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(date.name());
			baseDTO.setValue(date.getDisplayName());
			closeByDateList.add(baseDTO);
		}

		RestResponse statusResponse = new RestResponse(RestResponse.SUCCESS, closeByDateList);

		return statusResponse;
	}

	@RequestMapping(value = "/ctcrange", method = RequestMethod.GET)
	public RestResponse getExpectedCTCRange() {

		List<BaseDTO> ctcRanges = new ArrayList<BaseDTO>();
		for (ExpectedCTCRange range : ExpectedCTCRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(range.name());
			baseDTO.setValue(range.getDisplayName());
			ctcRanges.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, ctcRanges);

		return rangeResponse;
	}

	@RequestMapping(value = "/noticeperiod", method = RequestMethod.GET)
	public RestResponse getNoticePeriodRange() {

		List<BaseDTO> noticeRanges = new ArrayList<BaseDTO>();
		for (NoticePeriodRange range : NoticePeriodRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(range.name());
			baseDTO.setValue(range.getDisplayName());
			noticeRanges.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, noticeRanges);

		return rangeResponse;
	}

	@RequestMapping(value = "/advancedsearch/tabs", method = RequestMethod.GET)
	public RestResponse getAdvancedSearchTabs() throws UnknownHostException, IOException, ParseException {
		if (checkAppSettingsService.isValidityExpired()) {
			return new RestResponse(false, ErrorHandler.RENEW_LICENCE, ErrorHandler.LICENCE_EXPIRED);
		}

		// checking for advance search feature is enable or not
		if (!checkAppSettingsService.isAdvancedSearchFeatureEnabled()) {
			return new RestResponse(false, ErrorHandler.FEATURE_NOT_ENABLED, ErrorHandler.FEATURE_NOT_ALLOWED);
		}

		List<BaseDTO> tabList = new ArrayList<BaseDTO>();
		for (AdvancedSearchTabs tab : AdvancedSearchTabs.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(tab.name());
			baseDTO.setValue(tab.getDisplayName());
			tabList.add(baseDTO);
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, tabList);

		return response;
	}

	@RequestMapping(value = "/experience", method = RequestMethod.GET)
	public RestResponse getExperienceRange() {

		List<BaseDTO> expRanges = new ArrayList<BaseDTO>();
		for (ExperinceRange range : ExperinceRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(range.name());
			baseDTO.setValue(range.getDisplayName());
			expRanges.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, expRanges);

		return rangeResponse;
	}

	/**
	 * get position list for HR Executive
	 *
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/hr/position", method = RequestMethod.GET)
	public RestResponse getHRPositionList() throws RecruizException {

		List<BaseDTO> positionList = new ArrayList<BaseDTO>();
		List<Position> positionListFromDB = positionService.getPositionByHrExecutive();
		if (positionListFromDB == null || positionListFromDB.isEmpty()) {
			throw new RecruizWarnException(ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.NO_POSITION);
		}

		for (Position position : positionListFromDB) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(position.getPositionCode());
			baseDTO.setValue(position.getClient().getClientName() + "/" + position.getTitle());
			positionList.add(baseDTO);
		}
		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, positionList);

		return rangeResponse;
	}

	/**
	 * getting vendor type
	 *
	 * @return
	 */
	@RequestMapping(value = "/vendor/type", method = RequestMethod.GET)
	public RestResponse getVendorType() {

		List<BaseDTO> vendorTypes = new ArrayList<BaseDTO>();
		for (VendorType type : VendorType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			vendorTypes.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, vendorTypes);

		return rangeResponse;
	}

	/**
	 * getting time periods
	 *
	 * @return
	 */
	@RequestMapping(value = "/timeperiod", method = RequestMethod.GET)
	public RestResponse getTimePeriod() {

		List<BaseDTO> timePeriodList = new ArrayList<BaseDTO>();
		for (MetricsTimePeriod timePeriod : MetricsTimePeriod.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(timePeriod.name());
			baseDTO.setValue(timePeriod.name());
			timePeriodList.add(baseDTO);
		}

		RestResponse timePeriodResponse = new RestResponse(RestResponse.SUCCESS, timePeriodList);

		return timePeriodResponse;
	}

	/**
	 * getting report time periods
	 *
	 * @return
	 */
	@RequestMapping(value = "/report/timeperiod", method = RequestMethod.GET)
	public RestResponse getReportTimePeriod() {

		List<BaseDTO> timePeriodList = new ArrayList<BaseDTO>();
		for (ReportTimePeriod timePeriod : ReportTimePeriod.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(timePeriod.name());
			baseDTO.setValue(timePeriod.getDisplayName());
			timePeriodList.add(baseDTO);
		}

		RestResponse timePeriodResponse = new RestResponse(RestResponse.SUCCESS, timePeriodList);

		return timePeriodResponse;
	}

	/**
	 * getting report intervals
	 *
	 * @return
	 */
	@RequestMapping(value = "/report/interval", method = RequestMethod.GET)
	public RestResponse getReportInterval() {

		List<BaseDTO> timePeriodList = new ArrayList<BaseDTO>();
		for (ReportInterval timePeriod : ReportInterval.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(timePeriod.getIntervalValue());
			baseDTO.setValue(timePeriod.getDisplayName());
			timePeriodList.add(baseDTO);
		}

		RestResponse timePeriodResponse = new RestResponse(RestResponse.SUCCESS, timePeriodList);

		return timePeriodResponse;
	}

	/**
	 * get Template Category
	 *
	 * @return
	 */
	@RequestMapping(value = "/template/category", method = RequestMethod.GET)
	public RestResponse getTemplateCategory() {

		List<BaseDTO> templateCategoryList = new ArrayList<BaseDTO>();
		for (TemplateCategory category : TemplateCategory.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(category.name());
			baseDTO.setValue(category.getDisplayName());
			templateCategoryList.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, templateCategoryList);

		return rangeResponse;
	}

	@RequestMapping(value = "/status/position/request", method = RequestMethod.GET)
	public RestResponse getPositionRequestStatus() {

		List<BaseDTO> allReqPositionStatus = new ArrayList<BaseDTO>();
		for (PositionRequestStatus status : PositionRequestStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(status.name());
			baseDTO.setValue(status.getDisplayName());
			allReqPositionStatus.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, allReqPositionStatus);
		return rangeResponse;
	}

	@RequestMapping(value = "/email/templates/category", method = RequestMethod.GET)
	public RestResponse getEmailtemplateCategory() {

		List<BaseDTO> allEmailTemplateCategory = new ArrayList<BaseDTO>();
		for (EmailTemplateCategory category : EmailTemplateCategory.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(category.name());
			baseDTO.setValue(category.getDisplayName());
			allEmailTemplateCategory.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, allEmailTemplateCategory);
		return rangeResponse;
	}

	/**
	 * to get list of industry options
	 *
	 * @return
	 */
	@RequestMapping(value = "/industry/options", method = RequestMethod.GET)
	public RestResponse getIndustryOptions() {

		List<BaseDTO> allIndustryOptions = new ArrayList<BaseDTO>();
		for (IndustryOptions options : IndustryOptions.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(options.name());
			baseDTO.setValue(options.getDisplayName());
			allIndustryOptions.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, allIndustryOptions);
		return rangeResponse;
	}

	@RequestMapping(value = "/position/vertical", method = RequestMethod.GET)
	public RestResponse getVerticalOptions() {

		List<BaseDTO> allVerticalOptions = new ArrayList<BaseDTO>();
		for (Vertical vertical : Vertical.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(vertical.name());
			baseDTO.setValue(vertical.toString());
			allVerticalOptions.add(baseDTO);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, allVerticalOptions);
		return response;
	}

	/**
	 * to get list of Category options
	 *
	 * @return
	 */
	@RequestMapping(value = "/category/options", method = RequestMethod.GET)
	public RestResponse getCategoryOptions() {

		List<BaseDTO> allCategoryOptions = new ArrayList<BaseDTO>();
		for (CategoryOptions options : CategoryOptions.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(options.name());
			baseDTO.setValue(options.getDisplayName());
			allCategoryOptions.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, allCategoryOptions);
		return rangeResponse;
	}

	@RequestMapping(value = "/customer/feedback/type", method = RequestMethod.GET)
	public RestResponse getCustomeFeedbackTypes() {

		List<BaseDTO> allFeedbackTypes = new ArrayList<BaseDTO>();
		for (CustomerFeedbackType options : CustomerFeedbackType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(options.name());
			baseDTO.setValue(options.getType());
			allFeedbackTypes.add(baseDTO);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, allFeedbackTypes);
		return response;
	}

	@RequestMapping(value = "/prospect/activity/type", method = RequestMethod.GET)
	public RestResponse getProspectActivityTypeDropdownList() {

		List<BaseDTO> prospectActivityTypeList = new ArrayList<BaseDTO>();
		for (ProspectActivityType prospectActivityType : ProspectActivityType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(prospectActivityType.name());
			baseDTO.setValue(prospectActivityType.getDisplayName());
			prospectActivityTypeList.add(baseDTO);
		}
		RestResponse prospectActivityTypeResponse = new RestResponse(RestResponse.SUCCESS, prospectActivityTypeList,
				SuccessHandler.GET_SUCCESS);
		return prospectActivityTypeResponse;
	}

	@RequestMapping(value = "/prospect/status", method = RequestMethod.GET)
	public RestResponse getProspectStatusDropdownList() {

		List<BaseDTO> prospectStatusList = new ArrayList<BaseDTO>();
		for (ProspectStatus prospectStatus : ProspectStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(prospectStatus.name());
			baseDTO.setValue(prospectStatus.getDisplayName());
			prospectStatusList.add(baseDTO);
		}
		RestResponse prospectStatusResponse = new RestResponse(RestResponse.SUCCESS, prospectStatusList,
				SuccessHandler.GET_SUCCESS);
		return prospectStatusResponse;
	}

	@RequestMapping(value = "/invoice/status", method = RequestMethod.GET)
	public RestResponse getInvoicetStatusDropdownList() {

		List<BaseDTO> invoiceStatusList = new ArrayList<BaseDTO>();
		for (InvoiceStatus invoiceStatus : InvoiceStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(invoiceStatus.name());
			baseDTO.setValue(invoiceStatus.getDisplayName());
			invoiceStatusList.add(baseDTO);
		}
		RestResponse prospectStatusResponse = new RestResponse(RestResponse.SUCCESS, invoiceStatusList,
				SuccessHandler.GET_SUCCESS);
		return prospectStatusResponse;
	}

	@RequestMapping(value = "/campaign/type", method = RequestMethod.GET)
	public RestResponse getCampaignTypes() {

		List<BaseDTO> campaignTypeList = new ArrayList<BaseDTO>();
		for (CampaignType type : CampaignType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			campaignTypeList.add(baseDTO);
		}
		RestResponse prospectStatusResponse = new RestResponse(RestResponse.SUCCESS, campaignTypeList,
				SuccessHandler.GET_SUCCESS);
		return prospectStatusResponse;
	}

	@RequestMapping(value = "/email/client/type", method = RequestMethod.GET)
	public RestResponse getAllEmailClientType() {

		List<BaseDTO> emailClients = new ArrayList<BaseDTO>();
		for (EmailClientType type : EmailClientType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			emailClients.add(baseDTO);
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, emailClients, SuccessHandler.GET_SUCCESS);
		return response;
	}

	@RequestMapping(value = "/inactive/range", method = RequestMethod.GET)
	public RestResponse getInactiveRange() {

		List<BaseDTO> inactiveSinceRange = new ArrayList<BaseDTO>();
		for (InactiveSinceRange range : InactiveSinceRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(range.name());
			baseDTO.setValue(range.getDisplayName());
			inactiveSinceRange.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, inactiveSinceRange);

		return rangeResponse;
	}


	@RequestMapping(value = "/position/fields", method = RequestMethod.GET)
	public RestResponse getPositionFileds() {
		Set<String> fieldMap = new HashSet<>();

		candidateService.getPositionFiledName(fieldMap);

		List<CustomFields> customFields = customFieldService.getAllFieldsByEntity(CustomFieldEntityType.Position);
		if (null != customFields && !customFields.isEmpty()) {
			for (CustomFields customField : customFields) {
				fieldMap.add(customField.getName());
			}
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, fieldMap);
		return rangeResponse;
	}


	@RequestMapping(value = "/candidate/modification/range", method = RequestMethod.GET)
	public RestResponse getCandidateModicationRange() {

		List<BaseDTO> rangeValues = new ArrayList<BaseDTO>();
		for (CandidateModificationRange range : CandidateModificationRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(range.getRangeValue() + "");
			baseDTO.setValue(range.getDisplayName());
			rangeValues.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, rangeValues);

		return rangeResponse;
	}

	// drop down for on baording category
	@RequestMapping(value = "/on/boarding/category", method = RequestMethod.GET)
	public RestResponse getBoardingCategory() {

		LinkedList<BaseDTO> categories = new LinkedList<BaseDTO>();

		for (OnBoardingCategory category : OnBoardingCategory.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(category.name());
			baseDTO.setValue(category.getDisplayName());
			categories.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, categories);
		return rangeResponse;
	}

	@RequestMapping(value = "/employee/type", method = RequestMethod.GET)
	public RestResponse getEmployeeType() {
		LinkedList<BaseDTO> types = new LinkedList<BaseDTO>();
		for (EmployeeType type : EmployeeType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			types.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, types);
		return rangeResponse;
	}

	@RequestMapping(value = "/employee/status", method = RequestMethod.GET)
	public RestResponse getEmployeeStatusType() {
		LinkedList<BaseDTO> types = new LinkedList<BaseDTO>();
		for (EmployeeStatus type : EmployeeStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			types.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, types);
		return rangeResponse;
	}

	@RequestMapping(value = "/sixthsense/view/usage", method = RequestMethod.GET)
	public RestResponse getSixthSenseViewUsage() {
		LinkedList<BaseDTO> types = new LinkedList<BaseDTO>();
		for (ViewUsageType type : ViewUsageType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(type.name());
			baseDTO.setValue(type.getDisplayName());
			types.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, types);
		return rangeResponse;
	}

	// to get permissions
	@RequestMapping(value = "/userRoles/getAllPermission", method = RequestMethod.GET)
	public RestResponse getAllPermissions() {
		LinkedList<BaseDTO> types = new LinkedList<BaseDTO>();
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
			types.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, types);
		return rangeResponse;
	}

	// drop down for on app usage range
	@RequestMapping(value = "/app/usage/range", method = RequestMethod.GET)
	public RestResponse getAppusageRange() {

		LinkedList<BaseDTO> categories = new LinkedList<BaseDTO>();

		for (AppUsageStatRange category : AppUsageStatRange.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(category.name());
			baseDTO.setValue(category.getDisplayName());
			categories.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, categories);
		return rangeResponse;
	}

	
	@RequestMapping(value = "/candidate/fields", method = RequestMethod.GET)
	public RestResponse getCandidateFileds() {
		List<BaseDTO> fieldMap = new ArrayList<BaseDTO>();
		Field[] fields = Candidate.class.getDeclaredFields();
		if (null != fields && fields.length != 0) {
			for (Field field : fields) {
				BaseDTO baseDTO = new BaseDTO();

				String fieldValue = candidateService.getCandidateFiledName(field.getName());
				if (null != fieldValue && !fieldValue.trim().isEmpty()) {
					baseDTO.setId(field.getName());
					baseDTO.setValue(fieldValue);
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

	
}
