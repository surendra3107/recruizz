package com.bbytes.recruiz.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.LazyInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class BoardRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	BoardRepository boardRepository;

	@Autowired
	RoundRepository roundRepo;

	private String tenantName = "dummy";

	@Before
	public void setup() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addBoard() {
		Board board = new Board();
		board.setStatus(true);
		Round round1 = new Round();
		round1.setOrderNo(1);
		round1.setRoundType("Short List");
		round1.setBoard(board);
		Round round2 = new Round();
		round2.setOrderNo(2);
		round2.setRoundType("Techincal");
		round2.setBoard(board);

		board.getRounds().add(round1);
		board.getRounds().add(round2);
		boardRepository.saveAndFlush(board);
	}

	@Test
	@Transactional
	public void fetchAllBoard() {
		List<Board> boards = boardRepository.findAll();
		for (Board board : boards) {
			for (Round round : board.getRounds()) {
				System.out.print("\t" + round.getRoundType() + " cloumn : " + round.getOrderNo());
			}
		}
	}

	@Test(expected = LazyInitializationException.class)
	public void updateBoard() {
		List<Board> boards = boardRepository.findAll();
		for (Board board : boards) {
			Round round = new Round();
			round.setOrderNo(3);
			round.setRoundType("Tech 2");
			round.setBoard(board);
			roundRepo.save(round);
			board.getRounds().add(round);
			boardRepository.saveAndFlush(board);
		}
	}

	@Test
	public void deleteBoard() {
		boardRepository.deleteAll();
	}

}
