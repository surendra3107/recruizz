package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.OfferLetterForCandidate;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.domain.SendingEmailList;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.CandidateFileRepository;
import com.bbytes.recruiz.repository.EmailRepository;
import com.bbytes.recruiz.repository.OfferLetterForCandidateRepository;
import com.bbytes.recruiz.repository.SendingEmailListRepository;
import com.bbytes.recruiz.utils.FileUtils;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.VelocityTemplateUtils;

@Service
public class EmailActivityService extends AbstractService<EmailActivity, Long> {
	private static final Logger logger = LoggerFactory.getLogger(EmailActivityService.class);

	private EmailRepository emailRepository;

	@Autowired
	public EmailActivityService(EmailRepository emailRepository) {
		super(emailRepository);
		this.emailRepository = emailRepository;
	}

	@Autowired
	private RoundCandidateService roundService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private SendingEmailListRepository sendingEmailListRepository;

	@Autowired
	protected FileService fileService;

	@Autowired
	protected S3DownloadClient s3DownloadClient;

	@Autowired
	private UploadFileService uploadFileService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private EmailTemplateDataService emailTemplateDataService;

	@Autowired
	OfferLetterForCandidateRepository offerLetterForCandidateRepository;

	@Autowired
	CandidateFileRepository candidateFileRepository;

	@Autowired
	CandidateActivityService candidateActivityService;

	@Value("${logo.recruiz}")
	private String logoPath;

	@Transactional(readOnly = true)
	public List<EmailActivity> getAllTemplate() {
		return emailRepository.findAll();
	}

	/**
	 * This method is used to send bulk email to candidates
	 *
	 * @author Akshay
	 * @param emailActivity
	 * @param emailList
	 * @param file
	 * @param fileName
	 * @throws IOException
	 * @throws RecruizException
	 */
	@Transactional
	public void sendBulkEmailActivity(EmailActivity emailActivity, List<String> emailList, MultipartFile file,
			String fileName, boolean isRendered, String templateType) throws IOException, RecruizException {
		saveEmailActivity(emailActivity, null, file, fileName, emailList, isRendered, templateType,null);
	}

	@Transactional
	public void sendBulkEmailActivity(EmailActivity emailActivity, List<String> emailList, MultipartFile file,
			String fileName, boolean isRendered) throws IOException, RecruizException {
		saveEmailActivity(emailActivity, null, file, fileName, emailList, isRendered);
	}

	@Transactional
	public void saveEmailActivity(EmailActivity emailActivity, String roundCandidateId, MultipartFile file,
			String fileName, List<String> candidateEmailList, boolean isRendered) throws IOException, RecruizException {
		saveEmailActivity(emailActivity, roundCandidateId, file, fileName, candidateEmailList, isRendered, null,null);
	}

