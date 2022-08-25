package com.bbytes.recruiz.domain.integration;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import org.joda.time.DateTime;

import com.bbytes.recruiz.domain.AbstractEntity;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This Domain is used to have resume view information
 * 
 * @author akshay
 *
 */

@Data
@NoArgsConstructor
@Entity(name = "sixth_sense_resume_view")
@EqualsAndHashCode(callSuper = false)
@EntityListeners({ AbstractEntityListener.class })
@ToString
public class SixthSenseResumeView extends AbstractEntity {

	private static final long serialVersionUID = -3776845265055238374L;

	@Id
	@Column(name = "resume_id", nullable = false)
	private String resumeId;

	@Column(name = "view_on_date", nullable = false)
	private Date viewOnDate = DateTime.now().toDate();
	
	@Column(name = "source", nullable = false)
	private String source;
}
