package com.bbytes.recruiz.domain.integration;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.joda.time.DateTime;

import com.bbytes.recruiz.domain.AbstractEntity;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This Domain is used to have user usage information
 * 
 * @author akshay
 *
 */

@Data
@NoArgsConstructor
@Entity(name = "sixth_sense_user_usage")
@EqualsAndHashCode(callSuper = false)
@EntityListeners({ AbstractEntityListener.class })
@ToString
public class SixthSenseUserUsage extends AbstractEntity {

	private static final long serialVersionUID = -3776845265055238374L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "usage_type")
	private String usageType;

	@Column(name = "view_count", nullable = false)
	private int viewCount = 0;

	@Column(name = "date_time", nullable = false)
	private Date dateTime = DateTime.now().toDate();
}