	/**
	 * save attachment and save activity to db
	 *
	 * @param emailActivity
	 * @param roundCandidateId
	 * @param file
	 * @param additionalAttachment 
	 * @throws IOException
	 * @throws RecruizException
	 */
	@Transactional
	public void saveEmailActivity(EmailActivity emailActivity, String roundCandidateId, MultipartFile file,
			String fileName, List<String> candidateEmailList, boolean isRendered, String templateType, String additionalAttachment)
					throws IOException, RecruizException {
		try {

			List<String> candidateFiles = new ArrayList<>();

			if(roundCandidateId!=null){
				RoundCandidate rcandidate = roundService.findOne(Long.parseLong(roundCandidateId));
				if(additionalAttachment!=null && rcandidate!=null)
					candidateFiles = addSelectedFilesWithAttachment(additionalAttachment, rcandidate.getCandidate().getCid());
			}
			String attachmentPath = "";
			File attachementFile = null;
			if (file != null && !file.isEmpty()) {
				attachmentPath = uploadFileService.createFolderStructureForEmailActivity(userService.getLoggedInUserEmail());
				attachmentPath = FileUtils.uploadAttachmentToLocalServer(file, emailActivity.getId() + "_" + fileName,
						attachmentPath);
				attachementFile = new File(attachmentPath);
				emailActivity.setAttachmentLink(attachmentPath);
				logger.info("Attachement saved at : " + attachmentPath);
			} else if (null != emailActivity.getAttachmentLink()
					&& !emailActivity.getAttachmentLink().trim().isEmpty()) {
				attachementFile = new File(emailActivity.getAttachmentLink());

				// taking only local file
				if (!attachementFile.exists()) {
					attachementFile = null;
				}
			}

			List<String> emailList = new ArrayList<String>();
			if (candidateEmailList != null && !candidateEmailList.isEmpty()) {
				emailList.addAll(candidateEmailList);
				emailActivity.setEmailTo(StringUtils.commaSeparate(candidateEmailList));
			} else {
				emailList.add(emailActivity.getEmailTo());
			}


			String emailTemplate = emailActivity.getBody();
			if (!isRendered) {
				if (null != templateType && !templateType.trim().isEmpty()
						&& templateType.equalsIgnoreCase(GlobalConstants.TemplateTypeFullWidth)) {
					emailTemplate = emailTemplateDataService
							.getMasterTemplateWithoutButtonFullWidth(emailActivity.getBody());
				} else {
					emailTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(emailActivity.getBody());
				}

			}

			if (null != attachementFile && attachementFile.exists() || candidateFiles!=null && candidateFiles.size()>0) {
				List<File> files = new ArrayList<>();
				if(null != attachementFile && attachementFile.exists()){
					files.add(attachementFile);
				}
				if(candidateFiles!=null && candidateFiles.size()>0){
					addSelectedFilesWithAttachment(candidateFiles, files);
				}

				logger.error("Senging bulk attachment email list : " + emailList +" template :"+ emailTemplate + " CC list "+ emailActivity.getCcEmails());
				emailService.sendBulkEmail(emailList, emailTemplate, emailActivity.getSubject(), files, userService.getLoggedInUserEmail(), emailActivity.getCcEmails());
		
			
			} else {
				logger.error("Senging bulk email list : " + emailList +" template :"+ emailTemplate + " CC list "+ emailActivity.getCcEmails());
				emailService.sendBulkEmail(emailList, emailTemplate, emailActivity.getSubject(), null, userService.getLoggedInUserEmail(), emailActivity.getCcEmails());
			}

			emailActivity.setBody(emailTemplate);
			emailActivity.setDate(new java.util.Date());
			emailActivity.setEmailFrom(userService.getLoggedInUserEmail());
			emailRepository.save(emailActivity);

			if (roundCandidateId != null && !roundCandidateId.isEmpty()) {
				candidateActivityService.emailEvent(emailActivity, roundCandidateId);
			} else {
				candidateActivityService.emailEvent(emailActivity, emailList);
			}

			if(roundCandidateId!=null){
				Position position = null;
				long num = Long.parseLong(roundCandidateId);
				RoundCandidate can = roundService.findOne(num);
				if(can.getPositionCode()!=null)
					position = positionService.getPositionByCode(can.getPositionCode());
				if(position!=null){
					position.setModificationDate(new Date());
					positionService.save(position);
				}
			}
			
			
		} catch (Exception ex) {
			logger.error("Email sent failed : " + ex.getMessage(), ex);
		}
	}

	@Transactional
	public void saveEmailActivity(List<EmailActivity> emailActivityList) {
		emailRepository.save(emailActivityList);
	}

	@Transactional
	private String bindMasterTemplateForEmail(String masteTemplateName, String body) {

		Map<String, Object> emailVariableMap = new HashMap<>();
		emailVariableMap.put(GlobalConstants.MasterEmailTemplate, body);
		emailVariableMap.put(GlobalConstants.MasterEmailTemplateLogo, logoPath);

		String masterTemplate = emailTemplateDataService.getTemplateByName(masteTemplateName).getBody();
		String renderedTemplate = VelocityTemplateUtils.getTemplateString(masterTemplate, emailVariableMap);
		return renderedTemplate;
	}

