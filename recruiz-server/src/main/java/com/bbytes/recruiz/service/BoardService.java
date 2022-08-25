package com.bbytes.recruiz.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.ExpectedCTCRange;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.InactiveSinceRange;
import com.bbytes.recruiz.enums.NoticePeriodRange;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.BoardRepository;
import com.bbytes.recruiz.rest.dto.models.BoardDTO;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RoundResponseDTO;
import com.bbytes.recruiz.utils.DateUtil;

@Service
public class BoardService extends AbstractService<Board, Long> {

	private BoardRepository boardRepository;

	@Autowired
	private PositionService positionServices;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private FeedbackService feeedbackService;

	@Autowired
	private UserService userService;

	@Autowired
	OfferLetterApprovalsService offerLetterApprovalsService; 

	@Autowired
	CandidateFileService candidateFileService;

	@Autowired
	OrganizationService organizationService;

	@Autowired
	public BoardService(BoardRepository boardRepository) {
		super(boardRepository);
		this.boardRepository = boardRepository;
	}

	public Board getBoardByID(long id) {
		return boardRepository.findBoardById(id);
	}


	public BoardDTO getBoard(String positionCode, String status, String sourcedBy, Date startDate, Date endDate)
			throws RecruizException {
		//String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());		
		//System.out.println("Inside Get Board API - Time: " +timeStamp);

		BoardDTO boardDTO = new BoardDTO();
		RoundResponseDTO roundDTO;
		RoundCandidateDTO candidateDTO;
		Board positionBoard = positionServices.getPositionBoard(positionCode);
		boardDTO.setBoardId(positionBoard.getId() + "");
		boardDTO.setCreatedDate(positionBoard.getCreationDate());
		boardDTO.setPositionName(positionServices.getPositionByCode(positionCode).getTitle());
		boardDTO.setClientName(positionServices.getPositionByCode(positionCode).getClient().getClientName());
		boardDTO.setPositionStatus(positionBoard.getPositionStatus());
		boardDTO.setClientStatus(positionBoard.getClientStatus());
		boardDTO.setPositionCode(positionCode);
		boardDTO.setPositionId(positionServices.getPositionByCode(positionCode).getId() + "");
		boardDTO.setClientId(positionServices.getPositionByCode(positionCode).getClient().getId() + "");

		for (Round round : positionBoard.getRounds()) {
			roundDTO = new RoundResponseDTO();
			roundDTO.setRoundId(round.getId() + "");
			roundDTO.setName(round.getRoundName());
			roundDTO.setType(round.getRoundType());
			roundDTO.setOrderNo(round.getOrderNo());
			Set<RoundCandidate> roundCandidateList = new HashSet<RoundCandidate>();

			// this condition only when user routes from performance report and
			// view board per user per position
			if (sourcedBy != null && !sourcedBy.isEmpty() && startDate != null && endDate != null) {

				if (status != null && !status.isEmpty())
					roundCandidateList = roundCandidateService
					.findByModificationDateBetweenAndRoundIdAndSourcedByAndStatus(startDate, endDate,
							round.getId() + "", sourcedBy, status);
				else
					roundCandidateList = roundCandidateService.findByModificationDateBetweenAndRoundIdAndSourcedBy(
							startDate, endDate, round.getId() + "", sourcedBy);
			} else {
				// checking user has view all candidate permission
				if (checkPermissionService.isSuperAdmin() || checkPermissionService.hasViewAllCandidatesPermission()
						|| checkPermissionService.isDeptHead()) {
					roundCandidateList = round.getCandidates();
				} else {
					roundCandidateList = roundCandidateService.getCandidatebySourcedBy(round.getId() + "",
							userService.getLoggedInUserEmail());
				}
			}

			if (roundCandidateList != null && !roundCandidateList.isEmpty()) {
				//String timeStamp1 = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());		
				//System.out.println("Getting Round Candidates Start " +timeStamp1);

				double index = 1;
				for (RoundCandidate roundCandidate : roundCandidateList) {
					Candidate candidate = roundCandidate.getCandidate();

					candidateDTO = new RoundCandidateDTO();
					candidateDTO.setRoundCandidateId(roundCandidate.getId() + "");
					candidateDTO.setEmail(candidate.getEmail());
					candidateDTO.setId(candidate.getCid() + "");
					candidateDTO.setLocation(candidate.getCurrentLocation());
					candidateDTO.setName(candidate.getFullName());
					candidateDTO.setMobile(candidate.getMobile());
					candidateDTO.setTotalExpRange(getExperinceRange(candidate.getTotalExp()));
					candidateDTO.setTotalExperience(candidate.getTotalExp());
					candidateDTO.setExpectedCtcRange(getExpectedCtcRange(candidate.getExpectedCtc()));
					candidateDTO.setNoticePeriodRange(
							getNoticePeriodRange(candidate.getNoticePeriod(), candidate.isNoticeStatus()));
					candidateDTO.setSourcedFrom(candidate.getSource());
					candidateDTO.setEmploymentType(candidate.getEmploymentType());
					candidateDTO.setInactiveSinceRange(getIdleSinceRange(roundCandidate.getModificationDate()));
					candidateDTO.setSourceBy(roundCandidate.getSourcedBy());
					candidateDTO.setResumeLink(candidate.getResumeLink());
					candidateDTO.setStatus(roundCandidate.getStatus());


					if(candidate.getGeneratedOfferLetter()==null){
						candidateDTO.setGeneratedOfferLetter(false);
					}else{
						candidateDTO.setGeneratedOfferLetter(candidate.getGeneratedOfferLetter());
					}
					candidateDTO.setSourceFreshness(
							DateUtil.getDifferenceDateDay(roundCandidate.getModificationDate(), new Date()));

					// setting all candidate card index
					if (roundCandidate.getCardIndex() == null) {
						roundCandidate.setCardIndex(index);
						roundCandidateService.save(roundCandidate);
						index++;
					}
					candidateDTO.setCardIndex(roundCandidate.getCardIndex());
					// adding source information to card
					if (candidate.getSource()!=null && candidate.getSource().equalsIgnoreCase("Vendor")) {
						candidateDTO.setSourcedFrom(candidate.getSource() + " (" + candidate.getSourceName() + ")");
					}

/*					Organization org = organizationService.getOrgInfo();
					if (org != null && org.getDocumentsCheck()!=null && org.getDocumentsCheck().equalsIgnoreCase("Yes")){ 

						String docList = org.getMandatoryDocs();

						List<CandidateFile> files = candidateFileService.getCandidateFile(String.valueOf(candidate.getCid()));
						Set<String> canFiles = new HashSet<>();
						Set<String> listFiles = new HashSet<>();

						if(docList==null){
							listFiles.add(FileType.PAN_CARD.getDisplayName());listFiles.add(FileType.AADHAR_CARD.getDisplayName());listFiles.add(FileType.UPDATED_RESUME.getDisplayName());listFiles.add(FileType.TENTH_EDU_DOC.getDisplayName());
							listFiles.add(FileType.TWELETH_EDU_DOC.getDisplayName());listFiles.add(FileType.DEGREE_DOC.getDisplayName());listFiles.add(FileType.APPOINTMENT_LETTER.getDisplayName());listFiles.add(FileType.SALARY_SLIPS.getDisplayName());
							listFiles.add(FileType.RELIEVING_LETTER.getDisplayName());listFiles.add(FileType.PASSPORT_PHOTOGRAPH.getDisplayName());listFiles.add(FileType.STATEMENT_CHEQUE.getDisplayName());listFiles.add(FileType.ADDRESS_PROOF.getDisplayName());
						}else{
							if(docList.contains(",")){
								
								String[] docs = docList.split(",");
								listFiles.addAll(new HashSet<>(Arrays.asList(docs)));
							}else{
								listFiles.add(docList);
							}
						}
						for (CandidateFile file : files) {
							System.out.println(file.getFileType());
							for (String doc : listFiles) {
								if(file.getFileType().equalsIgnoreCase(doc.trim()))
									canFiles.add(doc);
							}
						}

						if(listFiles.size()>canFiles.size()){

							listFiles.removeAll(canFiles);
							candidateDTO.setPendingDocs(listFiles);
							candidateDTO.setMandatoryDocAvailable(false);

						}else{

							candidateDTO.setMandatoryDocAvailable(true);

						}


					}*/
					//This section is commented. Loading board was taking a lot of time and regular timeout was occuring.
					//Workaround is to remove the interview schedule query & the feedback query. Time reduced more than 70%

					//String roundCandidateId = roundCandidate.getId() + "";
					//String totalApproved = feeedbackService.getFeedbackCountByStatusAndCandidate("Approved",
					//		roundCandidateId);
					//candidateDTO.setTotalApproved(totalApproved);
					//candidateDTO.setTotalExpectedFeedback(
					//		feeedbackService.getTotalFeedbackCountByRoundCandidate(roundCandidateId));
					//candidateDTO.setTotalOnHold(
					//		feeedbackService.getFeedbackCountByStatusAndCandidate("OnHold", roundCandidateId));
					//candidateDTO.setTotalRejected(
					//		feeedbackService.getFeedbackCountByStatusAndCandidate("Rejected", roundCandidateId));
					//int totalFeedbackReceived = Integer.parseInt(candidateDTO.getTotalApproved())
					//		+ Integer.parseInt(candidateDTO.getTotalOnHold())
					//		+ Integer.parseInt(candidateDTO.getTotalRejected());
					//candidateDTO.setTotalFeedbackReceived(String.valueOf(totalFeedbackReceived));


					/*OfferLetterApprovals offerLetterApproval = offerLetterApprovalsService.getApprovalDetailsByPositionIdAndCandidateId(
							positionServices.getPositionByCode(positionCode).getId(),candidate.getCid());


					if(offerLetterApproval!=null  && offerLetterApproval.getApproval_status().equalsIgnoreCase(GlobalConstants.PENDING_STATUS)){
						candidateDTO.setOfferLetterRollout(true);
					}else{
						candidateDTO.setOfferLetterRollout(false);
					}*/

					roundDTO.getCandidateList().add(candidateDTO);
					candidateDTO.setActiveScheduleCount(interviewScheduleService
							.getAllScheduleCountByPositionCodeAndCandidateEmail(positionCode, candidate.getEmail()));

				}
			}

			//String timeStamp2 = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());		
			//System.out.println("Done Getting Round Candidates " +timeStamp2);

			doCandidateSort(roundDTO.getCandidateList());
			boardDTO.getRounds().add(roundDTO);

			//String timeStamp3 = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());		
			//System.out.println("Sorting Complete " +timeStamp3);
		}
		return boardDTO;
	}

