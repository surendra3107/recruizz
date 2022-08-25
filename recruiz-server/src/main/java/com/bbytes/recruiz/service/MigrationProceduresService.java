package com.bbytes.recruiz.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.repository.MigrationProcedureRepository;

@Service("MigrationProceduresService")
public class MigrationProceduresService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationProceduresService.class);

    @Autowired
    private MigrationProcedureRepository migrationProcedureRepository;

    @Autowired
    private InterviewPanelService interviewPanelService;

    @Autowired
    private GenericInterviewerService genericInterviewerService;

    @Transactional
    public void migrateExistingClientInterviewer(String procedureName, String version, String tenantId) {

	try {
	    logger.info("Installing migration procedure : " + procedureName + " for organization " + tenantId);

	    List<ClientInterviewerPanel> clientInterviewers = interviewPanelService.findAll();
	    Set<ClientInterviewerPanel> genericInterviewersToBeAdded = new HashSet<>();

	    if (null != clientInterviewers && !clientInterviewers.isEmpty()) {
		genericInterviewersToBeAdded.addAll(clientInterviewers);
	    }

	    if (null != genericInterviewersToBeAdded && !genericInterviewersToBeAdded.isEmpty()) {
		genericInterviewerService.addGenericInterviewerFromClientOrPosition(genericInterviewersToBeAdded);
	    }
	    migrationProcedureRepository.updateExecutionStatus(procedureName, version, tenantId, true);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    migrationProcedureRepository.updateExecutionStatus(procedureName, version, tenantId, false);
	}

	logger.info("\t\t Installed migration procedure : " + procedureName + " for organization " + tenantId);

    }

}