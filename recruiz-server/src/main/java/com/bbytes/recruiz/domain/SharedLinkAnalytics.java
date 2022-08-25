package com.bbytes.recruiz.domain;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity
@EntityListeners({ AbstractEntityListener.class })
public class SharedLinkAnalytics extends AbstractEntity {

	private static final long serialVersionUID = -5502156797959922118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String positionCode;

	private String platform;

	private String eventType;

	private String linkFrom;

}
