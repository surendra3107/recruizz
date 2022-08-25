package com.bbytes.recruiz.rabbit;

import java.io.IOException;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionEventDTO;
import com.auklabs.recruiz.connect.core.enums.ConnectEvent;
import com.auklabs.recruiz.connect.core.utils.RabbitMQConstants;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.DTOToDomainConverstionService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.RoundService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class RabbitMessageConsumer {

	@Autowired
	private DTOToDomainConverstionService dtoToDomainConverstionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Value("${candidate.folderPath.path}")
	private String candidateFolderPath;

	@Value("${dummy.resume.pdf.path}")
	private String candidateDummyResumeFilePath;

	@Autowired
	private UserService userService;

	// Commented out since connect feature is not used
	// @RabbitListener(queues =
	// RabbitMQConstants.RECRUIZ_CONNECT_POSITION_TOPIC_QUEUE)
	public void receiveMessageFromPosition(ConnectPositionEventDTO connectEventResponse) throws RecruizException {

		ConnectPositionDTO connectPositionDTO = connectEventResponse.getConnectPositionDTO();
		TenantContextHolder.setTenant(connectEventResponse.getVendorInstanceIdDTOs().get(0).getVendorId());
		Organization org = organizationService.getCurrentOrganization();
		if (GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(org.getOrgType()))
			triggerPositionEvent(connectEventResponse, connectPositionDTO);
	}

	// Commented out since connect feature is not used
	// @RabbitListener(queues =
	// RabbitMQConstants.RECRUIZ_CONNECT_CANDIDATE_TOPIC_QUEUE)
	public void receiveMessageFromCandidate(ConnectCandidateEventDTO connectCandidateEventDTO)
			throws RecruizException, IOException {

		String tenantId = connectCandidateEventDTO.getTenantId();
		TenantContextHolder.setTenant(tenantId);
		triggerCandidateEvent(connectCandidateEventDTO);

	}

	public void triggerCandidateEvent(ConnectCandidateEventDTO connectCandidateEventDTO)
			throws RecruizException, IOException {

		ConnectEvent connectEvent = connectCandidateEventDTO.getEventType();

		switch (connectEvent) {
		case moveCandidate:
			moveCandidateToRound(connectCandidateEventDTO);
			break;
		default:
			break;
		}
	}

	@Transactional
	public void moveCandidateToRound(ConnectCandidateEventDTO connectCandidateEventDTO) throws RecruizException {

		roundCandidateService.moveCandidate(connectCandidateEventDTO.getCandidateEmailList(),
				connectCandidateEventDTO.getConnectSrcRoundId(), connectCandidateEventDTO.getConnectDestRoundId(),
				connectCandidateEventDTO.getCardIndex());
	}

	public void triggerPositionEvent(ConnectPositionEventDTO connectEventResponse,
			ConnectPositionDTO connectPositionDTO) throws RecruizException {

		switch (connectEventResponse.getEventType()) {
		case addPositionToVendor:
			savePosition(connectPositionDTO);

			break;
		default:
			break;
		}
	}

	@Transactional
	private void savePosition(ConnectPositionDTO connectEventResponse) throws RecruizException {
		Client client = clientService.getClientByName(connectEventResponse.getConnectClientDTO().getClientName());

		if (client == null) {
			client = dtoToDomainConverstionService.convertConnectClient(connectEventResponse.getConnectClientDTO());
			client = clientService.save(client);
		}

		Position position = dtoToDomainConverstionService.convertConnectPosition(connectEventResponse);
		position.setClient(client);

		List<Round> rounds = dtoToDomainConverstionService
				.convertConnectRound(connectEventResponse.getConnectRoundDTOs());

		// TODO currently putting super admin, afterward whoever agency's user
		// accept the published position their email address would be the
		// loggedInUser email address
		String loggedInUserEmail = null;
		List<User> users = userService.getAllByRoleName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
		if (users != null && !users.isEmpty()) {
			loggedInUserEmail = users.get(0).getEmail();
		}

		positionService.addPositionFromRecruizConnect(position, loggedInUserEmail, rounds);

	}

}