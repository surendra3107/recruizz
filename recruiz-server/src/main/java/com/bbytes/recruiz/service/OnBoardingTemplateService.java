package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.OnBoardingDetailsAdmin;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.domain.OnBoardingTemplate;
import com.bbytes.recruiz.enums.OnBoardingCategory;
import com.bbytes.recruiz.repository.OnBoardingDetailsAdminRepository;
import com.bbytes.recruiz.repository.OnBoardingTemplateRepository;

@Service
public class OnBoardingTemplateService extends AbstractService<OnBoardingTemplate, Long> {

    private OnBoardingTemplateRepository templateRepository;

    @Autowired
    public OnBoardingTemplateService(OnBoardingTemplateRepository templateRepository) {
	super(templateRepository);
	this.templateRepository = templateRepository;
    }

    @Autowired
    private OnBoardingDetailsAdminService onBoardingService;

    @Autowired
    private OnBoardingCategoryService onBoardingCategoryService;

    // to get admin template in a category
    @Transactional(readOnly=true)
    public Map<String, Object> getGroupedTemplateOnBoardingDetails() {
	List<OnBoardingTemplate> templates = templateRepository.findAll();
	Map<String, Object> templateMap = new HashMap<>();
	if (null != templates && !templates.isEmpty()) {
	    for (OnBoardingTemplate onBoardingTemplate : templates) {
		List<Long> taskIds = new ArrayList<>();
		for (OnBoardingDetailsAdmin task : onBoardingTemplate.getTasks()) {
		    taskIds.add(task.getId());
		}

		
		LinkedHashMap<String, Object> categoryMap = new LinkedHashMap<>();
		
		for (OnBoardingCategory category : OnBoardingCategory.values()) {
		    List<OnBoardingSubCategory> subCategories = onBoardingCategoryService
			    .getSubCategoryByCategory(category.name());

		    if (null != subCategories && !subCategories.isEmpty()) {
			Map<String, Object> subCategoryMap = new HashMap<>();
			for (OnBoardingSubCategory onBoardingSubCategory : subCategories) {
			    List<OnBoardingDetailsAdmin> onBoardingDetails = onBoardingService
				    .getGroupedTemplateOnBoardingDetails(category.name(),
					    onBoardingSubCategory.getSubCategoryName(), taskIds);
			    if (null != onBoardingDetails && !onBoardingDetails.isEmpty()) {
				subCategoryMap.put(onBoardingSubCategory.getSubCategoryName(), onBoardingDetails);
			    }
			}

			if (null != subCategoryMap && subCategoryMap.size() > 0) {
			    categoryMap.put(category.name(), subCategoryMap);
			}
		    }
		}
		templateMap.put(onBoardingTemplate.getTemplateName(), categoryMap);
	    }
	}

	return templateMap;
    }

    public void addTemplate(String templateName, List<Long> taskIds) {
	Set<OnBoardingDetailsAdmin> tasks = new HashSet<>();
	for (Long taskId : taskIds) {
	    OnBoardingDetailsAdmin task = onBoardingService.findOne(taskId);
	    tasks.add(task);
	}
	OnBoardingTemplate template = new OnBoardingTemplate();
	template.setTemplateName(templateName);
	template.setTasks(tasks);
	templateRepository.save(template);
    }
    
    public void editTemplate(String templateName, List<Long> taskIds, List<Long> removedTaskIds,Long templateId) {
  	OnBoardingTemplate template = templateRepository.findOne(templateId);
	Set<OnBoardingDetailsAdmin> tasks = template.getTasks();
  	for (Long taskId : taskIds) {
  	    OnBoardingDetailsAdmin task = onBoardingService.findOne(taskId);
  	    tasks.add(task);
  	}
  	
  	// removing the task
  	if(null != removedTaskIds && !removedTaskIds.isEmpty()) {
  		for (Long taskId : removedTaskIds) {
  	  	    OnBoardingDetailsAdmin task = onBoardingService.findOne(taskId);
  	  	    if(null != tasks && tasks.contains(task)) {
  	  		tasks.remove(task);
  	  	    }
  	  	}
  	}
  
  	
  	template.setTemplateName(templateName);
  	//template.getTasks().clear();
  	template.setTasks(tasks);
  	templateRepository.save(template);
      }

   
    public OnBoardingTemplate getTemplateByName(String templateName) {
	OnBoardingTemplate template = templateRepository.findByTemplateName(templateName);
	return template;
    }



}