	@Transactional(readOnly = true)
	public void sendJdResponseToHr(String responseText, String hrEmail, String cemail, String pid)
			throws RecruizException {

		String jdResponseTemplate = "email-template-JD-share-response.html";

		Position position = positionService.getPositionByCode(pid);
		String orgName = organizationService.getCurrentOrganization().getOrgName();

		Map<String, Object> variableMap = new HashMap<>();
		variableMap.put(GlobalConstants.ORG_NAME, orgName);
		variableMap.put(GlobalConstants.POSITION_NAME, position.getTitle());
		variableMap.put("cemail", cemail);
		variableMap.put("clientName", position.getClient().getClientName());
		if (responseText.equalsIgnoreCase("interested")) {
			variableMap.put("response", "interested");
		} else if (responseText.equalsIgnoreCase("notinterested")) {
			variableMap.put("response", "not interested");
		}
		emailTemplateDataService.initEmailBodyDefaultVariables(variableMap);

		String renderedTemplate = emailTemplateDataService.getHtmlContentFromFile(variableMap, jdResponseTemplate);

		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplate);
		String subject = "Shared JD Response for position " + position.getTitle();
		ArrayList<String> emailList = new ArrayList<>();
		emailList.add(hrEmail);

		emailService.sendEmail(emailList, renderedMasterTemplate, subject);

	}

	
	@Transactional(readOnly = true)
	public void sendMailToVendorForChangesInCandidateStatus(String oidStatus,String newStatus,String vendorName, String candidateName, String vendorEmail)
			throws RecruizException {

		String jdResponseTemplate = "email-template-vendor-candidateStatus.html";

		Map<String, Object> variableMap = new HashMap<>();
		variableMap.put("Person", vendorName);
		variableMap.put("candidateName", candidateName);
		variableMap.put("oldstatusname", oidStatus);
		variableMap.put("newstatusname", newStatus);
	
		emailTemplateDataService.initEmailBodyDefaultVariables(variableMap);

		String renderedTemplate = emailTemplateDataService.getHtmlContentFromFile(variableMap, jdResponseTemplate);

		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplate);
		String subject = "There has been an update on the candidate profile ";
		ArrayList<String> emailList = new ArrayList<>();
		emailList.add(vendorEmail);

		emailService.sendEmail(emailList, renderedMasterTemplate, subject);

	}
	
	
	@Transactional(readOnly = true)
	public void sendMailToVendorForChangesInCandidateStage(String oidStage,String newStage,String vendorName, String candidateName, String vendorEmail)
			throws RecruizException {

		String jdResponseTemplate = "email-template-vendor-candidateStage.html";

		Map<String, Object> variableMap = new HashMap<>();
		variableMap.put("Person", vendorName);
		variableMap.put("candidateName", candidateName);
		variableMap.put("oldstagename", oidStage);
		variableMap.put("newstagename", newStage);
	
		emailTemplateDataService.initEmailBodyDefaultVariables(variableMap);

		String renderedTemplate = emailTemplateDataService.getHtmlContentFromFile(variableMap, jdResponseTemplate);

		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplate);
		String subject = "There has been an update on the candidate profile ";
		ArrayList<String> emailList = new ArrayList<>();
		emailList.add(vendorEmail);

		emailService.sendEmail(emailList, renderedMasterTemplate, subject);

	}
	
	
	@Transactional(readOnly = true)
	public void sendMailToVendorForChangesInCandidateNotes(String vendorName, String candidateName, String vendorEmail)
			throws RecruizException {

		String jdResponseTemplate = "email-template-vendor-candidateNote.html";

		Map<String, Object> variableMap = new HashMap<>();
		variableMap.put("Person", vendorName);
		variableMap.put("candidateName", candidateName);
	
		emailTemplateDataService.initEmailBodyDefaultVariables(variableMap);

		String renderedTemplate = emailTemplateDataService.getHtmlContentFromFile(variableMap, jdResponseTemplate);

		String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(renderedTemplate);
		String subject = "There has been an update on the candidate profile ";
		ArrayList<String> emailList = new ArrayList<>();
		emailList.add(vendorEmail);

		emailService.sendEmail(emailList, renderedMasterTemplate, subject);

	}
	
	
	@Transactional
	public void saveEmail(String emailBody, String subject, List<String> toEmails, List<String> ccEmails, String fromEmail,String... attachmentFiles) {
		EmailActivity email = new EmailActivity();
		email.setBody(emailBody);
		if (null != ccEmails && !ccEmails.isEmpty()) {
			email.setCc(StringUtils.commaSeparate(ccEmails));
		}
		if (null != toEmails && !toEmails.isEmpty()) {
			email.setEmailTo(StringUtils.commaSeparate(toEmails));
		}
		email.setSubject(subject);
		email.setEmailFrom(fromEmail);
		email.setDate(new java.util.Date());
		if(null != attachmentFiles && attachmentFiles.length>0) {
			List<String> attachmentPath = new ArrayList<>();
			for (String filePath : attachmentFiles) {
				try {
					// copying the sent files to other directory
					String attachmentFileDirectory = uploadFileService.createFolderStructureForEmailActivity(userService.getLoggedInUserEmail());
					File existingFile = new File(filePath);
					if(existingFile.exists()) {
						File attachedFileCopy = new File(attachmentFileDirectory+File.separator+existingFile.getName());
						attachmentPath.add(attachedFileCopy.getPath());
						Files.copy(existingFile.toPath(), attachedFileCopy.toPath());
						//			attachmentPath.add(attachedFileCopy.getPath());
					}
				}catch(Exception ex) {
					// do nothing
				}
			}
			email.setAttachmentLink(StringUtils.commaSeparate(attachmentPath));
		}

		save(email);
	}

	@Transactional(readOnly=true)
	public Page<EmailActivity> getAllEmailsSentBy(String loggedInUserEmail, Pageable pageable) {
		return emailRepository.findByEmailFrom(loggedInUserEmail, pageable);
	}


	private List<String> addSelectedFilesWithAttachment(String selectedFiles, long cid) {

		List<String> filePath = new ArrayList<>();

		String[] fileName = selectedFiles.split(",");
		Set<String> uniqueWords = new HashSet<String>(Arrays.asList(fileName));

		for (String name : uniqueWords) {

			List<OfferLetterForCandidate> offerLetterList = offerLetterForCandidateRepository.getListOfOfferLetterByCandidateId(cid);

			if(offerLetterList.size()>0 && offerLetterList.get(0).getFinalOfferLetterPath()!=null){
				filePath.add(offerLetterList.get(0).getFinalOfferLetterPath());
				String filename = offerLetterList.get(0).getFinalOfferLetterPath().split("offerLetter")[1];
				filename = filename.replace("//", "");
				
				
				name = "/"+name;
				logger.error(" send offerletter via mail to candidate  name = "+name +" filename = "+filename);
				if(name.equalsIgnoreCase(filename)){
					OfferLetterForCandidate data = offerLetterList.get(0);
					if(data!=null){
						data.setField1("yes");
						offerLetterForCandidateRepository.save(data);
					}
				}
				
			}
			
			/*if(name.equalsIgnoreCase("offer.pdf") || name.equalsIgnoreCase("offer_letter")){
				List<OfferLetterForCandidate> list= offerLetterForCandidateRepository.getListOfOfferLetterByCandidateId(cid);
				if(list.get(0).getFinalOfferLetterPath()!=null){
					filePath.add(list.get(0).getFinalOfferLetterPath());
					
					//if offerletter send to candidate then we need to mark yes offerletter sent to candidate
					OfferLetterForCandidate data = list.get(0);
					if(data!=null){
						data.setField1("yes");
						offerLetterForCandidateRepository.save(data);
					}
				}
			}
*/
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

}
