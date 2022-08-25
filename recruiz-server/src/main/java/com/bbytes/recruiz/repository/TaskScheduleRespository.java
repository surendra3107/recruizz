package com.bbytes.recruiz.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.TaskSchedule;


public interface TaskScheduleRespository extends JpaRepository<TaskSchedule, Long> {

	List<TaskSchedule> findByTaskCreaterEmail(String createrEmail);

	List<TaskSchedule> findByStartAtBetweenAndActive(Date startAt,Date endAt,Boolean status);
	
}
