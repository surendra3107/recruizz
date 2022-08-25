package com.bbytes.recruiz.domain;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.DateTime;

import com.bbytes.recruiz.enums.ReminderPeriodType;
import com.bbytes.recruiz.enums.TaskState;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Task Item Domain Object
 * 
 */

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "owner", "users", "taskSchedule" })
@ToString(exclude = { "owner", "users", "taskSchedule" })
@NoArgsConstructor
@Entity(name = "task_item")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@CyclicReferenceId")
public class TaskItem extends AbstractEntity {

	private static final long serialVersionUID = -5509956797959922118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TaskState state = TaskState.YET_TO_START;

	@Column(nullable = false)
	private String name;

	@Column(length = 10000)
	private String notes;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonSerialize(using = DatePickerDateSerializer.class)
	@Column(name = "due_date_time")
	private Date dueDateTime;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonSerialize(using = DatePickerDateSerializer.class)
	@Column(name = "reminder_date_time")
	private Date reminderDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	private TaskFolder taskFolder;

	@ManyToOne(fetch = FetchType.LAZY)
	private User owner;

	// @ManyToMany(fetch = FetchType.LAZY)
	// @CollectionTable(name = "task_users", joinColumns = { @JoinColumn(name =
	// "id") })
	@ManyToMany(fetch = FetchType.LAZY)
	private Set<User> users = new HashSet<>();

	@Column(nullable = true)
	private Integer reminderPeriod;

	@Enumerated(EnumType.STRING)
	private ReminderPeriodType reminderPeriodType;

	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
	private TaskSchedule taskSchedule;

	public TaskItem(String name, String notes, Date dueDateTime, Integer reminderPeriod,
			ReminderPeriodType reminderPeriodType) {
		this.name = name;
		this.notes = notes;
		this.dueDateTime = dueDateTime;
		this.reminderPeriod = reminderPeriod;
		this.reminderPeriodType = reminderPeriodType;
	}

	public TaskItem(String name, String notes, Date dueDateTime, Date reminderDateTime) {
		this.name = name;
		this.notes = notes;
		this.dueDateTime = dueDateTime;
		this.reminderDateTime = reminderDateTime;
	}

	public void addUsers(User user) {
		if (users == null) {
			users = new HashSet<User>();
		}
		users.add(user);
	}

	public void setUsers(Set<User> users) {
		if (users == null) {
			users = new HashSet<User>();
		}
		users.removeAll(Collections.singleton(null));
		this.users = users;
	}

	public void removeUsers(User user) {
		users.remove(user);
	}

	public void setOwner(User owner) {
		this.owner = owner;
		addUsers(owner);
	}

	public void addUsers(Set<User> users) {
		if (users == null) {
			users = new HashSet<User>();
		}
		this.users.addAll(users);
	}

	public Date processReminderDateTime() {
		if (dueDateTime == null || reminderPeriod == null || reminderPeriodType == null)
			return null;

		Date reminderDateTime;
		switch (reminderPeriodType) {
		case Minutes:
			reminderDateTime = new DateTime(dueDateTime).minusMinutes(reminderPeriod).toDate();
			break;
		case Hours:
			reminderDateTime = new DateTime(dueDateTime).minusHours(reminderPeriod).toDate();
			break;
		case Days:
			reminderDateTime = new DateTime(dueDateTime).minusDays(reminderPeriod).toDate();
			break;
		default:
			reminderDateTime = new DateTime(dueDateTime).minusMinutes(reminderPeriod).toDate();
			break;
		}
		setReminderDateTime(reminderDateTime);
		return reminderDateTime;
	}
}
