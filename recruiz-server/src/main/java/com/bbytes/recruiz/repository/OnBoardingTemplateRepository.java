package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.ClientFile;
import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.domain.OnBoardingTemplate;

public interface OnBoardingTemplateRepository extends JpaRepository<OnBoardingTemplate, Long> {
    
    OnBoardingTemplate findByTemplateName(String name);
    
}
