package com.bbytes.recruiz.service;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateInvoice;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.ClientDecisionMaker;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.GenericDecisionMaker;
import com.bbytes.recruiz.domain.GenericInterviewer;
import com.bbytes.recruiz.domain.InvoiceSettings;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CategoryOptions;
import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.IndustryOptions;
import com.bbytes.recruiz.enums.InvoiceStatus;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.FileUploadRequestDTO;
import com.bbytes.recruiz.rest.dto.models.JoinedCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectPostionDTO;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class DummyValueService {

    private Logger logger = LoggerFactory.getLogger(DummyValueService.class);

    @Autowired
    private PositionService positionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private RoundCandidateService roundCandidateService;

    @Autowired
    private CandidateActivityService candidateActivityService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private InterviewScheduleService interviewScheduleService;

    @Autowired
    private CandidateStatusService candidateStatusService;

    @Autowired
    private AgencyInvoiceService agencyInvoiceService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProspectService prospectService;

    @Autowired
    private ProspectPositionService prospectPositionService;

    @Autowired
    private InvoiceSettingsService invoiceSettingsService;

    @Autowired
    private TaxService taxService;

    @Autowired
    private GenericInterviewerService genericInterviewerService;

    @Autowired
    private GenericDecisionMakerService genericDecisionMakerService;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${dummy.cv.path}")
    private String dbTemplateFolderPath;

    public List<Client> addClient() throws RecruizException {

	String clientName1 = "Dummy_Auk Labs Inc.";
	String clientName2 = "Dummy_Beyond Bytes Technology Pvt Ltd";

	// decision maker constants
	String decisionMakerEmail1 = "dummy_dm1@example.com";
	String decisionMakerEmail2 = "dummy_dm2@example.com";

	String decisionMakerMobile1 = "+917007000001";
	String decisionMakerMobile2 = "+917007000002";

	String decisionMakerName1 = "CDM Kumar";
	String decisionMakerName2 = "CDM Kumar";

	// interviewer constants
	String interviewerEmail1 = "dummy_ie1@example.com";
	String interviewerEmail2 = "dummy_ie2@example.com";

	String interviewerMobile1 = "+918008000001";
	String interviewerMobile2 = "+918008000002";

	String interviewerName1 = "CIE Kumar";
	String interviewerName2 = "CIE Kumar";

	// client 1 data
	ClientDTO clientDto1 = new ClientDTO();
	clientDto1.setAddress("Kasturi Nagar, Bangalore");
	clientDto1.setClientName(clientName1);
	clientDto1.setEmpSize("50");
	clientDto1.setTurnOvr("50");
	clientDto1.setWebsite("http://www.recruiz.com");
	clientDto1.setClientLocation("Bangalore, Karnataka, India");
	clientDto1.setDummy(true);

	ClientDecisionMaker dm1 = new ClientDecisionMaker();
	dm1.setEmail(decisionMakerEmail1);
	dm1.setMobile(decisionMakerMobile1);
	dm1.setName(decisionMakerName1);

	Set<ClientDecisionMaker> clientDecisionMakers = new HashSet<>();
	clientDecisionMakers.add(dm1);

	ClientInterviewerPanel cie1 = new ClientInterviewerPanel();
	cie1.setEmail(interviewerEmail1);
	cie1.setMobile(interviewerMobile1);
	cie1.setName(interviewerName1);

	Set<ClientInterviewerPanel> interviewers = new HashSet<>();
	interviewers.add(cie1);

	clientDto1.setClientDecisionMaker(clientDecisionMakers);
	clientDto1.setClientInterviewerPanel(interviewers);

	// client 2 data
	ClientDTO clientDto2 = new ClientDTO();
	clientDto2.setAddress("Hyderabad, Telangana, India");
	clientDto2.setClientName(clientName2);
	clientDto2.setEmpSize("50");
	clientDto2.setTurnOvr("50");
	clientDto2.setWebsite("http://www.recruiz.com");
	clientDto2.setClientLocation("Hyderabad, Telangana, India");
	clientDto2.setDummy(true);

	ClientDecisionMaker dm2 = new ClientDecisionMaker();
	dm2.setEmail(decisionMakerEmail2);
	dm2.setMobile(decisionMakerMobile2);
	dm2.setName(decisionMakerName2);

	Set<ClientDecisionMaker> clientDecisionMakers2 = new HashSet<>();
	clientDecisionMakers2.add(dm2);

	ClientInterviewerPanel cie2 = new ClientInterviewerPanel();
	cie2.setEmail(interviewerEmail2);
	cie2.setMobile(interviewerMobile2);
	cie2.setName(interviewerName2);

	Set<ClientInterviewerPanel> interviewers2 = new HashSet<>();
	interviewers2.add(cie2);

	clientDto2.setClientDecisionMaker(clientDecisionMakers2);
	clientDto2.setClientInterviewerPanel(interviewers2);

	// adding clients to DB
	Client c1 = clientService.addClientToDB(clientDto1);
	Client c2 = clientService.addClientToDB(clientDto2);

	List<Client> addedClients = new ArrayList<>();
	addedClients.add(c1);
	addedClients.add(c2);

	return addedClients;
    }

    // adding positions
    public List<Position> addPosition(Client c1, Client c2, String userEmail) throws RecruizException {

	String positionName1 = "Dummy_Java Developer";
	String positionName2 = "Dummy_Angular Developer";
	String positionName3 = "Dummy_UI Architect";
	String positionName4 = "Dummy_MySql Adviser";

	String description1 = "Description goes here...";
	String description2 = "Description goes here...";
	String description3 = "Description goes here...";
	String description4 = "Description goes here...";

	Set<String> educationalQual1 = new HashSet<>();
	educationalQual1.add("M. Tech");

	Set<String> educationalQual2 = new HashSet<>();
	educationalQual1.add("MSc");

	Set<String> educationalQual3 = new HashSet<>();
	educationalQual1.add("B Tech");

	Set<String> educationalQual4 = new HashSet<>();
	educationalQual1.add("MBA");

	String expRange1 = "3-5 Years";
	String expRange2 = "3-5 Years";
	String expRange3 = "3-5 Years";
	String expRange4 = "3-5 Years";

	Set<String> skills1 = new HashSet<>();
	educationalQual1.add("MBA");

	Set<String> skills2 = new HashSet<>();
	educationalQual1.add("MCA");

	Set<String> skills3 = new HashSet<>();
	educationalQual1.add("BE");

	Set<String> skills4 = new HashSet<>();
	educationalQual1.add("B Com");

	// position 1 data
	Position pos1 = new Position();
	pos1.addInterviewerPanel(c1.getClientInterviewerPanel());
	pos1.setClient(c1);
	pos1.setCloseByDate(new Date());
	pos1.setDescription(description1);
	pos1.setEducationalQualification(educationalQual1);
	pos1.setExperienceRange(expRange1);
	pos1.setFunctionalArea(CategoryOptions.Accounts_Finance_Tax_Company_Secretary_Audit.getDisplayName());
	pos1.setGoodSkillSet(skills1);
	pos1.setIndustry(IndustryOptions.Accounting_Finance.getDisplayName());
	pos1.setLocation("Bangalore, India");
	pos1.setMaxSal(1200000);
	pos1.setMinSal(500000);
	pos1.setNotes(description1);
	pos1.setOpenedDate(new Date());
	pos1.setOwner(userEmail);
	pos1.setRemoteWork(true);
	pos1.setReqSkillSet(skills1);
	pos1.setTitle(positionName1);
	pos1.setTotalPosition(5);
	Set<User> hr1 = new HashSet<>();
	hr1.add(userService.getUserByEmail(userEmail));
	pos1.setHrExecutives(hr1);
	pos1.setStatus(Status.Active.name());
	pos1.setSalUnit("Rupee");
	pos1.setPositionCode("pos_1");
	pos1.setDummy(true);
	pos1.setType(EmploymentType.FullTime.toString());

	// pos2 data
	Position pos2 = new Position();
	pos2.addInterviewerPanel(c1.getClientInterviewerPanel());
	pos2.setClient(c1);
	pos2.setCloseByDate(new Date());
	pos2.setDescription(description2);
	pos2.setEducationalQualification(educationalQual2);
	pos2.setExperienceRange(expRange2);
	pos2.setFunctionalArea(CategoryOptions.IT_Software_Application_Programming.getDisplayName());
	pos2.setGoodSkillSet(skills2);
	pos2.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	pos2.setLocation("Bangalore, India");
	pos2.setMaxSal(1200000);
	pos2.setMinSal(500000);
	pos2.setNotes(description2);
	pos2.setOpenedDate(new Date());
	pos2.setOwner(userEmail);
	pos2.setRemoteWork(true);
	pos2.setReqSkillSet(skills2);
	pos2.setTitle(positionName2);
	pos2.setTotalPosition(5);
	pos2.setStatus(Status.Active.name());
	pos2.setSalUnit("Rupee");
	pos2.setType(EmploymentType.FullTime.toString());

	Set<User> hr2 = new HashSet<>();
	hr2.add(userService.getUserByEmail(userEmail));
	pos2.setHrExecutives(hr2);
	pos2.setPositionCode("pos_2");
	pos2.setDummy(true);

	// position 3 data
	Position pos3 = new Position();
	pos3.addInterviewerPanel(c2.getClientInterviewerPanel());
	pos3.setClient(c2);
	pos3.setCloseByDate(new Date());
	pos3.setDescription(description3);
	pos3.setEducationalQualification(educationalQual3);
	pos3.setExperienceRange(expRange3);
	pos3.setFunctionalArea(CategoryOptions.IT_Software_Application_Programming.getDisplayName());
	pos3.setGoodSkillSet(skills3);
	pos3.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	pos3.setLocation("Bangalore, India");
	pos3.setMaxSal(1200000);
	pos3.setMinSal(500000);
	pos3.setNotes(description3);
	pos3.setOpenedDate(new Date());
	pos3.setOwner(userEmail);
	pos3.setRemoteWork(true);
	pos3.setReqSkillSet(skills3);
	pos3.setTitle(positionName3);
	pos3.setTotalPosition(5);
	Set<User> hr3 = new HashSet<>();
	hr3.add(userService.getUserByEmail(userEmail));
	pos3.setHrExecutives(hr3);
	pos3.setStatus(Status.Active.name());
	pos3.setSalUnit("Rupee");
	pos3.setPositionCode("pos_3");
	pos3.setDummy(true);
	pos3.setType(EmploymentType.FullTime.toString());

	// position 3 data
	Position pos4 = new Position();
	pos4.addInterviewerPanel(c2.getClientInterviewerPanel());
	pos4.setClient(c2);
	pos4.setCloseByDate(new Date());
	pos4.setDescription(description4);
	pos4.setEducationalQualification(educationalQual4);
	pos4.setExperienceRange(expRange4);
	pos4.setFunctionalArea(CategoryOptions.HR_Recruitment_Administration_IR.getDisplayName());
	pos4.setGoodSkillSet(skills4);
	pos4.setIndustry(IndustryOptions.ITES.getDisplayName());
	pos4.setLocation("Bangalore, India");
	pos4.setMaxSal(1200000);
	pos4.setMinSal(500000);
	pos4.setNotes(description4);
	pos4.setOpenedDate(new Date());
	pos4.setOwner(userEmail);
	pos4.setRemoteWork(true);
	pos4.setReqSkillSet(skills4);
	pos4.setTitle(positionName4);
	pos4.setTotalPosition(5);
	pos4.setStatus(Status.Active.name());
	pos4.setSalUnit("Rupee");
	Set<User> hr4 = new HashSet<>();
	hr4.add(userService.getUserByEmail(userEmail));
	pos4.setHrExecutives(hr4);
	pos4.setPositionCode("pos_4");
	pos4.setDummy(true);
	pos4.setType(EmploymentType.FullTime.toString());

	pos1 = positionService.addPositionToDB(pos1, userEmail);
	pos2 = positionService.addPositionToDB(pos2, userEmail);
	pos3 = positionService.addPositionToDB(pos3, userEmail);
	pos4 = positionService.addPositionToDB(pos4, userEmail);

	List<Position> addedPositions = new ArrayList<>();
	addedPositions.add(pos1);
	addedPositions.add(pos2);
	addedPositions.add(pos3);
	addedPositions.add(pos4);

	return addedPositions;

    }

    public List<Candidate> addCandidate() {

    	logger.error("###########call  queueParseResume() method from  DummyValueService.java #############");	
	String source = "Dummy Candidate";
	List<Candidate> candidates = new ArrayList<>();

	File file = new File(dbTemplateFolderPath);

	if (!file.exists()) {
	    logger.error(
		    "************Dummy CV Folder not found, exiting the process of adding dummy candidate**********");
	    return null;
	}

	Collection<File> files = FileUtils.listFiles(file, null, true);

	if (files != null && !files.isEmpty()) {
	    for (File cv : files) {
		try {
		    Candidate candidate = candidateService.addResumeFileAsCandidate(cv);
		    candidate.setDummy(true);
		    FileUploadRequestDTO fileUploadRequestDTO = new FileUploadRequestDTO();
		    fileUploadRequestDTO.setOverwrite(true);
		    fileUploadRequestDTO.setSource(source);
		    fileUploadRequestDTO.setFilebytes(FileUtils.readFileToByteArray(cv));
		    fileUploadRequestDTO.setFileName(cv.getName());
		    fileUploadRequestDTO.setSourceDetails(source);

		    candidateService.saveCandidateToDB(candidate, fileUploadRequestDTO);
		    candidates.add(candidate);

		} catch (Exception ex) {
		    // logger.info(ex.getMessage(), ex);
		}
	    }
	}

	return candidates;
    }

    // to called from sign up validate request (activate account)
    @Transactional
    public void addDummyData(String userEmail) throws Exception {
	try {
	    List<Client> clients = addClient();
	    List<Position> positions = addPosition(clients.get(0), clients.get(1), userEmail);
	    List<Candidate> candidates = addCandidate();

	    sourceToBoard(positions, candidates);
	} catch (RecruizException e) {
	    logger.info(e.getMessage(), e);
	}
    }

    @Transactional
    private void sourceToBoard(List<Position> positions, List<Candidate> candidates) throws Exception {
	if (candidates != null && !candidates.isEmpty()) {
	    for (Position position : positions) {
		CandidateToRoundDTO roundCandidateDto = new CandidateToRoundDTO();
		roundCandidateDto.setPositionCode(position.getPositionCode());

		List<String> candidateEmail = new ArrayList<>();
		for (Candidate candidate : candidates) {
		    candidateEmail.add(candidate.getEmail());
		}
		roundCandidateDto.setCandidateEmailList(candidateEmail);

		roundCandidateService.addCandidateToPosition(roundCandidateDto, "Dummy");
	    }
	}
    }

    // to called from sign up validate request (activate account)
    public void addDummyData(String userEmail, List<Candidate> candidates, List<Position> positions) throws Exception {
	try {
	    sourceToBoard(positions, candidates);
	} catch (RecruizException e) {
	    logger.info(e.getMessage(), e);
	}
    }

    @Transactional
    public void deleteDummyData() throws RecruizException {
	List<Candidate> dummyCandidates = candidateService.getDummyCandidate();
	if (dummyCandidates != null && !dummyCandidates.isEmpty()) {
	    for (Candidate candidate : dummyCandidates) {
		List<AgencyInvoice> candidateInvoices = agencyInvoiceService.getCandidateInvoices(candidate);
		if (candidateInvoices != null && !candidateInvoices.isEmpty()) {
		    agencyInvoiceService.delete(candidateInvoices);
		}
		List<CandidateStatus> candidateStatus = candidateStatusService.getByCandidate(candidate);
		if (candidateStatus != null && !candidateStatus.isEmpty()) {
		    candidateStatusService.delete(candidateStatus);
		}
		candidateActivityService.deleteByCandidateId(candidate.getCid());

		List<RoundCandidate> roundCandidates = roundCandidateService.getAllRoundCandidates(candidate);
		if (roundCandidates != null && !roundCandidates.isEmpty()) {
		    roundCandidateService.delete(roundCandidates);
		}
	    }
	    candidateService.delete(dummyCandidates);
	}

	List<Position> positions = positionService.getDummyPositions();
	if (positions != null && !positions.isEmpty()) {
	    for (Position position : positions) {

		Set<ClientInterviewerPanel> interviewers = position.getInterviewers();
		if (null != interviewers && !interviewers.isEmpty()) {
		    for (ClientInterviewerPanel clientInterviewerPanel : interviewers) {
			if (clientInterviewerPanel.getEmail().toLowerCase().startsWith("dummy")) {
			    GenericInterviewer genInterviewer = genericInterviewerService
				    .getInterviewerByEmail(clientInterviewerPanel.getEmail());
			    if (null != genInterviewer) {
				genericInterviewerService.delete(genInterviewer);
			    }
			}
		    }
		}

		candidateStatusService.deleteByPositionId(position.getId());
		notificationService.deleteByPositionCode(position.getPositionCode());
		interviewScheduleService.deleteByPositioncode(position.getPositionCode());
	    }
	    positionService.delete(positions);
	}

	List<Client> clients = clientService.getDummyClient();
	if (clients != null && !clients.isEmpty()) {
	    for (Client client : clients) {
		List<AgencyInvoice> agencyInvoices = agencyInvoiceService.getAllInvoiceByClient(client.getClientName());
		if (agencyInvoices != null) {
		    agencyInvoiceService.delete(agencyInvoices);
		}

		List<Position> positionList = positionService.getAllPositionByClient(client);
		for (Position position : positionList) {

		    // deleting generic interviewer who is added to this
		    // position
		    Set<ClientInterviewerPanel> interviewers = position.getInterviewers();
		    if (null != interviewers && !interviewers.isEmpty()) {
			for (ClientInterviewerPanel clientInterviewerPanel : interviewers) {
			    if (clientInterviewerPanel.getEmail().toLowerCase().startsWith("dummy")) {
				GenericInterviewer genInterviewer = genericInterviewerService
					.getInterviewerByEmail(clientInterviewerPanel.getEmail());
				if (null != genInterviewer) {
				    genericInterviewerService.delete(genInterviewer);
				}
			    }
			}
		    }
		    candidateStatusService.deleteByPositionId(position.getId());
		    positionService.deletePosition(position.getId());
		}

		notificationService.deleteByClientId(client.getId());

		// deleting client generic decision maker
		Set<ClientDecisionMaker> dms = client.getClientDecisionMaker();
		if (null != dms && !dms.isEmpty()) {
		    for (ClientDecisionMaker clientDecisionMaker : dms) {
			GenericDecisionMaker genDM = genericDecisionMakerService
				.getDecisionMakerByEmail(clientDecisionMaker.getEmail());
			if (null != genDM) {
			    genericDecisionMakerService.delete(genDM);
			}
		    }
		}
	    }

	    clientService.delete(clients);
	}

	List<Prospect> dummyProspects = prospectService.getDummyProspect();
	prospectService.delete(dummyProspects);
    }

	public void addDummyInvoice() throws RecruizException {

		String clientName = "Dummy_Auk Labs Inc.";
		String positionCode = "pos_1";

		List<CandidateStatus> candidateStatus = candidateStatusService
				.getJoinedCandidateStatusByClientNameAndPositionCodeAndStatus(clientName, positionCode,
						BoardStatus.Joined.getDisplayName());
		List<JoinedCandidateDTO> joinedCandidateDTOs = agencyInvoiceService.convertCandidateStatus(candidateStatus);
		int i = 1;
		for (JoinedCandidateDTO joinedCandidateDTO : joinedCandidateDTOs) {

			Set<CandidateInvoice> candidateInvoices = new HashSet<CandidateInvoice>();
			AgencyInvoice agencyInvoice = new AgencyInvoice();
			CandidateInvoice candidateInvoice = new CandidateInvoice(joinedCandidateDTO.getFullName(),
					joinedCandidateDTO.getEmail(), joinedCandidateDTO.getPostionTitle(),
					joinedCandidateDTO.getPositionCode(), joinedCandidateDTO.getJoinedDate());
			candidateInvoice.setAmount(20000);
			candidateInvoice.setAgencyInvoice(agencyInvoice);
			candidateInvoice.setClientId(joinedCandidateDTO.getClientId());
			candidateInvoice.setClientName(joinedCandidateDTO.getClientName());
			candidateInvoices.add(candidateInvoice);

			agencyInvoice.setInvoiceId(agencyInvoiceService.getNextInvoiceId());
			agencyInvoice.setInvoiceNumber("AA100111" + i);
			agencyInvoice.setClientName(joinedCandidateDTO.getClientName());
			agencyInvoice.setClientId(joinedCandidateDTO.getClientId());
			org.joda.time.DateTime dateTime = new org.joda.time.DateTime().plusDays(30);
			agencyInvoice.setDueDate(dateTime.toDate());
			agencyInvoice.setCurrency(Currency.Rupee.toString());
			agencyInvoice.setCreationDate(new Date());
			agencyInvoice.setModificationDate(new Date());
			agencyInvoice.setTotalAmount(24000);

			agencyInvoice.setTotalAmountAfterDiscount(Double.parseDouble(new DecimalFormat("##.##").format(22000)));

			agencyInvoice.setDiscount(20);
			agencyInvoice.setInformationFilledByUser(userService.getLoggedInUserEmail());
			agencyInvoice.setInvoiceStatus(InvoiceStatus.Pending.getDisplayName());
			agencyInvoice.setCandidateInvoices(candidateInvoices);
			agencyInvoice.setAmount(20000);
			Organization org = organizationService.findByOrgId(TenantContextHolder.getTenant());
			agencyInvoice.setOrganizationName(org.getOrgName());
			agencyInvoice.setOrganization_address_1("2nd Main Road");
			agencyInvoice.setOrganization_address_2("Kasturi Nagar");
			agencyInvoice.setOrganizationCity("Bangalore");
			agencyInvoice.setOrganizationState("Karnataka");
			agencyInvoice.setOrganizationCountry("India");
			agencyInvoice.setOrganizationPin("560043");
			agencyInvoice.setOrganizationPhone("8041515177");
			agencyInvoice.setChequePayable(org.getOrgName());
			agencyInvoice.setOrganizationAccountName(org.getOrgName());
			agencyInvoice.setOrganizationAccountNumber("2339050001111");
			agencyInvoice.setOrganizationBankName("ICICI");
			agencyInvoice.setOrganizationBankBranchName("Kasturinagar");
			agencyInvoice.setOrganizationBankIfsc("ICIC0002339");
			agencyInvoice.setNote("Please note that the invoice has to be cleared within 60days.");
			agencyInvoice.setBillClientName("Dummy Beyond Bytes Technologies");
			agencyInvoice.setBillContactName("Shwetha");
			agencyInvoice.setBill_address_1("4th Main Road");
			agencyInvoice.setBill_address_2("Kasturi Nagar");
			agencyInvoice.setBillCity("Bangalore");
			agencyInvoice.setBillState("Karnataka");
			agencyInvoice.setBillCountry("India");
			agencyInvoice.setBillPin("560012");
			agencyInvoice.setBillPhone("8011111111");
			Map<String, Double> taxDetails = new HashMap<String, Double>();
			taxDetails.put("CGST", 10.00);
			taxDetails.put("SGST", 10.00);

			Map<String, String> taxRelatedDetails = new HashMap<String, String>();
			taxRelatedDetails.put("GSTIN", "29AADCB6899C2ZA");
			taxRelatedDetails.put("PAN", "AADCB6899C");

			agencyInvoice.setTaxDetails(taxDetails);
			agencyInvoice.setTaxRelatedDetails(taxRelatedDetails);
			agencyInvoice = agencyInvoiceService.save(agencyInvoice);
			i++;

			InvoiceSettings invoiceSettings = invoiceSettingsService.getInvoiceSettings();
			if (invoiceSettings == null) {
				invoiceSettings = new InvoiceSettings();
			}

			invoiceSettings.setOrganizationName(agencyInvoice.getOrganizationName());
			invoiceSettings.setOrganization_address_1(agencyInvoice.getOrganization_address_1());
			invoiceSettings.setOrganization_address_2(agencyInvoice.getOrganization_address_2());
			invoiceSettings.setOrganizationCity(agencyInvoice.getOrganizationCity());
			invoiceSettings.setOrganizationState(agencyInvoice.getOrganizationState());
			invoiceSettings.setOrganizationCountry(agencyInvoice.getOrganizationCountry());
			invoiceSettings.setOrganizationPin(agencyInvoice.getOrganizationPin());
			invoiceSettings.setOrganizationPhone(agencyInvoice.getOrganizationPhone());
			invoiceSettings.setChequePayable(agencyInvoice.getChequePayable());
			invoiceSettings.setOrganizationAccountName(agencyInvoice.getOrganizationAccountName());
			invoiceSettings.setOrganizationAccountNumber(agencyInvoice.getOrganizationAccountNumber());
			invoiceSettings.setOrganizationBankName(agencyInvoice.getOrganizationBankName());
			invoiceSettings.setOrganizationBankBranchName(agencyInvoice.getOrganizationBankBranchName());
			invoiceSettings.setOrganizationBankIfsc(agencyInvoice.getOrganizationBankIfsc());
			invoiceSettings.setNote(agencyInvoice.getNote());
			invoiceSettings.setBillClientName(agencyInvoice.getBillClientName());
			invoiceSettings.setBillContactName(agencyInvoice.getBillContactName());
			invoiceSettings.setBill_address_1(agencyInvoice.getBill_address_1());
			invoiceSettings.setBill_address_2(agencyInvoice.getBill_address_2());
			invoiceSettings.setBillCity(agencyInvoice.getBillCity());
			invoiceSettings.setBillState(agencyInvoice.getBillState());
			invoiceSettings.setBillCountry(agencyInvoice.getBillCountry());
			invoiceSettings.setBillPin(agencyInvoice.getBillPin());
			invoiceSettings.setBillPhone(agencyInvoice.getBillPhone());
			invoiceSettingsService.save(invoiceSettings);

			for (Map.Entry<String, String> entry : taxRelatedDetails.entrySet()) {
				if (taxService.isTaxNameExist(entry.getKey())) {
					Tax tax = taxService.getByTaxName(entry.getKey());
					tax.setTaxNumber(entry.getValue());
					taxService.save(tax);
				} else {
					Tax tax = new Tax();
					tax.setTaxName(entry.getKey());
					tax.setTaxNumber(entry.getValue());
					taxService.save(tax);
				}
			}
		}
	}

    public void addDummyProspect() throws RecruizException {

	Prospect dummyProspect1 = new Prospect();
	dummyProspect1.setCompanyName("DummyProspect-1");
	dummyProspect1.setName("dummy-1");
	dummyProspect1.setEmail("vishal@gmail.com");
	dummyProspect1.setMobile("9912356789");
	dummyProspect1.setOwner(userService.getLoggedInUserEmail());
	dummyProspect1.setDesignation("HR Manager");
	dummyProspect1.setLocation("Bangalore, Karnataka, India");
	dummyProspect1.setAddress("KasthuriNagar Main Road");
	dummyProspect1.setSource("Advertisment");
	dummyProspect1.setMode("app");
	dummyProspect1.setWebsite("www.dummyprospect.com");
	dummyProspect1.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	dummyProspect1.setCategory(CategoryOptions.IT_Software_Mobile.getDisplayName());
	dummyProspect1.setProspectRating(0);
	dummyProspect1.setDealSize(0);
	dummyProspect1.setPercentage(0);
	dummyProspect1.setValue(0);
	dummyProspect1.setDummy(true);
	dummyProspect1.setCurrency(Currency.Rupee.toString());
	dummyProspect1 = prospectService.save(dummyProspect1);

	Prospect dummyProspect2 = new Prospect();
	dummyProspect2.setCompanyName("DummyProspect-2");
	dummyProspect2.setName("dummy-1");
	dummyProspect2.setEmail("skumar@gmail.com");
	dummyProspect2.setMobile("9915356789");
	dummyProspect2.setOwner(userService.getLoggedInUserEmail());
	dummyProspect2.setDesignation("Director");
	dummyProspect2.setLocation("Bangalore, Karnataka, India");
	dummyProspect2.setAddress("KasthuriNagar Main Road");
	dummyProspect2.setSource("Advertisment");
	dummyProspect2.setMode("app");
	dummyProspect2.setWebsite("www.dummyprospectdata.com");
	dummyProspect2.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	dummyProspect2.setCategory(CategoryOptions.IT_Software_Mobile.getDisplayName());
	dummyProspect2.setProspectRating(0);
	dummyProspect2.setDealSize(0);
	dummyProspect2.setPercentage(0);
	dummyProspect2.setValue(0);
	dummyProspect2.setDummy(true);
	dummyProspect2.setCurrency(Currency.Rupee.toString());
	dummyProspect2 = prospectService.save(dummyProspect2);

	Set<String> keySkill = new HashSet<String>();
	keySkill.add("Java");
	keySkill.add("Spring");

	Set<String> educationQualification = new HashSet<String>();
	educationQualification.add("MCA");
	educationQualification.add("ME");
	educationQualification.add("BCA");
	educationQualification.add("BE");

	List<ProspectPostionDTO> dummyProspectPostionDTOs1 = new ArrayList<ProspectPostionDTO>();
	ProspectPostionDTO dummyProspectPostionDTO1 = new ProspectPostionDTO();
	dummyProspectPostionDTO1.setPositionName("Team Lead");
	DateTime dateTime = new DateTime().plusDays(25);
	dummyProspectPostionDTO1.setClosureDate(dateTime.toDate());
	dummyProspectPostionDTO1.setNumberOfOpenings(2);
	dummyProspectPostionDTO1.setMinExperience(5.0);
	dummyProspectPostionDTO1.setMaxExperience(10.0);
	dummyProspectPostionDTO1.setKeySkills(keySkill);
	dummyProspectPostionDTO1.setLocation("Bangalore, Karnataka, India");
	dummyProspectPostionDTO1.setEducationQualification(educationQualification);
	dummyProspectPostionDTO1.setType("FullTime");
	dummyProspectPostionDTO1.setRemoteWork(false);
	dummyProspectPostionDTO1.setMinSal(1000000);
	dummyProspectPostionDTO1.setMaxSal(2000000);
	dummyProspectPostionDTO1.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	dummyProspectPostionDTO1.setFunctionalArea(CategoryOptions.IT_Software_Mobile.getDisplayName());
	dummyProspectPostionDTO1.setCurrency(Currency.Rupee.toString());
	dummyProspectPostionDTOs1.add(dummyProspectPostionDTO1);

	List<ProspectPostionDTO> dummyProspectPostionDTOs2 = new ArrayList<ProspectPostionDTO>();
	ProspectPostionDTO dummyProspectPostionDTO2 = new ProspectPostionDTO();
	dummyProspectPostionDTO2.setPositionName("Team Lead");
	dummyProspectPostionDTO2.setClosureDate(dateTime.toDate());
	dummyProspectPostionDTO2.setNumberOfOpenings(2);
	dummyProspectPostionDTO2.setMinExperience(5.0);
	dummyProspectPostionDTO2.setMaxExperience(10.0);
	dummyProspectPostionDTO2.setKeySkills(keySkill);
	dummyProspectPostionDTO2.setLocation("Bangalore, Karnataka, India");
	dummyProspectPostionDTO2.setEducationQualification(educationQualification);
	dummyProspectPostionDTO2.setType("FullTime");
	dummyProspectPostionDTO2.setRemoteWork(false);
	dummyProspectPostionDTO2.setMinSal(1000000);
	dummyProspectPostionDTO2.setMaxSal(2000000);
	dummyProspectPostionDTO2.setIndustry(IndustryOptions.IT_SW.getDisplayName());
	dummyProspectPostionDTO2.setFunctionalArea(CategoryOptions.IT_Software_Mobile.getDisplayName());
	dummyProspectPostionDTO2.setCurrency(Currency.Rupee.toString());
	dummyProspectPostionDTOs2.add(dummyProspectPostionDTO2);

	prospectPositionService.addPositionInProspect(dummyProspect1.getProspectId(), dummyProspectPostionDTOs1);
	prospectPositionService.addPositionInProspect(dummyProspect2.getProspectId(), dummyProspectPostionDTOs2);

    }

}
