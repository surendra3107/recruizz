package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.CustomRounds;

//@JaversSpringDataAuditable
public interface RoundCustomRepository extends JpaRepository<CustomRounds, Long> {

//    List<CustomRounds> findAllOrderByOrderNoAsc();

}
