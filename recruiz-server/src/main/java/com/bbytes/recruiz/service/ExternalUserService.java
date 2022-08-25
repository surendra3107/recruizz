package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.mailgun.model.MailgunSendResponse;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.ClientInterviewerPanel;
import com.bbytes.recruiz.domain.Feedback;
import com.bbytes.recruiz.domain.ForwardProfile;
import com.bbytes.recruiz.domain.OfferLetterForCandidate;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Round;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.SharedLinkAnalytics;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.CampaignCandidateActionType;
import com.bbytes.recruiz.enums.CandidateActivityType;
import com.bbytes.recruiz.enums.FileType;
import com.bbytes.recruiz.enums.RoundType;
import com.bbytes.recruiz.enums.Source;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.integration.levelbar.LevelbarService;
import com.bbytes.recruiz.repository.CandidateFileRepository;
import com.bbytes.recruiz.repository.OfferLetterForCandidateRepository;
import com.bbytes.recruiz.rest.dto.models.CandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.ExternalPositionDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.FeedbackShareDTO;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.EncryptKeyUtils;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class ExternalUserService {

	private static Logger logger = LoggerFactory.getLogger(ExternalUserService.class);

	@Autowired
	protected TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	CandidateFileRepository candidateFileRepository;

	@Autowired
	OfferLetterForCandidateRepository offerLetterForCandidateRepository;


	@Autowired
	protected InterviewPanelService interviewPanelService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private UserService userService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private RoundService roundService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private ForwardProfileService forwardProfileService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SharedLinkAssesmentService sharedLinkAssesmentService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Autowired
	private DefaultEmailService defaultEmailService;

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private LevelbarService levelbarService;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	private EmailActivityService emailActivityService;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${email.forward.profile.subject}")
	private String forwardProfileSubject;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	@Value("${logo.recruiz}")
	private String logoPath;

	@Value("${file.upload.temp.path}")
	private String tempFolderPath;

	@Value("${allowed.external.user.links.validity}")
	private String extendTokenForDays;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	/**
	 * Method used to send email of candidate profile to interviewers.
	 *
	 * @param candidateProfileDTO
	 * @param position
	 * @param selectedFiles 
	 * @throws ParseException
	 * @throws RecruizException
	 * @throws IOException
	 * @throws MessagingException
	 */

	public void sendCandidateProfile(CandidateProfileDTO candidateProfileDTO, Position position, String type, String selectedFiles)
			throws ParseException, RecruizException, IOException, MessagingException {

		final String email = userService.getLoggedInUserEmail();
		Set<String> candidateResumeFilePath = new HashSet<>();
		Candidate candidate = null;
		File excelFile = null;
		if (candidateProfileDTO.getExcelAttachmentPath() != null
				&& !candidateProfileDTO.getExcelAttachmentPath().trim().isEmpty()) {
			excelFile = new File(candidateProfileDTO.getExcelAttachmentPath());
			if (!excelFile.exists()) {
				excelFile = null;
			}
		}

		int linkValidityHour = (int) (Integer.parseInt(extendTokenForDays) * 24);

		final String externalUserAuthToken = tokenAuthenticationProvider.getAuthTokenForUser(email,
				TenantContextHolder.getTenant(), WebMode.STANDALONE, linkValidityHour,
				userService.getLoggedInUserObject().getTimezone(), userService.getLoggedInUserObject().getLocale());

		File positionDescriptionFile = null;
		if (candidateProfileDTO.getIsJDAttached()) {

			String renderedJdTemplate = emailTemplateDataService.getRenderedTemplateForJD(position);

			// create JD file here and delete after sending email
			File tempFileLocation = new File(tempFolderPath + "/" + TenantContextHolder.getTenant());
			if (tempFileLocation.isDirectory()) {
				tempFileLocation.mkdirs();
			}
			if (!tempFileLocation.exists())
				tempFileLocation.mkdirs();

			positionDescriptionFile = new File(tempFileLocation + "/" + GlobalConstants.JOB_DESC_FILE_NAME);
			if (positionDescriptionFile.exists()) {
				positionDescriptionFile.delete();
				positionDescriptionFile.createNewFile();
			}

			FileWriter fileWriter = new FileWriter(positionDescriptionFile);
			fileWriter.write(renderedJdTemplate);
			fileWriter.flush();
			fileWriter.close();

		}

		if (candidateProfileDTO.getInterviewerEmails() != null
				&& !candidateProfileDTO.getInterviewerEmails().isEmpty()) {
			for (String interviewerEmail : candidateProfileDTO.getInterviewerEmails()) {

				List<String> emailList = new ArrayList<String>();
				emailList.add(interviewerEmail);

				String interviewerName = StringUtils.getNameFromEmail(interviewerEmail);
				String interviewerMobile = null;
				ClientInterviewerPanel interviewer = interviewPanelService
						.getInterviewerByEmailAndClient(interviewerEmail, position.getClient());
				if (interviewer != null) {
					interviewerName = interviewer.getName();
					interviewerMobile = interviewer.getMobile();
				}

				// this map will contain map of candidate id and
				// feedback id
				List<String> candidateFeedbackIdMap = new ArrayList<>();
				List<String> candidateLevelbarKeyMap = new ArrayList<>();
				// saving expected feedback to db
				if (candidateProfileDTO.getRoundCandidateData() != null
						&& !candidateProfileDTO.getRoundCandidateData().isEmpty()) {

					for (Map<String, String> map : candidateProfileDTO.getRoundCandidateData()) {
						for (Map.Entry<String, String> entry : map.entrySet()) {
							String roundId = entry.getKey();
							String candidateId = entry.getValue();
							candidate = candidateService.getCandidateById(Long.parseLong(candidateId));
							if (candidateProfileDTO.isAttchedResume()) {
								if (candidateProfileDTO.getMaskedResume().toLowerCase().startsWith("y")) {
									String maskedFilePath = candidateService
											.getConvertedMaskedResumePath(candidate.getCid());
									if (null != maskedFilePath) {
										candidateResumeFilePath.add(maskedFilePath);
									}
								} else {
									candidateResumeFilePath.add(candidate.getResumeLink());
								}
							}

							// calling feedback share levelbar API for custom
							// feedback
							// form
							String feedbackSharedId = createFeedbackShare(position, candidate, roundId,
									candidateProfileDTO.getFeedbackQueSetId(),
									GlobalConstants.PROFILE_FORWARD_MODE_FORWARD);

							if (candidate != null) {
								boolean profileMaksed = false;
								if (null != candidateProfileDTO.getMaskedResume()
										&& candidateProfileDTO.getMaskedResume().toLowerCase().startsWith("y")) {
									profileMaksed = true;
								}

								Set<String> expectedFeddbackBy = new HashSet<String>();
								expectedFeddbackBy.add(interviewerEmail);
								String forwardType = "Forwarded";
								if (candidateProfileDTO.isIgnoreFeedback()) {
									forwardType = "Forwarded Ignored";
								}
								Set<Feedback> requiredFeedbacks = interviewScheduleService
										.setInterviewerForExpectedFeedback(expectedFeddbackBy, roundId,
												candidate.getEmail(), null, forwardType, position.getPositionCode(),
												feedbackSharedId, profileMaksed);
								if (requiredFeedbacks == null || requiredFeedbacks.isEmpty()) {
									break;
								}

								// sending feedback to the expected person
								// with
								// feedback Id
								for (Feedback feedback : requiredFeedbacks) {
									if (feedback.getFeedbackBy().equalsIgnoreCase(interviewerEmail)) {
										logger.debug(
												"Sending feedback to " + interviewerEmail + " : " + emailList.size());
										// add entry to feedback id Map
										candidateFeedbackIdMap.add(candidate.getCid() + ":" + feedback.getId());
										candidateLevelbarKeyMap.add(
												candidate.getCid() + ":" + feedback.getLevelbarFeedbackShareResultId());
									}
								}
							}

							if (candidateProfileDTO.isIgnoreFeedback()) {
								candidateFeedbackIdMap.add(candidate.getCid() + ":" + -1);
								candidateLevelbarKeyMap.add(candidate.getCid() + ":" + null);
							}

							// making entry to candidate activity
							candidateActivityService.profileForwardEvent(candidateProfileDTO, position,
									candidateProfileDTO.getNotes(), candidateId);
						}
					}

					// if ignore feedback is checked then add all interviewer in
					// the emailList and sending one list, so after sending the
					// email break statement is used to come out of the loop

					if (candidateProfileDTO.isIgnoreFeedback()) {
						emailList.clear();
						emailList.addAll(candidateProfileDTO.getInterviewerEmails());
					}

					// sending profile email here
					sendFeedBackEmail(candidateProfileDTO, position, type, externalUserAuthToken, interviewerEmail,
							emailList, interviewerName, interviewerMobile,
							StringUtils.commaSeparate(candidateFeedbackIdMap),
							StringUtils.commaSeparate(candidateLevelbarKeyMap), positionDescriptionFile,
							candidateResumeFilePath, excelFile,selectedFiles, candidate);

					// breaking the loop after sending the email to all in case
					// of ignore feedback is true

					// commenting this code because asked to add add entry to
					// feedback
					/*
					 * if (candidateProfileDTO.isIgnoreFeedback()) { break; }
					 */

				}
			}

			// add to forward profile db
			Set<String> profileReciever = new HashSet<String>(candidateProfileDTO.getInterviewerEmails());
			Set<String> forwardedRoundcandidates = new HashSet<String>(candidateProfileDTO.getRoundCandidateIds());
			ForwardProfile forwardProfile = new ForwardProfile();
			forwardProfile.setProfileReciever(profileReciever);
			forwardProfile.setRoundCandidateId(forwardedRoundcandidates);
			forwardProfile.setSubject(candidateProfileDTO.getSubject());
			forwardProfile.setBody(candidateProfileDTO.getNotes());
			forwardProfile.setEmailFrom(userService.getLoggedInUserEmail());
			forwardProfile.setPositionCode(candidateProfileDTO.getPositionCode());
			forwardProfileService.save(forwardProfile);
		}
		// deleting the temp created file
		if (null != positionDescriptionFile && positionDescriptionFile.exists())
			positionDescriptionFile.delete();

		if(position!=null){
			position.setModificationDate(new Date());
			positionService.save(position);
		}

	}

	@Transactional(readOnly = true)
	private FeedbackShareDTO getFeedbackShareDTO(Position position, Candidate candidate, String roundId, String type)
			throws RecruizException, NumberFormatException {

		FeedbackShareDTO feedbackShareDTO = null;

		Organization org = organizationService.getCurrentOrganization();
		Round round = roundService.findOne(Long.parseLong(roundId));

		if (position != null && candidate != null) {
			feedbackShareDTO = new FeedbackShareDTO();
			feedbackShareDTO.setCandidateName(candidate.getFullName());
			feedbackShareDTO.setCandidateEmail(candidate.getEmail());
			feedbackShareDTO.setCandidateMobile(candidate.getMobile());
			feedbackShareDTO.setClientName(position.getClient().getClientName());
			feedbackShareDTO.setPositionCode(position.getPositionCode());
			feedbackShareDTO.setPositionName(position.getTitle());
			feedbackShareDTO.setRoundName(round.getRoundName());
			feedbackShareDTO.setTenant(TenantContextHolder.getTenant());
			feedbackShareDTO.setType(type);
			feedbackShareDTO.setOrgType(org.getOrgType());
		}
		return feedbackShareDTO;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public String createFeedbackShare(Position position, Candidate candidate, String round, String feedbackQueSetId,
			String type) throws RecruizException {

		String feedbackShareId = null;
		FeedbackShareDTO feedbackShareDTO = getFeedbackShareDTO(position, candidate, round, type);
		if (feedbackShareDTO == null)
			return null;

		ResponseEntity<RestResponse> response = levelbarService.createFeedbackShare(feedbackShareDTO, feedbackQueSetId);

		if (response != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
			Map<String, String> responseFeedbackShare = (LinkedHashMap<String, String>) response.getBody().getData();

			feedbackShareId = responseFeedbackShare.get("id");
			return feedbackShareId;
		}
		return feedbackShareId;
	}

	private void sendFeedBackEmail(CandidateProfileDTO candidateProfileDTO, Position position, String type,
			final String externalUserAuthToken, String interviewerEmail, List<String> emailList, String interviewerName,
			String interviewerMobile, String feedbackIdMap, String candidateLevelbarKeyMap,
			File positionDescriptionFile, Set<String> candidateResumeFilePath, File excelFile, String selectedFiles, Candidate candidate)
					throws ParseException, RecruizException, MessagingException, IOException {
		String renderedTemplate;
		String interviewerDetails = StringUtils.getUserInforToEncrypt(interviewerEmail, interviewerName,
				interviewerMobile);
		String roundAndCandidateId = StringUtils.convertMapToString(candidateProfileDTO.getRoundCandidateData());
		String accessToken = EncryptKeyUtils.getEncryptedKey(interviewerDetails);
		String token = baseUrl + GlobalConstants.FORWARD_PROFILE_URL + GlobalConstants.PASSKEY + externalUserAuthToken
				+ GlobalConstants.ROUND_AND_CANDIDATE_ID + roundAndCandidateId + GlobalConstants.POSITION_CODE
				+ position.getPositionCode() + GlobalConstants.ACCESS_INFO + accessToken + GlobalConstants.FORWARD_MODE
				+ type + GlobalConstants.FEEDBACK_ID + feedbackIdMap + GlobalConstants.MASKED_RESUME
				+ candidateProfileDTO.getMaskedResume() + GlobalConstants.LEVELBAR_KEY + candidateLevelbarKeyMap + GlobalConstants.MASKED_CTC + candidateProfileDTO.getMaskedCtc();

		String postDate = DateTimeUtils.getDateTimeAsString(new Date());

		Map<String, Object> emailBody = new HashMap<>();
		emailBody.put(GlobalConstants.POST_DATE, postDate);
		emailBody.put(GlobalConstants.POSITION_NAME, position.getTitle());
		emailBody.put(GlobalConstants.NOTE_LINK, token);
		emailBody.put(GlobalConstants.MasterEmailTemplateLogo, logoPath);
		if(null !=  userService.getLoggedInUserObject().getProfileSignature() && ! userService.getLoggedInUserObject().getProfileSignature().isEmpty()) {
			emailBody.put("emailSignature", userService.getLoggedInUserObject().getProfileSignature());
		}else {
			emailBody.put("emailSignature", " ");
		}


		// get Template and send

		String buttonText = "Give Feedback";
		if (candidateProfileDTO.isIgnoreFeedback()) {
			buttonText = "View Details";
		}
		renderedTemplate = emailTemplateDataService
				.getMasterTemplateWithButtonForForwardProfile(candidateProfileDTO.getNotes(), token, buttonText);
		/*
		 * emailService.sendProfile(emailList, renderedTemplate,
		 * candidateProfileDTO.getSubject(), positionDescriptionFile,
		 * userService.getLoggedInUserEmail());
		 */


		List<String> candidateFiles = new ArrayList<>();
		if(selectedFiles!=null)
			candidateFiles = addSelectedFilesWithAttachment(selectedFiles, candidate.getCid());



		List<File> attachmentFiles = new ArrayList<>();
		List<String> attachmentFilesPath = new ArrayList<>();

		if(candidateFiles!=null && candidateFiles.size()>0){
			addSelectedFilesWithAttachment(candidateFiles, attachmentFiles);
		}


		if (null != positionDescriptionFile) {
			attachmentFiles.add(positionDescriptionFile);
		}

		if (null != excelFile) {
			attachmentFiles.add(excelFile);
		}
		if (candidateResumeFilePath != null && !candidateResumeFilePath.isEmpty()) {
			for (String filePath : candidateResumeFilePath) {
				if (null == filePath || filePath.trim().isEmpty()) {
					continue;
				}
				File attachementFile = null;
				if (null != filePath && filePath.startsWith("http")) {
					attachementFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), filePath);
				} else {
					attachementFile = new File(filePath);
				}
				attachmentFiles.add(attachementFile);

			}
		}

		// adding all attachment files for emails
		for (File attchedFile : attachmentFiles) {
			attachmentFilesPath.add(attchedFile.getPath());
		}

		// adding logged in user as CC to the email list -- RECZFT-272
		candidateProfileDTO.getCcEmails().add(userService.getLoggedInUserEmail());

		// emailList.add(userService.getLoggedInUserEmail());

		MailgunSendResponse response = emailService.sendBulkEmail(emailList, renderedTemplate,
				candidateProfileDTO.getSubject(), attachmentFiles, userService.getLoggedInUserEmail(),
				candidateProfileDTO.getCcEmails());
		try {
			String[] fileArray = new String[attachmentFilesPath.size()];
			emailActivityService.saveEmail(renderedTemplate, candidateProfileDTO.getSubject(), emailList,
					candidateProfileDTO.getCcEmails(), userService.getLoggedInUserEmail(),
					attachmentFilesPath.toArray(fileArray));
		} catch (Exception ex) {
		}

	}

	/**
	 * this method will be called from controller and after validation of tenant
	 * the correct tenant will be set
	 *
	 * @param sourcedFrom
	 * @param urlCode
	 * @param name
	 * @param mobile
	 * @param email
	 * @param resume
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */

	public void processCandidate(String sourcedFrom, String urlCode, String name, String mobile, String email,
			MultipartFile resume, String sourceMobile, String sourceEmail, String sourceName, String fromUrl,
			String owner) throws RecruizException, IllegalStateException, IOException {
		String positionCode = urlCode;
		File resumeFile = fileService.multipartToFile(resume);
		addCandidateFromExternalSource(name, mobile, email, resumeFile, positionCode, sourcedFrom, sourceMobile,
				sourceEmail, sourceName, owner);

		// add entry to analytics table
		addtoSharedLinkAnalytics(positionCode, sourcedFrom, GlobalConstants.AnalyticsEvenTypeApplied, fromUrl);
	}


	public void processCandidateForPositionApply(Candidate candidateDto,String sourcedFrom, String urlCode,MultipartFile resume, String fromUrl) throws RecruizException, IllegalStateException, IOException {
		String positionCode = urlCode;
		File resumeFile = fileService.multipartToFile(resume);
		addCandidateFromExternalSourcePositionApply( resumeFile, positionCode, sourcedFrom,candidateDto);

		// add entry to analytics table
		addtoSharedLinkAnalytics(positionCode, sourcedFrom, GlobalConstants.AnalyticsEvenTypeApplied, fromUrl);
	}



	/**
	 * Add candidate to database , if resume is present parse it and add,
	 * otherwise add with name,email and mobile number. The file passed is a
	 * temp file so we add a copy to the candidate and delete the passed file
	 *
	 * @param name
	 * @param mobile
	 * @param email
	 * @param resume
	 * @param positionCode
	 * @param sourcedFrom
	 * @throws RecruizException
	 * @throws IllegalStateException
	 * @throws IOException
	 */

	public void addCandidateFromExternalSource(String name, String mobile, String email, File resumeFile,
			String positionCode, String sourcedFrom, String sourceMobile, String sourceEmail, String sourceName,
			String owner) throws RecruizException, IllegalStateException, IOException {

		if (resumeFile == null || !resumeFile.exists())
			throw new RecruizWarnException(ErrorHandler.RESUME_MISSING, ErrorHandler.NO_RESUME);

		try {
			Position position = positionService.getPositionByCode(positionCode);
			if (position == null) {
				throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.CAN_NOT_APPY);
			} else {
				positionService.calculateFinalStatusForPosition(position);
				if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.getDisplayName())) {
					throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.CAN_NOT_APPY);
				}
			}

			Candidate candidate = candidateService.getCandidateByEmail(email);
			if (candidate == null) {
				candidate = new Candidate();
				candidate.setSourcedOnDate(new Date());
				candidate.setSource(sourcedFrom);
				candidate.setSourceDetails(sourceName);
				candidate.setOwner(owner);
				candidate.setCandidateRandomId("C-" + StringUtils.get6RandomDigit());
			}

			candidate.setFullName(name);
			candidate.setMobile(mobile);
			candidate.setEmail(email);

			for (Source source : Source.values()) {
				if (source.name().equalsIgnoreCase(sourcedFrom)) {
					sourcedFrom = source.getDisplayName();
					break;
				}
			}

			candidate = setDefaultValues(candidate);
			candidateService.setSourceInfo(candidate, sourceName, sourcedFrom, sourceName);
			candidate.setSourceEmail(sourceEmail);
			candidate.setSourceMobile(sourceMobile);
			candidate.setSourceName(sourceName);
			candidateService.save(candidate);

			String byWhom = "";
			if (candidate.getOwner() != null && !candidate.getOwner().isEmpty()) {
				byWhom = candidate.getOwner();
			} else if (sourceName != null && !sourceName.isEmpty()) {
				byWhom = sourceName;
			} else {
				String sourceForm = Source.getDisplayNameOfStatus(sourcedFrom);
				byWhom = sourceForm;
			}

			if (resumeFile != null && resumeFile.exists()) {
				uploadFileService.createFolderStructureForCandidate(folderPath, candidate.getCid() + "");
				String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, resumeFile.getName(),
						"resume", candidate.getCid() + "");

				String pdfFilePath = fileService.convert(serverPath);
				// converting resume to candidate object here
				long cid = candidate.getCid();
				// getting existing source info, since parser creates new object
				// of candidate
				Date sourcedOnDate = candidate.getSourcedOnDate();
				String source = candidate.getSource();
				owner = candidate.getOwner();
				String sourceDetails = candidate.getSourceDetails();

				Candidate parsedCandidate = resumeParserService.parseResumeForExternalUser(resumeFile);

				if(parsedCandidate!=null)
					candidate = parsedCandidate;

				candidate.setCid(cid);
				candidate.setFullName(name);
				candidate.setMobile(mobile);
				candidate.setEmail(email);
				candidateService.setDefaultValues(candidate);
				candidateService.setSourceInfo(candidate, sourceName, sourcedFrom, sourceName);
				candidate.setSourcedOnDate(sourcedOnDate);
				candidate.setSource(source);
				candidate.setOwner(owner);
				candidate.setSourceDetails(sourceDetails);
				candidate.setResumeLink(pdfFilePath);
				candidateService.save(candidate);

				candidateService.uploadCandidateFiles(serverPath, resumeFile.getName(),
						FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "", pdfFilePath);

				String activityAction = "";
				if (sourcedFrom != null && !sourcedFrom.isEmpty()) {
					activityAction = "Canddiate added via " + Source.getDisplayNameOfStatus(sourcedFrom);
				} else {
					activityAction = "Candidate added ";
				}

				candidateActivityService.addActivity(activityAction, byWhom, candidate.getCid() + "",
						CandidateActivityType.Added.getDisplayName());

				// masking resume
				try {
					Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
					if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
						candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
					}
				} catch (Exception ex) {
					logger.warn("\n\n\n*******Failed to mask resume*********", ex);
				}

			}
			addCandidateToBoard(candidate, positionCode);

			if (position != null) {
				// making entry to candidate activity
				RoundCandidate roundCandidate = roundCandidateService.getExistingBoardCandidate(candidate,
						positionCode);
				if (roundCandidate != null) {
					candidateActivityService.addActivity(
							"Added to board for position : $@"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
									.getId()
									+ "_"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
									.getClientName()
									+ " $#"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle()
									+ "$#",
									byWhom, candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());
				}

				logger.error("Step 1 Email Hr executives list checking");

				// send email to Hr Executive here
				Set<User> positionHR = new HashSet<User>();
				positionHR = position.getHrExecutives();

				logger.error("Step 2 Email Hr executives list checking positionHR = "+positionHR);
				positionHR.add(userService.getUserByEmail(position.getOwner()));
				List<String> emailList = new ArrayList<>();
				for (User user : positionHR) {
					emailList.add(user.getEmail());
				}

				logger.error("Step 3 Email Hr executives list checking emailList = "+emailList);

				final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_EXTERNAL_SOURCE_CANDIDATE_APPLY;
				String emailSubject = "New candidate '" + candidate.getFullName() + "' sourced ";
				// getting display name of the status
				String status = Source.getDisplayNameOfStatus(sourcedFrom);

				logger.error("Step 4 Email Hr executives list checking ");

				//		Map<String, Object> bodyMap = emailTemplateDataService
				//			.getEmailBodyValueMapForCandidateSourceEmailForHr(position, candidate, status);

				Map<String, Object> bodyMap = new HashMap<>();
				bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
				//	bodyMap.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
				bodyMap.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());
				bodyMap.put(GlobalConstants.CANDIDATE_EMAIL, candidate.getEmail());

				logger.error("Step 5 Email Hr executives list checking ");

				String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
				/*String link = baseUrl + GlobalConstants.BOARD_URL + position.getPositionCode() + "/";
		String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString,
			link, "View");*/
				logger.error("Step 5 Email Hr executives list checking, before mail send succesfully  ");


				try{
					defaultEmailService.sendEmailForExternalApi(emailList, templateString, emailSubject, true);
				}catch(Exception e){
					logger.error("Getting error during send email..................... ");
					defaultEmailService.sendBulkEmail(emailList, templateString, emailSubject, null, null, null);
				}
				logger.error("Step 6 Email Hr executives list checking, after mail send succesfully  ");

			}

		} catch (Exception e) {
			logger.error(e.getMessage()+" error Message ", e);
		} finally {
			if (resumeFile != null && resumeFile.exists()) {
				resumeFile.delete();
			}
		}
	}




	public void addCandidateFromExternalSourcePositionApply(File resumeFile,String positionCode, String sourcedFrom,Candidate candidateDto) throws RecruizException, IllegalStateException, IOException {

		if (resumeFile == null || !resumeFile.exists())
			throw new RecruizWarnException(ErrorHandler.RESUME_MISSING, ErrorHandler.NO_RESUME);

		try {
			Position position = positionService.getPositionByCode(positionCode);
			if (position == null) {
				throw new RecruizWarnException(ErrorHandler.POSITION_NOT_EXIST, ErrorHandler.CAN_NOT_APPY);
			} else {
				positionService.calculateFinalStatusForPosition(position);
				if (!position.getFinalStatus().equalsIgnoreCase(Status.Active.getDisplayName())) {
					throw new RecruizWarnException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.CAN_NOT_APPY);
				}
			}

			Candidate candidate = candidateService.getCandidateByEmail(candidateDto.getEmail());
			if (candidate == null) {
				candidate = candidateDto;
				candidate.setSourcedOnDate(new Date());
				candidate.setSource(sourcedFrom);
				candidate.setCandidateRandomId("C-" + StringUtils.get6RandomDigit());
			}

			for (Source source : Source.values()) {
				if (source.name().equalsIgnoreCase(sourcedFrom)) {
					sourcedFrom = source.getDisplayName();
					break;
				}
			}

			candidate = setDefaultValues(candidate);
			//	    candidateService.setSourceInfo(candidate, sourceName, sourcedFrom, sourceName);
			candidateService.save(candidate);

			String byWhom = "";
			if (candidate.getOwner() != null && !candidate.getOwner().isEmpty()) {
				byWhom = candidate.getOwner();
			} else if (candidate.getSourceName() != null && !candidate.getSourceName().isEmpty()) {
				byWhom = candidate.getSourceName();
			} else {
				String sourceForm = Source.getDisplayNameOfStatus(sourcedFrom);
				byWhom = sourceForm;
			}

			if (resumeFile != null && resumeFile.exists()) {
				uploadFileService.createFolderStructureForCandidate(folderPath, candidate.getCid() + "");
				String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, resumeFile.getName(),
						"resume", candidate.getCid() + "");

				String pdfFilePath = fileService.convert(serverPath);
				// converting resume to candidate object here
				long cid = candidate.getCid();
				// getting existing source info, since parser creates new object
				// of candidate
				Date sourcedOnDate = candidate.getSourcedOnDate();
				String source = candidate.getSource();
				//	owner = candidate.getOwner();
				String sourceDetails = candidate.getSourceDetails();

				//	Candidate parsedCandidate = resumeParserService.parseResumeForExternalUser(resumeFile);

				/*		if(parsedCandidate!=null)
    			candidate = parsedCandidate;
				 */		
				//		candidate.setCid(cid);

				candidateService.setDefaultValues(candidate);
				//    		candidateService.setSourceInfo(candidate, sourceName, sourcedFrom, sourceName);
				candidate.setSourcedOnDate(sourcedOnDate);
				candidate.setSource(source);
				//  		candidate.setOwner(owner);
				candidate.setSourceDetails(sourceDetails);
				candidate.setResumeLink(pdfFilePath);
				candidateService.save(candidate);

				candidateService.uploadCandidateFiles(serverPath, resumeFile.getName(),
						FileType.Original_Resume.getDisplayName(), "new", candidate.getCid() + "", pdfFilePath);

				String activityAction = "";
				if (sourcedFrom != null && !sourcedFrom.isEmpty()) {
					activityAction = "Canddiate added via " + Source.getDisplayNameOfStatus(sourcedFrom);
				} else {
					activityAction = "Candidate added ";
				}

				candidateActivityService.addActivity(activityAction, byWhom, candidate.getCid() + "",
						CandidateActivityType.Added.getDisplayName());

				// masking resume
				try {
					Map<String, String> maskedResumeFiles = candidateService.maskResume(cid);
					if (null != maskedResumeFiles && !maskedResumeFiles.isEmpty()) {
						candidateService.attachMaskedResumeToCadidate(maskedResumeFiles, cid);
					}
				} catch (Exception ex) {
					logger.warn("\n\n\n*******Failed to mask resume*********", ex);
				}

			}
			addCandidateToBoard(candidate, positionCode);

			if (position != null) {
				// making entry to candidate activity
				RoundCandidate roundCandidate = roundCandidateService.getExistingBoardCandidate(candidate,
						positionCode);
				if (roundCandidate != null) {
					candidateActivityService.addActivity(
							"Added to board for position : $@"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
									.getId()
									+ "_"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
									.getClientName()
									+ " $#"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
									+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle()
									+ "$#",
									byWhom, candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());
				}

				// send email to Hr Executive here
				Set<User> positionHR = new HashSet<User>();
				positionHR = position.getHrExecutives();
				positionHR.add(userService.getUserByEmail(position.getOwner()));
				List<String> emailList = new ArrayList<>();
				for (User user : positionHR) {
					emailList.add(user.getEmail());
				}

				/*		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_SOURCE_CANDIDATE_REPLY;
    		String emailSubject = "New candidate '" + candidate.getFullName() + "' sourced ";
    		// getting display name of the status
    		String status = Source.getDisplayNameOfStatus(sourcedFrom);

    		Map<String, Object> bodyMap = emailTemplateDataService
    			.getEmailBodyValueMapForCandidateSourceEmailForHr(position, candidate, status);

    		String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
    		String link = baseUrl + GlobalConstants.BOARD_URL + position.getPositionCode() + "/";
    		String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithButton(templateString,
    			link, "View");
    		emailService.sendEmail(emailList, masterRenderedTemplate, emailSubject, true);
				 */    	

				final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_EXTERNAL_SOURCE_CANDIDATE_APPLY;
				String emailSubject = "New candidate sourced ( "+candidate.getEmail()+" )";
				// getting display name of the status
				String status = Source.getDisplayNameOfStatus(sourcedFrom);

				Map<String, Object> bodyMap = new HashMap<>();
				bodyMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
				//	bodyMap.put(GlobalConstants.CLIENT_NAME, position.getClient().getClientName());
				bodyMap.put(GlobalConstants.CANDIDATE_NAME, candidate.getFullName());
				bodyMap.put(GlobalConstants.CANDIDATE_EMAIL, candidate.getEmail());


				String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
				emailService.sendEmail(emailList, templateString, emailSubject, true);

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (resumeFile != null && resumeFile.exists()) {
				resumeFile.delete();
			}
		}
	}





	private Candidate setDefaultValues(Candidate candidate) {

		if (candidate.getCurrentCompany() == null || candidate.getCurrentCompany().isEmpty())
			candidate.setCurrentCompany("N/A");
		if (candidate.getCurrentTitle() == null || candidate.getCurrentTitle().isEmpty())
			candidate.setCurrentTitle("N/A");
		if (candidate.getCurrentLocation() == null || candidate.getCurrentLocation().isEmpty())
			candidate.setCurrentLocation("N/A");
		if (candidate.getHighestQual() == null || candidate.getHighestQual().isEmpty())
			candidate.setHighestQual("N/A");
		if (candidate.getEmploymentType() == null || candidate.getEmploymentType().isEmpty())
			candidate.setEmploymentType("N/A");
		if (candidate.getPreferredLocation() == null || candidate.getPreferredLocation().isEmpty())
			candidate.setPreferredLocation("N/A");
		if (candidate.getComments() == null || candidate.getComments().isEmpty())
			candidate.setComments("N/A");
		if (candidate.getCommunication() == null || candidate.getCommunication().isEmpty())
			candidate.setCommunication("N/A");
		if (candidate.getGender() == null || candidate.getGender().isEmpty())
			candidate.setGender("N/A");
		if (candidate.getSource() == null || candidate.getSource().isEmpty())
			candidate.setSource("N/A");

		return candidate;
	}

	/**
	 * add sourced candidate to positin board in source round
	 *
	 * @param candidate
	 * @param positionCode
	 * @throws RecruizException
	 */
	private void addCandidateToBoard(Candidate candidate, String positionCode) throws RecruizException {

		if (roundCandidateService.getRoundcandidateByPosition(candidate, positionCode) != null)
			throw new RecruizWarnException(ErrorHandler.ALREADY_APPLIED_FOR_POSITION, ErrorHandler.ALREADY_APPLIED);

		Position position = positionService.getPositionByCode(positionCode);

		if (position != null) {
			if (position.getClientStatus().equalsIgnoreCase(Status.OnHold.toString())
					|| position.getClientStatus().equalsIgnoreCase(Status.Closed.toString())) {
				throw new RecruizException(ErrorHandler.CLIENT_STATUS_OPERATION, ErrorHandler.CLIENT_ONHOLD_CLOSED);
			}
			if (position.getStatus().equalsIgnoreCase(Status.OnHold.toString())
					|| position.getStatus().equalsIgnoreCase(Status.Closed.toString())) {
				throw new RecruizException(ErrorHandler.POSITION_STATUS_OPERATION, ErrorHandler.POSITION_ONHOLD_CLOSED);
			}

			Round round = roundService.getRoundByBoardAndType(position.getBoard(), RoundType.Source.toString());
			RoundCandidate roundCandidate = new RoundCandidate();
			roundCandidate.setCandidate(candidate);
			roundCandidate.setStatus(BoardStatus.YetToProcess.toString());
			roundCandidate.setRoundId(round.getId() + "");
			roundCandidate.setRound(round);
			roundCandidate.setPositionCode(positionCode);
			roundCandidate.setSourcedBy(candidate.getOwner());
			roundCandidateService.save(roundCandidate);
		}
	}

	/**
	 * To get custom position information to display for external user (3rd
	 * party application/portal)
	 *
	 * @param positionCode
	 * @return
	 * @throws RecruizException
	 */

	public Map<String, Object> getPositionForExternal(String urlCode, String sourceFrom, String fromUrl)
			throws RecruizException {
		String positionCode = urlCode;

		Map<String, Object> externalPosition = getPosition(positionCode);

		// adding to analytics table
		if (!sourceFrom.equalsIgnoreCase(GlobalConstants.SHARE_MODE_JD)) {
			addtoSharedLinkAnalytics(positionCode, sourceFrom, GlobalConstants.AnalyticsEvenTypeViewed, fromUrl);
		}

		return externalPosition;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getPosition(String positionCode) throws RecruizException {
		Map<String, Object> externalPositionMap = new HashMap<>();
		Position position = positionService.getPositionByCode(positionCode);
		ExternalPositionDTO externalPosition = new ExternalPositionDTO();
		if (position == null)
			throw new RecruizWarnException(ErrorHandler.NO_POSITION_EXISTS, ErrorHandler.POSITION_NOT_FOUND);

		if (GlobalConstants.ORG_TYPE_AGENCY
				.equalsIgnoreCase(organizationService.getCurrentOrganization().getOrgType())) {
			externalPosition.setClientName("");
		} else {
			externalPosition.setClientName(position.getClient().getClientName());
		}

		externalPosition.setId(position.getId());
		externalPosition.setPositionCode(position.getPositionCode());
		externalPosition.setTitle(position.getTitle());
		externalPosition.setLocation(position.getLocation());
		externalPosition.setDescription(position.getDescription());
		externalPosition.setGoodSkillSet(position.getGoodSkillSet());
		externalPosition.setReqSkillSet(position.getReqSkillSet());
		externalPosition.setEducationalQualification(position.getEducationalQualification());
		externalPosition.setNotes(position.getNotes());
		// externalPosition.setOpenedDate(position.getOpenedDate());
		externalPosition.setRemoteWork(position.isRemoteWork() + "");
		externalPosition.setType(position.getType());
		externalPosition.setRemoteWork(position.isRemoteWork() + "");
		externalPosition.setNotes(position.getNotes());
		externalPosition.setTotalPosition(position.getTotalPosition());
		// externalPosition.setCloseByDate(position.getCloseByDate());
		externalPosition.setPositionUrl(position.getPositionUrl());
		externalPosition.setExperienceRange(position.getExperienceRange());
		externalPosition.setMaxSal(position.getMaxSal());
		externalPosition.setMinSal(position.getMinSal());
		externalPosition.setSalUnit(position.getSalUnit());
		externalPosition.setIndustry(position.getIndustry());
		externalPosition.setFunctionalArea(position.getFunctionalArea());
		externalPosition.setClientStatus(position.getClientStatus());
		positionService.getEmploymentTypeDisplayName(position);
		positionService.calculateFinalStatusForPosition(position);
		externalPosition.setStatus(position.getFinalStatus());
		externalPosition.setFinalStatus(position.getFinalStatus());
		externalPosition.setTypeDisplayName(position.getTypeDisplayName());

		if (externalPosition != null) {
			externalPosition.getGoodSkillSet().size();
			externalPosition.getReqSkillSet().size();
			externalPosition.getEducationalQualification().size();
		}

		externalPositionMap.put("positionDetails", externalPosition);
		externalPositionMap.put("orgName", organizationService.getCurrentOrganization().getOrgName());
		externalPositionMap.put("orgLogo", organizationService.getOrgUrl(organizationService.getCurrentOrganization()));

		return externalPositionMap;
	}

	/**
	 * add entry to analytics table
	 *
	 * @param positionCode
	 * @param sourceFrom
	 */
	@Transactional
	public void addtoSharedLinkAnalytics(String positionCode, String sourceFrom, String eventType, String fromUrl) {
		SharedLinkAnalytics analytics = new SharedLinkAnalytics();
		analytics.setEventType(eventType);
		analytics.setPositionCode(positionCode);
		analytics.setPlatform(sourceFrom);
		analytics.setLinkFrom(fromUrl);
		sharedLinkAssesmentService.save(analytics);
	}

	/**
	 * to update candidate profile
	 *
	 * @param campaignCandidateId
	 * @param candidateFromRequest
	 * @param file
	 * @param fileName
	 * @throws RecruizException
	 * @throws IOException
	 */
	public Candidate updateCandidateProfileByCandidate(Candidate candidateFromRequest, MultipartFile file,
			String fileName, Long campaignCandidateId) throws RecruizException, IOException {

		Candidate candidateToUpdate = candidateService.getCandidateByEmail(candidateFromRequest.getEmail());
		if (candidateToUpdate != null) {
			candidateFromRequest.setCid(candidateToUpdate.getCid());
			candidateService.save(candidateFromRequest);

			String candidateId = candidateToUpdate.getCid() + "";

			uploadFileService.createFolderStructureForCandidate(folderPath, candidateId);
			// to delete file if file is passed to update or notified to delete
			// file
			if ((file != null && !file.isEmpty()) || candidateFromRequest.getResumeLink().equalsIgnoreCase("")) {
				if (candidateFromRequest.getResumeLink() != null
						&& candidateFromRequest.getResumeLink().equalsIgnoreCase("")) {
					removeResumeCandidateFile(candidateFromRequest, candidateId);
				}
			}
			if (file != null && !file.isEmpty()) {
				removeResumeCandidateFile(candidateFromRequest, candidateId);
				File resumeFile = fileService.multipartToFile(file);
				String serverPath = uploadFileService.uploadFileToLocalServer(resumeFile, fileName, "resume",
						candidateId);
				String pdfFilePath = fileService.convert(serverPath);
				candidateFromRequest.setResumeLink(pdfFilePath);

				candidateService.uploadCandidateFiles(serverPath, fileName, FileType.Original_Resume.getDisplayName(),
						"new", candidateId, pdfFilePath);
			}

			// deleting file if it is marked for delete (When profileUrl is
			// passed
			// as null)
			if (candidateFromRequest.getPublicProfileUrl() == null
					|| candidateFromRequest.getPublicProfileUrl().isEmpty()) {
				String filePath = publicFolder + "/" + candidateToUpdate.getProfileUrl();
				File profilrPic = new File(filePath);
				if (profilrPic.exists())
					profilrPic.delete();
				candidateToUpdate.setProfileUrl("");
			}

			// adding profile pic o public folder
			if ((candidateFromRequest.getImageContent() != null && !candidateFromRequest.getImageContent().isEmpty())
					&& (candidateFromRequest.getImageName() != null
					&& !candidateFromRequest.getImageName().isEmpty())) {
				byte[] imageBytes = Base64.decode(candidateFromRequest.getImageContent().getBytes());
				if (imageBytes != null && imageBytes.length > 0) {

					File publicProfilePath = new File(
							publicFolder + "/" + TenantContextHolder.getTenant() + "/candidate/" + candidateId);

					if (!publicProfilePath.exists())
						org.apache.commons.io.FileUtils.forceMkdir(publicProfilePath);

					File logoFile = new File(publicProfilePath + "/" + candidateFromRequest.getImageName());

					org.apache.commons.io.FileUtils.writeByteArrayToFile(logoFile, imageBytes);
					String dpPath = logoFile.getAbsolutePath().replace(publicFolder, "");
					// String logoPath =
					// logoFile.getAbsolutePath().substring(index,
					// logoFile.getAbsolutePath().length());
					if (dpPath != null && !dpPath.isEmpty()) {
						candidateToUpdate.setProfileUrl(dpPath);
						candidateService.save(candidateToUpdate);
					}
				}
			}

			candidateFromRequest = candidateService.setDefaultValues(candidateFromRequest);
			candidateFromRequest.setOwner(candidateToUpdate.getOwner());

			// deleting cover letter if file if it is marked for delete
			if (candidateFromRequest.getCoverLetterPath() == null
					|| candidateFromRequest.getCoverLetterPath().isEmpty()) {
				if (candidateToUpdate.getCoverLetterPath() != null
						&& !candidateToUpdate.getCoverLetterPath().isEmpty()) {
					File coverLetter = new File(candidateToUpdate.getCoverLetterPath());
					if (coverLetter.exists())
						coverLetter.delete();
					candidateToUpdate.setCoverLetterPath("");
				}
			}
			// updating cover letter if any new file content is passed
			candidateFromRequest.setCoverFileContent(candidateFromRequest.getCoverFileContent());
			candidateFromRequest.setCoverFileName(candidateFromRequest.getCoverFileName());
			candidateFileService.uploadCandidateCoverLetter(candidateFromRequest);
		}

		campaignService.addUpdateCampaignCandidateAction(CampaignCandidateActionType.ProfileUpdated.getDisplayName(),
				"Profile Updated", campaignCandidateId);

		return candidateFromRequest;
	}


	public void removeResumeCandidateFile(Candidate candidate, String candidateId) throws RecruizException {
		List<CandidateFile> candidateFiles = candidateFileService
				.getCandidateFileByTypeAndId(FileType.Original_Resume.getDisplayName(), candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile candidateFile : candidateFiles) {
				boolean deleted = fileService.deleteFile(candidateFile.getFilePath());
				candidateFileService.delete(candidateFile);
			}
		}

		candidateFiles = candidateFileService
				.getCandidateFileByTypeAndId(FileType.Original_Converted_Resume.getDisplayName(), candidateId);
		if (candidateFiles != null && !candidateFiles.isEmpty()) {
			for (CandidateFile candidateFile : candidateFiles) {
				boolean deleted = fileService.deleteFile(candidateFile.getFilePath());
				candidateFileService.delete(candidateFile);
			}
		}

	}


	private List<String> addSelectedFilesWithAttachment(String selectedFiles, long cid) {

		List<String> filePath = new ArrayList<>();

		String[] fileName = selectedFiles.split(",");
		Set<String> uniqueWords = new HashSet<String>(Arrays.asList(fileName));

		for (String name : uniqueWords) {

			if(name.equalsIgnoreCase("offer.pdf")){
				List<OfferLetterForCandidate> list= offerLetterForCandidateRepository.getListOfOfferLetterByCandidateId(cid);
				if(list.get(0).getFinalOfferLetterPath()!=null){
					filePath.add(list.get(0).getFinalOfferLetterPath());
				}
			}

			List<CandidateFile> candidateFile = candidateFileRepository.findByCandidateIdAndFileName(String.valueOf(cid).trim(),name.trim());

			if(candidateFile.size()>0){
				filePath.add(candidateFile.get(0).getFilePath());
			}
		}

		return filePath;

	}


	private void addSelectedFilesWithAttachment(List<String> selectedFiles, List<File> attachmentFiles) {

		for (String name : selectedFiles) { 		
			if (name != null && !name.isEmpty()) {
				File resumeFile = getFileFromPath(name);
				if (resumeFile != null && resumeFile.exists()) {
					attachmentFiles.add(resumeFile);
				}
			}

		}


	}


	public File getFileFromPath(String fileName) {
		if(fileName == null || fileName.trim().isEmpty()) {
			return null;
		}
		File attachmentFile = null;
		if (fileName.startsWith("http")) {
			attachmentFile = s3DownloadClient.getS3File(fileService.getTenantBucket(), fileName);
		} else {
			attachmentFile = new File(fileName);
		}
		return attachmentFile;
	}

	public boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	public RestResponse processCandidateViaJobPortal(String jobTitle, double totalExperience, String name, String mobile,
			String email, MultipartFile file, double currentCtc, String location, String pastExperience,
			int noticePeriod, String education, String jobPortal, String keySkills, String string) {
		try{

			if(name==null || name.trim().equalsIgnoreCase(""))
				return new RestResponse(RestResponse.FAILED, "Name is missing");
			if(name==null || name.trim().equalsIgnoreCase(""))
				return new RestResponse(RestResponse.FAILED, "email is missing");
			Candidate candidate = new Candidate();
			Set<String> skills = new HashSet<>();
			candidate.setCurrentTitle(jobTitle);
			candidate.setTotalExp(totalExperience);
			candidate.setFullName(name);
			candidate.setMobile(mobile);
			candidate.setEmail(email);
			candidate.setCurrentCtc(currentCtc);
			candidate.setCurrentLocation(location);
			candidate.setNoticePeriod(noticePeriod);
			candidate.setSource(jobPortal);
			candidate.setComments(education);

			if(keySkills!=null && keySkills.trim().contains(",")){
				List<String> items = Arrays.asList(keySkills.split("\\s*,\\s*"));
				for (String skill : items) {
					skills.add(skill);
				}
			}else if(keySkills!=null && !keySkills.trim().equalsIgnoreCase("")){
				skills.add(keySkills);
			}
			candidate.setKeySkills(skills);
			candidate = candidateService.save(candidate);

			if (file != null && !file.isEmpty()) {
				File resumeFile = fileService.multipartToFile(file);
				candidateService.uploadCandidateFile(candidate, resumeFile);

				String activityAction = "";
				if (jobPortal != null && !jobPortal.isEmpty()) {
					activityAction = "Canddiate added via " + jobPortal;
				} else {
					activityAction = "Candidate added ";
				}

				candidateActivityService.addActivity(activityAction, jobPortal, candidate.getCid() + "",
						CandidateActivityType.Added.getDisplayName());

			}

			try{
				String positionCode = null;
				if(jobTitle.contains("(")){
					positionCode = jobTitle.split("\\(")[1].split("\\)")[0].trim();
				}
				
				Position position = positionService.getPositionByCode(positionCode);
				if (position != null) {
					positionService.calculateFinalStatusForPosition(position);
					if (position.getFinalStatus().equalsIgnoreCase(Status.Active.getDisplayName())) {
						addCandidateToBoard(candidate, positionCode);

							// making entry to candidate activity
							RoundCandidate roundCandidate = roundCandidateService.getExistingBoardCandidate(candidate,
									positionCode);
							if (roundCandidate != null) {
								candidateActivityService.addActivity(
										"Added to board for position : $@"
												+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
												.getId()
												+ "_"
												+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getClient()
												.getClientName()
												+ " $#"
												+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getId() + "_"
												+ positionService.getPositionByCode(roundCandidate.getPositionCode()).getTitle()
												+ "$#",
												jobTitle, candidate.getCid() + "", CandidateActivityType.SourcedToBoard.getDisplayName());
							}

					}
				}
			}catch(Exception e){
				return new RestResponse(RestResponse.SUCCESS,"candidate added successfully !!!!!"); 
			}

			return new RestResponse(RestResponse.SUCCESS,"candidate added successfully");
		}catch(Exception e){
			return new RestResponse(RestResponse.FAILED, e.getMessage());
		}

	}	



}
