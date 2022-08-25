package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.OnBoardingSubCategory;

public interface OnBoardingSubCategoryRepository extends JpaRepository<OnBoardingSubCategory, Long> {

    List<OnBoardingSubCategory> findByOnboardCategory(String onbaordCategory);

    OnBoardingSubCategory findByOnboardCategoryAndSubCategoryName(String onBoardCategory, String subCategory);

    OnBoardingSubCategory findByCompositeKey(String key);

    @Query(value = "SELECT distinct(sub_category_name) FROM onboarding_details where onboard_category= ?1 AND eid_id= ?2", nativeQuery = true)
    List<String> findDistinctSubCategoryByCategory(String category, String eid);

}
