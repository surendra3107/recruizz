package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.BoardCustomStatus;
import com.bbytes.recruiz.domain.OnBoardingSubCategory;
import com.bbytes.recruiz.repository.BoardStatusRepository;

@Service
public class BoardCustomStatusService extends AbstractService<BoardCustomStatus, Long> {

    private BoardStatusRepository boardStatusRepository;

    @Autowired
    public BoardCustomStatusService(BoardStatusRepository boardStatusRepository) {
	super(boardStatusRepository);
	this.boardStatusRepository = boardStatusRepository;
    }
    
    public List<BoardCustomStatus> getAllBoardStatus(){
	return boardStatusRepository.findAll();
    }

    public BoardCustomStatus getBoardCustomStatusByKey(String key) {
	return boardStatusRepository.findByStatusKey(key);
    }
  
}
