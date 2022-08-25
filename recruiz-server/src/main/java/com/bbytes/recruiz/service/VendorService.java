package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.EmailValidator;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Board;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateActivity;
import com.bbytes.recruiz.domain.Permission;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.VendorPermission;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.VendorRepository;
import com.bbytes.recruiz.rest.dto.models.BoardDTO;
import com.bbytes.recruiz.rest.dto.models.CandidateToRoundDTO;
import com.bbytes.recruiz.rest.dto.models.InviteUser;
import com.bbytes.recruiz.rest.dto.models.OrganizationUserDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.RoundCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.RoundResponseDTO;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.bbytes.recruiz.utils.ValidateEmailDomain;

@Service
public class VendorService extends AbstractService<Vendor, Long> {

	private VendorRepository vendorRepository;

	@Autowired
	public VendorService(VendorRepository vendorRepository) {
		super(vendorRepository);
		this.vendorRepository = vendorRepository;
	}

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private UserService userService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;
	
	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private PasswordHashService passwordHashService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private PositionService positionServices;

	@Autowired
	private BoardService boardService;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${email.inviteToJoin.subject}")
	private String inviteToJoinSubject;

	@Value("${email.inviteToSignup.subject}")
	private String inviteToSignupSubject;

	@Value("${email.reinviteToJoin.subject}")
	private String reInviteToJoinSubject;

	@Value("${email.reinviteToSignup.subject}")
	private String reInviteToSignupSubject;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Transactional
	public Vendor inviteVendor(Vendor vendorToAdd) throws RecruizException {

		// checking if email is registered for some vendor in this organization
		User existingVendorUser = userService.getUserByEmail(vendorToAdd.getEmail());
		Vendor existingVendor = vendorRepository.findByEmail(vendorToAdd.getEmail());
		Vendor vendor = new Vendor();

		if (existingVendorUser != null) {
			throw new RecruizWarnException(ErrorHandler.EMAIL_IN_USE, ErrorHandler.EMAIL_EXISTS);
		}

		User globalUser = tenantResolverService.findUserByEmail(vendorToAdd.getEmail());

		User user = userService.getLoggedInUserObject();
		// if (!tenantResolverService.isVendorExists(vendorToAdd.getEmail())) {

		final String inviteToSignup = GlobalConstants.INVITE_VENDOR_TO_SIGNUP_EMAIL_TEMPLATE;

		final String inviteToJoinTemplate = GlobalConstants.INVITE_VENDOR_TO_JOIN_EMAIL_TEMPLATE;

		// creating vendor object here
		vendor.setEmail(vendorToAdd.getEmail());
		vendor.setName(vendorToAdd.getName());
		vendor.setType(vendorToAdd.getType());
		vendor.setAddress(vendorToAdd.getAddress());
		vendor.setPhone(vendorToAdd.getPhone());
		vendor.setOrganization(user.getOrganization());
		save(vendor);

		UserRole vendorRole = createVendorRole();
		if (globalUser == null) {
			User vendorUser = new User();
			vendorUser.setName(vendorToAdd.getName());
			vendorUser.setEmail(vendorToAdd.getEmail());
			vendorUser.setLocale(user.getLocale());
			vendorUser.setTimezone(user.getTimezone());
			vendorUser.setUserType(GlobalConstants.USER_TYPE_VENDOR);
			vendorUser.setUserRole(vendorRole);
			vendorUser.setOrganization(user.getOrganization());
			vendorUser.setAccountStatus(true);
			vendorUser.setJoinedStatus(false);
			vendorUser.setVendorId(vendor.getId() + "");
			userService.save(vendorUser);

			inviteToSignUp(vendorToAdd.getEmail(), inviteToSignup);
		} else {
			List<User> invitedUser = new ArrayList<>();
			inviteToJoin(vendor, inviteToJoinTemplate, invitedUser, user, vendorToAdd.getEmail());
		}

		return vendor;
		// }

	}

	/**
	 * Send invition to vendor to join.
	 * 
	 * @param email
	 * @param template
	 * @throws RecruizException
	 */
	private void inviteToSignUp(String email, String template) throws RecruizException {
		int index = email.indexOf("@");
		String name = email.substring(0, index);

		List<String> emailList = new ArrayList<String>();
		emailList.add(email);

		String key = email + ":" + userService.getLoggedInUserObject().getOrganization().getOrgId() + ":"
				+ GlobalConstants.SIGNUP_MODE_VENDOR;
		String encryptedKey = Base64.encodeBase64URLSafeString(key.getBytes());

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, name);
		emailBody.put(GlobalConstants.ORG_NAME, userService.getLoggedInUserObject().getOrganization().getOrgName());
		emailBody.put(GlobalConstants.ACTIVATION_LINK,
				baseUrl + GlobalConstants.INVITE_SIGNUP_URL + GlobalConstants.PASSKEY + encryptedKey);

