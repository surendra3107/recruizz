package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.User;

public interface TaskFolderRepository extends JpaRepository<TaskFolder, Long> {

	List<TaskFolder> findByName(String name);

	List<TaskFolder> findByOwner(User owner);

	TaskFolder findByNameAndOwner(String folderName, User owner);

}
