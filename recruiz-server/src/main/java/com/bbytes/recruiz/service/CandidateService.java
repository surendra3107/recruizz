package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.AgencyInvoice;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateEducationDetails;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CandidateStatus;
import com.bbytes.recruiz.domain.Folder;
import com.bbytes.recruiz.domain.Notification;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.EmploymentType;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.NotificationEvent;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.exception.RecruizCandidateExistException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.CandidateRepository;
import com.bbytes.recruiz.repository.FolderRepository;
import com.bbytes.recruiz.rest.dto.models.FileUploadRequestDTO;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.JPAUtil;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.lowagie.text.DocumentException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

@Service
public class CandidateService extends AbstractService<Candidate, Long> {

	private static final Logger logger = LoggerFactory.getLogger(CandidateService.class);

	private CandidateRepository candidateRepository;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private FileService fileService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private AgencyInvoiceService agencyInvoiceService;

	@Autowired
	private CandidateStatusService candidateStatusService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private CandidateResumeBulkUploadBatchService candidateResumeBulkUploadBatchService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	@Autowired
	private CandidateFolderService candidateFolderService;

	@Autowired
	FolderRepository folderRepository;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${file.upload.temp.path}")
	private String tempFilePath;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Value("${candidate.filestorage.mode}")
	private String fileStorageMode;
	
	@Value("${candidate.aws.bucketname}")
	private String BUCKET_NAME;
	
	@Autowired
	public CandidateService(CandidateRepository candidateRepository) {
		super(candidateRepository);
		this.candidateRepository = candidateRepository;
	}

	@Transactional(readOnly = true)
	public boolean isCandidateExists(String email) {
		List<Candidate> candidates = candidateRepository.findDistinctByEmailOrAlternateEmail(email, email);
		if (candidates == null || candidates.isEmpty()) {
			return false;
		}
		return true;
	}


	@Transactional(readOnly = true)
	public Long getCandidateCountForLoggedInUser() {
		return candidateRepository.countByOwner(userService.getLoggedInUserEmail());
	}


	@Transactional(readOnly = true)
	public boolean isCandidateExists(long candidateId) {
		boolean state = candidateRepository.findOne(candidateId) == null ? false : true;
		return state;
	}

