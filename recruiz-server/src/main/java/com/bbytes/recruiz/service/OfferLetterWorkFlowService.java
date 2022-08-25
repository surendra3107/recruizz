package com.bbytes.recruiz.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.OfferLetterApprovals;
import com.bbytes.recruiz.domain.OfferLetterWorkflow;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.OfferLetterWorkflowRepository;
import com.bbytes.recruiz.rest.dto.models.OfferLetterApprovalDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterApprovalDataDTO;
import com.bbytes.recruiz.rest.dto.models.OfferLetterWorkflowDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;

@Service
public class OfferLetterWorkFlowService  extends AbstractService<OfferLetterWorkflow, Long> {

	private static final Logger logger = LoggerFactory.getLogger(CandidateService.class);

	@Autowired
	OfferLetterWorkflowRepository offerLetterWorkflowRepository;

	@Autowired
	EmailTemplateDataService emailTemplateDataService;

	@Autowired
	PositionService positionService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService;

	@Autowired
	public OfferLetterWorkFlowService(OfferLetterWorkflowRepository offerLetterWorkflowRepository) {
		super(offerLetterWorkflowRepository);
		this.offerLetterWorkflowRepository = offerLetterWorkflowRepository;
	}

	public RestResponse addNewWorkflow(List<OfferLetterWorkflowDTO> workflowDTOList) {


		try{
			for (OfferLetterWorkflowDTO workflowDTO : workflowDTOList) {
				OfferLetterWorkflow offerWorkflow = offerLetterWorkflowRepository.findByWorkflowName(workflowDTO.getWorkflowName());
				
				if(offerWorkflow!=null)
					return new RestResponse(RestResponse.FAILED, "Please choose  different workflow name !");
				try{
					offerWorkflow = new OfferLetterWorkflow();

					offerWorkflow.setWorkflow_name(workflowDTO.getWorkflowName());
					offerWorkflow.setLower_margin(workflowDTO.getLowerMargin());
					offerWorkflow.setLower_margin_operator(workflowDTO.getLowerMarginOperator());
					offerWorkflow.setUpper_margin(workflowDTO.getUpperMargin());
					offerWorkflow.setUpper_margin_operator(workflowDTO.getUpperMarginOperator());
					offerWorkflow.setApprover_email(workflowDTO.getApproverEmail());
					offerWorkflow.setApprover_name(workflowDTO.getApproverName());

					if(workflowDTO.getOtherLoop()!=null)
						offerWorkflow.setOther_loop(workflowDTO.getOtherLoop());
					if(workflowDTO.getOtherLoopName()!=null)
						offerWorkflow.setOther_loop_name(workflowDTO.getOtherLoopName());

					offerWorkflow.setStatus(true);
					offerLetterWorkflowRepository.save(offerWorkflow);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return new RestResponse(RestResponse.SUCCESS, "offerletter workflow save successfully !");
		}catch(Exception e){
			e.printStackTrace();
		}
		return new RestResponse(RestResponse.FAILED, "Internal server error !");
	}

	public RestResponse updateOfferLetterWorkflow(OfferLetterWorkflowDTO workflowDTO) {

		try{

			OfferLetterWorkflow offerWorkflow = offerLetterWorkflowRepository.findOne(workflowDTO.getId());

			if(offerWorkflow==null)
				return new RestResponse(RestResponse.FAILED, "offerletter workflow Not found !");

			offerWorkflow.setWorkflow_name(workflowDTO.getWorkflowName());
			offerWorkflow.setLower_margin(workflowDTO.getLowerMargin());
			offerWorkflow.setLower_margin_operator(workflowDTO.getLowerMarginOperator());
			offerWorkflow.setUpper_margin(workflowDTO.getUpperMargin());
			offerWorkflow.setUpper_margin_operator(workflowDTO.getUpperMarginOperator());
			offerWorkflow.setApprover_email(workflowDTO.getApproverEmail());
			offerWorkflow.setApprover_name(workflowDTO.getApproverName());

			if(workflowDTO.getOtherLoop()!=null)
				offerWorkflow.setOther_loop(workflowDTO.getOtherLoop());
			if(workflowDTO.getOtherLoopName()!=null)
				offerWorkflow.setOther_loop_name(workflowDTO.getOtherLoopName());

			offerWorkflow.setStatus(true);
			offerLetterWorkflowRepository.save(offerWorkflow);

			return new RestResponse(RestResponse.SUCCESS, "offerletter workflow save successfully !");
		}catch(Exception e){
			e.printStackTrace();
		}
		return new RestResponse(RestResponse.FAILED, "Internal server error !");
	}

	public RestResponse getOfferLetterWorkflowById(String id) {

		try{
			OfferLetterWorkflow offerWorkflow = offerLetterWorkflowRepository.findOne(Long.parseLong(id));

			if(offerWorkflow==null)
				return new RestResponse(RestResponse.FAILED, "offerletter workflow Not found !");

			return new RestResponse(RestResponse.SUCCESS,offerWorkflow, "get offerletter workflow successfully !");
		}catch(Exception e){
			e.printStackTrace();
		}

		return new RestResponse(RestResponse.FAILED, "Internal server error !");
	}

	public RestResponse deleteOfferLetterWorkflowById(String id) {

		try{
			OfferLetterWorkflow offerWorkflow = offerLetterWorkflowRepository.findOne(Long.parseLong(id));

			if(offerWorkflow==null)
				return new RestResponse(RestResponse.FAILED, "offerletter workflow Not found !");


			offerLetterWorkflowRepository.delete(Long.parseLong(id));

			return new RestResponse(RestResponse.SUCCESS, "delete offerletter workflow successfully !");
		}catch(Exception e){
			e.printStackTrace();
		}

		return new RestResponse(RestResponse.FAILED, "Internal server error !");
	}

	public RestResponse getAllOfferLetterWorkflows() {

		try{
			List<OfferLetterWorkflow> offerWorkflow = offerLetterWorkflowRepository.findAll();

			return new RestResponse(RestResponse.SUCCESS,offerWorkflow, "get All offerletter workflow successfully !");
		}catch(Exception e){
			e.printStackTrace();
		}

		return new RestResponse(RestResponse.FAILED, "Internal server error !");
	}

	public RestResponse getOfferLetterApprovalData(String positionId, String candidateId) {

		try{

			Position position = positionService.getPositionById(Long.parseLong(positionId));
			Candidate candidate = candidateService.getCandidateById(Long.parseLong(candidateId));

			if(position==null || candidate==null){
				return new RestResponse(RestResponse.FAILED, "Not Found data of either position Or candidate !");
			}


			OfferLetterApprovalDataDTO data = new OfferLetterApprovalDataDTO();

			data.setCandidateName(candidate.getFullName());
			data.setClientName(position.getClient().getClientName());
			data.setCurrentCTC(candidate.getCurrentCtc());
			data.setEmail(candidate.getEmail());
			data.setPositionName(position.getTitle());

			return new RestResponse(RestResponse.SUCCESS, "get Offer_letter_approval_data !");
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "Internal server error !");
		}

	}

	public RestResponse sendOfferLetterApprovalRequest(OfferLetterApprovalDTO approvalDTO) {

		return offerLetterApprovalsService.sendOfferLetterApprovalRequest(approvalDTO);
	}

	public RestResponse marginValueExistingInTemplateORNot(String marginValue) {

		boolean marginStatus = true;
		double margin = Double.parseDouble(marginValue);

		List<OfferLetterWorkflow> approvalList = offerLetterWorkflowRepository.findAll();

		for (OfferLetterWorkflow workflow : approvalList) {

			double lower = Double.parseDouble(workflow.getLower_margin());
			double upper = Double.parseDouble(workflow.getUpper_margin());

			int valLower = Double.compare(margin, lower);
			int valUpper = Double.compare(margin, upper);

			if((valLower==0 || valLower>0) && (valUpper==0 || valUpper<0)){

				if(valLower==0){
					if(workflow.getLower_margin_operator().equals(">") || workflow.getLower_margin_operator().equals("<"))
						continue;
				}
				if(valUpper==0){
					if(workflow.getUpper_margin_operator().equals(">") || workflow.getUpper_margin_operator().equals("<"))
						continue;
				}

				marginStatus = false;
				break;

			}

		}

		if(marginStatus)
			return new RestResponse(RestResponse.SUCCESS, "You can choose this value !");

		return new RestResponse(RestResponse.FAILED, "Pls choose another value, It already existing in templates !");
	}

	public void sendMailOfApproverResponse(OfferLetterApprovals offerLetterApproval, boolean isAccepted) throws MessagingException, IOException, RecruizException {

		emailTemplateDataService.sendMailOfApproverResponse(offerLetterApproval,isAccepted);

	}
}
