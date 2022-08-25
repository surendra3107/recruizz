package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.enums.FeedbackReason;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.FeedbackService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class CandidateFeedbackController {

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * get list of all candidate activity
	 * 
	 * @param roundCandidateDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/feedback/candidate/{candidateId}", method = RequestMethod.GET)
	public RestResponse getCandidateFeedback(@PathVariable("candidateId") String candidateId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
			UsageActionType.GetCandidateFeedback.name());*/
	    
	    
		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Map<String, Object> allFeedback = feedbackService.getFeedbackListByCandidate(candidateId,
				pageableService.getPageRequestObject(pageNo, sortField));
		if (allFeedback != null && !allFeedback.isEmpty()) {
			return new RestResponse(RestResponse.SUCCESS, allFeedback, null);
		}
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, "No Feedback Found", null);
		return addRoundResponse;
	}

	@RequestMapping(value = "auth/feedback/reason", method = RequestMethod.GET)
	public RestResponse getFeedbackReason() {

		List<BaseDTO> allFeedbackReason = new ArrayList<BaseDTO>();
		for (FeedbackReason reason : FeedbackReason.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(reason.name());
			baseDTO.setValue(reason.getDisplayName());
			allFeedbackReason.add(baseDTO);
		}

		RestResponse rangeResponse = new RestResponse(RestResponse.SUCCESS, allFeedbackReason);
		return rangeResponse;
	}

}
