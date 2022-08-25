package com.bbytes.recruiz.web.controller.email;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.auth.storage.emails.EmailData;
import com.bbytes.recruiz.auth.storage.emails.EmailFolder;
import com.bbytes.recruiz.auth.storage.emails.EmailStoreService;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.mail.service.EmailClientSession;
import com.bbytes.recruiz.mail.service.imap.ImapClient;
import com.bbytes.recruiz.rest.dto.models.ComposeEmailDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.email.IncomingEmailDTO;
import com.bbytes.recruiz.rest.dto.models.email.ListEmailOperationDTO;
import com.bbytes.recruiz.service.EmailAccountDetailService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.SuccessHandler;

@RestController
public class UserMailboxController {

	private static Logger logger = LoggerFactory.getLogger(UserMailboxController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private EmailAccountDetailService emailClientDetailService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private EmailStoreService emailStoreService;

	@Autowired
	private PageableService pageableService;

	/*@Autowired
	private RedisKeyValueTemplate redisKeyValueTemplate;*/

	@Autowired
	private FileService fileService;
	
	@Autowired
	private ImapClient client;

	private static int pageSize = 25;
	private static int startIndexSize = 1;

	@RequestMapping("/api/v1/user/connect/imap")
	public RestResponse getEmails(@RequestParam(required = false, defaultValue = "1") int pageNumber,
			@RequestParam Long emailClientId,
			@RequestParam(required = false, defaultValue = "All Mail") String folder) {
		RestResponse response = null;
		try {

			EmailClientDetails emailClientDetails = emailClientDetailService.findOne(emailClientId);

			if (null == emailClientDetails) {
				return new RestResponse(false, ErrorHandler.No_Email_Client_Configured, ErrorHandler.NO_EMAIL_CLIENT);
			}

			// EmailClientSession connectedClientSession =
			// client.getConnectedClient(emailClientDetails);

			// connectedClientSession =
			// client.getConnectedClient(emailClientDetails);
			// if (null !=
			// emailClientDetailService.getConnectEmailAccountSessionMap() &&
			// emailClientDetailService
			// .getConnectEmailAccountSessionMap().containsKey(emailClientDetails.getEmailId()))
			// {
			// connectedClientSession =
			// emailClientDetailService.getConnectEmailAccountSessionMap()
			// .get(emailClientDetails.getEmailId());
			// } else {
			// emailClientDetailService.getConnectEmailAccountSessionMap().put(emailClientDetails.getEmailId(),
			// connectedClientSession);
			// client.readAndStoreInRedis(connectedClientSession,
			// emailClientDetails.getEmailId());
			// }

			//List<String> folderName = emailStoreService.getFolderNames(emailClientDetails.getEmailId());

			List<EmailFolder> folders = emailStoreService.getEmailAccountFolder(emailClientDetails);
			
			Pageable pageable = new PageRequest(pageNumber - 1, pageSize,
					new Sort(Sort.Direction.DESC, "recievedDate"));

			Page<EmailData> incomingEmail = emailStoreService.getUserAllEmailData(emailClientDetails.getEmailId(),
					pageable, folder);

			int emailCount = emailStoreService.getEmailCountForFolder(folder, emailClientDetails.getEmailId());

			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("emails", incomingEmail);
			responseMap.put("folders", folders);
			responseMap.put("emailCount", emailCount);

			// checking if the content in mongo cache is less than page size of
			// next then starting a thread to fetch more emails
			client.fetchNextPages(folder, pageNumber, emailClientDetails, pageSize);

			response = new RestResponse(true, responseMap);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_FETCH_EMAIL_INBOX);
		}
		return response;
	}

