package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.repository.CustomFieldsRepository;

@Service
public class CustomFieldService extends AbstractService<CustomFields, Long> {

	private CustomFieldsRepository customFieldsRepository;

	@Autowired
	public CustomFieldService(CustomFieldsRepository customFieldsRepository) {
		super(customFieldsRepository);
		this.customFieldsRepository = customFieldsRepository;
	}

	@Transactional(readOnly=true)
	public List<CustomFields> getAllFieldsByEntity(CustomFieldEntityType entityType){
	   return customFieldsRepository.findByEntityType(entityType);
	}


}
