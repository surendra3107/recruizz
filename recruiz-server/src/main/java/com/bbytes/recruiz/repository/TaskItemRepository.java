package com.bbytes.recruiz.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.TaskState;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {

	@Query("SELECT COUNT(distinct ti.id) FROM task_item ti JOIN ti.users u WHERE ti.owner.email=:email OR u.email = :email")
	Long findTaskItemCountForUser(@Param("email") String email);

	List<TaskItem> findByName(String name);

	TaskItem findByIdAndOwner(Long id, User owner);

	List<TaskItem> findByOwner(User owner);
	
	List<TaskItem> findByUsersIn(Set<User> taskuser);

	List<TaskItem> findByUsers(User user);

	List<TaskItem> findByState(TaskState state);

	List<TaskItem> findByUsersIn(List<User> users);

	List<TaskItem> findByTaskFolder(TaskFolder taskFolder);

	List<TaskItem> findByUsersAndTaskFolderIn(User user, List<TaskFolder> taskFolders);

	List<TaskItem> findByOwnerAndTaskFolderIn(User user, List<TaskFolder> taskFolders);

	List<TaskItem> findByDueDateTimeBetween(Date start, Date end);

	List<TaskItem> findByReminderDateTimeBetween(Date start, Date end);

	List<TaskItem> findByUsersAndDueDateTimeBetween(User user, Date start, Date end);

	List<TaskItem> findByUsersAndReminderDateTimeBetween(User user, Date start, Date end);

	List<TaskItem> findByOwnerAndDueDateTimeBetween(User user, Date start, Date end);

	List<TaskItem> findByOwnerAndReminderDateTimeBetween(User user, Date start, Date end);

}
