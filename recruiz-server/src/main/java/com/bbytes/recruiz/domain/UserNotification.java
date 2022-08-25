package com.bbytes.recruiz.domain;

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
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "user_notification")
@EntityListeners({ AbstractEntityListener.class })
public class UserNotification extends AbstractEntity {
	
	private static final long serialVersionUID = -991935115273119911L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String notification_title;

	@Column(length=1000)
	private String details;

	private String userBy;

	private String userFor;

	private String forDate;

}
