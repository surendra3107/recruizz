package com.bbytes.recruiz.domain;

import java.util.LinkedList;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScheduleInterviewResponse {

	
	LinkedList<InterviewSchedule> schedules;
	
	boolean interviewerData=true;
}
