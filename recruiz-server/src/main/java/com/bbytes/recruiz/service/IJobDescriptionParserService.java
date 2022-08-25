package com.bbytes.recruiz.service;

import java.io.File;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.exception.RecruizException;

public interface IJobDescriptionParserService {

	
	Position parseJobDescription(File jd) throws RecruizException;
	
	Position parseJobDescriptionText(String jdContent) throws RecruizException;

}
