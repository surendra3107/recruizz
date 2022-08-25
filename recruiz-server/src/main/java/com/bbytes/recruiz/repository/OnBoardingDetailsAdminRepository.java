package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.OnBoardingDetailsAdmin;

public interface OnBoardingDetailsAdminRepository extends JpaRepository<OnBoardingDetailsAdmin, Long> {

    List<OnBoardingDetailsAdmin> findByOnboardCategoryAndSubCategoryName(String category, String subCategory);

   public List<OnBoardingDetailsAdmin> findByOnboardCategoryAndSubCategoryNameAndIdIn(String category, String subCategory,List<Long> ids);
    
}
