package com.bbytes.recruiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EmployeeActivity;
import com.bbytes.recruiz.repository.EmployeeActivityRepository;

@Service
public class EmployeeActivityService extends AbstractService<EmployeeActivity, Long> {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeActivityService.class);

	private EmployeeActivityRepository employeeActivityRepository;

	@Autowired
	public EmployeeActivityService(EmployeeActivityRepository employeeActivityRepository) {
		super(employeeActivityRepository);
		this.employeeActivityRepository = employeeActivityRepository;
	}

	@Transactional
	public void addActivity(EmployeeActivity activity) {
	    employeeActivityRepository.save(activity);
	}

	public Page<EmployeeActivity> getEmployeeActivity(Long eid, Pageable pageable) {
		return employeeActivityRepository.findByEid(eid, pageable);
	}

}