	public String getExperinceRange(double totalExp) {

		if ((totalExp >= ExperinceRange.Zero_To_OneYear.getFromExp())
				&& (totalExp <= ExperinceRange.Zero_To_OneYear.getToExp())) {
			return ExperinceRange.Zero_To_OneYear.getDisplayName();
		} else if ((totalExp >= ExperinceRange.One_To_TwoYears.getFromExp())
				&& (totalExp <= ExperinceRange.One_To_TwoYears.getToExp())) {
			return ExperinceRange.One_To_TwoYears.getDisplayName();
		} else if ((totalExp >= ExperinceRange.Two_To_ThreeYears.getFromExp())
				&& (totalExp <= ExperinceRange.Two_To_ThreeYears.getToExp())) {
			return ExperinceRange.Two_To_ThreeYears.getDisplayName();
		} else if ((totalExp >= ExperinceRange.Three_To_FourYears.getFromExp())
				&& (totalExp <= ExperinceRange.Three_To_FourYears.getToExp())) {
			return ExperinceRange.Three_To_FourYears.getDisplayName();
		} else if ((totalExp >= ExperinceRange.Four_To_FiveYears.getFromExp())
				&& (totalExp <= ExperinceRange.Four_To_FiveYears.getToExp())) {
			return ExperinceRange.Four_To_FiveYears.getDisplayName();
		} else if ((totalExp >= ExperinceRange.Five_To_EightYears.getFromExp())
				&& (totalExp <= ExperinceRange.Five_To_EightYears.getToExp())) {
			return ExperinceRange.Five_To_EightYears.getDisplayName();
		} else if ((totalExp >= ExperinceRange.Eight_To_TenYears.getFromExp())
				&& (totalExp <= ExperinceRange.Eight_To_TenYears.getToExp())) {
			return ExperinceRange.Eight_To_TenYears.getDisplayName();
		} else {
			return ExperinceRange.Above10Years.getDisplayName();
		}
	}

