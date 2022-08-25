package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectClientDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectRoundDTO;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.OrganizationTaxDetails;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.TeamMember;
import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.TeamRole;
import com.bbytes.recruiz.rest.dto.models.OrganizationTaxDetailsDTO;
import com.bbytes.recruiz.rest.dto.models.TaxDTO;
import com.bbytes.recruiz.rest.dto.models.TeamDTO;
import com.bbytes.recruiz.rest.dto.models.TeamMemberDTO;
import com.bbytes.recruiz.rest.dto.models.integration.RchilliJDParserData;
import com.bbytes.recruiz.rest.dto.models.integration.RchilliJDPreferredSkill;
import com.bbytes.recruiz.rest.dto.models.integration.RchilliJDRequiredSkill;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class DTOToDomainConverstionService {

	@Autowired
	private TeamService teamService;

	@Autowired
	private UserService userService;

	public Position convertConnectPosition(ConnectPositionDTO connectPositionDTO) {

		Position position = new Position();

		position.setPositionCode(connectPositionDTO.getPositionCode());
		position.setTitle(connectPositionDTO.getTitle());
		position.setLocation(connectPositionDTO.getLocation());
		position.setTotalPosition(connectPositionDTO.getTotalPosition());
		position.setOpenedDate(connectPositionDTO.getOpenedDate());
		position.setCloseByDate(connectPositionDTO.getCloseByDate());
		position.setReqSkillSet(connectPositionDTO.getReqSkillSet());
		position.setGoodSkillSet(connectPositionDTO.getGoodSkillSet());
		position.setEducationalQualification(connectPositionDTO.getEducationalQualification());
		position.setType(connectPositionDTO.getType());
		position.setRemoteWork(connectPositionDTO.isRemoteWork());
		position.setMinSal(connectPositionDTO.getMinSal());
		position.setMaxSal(connectPositionDTO.getMinSal());
		position.setSalUnit(connectPositionDTO.getSalUnit());
		position.setPositionUrl(connectPositionDTO.getPositionUrl());
		position.setDescription(connectPositionDTO.getDescription());
		position.setStatus(connectPositionDTO.getStatus());
		position.setExperienceRange(connectPositionDTO.getExperienceRange());
		position.setFunctionalArea(connectPositionDTO.getFunctionalArea());
		position.setIndustry(connectPositionDTO.getIndustry());
		position.setNationality(connectPositionDTO.getNationality());
		position.setOwner(connectPositionDTO.getOwner());
		position.setClientStatus(connectPositionDTO.getClientStatus());
		position.setClosedByUser(connectPositionDTO.getClosedByUser());
		position.setPublishCareerSite(connectPositionDTO.isPublishCareerSite());
		position.setConnectCorporateId(connectPositionDTO.getTenantId());
		position.setConnectInstanceId(connectPositionDTO.getInstanceId());
		position.setPublishMode(GlobalConstants.PUBLISH_MODE_CONNECT);

		return position;
	}

	public Client convertConnectClient(ConnectClientDTO connectClientDTO) {

		Client client = new Client();

		client.setClientName(connectClientDTO.getClientName());
		client.setClientLocation(connectClientDTO.getClientLocation());
		client.setAddress(connectClientDTO.getAddress());
		client.setEmpSize(connectClientDTO.getEmpSize());
		client.setTurnOvr(connectClientDTO.getTurnOver());
		client.setNotes(connectClientDTO.getNotes());
		client.setOwner(connectClientDTO.getOwner());
		client.setStatus(connectClientDTO.getStatus());
		client.setWebsite(connectClientDTO.getWebsite());

		return client;
	}

	public List<Round> convertConnectRound(List<ConnectRoundDTO> connectRoundDTOs) {

		List<Round> rounds = new ArrayList<Round>();
		for (ConnectRoundDTO connectRoundDTO : connectRoundDTOs) {

			Round round = convertConnectRound(connectRoundDTO);
			rounds.add(round);
		}
		return rounds;
	}

	public Round convertConnectRound(ConnectRoundDTO connectRoundDTO) {

		Round round = new Round();

		round.setRoundName(connectRoundDTO.getRoundName());
		round.setRoundType(connectRoundDTO.getRoundType());
		round.setOrderNo(connectRoundDTO.getOrderNo());
		round.setConnectId(connectRoundDTO.getConnectId());

		return round;
	}

	public Candidate convertConnectCandidate(ConnectCandidateDTO connectCandidateDTO) {
		Candidate candidate = new Candidate();
		candidate.setFullName(connectCandidateDTO.getFullName());
		candidate.setEmail(connectCandidateDTO.getEmail());
		candidate.setMobile(connectCandidateDTO.getMobile());
		candidate.setCurrentCompany(connectCandidateDTO.getCurrentCompany());
		candidate.setCurrentTitle(connectCandidateDTO.getCurrentTitle());
		candidate.setCurrentLocation(connectCandidateDTO.getCurrentLocation());
		candidate.setHighestQual(connectCandidateDTO.getHighestQual());
		candidate.setTotalExp(connectCandidateDTO.getTotalExp());
		candidate.setEmploymentType(connectCandidateDTO.getEmploymentType());
		candidate.setCurrentCtc(connectCandidateDTO.getCurrentCtc());
		candidate.setExpectedCtc(connectCandidateDTO.getExpectedCtc());
		candidate.setCtcUnit(connectCandidateDTO.getCtcUnit());
		candidate.setNoticePeriod(connectCandidateDTO.getNoticePeriod());
		candidate.setNoticeStatus(connectCandidateDTO.isNoticeStatus());
		candidate.setLastWorkingDay(connectCandidateDTO.getLastWorkingDay());
		candidate.setPreferredLocation(connectCandidateDTO.getPreferredLocation());
		candidate.setKeySkills(new HashSet<String>(connectCandidateDTO.getKeySkills()));
		candidate.setDob(connectCandidateDTO.getDob());
		candidate.setResumeLink(connectCandidateDTO.getResumeLink());
		candidate.setGender(connectCandidateDTO.getGender());
		candidate.setCommunication(connectCandidateDTO.getCommunication());
		candidate.setComments(connectCandidateDTO.getComments());
		candidate.setLinkedinProf(connectCandidateDTO.getLinkedinProf());
		candidate.setFacebookProf(connectCandidateDTO.getFacebookProf());
		candidate.setTwitterProf(connectCandidateDTO.getTwitterProf());
		candidate.setGithubProf(connectCandidateDTO.getGithubProf());
		candidate.setStatus(connectCandidateDTO.getStatus());
		candidate.setSource(connectCandidateDTO.getSource());
		candidate.setSourceDetails(connectCandidateDTO.getSourceDetails());
		candidate.setProfileUrl(connectCandidateDTO.getProfileUrl());
		candidate.setOwner(connectCandidateDTO.getOwner());
		candidate.setAlternateEmail(connectCandidateDTO.getAlternateEmail());
		candidate.setAlternateMobile(connectCandidateDTO.getAlternateMobile());
		candidate.setSourceName(connectCandidateDTO.getSourceName());
		candidate.setSourceMobile(connectCandidateDTO.getSourceMobile());
		candidate.setSourceEmail(connectCandidateDTO.getSourceEmail());
		candidate.setNationality(connectCandidateDTO.getNationality());
		candidate.setMaritalStatus(connectCandidateDTO.getMaritalStatus());
		candidate.setCategory(connectCandidateDTO.getCategory());
		candidate.setSubCategory(connectCandidateDTO.getSubCategory());
		candidate.setLanguages(connectCandidateDTO.getLanguages());
		candidate.setAverageStayInCompany(Double.parseDouble(connectCandidateDTO.getAverageStayInCompany() + ""));
		candidate.setLongestStayInCompany(Double.parseDouble(connectCandidateDTO.getLongestStayInCompany() + ""));
		candidate.setSourcedOnDate(connectCandidateDTO.getSourcedOnDate());
		candidate.setSummary(connectCandidateDTO.getSummary());
		candidate.setCoverLetterPath(connectCandidateDTO.getCoverLetterPath());
		candidate.setActualSource(connectCandidateDTO.getActualSource());
		candidate.setS3Enabled(connectCandidateDTO.getS3Enabled());
		candidate.setDummy(connectCandidateDTO.getDummy());
		candidate.setCandidateRandomId(connectCandidateDTO.getCandidateRandomId());
		candidate.setPreviousEmployment(connectCandidateDTO.getPreviousEmployment());
		candidate.setAddress(connectCandidateDTO.getAddress());
		candidate.setIndustry(connectCandidateDTO.getIndustry());
		candidate.setLastActive(connectCandidateDTO.getLastActive());
		return candidate;
	}

	public Position convert(RchilliJDParserData jdParserData) {
		Position position = new Position();
		position.setDescription(jdParserData.getJobDescription());
		position.setExperienceRange(jdParserData.getExperienceRequired().getMinimumYearsExperience() + " - "
				+ jdParserData.getExperienceRequired().getMaximumYearsExperience());
		position.setTitle(jdParserData.getJobProfile().getTitle());
		position.setIndustry(jdParserData.getIndustryType());
		position.setLocation(jdParserData.getJobLocation().getLocation());
		position.setNationality(jdParserData.getJobLocation().getCountry());
		position.setType(jdParserData.getExcecutiveType());
		position.setTypeDisplayName(jdParserData.getExcecutiveType());
		Set<String> reqSkillSet = new HashSet<String>();
		for (RchilliJDRequiredSkill reqSkill : jdParserData.getSkills().getRequired()) {
			reqSkillSet.add(reqSkill.getSkill());
		}
		position.setReqSkillSet(reqSkillSet);

		Set<String> prefSkillSet = new HashSet<String>();
		for (RchilliJDPreferredSkill prefSkill : jdParserData.getSkills().getPreferred()) {
			prefSkillSet.add(prefSkill.getSkill());
		}
		position.setGoodSkillSet(prefSkillSet);

		String domainCommaSeperated = jdParserData.getDomains().stream().map(String::toUpperCase)
				.collect(Collectors.joining(","));
		position.setNotes("Domains List : " + domainCommaSeperated);

		try {
			if (jdParserData.getNoOfOpenings() != null)
				position.setTotalPosition(Integer.parseInt(jdParserData.getNoOfOpenings()));
		} catch (NumberFormatException e) {
			// do nothing
		}

		return position;
	}

	public Tax convertTax(TaxDTO taxDTO) {
		Tax tax = new Tax();
		tax.setTaxName(taxDTO.getTaxName());
		tax.setTaxNumber(taxDTO.getTaxNumber());
		return tax;
	}

	public OrganizationTaxDetails convertOrganizationTaxDetails(OrganizationTaxDetailsDTO organizationTaxDetailsDTO) {
		OrganizationTaxDetails organizationTaxDetails = new OrganizationTaxDetails(
				organizationTaxDetailsDTO.getTaxName(), organizationTaxDetailsDTO.getTaxValue());
		return organizationTaxDetails;
	}

	public Team convertTeam(TeamDTO teamDTO) {

		Team team = new Team();
		if (teamDTO.getTeamId() != null && teamDTO.getTeamId() != 0)
			team.setId(teamDTO.getTeamId());

		if (teamDTO.getTeamName() != null)
			team.setTeamName(teamDTO.getTeamName());

		if (teamDTO.getTeamDesc() != null)
			team.setTeamDesc(teamDTO.getTeamDesc());
		
		if (teamDTO.getRootTeam()!= null)
			team.setRootTeam(teamDTO.getRootTeam());

		if (teamDTO.getTeamTargetAmount() != null)
			team.setTeamTargetAmount(teamDTO.getTeamTargetAmount());
		
		if (teamDTO.getTeamTargetPositionOpeningClosure() != null)
			team.setTeamTargetPositionOpeningClosure(teamDTO.getTeamTargetPositionOpeningClosure());

		if (teamDTO.getTeamTargetAmountCurrency() != null)
			team.setTeamTargetAmountCurrency(Currency.valueOf(teamDTO.getTeamTargetAmountCurrency()));

		return team;
	}

	public TeamMember convertTeamMemberDTO(TeamMemberDTO memberDTO, Team team) {
		TeamMember member = new TeamMember();
		member.setId(memberDTO.getId());
		
		if(memberDTO.getRole()==null)
			member.setRole(TeamRole.member);
		else
			member.setRole(TeamRole.valueOf(memberDTO.getRole()));
		
		member.setTargetAmount(memberDTO.getTargetAmount());
		member.setTargetPositionOpeningClosure(memberDTO.getTargetPositionOpeningClosure());
		member.setTeam(team);
		if (memberDTO.getEmail() != null)
			member.setUser(userService.getUserByEmail(memberDTO.getEmail()));
		return member;
	}


	public List<TeamMember> convertTeamMemberDTOs(Collection<TeamMemberDTO> memberDTOs, Team team) {
		List<TeamMember> teamMembers = new ArrayList<>();
		for (TeamMemberDTO teamMemberDTO : memberDTOs) {
			teamMembers.add(convertTeamMemberDTO(teamMemberDTO, team));
		}

		return teamMembers;
	}

}