	@Transactional
	public Candidate addCandidate(Candidate candidate) throws RecruizException {
		if (!checkPermissionService.hasAddEditCandidatePermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		if (null == candidate.getFullName() || candidate.getFullName().trim().isEmpty()) {
			throw new RecruizWarnException(ErrorHandler.CANDIDATE_NAME_MISSING, ErrorHandler.CANDIDATE_NAME_MANDATORY);
		}

		// removing bad skills
		List<String> badSkillSet = StringUtils.commaSeparateStringToList(GlobalConstants.BAD_SKILLS);
		if (null != candidate.getKeySkills() && !candidate.getKeySkills().isEmpty()) {
			candidate.getKeySkills().removeAll(badSkillSet);
		}

		candidate.setOwner(userService.getLoggedInUserEmail());
		candidate.setCandidateRandomId("C-" + StringUtils.get6RandomDigit());
		save(candidate);
		// making entry to candidate activity after adding
		candidateActivityService.addActivity("Candidate added", candidate.getOwner(), candidate.getCid() + "",
				CandidateActivityType.Added.getDisplayName());
		return candidate;
	}

	@Transactional
	public void addVendorCandidate(Candidate candidate) throws RecruizException {
		candidate.setOwner(userService.getLoggedInUserEmail());
		candidate.setCandidateRandomId("C-" + StringUtils.get6RandomDigit());

		// removing bad skills
		List<String> badSkillSet = StringUtils.commaSeparateStringToList(GlobalConstants.BAD_SKILLS);
		if (null != candidate.getKeySkills() && !candidate.getKeySkills().isEmpty()) {
			candidate.getKeySkills().removeAll(badSkillSet);
		}

		save(candidate);
		// making entry to candidate activity after adding
		candidateActivityService.addActivity("Candidate added by vendor : " + candidate.getSourceName(), candidate.getOwner(),
				candidate.getCid() + "", CandidateActivityType.Added.toString());
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidateByEmailOrAlternateEmail(String email) throws RecruizException {
		return setCandidatePublicProfileUrl(candidateRepository.findDistinctByEmailOrAlternateEmail(email, email));
	}

	@Transactional(readOnly = true)
	public Candidate getOneByCandidateEmail(String email) {
		return candidateRepository.findByEmail(email);
	}

	public Candidate getCandidateByEmail(String email) throws RecruizException {
		return setCandidatePublicProfileUrl(candidateRepository.findByEmail(email));
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidateByHighestQualLike(String qual) throws RecruizException {
		return candidateRepository.findByHighestQualLike(qual);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidateByCurrentLocationAndKeySkills(String location, String keySkill) throws RecruizException {

		return candidateRepository.findByCurrentLocationAndKeySkills(location, keySkill);
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getCandidateListForCurrentUser(String pageNo, String sortField, String sortOrder) throws RecruizException {
		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizPermissionDeniedException(ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Page<Candidate> candidateList = null;
		Calendar cal = Calendar.getInstance(); //Get current date/month 
		Date endDate = cal.getTime();
		cal.add(Calendar.MONTH, -6);
		Date startDate = cal.getTime();

		// checking user has view all candidate permission
		if (checkUserPermission.isSuperAdmin() || checkUserPermission.hasViewAllCandidatesPermission()) {
			candidateList = getAllCandidate(
					pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),startDate,endDate);
		} else {
			String loggedInUserEmail = userService.getLoggedInUserEmail();
			candidateList = getAllCandidateByOwner(
					pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
					loggedInUserEmail,startDate,endDate);
		}
		// Need for lazy loading
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		}

		return candidateList;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getCandidateListForCurrentUserAndNotInList(String pageNo, String sortField, String sortOrder,
			List<Long> candidateCids) throws RecruizException {
		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizPermissionDeniedException(ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Page<Candidate> candidateList = null;

		// checking user has view all candidate permission
		if (checkUserPermission.isSuperAdmin() || checkUserPermission.hasViewAllCandidatesPermission()) {
			if (candidateCids == null || candidateCids.isEmpty())
				candidateList = getAllCandidate(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)));
			else
				candidateList = getAllCandidate(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
						candidateCids);
		} else {
			if (candidateCids == null || candidateCids.isEmpty()) {
				String loggedInUserEmail = userService.getLoggedInUserEmail();
				candidateList = getAllCandidateByOwner(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
						loggedInUserEmail);
			} else {
				String loggedInUserEmail = userService.getLoggedInUserEmail();
				candidateList = getAllCandidateByOwner(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)),
						loggedInUserEmail, candidateCids);
			}

		}
		// Need for lazy loading
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		}

		return candidateList;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> searchCandidateListForCurrentUser(String searchText, String pageNo, String sortField, String sortOrder,
			List<Long> candidateCids) throws RecruizException {
		if (!checkUserPermission.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (checkUserPermission.hasNormalRole()) {
			throw new RecruizPermissionDeniedException(ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Page<Candidate> candidateList = null;

		// checking user has view all candidate permission
		if (checkUserPermission.isSuperAdmin() || checkUserPermission.hasViewAllCandidatesPermission()) {
			if (candidateCids == null || candidateCids.isEmpty())
				candidateList = searchTextInNameOrEmailOrMobile(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)), searchText);
			else
				candidateList = searchTextInNameOrEmailOrMobile(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)), searchText,
						candidateCids);

		} else {
			if (candidateCids == null || candidateCids.isEmpty()) {
				String loggedInUserEmail = userService.getLoggedInUserEmail();
				candidateList = searchTextInNameOrEmailOrMobileByOwner(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)), searchText,
						loggedInUserEmail);
			} else {
				String loggedInUserEmail = userService.getLoggedInUserEmail();
				candidateList = searchTextInNameOrEmailOrMobileByOwner(
						pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder)), searchText,
						loggedInUserEmail, candidateCids);
			}

		}
		// Need for lazy loading
		for (Candidate candidate : candidateList) {
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
		}

		return candidateList;
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllcandidateByKeySkillsAndTotalExpAndHighestQual(String keySkill, String totalExp, String qual)
			throws RecruizException {

		return candidateRepository.findByKeySkillsAndTotalExpAndHighestQual(keySkill, totalExp, qual);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidateByKeySkillsAndTotalExpAndHighestQualAndExpectedCtc(String keySkill, String totalExp, String qual,
			String ctc) throws RecruizException {

		return candidateRepository.findByKeySkillsAndTotalExpAndHighestQualAndExpectedCtc(keySkill, totalExp, qual, ctc);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidate() throws RecruizException {
		return setCandidatePublicProfileUrl(candidateRepository.findAll());
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidates() throws RecruizException {
		return candidateRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidate(Pageable pageable) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findAll(pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}
	
	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidate(Pageable pageable,Date startDate, Date endDate) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findAllByModificationDateBetween(startDate,endDate,pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidate(Pageable pageable, List<Long> notInListCandidates) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findByCidNotIn(notInListCandidates, pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidate(Collection<Long> cids, Pageable pageable) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findByCidIn(cids, pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidate(Collection<Long> cids) throws RecruizException {
		List<Candidate> candidateList = candidateRepository.findByCidIn(cids);
		setCandidatePublicProfileUrl(candidateList);
		return candidateList;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidateByOwner(Pageable pageable, String ownerEmail) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findByOwner(pageable, ownerEmail);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}
	
	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidateByOwner(Pageable pageable, String ownerEmail,Date startDate,Date endDate) throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findAllByOwnerAndModificationDateBetween(ownerEmail,startDate,endDate,pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}
	
	
	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidateByOwner(String ownerEmail) throws RecruizException {
		List<Candidate> candidateList = candidateRepository.findListByOwner(ownerEmail);
		return candidateList;
	}
	

	@Transactional(readOnly = true)
	public Page<Candidate> getAllCandidateByOwner(Pageable pageable, String ownerEmail, List<Long> notInListCandidates)
			throws RecruizException {
		Page<Candidate> candidatePage = candidateRepository.findByOwnerAndCidNotIn(ownerEmail, notInListCandidates, pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}

	@Transactional(readOnly = true)
	public Page<Candidate> searchTextInNameOrEmailOrMobile(Pageable pageable, String searchText) {
		return candidateRepository.findAll(Specifications.where(JPAUtil.candidateContainsTextInName(searchText)), pageable);
	}

	@Transactional(readOnly = true)
	public Page<Candidate> searchTextInNameOrEmailOrMobile(Pageable pageable, String searchText, List<Long> notInListCandidates) {
		return candidateRepository.findByCidNotIn(notInListCandidates, Specifications.where(JPAUtil.candidateContainsTextInName(searchText)),
				pageable);
	}

	@Transactional(readOnly = true)
	public Page<Candidate> searchTextInNameOrEmailOrMobileByOwner(Pageable pageable, String searchText, String ownerEmail) {
		return candidateRepository.findByOwner(ownerEmail, Specifications.where(JPAUtil.candidateContainsTextInName(searchText)), pageable);
	}

	@Transactional(readOnly = true)
	public Page<Candidate> searchTextInNameOrEmailOrMobileByOwner(Pageable pageable, String searchText, String ownerEmail,
			List<Long> notInListCandidates) {
		return candidateRepository.findByOwnerAndCidNotIn(ownerEmail, notInListCandidates,
				Specifications.where(JPAUtil.candidateContainsTextInName(searchText)), pageable);
	}

	@Transactional(readOnly = true)
	public long candidateCountByOwner(String ownerEmail) throws RecruizException {
		long count = candidateRepository.countByStatusAndOwner(Status.Active.toString(), ownerEmail);
		return count;
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidateToSource() throws RecruizException {
		List<Candidate> responseCandidate = new ArrayList<Candidate>();
		List<Candidate> candidates = candidateRepository.findAll();
		for (Candidate candidate : candidates) {
			if (candidate.getStatus().equalsIgnoreCase(Status.Active.toString()))
				responseCandidate.add(candidate);
		}
		return responseCandidate;
	}

	@Transactional
	public Candidate updateCandidateStatus(long candidateId, String status) throws RecruizException {

		Candidate candidate = candidateRepository.findOne(candidateId);
		boolean updatable = false;
		if (candidate.getOwner() == null && checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (candidate.getOwner() == null) {
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);
		} else if (candidate.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				|| checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditCandidatePermission()) {
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);
		}

		candidate.setStatus(status);
		candidate = candidateRepository.save(candidate);
		// making entry to candidate activity after adding
		candidateActivityService.statusChangedEvent(status, candidate);

		// send notifications to Hr executives of the board candidate belongs to
		notificationOnCandidateChange(candidate, NotificationEvent.CANDIDATE_STATUS_CHANGED.getDisplayName(),
				"Status changed to " + status + " for candidate : " + candidate.getFullName());

		return candidate;
	}

	private void notificationOnCandidateChange(Candidate candidate, String notificationType, String msg) {
		List<Position> positions = positionService.getPositionsByCandidate(candidate.getCid() + "");
		if (positions != null && !positions.isEmpty()) {
			Set<User> hrExecutives = new HashSet<>();
			for (Position position : positions) {
				hrExecutives.addAll(position.getHrExecutives());
			}

			if (hrExecutives != null && !hrExecutives.isEmpty()) {
				for (User hr : hrExecutives) {
					if (!hr.getEmail().equalsIgnoreCase(userService.getLoggedInUserEmail())) {
						notificationService.sendNotification(new Notification(hr.getEmail(), userService.getLoggedInUserEmail(),
								userService.getLoggedInUserObject().getName(), notificationType,
								notificationService.getMessageForCandidateProfileUpdated(candidate.getFullName()), new Date(), null, 0,
								candidate.getCid(), 0, null));
					}
				}
			}
		}
	}

	/**
	 * <code>updateCandidate</code> method return updated candidate object by
	 * passing candidateId and candidate details as argument.
	 *
	 * @param updateCandidate
	 * @param candidateId
	 * @return
	 * @throws RecruizException
	 */
	
	public Candidate updateCandidate(Candidate updateCandidate, String candidateId, boolean updatable) throws RecruizException {
		Candidate candidateFromDB = candidateRepository.findOne(Long.valueOf(candidateId));
		if (!updatable && !checkPermissionService.hasAddEditCandidatePermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		return updateCandidateProfile(updateCandidate, candidateId, candidateFromDB);
	}

	@Transactional
	public Candidate updateCandidate(Candidate updateCandidate, String candidateId) throws RecruizException {

		Candidate candidateFromDB = candidateRepository.findOne(Long.valueOf(candidateId));
		boolean updatable = false;
		if (userService.getLoggedInUserEmail().equalsIgnoreCase(candidateFromDB.getOwner())
				|| checkPermissionService.hasGlobalEditPermission()) {
			updatable = true;
		} else if (!updatable && !checkPermissionService.hasAddEditCandidatePermission())
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);

		return updateCandidateProfile(updateCandidate, candidateId, candidateFromDB);
	}

	
	public Candidate updateCandidateProfile(Candidate updateCandidate, String candidateId, Candidate candidateFromDB)
			throws RecruizWarnException {
		if (isCandidateExists(Long.valueOf(candidateId))) {

			candidateFromDB.setFullName(updateCandidate.getFullName());
			candidateFromDB.setMobile(updateCandidate.getMobile());
			candidateFromDB.setAlternateMobile(updateCandidate.getAlternateMobile());
			candidateFromDB.setEmail(updateCandidate.getEmail());
			candidateFromDB.setAlternateEmail(updateCandidate.getAlternateEmail());
			candidateFromDB.setCurrentCompany(updateCandidate.getCurrentCompany());
			candidateFromDB.setCurrentTitle(updateCandidate.getCurrentTitle());
			candidateFromDB.setCurrentLocation(updateCandidate.getCurrentLocation());
			candidateFromDB.setCurrentCtc(updateCandidate.getCurrentCtc());
			candidateFromDB.setTotalExp(updateCandidate.getTotalExp());
			candidateFromDB.setHighestQual(updateCandidate.getHighestQual());
			candidateFromDB.setEmploymentType(updateCandidate.getEmploymentType());
			candidateFromDB.setExpectedCtc(updateCandidate.getExpectedCtc());
			candidateFromDB.setNoticePeriod(updateCandidate.getNoticePeriod());
			candidateFromDB.setNoticeStatus(updateCandidate.isNoticeStatus());
			candidateFromDB.setMaritalStatus(updateCandidate.getMaritalStatus());
			candidateFromDB.setLanguages(updateCandidate.getLanguages());
			candidateFromDB.setSource(updateCandidate.getSource());

			candidateFromDB.setCustomField(updateCandidate.getCustomField());

			if (null != updateCandidate.getSourceDetails() && !updateCandidate.getSourceDetails().isEmpty()) {
				candidateFromDB.setSourceDetails(updateCandidate.getSourceDetails());
			}

			if (null != updateCandidate.getOwner() && !updateCandidate.getOwner().isEmpty()) {
				candidateFromDB.setOwner(updateCandidate.getOwner());
			}

			if (null != updateCandidate.getSourcedOnDate()) {
				candidateFromDB.setSourcedOnDate(updateCandidate.getSourcedOnDate());
			}
			if (null != updateCandidate.getSourceEmail() && !updateCandidate.getSourceEmail().isEmpty()) {
				candidateFromDB.setSourceEmail(updateCandidate.getSourceEmail());
			}
			if (null != updateCandidate.getSourceMobile() && !updateCandidate.getSourceMobile().isEmpty()) {
				candidateFromDB.setSourceMobile(updateCandidate.getSourceMobile());
			}

			if (null != updateCandidate.getSourceName() && !updateCandidate.getSourceName().isEmpty()) {
				candidateFromDB.setSourceName(updateCandidate.getSourceName());
			}

			candidateFromDB.setLastWorkingDay(updateCandidate.getLastWorkingDay());
			candidateFromDB.setKeySkills(updateCandidate.getKeySkills());
			candidateFromDB.setResumeLink(updateCandidate.getResumeLink());
			candidateFromDB.setDob(updateCandidate.getDob());
			candidateFromDB.setGender(updateCandidate.getGender());
			candidateFromDB.setCommunication(updateCandidate.getCommunication());
			candidateFromDB.setFacebookProf(updateCandidate.getFacebookProf());
			candidateFromDB.setLinkedinProf(updateCandidate.getLinkedinProf());
			candidateFromDB.setGithubProf(updateCandidate.getGithubProf());
			candidateFromDB.setTwitterProf(updateCandidate.getTwitterProf());
			candidateFromDB.setComments(updateCandidate.getComments());
			candidateFromDB.setStatus(updateCandidate.getStatus());
			candidateFromDB.setPreferredLocation(updateCandidate.getPreferredLocation());
			candidateFromDB.setPreviousEmployment(updateCandidate.getPreviousEmployment());
			candidateFromDB.setIndustry(updateCandidate.getIndustry());
			candidateFromDB.setAddress(updateCandidate.getAddress());
			candidateFromDB.setAverageStayInCompany(updateCandidate.getAverageStayInCompany());
			candidateFromDB.setLongestStayInCompany(updateCandidate.getLongestStayInCompany());
			candidateFromDB.setCategory(updateCandidate.getCategory());
			candidateFromDB.setSubCategory(updateCandidate.getSubCategory());
			candidateFromDB.setNationality(updateCandidate.getNationality());
			// changing the s3Enabled status when candidate is updated
			candidateFromDB.setS3Enabled(false);

			if (updateCandidate.getEducationDetails() != null && !updateCandidate.getEducationDetails().isEmpty()) {

				for (CandidateEducationDetails educationDetails : updateCandidate.getEducationDetails()) {
					educationDetails.setCandidate(candidateFromDB);
				}
			}
			if (null != candidateFromDB.getEducationDetails()) {
				candidateFromDB.getEducationDetails().clear();
			}
			if (null != updateCandidate.getEducationDetails() && !updateCandidate.getEducationDetails().isEmpty()) {
				candidateFromDB.getEducationDetails().addAll(updateCandidate.getEducationDetails());
			}

		} else {
			throw new RecruizWarnException(ErrorHandler.INVLID_DATA, ErrorHandler.CANDIDATE_NOT_FOUND);
		}

		// removing bad skills
		List<String> badSkillSet = StringUtils.commaSeparateStringToList(GlobalConstants.BAD_SKILLS);
		if (null != candidateFromDB.getKeySkills() && !candidateFromDB.getKeySkills().isEmpty()) {
			candidateFromDB.getKeySkills().removeAll(badSkillSet);
		}

		candidateFromDB = candidateRepository.save(candidateFromDB);
		try {
			// making entry to candidate activity after adding
			candidateActivityService.detailsUpdated(candidateFromDB);

			notificationOnCandidateChange(candidateFromDB, NotificationEvent.CANDIDATE_DETAILS_UPDATED.getDisplayName(),
					" Details updated for candidate : " + candidateFromDB.getFullName());
		} catch (Exception ex) {
			// might throw NPE for logged in user when called for external user.
		}

		return candidateFromDB;
	}

	@Transactional(readOnly = true)
	public List<String> getAllCandidateEmail() throws RecruizException {
		return candidateRepository.getAllCandidateEmail();
	}

	/**
	 * This method is used to delete multiple candidates by id
	 *
	 * @author Akshay
	 * @param candidateIdList
	 * @throws RecruizException
	 */
	@Transactional
	public void deleteBulkCandidates(List<String> candidateIdList, boolean removeInvoiceFlag) throws RecruizException {
		for (String candidateId : candidateIdList) {
			this.deleteCandidate(Long.parseLong(candidateId), removeInvoiceFlag);
		}
	}

	/**
	 * <code>deleteCandidate</code> method is used to delete candidate by
	 * candidateId
	 *
	 * @param candidateId
	 * @throws RecruizException
	 */
	@Transactional
	public void deleteCandidate(long candidateId, boolean removeInvoiceFlag) throws RecruizException {
		if (!checkPermissionService.hasDeleteCandidatePermission() && !checkPermissionService.hasGlobalDeletePermission())
			throw new RecruizException(ErrorHandler.PERMISSION_DENIED, ErrorHandler.INSUFFICIENT_PRIVILEGE);

		Candidate candidate = candidateRepository.findOne(candidateId);
		if (candidate == null) {
			throw new RecruizWarnException(ErrorHandler.CANDIDATE_NOT_EXISTS, ErrorHandler.CANDIDATE_NOT_FOUND);
		}

		Set<CandidateFile> candidateFiles = candidate.getFiles();
		List<String> filePath = new ArrayList<>();
		if (null != candidateFiles && !candidateFiles.isEmpty()) {
			for (String path : filePath) {
				filePath.add(path);
			}
		}

		filePath.add(candidate.getResumeLink());

		List<CandidateStatus> candidateStatus = candidateStatusService.getByCandidate(candidate);
		if (candidateStatus != null && !candidateStatus.isEmpty()) {
			candidateStatusService.delete(candidateStatus);
		}

		List<AgencyInvoice> candidateInvoices = agencyInvoiceService.getCandidateInvoices(candidate);
		if (candidateInvoices != null && !candidateInvoices.isEmpty() && removeInvoiceFlag) {
			agencyInvoiceService.delete(candidateInvoices);
		}

		if ((candidate.getOwner() == null) && checkPermissionService.hasGlobalDeletePermission()) {
			List<RoundCandidate> roundCandidates = roundCandidateService.getAllRoundCandidates(candidate);
			if (roundCandidates != null && !roundCandidates.isEmpty()) {
				for (RoundCandidate roundCandidate : roundCandidates) {
					roundCandidateService.delete(roundCandidate);
				}
			}
			delete(candidate);
		} else if (!candidate.getOwner().equalsIgnoreCase(userService.getLoggedInUserEmail())
				&& !checkPermissionService.hasGlobalDeletePermission()) {
			throw new RecruizWarnException(ErrorHandler.PERMISSION_DENIED_NON_OWNER, ErrorHandler.NO_OWNERSHIP);
		} else {
			List<RoundCandidate> roundCandidates = roundCandidateService.getAllRoundCandidates(candidate);
			if (roundCandidates != null && !roundCandidates.isEmpty()) {
				for (RoundCandidate roundCandidate : roundCandidates) {
					roundCandidateService.delete(roundCandidate);
				}
			}
			delete(candidate);
		}

		try{	
			String s3CandidatePath = "files"+"/"+TenantContextHolder.getTenant()+"/"+"candidate"+"/"+candidateId+"";
			uploadFileService.deleteCandidateFolderFromAWS(s3CandidatePath);
		}catch(Exception e){
			
		}
		
		
		// deleting file on deleting candidate
		try {
			if (null != filePath && !filePath.isEmpty()) {
				for (String path : filePath) {

					if (null != path && !path.trim().isEmpty()) {
						continue;
					}

					if (path.startsWith("http")) {
						URL url = new URL(path);
						String httpFileUrl = url.getPath();
						httpFileUrl = httpFileUrl.replaceAll("%20", "+");
						if (httpFileUrl.startsWith("/")) {
							httpFileUrl = httpFileUrl.substring(1, httpFileUrl.length());
						}
						s3DownloadClient.deleteS3File(fileService.getTenantBucket(), httpFileUrl);
					} else {
						File file = new File(path);
						if (file.exists()) {
							file.delete();
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.warn("\n\t failed to delete file on deleting candidate " + ex.getMessage());
		}

	}

	/**
	 * <code>deleteCandidate</code> method return candidate object by passing
	 * candidateId as argument.
	 *
	 * @param candidateId
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public Candidate getCandidateById(long candidateId) throws RecruizException {
		Candidate candidate = candidateRepository.findOne(candidateId);
		return setCandidatePublicProfileUrl(candidate);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidatesToBoard(Set<Long> cids) {

		List<Candidate> candidates = candidateRepository.findByCidNotIn(cids);
		setCandidatePublicProfileUrl(candidates);

		List<Candidate> responseCandidate = new ArrayList<Candidate>();
		for (Candidate candidate : candidates) {
			if (candidate.getStatus().equalsIgnoreCase(Status.Active.toString()))
				responseCandidate.add(candidate);
		}
		return responseCandidate;
	}

	/**
	 * this method to add display name of employment type for position
	 */
	@Transactional(readOnly = true)
	public void getEmploymentTypeDisplayName(Candidate candidate) {
		if (EmploymentType.FullTime.toString().equalsIgnoreCase(candidate.getEmploymentType()))
			candidate.setEmploymentTypeDisplayName(EmploymentType.FullTime.getDisplayName());
		else if (EmploymentType.PartTime.toString().equalsIgnoreCase(candidate.getEmploymentType()))
			candidate.setEmploymentTypeDisplayName(EmploymentType.PartTime.getDisplayName());
		else
			candidate.setEmploymentTypeDisplayName(candidate.getEmploymentType());
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidatesToBoard(String searchQuery, Set<Long> cids) {
		List<Candidate> candidates;

		// checking user has view all candidate permission
		if (checkPermissionService.isSuperAdmin() || checkPermissionService.hasViewAllCandidatesPermission()) {
			if (cids.isEmpty()) {
				if (searchQuery == null || searchQuery.isEmpty())
					candidates = candidateRepository.findTop100ByOrderByModificationDateDesc();
				else
					candidates = candidateRepository
					.findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingOrderByModificationDateDesc(searchQuery,
							searchQuery);
			} else {
				if (searchQuery == null || searchQuery.isEmpty())
					candidates = candidateRepository.findTop100ByCidNotInOrderByModificationDateDesc(cids);
				else
					candidates = candidateRepository
					.findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndCidNotInOrderByModificationDateDesc(
							searchQuery, searchQuery, cids);
			}
		} else {
			String ownerEmail = userService.getLoggedInUserEmail();
			if (cids.isEmpty()) {
				if (searchQuery == null || searchQuery.isEmpty())
					candidates = candidateRepository.findTop100ByOwnerOrderByModificationDateDesc(ownerEmail);
				else
					candidates = candidateRepository
					.findTop100ByFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndOwnerOrderByModificationDateDesc(
							searchQuery, searchQuery, ownerEmail);
			} else {
				if (searchQuery == null || searchQuery.isEmpty())
					candidates = candidateRepository.findTop100ByOwnerAndCidNotInOrderByModificationDateDesc(ownerEmail, cids);
				else
					candidates = candidateRepository
					.findTop100ByOwnerAndFullNameIgnoreCaseContainingOrEmailIgnoreCaseContainingAndCidNotInOrderByModificationDateDesc(
							ownerEmail, searchQuery, searchQuery, cids);
			}
		}

		setCandidatePublicProfileUrl(candidates);

		List<Candidate> responseCandidate = new ArrayList<Candidate>();
		for (Candidate candidate : candidates) {
			if (candidate.getStatus().equalsIgnoreCase(Status.Active.toString()))
				responseCandidate.add(candidate);
		}

		return responseCandidate;
	}

	@Transactional(readOnly = true)
	public int getActiveCandidateCount() {
		return candidateRepository.getActiveCandidateCount();
	}

	@Transactional(readOnly = true)
	public boolean numberExists(String number) {
		List<Candidate> candidate = candidateRepository.findByMobileOrAlternateMobile(number, number);
		if (candidate != null && !candidate.isEmpty())
			return true;
		else
			return false;
	}

	@Transactional(readOnly = true)
	public List<Candidate> getAllCandidateForSearchIndex(Pageable pageable) {

		List<Candidate> result = new ArrayList<>();
		Page<Candidate> candidates = candidateRepository.findAll(pageable);

		if (candidates == null)
			return result;

		for (Candidate candidate : candidates) {
			// Need to load lazy
			candidate.getKeySkills().size();
			candidate.getEducationDetails().size();
			result.add(candidate);
		}
		setCandidatePublicProfileUrl(result);
		return result;
	}

	public Candidate setDefaultValues(Candidate candidate) {
		if (candidate.getCurrentCompany() == null || candidate.getCurrentCompany().trim().isEmpty())
			candidate.setCurrentCompany("N/A");
		if (candidate.getAlternateMobile() == null || candidate.getAlternateMobile().trim().isEmpty())
			candidate.setAlternateMobile("");
		if (candidate.getCurrentTitle() == null || candidate.getCurrentTitle().trim().isEmpty())
			candidate.setCurrentTitle("N/A");
		if (candidate.getCurrentLocation() == null || candidate.getCurrentLocation().trim().isEmpty())
			candidate.setCurrentLocation("N/A");
		if (candidate.getHighestQual() == null || candidate.getHighestQual().trim().isEmpty())
			candidate.setHighestQual("N/A");
		if (candidate.getEmploymentType() == null || candidate.getEmploymentType().trim().isEmpty())
			candidate.setEmploymentType("N/A");
		if (candidate.getPreferredLocation() == null || candidate.getPreferredLocation().trim().isEmpty())
			candidate.setPreferredLocation("N/A");
		if (candidate.getComments() == null || candidate.getComments().trim().isEmpty())
			candidate.setComments("N/A");
		if (candidate.getPreviousEmployment() == null || candidate.getPreviousEmployment().trim().isEmpty())
			candidate.setPreviousEmployment("N/A");
		if (candidate.getIndustry() == null || candidate.getIndustry().trim().isEmpty())
			candidate.setIndustry("N/A");
		if (candidate.getAddress() == null || candidate.getAddress().trim().isEmpty())
			candidate.setAddress("N/A");
		if (candidate.getCommunication() == null || candidate.getCommunication().trim().isEmpty())
			candidate.setCommunication("N/A");
		if (candidate.getGender() == null || candidate.getGender().trim().isEmpty())
			candidate.setGender("N/A");
		if (candidate.getSource() == null || candidate.getSource().trim().isEmpty()) {
			candidate.setSource(GlobalConstants.SOURCED_BY_HR);
			candidate.setSourceName("");
		}

		return candidate;
	}

	public List<Candidate> getVendorCandidate(String email) {
		return setCandidatePublicProfileUrl(candidateRepository.findBySourceEmail(email));
	}

	public Page<Candidate> getVendorCandidate(String email, Pageable pageable) {
		Page<Candidate> candidatePage = candidateRepository.findBySourceEmail(email, pageable);
		setCandidatePublicProfileUrl(candidatePage.getContent());
		return candidatePage;
	}

	public List<Candidate> getVendorCandidate(String email, Set<Long> cids) {
		return setCandidatePublicProfileUrl(candidateRepository.findBySourceEmailAndCidNotIn(email, cids));
	}

	@Transactional(readOnly = true)
	public Candidate getCandidateByFirstEmail(String email) {
		return setCandidatePublicProfileUrl(candidateRepository.findOneByEmail(email));
	}


	public Candidate setSourceInfo(Candidate candidate, String sourceEmail, String source, String sourceDetails) {
		// updating source information if candidate older than 6 months
		try{
			if (candidate.getSourcedOnDate() == null)
				candidate.setSourcedOnDate(new Date());
			if (candidate.getSourcedOnDate().before(new DateTime().minusDays(GlobalConstants.SOURCE_INFO_UPDATE_AFTER).toDate())) {
				candidate.setSourcedOnDate(new Date());
				candidate.setSource(source);
				candidate.setOwner(sourceEmail);
				if (sourceDetails != null)
					candidate.setSourceDetails(sourceDetails);
			}
		}catch(Exception e){
			logger.error(""+e);
		}
		return candidate;
	}


	public Candidate sourceCandidate(Candidate candidate, MultipartFile file, String fileName, String roundId, String positionCode)
			throws Exception {

		// if org is agency and position is published to connect, sync
		// candidates with corporate
		Position position = positionService.getPositionByCode(positionCode);
		if (null != position) {
			Round round = null;
			if (roundId == null || roundId.trim().isEmpty())
				round = roundService.getRoundByBoardAndType(position.getBoard(), RoundType.Source.getDisplayName());
			else
				round = roundService.findOne(Long.valueOf(roundId));

			Organization org = userService.getLoggedInUserObject().getOrganization();
			if (GlobalConstants.PUBLISH_MODE_CONNECT.equals(position.getPublishMode())
					&& GlobalConstants.SIGNUP_MODE_AGENCY.equalsIgnoreCase(org.getOrgType())) {
				ResponseEntity<RestResponse> restResponse = recruizConnectService.sourceCandidateToPosition(
						position.getConnectCorporateId(), position.getConnectInstanceId(), position.getPositionCode(), round, candidate);
				if (!restResponse.getBody().isSuccess())
					throw new RecruizWarnException((String) restResponse.getBody().getData(), (String) restResponse.getBody().getReason());
			}
		}

		candidate = addSourceCandidateIntoDB(candidate, fileName);

		if (file != null && !file.isEmpty()) {
			File resumeFile = fileService.multipartToFile(file);
			uploadCandidateFile(candidate, resumeFile);
		}

		addSourceRoundCandidate(candidate, roundId, positionCode);
		return candidate;
	}

	
	public Candidate sourceCandidate(Candidate candidate, File resumeFile, String roundId, String positionCode)
			throws RecruizException, IOException, RecruizWarnException {

		candidate = setDefaultValues(candidate);
		if (candidate.getEducationDetails() != null && !candidate.getEducationDetails().isEmpty()) {
			for (CandidateEducationDetails educationDetails : candidate.getEducationDetails()) {
				educationDetails.setCandidate(candidate);
			}
		}
		candidate.setSourcedOnDate(new Date());
		save(candidate);

		if (resumeFile != null) {
			uploadCandidateFile(candidate, resumeFile);
		}
		addSourceRoundCandidate(candidate, roundId, positionCode);
		return candidate;
	}

	
	private void addSourceRoundCandidate(Candidate candidate, String roundId, String positionCode)
			throws IOException, RecruizException, RecruizWarnException {
		// adding cover page here
		candidateFileService.uploadCandidateCoverLetter(candidate);

		// adding profile pic in pubset folder
		if ((candidate.getImageContent() != null && !candidate.getImageContent().isEmpty())
				&& (candidate.getImageName() != null && !candidate.getImageName().isEmpty())) {
			byte[] imageBytes = Base64.decode(candidate.getImageContent().getBytes());
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
					save(candidate);
				}
			}
		}
		Round round = null;
		if (roundId == null || roundId.trim().isEmpty()) {
			if (positionCode != null && !positionCode.isEmpty()) {
				Position position = positionService.getPositionByCode(positionCode);
				if (position == null)
					throw new RecruizWarnException(ErrorHandler.NO_POSITION, ErrorHandler.NO_POSITION);
				round = roundService.getRoundByBoardAndType(position.getBoard(), RoundType.Source.getDisplayName());
				RoundCandidate roundCandidate = new RoundCandidate();
				roundCandidate.setCandidate(candidate);
				roundCandidate.setStatus(BoardStatus.InProgress.toString());
				roundCandidate.setRoundId(String.valueOf(round.getId()));
				roundCandidate.setRound(round);
				roundCandidate.setPositionCode(positionCode);
				roundCandidate.setSourcedBy(userService.getLoggedInUserEmail());
				roundCandidateService.save(roundCandidate);

				// making entry to candidate activity
				candidateActivityService.addedToBoardEvent(candidate, roundCandidate);
			}
		} else {
			if (roundId != null && !roundId.isEmpty()) {
				round = roundService.findOne(Long.parseLong(roundId));
				RoundCandidate roundCandidate = new RoundCandidate();
				roundCandidate.setCandidate(candidate);
				roundCandidate.setStatus(BoardStatus.InProgress.toString());
				roundCandidate.setRoundId(roundId);
				roundCandidate.setRound(round);
				roundCandidate.setPositionCode(positionService.getPositionByBoard(round.getBoard()).getPositionCode());
				roundCandidate.setSourcedBy(userService.getLoggedInUserEmail());
				roundCandidateService.save(roundCandidate);

				// making entry to candidate activity
				candidateActivityService.addedToBoardEvent(candidate, roundCandidate);
			}
		}
	}

	@Transactional
	private Candidate addSourceCandidateIntoDB(Candidate candidate, String fileName) throws RecruizException {
		if (fileName != null && !fileName.isEmpty()) {
			fileName = StringUtils.cleanFileName(fileName);
		}

		candidate = setDefaultValues(candidate);
		if (candidate.getEducationDetails() != null && !candidate.getEducationDetails().isEmpty()) {
			for (CandidateEducationDetails educationDetails : candidate.getEducationDetails()) {
				educationDetails.setCandidate(candidate);
			}
		}
		candidate.setSourcedOnDate(new Date());
		addCandidate(candidate);
		return candidate;
	}


	public void uploadCandidateFile(Candidate candidate, File resumeFile) throws RecruizException, IOException {
		if (candidate.getCid() > 0) {
			try {
				uploadFileService.createFolderStructureForCandidate(folderPath, candidate.getCid() + "");
				
			    String originalResume = uploadFileService.uploadFileToLocalServer(resumeFile, StringUtils.cleanFileName(resumeFile.getName()),"resume", candidate.getCid() + "");
				
				String convertedResume = fileService.convert(originalResume);
				updateCandidateResume(candidate, convertedResume);

				// add to resume docs
				String resumePath = uploadFileService.uploadFileToLocalServer(resumeFile, StringUtils.cleanFileName(resumeFile.getName()),
						FileType.Original_Resume.getDisplayName(), candidate.getCid() + "");
				// convert file first then upload it.
				String convertedResumePath = fileService.convert(resumePath);
				uploadCandidateFiles(resumePath, resumeFile.getName(), FileType.Original_Resume.getDisplayName(), "new",
						candidate.getCid() + "", convertedResumePath);

				// masking resume

				Map<String, String> maskedResumeFiles = maskResume(candidate.getCid());
				if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
					attachMaskedResumeToCadidate(maskedResumeFiles, candidate.getCid());
				}
				
				
				if(fileStorageMode!=null && fileStorageMode.equalsIgnoreCase("aws")){
					uploadFileService.createFolderStructureInAWSForCandidate(folderPath, candidate.getCid() + "");	
					
					//upload docs file of candidate in aws original resume
					uploadFileService.uploadFileToAWSServer(resumeFile, StringUtils.cleanFileName(resumeFile.getName()),FileType.Original_Resume.getDisplayName(), candidate.getCid() + "");
					
					//upload pdf file of candidate in aws original resume
					File resumePdf = new File(convertedResume);
					String pdfAwsPath = uploadFileService.uploadFileToAWSServer(resumePdf, StringUtils.cleanFileName(resumePdf.getName()),FileType.Original_Resume.getDisplayName(), candidate.getCid() + "");
					
					updateCandidateResume(candidate, pdfAwsPath);
					uploadCandidateFiles(pdfAwsPath.split("\\.")[0]+".docx", resumeFile.getName(), FileType.Original_Resume.getDisplayName(), "new",
							candidate.getCid() + "", pdfAwsPath);
					
					String orgType = organizationService.getCurrentOrganization().getOrgType();
					if (!orgType.equalsIgnoreCase("Corporate")) {
						
				//	read masked files and upload to aws
					uploadFileService.readMaskedFilesAndUploadInAws(candidate.getCid());
					}	
				
					//delete candidate folder data	
					/*File candidateFolder = new File(
							folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
									+ File.separator + "candidate" + File.separator + candidate.getCid());
				
					System.out.println("going to delete folder ====");
					if (candidateFolder.exists())
						FileUtils.deleteDirectory(candidateFolder);*/
						
				}	
				

			} catch (Exception ex) {
				logger.warn("\n\n\n*******Failed to mask resume*********", ex);
				FileUtils.deleteDirectory(new File(
						folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
						+ File.separator + "candidate" + File.separator + 11));
			
			}

		}
	}

	@Transactional
	public Candidate candidateBulkParserSave(Candidate candidate)
			throws IOException, RecruizException, MySQLIntegrityConstraintViolationException {

		if (candidate == null) {
			return candidate;
		}
		if (null == candidate.getFullName() || candidate.getFullName().trim().isEmpty()) {
			throw new RecruizException(ErrorHandler.CANDIDATE_NAME_MISSING, ErrorHandler.CANDIDATE_NAME_MANDATORY);
		}
		try {

			if (isCandidateExists(candidate.getEmail())) {
				// updating candidate here
				candidate = setDefaultValues(candidate);
				Candidate existingCandidate = candidateRepository.findByEmail(candidate.getEmail());
				candidate = existingCandidate.copy(candidate);
				setSourceInfo(existingCandidate, userService.getLoggedInUserEmail(), GlobalConstants.SOURCED_BY_HR, null);
				candidate.setS3Enabled(false);

				// making entry to candidate activity after adding
				candidateActivityService.addActivity("Candidate details updated", candidate.getOwner(), candidate.getCid() + "",
						CandidateActivityType.DetailsUpdated.getDisplayName());

			} else {
				candidate.setSourcedOnDate(new Date());
				candidate.setOwner(userService.getLoggedInUserEmail());
				candidate = setDefaultValues(candidate);
				candidate.setSource(GlobalConstants.SOURCED_BY_HR);
			}
			save(candidate);

			// making entry to candidate activity after adding
			candidateActivityService.addActivity("Candidate added", candidate.getOwner(), candidate.getCid() + "",
					CandidateActivityType.Added.getDisplayName());

		} catch (DataIntegrityViolationException ex) {
			// implemented this block because race candition was causing adding
			// of one candidate at same time
			Candidate cand = candidateRepository.findByEmail(candidate.getEmail());
			if (cand != null) {
				candidate = cand;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e);
		}

		return candidate;
	}

	@Transactional
	public Candidate candidateBulkParserSaveData(Candidate candidate, String batchId)
			throws IOException, RecruizException, MySQLIntegrityConstraintViolationException {

		if (candidate == null) {
			return candidate;
		}
		if (null == candidate.getFullName() || candidate.getFullName().trim().isEmpty()) {
			throw new RecruizException(ErrorHandler.CANDIDATE_NAME_MISSING, ErrorHandler.CANDIDATE_NAME_MANDATORY);
		}
		try {

			if (isCandidateExists(candidate.getEmail())) {
				// updating candidate here
				candidate = setDefaultValues(candidate);
				Candidate existingCandidate = candidateRepository.findByEmail(candidate.getEmail());
				candidate = existingCandidate.copy(candidate);
				setSourceInfo(existingCandidate, userService.getLoggedInUserEmail(), GlobalConstants.SOURCED_BY_HR, null);
				candidate.setS3Enabled(false);

				// making entry to candidate activity after adding
				candidateActivityService.addActivity("Candidate details updated", candidate.getOwner(), candidate.getCid() + "",
						CandidateActivityType.DetailsUpdated.getDisplayName());

			} else {
				candidate.setSourcedOnDate(new Date());
				candidate.setOwner(userService.getLoggedInUserEmail());
				candidate = setDefaultValues(candidate);
				candidate.setSource(GlobalConstants.SOURCED_BY_HR);
			}
			CandidateResumeBulkUploadBatch candidateResumeBulkUploadBatch = candidateResumeBulkUploadBatchService.findByBatchId(batchId);
			if(candidateResumeBulkUploadBatch!=null){
				if(candidateResumeBulkUploadBatch.getSource()!=null){
					candidate.setSource(candidateResumeBulkUploadBatch.getSource());
				}

			}

			Candidate candidateNew = save(candidate);

			if(candidateNew!=null){
				if(candidateResumeBulkUploadBatch.getFolderId()!=null && !candidateResumeBulkUploadBatch.getFolderId().equalsIgnoreCase("undefined")){
					Collection<Long> candidateIds = new ArrayList<>();
					candidateIds.add(candidateNew.getCid());

					long folder_Id = Long.parseLong(candidateResumeBulkUploadBatch.getFolderId());
					Folder folder = folderRepository.findOne(folder_Id);

					if(folder!=null)
						candidateFolderService.addCandidatesToFolder(candidateIds, folder.getDisplayName());
				}
			}

			// making entry to candidate activity after adding
			candidateActivityService.addActivity("Candidate added", candidate.getOwner(), candidate.getCid() + "",
					CandidateActivityType.Added.getDisplayName());

		} catch (DataIntegrityViolationException ex) {
			// implemented this block because race candition was causing adding
			// of one candidate at same time
			Candidate cand = candidateRepository.findByEmail(candidate.getEmail());
			if (cand != null) {
				candidate = cand;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e);
		}

		return candidate;
	}



	/**
	 * Used only by bulk upload to control the number of request sent to parser
	 * server
	 *
	 * @param resumeFile
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	public Candidate addResumeFileAsCandidateToQueue(File resumeFile) throws RecruizException, IOException {
		return addResumeFileAsCandidate(resumeFile, true);
	}

	public Candidate addResumeFileAsCandidate(File resumeFile) throws RecruizException, IOException {
		return addResumeFileAsCandidate(resumeFile, false);
	}

	private Candidate addResumeFileAsCandidate(File resumeFile, boolean queue) throws RecruizException, IOException {
		logger.error("###########call  queueParseResume() method from  CandidateService.java #############");
		Candidate candidate = null;
		try {
			if (queue)
				candidate = resumeParserService.queueParseResume(resumeFile);
			else
				candidate = resumeParserService.parseResume(resumeFile);
		} catch (Exception e) {
			throw new RecruizException(e.getMessage(), e);
		}

		if (candidate == null) {
			throw new RecruizException("Parsing candidate information failed");
		}

		if (candidate.getEmail() == null || candidate.getEmail().isEmpty()) {
			throw new RecruizException("Candidate email information missing");
		}

		// decide to implement it later so commenting the count update code

		/*
		 * // making entry to parser count ParserCount parserCount = new
		 * ParserCount();
		 * parserCount.setUsedBy(userService.getLoggedInUserEmail());
		 * parserCountService.save(parserCount);
		 */
		return candidate;
	}

	// @Transactional
	// public Map<String, Object> pluginCandidateUpload(MultipartFile file,
	// String source)
	// throws IOException, RecruizException, RecruizCandidateExistException {
	// FileUploadRequestDTO fileUploadRequestDTO = new FileUploadRequestDTO();
	// fileUploadRequestDTO.setCreationDate(DateTime.now().toDate());
	// fileUploadRequestDTO.setFileName(file.getOriginalFilename());
	// fileUploadRequestDTO.setSource(source);
	// fileUploadRequestDTO.setSourceDetails("Uploaded via plugin by '" +
	// userService.getLoggedInUserName() + "'");
	// fileUploadRequestDTO.setFilebytes(file.getBytes());
	// if
	// (userService.getLoggedInUserObject().getUserType().equalsIgnoreCase(UserType.APP.getDisplayName()))
	// fileUploadRequestDTO.setOverwrite(true);
	//
	// return saveCandidateViaResumeFile(fileUploadRequestDTO);
	// }

	// public List<FileUploadResponseDTO>
	// quickBulkUpload(List<FileUploadRequestDTO> uploadDTO)
	// throws IOException, RecruizCandidateExistException {
	// List<FileUploadResponseDTO> result = new ArrayList<>();
	// for (FileUploadRequestDTO dto : uploadDTO) {
	// dto.setSourceDetails("Bulk upload by '" +
	// userService.getLoggedInUserName() + "'");
	// FileUploadResponseDTO response = new FileUploadResponseDTO();
	// try {
	// saveCandidateViaResumeFile(dto);
	// response.setResult(true, "File : " + dto.getFileName() + " processed");
	// } catch (RecruizException e) {
	// response.setResult(false, e.getMessage());
	// }
	// result.add(response);
	// }
	// return result;
	// }

	/*
	 * @Transactional public Map<String, Object>
	 * saveCandidateViaResumeFile(FileUploadRequestDTO fileUploadRequestDTO)
	 * throws RecruizException, RecruizCandidateExistException {
	 * FileUploadResponseDTO result = new FileUploadResponseDTO();
	 *
	 * result.setFileName(fileUploadRequestDTO.getFileName()); File resumeFile =
	 * null; try { resumeFile =
	 * FileUtils.writeToFile(fileUploadRequestDTO.getFileName(),
	 * fileUploadRequestDTO.getFilebytes());
	 *
	 * if (resumeFile == null || resumeFile.length() == 0) { throw new
	 * RecruizException("Empty File");
	 *
	 * } else {
	 *
	 * Candidate candidate = addResumeFileAsCandidate(resumeFile);
	 * candidate.setSourcedOnDate(fileUploadRequestDTO.getCreationDate());
	 * candidate.setSourceDetails(fileUploadRequestDTO.getSourceDetails());
	 *
	 * // fill N/A if no values or null candidate = setDefaultValues(candidate);
	 *
	 * if (fileUploadRequestDTO.getSource() == null ||
	 * fileUploadRequestDTO.getSource().isEmpty()) {
	 * candidate.setSource(GlobalConstants.SOURCED_BY_HR); } else {
	 * candidate.setSource(fileUploadRequestDTO.getSource()); }
	 *
	 * candidate = saveCandidateToDB(candidate, fileUploadRequestDTO);
	 *
	 * // adding original resume to candidate files
	 * uploadCandidateFiles(resumeFile, fileUploadRequestDTO.getFileName(),
	 * FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() +
	 * "");
	 *
	 * Map<String, Object> candidateSaveMap = new HashMap<>();
	 * candidateSaveMap.put("candidate", candidate);
	 * candidateSaveMap.put("isUpdated",
	 * fileUploadRequestDTO.isCandidateUpdated());
	 *
	 * return candidateSaveMap;
	 *
	 * } } catch (RecruizCandidateExistException ex) { throw ex; } catch
	 * (Exception ex) { throw new RecruizException(ex.getMessage(), ex); }
	 * finally { if (resumeFile != null && resumeFile.exists()) {
	 * resumeFile.delete(); } }
	 *
	 * }
	 */

	@Transactional
	public Candidate saveCandidateToDB(Candidate candidate, FileUploadRequestDTO fileUploadRequestDTO)
			throws RecruizCandidateExistException, RecruizException, IOException {

		// if candidate exist and if overwrite is false then throw
		// exception
		if (isCandidateExists(candidate.getEmail())) {
			if (fileUploadRequestDTO.isOverwrite()) {
				Candidate candidateFromDB = candidateRepository.findByEmail(candidate.getEmail());
				if (candidateFromDB != null) {
					candidate = candidateFromDB.copy(candidate);
					fileUploadRequestDTO.setCandidateUpdated(true);
					setSourceInfo(candidateFromDB, userService.getLoggedInUserEmail(), fileUploadRequestDTO.getSource(), null);
				}
			} else {
				throw new RecruizCandidateExistException(candidate.getEmail());
			}
		}

		// removing bad skills
		List<String> badSkillSet = StringUtils.commaSeparateStringToList(GlobalConstants.BAD_SKILLS);
		if (null != candidate.getKeySkills() && !candidate.getKeySkills().isEmpty()) {
			candidate.getKeySkills().removeAll(badSkillSet);
		}

		candidate = save(candidate);

		// upload resume to server
		uploadFileService.createFolderStructureForCandidate(folderPath, candidate.getCid() + "");
		String serverPath = uploadFileService.uploadFileToLocalServer(fileUploadRequestDTO.getFilebytes(),
				fileUploadRequestDTO.getFileName(), "resume", candidate.getCid() + "");

		String pdfFilePath = fileService.convert(serverPath);
		candidate.setResumeLink(pdfFilePath);

		// making entry to candidate activity after adding
		candidateActivityService.addActivity("Candidate added", candidate.getOwner(), candidate.getCid() + "",
				CandidateActivityType.Added.getDisplayName());

		// adding converted resume to candidate files
		if (pdfFilePath.endsWith(".pdf")) {
			CandidateFile candidateFile = new CandidateFile();
			candidateFile.setCompanyType("new");
			candidateFile.setFileName(fileUploadRequestDTO.getFileName());
			candidateFile.setFilePath(serverPath);
			candidateFile.setFileType(FileType.Original_Converted_Resume.getDisplayName());
			candidateFile = candidateFileService.save(candidateFile);
			candidate.getFiles().add(candidateFile);
		}

		candidate = save(candidate);
		return candidate;
	}

	
	public void uploadCandidateFiles(String serverPath, String fileName, String fileType, String companyType, String candidateId,
			String pdfFilePath) throws RecruizException, IOException {

		if (fileType.equalsIgnoreCase(FileType.Masked_Resume_Original.getDisplayName())) {
			List<CandidateFile> maskedResumeFiles = new ArrayList<>();

			CandidateFile maskedResume = new CandidateFile();
			maskedResume = createAndReturnCandidateFile(fileName, companyType, candidateId, serverPath, maskedResume,
					FileType.Masked_Resume_Original.getDisplayName());
			if(serverPath.contains(BUCKET_NAME)){
				maskedResume.setStorageMode("aws");
			}
			
			maskedResumeFiles.add(maskedResume);

			if (pdfFilePath.endsWith(".pdf")) {
				CandidateFile maskedResumeConvertedPdf = new CandidateFile();
				maskedResumeConvertedPdf = createAndReturnCandidateFile(fileName, companyType, candidateId, pdfFilePath,
						maskedResumeConvertedPdf, FileType.Masked_Resume_Converted.getDisplayName());
				if(serverPath.contains(BUCKET_NAME)){
					maskedResumeConvertedPdf.setStorageMode("aws");
				}
				maskedResumeFiles.add(maskedResumeConvertedPdf);
			}
			candidateFileService.save(maskedResumeFiles);

		} else if (fileType.equalsIgnoreCase(FileType.Original_Resume.getDisplayName())) {
			// deleting resume if it exists for this candidate
			List<CandidateFile> existingResumeFiles = candidateFileService
					.getCandidateFileByTypeAndId(FileType.Original_Resume.getDisplayName(), candidateId);
			if (null != existingResumeFiles && !existingResumeFiles.isEmpty()) {
				candidateFileService.delete(existingResumeFiles);
			}

			List<CandidateFile> resumeFiles = new ArrayList<>();

			CandidateFile originalResume = new CandidateFile();
			originalResume = createAndReturnCandidateFile(fileName, companyType, candidateId, serverPath, originalResume,
					FileType.Original_Resume.getDisplayName());
			
			if(serverPath.contains(BUCKET_NAME)){
				originalResume.setStorageMode("aws");
			}
			
			resumeFiles.add(originalResume);

			if (pdfFilePath.endsWith(".pdf")) {

				// deleting converted resume if it exists for this candidate
				List<CandidateFile> existingConvertedResumeFiles = candidateFileService
						.getCandidateFileByTypeAndId(FileType.Original_Converted_Resume.getDisplayName(), candidateId);
				if (null != existingConvertedResumeFiles && !existingConvertedResumeFiles.isEmpty()) {
					candidateFileService.delete(existingConvertedResumeFiles);
				}

				CandidateFile originalResumePdfVersion = new CandidateFile();
				originalResumePdfVersion = createAndReturnCandidateFile(fileName, companyType, candidateId, pdfFilePath,
						originalResumePdfVersion, FileType.Original_Converted_Resume.getDisplayName());
				
				if(serverPath.contains(BUCKET_NAME)){
					originalResumePdfVersion.setStorageMode("aws");
				}
				resumeFiles.add(originalResumePdfVersion);
			}
			candidateFileService.save(resumeFiles);

		}

		// since the file is same path and it is added to candidate db we dont
		// need to update db path entry as the path hasn't changed
		List<CandidateFile> existingFiles = candidateFileService.getCandidateFileByTypeAndId(fileType, candidateId);
		if (existingFiles != null && !existingFiles.isEmpty()) {
			for (CandidateFile candidateFile : existingFiles) {
				if (candidateFile.getFileName().equalsIgnoreCase(fileName)) {
					return;
				}
			}
		}

		CandidateFile candidateFile = new CandidateFile();
		candidateFile.setCompanyType(companyType);
		candidateFile.setFileName(fileName);
		candidateFile.setFilePath(serverPath);
		candidateFile.setFileType(fileType);
		candidateFile.setCandidateId(candidateId);

		if(serverPath.contains(BUCKET_NAME)){
			candidateFile.setStorageMode("aws");
		}
		
		candidateFile = candidateFileService.save(candidateFile);
	}

	private CandidateFile createAndReturnCandidateFile(String fileName, String companyType, String candidateId, String serverPath,
			CandidateFile candidateFile, String fileType) {

		// checking if the same file exists in db then returning the existing
		// file instead of creating a new object
		List<CandidateFile> file = candidateFileService.getCandidateFileByTypeAndId(fileType, candidateId);
		if (file != null && !file.isEmpty()) {
			for (CandidateFile existingFile : file) {
				if (existingFile.getFileName().equalsIgnoreCase(fileName)) {
					return existingFile;
				}
			}
		}

		candidateFile.setCompanyType(companyType);
		candidateFile.setFileName(fileName);
		candidateFile.setFilePath(serverPath);
		candidateFile.setFileType(fileType);
		candidateFile.setCandidateId(candidateId);
		return candidateFile;
		// FileType.Original_Resume.getDisplayName()
	}

	// converting multi part to file in temp folder
	public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException, RecruizWarnException {
		File convFile = fileService.multipartToFile(multipart);
		/*
		 * File convFile = new File(tempFilePath +
		 * multipart.getOriginalFilename()); multipart.transferTo(convFile);
		 */
		return convFile;
	}

	/**
	 * To get candidate info along with position description
	 *
	 * @param candidateId
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	
	public Map<String, Object> getCandidateDetailsForExternalUser(long candidateId, String positionCode, String mskd)
			throws RecruizException, IOException {
		Candidate candidate = candidateRepository.findOne(candidateId);
		setCandidatePublicProfileUrl(candidate);
		Position position = positionService.getPositionByCode(positionCode);
		if (candidate == null || position == null) {
			return null;
		}
		// marked as masked candidate hiding the info
		if (mskd != null && !mskd.isEmpty() && mskd.equalsIgnoreCase("y")) {
			candidate.setFullName("Hidden");
			candidate.setEmail("Hidden");
			candidate.setAlternateEmail("Hidden");
			candidate.setMobile("XXXXXXXXXXXX");
			candidate.setAlternateMobile("XXXXXXXXXXXX");

			// why list ? -> bcoz all candidate related files belong to one
			// object called candidate files.
			List<CandidateFile> candidateMaskedResume = candidateFileService
					.getCandidateFileByTypeAndId(FileType.Masked_Resume_Converted.getDisplayName(), candidateId + "");
			if (candidateMaskedResume != null && !candidateMaskedResume.isEmpty()) {
				String maskedResumeFilePath = candidateMaskedResume.get(0).getFilePath();
				candidate.setResumeLink(maskedResumeFilePath);
			} else {
				candidate.setResumeLink("");
			}
		}
		candidate.getKeySkills().size();
		candidate.getEducationDetails().size();
		candidate.getCustomField().size();
		getEmploymentTypeDisplayName(candidate);
		byte[] fileBytes = null;
		String resumeName = "";
		if (candidate.getResumeLink() != null && !candidate.getResumeLink().isEmpty()) {
			File resume = new File(candidate.getResumeLink());
			if (resume != null && resume.exists()) {
				resumeName = resume.getName();
				fileBytes = Files.readAllBytes(resume.toPath());
			}
		}

		String fileData = null;
		if (fileBytes != null && fileBytes.length > 1) {
			fileData = org.apache.tomcat.util.codec.binary.Base64.encodeBase64String(fileBytes);
		}

		String description = position.getDescription();
		Map<String, Object> candidateInfoMapForExternalUser = new HashMap<>();
		candidateInfoMapForExternalUser.put("candidateInfo", candidate);
		candidateInfoMapForExternalUser.put("positionDescription", description);
		candidateInfoMapForExternalUser.put("candidateFile", fileData);
		candidateInfoMapForExternalUser.put("candidateFileName", resumeName);

		return candidateInfoMapForExternalUser;
	}


	public Candidate setCandidatePublicProfileUrl(Candidate candidate) {
		if (candidate != null && candidate.getProfileUrl() != null && !candidate.getProfileUrl().isEmpty()) {
			String url = baseUrl + "/pubset/" + candidate.getProfileUrl();
			candidate.setPublicProfileUrl(url);
		}
		return candidate;
	}

	// to set public profile pic url
	@Transactional(readOnly = true)
	public List<Candidate> setCandidatePublicProfileUrl(List<Candidate> candidateList) {
		if (candidateList != null && !candidateList.isEmpty()) {
			for (Candidate candidate : candidateList) {
				if (candidate != null && candidate.getProfileUrl() != null && !candidate.getProfileUrl().isEmpty()) {
					String url = baseUrl + "/pubset/" + candidate.getProfileUrl();
					candidate.setPublicProfileUrl(url);
				}
			}
		}
		return candidateList;
	}

	// update candidate resume after creating the candidate

	public void updateCandidateResume(Candidate candidate, String resumePath) {
		candidate.setResumeLink(resumePath);
		candidateRepository.save(candidate);
	}

	@Transactional(readOnly = true)
	public List<Long> getCandidateids() {
		return candidateRepository.getCandidateIds();
	}

	@Transactional(readOnly = true)
	public List<Long> findCandidateIdsBySourcebyBetweenDate(String owner, Date startDate, Date endDate) {
		return candidateRepository.findCandidateIdsBySourcebyBetweenDate(owner, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public List<BigInteger> getCandidateidsOfLocalFiles() {
		return candidateRepository.getCandidateIdsForlocalFiles();
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidatesByIds(List<Long> idList) {
		return candidateRepository.findByCidIn(idList);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidateByEmailIdsIn(List<String> emails) {
		return candidateRepository.findByEmailIn(emails);
	}

	@Transactional(readOnly = true)
	public List<Candidate> getTop1000CandidateByS3Status(boolean b) {
		return candidateRepository.findTop1000ByS3EnabledIsTrue();
	}

	@Transactional(readOnly = true)
	public Page<Candidate> getAllS3UploadedCandidate(Pageable pageable) {
		return candidateRepository.findByS3EnabledIsTrue(pageable);
	}

	@Transactional(readOnly = true)
	public long getCountByS3Enabled() {
		return candidateRepository.countByS3EnabledIsTrue();
	}

	@Transactional(readOnly = true)
	public List<Candidate> getDummyCandidate() {
		return candidateRepository.findByDummy(true);
	}

	/**
	 * To create masked resume file
	 *
	 * @param cid
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 * @throws InvalidFormatException
	 */
	public Map<String, String> maskResume(long cid) {
		Map<String, String> maskedFileMap = new HashMap<>();
		try {

			// if organization is not of type agency then return null so that
			// files
			// will not be masked for them
			String orgType = organizationService.getCurrentOrganization().getOrgType();
			if (orgType.equalsIgnoreCase("Corporate")) {
				return null;
			}

			Candidate candidate = candidateRepository.findOne(cid);

			List<CandidateFile> originalResume = candidateFileService.getCandidateFileByTypeAndId(FileType.Original_Resume.getDisplayName(),
					cid + "");

			if (originalResume != null && !originalResume.isEmpty()) {
				for (CandidateFile file : originalResume) {
					String watermarkedMaskedFilePath = "";
					// passing empty header text for masked file
					String headerText = " ";
					String maskedFilePath = "";

					String maskedFileDirectory = folderPath + File.separator + "files" + File.separator + TenantContextHolder.getTenant()
					+ File.separator + "candidate" + File.separator + cid + File.separator + "Masked Resume";

					File maskedDirectory = new File(maskedFileDirectory);
					if (!maskedDirectory.exists()) {
						maskedDirectory.mkdirs();
					}

					Map<String, String> textToReplaceInFile = new HashMap<String, String>();
					String alternateEmail = "", mobile = "", alternateMobile = "";
					String email = candidate.getEmail();

					if (null != candidate.getMobile() && !candidate.getMobile().isEmpty()) {
						mobile = candidate.getMobile().replaceAll(" ", "");
					}
					if (null != candidate.getAlternateMobile() && !candidate.getAlternateMobile().isEmpty()) {
						alternateMobile = candidate.getAlternateMobile().replaceAll(" ", "");
					}

					if (null != candidate.getAlternateEmail() && !candidate.getAlternateEmail().isEmpty()) {
						alternateEmail = candidate.getAlternateEmail().replaceAll(" ", "");
					}

					String name = candidate.getFullName();

					// adding name to replace in a map
					if (name != null && !name.isEmpty()) {
						name = name.trim();
						String[] nameArray = name.split(" ");
						String nameToReplace = name;
						if (nameArray.length > 0) {
							for (String namePart : nameArray) {
								if (namePart.trim().length() > 2) {
									nameToReplace = namePart.trim();
									break;
								}
							}
						}
						textToReplaceInFile.put(nameToReplace, "XXXXXXXXXX");
					}

					if (mobile != null && !mobile.isEmpty()) {
						String mobileLast3Digit = mobile.substring(mobile.length() - 3, mobile.length());
						textToReplaceInFile.put(mobileLast3Digit, "***");
					}

					if (alternateMobile != null && !alternateMobile.isEmpty()) {
						String alternateMobileLast3Digit = alternateMobile.substring(alternateMobile.length() - 3,
								alternateMobile.length());
						textToReplaceInFile.put(alternateMobileLast3Digit, "***");
					}

					if (email != null && !email.isEmpty()) {
						String emailKey = email.substring(0, email.indexOf("@"));
						textToReplaceInFile.put(emailKey, "**********");
					}

					if (alternateEmail != null && !alternateEmail.isEmpty()) {
						String emailKey = alternateEmail.substring(0, alternateEmail.indexOf("@"));
						textToReplaceInFile.put(emailKey, "**********");
					}

					if (file.getFilePath().endsWith("doc")) {
						maskedFilePath = maskedFileDirectory + File.separator + "maskedFile.doc";
						if (file.getFilePath().startsWith("http")) {
							// thsi will mask name/email and mobile
							maskedFilePath = maskRemoteFile(candidate, file, maskedFilePath, maskedFileDirectory);
						} else {
							// masking email here
							maskedFilePath = maskEmail(candidate, file, maskedFilePath, maskedFileDirectory);

							// masking name here
							// System.out.println("\n\n\n***********************Masking
							// Name*********");
							String nameMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis()
							+ "nameMaskedFile.doc";
							maskedFilePath = maskName(candidate, maskedFilePath, nameMaskedFilePath);

							// masking mobile here
							String mobileMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis()
							+ "mobileMaskedFile.doc";
							maskedFilePath = maskMobile(candidate.getMobile(), maskedFilePath, maskedFileDirectory, nameMaskedFilePath,
									mobileMaskedFilePath);

							// System.out.println("\n\n\n***********************Mobile
							// masked *********");

							// masking alternate mobile here
							if (null != candidate.getAlternateMobile() && !candidate.getAlternateMobile().isEmpty()) {
								String alternateMobileMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis()
								+ "mobileMaskedFile.doc";
								maskedFilePath = maskMobile(candidate.getAlternateMobile(), maskedFilePath, maskedFileDirectory,
										mobileMaskedFilePath, alternateMobileMaskedFilePath);
							}
							//// System.out.println("\n\n\n***********************all
							//// masking done for doc*********");
						}

					} else if (file.getFilePath().endsWith("docx")) {
						maskedFilePath = maskedFileDirectory + File.separator + "maskedFile.docx";
						if (file.getFilePath().startsWith("http")) {
							File remoteFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), file.getFilePath());
							fileService.replaceDocxWordFile(remoteFile.getAbsolutePath(), maskedFilePath, textToReplaceInFile,
									organizationService.getCurrentOrganization().getOrgName());
						} else {
							fileService.replaceDocxWordFile(file.getFilePath(), maskedFilePath, textToReplaceInFile,
									organizationService.getCurrentOrganization().getOrgName());
						}
					}

					// System.out.println("\n\n\n***********************
					// converting masked file *********");
					// check if masked file exists in file system
					File maskedFile = new File(maskedFilePath);
					if (maskedFile.exists()) {
						maskedFileMap.put("originalMaskedFile", maskedFilePath);
						String maskedPdfFilePath = fileService.convert(maskedFilePath);
						// check if converted file exists on file system then
						// add
						// water mark and header to pdf file
						File maskedPdfFile = new File(maskedPdfFilePath);
						if (maskedPdfFile.exists() && maskedPdfFilePath.endsWith("pdf")) {

							String headerFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis() + ".pdf";

							fileService.addHeaderInPdf(maskedPdfFilePath, headerFilePath, headerText);

							watermarkedMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis() + ".pdf";

							String logoUrl = "";
							if (organizationService.getCurrentOrganization().getLogoUrlPath() != null
									&& !organizationService.getCurrentOrganization().getLogoUrlPath().isEmpty()) {
								logoUrl = baseUrl + "/pubset/" + organizationService.getCurrentOrganization().getLogoUrlPath();
							}

							fileService.imageWatermarkPDF(headerFilePath, watermarkedMaskedFilePath, logoUrl,
									organizationService.getCurrentOrganization().getOrgName());
							File headerFile = new File(headerFilePath);
							if (headerFile.exists()) {
								headerFile.delete();
							}

							// deleting converted original pdf file
							maskedPdfFile.delete();
						}

						maskedFileMap.put("WatermarkedFile", watermarkedMaskedFilePath);
					}
				}
			}

		} catch (Exception th) {
			logger.warn(th.getMessage(), th);
		}

		return maskedFileMap;
	}

	// to mask name , email and mobile in remote file
	private String maskRemoteFile(Candidate candidate, CandidateFile file, String maskedFilePath, String maskedFileDirectory)
			throws IOException {
		File remoteFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), file.getFilePath());

		// masking email id here
		// System.out.println("***********************Masking Emial*********");
		fileService.maskEmailInDoc(remoteFile.getAbsolutePath(), maskedFilePath, candidate.getEmail());
		if (candidate.getAlternateEmail() != null && !candidate.getAlternateEmail().isEmpty()) {
			String maskedFile2 = maskedFileDirectory + File.separator + System.currentTimeMillis() + "maskedFile.doc";
			fileService.maskEmailInDoc(maskedFilePath, maskedFile2, candidate.getAlternateEmail());
			File maskedFile = new File(maskedFilePath);
			if (maskedFile.exists()) {
				maskedFile.delete();
				maskedFilePath = maskedFile2;
			}
		}

		// masking name here
		// System.out.println("\n\n\n***********************Masking
		// Name*********");
		String nameMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis() + "nameMaskedFile.doc";

		maskedFilePath = maskName(candidate, maskedFilePath, nameMaskedFilePath);

		// masking mobile here
		String mobileMaskedFilePath = maskedFileDirectory + File.separator + System.currentTimeMillis() + "mobileMaskedFile.doc";

		String candidateMobileLast3Digit = candidate.getMobile().substring(candidate.getMobile().length() - 3,
				candidate.getMobile().length());

		fileService.maskMobileInDoc(maskedFilePath, mobileMaskedFilePath, candidateMobileLast3Digit);

		File mobileMaskedFile = new File(nameMaskedFilePath);
		if (mobileMaskedFile.exists()) {
			File maskedFile = new File(maskedFilePath);
			maskedFile.delete();
			maskedFilePath = mobileMaskedFilePath;
		}
		return maskedFilePath;
	}

	private String maskEmail(Candidate candidate, CandidateFile file, String maskedFilePath, String maskedFileDirectory)
			throws IOException {
		fileService.maskEmailInDoc(file.getFilePath(), maskedFilePath, candidate.getEmail());
		if (candidate.getAlternateEmail() != null && !candidate.getAlternateEmail().isEmpty()) {
			String maskedFile2 = maskedFileDirectory + File.separator + System.currentTimeMillis() + "maskedFile.doc";
			fileService.maskEmailInDoc(maskedFilePath, maskedFile2, candidate.getAlternateEmail());
			File maskedFile = new File(maskedFilePath);
			if (maskedFile.exists()) {
				maskedFile.delete();
				maskedFilePath = maskedFile2;
			}
		}
		return maskedFilePath;
	}

	// to mak name in doc file
	private String maskName(Candidate candidate, String maskedFilePath, String nameMaskedFilePath) throws IOException {
		String name = candidate.getFullName();
		// adding name to replace in a map
		if (name != null && !name.isEmpty()) {
			name = name.trim();
			String[] nameArray = name.split(" ");
			String nameToReplace = name;
			if (nameArray.length > 0) {
				for (String namePart : nameArray) {
					if (namePart.trim().length() > 2) {
						nameToReplace = namePart.trim();
						break;
					}
				}
			}
			fileService.maskNameInDoc(maskedFilePath, nameMaskedFilePath, nameToReplace);
			File nameMaskedFile = new File(nameMaskedFilePath);
			if (nameMaskedFile.exists()) {
				File maskedFile = new File(maskedFilePath);
				maskedFile.delete();
				maskedFilePath = nameMaskedFilePath;
			}
			return maskedFilePath;
		}

		// returning null if candidate don't have name in any case
		return null;

	}

	private String maskMobile(String mobileNumber, String maskedFilePath, String maskedFileDirectory, String nameMaskedFilePath,
			String mobileMaskedFilePath) throws IOException {

		if (mobileNumber != null) {
			String candidateMobileLast3Digit = mobileNumber.substring(mobileNumber.length() - 3, mobileNumber.length());
			fileService.maskMobileInDoc(maskedFilePath, mobileMaskedFilePath, candidateMobileLast3Digit);
		}

		File mobileMaskedFile = new File(nameMaskedFilePath);
		if (mobileMaskedFile.exists()) {
			File maskedFile = new File(maskedFilePath);
			maskedFile.delete();
			maskedFilePath = mobileMaskedFilePath;
		}

		return maskedFilePath;
	}

	/**
	 * To attach masked resume to candidate
	 *
	 * @param maskedResumeFiles
	 * @param cid
	 */

	public void attachMaskedResumeToCadidate(Map<String, String> maskedResumeFiles, long cid) {
		List<CandidateFile> candidateFilesToAdd = new ArrayList<>();

		for (Map.Entry<String, String> entry : maskedResumeFiles.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("originalMaskedFile")) {
				// if any mask file exists then delete it
				List<CandidateFile> existingOriginalMaskedFile = candidateFileService
						.getCandidateFileByTypeAndId(FileType.Masked_Resume_Original.getDisplayName(), cid + "");

				if (existingOriginalMaskedFile != null) {
					candidateFileService.delete(existingOriginalMaskedFile);
				}

				CandidateFile originalMaskedFile = new CandidateFile();
				originalMaskedFile.setCandidateId(cid + "");
				originalMaskedFile.setFilePath(entry.getValue());
				originalMaskedFile.setFileName(FilenameUtils.getName(entry.getValue()));
				originalMaskedFile.setFileType(FileType.Masked_Resume_Original.getDisplayName());
				candidateFilesToAdd.add(originalMaskedFile);
			} else if (entry.getKey().equalsIgnoreCase("WatermarkedFile")) {

				// if any mask file exists then delete it
				List<CandidateFile> existingOriginalMaskedFile = candidateFileService
						.getCandidateFileByTypeAndId(FileType.Masked_Resume_Converted.getDisplayName(), cid + "");

				if (existingOriginalMaskedFile != null) {
					candidateFileService.delete(existingOriginalMaskedFile);
				}

				CandidateFile convertedMaskedFile = new CandidateFile();
				convertedMaskedFile.setCandidateId(cid + "");
				convertedMaskedFile.setFilePath(entry.getValue());
				convertedMaskedFile.setFileName(FilenameUtils.getName(entry.getValue()));
				convertedMaskedFile.setFileType(FileType.Masked_Resume_Converted.getDisplayName());
				candidateFilesToAdd.add(convertedMaskedFile);
			}
			candidateFileService.save(candidateFilesToAdd);
		}
	}

	/**
	 * to get candidate page with null value
	 *
	 * @param pageble
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<Candidate> getCandidateWithNullRandomId(Pageable pageable) {
		Page<Candidate> candidateWithNullRandomId = candidateRepository.findByCandidateRandomId(pageable, null);
		return candidateWithNullRandomId;
	}

	public void updateRandomCandidateId() {

		double nullRandomIdCount = candidateRepository.countByCandidateRandomId(null);

		int pageSize = 1000;

		nullRandomIdCount = Math.ceil(nullRandomIdCount / pageSize);

		for (int i = 0; i < nullRandomIdCount; i++) {

			Page<Candidate> candidateWithNullRandomIdPage = getCandidateWithNullRandomId(pageableService.defaultPageRequest(i, pageSize));
			if (candidateWithNullRandomIdPage.getContent() != null && !candidateWithNullRandomIdPage.getContent().isEmpty()) {

				List<Candidate> candidateToUpdateList = new LinkedList<>();
				for (Candidate candidate : candidateWithNullRandomIdPage.getContent()) {
					candidate.setCandidateRandomId("C - " + candidate.getCid() + " - " + StringUtils.get6RandomDigit());
					candidateToUpdateList.add(candidate);
				}
				candidateRepository.save(candidateToUpdateList);
			} else {
				logger.warn("******** No Candidate to update random id **********");
			}
		}
	}

	/**
	 * To get converted masked resume path if available
	 *
	 * @param cid
	 * @return
	 */
	public String getConvertedMaskedResumePath(long cid) {
		List<CandidateFile> maskedFiles = candidateFileService
				.getCandidateFileByTypeAndId(FileType.Masked_Resume_Converted.getDisplayName(), cid + "");
		if (maskedFiles != null && !maskedFiles.isEmpty()) {
			return maskedFiles.get(0).getFilePath();
		}
		return null;
	}

	public List<Candidate> getByExternalAppCandidateIdIn(Collection<String> externalIds) {
		return candidateRepository.findByExternalAppCandidateIdIn(externalIds);
	}

	public List<Candidate> getByCandidateSha1HashIn(Collection<String> candidateHashList) {
		return candidateRepository.findByCandidateSha1HashIn(candidateHashList);
	}

	public Candidate getByCandidateSha1HashOrExternalAppCandidateId(String candidateHash, String externalId) {
		return candidateRepository.findByCandidateSha1HashOrExternalAppCandidateId(candidateHash, externalId);
	}

	public String getCandidateFiledName(String name) {
		if (name.equalsIgnoreCase("candidateRandomId")) {
			return "Candidate ID";
		} else if (name.equalsIgnoreCase("fullName")) {
			return "Name";
		} else if (name.equalsIgnoreCase("mobile")) {
			return "Mobile";
		} else if (name.equalsIgnoreCase("email")) {
			return "Email";
		} else if (name.equalsIgnoreCase("currentCompany")) {
			return "Current Company";
		} else if (name.equalsIgnoreCase("currentTitle")) {
			return "Current Title";
		} else if (name.equalsIgnoreCase("currentLocation")) {
			return "Current Location";
		} else if (name.equalsIgnoreCase("highestQual")) {
			return "Highest Qualification";
		} else if (name.equalsIgnoreCase("totalExp")) {
			return "Total Experience";
		} else if (name.equalsIgnoreCase("employmentType")) {
			return "Employment Type";
		} else if (name.equalsIgnoreCase("currentCtc")) {
			return "Current CTC";
		} else if (name.equalsIgnoreCase("expectedCtc")) {
			return "Expected CTC";
		} else if (name.equalsIgnoreCase("noticeStatus")) {
			return "Notice Status";
		} else if (name.equalsIgnoreCase("preferredLocation")) {
			return "Preferred Location";
		} else if (name.equalsIgnoreCase("keySkills")) {
			return "Key Skills";
		} else if (name.equalsIgnoreCase("gender")) {
			return "Gender";
		} else if (name.equalsIgnoreCase("communication")) {
			return "Communication";
		} else if (name.equalsIgnoreCase("linkedinProf")) {
			return "LinkedIn Profile";
		} else if (name.equalsIgnoreCase("githubProf")) {
			return "Github Profile";
		} else if (name.equalsIgnoreCase("twitterProf")) {
			return "Twitter Profile";
		} else if (name.equalsIgnoreCase("facebookProf")) {
			return "Facebook Profile";
		} else if (name.equalsIgnoreCase("alternateEmail")) {
			return "Alternate Email";
		} else if (name.equalsIgnoreCase("alternateMobile")) {
			return "Alternate Mobile";
		} else if (name.equalsIgnoreCase("nationality")) {
			return "Nationality";
		} else if (name.equalsIgnoreCase("maritalStatus")) {
			return "Marital Status";
		} else if (name.equalsIgnoreCase("previousEmployment")) {
			return "Previous Employment";
		} else if (name.equalsIgnoreCase("address")) {
			return "Address";
		} else if (name.equalsIgnoreCase("longestStayInCompany")) {
			return "Longest Stay In Company";
		} else if (name.equalsIgnoreCase("averageStayInCompany")) {
			return "Average Stay In Company";
		} else if (name.equalsIgnoreCase("languages")) {
			return "Languages";
		} else if (name.equalsIgnoreCase("industry")) {
			return "Industry";
		}

		return null;
	}

	// to attach current position to candidate
	public void attachCurrentPosition(List<Candidate> content) {
		if (null != content && !content.isEmpty()) {
			for (Candidate candidate : content) {
				List<RoundCandidate> allOfferedCandidate = roundCandidateService.getAllRoundCandidateByCandidate(candidate);

				for (RoundCandidate roundCandidate : allOfferedCandidate) {
					Position position = positionService.getOneByPositionCode(roundCandidate.getPositionCode());
					candidate.getCurrentPositionMap().put(roundCandidate.getPositionCode(),
							position.getClient().getClientName() + " / " + position.getTitle());
				}
			}
		}
	}

	public void attachCurrentPosition(Candidate candidate) {
		List<Candidate> candidates = new ArrayList<>();
		candidates.add(candidate);
		attachCurrentPosition(candidates);
	}

	public void deleteCustomFieldWithName(String name) {
		candidateRepository.deleteCustomFieldWithName(name);
	}

	public Long getCountByOwnerLikeAndDatebetween(String owner, Date startDate, Date endDate) {
		return candidateRepository.getCountByOwnerLikeAndDateBetween(owner, startDate, endDate);
	}

	public Object getCandidateDetailsFromRoundCandidateId(Long roundCandidateId) {
		return candidateRepository.getCandidateDetailsFromRoundCandidateID(roundCandidateId);
	}

	public String getLatestNoteForCandidate(String candidateID) {
		return candidateRepository.getLatestNoteForCandidate(candidateID);
	}

	public List<BigInteger> findCandidateByCustomFields(String fieldName, String fieldvalue) {		
		return candidateRepository.findCandidateByCustomFields(fieldName,fieldvalue);
	}

	public List<Candidate> findByCidIn(Collection<Long> finalCustomCandidates) {
		return candidateRepository.findByCidIn(finalCustomCandidates);
	}

	public Long getCountByOwnerAndDatebetween(String owner, Date startDate, Date endDate) {
		return candidateRepository.getCountByOwnerAndDateBetween(owner, startDate, endDate);
	}

	public List<Candidate> findByMobileNo(String recruizQuickSearch) {

		return candidateRepository.findByMobileNo(recruizQuickSearch);
	}

	public Report getAllPositionSoucingChannelMix() {

		Object[] candidateDetails = candidateRepository.getAllPositionSoucingChannelMix();

		List<Object> data = new ArrayList<>();
		String[] metaData = {"Source Channel", "Total Number"};
		for (Object object : candidateDetails) {
			data.add(object);
		}

		Report report = new Report();

		report.setReportData(data);
		report.setMetaData(metaData);

		return report;
	}

	public void getPositionFiledName(Set<String> fieldMap) {
		fieldMap.add("Client Name");
		fieldMap.add("Position Name");
		fieldMap.add("Requisition Id");
		fieldMap.add("Job Location");
		fieldMap.add("Job Description");
		fieldMap.add("Skill Set");
		fieldMap.add("Education Qualification");
		fieldMap.add("Hiring Manager");
		fieldMap.add("Vertical/Cluster");
		fieldMap.add("End Client");
		fieldMap.add("Screener");
		fieldMap.add("Internal Spoc");
		fieldMap.add("Inteview Date");
	}


}