	public String getNoticePeriodRange(double noticePeriod, boolean isServing) {

		if (isServing) {
			return NoticePeriodRange.CurrentlyServing.getDisplayName();
		} else {
			if ((noticePeriod > NoticePeriodRange.Zero_To_TenDays.getDaysFrom())
					&& (noticePeriod <= NoticePeriodRange.Zero_To_TenDays.getDaysTo())) {
				return NoticePeriodRange.Zero_To_TenDays.getDisplayName();
			} else if ((noticePeriod > NoticePeriodRange.Eleven_To_TwentyDays.getDaysFrom())
					&& (noticePeriod <= NoticePeriodRange.Eleven_To_TwentyDays.getDaysTo())) {
				return NoticePeriodRange.Eleven_To_TwentyDays.getDisplayName();
			} else if ((noticePeriod > NoticePeriodRange.TwentyOne_To_ThirtyDays.getDaysFrom())
					&& (noticePeriod <= NoticePeriodRange.TwentyOne_To_ThirtyDays.getDaysTo())) {
				return NoticePeriodRange.TwentyOne_To_ThirtyDays.getDisplayName();
			} else if ((noticePeriod > NoticePeriodRange.ThirtyOne_To_SixtyDays.getDaysFrom())
					&& (noticePeriod <= NoticePeriodRange.ThirtyOne_To_SixtyDays.getDaysTo())) {
				return NoticePeriodRange.ThirtyOne_To_SixtyDays.getDisplayName();
			} else if ((noticePeriod > NoticePeriodRange.SixtyOne_To_NinteyDays.getDaysFrom())
					&& (noticePeriod <= NoticePeriodRange.SixtyOne_To_NinteyDays.getDaysTo())) {
				return NoticePeriodRange.SixtyOne_To_NinteyDays.getDisplayName();
			} else {
				return NoticePeriodRange.Above90Days.getDisplayName();
			}
		}

	}

