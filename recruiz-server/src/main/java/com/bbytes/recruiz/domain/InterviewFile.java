package com.bbytes.recruiz.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Organization
 * 
 * @author souravkumar
 *
 */

@Data
@ToString(exclude = { "file", "schedule", "taskSchedule" })
@EqualsAndHashCode(callSuper = false, exclude = { "file", "schedule", "taskSchedule" })
@NoArgsConstructor
@Entity(name = "interview_file")
public class InterviewFile extends AbstractEntity {

	private static final long serialVersionUID = 986105275339999461L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Lob
	private byte[] file;

	private String fileType;

	@ManyToOne
	private InterviewSchedule schedule;

	@ManyToOne
	private TaskSchedule taskSchedule;

}