	@RequestMapping("/api/v1/user/imap/get/message/{uid}")
	public RestResponse getMessageUsingUID(@PathVariable long uid, @RequestParam(required = false) Long emailClientId,
			@RequestParam(required = false) String folderName) {

		RestResponse response = null;

		if (folderName == null || folderName.isEmpty()) {
			return new RestResponse(false, "Folder name is missing", "folder_name_missing");
		}

		try {
			EmailClientDetails emailClientDetails = null;
			if (null == emailClientId || emailClientId.longValue() <= 0) {
				emailClientDetails = emailClientDetailService
						.getDefaultEmailAccount(userService.getLoggedInUserObject());
			} else {
				emailClientDetails = emailClientDetailService.findOne(emailClientId);
			}

			if (null == emailClientDetails) {
				return new RestResponse(false, ErrorHandler.No_Email_Client_Configured, ErrorHandler.NO_EMAIL_CLIENT);
			}

			EmailData savedEmail = emailStoreService.findByUidAndRecieverEmailAndFolder(uid,
					emailClientDetails.getEmailId(), folderName);

			// updating the read status of fetched email to read
			savedEmail.setStatusFlag(true);
			emailStoreService.save(savedEmail);
			if (null != savedEmail && (savedEmail.getTextContent() != null || savedEmail.getHtmlContent() != null)) {
				response = new RestResponse(true, savedEmail);
			} else {
				IncomingEmailDTO message = client.getMessageUsingUID(emailClientDetails, uid, folderName);
				response = new RestResponse(true, message);
			}

		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_FETCH_EMAIL_INBOX);
		}
		return response;
	}

	@RequestMapping("/api/v1/user/imap/get/folder")
	public RestResponse getMessageFolder() {
		RestResponse response = null;
		List<String> emailFolders = new ArrayList<>();
		try {

			try {

				EmailClientDetails emailClientDetails = emailClientDetailService.findOne(6L);
				EmailClientSession connectedClientSession = client.getConnectedClient(emailClientDetails);

				javax.mail.Folder[] folders = connectedClientSession.getImapStore().getDefaultFolder().list("*");
				for (javax.mail.Folder folder : folders) {
					if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
						emailFolders.add(folder.getFullName());
						folder.open(Folder.READ_ONLY);
						System.out.println(folder.getFullName() + ": " + folder.getMessageCount());
						Message[] messages = folder.getMessages();
					}
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			response = new RestResponse(true, emailFolders);

		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_FETCH_EMAIL_INBOX);
		}
		return response;
	}

	/**
	 * To send email from user configured email box
	 * 
	 * @param uid
	 * @return
	 */
	@RequestMapping(value = "/api/v1/user/mailbox/email/send", method = RequestMethod.POST)
	public RestResponse sendEmailMessage(@RequestBody(required = false) ComposeEmailDTO composeEmailDTO) {

		if (null == composeEmailDTO.getReciepent() || composeEmailDTO.getReciepent().isEmpty() || composeEmailDTO.getReciepent().contains("")) {
			return new RestResponse(false, "Recipient Missing", "no_recipient");
		}

		RestResponse response = null;
		try {
			List<File> fileList = new LinkedList<File>();
			if (null != composeEmailDTO.getFileMap()) {
				try {
					List<Object> objArray = (List<Object>) composeEmailDTO.getFileMap();
					for (Object object : objArray) {
						try {
							Map<String, String> map = (Map<String, String>) object;
							String fileName = map.get("name");
							String fileContent = map.get("content");
							byte[] fileByte = org.apache.commons.codec.binary.Base64.decodeBase64(fileContent);
							File attachmentFile = new File(SystemUtils.getJavaIoTmpDir() + "/" + fileName);
							attachmentFile.createNewFile();
							Files.write(attachmentFile.toPath(), fileByte, StandardOpenOption.CREATE);
							fileList.add(attachmentFile);
						} catch (Exception ex) {
							// TODO
						}
					}
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}

			Message message = null;
			Store emailStore = null;
			Folder emailFolder = null;
			if (null != composeEmailDTO.getUid()) {
				UIDFolder uf = null;
				EmailClientDetails defaultAccount = emailClientDetailService
						.getDefaultEmailAccount(userService.getLoggedInUserObject());

				message = client.getMessage(composeEmailDTO.getUid(), defaultAccount.getEmailId(),
						defaultAccount.getPassword(), composeEmailDTO.getFolderName(), emailStore, uf,
						defaultAccount.getImapServerUrl());

				Multipart multiPart = (Multipart) message.getContent();
				for (int i = 0; i < multiPart.getCount(); i++) {
					MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || Part.INLINE.equalsIgnoreCase(part.getDisposition())) {
						String filePath = SystemUtils.JAVA_IO_TMPDIR + "/" + part.getFileName();
						part.saveFile(filePath);
						fileList.add(new File(filePath));
					}
				}
			}

			emailService.sendEmailFromEmailClient(composeEmailDTO.getReciepent(), composeEmailDTO.getCc(),
					composeEmailDTO.getBcc(), composeEmailDTO.getBody(), composeEmailDTO.getSubject(), fileList);
			
			try{
				if(emailFolder != null){
					emailFolder.close(true);
				}
				if(emailStore != null){
					emailStore.close();
				}
			}catch(Exception ex){
				logger.error(ex.getMessage(),ex);
			}

			response = new RestResponse(true, "Message Sent", "message_sent");
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_SEND_EMAIL);
		}
		return response;
	}

	/**
	 * To delete emails from inbox
	 * 
	 * @param messageUIDs
	 * @param emailClientId
	 * @return
	 */
	@RequestMapping(value = "/api/v1/user/mailbox/email/delete", method = RequestMethod.POST)
	public RestResponse deleteMessageUsingUID(@RequestBody ListEmailOperationDTO listEmailOperationDTO) {
		RestResponse response = null;
		try {
			if (null == listEmailOperationDTO.getMessageUids() || listEmailOperationDTO.getMessageUids().isEmpty()) {
				return new RestResponse(false, ErrorHandler.MESSAGE_ID_EMPTY, ErrorHandler.FAILED_TO_DELETE_EMAIL);
			}
			EmailClientDetails clientDetails = emailClientDetailService
					.findOne(listEmailOperationDTO.getEmailClientId());
			if (null == clientDetails) {
				return new RestResponse(false, ErrorHandler.EMAIL_CLIENT_DETAILS_NOT_FOUND,
						ErrorHandler.FAILED_TO_GET_EMAIL_CLIENT);
			}

			client.deleteMessageUsingUID(clientDetails, listEmailOperationDTO.getMessageUids(),
					listEmailOperationDTO.getFolderName());
			response = new RestResponse(true, SuccessHandler.MESSAGE_DELETE_SUCCESS, SuccessHandler.MESSAGE_DELETED);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_DELETE_EMAIL);
		}
		return response;
	}

	/***
	 * to send reply email message
	 * 
	 * @param composeEmailDTO
	 * @return
	 */

	@RequestMapping(value = "/api/v1/user/mailbox/email/reply/{messageUid}/{emailClientId}", method = RequestMethod.POST)
	public RestResponse sendReplyEmailMessage(@PathVariable Long messageUid, @PathVariable Long emailClientId,
			@RequestBody ComposeEmailDTO composeEmailDTO) {
		RestResponse response = null;
		try {
			EmailClientDetails emailClientDetails = emailClientDetailService.findOne(emailClientId);

			if (null == emailClientDetails) {
				return new RestResponse(false, ErrorHandler.FAILED_TO_GET_EMAIL_CLIENT_DETAILS,
						ErrorHandler.FAILED_TO_ADD_EMAIL_CLIENT);
			}

			client.replyToEmail(messageUid, composeEmailDTO, emailClientDetails);
			response = new RestResponse(true, "Message Sent", "message_sent");
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			response = new RestResponse(false, e.getMessage(), ErrorHandler.FAILED_TO_SEND_EMAIL);
		}
		return response;
	}

	@RequestMapping("/api/v1/user/mailbox/refresh")
	public RestResponse refreshMailBox(@RequestParam Long emailClientId,
			@RequestParam(required = false) String folder) {

		EmailClientDetails account = emailClientDetailService.findOne(emailClientId);
		
		if(null == account){
			return new RestResponse(false,ErrorHandler.EMAIL_ACCOUNT_NOT_FOUND,ErrorHandler.EMAIL_ACCOUNT_MISSING);
		}

		String refreshThreadKey = "refresh - " + account.getEmailId() + " - " + folder;

		if (!emailStoreService.isRunningThreadExistsInSet(refreshThreadKey)) {
			try {
				
				logger.warn("\n\n--- Refresh started for " + account.getEmailId());
				
				emailStoreService.addRunningThreadInSet(refreshThreadKey);

				EmailClientSession connectedClientSession = client.getConnectedClient(account);
				if(connectedClientSession != null){
					client.refreshMailBox(connectedClientSession, account.getEmailId(), folder);	
				}
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			} finally {
				emailStoreService.removeRunningThreadFromRefreshSet(refreshThreadKey);
			}
		}else{
			logger.warn("\n\n--- Refresh already running for " + account.getEmailId());
		}

		Pageable pageable = new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, "recievedDate"));
		Page<EmailData> incomingEmail = emailStoreService.getUserAllEmailData(account.getEmailId(), pageable, folder);

		Map<String, Object> refreshResponseMap = new HashMap<>();

		int emailCount = 0; //emailStoreService.getEmailCountForFolder(folder, account.getEmailId());

		List<EmailFolder> folders = emailStoreService.getEmailAccountFolder(account);

		refreshResponseMap.put("emails", incomingEmail);
		refreshResponseMap.put("emailCount", emailCount);
		refreshResponseMap.put("allFolders", folders);
		return new RestResponse(true, refreshResponseMap);
	}

	@RequestMapping("/api/v1/user/mailbox/folder/list")
	public RestResponse getMailBoxFolderList(@RequestParam(required = false) Long emailClientId) {

		EmailClientDetails emailAccount = null;
		if (null == emailClientId) {
			emailAccount = emailClientDetailService.getDefaultEmailAccount(userService.getLoggedInUserObject());
		} else {
			emailAccount = emailClientDetailService.findOne(emailClientId);
		}

		// List<String> folderList =
		// emailStoreService.getFolderNames(emailAccount.getEmailId());

		List<EmailFolder> emailAccountFolder = emailStoreService.getEmailAccountFolder(emailAccount);
		return new RestResponse(true, emailAccountFolder);
	}

	@RequestMapping(value = "/api/v1/email/attachment/all", headers = "Accept=*/*", method = RequestMethod.GET)
	public void downloadAllAttachments(HttpServletResponse response,
			@RequestParam(value = "uid", required = true) Long uid,
			@RequestParam(value = "emailClientId", required = false) Long emailClientId,
			@RequestParam(value = "folderName", required = false) String folderName) throws IOException {

		EmailClientDetails emailAccount = emailClientDetailService.findOne(emailClientId);
		client.downloadAllAttchment(emailAccount, uid, response, folderName);
	}

	@RequestMapping(value = "/api/v1/email/attachment/individual", headers = "Accept=*/*", method = RequestMethod.GET)
	public void downloadSingleAttachment(HttpServletResponse response,
			@RequestParam(value = "uid", required = true) Long uid,
			@RequestParam(value = "emailClientId", required = true) Long emailClientId,
			@RequestParam(value = "folderName", required = true) String folderName,
			@RequestParam(value = "fileName", required = true) String fileName) throws IOException, MessagingException {

		EmailClientDetails emailAccount = emailClientDetailService.findOne(emailClientId);

		EmailClientSession connectedClientSession = client.getConnectedClient(emailAccount);
		client.downloadIndividualAttchment(connectedClientSession, emailAccount, uid, response, folderName,
				fileName);
		try{
			connectedClientSession.getInboxFolder().close(true);
			connectedClientSession.getImapStore().close();
		}catch(Exception ex){
			logger.error(ex.getMessage(),ex);
		}
	}

	@RequestMapping(value = "/api/v1/user/mailbox/email/status/change", method = RequestMethod.PUT)
	public RestResponse changeEmailFlag(@RequestBody ListEmailOperationDTO listEmailOperationDTO) {

		EmailClientDetails emailAccount = null;
		if (null == listEmailOperationDTO.getEmailClientId()) {
			emailAccount = emailClientDetailService.getDefaultEmailAccount(userService.getLoggedInUserObject());
		} else {
			emailAccount = emailClientDetailService.findOne(listEmailOperationDTO.getEmailClientId());
		}

		try {
			client.setFlagInEmail(listEmailOperationDTO.getFolderName(), listEmailOperationDTO.getStatus(),
					listEmailOperationDTO.getMessageUids(), emailAccount);
		} catch (MessagingException e) {
			logger.warn(e.getMessage(), e);
			return new RestResponse(true, "Status change failed", "status_change_failed");
		}

		return new RestResponse(true, "Status Changed");
	}

}
