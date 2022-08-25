package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

/**
 * Task Folder dto object in UI it is called as task label
 * 
 */

@Data
public class TaskFolderDTO implements Serializable {

	private static final long serialVersionUID = -4847028027928716129L;

	private Long id;

	private String name;

	private Set<TaskItemDTO> taskItems = new HashSet<>();

	private UserDTO owner;

}
