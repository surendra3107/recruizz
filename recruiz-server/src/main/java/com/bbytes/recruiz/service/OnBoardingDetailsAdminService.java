package com.bbytes.recruiz.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.OnBoardingDetailsAdmin;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.enums.OnBoardingCategory;
import com.bbytes.recruiz.repository.OnBoardingDetailsAdminRepository;

@Service
public class OnBoardingDetailsAdminService extends AbstractService<OnBoardingDetailsAdmin, Long> {

    private OnBoardingDetailsAdminRepository onBoardingRepository;

    @Autowired
    public OnBoardingDetailsAdminService(OnBoardingDetailsAdminRepository onBoardingRepository) {
	super(onBoardingRepository);
	this.onBoardingRepository = onBoardingRepository;
    }

    @Autowired
    private OnBoardingCategoryService onBoardingCategoryService;

    public List<OnBoardingDetailsAdmin> getAdminOnBoardActivityList(String category, String subCategory) {
	return onBoardingRepository.findByOnboardCategoryAndSubCategoryName(category, subCategory);
    }

    public OnBoardingDetailsAdmin addOnBoardingActivity(OnBoardingDetailsAdmin onBoardingDetails) {
	onBoardingRepository.save(onBoardingDetails);
	return onBoardingDetails;
    }

    // to get admin template in a category

    @Transactional(readOnly = true)
    public LinkedHashMap<String, Object> getGroupedAdminOnBoardingDetails() {
	
	LinkedHashMap<String, Object> categoryMap = new LinkedHashMap<>();
	for (OnBoardingCategory category : OnBoardingCategory.values()) {
	    List<OnBoardingSubCategory> subCategories = onBoardingCategoryService
		    .getSubCategoryByCategory(category.name());

	    if (null != subCategories && !subCategories.isEmpty()) {
		HashMap<String, Object> subCategoryMap = new HashMap<>();
		for (OnBoardingSubCategory onBoardingSubCategory : subCategories) {
		    List<OnBoardingDetailsAdmin> onBoardingDetails = onBoardingRepository
			    .findByOnboardCategoryAndSubCategoryName(category.name(),
				    onBoardingSubCategory.getSubCategoryName());
		    if (null != onBoardingDetails && !onBoardingDetails.isEmpty()) {
			subCategoryMap.put(onBoardingSubCategory.getSubCategoryName(), onBoardingDetails);
		    }
		}

		if (null != subCategoryMap && subCategoryMap.size() > 0) {
		    categoryMap.put(category.name(), subCategoryMap);
		}
	    }
	}

	return categoryMap;
    }
    
    
    public List<OnBoardingDetailsAdmin> getGroupedTemplateOnBoardingDetails(String category,String subCategory,List<Long> ids) {
	return onBoardingRepository.findByOnboardCategoryAndSubCategoryNameAndIdIn(category, subCategory, ids);
    }
    

}