	public String getIdleSinceRange(Date modificationDate) {

		if (null == modificationDate) {
			return InactiveSinceRange.Zero_To_TenDays.getDisplayName();
		}

		long diffInMillies = Math.abs(new Date().getTime() - modificationDate.getTime());
		long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

		if ((diff < 10)) {
			return InactiveSinceRange.Zero_To_TenDays.getDisplayName();
		} else if ((diff > InactiveSinceRange.Eleven_To_TwentyDays.getDaysFrom())
				&& (diff <= InactiveSinceRange.Eleven_To_TwentyDays.getDaysTo())) {
			return InactiveSinceRange.Eleven_To_TwentyDays.getDisplayName();
		} else if ((diff > InactiveSinceRange.TwentyOne_To_ThirtyDays.getDaysFrom())
				&& (diff <= InactiveSinceRange.TwentyOne_To_ThirtyDays.getDaysTo())) {
			return InactiveSinceRange.TwentyOne_To_ThirtyDays.getDisplayName();
		} else if ((diff > InactiveSinceRange.ThirtyOne_To_SixtyDays.getDaysFrom())
				&& (diff <= InactiveSinceRange.ThirtyOne_To_SixtyDays.getDaysTo())) {
			return InactiveSinceRange.ThirtyOne_To_SixtyDays.getDisplayName();
		} else if ((diff > InactiveSinceRange.SixtyOne_To_NinteyDays.getDaysFrom())
				&& (diff <= InactiveSinceRange.SixtyOne_To_NinteyDays.getDaysTo())) {
			return InactiveSinceRange.SixtyOne_To_NinteyDays.getDisplayName();
		} else {
			return InactiveSinceRange.Above90Days.getDisplayName();
		}

	}

