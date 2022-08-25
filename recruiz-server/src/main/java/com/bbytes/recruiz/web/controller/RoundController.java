package com.bbytes.recruiz.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CustomRounds;
import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionCandidateData;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.EmployeeStatus;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.PositionCandidateDataRepository;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RoundDTO;
import com.bbytes.recruiz.service.BoardCustomStatusService;
import com.bbytes.recruiz.service.BoardService;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateNotesService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CandidateStatusService;
import com.bbytes.recruiz.service.CustomRoundService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.EmployeeService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RecruizConnectService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.RestResponseConstant;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class RoundController {

	private final Logger logger = LoggerFactory.getLogger(RoundController.class);

	@Autowired
	private RoundService roundService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private CandidateStatusService candidateStatusService;

	@Autowired
	private CandidateNotesService candidateNoteService;

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomRoundService customRoundService;

	@Autowired
	private BoardCustomStatusService boardCustomStatusService;

	@Autowired
	PositionCandidateDataRepository positionCandidateDataRepository;

	@Autowired
	EmailActivityService emailActivityService;

	/**
	 * API Used to add candidates to source round of board.
	 * 
	 * @param roundCandidateDTO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/round/candidate/source", method = RequestMethod.POST)
	public RestResponse sourceCandidate(@RequestBody CandidateToRoundDTO roundCandidateDTO,
			@RequestParam(value = "sourceMode", required = false) String sourceMode) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.SourceCandidateToRound.name());

		String positionCode = roundCandidateDTO.getPositionCode();
		Position position = positionService.getPositionByCode(positionCode);
		List<String> candidateList = roundCandidateService.addCandidateToPosition(roundCandidateDTO, sourceMode);

		//Return the list based on if the candidate has already been sourced to the position
		//Returns the list to the frontend to show the candidates who are already sourced to the position
		if 	(candidateList == null || candidateList.isEmpty())
		{
			RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, candidateList, RestResponseConstant.CANDIDATE_ADDED);
			if(position!=null){
				position.setModificationDate(new Date());
				positionService.save(position);
			}
			return addRoundResponse;


		} else
		{
			RestResponse addRoundResponse = new RestResponse(RestResponse.FAILED, candidateList, RestResponseConstant.FEW_CANDIDATES_EXITS);
			return addRoundResponse;


		}



	}

	// /**
	// * @param roundCandidateDTO
	// * @param roundId
	// * @param round
	// * @throws RecruizException
	// */
	// private void sourceCandidateToBoard(CandidateToRoundDTO
	// roundCandidateDTO, Round round) throws RecruizException {
	//
	// if
	// (round.getBoard().getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
	// ||
	// round.getBoard().getClientStatus().equalsIgnoreCase(Status.Closed.toString()))
	// {
	// throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION,
	// ErrorHandler.CLIENT_ONHOLD_CLOSED);
	// }
	// if
	// (round.getBoard().getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
	// ||
	// round.getBoard().getPositionStatus().equalsIgnoreCase(Status.Closed.toString()))
	// {
	// throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION,
	// ErrorHandler.POSITION_ONHOLD_CLOSED);
	// }
	//
	// // defining list to add to candidate activity
	// List<CandidateActivity> allCandidateActivity = new
	// ArrayList<CandidateActivity>();
	// CandidateActivity candidateActivity = null;
	// // reindexing all existing candidates
	// List<RoundCandidate> roundCandidateList =
	// roundCandidateService.getCandidateByRoundId(round.getId() + "");
	// Collections.sort(roundCandidateList);
	// int index = 1;
	// for (RoundCandidate roundCandidate : roundCandidateList) {
	// roundCandidate.setCardIndex(index++);
	// }
	// int cardIndex = roundCandidateList.size();
	// Set<RoundCandidate> candidateList = new HashSet<RoundCandidate>();
	// for (String email : roundCandidateDTO.getCandidateEmailList()) {
	// cardIndex++;
	// Candidate candidate = candidateService.getCandidateByEmail(email);
	// RoundCandidate existingCandidate =
	// roundCandidateService.getExistingBoardCandidate(candidate,
	// positionService.getPositionByBoard(round.getBoard()).getPositionCode());
	// if (existingCandidate == null) {
	// RoundCandidate roundCandidate = new RoundCandidate();
	// roundCandidate.setCandidate(candidate);
	// roundCandidate.setStatus(BoardStatus.InProgress.toString());
	// roundCandidate.setRoundId(round.getId() + "");
	// roundCandidate.setPositionCode(roundCandidateDTO.getPositionCode());
	// roundCandidate.setRound(round);
	// roundCandidate.setCardIndex(cardIndex);
	// roundCandidateService.save(roundCandidate);
	// candidateList.add(roundCandidate);
	// // adding to candidate activity list
	// candidateActivity =
	// candidateActivityService.addedToBoardEvent(roundCandidateDTO, candidate);
	// allCandidateActivity.add(candidateActivity);
	// }
	// }
	// round.getCandidates().addAll(candidateList);
	// roundService.save(round);
	//
	// candidateActivityService.save(allCandidateActivity);
	// }

	/**
	 * API Used to move candidates from one round to another.
	 * 
	 * @param roundCandidateDTO
	 * @param destRoundId
	 * @param sourceRoundId
	 * @param cardIndex
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/round/candidate/move", method = RequestMethod.PUT)
	public RestResponse moveCandidateToRound(@RequestBody CandidateToRoundDTO roundCandidateDTO,
			@RequestParam("destRoundId") String destRoundId, @RequestParam("sourceRoundId") String sourceRoundId,
			@RequestParam("cardIndex") String cardIndex) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.MoveCandidate.name());

		if (destRoundId == null || destRoundId.isEmpty())
			throw new RecruizWarnException(ErrorHandler.INVALID_MOVE, ErrorHandler.INVALID_REQUEST);

		Round destRound = roundService.findOne(Long.parseLong(destRoundId));
		Round sourceRound = roundService.findOne(Long.parseLong(sourceRoundId));

		if (destRound.getBoard().getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| destRound.getBoard().getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (destRound.getBoard().getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| destRound.getBoard().getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}
		// getting all round candidates by round id and sorting by card index
		List<RoundCandidate> destRoundCandidateList = roundCandidateService.getCandidateByRoundId(destRoundId);
		Collections.sort(destRoundCandidateList);

		double candidateCardIndex = Double.parseDouble(cardIndex);
		RoundCandidate movedCandidate = null;
		RoundCandidateDTO roundCandidateConvertedDTO = null;
		for (String email : roundCandidateDTO.getCandidateEmailList()) {

			Candidate candidate = candidateService.getCandidateByEmail(email);
			RoundCandidate existingCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate,
					sourceRoundId);


			if (existingCandidate == null || existingCandidate.getPositionCode() == null
					|| existingCandidate.getPositionCode().isEmpty() || existingCandidate.getRoundId() == null
					|| existingCandidate.getRoundId().isEmpty()) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.CANNOT_MOVE_CANDIDATE,
						ErrorHandler.MOVING_CANDIDATE_FAILED);
			}

			String oldStatus = existingCandidate.getStatus();
			Position position = positionService.getPositionByCode(existingCandidate.getPositionCode());
			/**
			 * ###############################################################
			 * Commenting below code of closing active interview schedule as it
			 * is not applicable on new requirement of scheduling multiple
			 * interviews ####################################################
			 */

			/*
			 * if (!destRoundId.equalsIgnoreCase(sourceRoundId)) {
			 * InterviewSchedule existingSchedule =
			 * interviewScheduleService.getScheduleByPositionCodeRoundEmail(
			 * existingCandidate.getPositionCode(),
			 * existingCandidate.getRoundId(), email); if (existingSchedule !=
			 * null) { existingSchedule.setActive(false);
			 * interviewScheduleService.save(existingSchedule); } }
			 */

			existingCandidate.setRoundId(destRoundId);
			existingCandidate.setRound(destRound);
			existingCandidate.setStatus(BoardStatus.InProgress.toString());
			existingCandidate.setCardIndex(candidateCardIndex);

			/**
			 * ###############################################################
			 * Commenting below code of closing active interview schedule as it
			 * is not applicable on new requirement of scheduling multiple
			 * interviews ####################################################
			 */
			/*
			 * Set<Feedback> existingFeedback = existingCandidate.getFeedback();
			 * if (!destRoundId.equalsIgnoreCase(sourceRoundId)) { if
			 * (existingFeedback != null && !existingFeedback.isEmpty()) { for
			 * (Feedback feedback : existingFeedback) {
			 * feedback.setActive(false); } } }
			 */

			movedCandidate = roundCandidateService.save(existingCandidate);

			PositionCandidateData posData = new PositionCandidateData();
			posData.setCandidateId(candidate.getCid());
			posData.setClientId(position.getClient().getId());
			posData.setPositionId(position.getId());
			posData.setLoggedUserId(userService.getLoggedInUserObject().getUserId());
			posData.setFromStage(sourceRound.getRoundName());
			posData.setToStage(destRound.getRoundName());
			posData.setFromStatus(oldStatus);
			posData.setToStatus(BoardStatus.InProgress.toString());
			posData.setModificationTimestamp(new Date());		
			posData = positionCandidateDataRepository.save(posData);


			roundCandidateConvertedDTO = dataModelToDTOConversionService.convertRoundCandidate(movedCandidate,
					destRound);

			// making a entry to candidate activity
			candidateActivityService.movedToRoundEvent(destRound, sourceRound, candidate, existingCandidate);
		}

		recruizConnectService.moveCandidateToRound(sourceRound.getConnectId(), destRound.getConnectId(),
				Double.parseDouble(cardIndex), roundCandidateDTO.getCandidateEmailList());

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, roundCandidateConvertedDTO);
		return addRoundResponse;
	}

	/**
	 * API Used to change the status of candidates within round.
	 * 
	 * @param roundCandidateDTO
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/round/candidate/status", method = RequestMethod.PUT)
	public RestResponse changeCandidateStatus(@RequestBody CandidateToRoundDTO roundCandidateDTO,
			@RequestParam(name = "changeExistingStatus", required = false) Boolean changeExistingStatus)
					throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangeCandidateStatus.name());

		Position position = null;
		if (roundCandidateDTO.getPositionCode() == null || roundCandidateDTO.getStatus() == null)
			new RestResponse(false, ErrorHandler.ROUNDID_STATUS_MISSING, ErrorHandler.INVALID_REQUEST);

		if (roundCandidateDTO.getStatus() == null || roundCandidateDTO.getStatus().isEmpty()) {
			return new RestResponse(false, ErrorHandler.INVALID_STATUS_SELECTED, ErrorHandler.INVALID_STATUS);
		}

		if (boardCustomStatusService.getBoardCustomStatusByKey(roundCandidateDTO.getStatus()) == null
				&& BoardStatus.valueOf(roundCandidateDTO.getStatus()) == null) {
			return new RestResponse(false, ErrorHandler.INVALID_STATUS_SELECTED, ErrorHandler.INVALID_STATUS);
		}

		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Joined.getDisplayName())) {
			position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
			int totalRequirement = position.getTotalPosition();
			List<String> joinedStatus = new ArrayList<>();
			joinedStatus.add(BoardStatus.Joined.getDisplayName());
			int joinedInCount = 0;
			Long joinedCount = roundCandidateService
					.getCountByPositionCodeAndStatusIn(roundCandidateDTO.getPositionCode(), joinedStatus);
			if (null != joinedCount) {
				joinedInCount = joinedCount.intValue();
			}
			if (roundCandidateDTO.getJoinedBy() == null || roundCandidateDTO.getJoinedBy().isEmpty()) {
				roundCandidateDTO.setJoinedBy(userService.getLoggedInUserEmail());
			}
			if (joinedInCount >= totalRequirement) {
				return new RestResponse(false, ErrorHandler.CAN_NOT_JOIN_MORE_CANDIDATE,
						ErrorHandler.STATUS_CHANGE_NOT_ALLOWED);
			}
		}

		Board bord = positionService.getPositionBoard(roundCandidateDTO.getPositionCode());

		if (bord.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			new RestResponse(false, ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (bord.getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			new RestResponse(false, ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Rejected.getDisplayName())
				&& (null == roundCandidateDTO.getRejectReason() || roundCandidateDTO.getRejectReason().isEmpty())) {
			new RestResponse(false, ErrorHandler.REASON_IS_MANDATORY, ErrorHandler.REASON_MANDATORY);
		}

		// checking existing employee status
		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Employee.name())) {
			List<String> employedCandidates = new ArrayList<String>();
			for (String email : roundCandidateDTO.getCandidateEmailList()) {
				Candidate candidate = candidateService.getCandidateByEmail(email);

				List<RoundCandidate> existingEmployeeCandidate = roundCandidateService
						.getCandidatebyStatusAndCandidate(BoardStatus.Employee.name(), candidate);

				if (existingEmployeeCandidate != null && !existingEmployeeCandidate.isEmpty()) {
					if (null != changeExistingStatus && changeExistingStatus) {
						for (RoundCandidate roundCandidate : existingEmployeeCandidate) {
							roundCandidate.setStatus(BoardStatus.MovedOut.name());
							roundCandidate.setJoinedByHr(roundCandidateDTO.getJoinedBy());
						}
						roundCandidateService.save(existingEmployeeCandidate);
					} else {
						employedCandidates.add(candidate.getFullName());
						continue;
					}
				}
			}

			if (employedCandidates != null && !employedCandidates.isEmpty()) {
				String clientLabel = "department";
				if (organizationService.isAgency()) {
					clientLabel = "client";
				}
				return new RestResponse(RestResponse.FAILED,
						StringUtils.commaSeparate(employedCandidates) + " is/are in employee status under another "
								+ clientLabel + "/position. Do you want to still proceed with this status change?",
								ErrorHandler.FEW_STATUS_CHANGE_FAILED_CANDIDATE_IN_EMPLOYEE_STATUS);
			}

		}

		// changing the status below
		for (String email : roundCandidateDTO.getCandidateEmailList()) {
			Candidate candidate = candidateService.getCandidateByEmail(email);

			position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
			Board board = positionService.getPositionBoard(roundCandidateDTO.getPositionCode());

			for (Round round : board.getRounds()) {
				RoundCandidate existingCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate,
						round.getId() + "");
				if (existingCandidate != null
						&& !existingCandidate.getStatus().equalsIgnoreCase(roundCandidateDTO.getStatus())) {
					String oldStatus = existingCandidate.getStatus();
					String newStatus = roundCandidateDTO.getStatus();
					existingCandidate.setStatus(roundCandidateDTO.getStatus());
					existingCandidate.setJoinedDate(roundCandidateDTO.getJoiningDate());
					existingCandidate.setJoinedByHr(roundCandidateDTO.getJoinedBy());

					if(roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Joined.getDisplayName())){
						existingCandidate.setSourcedBy(roundCandidateDTO.getJoinedBy());
					}

					// adding entry in candidate status table
					candidateStatusService.addCandidateStatus(position.getClient().getId(), candidate.getCid(),
							position.getPositionCode(), roundCandidateDTO.getStatus());

					
					if(userService.getUserByEmail(existingCandidate.getSourcedBy()).getUserRole().getRoleName().equalsIgnoreCase("vendor") 
							&& !userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())){
						String roundCandidateName = existingCandidate.getCandidate().getFullName();
						String roundCandidateSouredBy = userService.getUserByEmail(existingCandidate.getSourcedBy()).getName();
						String SouredByEmail = userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail();

						emailActivityService.sendMailToVendorForChangesInCandidateStatus(oldStatus,newStatus,roundCandidateSouredBy,roundCandidateName,SouredByEmail);
					}
					
					roundCandidateService.save(existingCandidate);

					if(candidate.getSource().equalsIgnoreCase(Source.CareersPortal.getDisplayName()) && (candidate.getOwner() == null || candidate.getOwner().equalsIgnoreCase(""))) {
						candidate.setOwner(userService.getLoggedInUserEmail());
					}


					// making entry to candidate activity
					candidateActivityService.boardStatusChangedEvent(roundCandidateDTO, candidate, existingCandidate,
							oldStatus);

					// making entry to candidate notes if reason is provided
					if (null != roundCandidateDTO.getRejectReason() && !roundCandidateDTO.getRejectReason().isEmpty()) {
						candidateNoteService.addNote(position.getTitle(), roundCandidateDTO.getRejectReason(), email);
					}

					// converting to employee if status is employee
					if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Employee.name())) {
						Employee employee = new Employee();
						employee.setPresonalEmail(candidate.getEmail());
						employee.setFirstName(candidate.getFullName());
						employee.setPrimaryContact(candidate.getMobile());
						employee.setOfficialEmail(candidate.getEmail());
						employee.setEmploymentStatus(EmployeeStatus.Active.name());
						employee.setEmploymentType("TBD");
						employee.setPositionCode(position.getPositionCode());
						employee.setClientName(position.getClient().getClientName());
						employee.setPositionTitle(position.getTitle());
						employee.setStatus(EmployeeStatus.Active.name());
						try {
							employeeService.saveEmployee(employee);
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
							existingCandidate.setStatus(oldStatus);
							roundCandidateService.save(existingCandidate);
						}

					}

					PositionCandidateData posData = new PositionCandidateData();
					posData.setCandidateId(candidate.getCid());
					posData.setClientId(position.getClient().getId());
					posData.setPositionId(position.getId());
					posData.setLoggedUserId(userService.getLoggedInUserObject().getUserId());
					posData.setFromStatus(oldStatus);
					posData.setToStatus(newStatus);
					posData.setFromStage(existingCandidate.getRound().getRoundName());
					posData.setToStage(existingCandidate.getRound().getRoundName());
					posData.setModificationTimestamp(new Date());		
					positionCandidateDataRepository.save(posData);

				}
			}
		}
		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS,
				RestResponseConstant.CANDIDATE_STATUS_CHANGED, null);

		return addRoundResponse;
	}


	@RequestMapping(value = "/api/v1/round/candidate/candidateStatus", method = RequestMethod.PUT)
	public RestResponse MoveCandidateWithChangeStatus(@RequestBody CandidateToRoundDTO roundCandidateDTO,
			@RequestParam(name = "changeExistingStatus", required = false) Boolean changeExistingStatus)
					throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.ChangeCandidateStatus.name());

		List<Long> roundDataList = new ArrayList<>();
		Position position = null;
		if (roundCandidateDTO.getPositionCode() == null || roundCandidateDTO.getStatus() == null)
			new RestResponse(false, ErrorHandler.ROUNDID_STATUS_MISSING, ErrorHandler.INVALID_REQUEST);

		if (roundCandidateDTO.getStatus() == null || roundCandidateDTO.getStatus().isEmpty()) {
			return new RestResponse(false, ErrorHandler.INVALID_STATUS_SELECTED, ErrorHandler.INVALID_STATUS);
		}

		if (boardCustomStatusService.getBoardCustomStatusByKey(roundCandidateDTO.getStatus()) == null
				&& BoardStatus.valueOf(roundCandidateDTO.getStatus()) == null) {
			return new RestResponse(false, ErrorHandler.INVALID_STATUS_SELECTED, ErrorHandler.INVALID_STATUS);
		}

		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Joined.getDisplayName())) {
			position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
			int totalRequirement = position.getTotalPosition();
			List<String> joinedStatus = new ArrayList<>();
			joinedStatus.add(BoardStatus.Joined.getDisplayName());
			int joinedInCount = 0;
			Long joinedCount = roundCandidateService
					.getCountByPositionCodeAndStatusIn(roundCandidateDTO.getPositionCode(), joinedStatus);
			if (null != joinedCount) {
				joinedInCount = joinedCount.intValue();
			}
			if (roundCandidateDTO.getJoinedBy() == null || roundCandidateDTO.getJoinedBy().isEmpty()) {
				roundCandidateDTO.setJoinedBy(userService.getLoggedInUserEmail());
			}
			if (joinedInCount >= totalRequirement) {
				return new RestResponse(false, ErrorHandler.CAN_NOT_JOIN_MORE_CANDIDATE,
						ErrorHandler.STATUS_CHANGE_NOT_ALLOWED);
			}
		}

		Board bord = positionService.getPositionBoard(roundCandidateDTO.getPositionCode());

		if (bord.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			new RestResponse(false, ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (bord.getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			new RestResponse(false, ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Rejected.getDisplayName())
				&& (null == roundCandidateDTO.getRejectReason() || roundCandidateDTO.getRejectReason().isEmpty())) {
			new RestResponse(false, ErrorHandler.REASON_IS_MANDATORY, ErrorHandler.REASON_MANDATORY);
		}

		// checking existing employee status
		if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Employee.name())) {
			List<String> employedCandidates = new ArrayList<String>();
			for (String email : roundCandidateDTO.getCandidateEmailList()) {
				Candidate candidate = candidateService.getCandidateByEmail(email);

				List<RoundCandidate> existingEmployeeCandidate = roundCandidateService
						.getCandidatebyStatusAndCandidate(BoardStatus.Employee.name(), candidate);

				if (existingEmployeeCandidate != null && !existingEmployeeCandidate.isEmpty()) {
					if (null != changeExistingStatus && changeExistingStatus) {
						for (RoundCandidate roundCandidate : existingEmployeeCandidate) {
							roundCandidate.setStatus(BoardStatus.MovedOut.name());
							roundCandidate.setJoinedByHr(roundCandidateDTO.getJoinedBy());
						}

						roundCandidateService.save(existingEmployeeCandidate);
					} else {
						employedCandidates.add(candidate.getFullName());
						continue;
					}
				}
			}

			if (employedCandidates != null && !employedCandidates.isEmpty()) {
				String clientLabel = "department";
				if (organizationService.isAgency()) {
					clientLabel = "client";
				}
				return new RestResponse(RestResponse.FAILED,
						StringUtils.commaSeparate(employedCandidates) + " is/are in employee status under another "
								+ clientLabel + "/position. Do you want to still proceed with this status change?",
								ErrorHandler.FEW_STATUS_CHANGE_FAILED_CANDIDATE_IN_EMPLOYEE_STATUS);
			}

		}

		// changing the status below
		for (String email : roundCandidateDTO.getCandidateEmailList()) {
			Candidate candidate = candidateService.getCandidateByEmail(email);

			position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
			Board board = positionService.getPositionBoard(roundCandidateDTO.getPositionCode());

			for (Round round : board.getRounds()) {
				RoundCandidate existingCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate,
						round.getId() + "");
				if (existingCandidate != null
						&& !existingCandidate.getStatus().equalsIgnoreCase(roundCandidateDTO.getStatus())) {
					String oldStatus = existingCandidate.getStatus();
					String newStatus = roundCandidateDTO.getStatus();
					existingCandidate.setStatus(roundCandidateDTO.getStatus());
					existingCandidate.setJoinedDate(roundCandidateDTO.getJoiningDate());
					existingCandidate.setJoinedByHr(roundCandidateDTO.getJoinedBy());

					if(roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Joined.getDisplayName())){
						existingCandidate.setSourcedBy(roundCandidateDTO.getJoinedBy());
					}

					// adding entry in candidate status table
					candidateStatusService.addCandidateStatus(position.getClient().getId(), candidate.getCid(),
							position.getPositionCode(), roundCandidateDTO.getStatus());

					roundCandidateService.save(existingCandidate);

					// making entry to candidate activity
					candidateActivityService.boardStatusChangedEvent(roundCandidateDTO, candidate, existingCandidate,
							oldStatus);

					// making entry to candidate notes if reason is provided
					if (null != roundCandidateDTO.getRejectReason() && !roundCandidateDTO.getRejectReason().isEmpty()) {
						candidateNoteService.addNote(position.getTitle(), roundCandidateDTO.getRejectReason(), email);
					}

					// converting to employee if status is employee
					if (roundCandidateDTO.getStatus().equalsIgnoreCase(BoardStatus.Employee.name())) {
						Employee employee = new Employee();
						employee.setPresonalEmail(candidate.getEmail());
						employee.setFirstName(candidate.getFullName());
						employee.setPrimaryContact(candidate.getMobile());
						employee.setOfficialEmail(candidate.getEmail());
						employee.setEmploymentStatus(EmployeeStatus.Active.name());
						employee.setEmploymentType("TBD");
						employee.setPositionCode(position.getPositionCode());
						employee.setClientName(position.getClient().getClientName());
						employee.setPositionTitle(position.getTitle());
						employee.setStatus(EmployeeStatus.Active.name());
						try {
							employeeService.saveEmployee(employee);
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
							existingCandidate.setStatus(oldStatus);
							roundCandidateService.save(existingCandidate);
						}

					}

					PositionCandidateData posData = new PositionCandidateData();
					posData.setCandidateId(candidate.getCid());
					posData.setClientId(position.getClient().getId());
					posData.setPositionId(position.getId());
					posData.setLoggedUserId(userService.getLoggedInUserObject().getUserId());
					posData.setFromStatus(oldStatus);
					posData.setToStatus(newStatus);
					posData.setModificationTimestamp(new Date());		
					posData = positionCandidateDataRepository.save(posData);

					roundDataList.add(posData.getId());

				}
			}
		}
		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS,
				roundDataList,RestResponseConstant.CANDIDATE_STATUS_CHANGED);

		return addRoundResponse;
	}



	@RequestMapping(value = "/api/v1/round/candidate/moveWithStatus", method = RequestMethod.PUT)
	public RestResponse moveCandidateWithStatusToRound(@RequestBody CandidateToRoundDTO roundCandidateDTO,
			@RequestParam("destRoundId") String destRoundId, @RequestParam("sourceRoundId") String sourceRoundId,
			@RequestParam("cardIndex") String cardIndex, @RequestParam("roundCandidateDataId") List<Long> roundCandidateDataId) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.MoveCandidate.name());

		if (destRoundId == null || destRoundId.isEmpty())
			throw new RecruizWarnException(ErrorHandler.INVALID_MOVE, ErrorHandler.INVALID_REQUEST);

		Round destRound = roundService.findOne(Long.parseLong(destRoundId));
		Round sourceRound = roundService.findOne(Long.parseLong(sourceRoundId));

		if (destRound.getBoard().getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| destRound.getBoard().getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (destRound.getBoard().getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| destRound.getBoard().getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}
		// getting all round candidates by round id and sorting by card index
		List<RoundCandidate> destRoundCandidateList = roundCandidateService.getCandidateByRoundId(destRoundId);
		Collections.sort(destRoundCandidateList);

		double candidateCardIndex = Double.parseDouble(cardIndex);
		RoundCandidate movedCandidate = null;
		RoundCandidateDTO roundCandidateConvertedDTO = null;
		for (String email : roundCandidateDTO.getCandidateEmailList()) {

			Candidate candidate = candidateService.getCandidateByEmail(email);
			RoundCandidate existingCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate,
					sourceRoundId);

			if (existingCandidate == null || existingCandidate.getPositionCode() == null
					|| existingCandidate.getPositionCode().isEmpty() || existingCandidate.getRoundId() == null
					|| existingCandidate.getRoundId().isEmpty()) {
				return new RestResponse(RestResponse.FAILED, ErrorHandler.CANNOT_MOVE_CANDIDATE,
						ErrorHandler.MOVING_CANDIDATE_FAILED);
			}

			/**
			 * ###############################################################
			 * Commenting below code of closing active interview schedule as it
			 * is not applicable on new requirement of scheduling multiple
			 * interviews ####################################################
			 */

			/*
			 * if (!destRoundId.equalsIgnoreCase(sourceRoundId)) {
			 * InterviewSchedule existingSchedule =
			 * interviewScheduleService.getScheduleByPositionCodeRoundEmail(
			 * existingCandidate.getPositionCode(),
			 * existingCandidate.getRoundId(), email); if (existingSchedule !=
			 * null) { existingSchedule.setActive(false);
			 * interviewScheduleService.save(existingSchedule); } }
			 */

			existingCandidate.setRoundId(destRoundId);
			existingCandidate.setRound(destRound);
			existingCandidate.setStatus(BoardStatus.InProgress.toString());
			existingCandidate.setCardIndex(candidateCardIndex);

			/**
			 * ###############################################################
			 * Commenting below code of closing active interview schedule as it
			 * is not applicable on new requirement of scheduling multiple
			 * interviews ####################################################
			 */
			/*
			 * Set<Feedback> existingFeedback = existingCandidate.getFeedback();
			 * if (!destRoundId.equalsIgnoreCase(sourceRoundId)) { if
			 * (existingFeedback != null && !existingFeedback.isEmpty()) { for
			 * (Feedback feedback : existingFeedback) {
			 * feedback.setActive(false); } } }
			 */

			movedCandidate = roundCandidateService.save(existingCandidate);

			if(userService.getUserByEmail(existingCandidate.getSourcedBy()).getUserRole().getRoleName().equalsIgnoreCase("vendor") 
					&& !userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())){
				String roundCandidateName = existingCandidate.getCandidate().getFullName();
				String roundCandidateSouredBy = userService.getUserByEmail(existingCandidate.getSourcedBy()).getName();
				String SouredByEmail = userService.getUserByEmail(existingCandidate.getSourcedBy()).getEmail();

				emailActivityService.sendMailToVendorForChangesInCandidateStage(sourceRound.getRoundName(),destRound.getRoundName(),roundCandidateSouredBy,roundCandidateName,SouredByEmail);
			}

			
			PositionCandidateData poData = null;

			if(roundCandidateDataId!=null && roundCandidateDataId.size()>0)
				poData = positionCandidateDataRepository.findOne(roundCandidateDataId.get(0));
			if(poData!=null){
				poData.setFromStage(sourceRound.getRoundName());
				poData.setToStage(destRound.getRoundName());
				poData.setToStatus(BoardStatus.InProgress.toString());

				positionCandidateDataRepository.save(poData);
			}

			roundCandidateConvertedDTO = dataModelToDTOConversionService.convertRoundCandidate(movedCandidate,
					destRound);

			//Assign owner if null
			if(candidate.getOwner() == null || candidate.getOwner().equalsIgnoreCase("")) {
				candidate.setOwner(userService.getLoggedInUserEmail());
			}

			// making a entry to candidate activity
			candidateActivityService.movedToRoundEvent(destRound, sourceRound, candidate, existingCandidate);
		}

		recruizConnectService.moveCandidateToRound(sourceRound.getConnectId(), destRound.getConnectId(),
				Double.parseDouble(cardIndex), roundCandidateDTO.getCandidateEmailList());

		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, roundCandidateConvertedDTO);
		return addRoundResponse;
	}


	/**
	 * API Used to delete round of board.
	 * 
	 * @param roundId
	 * @param boardId
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/board/{boardId}/round", method = RequestMethod.DELETE)
	public RestResponse deleteRound(@PathVariable("boardId") String boardId,
			@RequestParam("roundIdList") List<String> roundIds) throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteRound.name());

		Board bord = boardService.findOne(Long.parseLong(boardId));

		if (bord.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (bord.getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		for (String roundId : roundIds) {
			roundService.deleteRound(Long.parseLong(roundId));
		}
		roundService.reOrderRounds(Long.parseLong(boardId));
		RestResponse addRoundResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.ROUND_DELETED,
				null);


		return addRoundResponse;
	}

	/**
	 * API used to update all rounds.
	 * 
	 * @param boardId
	 * @param roundId
	 * @param orderNo
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/board/{boardId}/round", method = RequestMethod.PUT)
	public RestResponse saveRound(@RequestBody List<RoundDTO> roundDTO, @PathVariable("boardId") String boardId)
			throws RecruizException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.SaveRound.name());

		Board bord = boardService.findOne(Long.parseLong(boardId));
		Position position = positionService.findOne(Long.parseLong(boardId));
		if (bord.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (bord.getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		roundService.saveRound(roundDTO, boardId);
		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}
		RestResponse roundResponse = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.ROUND_SAVED);
		return roundResponse;
	}

	/**
	 * API Used to delete candidate from round of board.
	 * 
	 * @param roundCandidateDTO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/round/candidate", method = RequestMethod.DELETE)
	public RestResponse deleteCandidateFromRound(@RequestBody CandidateToRoundDTO roundCandidateDTO) throws Exception {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
				userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
				UsageActionType.DeleteCandidateFromRound.name());

		Board bord = positionService.getPositionBoard(roundCandidateDTO.getPositionCode());
		Position position = positionService.getPositionByCode(roundCandidateDTO.getPositionCode());
		if (bord.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (bord.getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| bord.getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		List<String> emailList = roundCandidateDTO.getCandidateEmailList();
		for (String email : emailList) {
			roundService.deleteCandidateFromRound(roundCandidateDTO.getPositionCode(), email);
			Candidate candidate = candidateService.getCandidateByEmail(email);

			// making entry to candidate activity
			candidateActivityService.removedFromBoard(roundCandidateDTO, candidate);
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.CANDIDATE_DELETED, null);
		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}
		return response;
	}

	/**
	 * to get lsit of round id and round name map
	 * 
	 * @param positionCode
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/api/v1/round/id/map", method = RequestMethod.GET)
	public RestResponse getRoundIdNameMap(@RequestParam String positionCode) throws Exception {

		Map<String, String> roundIdNameMap = roundService.getRoundsByPositionCode(positionCode);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, roundIdNameMap, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/round/custom/add", method = RequestMethod.POST)
	public RestResponse addCustomRounds(@RequestBody List<CustomRounds> customRounds) throws Exception {
		List<CustomRounds> addedCustomRounds = customRoundService.save(customRounds);
		RestResponse response = new RestResponse(RestResponse.SUCCESS, addedCustomRounds, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/round/custom/delete/{id}", method = RequestMethod.DELETE)
	public RestResponse deleteCustomRounds(@PathVariable Long id) throws Exception {
		customRoundService.delete(id);
		List<CustomRounds> addedCustomRounds = customRoundService.findAll();
		RestResponse response = new RestResponse(RestResponse.SUCCESS, addedCustomRounds, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/round/custom/edit/{id}", method = RequestMethod.PUT)
	public RestResponse editCustomRound(@PathVariable Long id,@RequestBody CustomRounds customRound) throws Exception {

		customRound.setId(id);
		customRound = customRoundService.save(customRound);

		//	List<CustomRounds> addedCustomRounds = customRoundService.findAll();
		RestResponse response = new RestResponse(RestResponse.SUCCESS, customRound, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/round/custom/all", method = RequestMethod.GET)
	public RestResponse getAllCustomRounds() throws Exception {
		List<CustomRounds> addedCustomRounds = customRoundService.getAllRounds();
		RestResponse response = new RestResponse(RestResponse.SUCCESS, addedCustomRounds, null);
		return response;
	}

}
