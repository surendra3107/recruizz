package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.EmailTemplateCategoryVariable;

public interface EmailTemplateVariableRepository extends JpaRepository<EmailTemplateCategoryVariable, Long> {

    List<EmailTemplateCategoryVariable> findByCategoryName(String category);

    @Query(value = "select distinct(templateVariable) from template_variable where templateId IN(select id from email_template_data where category=?1)", nativeQuery = true)
    List<String> findCategoryVariableByTemplateIds(String category);

    @Query(value = "select distinct(variable) from email_template_category_variable where categoryName=?1", nativeQuery = true)
    List<String> findListofVariablebyCategory(String category);
    
}
