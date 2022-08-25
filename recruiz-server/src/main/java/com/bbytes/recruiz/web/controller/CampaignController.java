package com.bbytes.recruiz.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Campaign;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.CampaignCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.CampaignDTO;
import com.bbytes.recruiz.rest.dto.models.CampaignStatDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CampaignService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.utils.ErrorHandler;

@RestController
public class CampaignController {

	public Logger logger = LoggerFactory.getLogger(CampaignController.class);

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	/**
	 * To add a new campaign
	 * 
	 * @param campaignDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/campaign/new", method = RequestMethod.POST)
	public RestResponse addNewCampaign(@RequestBody CampaignDTO campaignDTO) throws RecruizException {
		RestResponse response = null;
		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}
		try {
			Campaign campaign = campaignService.addNewCampaign(campaignDTO);
			response = new RestResponse(RestResponse.SUCCESS, campaign, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_CREATE_CAMPAIGN,
					ErrorHandler.FAILED_ADDING_CAMPAIGN);
			logger.warn(ex.getMessage(), ex);
		}
		return response;
	}

	/**
	 * To add members to existing campaign
	 * 
	 * @param campaignId
	 * @param campaignMemberDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/campaign/member/add/{campaignId}", method = RequestMethod.POST)
	public RestResponse addMemberToCampaign(@PathVariable Long campaignId,
			@RequestBody List<CampaignCandidateDTO> campaignMemberDTO) throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(campaignId);
			if (campaign == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
						ErrorHandler.FAILED_GETTING_CAMPAIGN);
			}
			campaign = campaignService.addCampaignCandidates(campaign, campaignMemberDTO);
			response = new RestResponse(RestResponse.SUCCESS, campaign, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_CREATE_CAMPAIGN,
					ErrorHandler.FAILED_ADDING_CAMPAIGN);
		}
		return response;
	}

	/**
	 * To update campaign
	 * 
	 * @param id
	 * @param campaignDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/campaign/edit/{id}", method = RequestMethod.POST)
	public RestResponse updateCampaign(@PathVariable Long id, @RequestBody CampaignDTO campaignDTO)
			throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(id);
			if (campaign == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
						ErrorHandler.FAILED_GETTING_CAMPAIGN);
			}
			campaign = campaignService.updateCampaign(campaign, campaignDTO);
			response = new RestResponse(RestResponse.SUCCESS, campaign, null);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_CREATE_CAMPAIGN,
					ErrorHandler.FAILED_ADDING_CAMPAIGN);
		}
		return response;
	}

	/**
	 * to run a campaign
	 * 
	 * @param campaignRunDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/campaign/run/{id}", method = RequestMethod.PUT)
	public RestResponse runCampaign(@PathVariable Long id, @RequestBody(required = false) CampaignDTO campaignDTO)
			throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(id);
			if (campaign == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
						ErrorHandler.FAILED_GETTING_CAMPAIGN);
			}
			if (campaign.getCampaignCandidates() == null || campaign.getCampaignCandidates().isEmpty()) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.CAN_NOT_RUN_CAMPAIGN_WITHOUT_CANDIDATE,
						ErrorHandler.NO_CAMPAIGN_CANDIDATE);
			}
			response = campaignService.runCampaign(campaign, campaignDTO);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_CREATE_CAMPAIGN,
					ErrorHandler.FAILED_ADDING_CAMPAIGN);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/campaign/all", method = RequestMethod.GET)
	public RestResponse getAllCampaign(@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {

			Pageable pageable = pageableService.getPageRequestObject(pageNo, sortField,
					pageableService.getSortDirection(sortOrder));

			Page<Campaign> campaign = campaignService.getAllCampaign(pageable);
			if (campaign.getContent() != null && !campaign.getContent().isEmpty()) {
				for (Campaign campgn : campaign.getContent()) {
					campgn.getCampaignCandidates().size();
					campgn.getCampaignHrMembers().size();
				}
			}

			response = new RestResponse(true, campaign);

		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
					ErrorHandler.FAILED_GETTING_CAMPAIGN);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/campaign/stat/{id}", method = RequestMethod.GET)
	public RestResponse getCampaignStat(@PathVariable Long id) throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(id);
			if (campaign == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
						ErrorHandler.FAILED_GETTING_CAMPAIGN);
			}

			CampaignStatDTO statDTO = campaignService.getCampaignStatDTO(campaign);
			response = new RestResponse(true, statDTO);
		} catch (Exception ex) {
			logger.error(ex.getMessage(),ex);
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN_STAT,
					ErrorHandler.FAILED_GETTING_CAMPAIGN_STAT);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/campaign/status/{id}", method = RequestMethod.PUT)
	public RestResponse changeCampaignStatus(@PathVariable Long id, @RequestParam String status)
			throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(id);
			if (campaign == null) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_GET_CAMPAIGN,
						ErrorHandler.FAILED_GETTING_CAMPAIGN);
			}
			campaign.setStatus(status);
			campaign = campaignService.save(campaign);

			response = new RestResponse(true, campaign);
		} catch (Exception ex) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.FAILED_TO_CHANGE_CAMPAIGN_STATUS,
					ErrorHandler.FAILED_CHANGING_CAMPAIGN_STATUS);
		}
		return response;
	}

	@RequestMapping(value = "/api/v1/campaign/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteCampaign(@PathVariable Long id) throws RecruizException {
		RestResponse response = null;

		if (!checkUserPermissionService.hasCampaignPermission()) {
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.USER_DONT_HAVE_CAMPAIGN_PERMISSION,
					ErrorHandler.NO_CAMPAIGN_PERMISSION);
			return response;
		}

		try {
			Campaign campaign = campaignService.findOne(id);
			campaignService.deleteCampaign(campaign);
			response = new RestResponse(true, "Deleted");
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
			response = new RestResponse(RestResponse.FAILED, ErrorHandler.ERROR_DELETING_CAMPAIGN,
					ErrorHandler.FAILED_TO_DELETE_CAMPAIGN);
		}
		return response;
	}

}
