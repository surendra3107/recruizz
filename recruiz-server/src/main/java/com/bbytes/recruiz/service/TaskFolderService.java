package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.repository.TaskFolderRepository;

@Service
public class TaskFolderService extends AbstractService<TaskFolder, Long> {

	private TaskFolderRepository taskFolderRepository;

	@Autowired
	public TaskFolderService(TaskFolderRepository taskFolderRepository) {
		super(taskFolderRepository);
		this.taskFolderRepository = taskFolderRepository;
	}

	public List<TaskFolder> findByName(String name) {
		return this.taskFolderRepository.findByName(name);
	}

	public List<TaskFolder> findByOwner(User owner) {
		return this.taskFolderRepository.findByOwner(owner);
	}
	
	public TaskFolder findByNameAndOwner(String folderName,User owner){
		return this.taskFolderRepository.findByNameAndOwner(folderName,owner);
	}
	

}
