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
@Entity(name = "client_activity")
@EntityListeners({ AbstractEntityListener.class })
public class ClientActivity extends AbstractEntity {

	private static final long serialVersionUID = -7160900260784830724L;

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

	/**
	 * 
	 * @param user
	 * @param actionByEmail
	 * @param ActionByName
	 * @param notificationEventType
	 * @param message
	 * @param time
	 */
	public ClientActivity(String user, String actionByEmail, String actionByName, String activityType, String message,
			Date time,Long clientId) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.clientId = clientId;
	}

	public ClientActivity(String user, String actionByEmail, String actionByName, String activityType, String message,
			Date time, String positionCode, long clientId) {
		this.actionByEmal = actionByEmail;
		this.actionByName = actionByName;
		this.message = message;
		this.activityType = activityType;
		this.time = time;
		this.positionCode = positionCode;
		this.clientId = clientId;
	}
}
