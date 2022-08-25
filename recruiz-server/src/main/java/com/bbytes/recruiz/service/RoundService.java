package com.bbytes.recruiz.service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.RoundRepository;
import com.bbytes.recruiz.rest.dto.models.RoundDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class RoundService extends AbstractService<Round, Long> {

	private RoundRepository roundRepository;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private InterviewScheduleService scheduleService;

	@Autowired
	private CalendarService calendarService;

	@Autowired
	public RoundService(RoundRepository roundRepository) {
		super(roundRepository);
		this.roundRepository = roundRepository;
	}

	@Transactional
	public boolean isRoundExist(String roundName, Board board) {
		boolean state = roundRepository.findOneByRoundNameAndBoard(roundName, board) == null ? false : true;
		return state;
	}

	@Transactional
	public void changeCandidateStatus(Round round) {
		save(round);
	}

	@Transactional
	public Round getRoundByRoundTypeAndBoard(String type, Board board) {
		return roundRepository.findOneByRoundNameAndBoard(type, board);
	}

	@Transactional
	public Round findByConnectId(String connectId) {
		return roundRepository.findByConnectId(connectId);
	}

	@Transactional
	public void saveRound(List<RoundDTO> roundDTO, String boardId) throws RecruizException {

		Board board = boardService.getBoardByID(Long.parseLong(boardId));
		int index = 1;
		for (RoundDTO round : roundDTO) {
			if (round.getRoundId() != null) {
				Round roundFromDB = roundRepository.findOne(Long.parseLong(round.getRoundId()));
				roundFromDB.setBoard(board);
				roundFromDB.setOrderNo(index);
				roundFromDB.setRoundName(round.getRoundName());
				// roundFromDB.setRoundType(round.getRoundType());
				save(roundFromDB);

			} else {
				Round updateRound = new Round();
				updateRound.setBoard(board);
				updateRound.setOrderNo(index);
				updateRound.setRoundName(round.getRoundName());
				// updateRound.setRoundType(round.getRoundType());
				save(updateRound);
			}

			index++;
		}

	}

	@Transactional
	public void deleteRound(long id) throws RecruizException {
		Round round = roundRepository.findOne(id);

		if (round == null)
			throw new RecruizWarnException(ErrorHandler.ROUND_NOT_PRESENT, ErrorHandler.INVALID_REQUEST);

		Set<RoundCandidate> candidates = round.getCandidates();
		if (candidates != null && !candidates.isEmpty())
			roundCandidateService.delete(candidates);

		if (round.getRoundType() != null)
			if (round.getRoundType().equalsIgnoreCase(GlobalConstants.ROUND_DEFAULT_TYPE_SOURCE))
				throw new RecruizException(ErrorHandler.ROUND_NOT_DELETABLE, ErrorHandler.INVALID_REQUEST);

		roundRepository.delete(round);
	}

	@Transactional
	public void reOrderRounds(long boardId) {
		Board board = boardService.findOne(boardId);

		Set<Round> rounds = new LinkedHashSet<Round>();
		rounds.addAll(board.getRounds());

		int index = 1;
		for (Round round : rounds) {
			round.setOrderNo(index);
			index++;
		}

		board.setRounds(rounds);
		boardService.save(board);
	}

	/**
	 * this will delete and cancel all scheduled interview for that candidate
	 * 
	 * @param positionCode
	 * @param candidateEmail
	 * @throws Exception
	 */
	@Transactional
	public void deleteCandidateFromRound(String positionCode, String candidateEmail) throws Exception {
		// Round round = roundRepo.findOne(Long.parseLong(roundId));
		Candidate candidate = candidateService.getCandidateByEmail(candidateEmail);
		Board board = positionService.getPositionBoard(positionCode);
		if (board != null) {
			List<Round> boardRounds = roundRepository.findByBoardOrderByOrderNoAsc(board);
			if (boardRounds != null) {
				for (Round round : boardRounds) {
					RoundCandidate roundCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate,
							round.getId() + "");
					if (roundCandidate != null) {
						InterviewSchedule scheduledInterview = scheduleService.getScheduleByPositionCodeRoundEmail(
								positionCode, roundCandidate.getRound().getId() + "",
								roundCandidate.getCandidate().getEmail());
						if (scheduledInterview != null) {
							calendarService.cancelInterviewerInvite(scheduledInterview.getId());
						}
						roundCandidateService.delete(roundCandidate);
					}
				}
			}
		}
	}

	@Transactional
	public String getRoundName(String id) {
		Round round = roundRepository.findOne(Long.parseLong(id));
		if (round != null) {
			return round.getRoundName();
		}
		return "";
	}

	@Transactional(readOnly = true)
	public Round getRoundByBoardAndType(Board board, String type) {
		return roundRepository.findByBoardAndRoundType(board, type);
	}
	
	@Transactional(readOnly = true)
	public Round getRoundByBoardAndName(Board board, String name) {
		List<Round> rounds = roundRepository.findByBoardAndRoundName(board, name);
		if(null != rounds && !rounds.isEmpty()) {
		    return rounds.get(0);
		}
		return null;
	}


	@Transactional(readOnly = true)
	public Map<String, String> getRoundsByPositionCode(String positionCode) throws RecruizException {
		Map<String, String> roundIdNameMap = new HashMap<>();
		Board board = positionService.getPositionBoard(positionCode);
		List<Round> rounds = roundRepository.findByBoardOrderByOrderNoAsc(board);
		if (rounds != null && !rounds.isEmpty()) {
			for (Round round : rounds) {
				roundIdNameMap.put(round.getId() + "", round.getRoundName());
			}
		}
		return roundIdNameMap;
	}
	
	@Transactional(readOnly = true)
	public List<Round> getRoundsByBoardPositionCode(Board board) throws RecruizException {
		List<Round> rounds = roundRepository.findByBoardOrderByOrderNoAsc(board);
		return rounds;
	}

}
