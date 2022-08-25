package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EmailTemplateCategoryVariable;
import com.bbytes.recruiz.repository.EmailTemplateVariableRepository;

@Service
public class EmailTemplateVariableService extends AbstractService<EmailTemplateCategoryVariable, Long> {

	private EmailTemplateVariableRepository emailTemplateVariableRepository;

	@Autowired
	public EmailTemplateVariableService(EmailTemplateVariableRepository emailTemplateVariableRepository) {
		super(emailTemplateVariableRepository);
		this.emailTemplateVariableRepository = emailTemplateVariableRepository;
	}

	@Transactional(readOnly = true)
	public List<EmailTemplateCategoryVariable> getTemplateVariblesByCategory(String category) {
		return emailTemplateVariableRepository.findByCategoryName(category);
	}
	
	@Transactional(readOnly = true)
	public List<String> getTemplateVaribleListByCategory(String category) {
		return emailTemplateVariableRepository.findCategoryVariableByTemplateIds(category);
	}
	
	@Transactional(readOnly = true)
	public List<String> getListofTemplateVariblesByCategory(String category) {
		return emailTemplateVariableRepository.findListofVariablebyCategory(category);
	}

}
