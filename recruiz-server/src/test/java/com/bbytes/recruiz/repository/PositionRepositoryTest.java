package com.bbytes.recruiz.repository;

import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

public class PositionRepositoryTest extends RecruizBaseApplicationTests {

	@Autowired
	PositionRepository positionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ClientDecisionMakerRepository clientDecisionMakerRepo;

	@Autowired
	ClientRepository clientRepo;

	@Autowired
	UserRolesRepository userRolesRepository;

	private String tenantName = "acme";

	@Before
	public void init() {
		TenantContextHolder.setTenant(tenantName);
	}

	@Test
	public void addPosition() throws ParseException {

		Set<String> goodSkillSet = new HashSet<>();
		goodSkillSet.add("JS");
		goodSkillSet.add("Node");
		goodSkillSet.add("Spring");

		Set<ClientDecisionMaker> clientDecisionMakerList = new LinkedHashSet<ClientDecisionMaker>();
		clientDecisionMakerList.addAll(clientDecisionMakerRepo.findAll());

		Position position = new Position();
		position.setTitle("JS Developer");
		position.setLocation("Bangalore");
		position.setTotalPosition(5);
		position.setOpenedDate(DateTimeUtils.getDateFromString("23-Apr-2016"));
		position.setCloseByDate(DateTimeUtils.getDateFromString("04-May-2016"));
		position.setPositionCode("SW-12");
		position.setPositionUrl("www.bb.in/careers/JS");
		position.setGoodSkillSet(goodSkillSet);
		position.setReqSkillSet(goodSkillSet);
		position.setType("Payroll");
		position.setRemoteWork(true);
		position.setMaxSal(50000);
		position.setNotes("No Additional Note");
		position.setStatus(Status.Active.getDisplayName());
		position.setHrExecutives(userRepository.findByUserRole(userRolesRepository.findOneByRoleName("HR")));
		position.setClient(clientRepo.findOne((long) 1));
		position.setDecisionMakers(clientDecisionMakerList);
		Board board = new Board();
		board.setStatus(true);
		// board.setPosition(position);

		// Default round for board
		Round rounds = new Round();
		rounds.setBoard(board);
		rounds.setRoundType("Short List");
		rounds.setOrderNo(1);

		Round round2 = new Round();
		round2.setBoard(board);
		round2.setRoundType("Techncal");
		round2.setOrderNo(2);
		board.getRounds().add(rounds);
		board.getRounds().add(round2);
		position.setBoard(board);

		positionRepository.saveAndFlush(position);
	}

	@Transactional
	@Test
	public void fetchAllPosition() {

		List<Position> position = positionRepository.findAll();
		for (Position pos : position) {
			System.out.println(" \t" + pos.getTitle() + "\t" + pos.getLocation() + "\t" + pos.getTotalPosition() + "\t"
					+ pos.getPositionCode() + "\t" + pos.getType() + "\t" + pos.getBoard().getId() + "\t"
					+ pos.getClient().getClientName());
			System.out.println("\t\t Hr Executives");
			Set<User> hrs = pos.getHrExecutives();
			for (User hr : hrs) {
				System.out.print("\t\t" + hr.getName());
			}
			System.out.println("\n\t\t Decision Makers");
			Set<ClientDecisionMaker> dms = pos.getDecisionMakers();
			for (ClientDecisionMaker dm : dms) {
				System.out.print("\t\t" + dm.getName());
			}
			System.out.println("\n");
		}
	}
}
