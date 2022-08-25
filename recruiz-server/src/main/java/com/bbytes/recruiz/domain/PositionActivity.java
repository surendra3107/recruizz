package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity(name = "position_activity")
@EntityListeners({ AbstractEntityListener.class })
public class PositionActivity extends AbstractEntity {

	private static final long serialVersionUID = 1922193407350206903L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String actionByEmal;

	@Column
	private String actionByName;

	@Column
	private String activityType;

	@Column(length = 1000)
	private String message;

	@Column
	private Date time;

	@Column
	private String positionCode;

	@Column
	private Long clientId;

	@Column
	private Long candidateId;

	@Column
	private Long roundId;

	@Column
	private Long requestedPositionId;

	// to identify candidate in board
	@Column
	private Long roundCandidateId;

	@Column
	private Long interviewScheduleId;

	@Column
	private Long teamId;

	@Column
	private String teamName;

	@Column
	private String offerApprovalId;
	
	@Column
	private String knowlarityCallDetailId;

	@Column
	private String ivr_integration;
	
	/**
	 * 
	 * @param user
	 * @param actionByEmail
	 * @param ActionByName
	 * @param notificationEventType
	 * @param message
	 * @param time
	 */
	public PositionActivity(String actionByEmail, String actionByName, String activityType, String message, Date time,
			String positionCode,Team team) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.positionCode = positionCode;
		if(null != team) {
		    this.teamId = team.getId();    
		    this.teamName = team.getTeamName();
		}
		
	}

	public PositionActivity(String actionByEmail, String actionByName, String activityType, String message, Date time,
			String positionCode, long clientId, long candidateId, long requestedPositionid,Team team) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		
		if(null != team) {
		    this.teamId = team.getId();    
		    this.teamName = team.getTeamName();
		}
	}

	/**
	 * 
	 * @param user
	 * @param actionByEmail
	 * @param ActionByName
	 * @param notificationEventType
	 * @param message
	 * @param time
	 * @param positionCode
	 * @param clientId
	 * @param candidateId
	 * @param requestedPositionid
	 * @param newUserEmail
	 * @param roundCandidateId
	 * @param roundId
	 */
	public PositionActivity(String actionByEmail, String actionByName, String activityType, String message, Date time,
			String positionCode, long clientId, long candidateId, long requestedPositionid, long roundCandidateId,
			long roundId, Team team) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		this.roundCandidateId = roundCandidateId;
		this.roundId = roundId;
		
		if(null != team) {
		    this.teamId = team.getId();    
		    this.teamName = team.getTeamName();
		}
	}

	/**
	 * 
	 * @param user
	 * @param actionByEmail
	 * @param ActionByName
	 * @param notificationEventType
	 * @param message
	 * @param time
	 * @param positionCode
	 * @param clientId
	 * @param candidateId
	 * @param requestedPositionid
	 * @param newUserEmail
	 * @param roundCandidateId
	 * @param roundId
	 * @param scheduleId
	 */
	public PositionActivity(String actionByEmail, String actionByName, String activityType, String message, Date time,
			String positionCode, long clientId, long candidateId, long requestedPositionid, long roundCandidateId,
			long roundId, long scheduleId,Team team) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		this.roundCandidateId = roundCandidateId;
		this.roundId = roundId;
		this.interviewScheduleId = scheduleId;
		
		if(null != team) {
		    this.teamId = team.getId();    
		    this.teamName = team.getTeamName();
		}
	}
}