	public String getExpectedCtcRange(double expectedCTC) {

		if (expectedCTC < ExpectedCTCRange.Below_1_LPA.getFromCTC()) {
			return ExpectedCTCRange.Below_1_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.One_To_Two_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.One_To_Two_LPA.getToCTC())) {
			return ExpectedCTCRange.One_To_Two_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Two_To_Three_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Two_To_Three_LPA.getToCTC())) {
			return ExpectedCTCRange.Two_To_Three_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Three_To_Four_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Three_To_Four_LPA.getToCTC())) {
			return ExpectedCTCRange.Three_To_Four_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Four_To_Five_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Four_To_Five_LPA.getToCTC())) {
			return ExpectedCTCRange.Four_To_Five_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Five_To_Eight_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Five_To_Eight_LPA.getToCTC())) {
			return ExpectedCTCRange.Five_To_Eight_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Eight_To_Ten_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Eight_To_Ten_LPA.getToCTC())) {
			return ExpectedCTCRange.Eight_To_Ten_LPA.getDisplayName();
		} else if ((expectedCTC > ExpectedCTCRange.Ten_To_Fifteen_LPA.getFromCTC())
				&& (expectedCTC <= ExpectedCTCRange.Ten_To_Fifteen_LPA.getToCTC())) {
			return ExpectedCTCRange.Ten_To_Fifteen_LPA.getDisplayName();
		} else {
			return ExpectedCTCRange.Above_Fifteen_LPA.getDisplayName();
		}
	}

	/**
	 * this method will return list of candidates, if the candidate is already
	 * part of that board then they won't be returned. The list of candidate
	 * will have only unique candidate who are not part of the board.
	 * 
	 * @param boardId
	 * @param searchQuery
	 * @return
	 * @throws RecruizException
	 */

	public List<Candidate> getCandidateToSource(String boardId, String searchQuery) throws RecruizException {
		List<Candidate> candidates = new LinkedList<Candidate>();
		Board board = boardRepository.findBoardById(Long.parseLong(boardId));
		Set<Round> rounds = board.getRounds();
		Set<Long> existingCid = new HashSet<Long>();
		if (rounds != null && !rounds.isEmpty()) {
			for (Round round : rounds) {
				Set<RoundCandidate> existingCandidateList = round.getCandidates();
				if (existingCandidateList != null && !existingCandidateList.isEmpty())
					for (RoundCandidate roundCandidate : existingCandidateList) {
						existingCid.add(roundCandidate.getCandidate().getCid());
					}
			}
			candidates = candidateService.getCandidatesToBoard(searchQuery, existingCid);
		} else {
			candidates = candidateService.getCandidatesToBoard(searchQuery, existingCid);
		}

		// doing sorting here
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate c1, Candidate c2) {
				int res = String.CASE_INSENSITIVE_ORDER.compare(c1.getFullName(), c2.getFullName());
				if (res == 0) {
					res = c1.getFullName().compareTo(c2.getFullName());
				}
				return res;
			}
		});

		return candidates;
	}

	/**
	 * Sorting board candidate list based on card index
	 * 
	 * @param candidateList
	 */
	public void doCandidateSort(List<RoundCandidateDTO> candidateList) {
		Collections.sort(candidateList, new Comparator<RoundCandidateDTO>() {
			@Override
			public int compare(RoundCandidateDTO candidateDTO1, RoundCandidateDTO candidateDTO2) {
				int res = Double.compare(candidateDTO1.getCardIndex(), candidateDTO2.getCardIndex());
				if (res == 0) {
					Double cardIndex1 = candidateDTO1.getCardIndex();
					Double cardIndex2 = candidateDTO2.getCardIndex();
					res = cardIndex1.compareTo(cardIndex2);
				}
				return res;
			}
		});
	}

}
