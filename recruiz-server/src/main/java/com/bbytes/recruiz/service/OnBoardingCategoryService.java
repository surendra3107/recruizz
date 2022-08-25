package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.repository.OnBoardingSubCategoryRepository;

@Service
public class OnBoardingCategoryService extends AbstractService<OnBoardingSubCategory, Long> {

    private OnBoardingSubCategoryRepository subCategoryRepository;

    @Autowired
    public OnBoardingCategoryService(OnBoardingSubCategoryRepository subCategoryRepository) {
	super(subCategoryRepository);
	this.subCategoryRepository = subCategoryRepository;
    }
    
    public List<OnBoardingSubCategory> getSubCategoryByCategory(String categoryName){
	return subCategoryRepository.findByOnboardCategory(categoryName);
    }

    // to add and update sub categories
    @Transactional
    public List<OnBoardingSubCategory> manageSubCategory(List<OnBoardingSubCategory> subCategories) {
	for (OnBoardingSubCategory onBoardingSubCategory : subCategories) {
	    OnBoardingSubCategory subCategory = subCategoryRepository.findByCompositeKey(onBoardingSubCategory.getCompositeKey());
	    if(null == subCategory) {
		subCategoryRepository.save(onBoardingSubCategory);
	    }else {
		onBoardingSubCategory.setId(onBoardingSubCategory.getId());
	    }
	}
	
	return subCategories;
	
    }
    
    
    public List<String> getAllDistinctStatusForEmployeeTask(String eid,String category){
	return subCategoryRepository.findDistinctSubCategoryByCategory(category,eid);
    }
    
    
    public OnBoardingSubCategory getSubCategoryByCategoryAndSubCategoryName(String category,String subCategory) {
	return subCategoryRepository.findByOnboardCategoryAndSubCategoryName(category, subCategory);
    }
    
    

  
}
