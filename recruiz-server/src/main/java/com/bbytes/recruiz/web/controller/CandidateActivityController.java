package com.bbytes.recruiz.web.controller;

import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.CandidateActivity;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.utils.GlobalConstants;

@RestController
public class CandidateActivityController {

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	/**
	 * get list of all candidate activity
	 * 
	 * @param roundCandidateDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/activity/candidate/{candidateId}", method = RequestMethod.GET)
	public RestResponse getCandidateActivity(@PathVariable("candidateId") String candidateId,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField) throws RecruizException {

		if(!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		Page<CandidateActivity> allActivity = candidateActivityService.getCandidateActivityById(candidateId,pageableService.getPageRequestObject(pageNo, sortField));
		for (CandidateActivity candidateActivity : allActivity) {
			if(candidateActivity.getType().equalsIgnoreCase(CandidateActivityType.Email.getDisplayName())) {
				try{
					String data = URLEncoder.encode(candidateActivity.getWhat().substring(candidateActivity.getWhat().indexOf("<html")));
					//	candidateActivity.setWhat("data:text/html;charset=utf-8,"+data);
					candidateActivity.setWhat(candidateActivity.getWhat().substring(candidateActivity.getWhat().indexOf("<html")));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, allActivity, null);
		return addRoundResponse;
	}
}
