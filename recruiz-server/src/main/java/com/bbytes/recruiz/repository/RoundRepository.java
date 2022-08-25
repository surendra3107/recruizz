package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Round;

//@JaversSpringDataAuditable
public interface RoundRepository extends JpaRepository<Round, Long> {

	Round findOneByRoundNameAndBoard(String roundType, Board board);

	@Query("select max(orderNo) from rounds where board = ?")
	Integer getMaxOrderNo(Board board);

	List<Round> findByBoardOrderByOrderNoAsc(Board board);

	Round findByBoardAndRoundType(Board board, String type);
	
	List<Round> findByBoardAndRoundName(Board board, String name);

	Round findByConnectId(String connectId);
}
