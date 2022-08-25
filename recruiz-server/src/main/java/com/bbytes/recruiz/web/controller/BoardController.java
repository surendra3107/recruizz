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

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
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
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private RoundCandidateService roundCandidateService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private RoundService roundService;

    @Autowired
    private CheckUserPermissionService checkUserPermissionService;

    @Autowired
    private InterviewScheduleService interviewScheduleService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TenantUsageStatService tenantUsageStatService;
    
    @Autowired
    private EmailTemplateDataService emailTemplateDataService;

    @Autowired
    private IEmailService emailService;

    /**
     * API used to get all candidates for sourcing apart from board.
     * 
     * @param boardId
     * @return
     * @throws RecruizException
     */
    @RequestMapping(value = "/api/v1/board/candidate/source", method = RequestMethod.GET)
    public RestResponse getCandidatesToSource(@RequestParam(value = "boardId", required = false) String boardId,
	    @RequestParam(value = "searchQuery", required = false) String searchQuery) throws RecruizException {

	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetCandidateToSourceBoard.name());

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (boardId == null || boardId.isEmpty())
	    return null;

	List<Candidate> candidateList = boardService.getCandidateToSource(boardId, searchQuery);
	RestResponse response = new RestResponse(RestResponse.SUCCESS, candidateList, null);
	return response;
    }

    @RequestMapping(value = "/api/v1/board/candidate/feedback", method = RequestMethod.GET)
    public RestResponse getCandidateFeedback(
	    @RequestParam(value = "positionCode", required = false) String positionCode,
	    @RequestParam(value = "email", required = false) String email) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetCandidateFeedback.name());*/

	if (!checkUserPermissionService.isUserTypeApp())
	    throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

	if (positionCode == null || positionCode.isEmpty() || email == null || email.isEmpty())
	    return null;
	Candidate candidate = candidateService.getCandidateByEmail(email);
	List<Object> feedbackResp = new ArrayList<Object>();

	if (candidate != null) {
	    RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, positionCode);
	    if (roundCandidate != null) {
		List<String> roundIds = feedbackService.getFeedbackRoundsByCandidateId(roundCandidate.getId() + "");
		if (roundIds != null && !roundIds.isEmpty()) {
		    for (String id : roundIds) {
			Map<String, Object> feedbackMap = new java.util.HashMap<String, Object>();
			List<Feedback> feedbacks = feedbackService.getAllFeedbackByRoundIdAndCandidateId(roundCandidate,
				id, "Cancelled");
			// doing sorting here
			Collections.sort(feedbacks, new Comparator<Feedback>() {
			    @Override
			    public int compare(Feedback f1, Feedback f2) {
				return (int) (f2.getId() - f1.getId());
			    }
			});

			if (feedbacks != null && !feedbacks.isEmpty()) {
			    feedbackMap.put("roundId", id);
			    feedbackMap.put("roundName", roundService.getRoundName(id));
			    feedbackMap.put("feedback", feedbacks);
			    feedbackResp.add(feedbackMap);
			}
		    }
		}
	    }
	}

	// sorting the feedback list object based on round id
	if (feedbackResp != null && !feedbackResp.isEmpty()) {
	    Collections.sort(feedbackResp, new Comparator<Object>() {

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object o1, Object o2) {

		    Map<String, Object> feedbackMap1 = (Map<String, Object>) o1;
		    Map<String, Object> feedbackMap2 = (Map<String, Object>) o2;

		    return (int) (Integer.parseInt((String) feedbackMap2.get("roundId"))
			    - Integer.parseInt((String) feedbackMap1.get("roundId")));
		}
	    });
	}

	RestResponse response = new RestResponse(RestResponse.SUCCESS, feedbackResp, null);
	return response;
    }

    @RequestMapping(value = "/api/v1/board/feedback/remind/{fid}/{pcode}", method = RequestMethod.GET)
    public RestResponse sendFeedbackReminderFromBoard(@PathVariable("fid") Long fid,
	    @PathVariable("pcode") String pcode) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.SendFeedbackReminder.name());
*/
	RestResponse response = null;
	Feedback feedback = feedbackService.findOne(fid);
	try {
	    interviewScheduleService.sendReminderToGiveFeedback(pcode, feedback);
	    response = new RestResponse(true, SuccessHandler.FEEDBACK_REMINDER_SEND);
	} catch (RecruizException rex) {
	    response = new RestResponse(false, rex.getMessage(), rex.getErrConstant());
	} catch (Exception ex) {
	    response = new RestResponse(false, ex.getMessage(), ErrorHandler.FEEDBACK_REMINDER_FAILED);
	}
	return response;
    }

    @RequestMapping(value = "/api/v1/client/position/{pcode}/report", method = RequestMethod.GET)
    public RestResponse sendBoardReportToClient(@PathVariable("pcode") String pcode,
	    @RequestParam List<String> candidateFields, @RequestParam List<String> statusIn) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.SendBoardReportToClient.name());*/

	RestResponse response = null;
	Map<String, Object> templateData = emailTemplateDataService.getRenderedTemplateForBoardReport(pcode,
		candidateFields, statusIn);
	response = new RestResponse(true, templateData);

	return response;
    }

}
