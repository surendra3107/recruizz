package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.bbytes.recruiz.enums.ReminderPeriodType;
import com.bbytes.recruiz.enums.TaskState;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * Task Item dto object
 * 
 */

@Data
public class TaskItemDTO implements Serializable {

	private static final long serialVersionUID = 7763271917975748908L;

	private Long id;

	private TaskState state = TaskState.YET_TO_START;

	private String name;

	private String notes;

	private Date dueDateTime;

	private Date reminderDateTime;

	private TaskFolderDTO taskFolder;

	private UserDTO owner;

	private Set<UserDTO> users = new HashSet<>();

	private Integer reminderPeriod;

	private ReminderPeriodType reminderPeriodType;
	
	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setDueDateTime(Date dueDateTime) {
		this.dueDateTime = dueDateTime;
	}

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getDueDateTime() {
		return dueDateTime;
	}

	
}
