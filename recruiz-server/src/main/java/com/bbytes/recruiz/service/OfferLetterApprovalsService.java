package com.bbytes.recruiz.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterWorkflow;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionActivity;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.OfferLetterApprovalsRepository;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.rest.dto.models.OfferLetterApprovalDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ActivityMessageConstants;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class OfferLetterApprovalsService  extends AbstractService<OfferLetterApprovals, Long> {

	private static final Logger logger = LoggerFactory.getLogger(OfferLetterApprovalsService.class);

	@Autowired
	OfferLetterApprovalsRepository offerLetterApprovalsRepository;

	@Autowired
	PositionService positionService;

	@Autowired
	CandidateService candidateService;
	
	@Autowired
	OfferLetterWorkFlowService offerLetterWorkFlowService;
	
	@Autowired
	EmailTemplateDataService emailTemplateDataService;
	
	@Autowired
	CandidateActivityService candidateActivityService;
	
	@Autowired
	PositionActivityService positionActivityService;
	
	@Autowired
	UserService userService;

	@Autowired
	public OfferLetterApprovalsService(OfferLetterApprovalsRepository offerLetterApprovalsRepository) {
		super(offerLetterApprovalsRepository);
		this.offerLetterApprovalsRepository = offerLetterApprovalsRepository;
	}

	public RestResponse sendOfferLetterApprovalRequest(OfferLetterApprovalDTO approvalDTO) {

		OfferLetterApprovals offerLetterApprovals =	new OfferLetterApprovals();

		try{
			
			  if(approvalDTO.getGrossMarginPercentage()==null || approvalDTO.getGrossMarginPercentage().equalsIgnoreCase("") || approvalDTO.getGrossMarginPercentage().isEmpty())
					return new RestResponse(RestResponse.FAILED, "GrossMarginPercentage field is empty !");

			  approvalDTO.setGrossMarginPercentage(approvalDTO.getGrossMarginPercentage().replace("%", ""));
			  
		    String worklowId = getWorkflowLevelByProfitMargin(approvalDTO.getGrossMarginPercentage()); 
		    
			if(worklowId==null)
				return new RestResponse(RestResponse.FAILED, "Workflow level not found !");

			Position position = positionService.findOne(Long.parseLong(approvalDTO.getPositionId()));

			offerLetterApprovals.setApproval_status(GlobalConstants.PENDING_STATUS);
			offerLetterApprovals.setCandidate_id(Long.parseLong(approvalDTO.getCandidateId()));
			offerLetterApprovals.setClient_id(position.getClient().getId());
			offerLetterApprovals.setPosition_code(position.getPositionCode());
			offerLetterApprovals.setPosition_id(position.getId());
			offerLetterApprovals.setWorkflow_id(Long.parseLong(worklowId));
			offerLetterApprovals.setRequest_send_from_user(userService.getLoggedInUserObject().getUserId());

			
			offerLetterApprovals.setDeliveryLead(approvalDTO.getDeliveryLead());
			offerLetterApprovals.setRecruitersName(approvalDTO.getRecruitersName());
			offerLetterApprovals.setAccountManager(approvalDTO.getAccountManager());
			offerLetterApprovals.setClient(position.getClient().getClientName());
			offerLetterApprovals.setClientPoC(approvalDTO.getClientPoC());
			offerLetterApprovals.setClientPhone(approvalDTO.getClientPhone());
			offerLetterApprovals.setClientEmail(approvalDTO.getClientEmail());
			offerLetterApprovals.setCandidateName(approvalDTO.getCandidateName());
			offerLetterApprovals.setDesignation(approvalDTO.getDesignation());
			offerLetterApprovals.setExperience(approvalDTO.getExperience());
			offerLetterApprovals.setDoj(approvalDTO.getDoj());
			offerLetterApprovals.setBillingDate(approvalDTO.getBillingDate());
			offerLetterApprovals.setCompensationAnnual(approvalDTO.getCompensationAnnual());
			offerLetterApprovals.setProjectDurationMonths(approvalDTO.getProjectDurationMonths());
			offerLetterApprovals.setBillingHours(approvalDTO.getBillingHours());
			offerLetterApprovals.setBillRatePerHour(approvalDTO.getBillRatePerHour());
			offerLetterApprovals.setSubConCompensation(approvalDTO.getSubConCompensation());
			offerLetterApprovals.setServiceTax(approvalDTO.getServiceTax());
			offerLetterApprovals.setOneTimeCost(approvalDTO.getOneTimeCost());
			offerLetterApprovals.setRelocationCost(approvalDTO.getRelocationCost());
			offerLetterApprovals.setJoiningBonusCost(approvalDTO.getJoiningBonusCost());
			offerLetterApprovals.setNoticePeriodCost(approvalDTO.getNoticePeriodCost());
			offerLetterApprovals.setHeadHunting(approvalDTO.getHeadHunting());
			offerLetterApprovals.setOtherAssociatedCost(approvalDTO.getOtherAssociatedCost());
			offerLetterApprovals.setMonthlyCost(approvalDTO.getMonthlyCost());
			offerLetterApprovals.setCostProjectDuration(approvalDTO.getCostProjectDuration());
			offerLetterApprovals.setBillingMonthly(approvalDTO.getBillingMonthly());
			offerLetterApprovals.setBillingProjectDuration(approvalDTO.getBillingProjectDuration());
			offerLetterApprovals.setMonthlyDirectCost(approvalDTO.getMonthlyDirectCost());
			offerLetterApprovals.setAnnualDirectCost(approvalDTO.getAnnualDirectCost());
			offerLetterApprovals.setMontlyGrossMargin(approvalDTO.getMontlyGrossMargin());
			offerLetterApprovals.setAnnualGorssMargin(approvalDTO.getAnnualGorssMargin());
			offerLetterApprovals.setGrossMarginPercentage(approvalDTO.getGrossMarginPercentage());
			offerLetterApprovals.setApprovedDate(approvalDTO.getApprovedDate());
			offerLetterApprovals.setApprovedBy(approvalDTO.getApprovedBy());
			offerLetterApprovals.setRemarks(approvalDTO.getRemarks());
			offerLetterApprovals.setOfferStatus(approvalDTO.getOfferStatus());
			offerLetterApprovals.setISJoinedOn(approvalDTO.getiSJoinedOn());
			offerLetterApprovals.setDateOfOffer(approvalDTO.getDateOfOffer());
			
			
			//send mail for approval		
			offerLetterApprovals = 	offerLetterApprovalsRepository.save(offerLetterApprovals);
		    emailTemplateDataService.getRenderedTemplateForRequestForApproval(approvalDTO,worklowId,offerLetterApprovals.getId());
				
				candidateActivityService.addActivityForApproval("Sent Candidate Profile For Approval",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				approvalDTO.getCandidateId() + "", CandidateActivityType.SEND_PROFILE_FOR_APPROVAL.getDisplayName(),String.valueOf(offerLetterApprovals.getId()));
				
				PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
						"sent "+approvalDTO.getCandidateName()+" profile for approval",
						"sent "+approvalDTO.getCandidateName()+" profile for approval", new Date(), position.getPositionCode(),
						position.getTeam());
				positionActivity.setCandidateId(Long.valueOf(approvalDTO.getCandidateId()));
				positionActivity.setOfferApprovalId(String.valueOf(offerLetterApprovals.getId()));
				positionActivityService.addActivity(positionActivity);
				
			    return new RestResponse(RestResponse.SUCCESS, "sent offer letter approval request successfully !");
			
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "Internal server error !");
		}

	}

	private String getWorkflowLevelByProfitMargin(String profitMargin) {

		List<OfferLetterWorkflow> workflowList = offerLetterWorkFlowService.findAll();

		if(workflowList!=null){

			for (OfferLetterWorkflow offerLetterWorkflow : workflowList) {
				boolean status = findWorkflowLevel(profitMargin,offerLetterWorkflow);
				if(status){
					return String.valueOf(offerLetterWorkflow.getId());
				}
			}
		}

		return null;
	}

	private boolean findWorkflowLevel(String profitMargin, OfferLetterWorkflow offerLetterWorkflow) {

		boolean lower = false;
		boolean upper = false;

		double lowerMargin = Double.parseDouble(offerLetterWorkflow.getLower_margin());
		double upperMargin = Double.parseDouble(offerLetterWorkflow.getUpper_margin());
		double margin      = Double.parseDouble(profitMargin);
		int valLower = Double.compare(margin, lowerMargin);
		//checking lower margin
		if(offerLetterWorkflow.getLower_margin_operator().equals(">")){
			if(valLower>0){
				lower = true;
			}
			
		}
		if(offerLetterWorkflow.getLower_margin_operator().equals("<=")){
			if(valLower==0 || valLower<0){
				lower = true;
			}
		}
		if(offerLetterWorkflow.getLower_margin_operator().equals(">=")){
			if(valLower==0 || valLower>0){
				lower = true;
			}
		}
		if(offerLetterWorkflow.getLower_margin_operator().equals("<")){
			if(valLower<0){
				lower = true;
			}
		}
		if(offerLetterWorkflow.getLower_margin_operator().equals("=")){
			if(valLower==0){
				lower = true;
			}
		}
		
		int valUpper = Double.compare(margin, upperMargin);
		//checking upper margin
		if(offerLetterWorkflow.getUpper_margin_operator().equals(">")){
			if(valUpper>0){
				upper = true;
			}
		}
		if(offerLetterWorkflow.getUpper_margin_operator().equals("<=")){
			if(valUpper==0 || valUpper<0){
				upper = true;
			}
		}
		if(offerLetterWorkflow.getUpper_margin_operator().equals(">=")){
			if(valUpper==0 || valUpper>0){
				upper = true;
			}
		}
		if(offerLetterWorkflow.getUpper_margin_operator().equals("<")){
			if(valUpper<0){
				upper = true;
			}
		}
		if(offerLetterWorkflow.getUpper_margin_operator().equals("=")){
			if(valUpper==0){
				upper = true;
			}
		}

		if(lower && upper)
			return true;


		return false;
	}

	public void addActivityInCandidateAndPosition(OfferLetterApprovals offerLetterApproval, boolean isAccepted) throws RecruizException {
	
		Position position = positionService.getPositionById(offerLetterApproval.getPosition_id());
		Candidate candidate = candidateService.getCandidateById(offerLetterApproval.getCandidate_id());
		
		if(isAccepted){

			candidateActivityService.addActivity(candidate.getFullName()+" profile accepted for "+position.getTitle(),
			userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
			candidate.getCid() + "", CandidateActivityType.ACCEPTED_PROFILE.getDisplayName());
			
			PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					candidate.getFullName()+" profile accepted for "+position.getTitle(),
					candidate.getFullName()+" profile accepted for "+position.getTitle(), new Date(), position.getPositionCode(),
					position.getTeam());

			positionActivityService.addActivity(positionActivity);
		}else{

			candidateActivityService.addActivity(candidate.getFullName()+" profile rejected for "+position.getTitle()+" (reason : "+offerLetterApproval.getReject_reason()+")",
			userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
			candidate.getCid() + "", CandidateActivityType.REJECTED_PROFILE.getDisplayName());
			
			PositionActivity positionActivity = new PositionActivity(userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
					candidate.getFullName()+" profile rejected for "+position.getTitle(),
					candidate.getFullName()+" profile rejected for "+position.getTitle(), new Date(), position.getPositionCode(),
					position.getTeam());

			positionActivityService.addActivity(positionActivity);
		}
		
	}

	public List<OfferLetterApprovals> getApprovalDetailsByPositionIdAndCandidateId(long positionId, long candidateId) {
		
		return offerLetterApprovalsRepository.getApprovalDetailsByPositionIdAndCandidateId(positionId,candidateId);
	}

	public List<OfferLetterApprovals> getApprovalListByRequestSenderAndDate(Long userId,Date startReportDate, Date endReportDate) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.getApprovalListByRequestSender(userId,startReportDate,endReportDate);
	}
	
	public List<OfferLetterApprovals> getApprovalListByRequestSenderAndDateAndStatus(Long userId,Date startReportDate, Date endReportDate, String status) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.getApprovalListByRequestSenderAndStatus(userId,startReportDate,endReportDate,status);
	}

	public List<OfferLetterApprovals> findAllByDate(Date startReportDate, Date endReportDate) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.findAllByDate(startReportDate,endReportDate);
	}

	public List<OfferLetterApprovals> findAllByDateAndStatus(Date startReportDate, Date endReportDate, String status) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.findAllByDateAndStatus(startReportDate,endReportDate,status);
	}
	
	
	public List<OfferLetterApprovals> findAllByPositionCode(Long userId, Date startReportDate, Date endReportDate, String positionCode) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.findAllByPositionCode(userId,startReportDate,endReportDate,positionCode);
	}

	public List<OfferLetterApprovals> findAllByPositionCode(Date startReportDate, Date endReportDate ,String positionCode) {
		// TODO Auto-generated method stub
		return offerLetterApprovalsRepository.findAllByPositionCode(startReportDate,endReportDate ,positionCode);
	}
}
