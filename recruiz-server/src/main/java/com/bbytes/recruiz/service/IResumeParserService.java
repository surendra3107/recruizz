package com.bbytes.recruiz.service;

import java.io.File;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.exception.RecruizException;

public interface IResumeParserService {

	/**
	 * The given resume is parsed and all key information is extracted and
	 * converted to {@link Candidate} object
	 * 
	 * @param resume
	 * @return
	 * @throws RecruizException
	 */

	Candidate parseResume(File resume) throws RecruizException;

	Candidate parseResumeForExternalUser(File resumeFile) throws RecruizException;

	Candidate parseResumeForExternalUser(String filePath, String fileName) throws RecruizException;

	/**
	 * Used only by bulk upload to control the number of request sent to parser server 
	 * @param resumeFile
	 * @return
	 * @throws RecruizException
	 */
	Candidate queueParseResume(File resumeFile) throws RecruizException ;

}
