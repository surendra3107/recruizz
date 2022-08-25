package com.bbytes.recruiz.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Task Folder Domain Object in UI it is called as task label
 * 
 */

@Data
@EqualsAndHashCode(callSuper=false ,exclude = { "taskItems", "owner" })
@ToString(exclude = { "taskItems", "owner" })
@Entity(name = "task_folder")
public class TaskFolder extends AbstractEntity {

	private static final long serialVersionUID = -5506656797959922118L;

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "taskFolder", orphanRemoval=true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<TaskItem> taskItems = new HashSet<>();

	@ManyToOne(fetch=FetchType.LAZY)
	private User owner;

	public TaskFolder() {
	}

	public TaskFolder(String name) {
		this.name = name;
	}

	public void addTaskItem(TaskItem taskItem) {
		taskItems.add(taskItem);
		taskItem.setTaskFolder(this);
		taskItem.setOwner(owner);
		taskItem.addUsers(owner);
	}

	public void removeTaskItem(TaskItem taskItem) {
		taskItems.remove(taskItem);
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

}
