package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity(name = "notification")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class Notification extends AbstractEntity {

	private static final long serialVersionUID = -1914458191824833988L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String user;

	@Column
	private String actionByEmal;

	@Column
	private String actionByName;

	@Column
	private String notificationEventType;

	@Column(length = 1000)
	private String message;

	@Column
	private Date time;

	@Column
	private Boolean readState = false;

	@Column
	private Boolean viewState = false;

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

	// if decided to notify on user added (on joined)
	@Column
	private String newUserEmail;
	
	@Column
	private Long interviewScheduleId;

	/**
	 * 
	 * @param user
	 * @param actionByEmail
	 * @param ActionByName
	 * @param notificationEventType
	 * @param message
	 * @param time
	 */
	public Notification(String user, String actionByEmail, String ActionByName, String notificationEventType,
			String message, Date time) {
		this.actionByEmal = actionByEmail;
		this.user = user;
		this.actionByEmal = actionByEmail;
		this.message = message;
		this.notificationEventType = notificationEventType;
		this.time = time;
		this.readState = false;
		this.viewState = false;
	}

	public Notification(String user, String actionByEmail, String ActionByName, String notificationEventType,
			String message, Date time, String positionCode, long clientId, long candidateId, long requestedPositionid,
			String newUserEmail) {
		this.actionByEmal = actionByEmail;
		this.user = user;
		this.actionByEmal = actionByEmail;
		this.message = message;
		this.notificationEventType = notificationEventType;
		this.time = time;
		this.readState = false;
		this.viewState = false;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		this.newUserEmail = newUserEmail;
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
	public Notification(String user, String actionByEmail, String ActionByName, String notificationEventType,
			String message, Date time, String positionCode, long clientId, long candidateId, long requestedPositionid,
			String newUserEmail, long roundCandidateId, long roundId) {
		this.actionByEmal = actionByEmail;
		this.user = user;
		this.actionByEmal = actionByEmail;
		this.message = message;
		this.notificationEventType = notificationEventType;
		this.time = time;
		this.readState = false;
		this.viewState = false;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		this.newUserEmail = newUserEmail;
		this.roundCandidateId = roundCandidateId;
		this.roundId = roundId;
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
	public Notification(String user, String actionByEmail, String ActionByName, String notificationEventType,
			String message, Date time, String positionCode, long clientId, long candidateId, long requestedPositionid,
			String newUserEmail, long roundCandidateId, long roundId,long scheduleId) {
		this.actionByEmal = actionByEmail;
		this.user = user;
		this.actionByEmal = actionByEmail;
		this.message = message;
		this.notificationEventType = notificationEventType;
		this.time = time;
		this.readState = false;
		this.viewState = false;
		this.positionCode = positionCode;
		this.clientId = clientId;
		this.candidateId = candidateId;
		this.requestedPositionId = requestedPositionid;
		this.newUserEmail = newUserEmail;
		this.roundCandidateId = roundCandidateId;
		this.roundId = roundId;
		this.interviewScheduleId=scheduleId;
	}
}
