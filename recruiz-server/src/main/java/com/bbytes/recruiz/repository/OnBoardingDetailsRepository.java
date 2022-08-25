package com.bbytes.recruiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.OnBoardingDetails;

public interface OnBoardingDetailsRepository extends JpaRepository<OnBoardingDetails, Long> {

    List<OnBoardingDetails> findByEidAndOnboardCategory(Employee eid,String category);
    
    List<OnBoardingDetails> findByEidAndState(Employee eid,String state);
    
    List<OnBoardingDetails> findByEidAndOnboardCategoryAndState(Employee eid,String category,String state);

    List<OnBoardingDetails> findByEid(Employee emp);
    
    @Query(value = "SELECT distinct(sub_category_name) FROM onboarding_details where onboard_category= ?1 AND eid_id= ?2", nativeQuery = true)
    List<String> findDistinctSubCategoryByCategoryAndEmployee(String category, String eid);
    
    List<OnBoardingDetails> findByEidAndSubCategoryNameAndOnboardCategoryAndState(Employee emp,String subCategory,String category,String state);
    
    Long countByEidAndStateAndCompletedStatus(Employee emp,String state,Boolean status);
    
}
