package com.bbytes.recruiz.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Campaign;
import com.bbytes.recruiz.domain.CampaignCandidate;
import com.bbytes.recruiz.domain.CampaignCandidateAction;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.CampaignCandidateActionType;
import com.bbytes.recruiz.enums.CampaignStatus;
import com.bbytes.recruiz.enums.CampaignType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.levelbar.LevelbarService;
import com.bbytes.recruiz.repository.CampaignRepositiory;
import com.bbytes.recruiz.rest.dto.models.CampaignCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.CampaignDTO;
import com.bbytes.recruiz.rest.dto.models.CampaignStatDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.ShareTestDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class CampaignService extends AbstractService<Campaign, Long> {

	private Logger logger = LoggerFactory.getLogger(CampaignService.class);

	private CampaignRepositiory campaignRepositiory;

	@Autowired
	public CampaignService(CampaignRepositiory campaignRepositiory) {
		super(campaignRepositiory);
		this.campaignRepositiory = campaignRepositiory;
	}

	@Autowired
	private CampaignCandidateService campaignMemberService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private UserService userService;

	@Autowired
	private CheckAppSettingsService checkAppSettingsService;

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private OrganizationConfigurationService organizationConfigurationService;

	@Autowired
	private LevelbarService levelbarService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private CampaignCandidateActionService campaignCandidateActionService;

	@Value("${base.url}")
	private String baseUrl;

	@Transactional(readOnly = true)
	public List<Campaign> getCampaignByType(String type) {
		return campaignRepositiory.findByType(type);
	}

	@Transactional(readOnly = true)
	public List<Campaign> getCampaignByTypeAndPositionCode(String type, String positionCode) {
		return campaignRepositiory.findByTypeAndPositionCode(type, positionCode);
	}

	@Transactional(readOnly = true)
	public Page<Campaign> getCampaignByType(String type, Pageable pageable) {
		return campaignRepositiory.findByType(type, pageable);
	}

	@Transactional(readOnly = true)
	public Page<Campaign> getCampaignByTypeAndPositionCode(String type, String positionCode, Pageable pageable) {
		return campaignRepositiory.findByTypeAndPositionCode(type, positionCode, pageable);
	}

	/**
	 * to add new campaign
	 * 
	 * @param campaignDTO
	 * @return
	 */
	@Transactional
	public Campaign addNewCampaign(CampaignDTO campaignDTO) {

		List<CampaignCandidate> campaignCandidates = new ArrayList<>();
		Set<User> campaignHrMembers = new HashSet<>();

		Campaign campaign = new Campaign();
		campaign.setClientId(campaignDTO.getClientId());
		campaign.setName(campaignDTO.getName());
		campaign.setPositionCode(campaignDTO.getPositionCode());
		campaign.setCampaignRenderedTemplate(campaignDTO.getCampaignRenderedTemplate());
		campaign.setCampaignSubjectTemplate(campaignDTO.getCampaignRenderedSubject());
		campaign.setType(campaignDTO.getType());
		campaign.setOwner(userService.getLoggedInUserEmail());
		if (campaignDTO.getCampaignStartDate() != null) {
			campaign.setStatus(CampaignStatus.Scheduled.getDisplayName());
			campaign.setStartDate(campaignDTO.getCampaignStartDate());
		} else {
			campaign.setStatus(CampaignStatus.NotScheduled.getDisplayName());
		}

		if (campaignDTO.getCampaignCandidates() != null && !campaignDTO.getCampaignCandidates().isEmpty()) {
			for (CampaignCandidateDTO campaigncandidateDTO : campaignDTO.getCampaignCandidates()) {
				CampaignCandidate campaignCandidate = new CampaignCandidate();
				campaignCandidate.setMemberEmailId(campaigncandidateDTO.getMemberEmailId());
				campaignCandidate.setMemberName(campaigncandidateDTO.getMemberName());
				campaignCandidate.setAddedToCampaignDate(new Date());
				campaignCandidate.setCampaign(campaign);
				campaignCandidate.setCampaignRunStatus(false);
				campaignCandidates.add(campaignCandidate);
			}
		}

		if (campaignDTO.getCampaignHrMembersEmail() != null && !campaignDTO.getCampaignHrMembersEmail().isEmpty()) {
			for (String email : campaignDTO.getCampaignHrMembersEmail()) {
				User hrUser = userService.getUserByEmail(email);
				campaignHrMembers.add(hrUser);
			}
		}

		// adding logged user by default
		campaignHrMembers.add(userService.getLoggedInUserObject());

		campaign.setCampaignCandidates(campaignCandidates);
		campaign.setCampaignHrMembers(campaignHrMembers);

		campaignRepositiory.save(campaign);

		return campaign;
	}

	/**
	 * to add new members to campaign
	 * 
	 * @param campaign
	 * @param campaignCandidateDTOList
	 * @return
	 */
	@Transactional
	public Campaign addCampaignCandidates(Campaign campaign, List<CampaignCandidateDTO> campaignCandidateDTOList) {

		List<CampaignCandidate> newCampaignCandidates = new ArrayList<>();

		if (campaignCandidateDTOList != null && !campaignCandidateDTOList.isEmpty()) {
			for (CampaignCandidateDTO campaignCandidateDTO : campaignCandidateDTOList) {
				CampaignCandidate campaignMember = new CampaignCandidate();
				campaignMember.setMemberEmailId(campaignCandidateDTO.getMemberEmailId());
				campaignMember.setMemberName(campaignCandidateDTO.getMemberName());
				campaignMember.setAddedToCampaignDate(new Date());
				campaignMember.setCampaign(campaign);
				campaignMember.setCampaignRunStatus(false);
				newCampaignCandidates.add(campaignMember);
			}
		}
		if (newCampaignCandidates != null && !newCampaignCandidates.isEmpty()) {
			campaignMemberService.save(newCampaignCandidates);
		}
		return campaign;
	}

	/**
	 * to update campaign
	 * 
	 * @param campaign
	 * @param campaignDTO
	 * @return
	 */
	@Transactional
	public Campaign updateCampaign(Campaign campaign, CampaignDTO campaignDTO) {

		campaign.setCampaignRenderedTemplate(campaignDTO.getCampaignRenderedTemplate());
		campaign.setCampaignSubjectTemplate(campaignDTO.getCampaignRenderedSubject());
		campaign.setName(campaignDTO.getName());
		campaign.setType(campaignDTO.getType());
		campaign.setPositionCode(campaignDTO.getPositionCode());
		campaign.setClientId(campaignDTO.getClientId());

		List<CampaignCandidate> newCampaignMembers = new ArrayList<>();
		if (campaignDTO.getCampaignCandidates() != null && !campaignDTO.getCampaignCandidates().isEmpty()) {
			for (CampaignCandidateDTO campaignMemberDTO : campaignDTO.getCampaignCandidates()) {
				CampaignCandidate campaignMember = new CampaignCandidate();
				campaignMember.setMemberEmailId(campaignMemberDTO.getMemberEmailId());
				campaignMember.setMemberName(campaignMemberDTO.getMemberName());
				campaignMember.setAddedToCampaignDate(new Date());
				campaignMember.setCampaign(campaign);
				campaignMember.setCampaignRunStatus(false);
				newCampaignMembers.add(campaignMember);
			}
		} else {
			campaign.getCampaignCandidates().clear();
		}

		if (newCampaignMembers != null && !newCampaignMembers.isEmpty()) {
			campaign.getCampaignCandidates().clear();
			campaign.getCampaignCandidates().addAll(newCampaignMembers);
		}

		Set<User> campaignHrMembers = new HashSet<>();
		if (campaignDTO.getCampaignHrMembersEmail() != null && !campaignDTO.getCampaignHrMembersEmail().isEmpty()) {
			for (String email : campaignDTO.getCampaignHrMembersEmail()) {
				User hrUser = userService.getUserByEmail(email);
				campaignHrMembers.add(hrUser);
			}
		}
		campaign.getCampaignHrMembers().clear();
		campaign.getCampaignHrMembers().addAll(campaignHrMembers);

		campaign = campaignRepositiory.save(campaign);
		return campaign;
	}

	/**
	 * to run a campaign
	 * 
	 * @param campaign
	 * @param campaignDTO
	 * @return
	 */
	public RestResponse runCampaign(Campaign campaign, CampaignDTO campaignDTO) {
		RestResponse response = null;
		try {
			switch (campaign.getType()) {
			case CampaignType.EmailReachOutConstant:
				runGenericEmailCampaign(campaign, campaignDTO);
				campaign.setStatus(CampaignStatus.Completed.getDisplayName());
				save(campaign);
				break;
			case CampaignType.PositionCampaignConstant:
				runGenericEmailCampaign(campaign, campaignDTO);
				campaign.setStatus(CampaignStatus.Completed.getDisplayName());
				save(campaign);
				break;
			case CampaignType.ProfileEnrichmentConstant:
				runProfileEnrichmentCampaign(campaign, campaignDTO);
				campaign.setStatus(CampaignStatus.Completed.getDisplayName());
				save(campaign);
				break;
			case CampaignType.OnlineAssessmentConstant:
				runOnlineAssesmentCampaign(campaign, campaignDTO);
				campaign.setStatus(CampaignStatus.Completed.getDisplayName());
				save(campaign);
				break;
			case CampaignType.IVRAssessmentConstant:
				runIVRAssessmentCampaign(campaign, campaignDTO);
				campaign.setStatus(CampaignStatus.Completed.getDisplayName());
				save(campaign);
				break;
			default:
				response = new RestResponse(false, ErrorHandler.CAMPAIGN_TYPE_NOT_FOUND,
						ErrorHandler.NO_CAMPAIGN_TYPE_FOUND);
				break;
			}
		} catch (Exception ex) {
			response = new RestResponse(false, ErrorHandler.SOMETHING_WENT_WRONG_WHILE_RUNNING,
					ErrorHandler.FAILED_TO_RUN_CAMPAIGN);
			logger.warn(ex.getMessage(), ex);
		}

		return response;
	}

	/**
	 * to run generic email template
	 * 
	 * @param campaign
	 * @param campaignDTO
	 * @throws RecruizException
	 * @throws IOException
	 * @throws MessagingException
	 */
	@Async
	private void runGenericEmailCampaign(Campaign campaign, CampaignDTO campaignDTO)
			throws RecruizException, MessagingException, IOException {
		// checking if the email usage is exceeded or it will exceed with
		// current campaign
		if (checkAppSettingsService.isEmailUsageLimitExceeded(campaign.getCampaignCandidates().size())) {
			throw new RecruizException(ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);
		}
		// looping through each member to send email campaign
		for (CampaignCandidate campaignMember : campaign.getCampaignCandidates()) {

			// 10 days
			int linkValidityHour = 24 * 10;
			String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(userService.getLoggedInUserEmail(),
					TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour,
					userService.getLoggedInUserObject().getTimezone(), userService.getLoggedInUserObject().getLocale());

			String emailLink = baseUrl + GlobalConstants.CAMPAIGN_URL + GlobalConstants.PASSKEY + xAuthToken
					+ GlobalConstants.CANDIDATE_EMAIL_PARAM + campaignMember.getMemberEmailId()
					+ GlobalConstants.POSITION_CODE + campaign.getPositionCode() + GlobalConstants.CAMPAIGN_ID_PARAM
					+ campaign.getId() + GlobalConstants.CAMPAIGN_MEMBER_ID_PARAM + campaignMember.getId()
					+ GlobalConstants.CAMPAIGN_TYPE + campaign.getType() + GlobalConstants.TENANT_PARAM
					+ TenantContextHolder.getTenant();

			String emailLinkNotInterested = baseUrl + GlobalConstants.CAMPAIGN_URL + GlobalConstants.PASSKEY
					+ xAuthToken + GlobalConstants.CANDIDATE_EMAIL_PARAM + campaignMember.getMemberEmailId()
					+ GlobalConstants.POSITION_CODE + campaign.getPositionCode() + GlobalConstants.CAMPAIGN_ID_PARAM
					+ campaign.getId() + GlobalConstants.CAMPAIGN_MEMBER_ID_PARAM + campaignMember.getId()
					+ GlobalConstants.CAMPAIGN_TYPE + campaign.getType() + GlobalConstants.TENANT_PARAM
					+ TenantContextHolder.getTenant() + GlobalConstants.NOT_INTERESTED_PARAM;

			String renderedEmailTemplate = emailTemplateDataService.getMasterRenderedTemplateForEmailCampaign(campaign,
					campaignMember.getMemberEmailId(), campaignMember.getMemberName(), emailLink, "View And Apply",
					emailLinkNotInterested);

			List<String> emailList = new ArrayList<>();
			// TODO
			// remove this check when moving to prod
			if (campaignMember.getMemberEmailId().endsWith("beyondbytes.co")
					|| campaignMember.getMemberEmailId().endsWith("beyondbytes.co.in")
					|| campaignMember.getMemberEmailId().endsWith("recruiz.com")) {
				emailList.add(campaignMember.getMemberEmailId());
			}

			if (emailList == null || emailList.isEmpty()) {
				continue;
			}

			MailgunSendResponse mailResponse = emailService.sendBulkEmail(emailList, renderedEmailTemplate,
					campaign.getCampaignSubjectTemplate(), null, userService.getLoggedInUserEmail(),null);

			if (mailResponse != null && mailResponse.isOk()) {
				// storing emailMessage id against this candidate
				String messageId = mailResponse.getId();
				messageId = messageId.replace("<", "");
				messageId = messageId.replace(">", "");
				campaignMember.setMailgunEmailId(messageId);

				CampaignCandidateAction action = new CampaignCandidateAction();
				action.setActionInterval(0);

				action.setActionResponse("Email Delivered To Candidate");
				action.setActionType(CampaignCandidateActionType.Delivered.getDisplayName());

				action.setCampaignCandidate(campaignMember);

				campaignMember.getActions().add(action);

				campaignMemberService.save(campaignMember);
				tenantResolverService.saveTenantAndMessageIdMap(TenantContextHolder.getTenant(), messageId);
			}
		}
	}

	/**
	 * To run profile enrichment campaign
	 * 
	 * @param campaign
	 * @param campaignRunDTO
	 * @throws RecruizException
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void runProfileEnrichmentCampaign(Campaign campaign, CampaignDTO campaignDTO)
			throws RecruizException, MessagingException, IOException {
		// checking if the email usage is exceeded or it will exceed with
		// current campaign
		if (checkAppSettingsService.isEmailUsageLimitExceeded(campaign.getCampaignCandidates().size())) {
			throw new RecruizException(ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);
		}
		// looping through each member to send email campaign
		for (CampaignCandidate campaignMember : campaign.getCampaignCandidates()) {

			// 10 days
			int linkValidityHour = 24 * 10;
			String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(userService.getLoggedInUserEmail(),
					TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour,
					userService.getLoggedInUserObject().getTimezone(), userService.getLoggedInUserObject().getLocale());

			String emailLink = baseUrl + GlobalConstants.INTERVIEW_FEEDBACK_URL + GlobalConstants.PASSKEY + xAuthToken
					+ "&" + GlobalConstants.CANDIDATE_EMAIL + campaignMember.getMemberEmailId()
					+ GlobalConstants.POSITION_CODE + campaign.getPositionCode() + GlobalConstants.CAMPAIGN_ID_PARAM
					+ campaign.getId() + GlobalConstants.CAMPAIGN_MEMBER_ID_PARAM + campaignMember.getId()
					+ GlobalConstants.CAMPAIGN_TYPE + campaign.getType();

			String emailLinkNotInterested = baseUrl + GlobalConstants.CAMPAIGN_URL + GlobalConstants.PASSKEY
					+ xAuthToken + GlobalConstants.CANDIDATE_EMAIL_PARAM + campaignMember.getMemberEmailId()
					+ GlobalConstants.POSITION_CODE + campaign.getPositionCode() + GlobalConstants.CAMPAIGN_ID_PARAM
					+ campaign.getId() + GlobalConstants.CAMPAIGN_MEMBER_ID_PARAM + campaignMember.getId()
					+ GlobalConstants.CAMPAIGN_TYPE + campaign.getType() + GlobalConstants.TENANT_PARAM
					+ TenantContextHolder.getTenant() + GlobalConstants.NOT_INTERESTED_PARAM;

			String renderedEmailTemplate = emailTemplateDataService.getMasterRenderedTemplateForEmailCampaign(campaign,
					campaignMember.getMemberEmailId(), campaignMember.getMemberName(), emailLink, "View And Apply",
					emailLinkNotInterested);

			List<String> emailList = new ArrayList<>();

			// TODO
			// remove this check when moving to prod
			if (campaignMember.getMemberEmailId().endsWith("beyondbytes.co")
					|| campaignMember.getMemberEmailId().endsWith("beyondbytes.co.in")
					|| campaignMember.getMemberEmailId().endsWith("recruiz.com")) {
				emailList.add(campaignMember.getMemberEmailId());
			}

			if (emailList == null || emailList.isEmpty()) {
				continue;
			}
			MailgunSendResponse mailResponse = emailService.sendBulkEmail(emailList, renderedEmailTemplate,
					campaign.getCampaignSubjectTemplate(), null, userService.getLoggedInUserEmail(),null);

			if (mailResponse != null && mailResponse.isOk()) {
				// storing emailMessage id against this candidate
				String messageId = mailResponse.getId();
				campaignMember.setMailgunEmailId(messageId);

				CampaignCandidateAction action = new CampaignCandidateAction();
				action.setActionInterval(0);

				action.setActionResponse("Email Delivered To Candidate");
				action.setActionType(CampaignCandidateActionType.Delivered.getDisplayName());

				action.setCampaignCandidate(campaignMember);

				campaignMember.getActions().add(action);

				campaignMemberService.save(campaignMember);
				tenantResolverService.saveTenantAndMessageIdMap(TenantContextHolder.getTenant(), messageId);

			}

		}
	}

	/**
	 * To run Online Assessment campaign
	 * 
	 * @param campaign
	 * @param campaignRunDTO
	 * @throws RecruizException
	 */
	@Transactional
	private void runOnlineAssesmentCampaign(Campaign campaign, CampaignDTO campaignDTO) throws RecruizException {
		// checking if the email usage is exceeded or it will exceed with
		// current campaign
		if (checkAppSettingsService.isEmailUsageLimitExceeded(campaign.getCampaignCandidates().size())) {
			throw new RecruizException(ErrorHandler.EMAIL_USAGE_EXCEEDED_BUY_MORE, ErrorHandler.EMAIL_USAGE_EXCEEDED);
		}

		ShareTestDTO shareTestDTO = new ShareTestDTO();
		shareTestDTO.setPositionCode(campaign.getPositionCode());
		shareTestDTO.setQuestionSetId(campaignDTO.getQuestionSetId());
		shareTestDTO.setRecruizTenant(TenantContextHolder.getTenant());
		shareTestDTO.setRandom(true);
		List<String> testCandidate = new ArrayList<>();
		Map<String, String> candidateProfileMap = new HashMap<>();

		// looping through each member to send email campaign
		for (CampaignCandidate campaignMember : campaign.getCampaignCandidates()) {

			// 10 days
			int linkValidityHour = 24 * 10;
			String xAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(userService.getLoggedInUserEmail(),
					TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour,
					userService.getLoggedInUserObject().getTimezone(), userService.getLoggedInUserObject().getLocale());

			String recruizProfileLink = baseUrl + GlobalConstants.INTERVIEW_FEEDBACK_URL + GlobalConstants.PASSKEY
					+ xAuthToken + "&" + GlobalConstants.CANDIDATE_EMAIL + campaignMember.getMemberEmailId()
					+ GlobalConstants.POSITION_CODE + campaign.getPositionCode() + GlobalConstants.CAMPAIGN_ID_PARAM
					+ campaign.getId() + GlobalConstants.CAMPAIGN_MEMBER_ID_PARAM + campaignMember.getId()
					+ GlobalConstants.CAMPAIGN_TYPE + campaign.getType();

			testCandidate.add(campaignMember.getMemberEmailId());
			shareTestDTO.setRecruizCandidateMailIds(testCandidate);

			// sending this data to level bar, emailId will be key and
			// profile Link will be value
			candidateProfileMap.put(campaignMember.getMemberEmailId(), recruizProfileLink);
		}

		// adding profile link in dto for level bar
		shareTestDTO.setRecruizCandidateProfileMap(candidateProfileMap);

		// TODO
		// return message id from level bar and then store it here

		Map<String, String> testMap = levelbarService.shareTestFromlevelbar(shareTestDTO);
		if (testMap != null && !testMap.isEmpty()) {
			levelbarService.storeSharedTestresponse(testMap, shareTestDTO);
			organizationConfigurationService.updateEmailCount(shareTestDTO.getRecruizCandidateMailIds().size());
		}
	}

	/**
	 * to run IVR Assessment campaign
	 * 
	 * @param campaign
	 * @param campaignRunDTO
	 */
	private void runIVRAssessmentCampaign(Campaign campaign, CampaignDTO campaignDTO) {
		// TODO Auto-generated method stub

	}

	/**
	 * to run email reach out campaign
	 * 
	 * @param campaign
	 * @param campaignRunDTO
	 */
	@Transactional
	private void runEmailReachOutCampaign(Campaign campaign, CampaignDTO campaignDTO) {

	}

	@Transactional(readOnly = true)
	public Page<Campaign> getAllCampaign(Pageable pageable) {
		return campaignRepositiory.findAll(pageable);
	}

	public CampaignStatDTO getCampaignStatDTO(Campaign campaign) {
		CampaignStatDTO dto = new CampaignStatDTO();

		int totalCandidate = campaign.getCampaignCandidates().size();
		dto.setTotalCandidate(campaign.getCampaignCandidates().size() + "");

		Long openedCount = campaignCandidateActionService.getCountByCampaignCandidatesAndActionType(
				campaign.getCampaignCandidates(), CampaignCandidateActionType.Opened.getDisplayName().toLowerCase());

		Double openedRate = (double) (openedCount.doubleValue() / totalCandidate) * 100;
		// dto.setOpenRate("(+" + openedCount + " Candidate" + ")" + openedRate
		// + "%");

		Long clickedCount = campaignCandidateActionService.getCountByCampaignCandidatesAndActionType(
				campaign.getCampaignCandidates(), CampaignCandidateActionType.Clicked.getDisplayName().toLowerCase());
		double clickedRate = ((clickedCount.doubleValue() / totalCandidate) * 100);
		// dto.setOpenRate("( " + clickedCount + " Candidate " + ")" +
		// clickedRate + "%");

		double avgTimetoOpen = campaignCandidateActionService.getAvgTimeToOpenEmail(campaign.getCampaignCandidates(),
				CampaignCandidateActionType.Opened.getDisplayName().toLowerCase());

		Long profileUpdatedCount = campaignCandidateActionService.getCountByCampaignCandidatesAndActionType(
				campaign.getCampaignCandidates(),
				CampaignCandidateActionType.ProfileUpdated.getDisplayName().toLowerCase());
		double profileUpdatedRate = ((profileUpdatedCount.doubleValue() / totalCandidate) * 100);

		Long notInterestedCount = campaignCandidateActionService.getCountByCampaignCandidatesAndActionType(
				campaign.getCampaignCandidates(),
				CampaignCandidateActionType.NotInterestedClick.getDisplayName().toLowerCase());
		double notInterestedLink = ((notInterestedCount.doubleValue() / totalCandidate) * 100);

		dto.setAvgTimeToOpen(avgTimetoOpen + " Mins");
		dto.setOpenRate(openedRate + "%");
		dto.setLinkClick(clickedRate + "%");
		dto.setProfileUpdateRate(profileUpdatedRate + "%");
		dto.setNotInterested(notInterestedLink + "%");

		return dto;
	}

	@Transactional
	public void deleteCampaign(Campaign campaign) {
		campaign.getCampaignHrMembers().clear();
		delete(campaign);
	}

	/**
	 * this will set tenant and then it will call method to update the tenant
	 * 
	 * @param tenant
	 * @param messaegId
	 * @param actionType
	 * @param action
	 */
	public void updateCampaignCandidateAction(String tenant, String messaegId, String actionType, String action) {
		TenantContextHolder.setTenant(tenant);
		updateCampaignCandidateAction(messaegId, actionType, action);
	}

	/**
	 * update the campaign on web hook hit
	 * 
	 * @param messaegId
	 * @param actionType
	 * @param action
	 */
	@Transactional
	public void updateCampaignCandidateAction(String messaegId, String actionType, String action) {

		CampaignCandidate campaignCandidate = campaignMemberService.getCamapignCandidateByMessageId(messaegId);
		addUpdateCampaignCandidateAction(actionType, action, campaignCandidate.getId());
	}

	@Transactional
	public void addUpdateCampaignCandidateAction(String actionType, String action, Long campaignCandidateId) {
		CampaignCandidate campaignCandidate = campaignMemberService.findOne(campaignCandidateId);
		if (campaignCandidate != null) {
			boolean actionExists = false;
			List<CampaignCandidateAction> actions = campaignCandidate.getActions();
			if (actions != null && !actions.isEmpty()) {
				for (CampaignCandidateAction campaignCandidateAction : actions) {
					if (campaignCandidateAction.getActionType().equalsIgnoreCase(actionType)) {
						actionExists = true;
						break;
					}
				}
			}

			CampaignCandidateAction deliveredAction = campaignCandidateActionService
					.getCampaignCandidateactionByActionTypeAndCandidate(
							CampaignCandidateActionType.Delivered.getDisplayName(), campaignCandidate);

			// if action does not exists add a new action
			if (!actionExists) {
				int interval = 0;
				if (deliveredAction != null) {
					Date currentTime = new Date();
					Date deliveredTime = deliveredAction.getActionTime();
					int intervalInMilliSecond = (int) (currentTime.getTime() - deliveredTime.getTime());

					// millisecond to minute
					interval = ((intervalInMilliSecond / 1000) / 60);
				}

				CampaignCandidateAction newAction = new CampaignCandidateAction();
				newAction.setActionInterval(interval);
				newAction.setActionResponse(action);
				newAction.setActionType(actionType);
				newAction.setCampaignCandidate(campaignCandidate);
				campaignCandidate.getActions().add(newAction);
				campaignMemberService.save(campaignCandidate);
			}
		}
	}

}