		emailBody.put(GlobalConstants.USER_EMAIL, email);
		emailBody.put(GlobalConstants.FIRST_PASSWORD, GlobalConstants.DEFAULT_PASSWORD);

		emailService.sendEmail(template, emailList, inviteToJoinSubject, emailBody);
	}

	/**
	 * this will set the tenant and will create a vendor object as well
	 * 
	 * @param invitedUserDTO
	 * @return
	 * @throws RecruizException
	 */
	public User signup(OrganizationUserDTO invitedUserDTO) throws RecruizException {
		TenantContextHolder.setTenant(invitedUserDTO.getOrgID());
		return checkAndUpdateVendorUser(invitedUserDTO);
	}

	/**
	 * post registration
	 * 
	 * @param invitedUserDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	private User checkAndUpdateVendorUser(OrganizationUserDTO invitedUserDTO) throws RecruizException {
		if (tenantResolverService.findByEmailAndOrgID(invitedUserDTO.getEmail(), invitedUserDTO.getOrgID()) != null)
			throw new RecruizWarnException(ErrorHandler.USER_ALREADY_REGISTERED, ErrorHandler.ALREADY_REGISTERED);
		User user = userService.getUserByEmail(invitedUserDTO.getEmail());
		if (user != null && !user.getJoinedStatus()) {
			int index = invitedUserDTO.getEmail().indexOf("@");
			String name = invitedUserDTO.getEmail().substring(0, index);

			user.setJoinedStatus(true);
			user.setName(name);
			user.setPassword(passwordHashService.encodePassword(invitedUserDTO.getPassword()));
			user.setTimezone(invitedUserDTO.getTimezone());
			user.setLocale(invitedUserDTO.getLocale());
			user.setUserType(GlobalConstants.USER_TYPE_VENDOR);
			user.setOrganization(user.getOrganization());
			userService.save(user);
			tenantResolverService.saveTenantResolverForUser(user);
		} else {
			throw new RecruizException(ErrorHandler.USER_NOT_FOUND, ErrorHandler.USER_NOT_FOUND);
		}
		return user;
	}

	/**
	 * If no vendor role create one
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public UserRole createVendorRole() throws RecruizException {
		UserRole vendorRole = new UserRole();
		vendorRole.getPermissions().add(new Permission(VendorPermission.PositionDetails.getDisplayName()));
		vendorRole.getPermissions().add(new Permission(VendorPermission.CandidateDetails.getDisplayName()));
		vendorRole.getPermissions().add(new Permission(VendorPermission.ViewBoard.getDisplayName()));
		vendorRole.setRoleName(GlobalConstants.VENDOR_ROLE);
		if (userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE) == null) {
			userRoleService.save(vendorRole);
		} else {
			vendorRole = userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE);
		}
		return vendorRole;
	}

	@Transactional
	public List<Vendor> getAllVendor() {
		List<Vendor> vendorList = vendorRepository.findAll();
		if (vendorList != null && !vendorList.isEmpty()) {
			Collections.sort(vendorList, new Comparator<Vendor>() {
				public int compare(Vendor v1, Vendor v2) {

					int res = String.CASE_INSENSITIVE_ORDER.compare(v1.getName(), v2.getName());
					if (res == 0) {
						res = v1.getName().compareTo(v2.getName());
					}
					return res;
				}
			});
			return vendorList;
		}
		return vendorList;
	}

	/**
	 * Get all active vendors
	 * 
	 * @return
	 */
	@Transactional
	public List<Vendor> getAllActiveVendor() {
		List<Vendor> vendorList = vendorRepository.findByStatus(true);
		if (vendorList != null && !vendorList.isEmpty()) {
			Collections.sort(vendorList, new Comparator<Vendor>() {
				public int compare(Vendor v1, Vendor v2) {

					int res = String.CASE_INSENSITIVE_ORDER.compare(v1.getName(), v2.getName());
					if (res == 0) {
						res = v1.getName().compareTo(v2.getName());
					}
					return res;
				}
			});

			// will return only those vendors whose account is activated
			List<Vendor> activeVendorList = new ArrayList<>();
			if (vendorList != null && !vendorList.isEmpty()) {
				for (Vendor vendor : vendorList) {
					User vendorSuperUser = userService.getUserByEmail(vendor.getEmail());
					if (vendorSuperUser != null && vendorSuperUser.getJoinedStatus()
							&& vendorSuperUser.getAccountStatus()) {
						activeVendorList.add(vendor);
					}
				}
			}

			return activeVendorList;
		}
		return vendorList;
	}

	/**
	 * get list of all position for logged in vendor
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Page<Position> getLoggedInVendorPosition(Pageable pageable) throws RecruizException {
		// getting vendor for Logged in User
		Vendor vendor = userService.getLoggedInUserVendor();

		Page<Position> positionList = positionService.getVendorPosition(vendor, pageable);
		if (positionList.getContent() != null && !positionList.getContent().isEmpty()) {
			for (Position position : positionList) {
				position.getEducationalQualification().size();
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
			}
		}
		positionService.calculateFinalStatusForPositions(positionList.getContent());
		return positionList;
	}

	@Transactional
	public List<Position> getLoggedInVendorPosition() throws RecruizException {
		// getting vendor for Logged in User
		Vendor vendor = userService.getLoggedInUserVendor();

		List<Position> positionList = positionService.getVendorPosition(vendor);
		if (positionList != null && !positionList.isEmpty()) {
			for (Position position : positionList) {
				position.getEducationalQualification().size();
				position.getGoodSkillSet().size();
				position.getReqSkillSet().size();
			}
		}
		positionService.calculateFinalStatusForPositions(positionList);
		return positionList;
	}

	/**
	 * Source vendor candidate to Recruiz
	 * 
	 * @param candidate
	 * @param multiPartFile
	 * @param fileName
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	@Transactional
	public Candidate sourceVendorCandidate(Candidate candidate, File resumeFile, String fileName, String positionCode)
			throws RecruizException, IOException {
		candidate = candidateService.setDefaultValues(candidate);

		// setting sourced info to candidate
		candidate.setSource(GlobalConstants.SOURCE_INFO_VENDOR);
		candidate.setSourceDetails(userService.getLoggedInUserVendor().getName());
		candidate.setSourceEmail(userService.getLoggedInUserVendor().getEmail());
		candidate.setSourceMobile(userService.getLoggedInUserVendor().getPhone());
		candidate.setSourceName(userService.getLoggedInUserVendor().getName());
		candidate.setSourcedOnDate(new Date());
		candidate.setCreationDate(new Date());
		candidate.setModificationDate(new Date());

		candidateService.addVendorCandidate(candidate);

		// if (resumeFile != null && resumeFile.exists()) {
		// uploadFileService.createFolderStructureForCandidate(folderPath,
		// candidate.getCid() + "");
		// String serverPath =
		// uploadFileService.uploadFileToLocalServer(resumeFile, fileName,
		// "resume",
		// candidate.getCid() + "");
		//
		// String pdfFilePath = fileService.convert(serverPath);
		//
		// candidate.setResumeLink(pdfFilePath);
		// candidateService.save(candidate);
		// }

		// adding profile pic o public folder
		if ((candidate.getImageContent() != null && !candidate.getImageContent().isEmpty())
				&& (candidate.getImageName() != null && !candidate.getImageName().isEmpty())) {
			byte[] imageBytes = org.springframework.security.crypto.codec.Base64
					.decode(candidate.getImageContent().getBytes());
			if (imageBytes != null && imageBytes.length > 0) {

				File publicProfilePath = new File(
						publicFolder + "/" + TenantContextHolder.getTenant() + "/candidate/" + candidate.getCid());

				if (!publicProfilePath.exists())
					org.apache.commons.io.FileUtils.forceMkdir(publicProfilePath);

				File logoFile = new File(publicProfilePath + "/" + candidate.getImageName());

				org.apache.commons.io.FileUtils.writeByteArrayToFile(logoFile, imageBytes);
				String dpPath = logoFile.getAbsolutePath().replace(publicFolder, "");
				// String logoPath = logoFile.getAbsolutePath().substring(index,
				// logoFile.getAbsolutePath().length());
				if (dpPath != null && !dpPath.isEmpty()) {
					candidate.setProfileUrl(dpPath);
					candidateService.save(candidate);
				}
			}
		}

		// adding cover letter here
		candidateFileService.uploadCandidateCoverLetter(candidate);

		addCandidateToPosition(candidate, positionCode);
		return candidate;
	}

	// add candidate to position
	public boolean addCandidateToPosition(Candidate candidate, String positionCode) throws RecruizException {
		if (positionCode == null || positionCode.trim().isEmpty())
			return false;

		Round round = roundService.getRoundByBoardAndType(positionService.getPositionBoard(positionCode),
				RoundType.Source.toString());
		RoundCandidate roundCandidate = new RoundCandidate();
		roundCandidate.setCandidate(candidate);
		roundCandidate.setStatus(BoardStatus.YetToProcess.toString());
		roundCandidate.setRoundId(round.getId() + "");
		roundCandidate.setRound(round);
		roundCandidate.setSourcedBy(userService.getLoggedInUserEmail());
		roundCandidate.setPositionCode(positionService.getPositionByBoard(round.getBoard()).getPositionCode());
		if (!roundCandidateService.isRoundCandidateExist(candidate, round.getId() + "")) {
			roundCandidateService.save(roundCandidate);
			// making entry to candidate activity
			candidateActivityService.addActivity(
					"Added to board for position : " + roundCandidate.getPositionCode() + " by vendor : "
							+ userService.getLoggedInUserVendor().getName(),
					userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
					candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());
		} else {
			return false;
		}

		Position position = positionService.getPositionByCode(positionCode);

		// send email to Hr Executive here
		Set<User> positionHR = new HashSet<User>();
		positionHR = position.getHrExecutives();
		positionHR.add(userService.getUserByEmail(position.getOwner()));
		List<String> emailList = new ArrayList<>();
		for (User user : positionHR) {
			emailList.add(user.getEmail());
		}

		// sending email to HR executive on sourcing candidate
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_SOURCE_CANDIDATE_REPLY_VENDOR;
		String emailSubject = "New Candidate '" + candidate.getFullName() + "' sourced from Vendor "
				+ userService.getLoggedInUserVendor().getName();
		Map<String, Object> bodyMap = emailTemplateDataService.getEmailBodyValueMapForCandidateSourceEmailForHr(
				position, candidate, userService.getLoggedInUserVendor().getName());
		String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
		String link = baseUrl + GlobalConstants.BOARD_URL + position.getPositionCode() + "/";
		String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString, link,
				"View");
		emailService.sendEmail(emailList, masterRenderedTemplate, emailSubject, true);

		return true;
	}

	/**
	 * Get all candidate for vendor
	 * 
	 * @param pageable
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Page<Candidate> getVendorCandidate(Pageable pageable) throws RecruizException {
		Page<Candidate> candidate = candidateService.getVendorCandidate(userService.getLoggedInUserVendor().getEmail(),
				pageable);
		return candidate;
	}

	/**
	 * Get candidate details for vendor
	 * 
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Candidate getVendorCandidateDetails(String email) throws RecruizException {
		Candidate candidate = candidateService.getCandidateByFirstEmail(email);
		if (candidate != null
				&& candidate.getSourceEmail().equalsIgnoreCase(userService.getLoggedInUserVendor().getEmail())) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		} else {
			throw new RecruizWarnException(ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.CANDIDATE_NOT_FOUND);
		}
		return candidate;
	}

	/**
	 * get board for vendors
	 * 
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public BoardDTO getVendorBoard(String positionCode) throws RecruizException {

		Position position = positionService.getPositionByCode(positionCode);
		positionService.calculateFinalStatusForPosition(position);

		BoardDTO boardDTO = new BoardDTO();
		RoundResponseDTO roundDTO;
		RoundCandidateDTO candidateDTO;
		Board positionBoard = positionServices.getPositionBoard(positionCode);
		boardDTO.setBoardId(positionBoard.getId() + "");
		boardDTO.setCreatedDate(positionBoard.getCreationDate());
		boardDTO.setPositionName(positionServices.getPositionByCode(positionCode).getTitle());
		boardDTO.setClientName(positionServices.getPositionByCode(positionCode).getClient().getClientName());
		boardDTO.setPositionStatus(position.getFinalStatus());
		boardDTO.setClientStatus(positionBoard.getClientStatus());
		boardDTO.setPositionCode(positionCode);
		boardDTO.setPositionId(positionServices.getPositionByCode(positionCode).getId() + "");
		boardDTO.setClientId(positionServices.getPositionByCode(positionCode).getClient().getId() + "");

		for (Round round : positionBoard.getRounds()) {
			roundDTO = new RoundResponseDTO();
			roundDTO.setRoundId(round.getId() + "");
			roundDTO.setName(round.getRoundName());
			roundDTO.setType(round.getRoundType());
			roundDTO.setOrderNo(round.getOrderNo());

			if (round.getCandidates() != null && !round.getCandidates().isEmpty()) {
				double index = 1;
				for (RoundCandidate roundCandidate : round.getCandidates()) {
					Candidate candidate = roundCandidate.getCandidate();
					candidateService.setCandidatePublicProfileUrl(candidate);
					if (candidate.getSourceEmail() != null && candidate.getSourceEmail()
							.equalsIgnoreCase(userService.getLoggedInUserVendor().getEmail())) {
						candidateDTO = new RoundCandidateDTO();
						candidateDTO.setRoundCandidateId(roundCandidate.getId() + "");
						candidateDTO.setEmail(candidate.getEmail());
						candidateDTO.setId(candidate.getCid() + "");
						candidateDTO.setLocation(candidate.getCurrentLocation());
						candidateDTO.setName(candidate.getFullName());
						candidateDTO.setMobile(candidate.getMobile());
						candidateDTO.setSourcedFrom(candidate.getSource());
						candidateDTO.setStatus(roundCandidate.getStatus());
						candidateDTO.setEmploymentType(candidate.getEmploymentType());
						candidateDTO.setTotalApproved("0");
						candidateDTO.setTotalExpectedFeedback("0");
						candidateDTO.setTotalOnHold("0");
						candidateDTO.setResumeLink(candidate.getResumeLink());
						candidateDTO.setTotalRejected("0");
						candidateDTO.setTotalFeedbackReceived("0");
						// setting all candidate card index
						if (roundCandidate.getCardIndex() == null) {
							roundCandidate.setCardIndex(index);
							roundCandidateService.save(roundCandidate);
							index++;
						}
						candidateDTO.setCardIndex(roundCandidate.getCardIndex());
						roundDTO.getCandidateList().add(candidateDTO);
						candidateDTO.setActiveScheduleCount(interviewScheduleService
								.getAllScheduleCountByPositionCodeAndCandidateEmail(positionCode, candidate.getEmail()));
					}
				}
			}
			boardService.doCandidateSort(roundDTO.getCandidateList());
			boardDTO.getRounds().add(roundDTO);
		}
		return boardDTO;
	}

	@Transactional
	public Candidate updateVendorCandidate(Candidate candidate, MultipartFile multiPartFile, String fileName,
			String candidateId) throws RecruizException, IOException {
		Candidate candidateToUpdate = candidateService.getCandidateById(Long.parseLong(candidateId));
		

		boolean updatable = false;
		if (candidateToUpdate.getOwner() == null || candidateToUpdate.getOwner().trim().isEmpty() || candidateToUpdate.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
			updatable = true;
		}

		uploadFileService.createFolderStructureForCandidate(folderPath, candidateId);
		// to delete file if file is passed to update or notified to delete file
		if ((multiPartFile != null && !multiPartFile.isEmpty()) || candidate.getResumeLink().equalsIgnoreCase("")) {
			if (candidate.getResumeLink() != null && candidate.getResumeLink().equalsIgnoreCase(""))
				fileService.deleteFile(candidate.getResumeLink());
		}
		if (multiPartFile != null && !multiPartFile.isEmpty()) {
			File resumeFile = fileService.multipartToFile(multiPartFile);
			String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, fileName, "resume", candidateId);
			String pdfFilePath = fileService.convert(serverPath);
			candidate.setResumeLink(pdfFilePath);
		}

		// deleting file if it is marked for delete (When profileUrl is passed
		// as null)
		if (candidate.getPublicProfileUrl() == null || candidate.getPublicProfileUrl().isEmpty()) {
			String filePath = publicFolder + "/" + candidateToUpdate.getProfileUrl();
			File profilrPic = new File(filePath);
			if (profilrPic.exists())
				profilrPic.delete();
			candidateToUpdate.setProfileUrl("");
		}

		// adding profile pic o public folder
		if ((candidate.getImageContent() != null && !candidate.getImageContent().isEmpty())
				&& (candidate.getImageName() != null && !candidate.getImageName().isEmpty())) {
			byte[] imageBytes = org.springframework.security.crypto.codec.Base64
					.decode(candidate.getImageContent().getBytes());
			if (imageBytes != null && imageBytes.length > 0) {

				File publicProfilePath = new File(publicFolder + "/" + TenantContextHolder.getTenant() + "/candidate/"
						+ candidateToUpdate.getCid());

				if (!publicProfilePath.exists())
					org.apache.commons.io.FileUtils.forceMkdir(publicProfilePath);

				File logoFile = new File(publicProfilePath + "/" + candidate.getImageName());

				org.apache.commons.io.FileUtils.writeByteArrayToFile(logoFile, imageBytes);
				String dpPath = logoFile.getAbsolutePath().replace(publicFolder, "");
				// String logoPath = logoFile.getAbsolutePath().substring(index,
				// logoFile.getAbsolutePath().length());
				if (dpPath != null && !dpPath.isEmpty()) {
					candidateToUpdate.setProfileUrl(dpPath);
					candidateService.save(candidateToUpdate);
				}
			}
		}

		candidate = candidateService.setDefaultValues(candidate);

		// deleting cover letter if file if it is marked for delete
		if (candidate.getCoverLetterPath() == null || candidate.getCoverLetterPath().isEmpty()) {
			if (candidateToUpdate.getCoverLetterPath() != null && !candidateToUpdate.getCoverLetterPath().isEmpty()) {
				File coverLetter = new File(candidateToUpdate.getCoverLetterPath());
				if (coverLetter.exists())
					coverLetter.delete();
				candidateToUpdate.setCoverLetterPath("");
			}
		}
		// updating cover letter if any new file content is passed
		candidateToUpdate.setCoverFileContent(candidate.getCoverFileContent());
		candidateToUpdate.setCoverFileName(candidate.getCoverFileName());
		candidateFileService.uploadCandidateCoverLetter(candidateToUpdate);

		// updating vendor information on candidate update
		candidate.setSource(GlobalConstants.SOURCE_INFO_VENDOR);
		candidate.setSourceDetails(userService.getLoggedInUserVendor().getName());
		candidate.setSourceEmail(userService.getLoggedInUserVendor().getEmail());
		candidate.setSourceMobile(userService.getLoggedInUserVendor().getPhone());
		candidate.setSourceName(userService.getLoggedInUserVendor().getName());
		candidate.setSourcedOnDate(new Date());
		candidate.setOwner(userService.getLoggedInUserVendor().getEmail());
		
		Candidate updatedCandidate = candidateService.updateCandidate(candidate, candidateId,true);
		return updatedCandidate;
	}

	@Transactional
	public Map<String, List<?>> inviteVendorUserList(List<InviteUser> inviteUser, Vendor vendor)
			throws RecruizException {

		final String inviteToSignup = GlobalConstants.INVITE_VENDOR_TO_SIGNUP_EMAIL_TEMPLATE;
		final String inviteToJoinTemplate = GlobalConstants.INVITE_VENDOR_TO_JOIN_EMAIL_TEMPLATE;

		List<String> invalidEmails = new ArrayList<String>();
		List<InviteUser> validUserList = new ArrayList<InviteUser>();
		List<String> invitedEmails = new ArrayList<String>();
		List<User> successfullyInvitedEmails = new ArrayList<User>();

		for (InviteUser user : inviteUser) {

			if (!(EmailValidator.getInstance().isValid(user.getEmail()))) {
				invalidEmails.add(user.getEmail());
			} else if (ValidateEmailDomain.isEmailDomainNotValid(user.getEmail())) {
				invalidEmails.add(user.getEmail());
			} else {
				validUserList.add(user);
			}
		}

		User loggedInUser = userService.getLoggedInUserObject();

		for (InviteUser validUser : validUserList) {
			boolean isUserRegisteredWithRecruiz = tenantResolverService.emailExist(validUser.getEmail());
			if (userService.isVendorUserExists(validUser.getEmail())) {
				invitedEmails.add(validUser.getEmail());
			} else if (isUserRegisteredWithRecruiz) {
				inviteToJoin(vendor, inviteToJoinTemplate, successfullyInvitedEmails, loggedInUser,
						validUser.getEmail());
			} else {
				User vendorUser = new User();
				vendorUser.setName(validUser.getUserName());
				vendorUser.setEmail(validUser.getEmail());
				vendorUser.setLocale(loggedInUser.getLocale());
				vendorUser.setTimezone(loggedInUser.getTimezone());
				vendorUser.setUserType(GlobalConstants.USER_TYPE_VENDOR);
				vendorUser.setUserRole(userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE));
				vendorUser.setOrganization(loggedInUser.getOrganization());
				vendorUser.setAccountStatus(true);
				vendorUser.setJoinedStatus(false);
				vendorUser.setVendorId(vendor.getId() + "");
				userService.save(vendorUser);
				inviteToSignUp(validUser.getEmail(), inviteToSignup);
				successfullyInvitedEmails.add(vendorUser);
			}
		}
		Map<String, List<?>> resp = new HashMap<String, List<?>>();
		resp.put("SuccessfullyInvitedEmails", successfullyInvitedEmails);
		resp.put("ExistingEmail", invitedEmails);
		resp.put("InvalidEmails", invalidEmails);
		return resp;
	}

	/**
	 * invite vedor user to join the vendor org
	 * 
	 * @param vendor
	 * @param inviteToJoinTemplate
	 * @param successfullyInvitedEmails
	 * @param user
	 * @param email
	 * @throws RecruizException
	 */
	private void inviteToJoin(Vendor vendor, final String inviteToJoinTemplate, List<User> successfullyInvitedEmails,
			User user, String email) throws RecruizException {

		int index = email.indexOf("@");
		User vendorUser = new User();
		vendorUser.setName(email.substring(0, index));
		vendorUser.setEmail(email);
		vendorUser.setLocale(user.getLocale());
		vendorUser.setTimezone(user.getTimezone());
		vendorUser.setUserType(GlobalConstants.USER_TYPE_VENDOR);
		vendorUser.setUserRole(userRoleService.getRolesByName(GlobalConstants.VENDOR_ROLE));
		vendorUser.setOrganization(user.getOrganization());
		vendorUser.setAccountStatus(true);
		vendorUser.setJoinedStatus(false);
		vendorUser.setVendorId(vendor.getId() + "");
		userService.save(vendorUser);
		if (!tenantResolverService.emailExist(email)) {
			tenantResolverService.saveTenantResolverForUser(vendorUser);
		}
		if (!tenantResolverService.userExistsForOrg(email, TenantContextHolder.getTenant())) {
			tenantResolverService.saveTenantResolverForUser(vendorUser);
		}

		String xauthToken = tokenAuthenticationProvider.getAuthTokenForUser(email, vendor.getOrganization().getOrgId(),
				WebMode.DASHBOARD, 48, user.getTimezone(), user.getLocale());

		List<String> emailList = new ArrayList<String>();
		emailList.add(email);
		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.USER_NAME, vendorUser.getName());
		emailBody.put(GlobalConstants.ACTIVATION_LINK,
				baseUrl + GlobalConstants.ACTIVATION_URL + GlobalConstants.PASSKEY + xauthToken);

		emailBody.put(GlobalConstants.USER_EMAIL, email);
		emailBody.put(GlobalConstants.FIRST_PASSWORD, GlobalConstants.DEFAULT_PASSWORD);

		String invitedSignup = "You are invited to signup for recruiz under " + user.getOrganization().getOrgName();

		emailService.sendEmail(inviteToJoinTemplate, emailList, invitedSignup, emailBody);

		successfullyInvitedEmails.add(vendorUser);
	}

	@Transactional
	public Position getPositionDetails(String positionCode) throws RecruizException {
		// getting vendor's position details for Logged in User
		Vendor vendor = userService.getLoggedInUserVendor();
		Position position = positionService.findByPositionCodeAndVendorsIsIn(positionCode, vendor);
		if (position != null) {
			position.getEducationalQualification().size();
			position.getGoodSkillSet().size();
			position.getReqSkillSet().size();
		}
		positionService.calculateFinalStatusForPosition(position);
		return position;
	}

	public List<Candidate> getCandidateToSource(String boardId) throws RecruizException {
		List<Candidate> candidates = new LinkedList<Candidate>();
		Board board = boardService.findOne(Long.parseLong(boardId));
		Set<Round> rounds = board.getRounds();
		Set<Long> existingCid = new HashSet<Long>();
		if (rounds != null && !rounds.isEmpty()) {
			for (Round round : rounds) {
				Set<RoundCandidate> existingCandidateList = round.getCandidates();
				if (existingCandidateList != null && !existingCandidateList.isEmpty())
					for (RoundCandidate roundCandidate : existingCandidateList) {
						existingCid.add(roundCandidate.getCandidate().getCid());
					}
			}
			if (userService.getLoggedInUserVendor() != null) {
				candidates = candidateService.getVendorCandidate(userService.getLoggedInUserVendor().getEmail(),
						existingCid);
			}
		} else {
			candidates = candidateService.getVendorCandidate(userService.getLoggedInUserVendor().getEmail(),
					existingCid);
		}
		if (existingCid.isEmpty()) {
			if (userService.getLoggedInUserVendor() != null) {
				candidates = candidateService.getVendorCandidate(userService.getLoggedInUserVendor().getEmail());
			}
		}

		// doing sorting here
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate c1, Candidate c2) {
				int res = String.CASE_INSENSITIVE_ORDER.compare(c1.getFullName(), c2.getFullName());
				if (res == 0) {
					res = c1.getFullName().compareTo(c2.getFullName());
				}
				return res;
			}
		});

		return candidates;
	}

	/**
	 * @param roundCandidateDTO
	 * @param roundId
	 * @param round
	 * @throws RecruizException
	 */
	
	public void sourceCandidateToBoard(CandidateToRoundDTO roundCandidateDTO, String roundId) throws RecruizException {

		Round round = roundService.findOne(Long.parseLong(roundId));

		if (round.getBoard().getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| round.getBoard().getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizWarnException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
		}
		if (round.getBoard().getPositionStatus().equalsIgnoreCase(Status.OnHold.toString())
				|| round.getBoard().getPositionStatus().equalsIgnoreCase(Status.Closed.toString())) {
			throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
		}

		if (!positionService.getPositionByBoard(round.getBoard()).getVendors()
				.contains(userService.getLoggedInUserVendor())) {
			throw new RecruizException(ErrorHandler.VENDOR_NOT_AUTHORISIED, ErrorHandler.POSITION_NOT_SHARED);
		}

		// defining list to add to candidate activity
		List<CandidateActivity> allCandidateActivity = new ArrayList<CandidateActivity>();
		CandidateActivity candidateActivity = null;
		// getting min index of candidate card in a round
		double minCardIndex = roundCandidateService.getMinCardIndex(round.getId() + "");

		Set<RoundCandidate> candidateList = new HashSet<RoundCandidate>();
		for (String email : roundCandidateDTO.getCandidateEmailList()) {
			Candidate candidate = candidateService.getCandidateByEmail(email);
			if (candidate != null && (candidate.getSourceEmail() != null || !candidate.getSourceEmail().isEmpty())
					&& candidate.getSourceEmail().equalsIgnoreCase(userService.getLoggedInUserVendor().getEmail())) {
				RoundCandidate existingCandidate = roundCandidateService.getCandidateByIdAndRoundId(candidate, roundId);
				if (existingCandidate == null) {
					RoundCandidate roundCandidate = new RoundCandidate();
					roundCandidate.setCandidate(candidate);
					roundCandidate.setStatus(BoardStatus.YetToProcess.toString());
					
					// calculating the mid index for candidate card
					double calculatedIndex = roundCandidateService.getMidOfIndex(0, minCardIndex);
					minCardIndex = calculatedIndex;
					
					roundCandidate.setRoundId(roundId);
					roundCandidate.setPositionCode(roundCandidateDTO.getPositionCode());
					roundCandidate.setRound(round);
					roundCandidate.setSourcedBy(userService.getLoggedInUserEmail());
					roundCandidate.setCardIndex(calculatedIndex);
					roundCandidateService.save(roundCandidate);
					candidateList.add(roundCandidate);
					// adding to candidate activity list
					candidateActivity = new CandidateActivity(
							userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName()
									+ ")",
							"Added to board by vendor for position : " + roundCandidateDTO.getPositionCode(),
							candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());
					allCandidateActivity.add(candidateActivity);
				}
			}
		}

		round.getCandidates().addAll(candidateList);
		roundService.save(round);

		candidateActivityService.save(allCandidateActivity);
	}

	/**
	 * this will calculate the status of the vendor based on vendor
	 * status/vendor super user status
	 * 
	 * @param vendor
	 */
	@Transactional(readOnly = true)
	public void calculateVendorStatus(Vendor vendor) {
		User vendorSuperUser = userService.getUserByEmail(vendor.getEmail());
		if (vendor.getStatus() && !vendorSuperUser.getJoinedStatus()) {
			vendor.setActivationStatus(Vendor.PENDING);
		} else if (vendorSuperUser != null && vendorSuperUser.getJoinedStatus() && vendorSuperUser.getAccountStatus()) {
			vendor.setActivationStatus(Vendor.ACTIVE);
		} else {
			vendor.setActivationStatus(Vendor.DISABLED);
		}
	}

	/**
	 * Overloaded method of calculateVendorStatus(Vendor vendor)
	 * 
	 * @param vendorList
	 */
	@Transactional(readOnly = true)
	public void calculateVendorStatus(List<Vendor> vendorList) {
		if (vendorList != null && !vendorList.isEmpty()) {
			for (Vendor vendor : vendorList) {
				calculateVendorStatus(vendor);
			}
		}
	}

	/**
	 * to delete the vendor
	 * 
	 * @param vendorId
	 * @throws RecruizException
	 */
	@Transactional
	public void deleteVendor(Vendor vendor) throws RecruizException {
		// getting all user for vendor
		List<User> vendorUsers = userService.getAllUserByVendor(vendor.getId() + "");

		// deleting all the vendor user
		if (vendorUsers != null && !vendorUsers.isEmpty()) {
			userService.delete(vendorUsers);
		}

		List<Position> positionList = positionService.getVendorPosition(vendor);
		if (positionList != null && !positionList.isEmpty()) {
			for (Position position : positionList) {
				position.getVendors().remove(vendor);
			}
		}
		delete(vendor);
	}

	public RestResponse giveScheduleInterviewPermission(String vendorId, String permission) {
		
		try{
		Vendor vendor = vendorRepository.findOne(Long.valueOf(vendorId));
		
		if (vendor == null)
		    return new RestResponse(RestResponse.FAILED, ErrorHandler.VENDOR_NOT_FOUND, ErrorHandler.VENDOR_NOT_EXISTS);
		    
		    vendor.setIsInterviewSchedule(permission);
		    save(vendor);
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "internal server error!");
		}
		return new RestResponse(RestResponse.SUCCESS, "Success");
	}

}
