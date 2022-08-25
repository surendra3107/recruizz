package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectClientDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectCorporateDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectRoundDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectSourceCandidateDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectVendorDTO;
import com.auklabs.recruiz.connect.core.dto.VendorInstanceIdDTO;
import com.auklabs.recruiz.connect.core.enums.ConnectEvent;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.connect.RecruizConnectClient;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rabbit.RabbitMessageSender;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class RecruizConnectService {

	@Autowired
	private RabbitMessageSender rabbitMessageSender;

	@Autowired
	private RoundService roundService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private FileService fileService;

	@Autowired
	private RecruizConnectClient recruizConnectClient;

	@Value("${recruiz.instance.identifier}")
	private String recruizInstanceId;

	public void addRecruizConnectCorporate(User user) throws Exception {

		ConnectCorporateDTO connectCorporateDTO = new ConnectCorporateDTO();
		connectCorporateDTO.setTenantId(user.getOrganization().getOrgId());
		connectCorporateDTO.setInstanceId(recruizInstanceId);
		connectCorporateDTO.setOrgName(user.getOrganization().getOrgName());
		connectCorporateDTO.setContactEmail(user.getEmail());
		connectCorporateDTO.setContactName(user.getName());
		connectCorporateDTO.setContactMobile(user.getMobile());
		connectCorporateDTO.setContactDesignation(user.getDesignation());
		connectCorporateDTO.setAddress(user.getOrganization().getAddress());
		connectCorporateDTO.setWebsite(user.getOrganization().getWebsiteUrl());

		recruizConnectClient.addConnectCorporate(connectCorporateDTO, null);
	}

	public void addRecruizConnectVendor(ConnectVendorDTO connectVendorDTO) throws Exception {

		User loggedInUser = userService.getLoggedInUserObject();

		connectVendorDTO.setTenantId(loggedInUser.getOrganization().getOrgId());
		connectVendorDTO.setInstanceId(recruizInstanceId);
		connectVendorDTO.setOrgName(loggedInUser.getOrganization().getOrgName());

		recruizConnectClient.addConnectVendor(connectVendorDTO, null);
	}

	public void updateRecruizConenctVendor(ConnectVendorDTO connectVendorDTO, String tenantId) throws Exception {

		User loggedInUser = userService.getLoggedInUserObject();

		connectVendorDTO.setTenantId(loggedInUser.getOrganization().getOrgId());
		connectVendorDTO.setInstanceId(recruizInstanceId);
		connectVendorDTO.setOrgName(loggedInUser.getOrganization().getOrgName());

		recruizConnectClient.updateConnectVendor(connectVendorDTO, tenantId, null);
	}

	public RestResponse getRecruizConnectVendor(String tenantId) throws Exception {

		ResponseEntity<RestResponse> restResponse = recruizConnectClient.getConnectVendor(tenantId, recruizInstanceId,
				null);
		if (restResponse.getBody().isSuccess())
			return new RestResponse(RestResponse.SUCCESS, restResponse.getBody().getData());
		else
			return new RestResponse(RestResponse.FAILED, restResponse.getBody().getData(),
					restResponse.getBody().getReason());

	}

	public List<ConnectVendorDTO> getRecruizConnectAllVendor() throws Exception {

		List<ConnectVendorDTO> connectVendorDTOList = recruizConnectClient.getAllConnectVendor(null);
		return connectVendorDTOList;
	}

	public List<ConnectPositionDTO> getAllPendingPositionVendor(String tenantId) throws Exception {

		List<ConnectPositionDTO> connectPositionDTOList = recruizConnectClient.getAllPendingPositionVendor(tenantId,
				recruizInstanceId, null);
		return connectPositionDTOList;
	}

	public void sendVendorPositionStatus(String positionCode, String corporateId, String corporateInstanceId,
			String vendorId, String status) {

		ConnectPositionEventDTO connectPositionEventDTO = new ConnectPositionEventDTO();

		VendorInstanceIdDTO vendorInstanceIdDTO = new VendorInstanceIdDTO();
		vendorInstanceIdDTO.setInstanceId(recruizInstanceId);
		vendorInstanceIdDTO.setVendorId(vendorId);

		ConnectPositionDTO connectPositionDTO = new ConnectPositionDTO();
		connectPositionDTO.setInstanceId(corporateInstanceId);
		connectPositionDTO.setTenantId(corporateId);
		connectPositionDTO.setPositionCode(positionCode);

		connectPositionEventDTO.setConnectPositionDTO(connectPositionDTO);
		connectPositionEventDTO.setVendorInstanceIdDTOs(new ArrayList<>(Arrays.asList(vendorInstanceIdDTO)));
		connectPositionEventDTO.setCorporatePositionStatus(status);
		connectPositionEventDTO.setEventType(ConnectEvent.addPositionToVendor);

		rabbitMessageSender.changeVendorPositionStatus(connectPositionEventDTO);
	}

	@Transactional
	public int sourceCandidate(ConnectCandidateEventDTO connectCandidateEventDTO) throws RecruizException, IOException {

		int duplicateCandidateCount = 0;
		for (ConnectSourceCandidateDTO connectSourceCandidateDTO : connectCandidateEventDTO.getCandidates()) {

			Candidate candidateFromDB = candidateService
					.getCandidateByEmail(connectSourceCandidateDTO.getCandidateEmail());
			if (candidateFromDB != null)
				duplicateCandidateCount++;
			else {
				Candidate candidate = resumeParserService.parseResume(connectSourceCandidateDTO.getResumeFile());
				Round round = roundService.findByConnectId(connectCandidateEventDTO.getRound().getConnectId());
				candidateService.sourceCandidate(candidate, connectSourceCandidateDTO.getResumeFile(),
						round.getId() + "", connectCandidateEventDTO.getPositionCode());
			}
		}
		return duplicateCandidateCount;
	}

	public void publishRecruizConnectPosition(Position position) throws Exception {

		User user = userService.getLoggedInUserObject();

		ResponseEntity<RestResponse> restResponse = recruizConnectClient
				.getConnectCorporate(user.getOrganization().getOrgId(), recruizInstanceId, null);

		if (!restResponse.getBody().isSuccess())
			throw new RecruizWarnException(ErrorHandler.CORPORATE_NOT_REGISTERED, ErrorHandler.CORPORATE_NOT_CONNECT);

		ConnectClientDTO clientDTO = new ConnectClientDTO();
		clientDTO.setTenantId(TenantContextHolder.getTenant());
		clientDTO.setInstanceId(recruizInstanceId);
		clientDTO.setClientName(position.getClient().getClientName());
		clientDTO.setAddress(position.getClient().getAddress());
		clientDTO.setWebsite(position.getClient().getWebsite());
		clientDTO.setClientLocation(position.getClient().getClientLocation());
		clientDTO.setEmpSize(position.getClient().getEmpSize());
		clientDTO.setTurnOver(position.getClient().getTurnOvr());
		clientDTO.setNotes(position.getClient().getNotes());
		clientDTO.setStatus(position.getClient().getStatus());
		clientDTO.setOwner(position.getClient().getOwner());

		ConnectPositionDTO connectPositionDTO = new ConnectPositionDTO();
		connectPositionDTO.setTenantId(TenantContextHolder.getTenant());
		connectPositionDTO.setInstanceId(recruizInstanceId);
		connectPositionDTO.setTitle(position.getTitle());
		connectPositionDTO.setPositionCode(position.getPositionCode());
		connectPositionDTO.setLocation(position.getLocation());
		connectPositionDTO.setTotalPosition(position.getTotalPosition());
		connectPositionDTO.setOpenedDate(position.getOpenedDate());
		connectPositionDTO.setCloseByDate(position.getCloseByDate());
		connectPositionDTO.setPositionUrl(position.getPositionUrl());
		connectPositionDTO.setReqSkillSet(new HashSet<String>(position.getReqSkillSet()));
		connectPositionDTO.setGoodSkillSet(new HashSet<String>(position.getGoodSkillSet()));
		connectPositionDTO.setEducationalQualification(new HashSet<String>(position.getEducationalQualification()));
		connectPositionDTO.setType(position.getType());
		connectPositionDTO.setRemoteWork(position.isRemoteWork());
		connectPositionDTO.setMinSal(position.getMinSal());
		connectPositionDTO.setMaxSal(position.getMaxSal());
		connectPositionDTO.setSalUnit(position.getSalUnit());
		connectPositionDTO.setDescription(position.getDescription());
		connectPositionDTO.setStatus(position.getStatus());
		connectPositionDTO.setOwner(position.getOwner());
		connectPositionDTO.setNotes(position.getNotes());
		connectPositionDTO.setExperienceRange(position.getExperienceRange());
		connectPositionDTO.setIndustry(position.getIndustry());
		connectPositionDTO.setFunctionalArea(position.getFunctionalArea());
		connectPositionDTO.setNationality(position.getNationality());
		connectPositionDTO.setPublishCareerSite(position.isPublishCareerSite());
		connectPositionDTO.setPublishRecruizConnect(position.isPublishRecruizConnect());
		connectPositionDTO.setNotes(position.getNotes());
		connectPositionDTO.setClientStatus(position.getClientStatus());
		connectPositionDTO.setClosedByUser(position.getClosedByUser());
		connectPositionDTO.setConnectClientDTO(clientDTO);

		connectPositionDTO.setConnectRoundDTOs(getConnectRounds(position));

		// TODO currently publishing to all vendor, later UI comes will send
		// vendor id and instance id from UI

		List<ConnectVendorDTO> connectVendorDTOList = getRecruizConnectAllVendor();
		List<VendorInstanceIdDTO> vendorInstanceIdDTOList = new ArrayList<VendorInstanceIdDTO>();

		if (connectVendorDTOList != null && !connectVendorDTOList.isEmpty()) {
			for (ConnectVendorDTO connectVendorDTO : connectVendorDTOList) {
				VendorInstanceIdDTO vendorInstanceIdDTO = new VendorInstanceIdDTO();
				vendorInstanceIdDTO.setInstanceId(connectVendorDTO.getInstanceId());
				vendorInstanceIdDTO.setVendorId(connectVendorDTO.getTenantId());

				vendorInstanceIdDTOList.add(vendorInstanceIdDTO);
			}
		}

		ConnectPositionEventDTO connectPositionEventDTO = new ConnectPositionEventDTO();
		connectPositionEventDTO.setConnectPositionDTO(connectPositionDTO);
		connectPositionEventDTO.setVendorInstanceIdDTOs(vendorInstanceIdDTOList);
		connectPositionEventDTO.setEventType(ConnectEvent.pushishPosition);

		if (connectVendorDTOList == null || connectVendorDTOList.isEmpty())
			throw new RecruizWarnException("No Vendor registered", "no_vendor_found");
		rabbitMessageSender.publishPosition(connectPositionEventDTO);
	}

	private List<ConnectRoundDTO> getConnectRounds(Position position) {
		List<ConnectRoundDTO> connectRoundDTOs = new LinkedList<ConnectRoundDTO>();
		Board board = position.getBoard();
		for (Round round : board.getRounds()) {

			// connect id is identifier for unique round in agency and corporate
			// i.e. combination of tenant,roundId and positionCode
			String connectId = TenantContextHolder.getTenant() + "-" + round.getId() + "-" + position.getPositionCode();

			ConnectRoundDTO connectRoundDTO = getConnectRoundDTO(round);
			connectRoundDTO.setConnectId(connectId);

			// saving unique connect id against round
			round.setConnectId(connectId);
			roundService.save(round);
			connectRoundDTOs.add(connectRoundDTO);
		}
		return connectRoundDTOs;
	}

	private ConnectRoundDTO getConnectRoundDTO(Round round) {
		ConnectRoundDTO connectRoundDTO = new ConnectRoundDTO();
		connectRoundDTO.setRoundName(round.getRoundName());
		connectRoundDTO.setRoundType(round.getRoundType());
		connectRoundDTO.setOrderNo(round.getOrderNo());
		connectRoundDTO.setConnectId(round.getConnectId());
		return connectRoundDTO;
	}

	public ResponseEntity<RestResponse> sourceCandidateToPosition(String corporateId, String corporateInstanceId,
			String positionCode, Round round, Candidate... candidates) throws Exception {

		ConnectCandidateEventDTO connectCandidateEventDTO = new ConnectCandidateEventDTO();

		connectCandidateEventDTO.setPositionCode(positionCode);
		connectCandidateEventDTO.setTenantId(TenantContextHolder.getTenant());
		connectCandidateEventDTO.setInstanceId(recruizInstanceId);
		connectCandidateEventDTO.setRound(getConnectRoundDTO(round));
		connectCandidateEventDTO.setCorporateId(corporateId);
		connectCandidateEventDTO.setCorporateInstanceId(corporateInstanceId);

		List<ConnectSourceCandidateDTO> connectSourceCandidateDTOs = new ArrayList<ConnectSourceCandidateDTO>();
		for (Candidate candidate : candidates) {
			ConnectSourceCandidateDTO connectSourceCandidateDTO = new ConnectSourceCandidateDTO();
			connectSourceCandidateDTO.setCandidateEmail(candidate.getEmail());
			connectSourceCandidateDTO.setCandidateName(candidate.getFullName());

			String resumeFilePath = candidate.getResumeLink();
			File resumeFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), resumeFilePath);
			connectSourceCandidateDTO.setResumeFile(resumeFile);

			connectSourceCandidateDTOs.add(connectSourceCandidateDTO);
		}
		connectCandidateEventDTO.setCandidates(connectSourceCandidateDTOs);
		connectCandidateEventDTO.setEventType(ConnectEvent.sourceCandidate);

		ResponseEntity<RestResponse> response = recruizConnectClient.sourceCandidate(connectCandidateEventDTO, null);
		return response;
	}

	private ConnectCandidateDTO getConnectCandidateDTO(Candidate candidate) {
		ConnectCandidateDTO connectCandidateDTO = new ConnectCandidateDTO();
		connectCandidateDTO.setCid(candidate.getCid());
		connectCandidateDTO.setFullName(candidate.getFullName());
		connectCandidateDTO.setEmail(candidate.getEmail());
		connectCandidateDTO.setMobile(candidate.getMobile());
		connectCandidateDTO.setCurrentCompany(candidate.getCurrentCompany());
		connectCandidateDTO.setCurrentTitle(candidate.getCurrentTitle());
		connectCandidateDTO.setCurrentLocation(candidate.getCurrentLocation());
		connectCandidateDTO.setHighestQual(candidate.getHighestQual());
		connectCandidateDTO.setTotalExp(candidate.getTotalExp());
		connectCandidateDTO.setEmploymentType(candidate.getEmploymentType());
		connectCandidateDTO.setCurrentCtc(candidate.getCurrentCtc());
		connectCandidateDTO.setExpectedCtc(candidate.getExpectedCtc());
		connectCandidateDTO.setCtcUnit(candidate.getCtcUnit());
		connectCandidateDTO.setNoticePeriod(candidate.getNoticePeriod());
		connectCandidateDTO.setNoticeStatus(candidate.isNoticeStatus());
		connectCandidateDTO.setLastWorkingDay(candidate.getLastWorkingDay());
		connectCandidateDTO.setPreferredLocation(candidate.getPreferredLocation());
		connectCandidateDTO.setKeySkills(new HashSet<String>(candidate.getKeySkills()));
		connectCandidateDTO.setDob(candidate.getDob());
		connectCandidateDTO.setResumeLink(candidate.getResumeLink());
		connectCandidateDTO.setGender(candidate.getGender());
		connectCandidateDTO.setCommunication(candidate.getCommunication());
		connectCandidateDTO.setComments(candidate.getComments());
		connectCandidateDTO.setLinkedinProf(candidate.getLinkedinProf());
		connectCandidateDTO.setFacebookProf(candidate.getFacebookProf());
		connectCandidateDTO.setTwitterProf(candidate.getTwitterProf());
		connectCandidateDTO.setGithubProf(candidate.getGithubProf());
		connectCandidateDTO.setStatus(candidate.getStatus());
		connectCandidateDTO.setSource(candidate.getSource());
		connectCandidateDTO.setSourceDetails(candidate.getSourceDetails());
		connectCandidateDTO.setProfileUrl(candidate.getProfileUrl());
		connectCandidateDTO.setOwner(candidate.getOwner());
		connectCandidateDTO.setAlternateEmail(candidate.getAlternateEmail());
		connectCandidateDTO.setAlternateMobile(candidate.getAlternateMobile());
		connectCandidateDTO.setSourceName(candidate.getSourceName());
		connectCandidateDTO.setSourceMobile(candidate.getSourceMobile());
		connectCandidateDTO.setSourceEmail(candidate.getSourceEmail());
		connectCandidateDTO.setNationality(candidate.getNationality());
		connectCandidateDTO.setMaritalStatus(candidate.getMaritalStatus());
		connectCandidateDTO.setCategory(candidate.getCategory());
		connectCandidateDTO.setSubCategory(candidate.getSubCategory());
		connectCandidateDTO.setLanguages(candidate.getLanguages());
		connectCandidateDTO.setAverageStayInCompany(candidate.getAverageStayInCompany().intValue());
		connectCandidateDTO.setLongestStayInCompany(candidate.getLongestStayInCompany().intValue());
		connectCandidateDTO.setSourcedOnDate(candidate.getSourcedOnDate());
		connectCandidateDTO.setSummary(candidate.getSummary());
		connectCandidateDTO.setCoverLetterPath(candidate.getCoverLetterPath());
		connectCandidateDTO.setActualSource(candidate.getActualSource());
		connectCandidateDTO.setS3Enabled(candidate.getS3Enabled());
		connectCandidateDTO.setDummy(candidate.getDummy());
		connectCandidateDTO.setCandidateRandomId(candidate.getCandidateRandomId());
		connectCandidateDTO.setPreviousEmployment(candidate.getPreviousEmployment());
		connectCandidateDTO.setAddress(candidate.getAddress());
		connectCandidateDTO.setIndustry(candidate.getIndustry());
		connectCandidateDTO.setLastActive(candidate.getLastActive());
		return connectCandidateDTO;
	}

	public void moveCandidateToRound(String connectSrcRoundId, String connectDestRoundId, double destCardIndex,
			List<String> candidateEmailList) {

		ConnectCandidateEventDTO connectCandidateEventDTO = new ConnectCandidateEventDTO();

		connectCandidateEventDTO.setConnectSrcRoundId(connectSrcRoundId);
		connectCandidateEventDTO.setConnectDestRoundId(connectDestRoundId);
		connectCandidateEventDTO.setCardIndex(destCardIndex);
		connectCandidateEventDTO.setTenantId(TenantContextHolder.getTenant());
		connectCandidateEventDTO.setInstanceId(recruizInstanceId);
		connectCandidateEventDTO.setCandidateEmailList(candidateEmailList);

		connectCandidateEventDTO.setEventType(ConnectEvent.moveCandidate);

		// as of now commented since connect feature on-hold @author-akshay
		//rabbitMessageSender.moveCandidateToRound(connectCandidateEventDTO, recruizInstanceId);

	}
}
