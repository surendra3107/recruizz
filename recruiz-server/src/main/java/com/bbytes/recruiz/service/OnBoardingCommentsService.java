package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.OnBoardingDetailsComments;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.repository.OnBoardingCommentsRepository;
import com.bbytes.recruiz.repository.OnBoardingSubCategoryRepository;

@Service
public class OnBoardingCommentsService extends AbstractService<OnBoardingDetailsComments, Long> {

    private OnBoardingCommentsRepository commentsRepository;

    @Autowired
    public OnBoardingCommentsService(OnBoardingCommentsRepository commentsRepository) {
	super(commentsRepository);
	this.commentsRepository = commentsRepository;
    }
   
    @Transactional
    private OnBoardingDetailsComments updateComments(Long id,String comment) {
	OnBoardingDetailsComments existingComment = commentsRepository.findOne(id);
	existingComment.setComment(comment);
	commentsRepository.save(existingComment);
	return existingComment;
    }
    
    
    

  
}
