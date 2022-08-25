package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.BoardCustomStatusService;
import com.bbytes.recruiz.service.BoardService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.FeedbackService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class BoardCustomStatusController {

    @Autowired
    private BoardCustomStatusService boardCustomStatusService;

    @RequestMapping(value = "/api/v1/board/custom/status/add", method = RequestMethod.POST)
    public RestResponse addBoardCustomeStatus(@RequestParam List<String> customStatus)
	    throws RecruizException {
	
	RestResponse response = null;
	
	List<BoardCustomStatus> boardStatus = new ArrayList<>();
	List<String> failedStatus = new ArrayList<>() ;
	
	for (String newStatus : customStatus) {
	    BoardCustomStatus status = boardCustomStatusService.getBoardCustomStatusByKey(newStatus.trim().replaceAll(" ", ""));
	    if(null != status) {
		failedStatus.add(newStatus);
		continue;
	    }
	    
	    status = new BoardCustomStatus();
	    status.setStatusKey(newStatus.trim().replaceAll(" ", ""));
	    status.setStatusName(newStatus);
	    boardStatus.add(status);
	}
	
	boardCustomStatusService.save(boardStatus);
	if(null != failedStatus && !failedStatus.isEmpty()) {
	    response = new RestResponse(RestResponse.SUCCESS, "Some of the staus already exists");    
	}else {
	    response = new RestResponse(RestResponse.SUCCESS, boardStatus);
	}
	
	return response;
    }
    
    @RequestMapping(value = "/api/v1/board/custom/status/edit/{id}", method = RequestMethod.PUT)
    public RestResponse updateCustomBoardStatus(@PathVariable Long id,@RequestParam String newStatusStatus)
	    throws RecruizException {
	
	RestResponse response = null;
	
	    BoardCustomStatus status = boardCustomStatusService.findOne(id);
	    if(null != status) {
		status.setStatusName(newStatusStatus.trim());
		boardCustomStatusService.save(status);
	    }
	    response = new RestResponse(RestResponse.SUCCESS,status);    
	
	return response;
    }
    
    @RequestMapping(value = "/api/v1/board/custom/status/delete/{id}", method = RequestMethod.DELETE)
    public RestResponse deleteBoardStatus(@PathVariable Long id)
	    throws RecruizException {
	
	RestResponse response = null;
	    BoardCustomStatus status = boardCustomStatusService.findOne(id);
	    if(null != status) {
		boardCustomStatusService.delete(status);
	    }
	    response = new RestResponse(RestResponse.SUCCESS,"Deleted Successfully");    
	
	return response;
    }

    @RequestMapping(value = "/api/v1/board/custom/status/all", method = RequestMethod.GET)
    public RestResponse getAllBoardStatus()
	    throws RecruizException {
	
	RestResponse response = null;
	    response = new RestResponse(RestResponse.SUCCESS,boardCustomStatusService.findAll());    
	
	return response;
    }
    
}
