package com.bbytes.recruiz.web.controller;

import java.util.HashMap;
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

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterWorkflow;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.OfferLetterApprovalDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterWorkflowDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateFileService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.OfferLetterApprovalsService;
import com.bbytes.recruiz.service.OfferLetterWorkFlowService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class OfferLetterWorkFlowController {

	private static final Logger logger = LoggerFactory.getLogger(OfferLetterWorkFlowController.class);

	@Autowired
	private OfferLetterWorkFlowService offerLetterWorkFlowService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private UserService userService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService;


	@Autowired
	OrganizationService organizationService;
	/**
	 * add new offer letter workflow
	 * @param workflowDTO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/offerletter/workflow/addOfferLetterWorkflow", method = RequestMethod.POST)
	public RestResponse addNewWorkflow(@RequestBody List<OfferLetterWorkflowDTO> workflowDTO)throws Exception{

		return offerLetterWorkFlowService.addNewWorkflow(workflowDTO);
	}

	/**
	 * update offer letter workflow details
	 * @param workflowDTO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/offerletter/workflow/updateOfferLetterWorkflow", method = RequestMethod.POST)
	public RestResponse updateOfferLetterWorkflow(@RequestBody OfferLetterWorkflowDTO workflowDTO)throws Exception{

		if(workflowDTO.getWorkflowName()==null || workflowDTO.getWorkflowName().equalsIgnoreCase("") || workflowDTO.getWorkflowName().isEmpty()
				|| workflowDTO.getLowerMarginOperator()==null || workflowDTO.getLowerMarginOperator().equalsIgnoreCase("") || workflowDTO.getLowerMarginOperator().isEmpty()
				|| workflowDTO.getUpperMarginOperator()==null || workflowDTO.getUpperMarginOperator().equalsIgnoreCase("") || workflowDTO.getUpperMarginOperator().isEmpty()
				|| workflowDTO.getApproverEmail()==null || workflowDTO.getApproverEmail().equalsIgnoreCase("") || workflowDTO.getApproverEmail().isEmpty()
				|| workflowDTO.getApproverName()==null || workflowDTO.getApproverName().equalsIgnoreCase("") || workflowDTO.getApproverName().isEmpty()){
			return new RestResponse(RestResponse.FAILED, "", "Some field value missing !");
		}

		return offerLetterWorkFlowService.updateOfferLetterWorkflow(workflowDTO);
	}

	/**
	 * get offerletter workflow details by id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/offerletter/workflow/getOfferLetterWorkflowById", method = RequestMethod.GET)
	public RestResponse getOfferLetterWorkflowById(@RequestParam(value = "id") String id)throws Exception{

		return offerLetterWorkFlowService.getOfferLetterWorkflowById(id);
	}

	/**
	 * delete offerletter workflow details from db by id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/offerletter/workflow/deleteOfferLetterWorkflowById", method = RequestMethod.GET)
	public RestResponse deleteOfferLetterWorkflowById(@RequestParam(value = "id") String id)throws Exception{

		return offerLetterWorkFlowService.deleteOfferLetterWorkflowById(id);
	}

	/**
	 * get all offerletter workflows from db
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/offerletter/workflow/getAllOfferLetterWorkflows", method = RequestMethod.GET)
	public RestResponse getAllOfferLetterWorkflows()throws Exception{

		return offerLetterWorkFlowService.getAllOfferLetterWorkflows();
	}



	@RequestMapping(value = "/api/v1/offerletter/workflow/getOfferLetterApprovalData()", method = RequestMethod.GET)
	public RestResponse getOfferLetterApprovalData(@RequestParam(value = "positionId") String positionId,@RequestParam(value = "candidateId") String candidateId)throws Exception{

		if(positionId==null || positionId.equalsIgnoreCase("") || positionId.isEmpty()
				|| candidateId==null || candidateId.equalsIgnoreCase("") || candidateId.isEmpty()){
			return new RestResponse(RestResponse.FAILED, "", "Some field value missing !");
		}

		return offerLetterWorkFlowService.getOfferLetterApprovalData(positionId,candidateId);
	}


	@RequestMapping(value = "/api/v1/offerletter/workflow/sendOfferLetterApprovalRequest", method = RequestMethod.POST)
	public RestResponse sendOfferLetterApprovalRequest(@RequestBody OfferLetterApprovalDTO approvalDTO)throws Exception{

		if(approvalDTO.getCandidateId()==null || approvalDTO.getCandidateId().equalsIgnoreCase("") || approvalDTO.getCandidateId().isEmpty()
				|| approvalDTO.getPositionId()==null || approvalDTO.getPositionId().equalsIgnoreCase("") || approvalDTO.getPositionId().isEmpty()){
			//	|| approvalDTO.getCtcOffered()==null || approvalDTO.getCtcOffered().equalsIgnoreCase("") || approvalDTO.getCtcOffered().isEmpty()
			//	|| approvalDTO.getCurrentCTC()==null || approvalDTO.getCurrentCTC().equalsIgnoreCase("") || approvalDTO.getCurrentCTC().isEmpty()
			//	|| approvalDTO.getPercentageHike()==null || approvalDTO.getPercentageHike().equalsIgnoreCase("") || approvalDTO.getPercentageHike().isEmpty()
			//	|| approvalDTO.getProfitMargin()==null || approvalDTO.getProfitMargin().equalsIgnoreCase("") || approvalDTO.getProfitMargin().isEmpty()){
			return new RestResponse(RestResponse.FAILED, "", "Some field value missing !");
		}

		
		if(approvalDTO.getMonthlyCost()==null || approvalDTO.getMonthlyCost().trim().equalsIgnoreCase("null")){
			approvalDTO.setMonthlyCost("0");
		}
		if(approvalDTO.getMontlyGrossMargin()==null || approvalDTO.getMontlyGrossMargin().trim().equalsIgnoreCase("null")){
			approvalDTO.setMontlyGrossMargin("0");
		}
		
		
		return offerLetterWorkFlowService.sendOfferLetterApprovalRequest(approvalDTO);
	}


	@RequestMapping(value = "/api/v1/external/offerletter/approval/candidate/round", method = RequestMethod.GET)
	public RestResponse getApprovalRoundCandidates(
			@RequestParam(value = "candidateId", required = false) String candidateId,
			@RequestParam(value = "positionCode", required = false) String positionCode,
			@RequestParam(value = "mskd", required = false) String mskd,
			@RequestParam(value = "candidateLevelbarkey", required = false) String candidateLevelbarkey,
			@RequestParam(value = "approvalId", required = false) String approvalId)
					throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.GetRoundCandidate.name());

		logger.error("step (1) enter in getApprovalRoundCandidates() candidateId = "+candidateId+" positionCode = "+positionCode+" approvalId = "+approvalId);

		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		logger.error("step (2) enter in getApprovalRoundCandidates() candidateId = "+candidateId+" positionCode = "+positionCode+" approvalId = "+approvalId);

		if (candidateId == null || candidateId.isEmpty() || positionCode == null
				|| positionCode.isEmpty())
			return null;
		List<Map<String, Object>> roundCandidateMap = new LinkedList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		Candidate candidate = candidateService.getCandidateById(Long.valueOf(candidateId));
		// marked as masked candidate hiding the info
		if (mskd != null && !mskd.isEmpty() && mskd.equalsIgnoreCase("y")) {
			candidate.setFullName("Hidden");
			candidate.setEmail("Hidden");
			candidate.setAlternateEmail("Hidden");
			candidate.setMobile("XXXXXXXXXXXX");
			candidate.setAlternateMobile("XXXXXXXXXXXX");
			logger.error("step (3) enter in getApprovalRoundCandidates() candidateId = "+candidateId+" positionCode = "+positionCode+" approvalId = "+approvalId);
			// why list ? -> bcoz all candidate related files belong to one
			// object called candidate files.
			List<CandidateFile> candidateMaskedResume = candidateFileService.getCandidateFileByTypeAndId(
					FileType.Masked_Resume_Converted.getDisplayName(), candidateId + "");
			if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
				String maskedResumeFilePath = candidateMaskedResume.get(0).getFilePath();
				candidate.setResumeLink(maskedResumeFilePath);
			} else {
				candidate.setResumeLink("");
			}

		}

		// Need for lazy loading
		candidate.getKeySkills().size();
		candidate.getEducationDetails().size();
		candidate.getCustomField().size();

		map.put("positionCode", positionCode);
		map.put("candidateDetails", candidate);
		logger.error("step (4) enter in getApprovalRoundCandidates() candidateId = "+candidateId+" positionCode = "+positionCode+" approvalId = "+approvalId);
		if(approvalId!=null && !approvalId.equalsIgnoreCase("") && !approvalId.isEmpty()){

			OfferLetterApprovals offerLetterApproval = offerLetterApprovalsService.findOne(Long.parseLong(approvalId));
			Position position = positionService.getPositionByCode(positionCode);

			Set<String> skills = candidate.getKeySkills();
			String candidateSkills = "";
			int y = 1;
			for (String skill : skills) {
				if(y==1)
					candidateSkills = skill;
				else
					candidateSkills = candidateSkills + "," + skill;
				y++;

				if(y==3)
					break;
			}

			if(offerLetterApproval!=null){
				map.put("clientName", position.getClient().getClientName());
				map.put("positionName", position.getTitle());
				map.put("candidateName", candidate.getFullName());
				map.put("skills", candidateSkills);
				map.put("request_send_from_user",offerLetterApproval.getRequest_send_from_user());
				map.put("approval_status", offerLetterApproval.getApproval_status());
				map.put("deliveryLead", offerLetterApproval.getDeliveryLead());
				map.put("recruitersName", offerLetterApproval.getRecruitersName());
				map.put("accountManager", offerLetterApproval.getAccountManager());
				map.put("client",offerLetterApproval.getClient());
				map.put("clientPoC", offerLetterApproval.getClientPoC());
				map.put("clientPhone", offerLetterApproval.getClientPhone());
				map.put("clientEmail", offerLetterApproval.getClientEmail());
				map.put("candidateName", offerLetterApproval.getCandidateName());
				map.put("designation", offerLetterApproval.getDesignation());
				map.put("dateOfOffer", offerLetterApproval.getDateOfOffer());
				map.put("experience", offerLetterApproval.getExperience());
				map.put("doj", offerLetterApproval.getDoj());
				map.put("billingDate",offerLetterApproval.getBillingDate());
				map.put("compensationAnnual", offerLetterApproval.getCompensationAnnual());
				map.put("projectDurationMonths", offerLetterApproval.getProjectDurationMonths());
				map.put("billingHours", offerLetterApproval.getBillingHours());
				map.put("billRatePerHour", offerLetterApproval.getBillRatePerHour());
				map.put("subConCompensation",offerLetterApproval.getSubConCompensation());
				map.put("serviceTax", offerLetterApproval.getServiceTax());
				map.put("oneTimeCost", offerLetterApproval.getOneTimeCost());
				map.put("headHunting", offerLetterApproval.getHeadHunting());
				map.put("otherAssociatedCost", offerLetterApproval.getOtherAssociatedCost());
				map.put("monthlyCost",offerLetterApproval.getMonthlyCost());
				map.put("costProjectDuration", offerLetterApproval.getCostProjectDuration());
				map.put("billingMonthly", offerLetterApproval.getBillingMonthly());
				map.put("billingProjectDuration", offerLetterApproval.getBillingProjectDuration());
				map.put("monthlyDirectCost", offerLetterApproval.getMonthlyDirectCost());
				map.put("annualDirectCost",offerLetterApproval.getAnnualDirectCost());
				map.put("montlyGrossMargin", offerLetterApproval.getMontlyGrossMargin());
				map.put("annualGorssMargin", offerLetterApproval.getAnnualGorssMargin());
				map.put("grossMarginPercentage", offerLetterApproval.getGrossMarginPercentage());
				map.put("approvedDate", offerLetterApproval.getApprovedDate());
				map.put("approvedBy", offerLetterApproval.getApprovedBy());
				map.put("remarks", offerLetterApproval.getRemarks());
				map.put("offerStatus", offerLetterApproval.getOfferStatus());
				map.put("iSJoinedOn", offerLetterApproval.getISJoinedOn());
				map.put("relocationCost", offerLetterApproval.getRelocationCost());
				map.put("joiningBonusCost", offerLetterApproval.getJoiningBonusCost());
				map.put("noticePeriodCost", offerLetterApproval.getNoticePeriodCost());
			}

		}

		roundCandidateMap.add(map);
		logger.error("step (5) enter in getApprovalRoundCandidates() candidateId = "+candidateId+" positionCode = "+positionCode+" approvalId = "+approvalId);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, roundCandidateMap);
		return response;
	}



	@RequestMapping(value = "/api/v1/external/offerletter/approval/candidate/accecptORreject", method = RequestMethod.GET)
	public RestResponse accecptORrejectApprovalRequest(@RequestParam(value = "approvalId", required = false) String approvalId,
			@RequestParam(value = "isAccepted", required = false) boolean isAccepted,@RequestParam(value = "reason", required = false) String reason)throws Exception {

		if(approvalId==null || approvalId.equalsIgnoreCase("") || approvalId.isEmpty())
			return null;

		try{
			OfferLetterApprovals offerLetterApproval = offerLetterApprovalsService.findOne(Long.parseLong(approvalId));

			if(offerLetterApproval==null)
				return new RestResponse(RestResponse.FAILED, "Not found any record of offer approval");


			OfferLetterWorkflow offerLetterWorkflow = offerLetterWorkFlowService.findOne(offerLetterApproval.getWorkflow_id());

				if(!offerLetterApproval.getApproval_status().equalsIgnoreCase(GlobalConstants.PENDING_STATUS))
					return new RestResponse(RestResponse.FAILED, "Approval is already "+offerLetterApproval.getApproval_status()+".Now you can't change it.");

				if(isAccepted){
					offerLetterApproval.setApproval_status(GlobalConstants.ACCEPTED);
				}else{
					offerLetterApproval.setApproval_status(GlobalConstants.REJECTED);
					if(reason!=null)
						offerLetterApproval.setReject_reason(reason);

				}

				if(offerLetterApproval!=null)
					offerLetterApproval.setApprovedBy(offerLetterWorkflow.getApprover_email());

				offerLetterApprovalsService.save(offerLetterApproval);
				offerLetterApprovalsService.addActivityInCandidateAndPosition(offerLetterApproval,isAccepted);

				offerLetterWorkFlowService.sendMailOfApproverResponse(offerLetterApproval,isAccepted);

				return new RestResponse(RestResponse.SUCCESS, "Success");
		
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "Internal server error");
		}
	}



	@RequestMapping(value = "/api/v1/offerletter/approval/checkmarginvalue/existingInTemplateORNot", method = RequestMethod.GET)
	public RestResponse marginValueExistingInTemplateORNot(@RequestParam(value = "marginValue", required = false) String marginValue)throws Exception {

		if(marginValue==null || marginValue.equalsIgnoreCase("") || marginValue.isEmpty())
			return new RestResponse(RestResponse.FAILED, "Profit margin value required !");

		return offerLetterWorkFlowService.marginValueExistingInTemplateORNot(marginValue);
	}



	@RequestMapping(value = "/api/v1/offerletter/approval/checkApprovalStatus", method = RequestMethod.GET)
	public RestResponse checkApprovalStatus(@RequestParam(value = "positionId") String positionId,@RequestParam(value = "candidateId") String candidateId)throws Exception {

		if(positionId==null || positionId.trim().equalsIgnoreCase("") || positionId.isEmpty() || candidateId==null || candidateId.trim().equalsIgnoreCase("") || candidateId.isEmpty())
			return new RestResponse(RestResponse.FAILED, "positionId OR candidateId values required !");

		boolean isApprovalRequestSend = false;

		
		List<OfferLetterApprovals> offerLetterApprovalList = offerLetterApprovalsService.getApprovalDetailsByPositionIdAndCandidateId(Long.parseLong(positionId), Long.parseLong(candidateId));

		for (OfferLetterApprovals offerLetterApproval : offerLetterApprovalList) {
			if(offerLetterApproval.getApproval_status().equalsIgnoreCase(GlobalConstants.PENDING_STATUS)){
				isApprovalRequestSend = true;
				break;
			}
		}
			
		return new RestResponse(RestResponse.SUCCESS, isApprovalRequestSend);
	}


}
