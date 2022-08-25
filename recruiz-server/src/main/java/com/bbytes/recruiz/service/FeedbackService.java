package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.auth.jwt.ExternalUserAccessDataHolder;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.FeedbackRepository;
import com.bbytes.recruiz.rest.dto.models.FeedbackDTO;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackResultResponseDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;

@Service
public class FeedbackService extends AbstractService<Feedback, Long> {

	private FeedbackRepository feedbackRepository;

	@Autowired
	public FeedbackService(FeedbackRepository feedbackRepository) {
		super(feedbackRepository);
		this.feedbackRepository = feedbackRepository;
	}

	@Autowired
	private IEmailService emailService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@org.springframework.beans.factory.annotation.Value("${base.url}")
	private String baseUrl;

	@org.springframework.beans.factory.annotation.Value("${levelbar.server.url}")
	private String levelbarBaseUrl;

	@Transactional(readOnly = true)
	public List<String> getFeedbackRoundsByCandidateId(String roundCandidateId) {
		return feedbackRepository.findRoundCandidateIdList(roundCandidateId);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getFeedbackListByCandidate(String candidateId, Pageable pageable) {
		Page<Feedback> feedbacks = feedbackRepository.findByCandidateIdAndActiveIsTrueOrderByRoundCandidateIdDesc(candidateId, pageable);

		List<Feedback> feedbackList = new LinkedList<Feedback>();
		feedbackList = feedbacks.getContent();

		// create the thing to store the sub lists
		Map<String, List<Feedback>> subs = new LinkedHashMap<String, List<Feedback>>();

		// iterate through your objects
		for (Feedback feedback : feedbackList) {

			List<Feedback> temp = subs.get("@" + feedback.getClientName() + " #" + feedback.getPositionName());
			if (temp == null) {
				temp = new ArrayList<Feedback>();
				subs.put("@" + feedback.getClientName() + " #" + feedback.getPositionName(), temp);
			}
			temp.add(feedback);
		}
		Map<String, Object> feedbackMap = new HashMap<>();
		feedbackMap.put("feedback", subs);
		feedbackMap.put("totalPage", feedbacks.getTotalPages());
		return feedbackMap;
	}

	@Transactional(readOnly = true)
	public List<Feedback> getFeedbackByRoundIdAndCandidateId(RoundCandidate candidate, String roundId) {
		return feedbackRepository.findByRoundCandidateAndRoundIdAndActive(candidate, roundId, true);
	}

	@Transactional(readOnly = true)
	public List<Feedback> getFeedbackByRoundIdAndCandidateIdAndType(RoundCandidate candidate, String roundId, String type) {
		return feedbackRepository.findByRoundCandidateAndRoundIdAndActiveAndType(candidate, roundId, true, type);
	}

	/**
	 * will return all the feedback apart from cancelled and rescheduled event
	 * feedbacks
	 *
	 * @param candidate
	 * @param roundId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Feedback> getAllFeedbackByRoundIdAndCandidateId(RoundCandidate candidate, String roundId, String status) {
		return feedbackRepository.findByRoundCandidateAndRoundIdAndStatusNot(candidate, roundId, status);
	}

	@Transactional(readOnly = true)
	public String getFeedbackCountByStatus(String roundId, String status, String roundCandidateId) {
		return feedbackRepository.findFeedbackCount(roundId, status, roundCandidateId);
	}

	@Transactional(readOnly = true)
	public String getTotalFeedbackCount(String roundId, String roundCandidateId) {
		return feedbackRepository.findTotalActiveFeedbackCount(roundId, roundCandidateId);
	}

	@Transactional(readOnly = true)
	public boolean isFeedbackActive(String roundCandidateId, String roundId, String feedbackBy, boolean active, String type) {
		List<Feedback> feedback = feedbackRepository.findByRoundCandidateIdAndRoundIdAndFeedbackByAndActiveAndType(roundCandidateId,
				roundId, feedbackBy, active, type);

		if (feedback != null && !feedback.isEmpty())
			return true;
		return false;
	}

	@Transactional(readOnly = true)
	public Feedback getFeedbackByRoundCandidateAndFeedbackByAndType(String roundCandidateId, String roundId, String feedbackBy,
			boolean active, String type) {
		List<Feedback> feedbacks = feedbackRepository.findByRoundCandidateIdAndRoundIdAndFeedbackByAndActiveAndType(roundCandidateId,
				roundId, feedbackBy, active, type);

		if (feedbacks != null && !feedbacks.isEmpty()) {
			return feedbacks.get(0);
		}
		return null;
	}

	@Transactional(readOnly = true)
	public Feedback getFeedbackByLevelbarKey(String levelbarKey) {

		Feedback feedback = feedbackRepository.findByLevelbarFeedbackShareResultId(levelbarKey);
		return feedback;
	}

	@Transactional(readOnly = true)
	public List<String> getRoundCAndidateIdByCandidate(String candidateId) {
		return feedbackRepository.findRoundCandidateIdByCandidate(candidateId);
	}

	@Transactional(readOnly = true)
	public Feedback getFeedbackByID(String feedbackId) {
		return feedbackRepository.findOne(Long.parseLong(feedbackId));
	}

	@Transactional
	public void sendEmailOnfeedbackReceived(FeedbackDTO feedbackDTO, Candidate candidate, RoundCandidate roundCandidate, Position position,
			ExternalUserAccessDataHolder externalUser, Feedback expectedFeedback) throws RecruizException {
		final String feedbackTemplate = GlobalConstants.FEEDBACK_RESPONSE_EMAIL_TEMPLATE;
		Set<User> hrExecutives = position.getHrExecutives();
		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			List<String> hrEmailList = new ArrayList<>();
			for (User user : hrExecutives) {
				hrEmailList.add(user.getEmail());
			}

			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put("candidateName", candidate.getFullName());
			valueMap.put("interviewerName", expectedFeedback.getFeedbackByName());
			valueMap.put("interviewerEmail", expectedFeedback.getFeedbackBy());
			valueMap.put("positionTitle", expectedFeedback.getPositionName());
			valueMap.put("clientName", expectedFeedback.getClientName());
			valueMap.put("feedbackStatus", feedbackDTO.getStatus());
			if (feedbackDTO.getStatus().equalsIgnoreCase("Approved")) {
				valueMap.put("reason", "N/A");
			} else {
				valueMap.put("reason", StringUtils.commaSeparate(feedbackDTO.getReason()));
			}

			valueMap.put("ratings", feedbackDTO.getRating());
			valueMap.put("comments", feedbackDTO.getFeedback());
			valueMap.put("eventCreatedBy", expectedFeedback.getEventCreatedBy());

			String link = baseUrl + GlobalConstants.BOARD_URL + feedbackDTO.getPositionCode() + "/";

			String emailSubject = "Feedback recieved for candidate " + candidate.getFullName() + " for position " + position.getTitle()
					+ " (Client : " + expectedFeedback.getClientName() + " )";

			String emailBody = emailTemplateDataService.getHtmlContentFromFile(valueMap, feedbackTemplate);
			String masterTemplateBody = emailTemplateDataService.getMasterTemplateWithButton(emailBody, link, "View");

			emailService.sendEmail(hrEmailList, masterTemplateBody, emailSubject);
		}

		// making entry to candidate activity
		candidateActivityService.feedbackRecievedEvent(feedbackDTO, roundCandidate, externalUser);

		// TODO
		// make entry to notification here
	}

	@Transactional
	public void sendEmailOnLevelbarFeedbackReceived(FeedbackResultResponseDTO feedbackResultResponseDTO, Candidate candidate,
			RoundCandidate roundCandidate, Position position, ExternalUserAccessDataHolder externalUser) throws RecruizException {

		final String link = levelbarBaseUrl + GlobalConstants.LEVELBAR_FEEDBACK_VIEW_URL + feedbackResultResponseDTO.getId();

		Feedback feedback = getFeedbackByLevelbarKey(feedbackResultResponseDTO.getId());

		feedback.setFeedback(link);
		feedback.setStatus(feedbackResultResponseDTO.getFeedbackStatus());
		feedback.setRatings(feedbackResultResponseDTO.getOverallrating().toString());
		feedbackRepository.save(feedback);

		final String feedbackTemplate = GlobalConstants.LEVELBAR_FEEDBACK_RESPONSE_EMAIL_TEMPLATE;
		Set<User> hrExecutives = position.getHrExecutives();
		if (hrExecutives != null && !hrExecutives.isEmpty()) {
			List<String> hrEmailList = new ArrayList<>();
			for (User user : hrExecutives) {
				hrEmailList.add(user.getEmail());
			}

			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put("candidateName", candidate.getFullName());
			valueMap.put("interviewerName", externalUser.getExtenalUserName());
			valueMap.put("positionTitle", position.getTitle());
			valueMap.put("clientName", feedbackResultResponseDTO.getClientName());
			valueMap.put("eventCreatedBy", feedback.getEventCreatedBy());

			String emailSubject = "Feedback recieved for candidate " + candidate.getFullName() + " for position " + position.getTitle()
					+ " (Client : " + feedbackResultResponseDTO.getClientName() + " )";

			String emailBody = emailTemplateDataService.getHtmlContentFromFile(valueMap, feedbackTemplate);
			String masterTemplateBody = emailTemplateDataService.getMasterTemplateWithButton(emailBody, link, "View");

			emailService.sendEmail(hrEmailList, masterTemplateBody, emailSubject);
		}

		// making entry to candidate activity
		candidateActivityService.levelbarFeedbackRecievedEvent(feedback.getRoundId(), roundCandidate, externalUser);
	}

	@Transactional(readOnly = true)
	public long getGivenFeedbackCount() {
		return feedbackRepository.countByFeedbackNot(GlobalConstants.DEFAULT_FEEDBACK_TEXT);
	}

	@Transactional(readOnly = true)
	public String getTotalFeedbackCountByRoundCandidate(String roundCandidateId) {
		return feedbackRepository.findTotalActiveFeedbackCountByCandidate(roundCandidateId);
	}

	@Transactional(readOnly = true)
	public String getFeedbackCountByStatusAndCandidate(String status, String roundCandidateId) {
		return feedbackRepository.findFeedbackCountByCandidateAndStatus(status, roundCandidateId);
	}

	/**
	 * To cancel feedback for given interviewer
	 *
	 * @param email
	 */
	@Transactional
	public void cancelFeedback(String email) {
		List<Feedback> feedbacks = feedbackRepository.findByFeedbackByAndActive(email, true);
		if (null != feedbacks && !feedbacks.isEmpty()) {
			for (Feedback feedback : feedbacks) {
				feedback.setActive(false);
				// feedback.setStatus(GlobalConstants.CANCELLED);
			}
			feedbackRepository.save(feedbacks);
		}
	}

	@Transactional(readOnly = true)
	public Long getForwardProfileCountForPosition(String positionName, String clientName, Date startDate, Date endDate) {
		Date stDate = new Date(startDate.getTime());
		Date edDate = new Date(endDate.getTime());
		return feedbackRepository.findTotalActiveFeedbackCountByCandidate(positionName, clientName, stDate, edDate);
	}

	@Transactional(readOnly = true)
	public Long getForwardProfileCountForPositionByUser(String positionName, String clientName, String userEmail) {
		return feedbackRepository.countByPositionNameAndClientNameAndEventCreatorEmailAndTypeNot(positionName, clientName, userEmail,
				"interview");
	}
	//@author - Sajin
	@Transactional(readOnly = true)
	public Long getForwardProfileCountForListofPositionsByUser(List<String> positionNames, String userEmail, Date startDate ,Date endDate) {
		return feedbackRepository.countByPositionListAndClientNameAndEventCreatorEmail(positionNames, userEmail, startDate, endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getForwardProfileCountForPositionByUserAndDateRange(String positionName, String clientName, String userEmail,Date startDate ,Date endDate) {
		return feedbackRepository.countByPositionNameAndClientNameAndEventCreatorEmailAndTypeNotAndModificationDateBetween(positionName, clientName, userEmail,
				"interview",startDate,endDate);
	}
	
	@Transactional(readOnly = true)
	public Long getForwardProfileCountForPositionAndDateRange(String positionName, String clientName,Date startDate ,Date endDate) {
		return feedbackRepository.countByPositionNameAndClientNameAndTypeNotAndModificationDateBetween(positionName, clientName,
				"interview",startDate,endDate);
	}

	
	
	@Transactional(readOnly = true)
	public Long getForwardProfileCountForPositionByUser(List<String> positionNames, String clientName, String userEmail) {
		return feedbackRepository.countByPositionNameInAndClientNameAndEventCreatorEmailAndTypeNot(positionNames, clientName, userEmail,
				"interview");
	}

	public Feedback getTopFeedbackByRoundCandidateForForwardType(Long roundCandidateId) {
		return feedbackRepository.getTopFeedbackByRoundCandidateForForwardType(roundCandidateId);
	}

	public Long getFeedbackCountByPositionCode(List<String> positionCodes) {
		return feedbackRepository.getTotalFeedbackCountByPositionCodes(positionCodes);
	}

	public Long getFeedbackCountByPositionCodeAndDateRange(List<String> positionCodes, Date startDate, Date endDate) {
		return feedbackRepository.getTotalFeedbackCountByPositionCodesAndDateRange(positionCodes, startDate, endDate);
	}

}
