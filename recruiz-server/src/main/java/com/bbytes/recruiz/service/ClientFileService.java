package com.bbytes.recruiz.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.ClientFile;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.ExpectedCTCRange;
import com.bbytes.recruiz.enums.ExperinceRange;
import com.bbytes.recruiz.enums.InactiveSinceRange;
import com.bbytes.recruiz.enums.NoticePeriodRange;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.BoardRepository;
import com.bbytes.recruiz.repository.ClientFileRepository;
import com.bbytes.recruiz.rest.dto.models.BoardDTO;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RoundResponseDTO;
import com.bbytes.recruiz.utils.DateUtil;

@Service
public class ClientFileService extends AbstractService<ClientFile, Long> {

    private ClientFileRepository clientFileRepository;

    @Autowired
    public ClientFileService(ClientFileRepository clientFileRepository) {
	super(clientFileRepository);
	this.clientFileRepository = clientFileRepository;
    }

    // to getclient files by client
    public List<ClientFile> getClientFilesByClientId(String clientId) {
	return clientFileRepository.findByClientId(clientId);
    }

	public List<ClientFile> getClientFileByStorageModeAWS() {
		// TODO Auto-generated method stub
		return clientFileRepository.getClientFileByStorageModeAWS();
	}
}
