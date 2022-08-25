package com.bbytes.recruiz.mail.service.imap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.URLName;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SystemUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.auth.storage.emails.EmailData;
import com.bbytes.recruiz.auth.storage.emails.EmailFolder;
import com.bbytes.recruiz.auth.storage.emails.EmailStoreService;
import com.bbytes.recruiz.domain.EmailClientDetails;
import com.bbytes.recruiz.mail.service.EmailClientSession;
import com.bbytes.recruiz.rest.dto.models.ComposeEmailDTO;
import com.bbytes.recruiz.rest.dto.models.email.AttachmentDTO;
import com.bbytes.recruiz.rest.dto.models.email.IncomingEmailDTO;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.HTMLUtil;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.sun.mail.imap.IMAPFolder;

import lombok.NonNull;

@Service
public class ImapClient {

	private static Logger logger = LoggerFactory.getLogger(ImapClient.class);

	@Autowired
	private EmailStoreService emailStoreService;

	// public ImapClient(EmailStoreService emailStoreService) {
	// this.emailStoreService = emailStoreService;
	// }

	public EmailClientSession getConnectedClient(EmailClientDetails emailClientDetails) throws MessagingException {

		Properties properties = new Properties();
		properties.put("mail.store.protocol", "imaps");
		Session emailSession = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailClientDetails.getEmailId(), emailClientDetails.getPassword());
			}
		});
		// emailSession.setDebug(true);

		final Store imapStore = emailSession.getStore("imaps");

		// try {

		imapStore.connect(emailClientDetails.getImapServerUrl(), emailClientDetails.getEmailId(),
				emailClientDetails.getPassword());

		// logger.warn("\n\n###### Authenticated for Email id " +
		// emailClientDetails.getEmailId());

		Folder inboxFolder = imapStore.getFolder("INBOX");

		EmailClientSession connectedClientSession = new EmailClientSession();
		connectedClientSession.setImapStore(imapStore);
		connectedClientSession.setInboxFolder(inboxFolder);

		Thread lsitenerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					IMAPFolder listenerInboxFolder = (IMAPFolder) imapStore.getFolder("INBOX");
					listenerInboxFolder.open(Folder.READ_ONLY);
					// listenerInboxFolder.idle();
					listenerInboxFolder.addMessageCountListener(new MessageCountListener() {
						@Override
						public void messagesRemoved(MessageCountEvent e) {
							System.out.println("Message removed");
							Message[] removedMessages = e.getMessages();
						}

						@Override
						public void messagesAdded(MessageCountEvent e) {
							System.out.println("Message came");
							Message[] addedMessage = e.getMessages();
						}
					});
				} catch (Exception me) {
					logger.error("\n\n------" + me.getMessage(), me);
				}
			}
		});

		lsitenerThread.start();
		return connectedClientSession;
		// } catch (MessagingException e) {
		// imapStore.close();
		// logger.warn("\n\n@@@@@@@ Email id " + emailClientDetails.getEmailId()
		// + " -> " + e.getMessage(), e);
		// }

		// return null;
	}

	public void getAllFolders(int pageNumber, int pageSize, EmailClientSession emailSession) {
		try {
			Folder[] allFolders = emailSession.getImapStore().getDefaultFolder().list("*");
			if (null != allFolders && allFolders.length <= 0) {
				for (Folder folder : allFolders) {
					Message[] message = folder.getMessages();
				}
			}
		} catch (MessagingException e) {
			logger.warn(e.getMessage(), e);
		}

	}

	// to check if message reply message for same conversation exists or not
	private boolean messageAddedinList(LinkedList<IncomingEmailDTO> addedMessages, @NonNull String referenceIdToCheck) {
		if (addedMessages == null || addedMessages.isEmpty()) {
			return false;
		}

		for (IncomingEmailDTO incomingEmailDTO : addedMessages) {
			if (null != incomingEmailDTO.getReferencesId()
					&& (incomingEmailDTO.getReferencesId().equalsIgnoreCase(referenceIdToCheck)
							|| incomingEmailDTO.getReferencesId().contains(referenceIdToCheck))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * to get IncomingEmailDTO of existing conversation
	 * 
	 * @param addedMessages
	 * @param referenceIdToCheck
	 * @return
	 */
	private IncomingEmailDTO getConversationThreadEmail(LinkedList<IncomingEmailDTO> addedMessages,
			@NonNull String referenceIdToCheck) {

		for (IncomingEmailDTO incomingEmailDTO : addedMessages) {
			if (null != incomingEmailDTO.getReferencesId()
					&& (incomingEmailDTO.getReferencesId().equalsIgnoreCase(referenceIdToCheck)
							|| incomingEmailDTO.getReferencesId().contains(referenceIdToCheck))) {
				return incomingEmailDTO;
			}
		}
		return null;
	}

	/**
	 * to get emails using uid from the inbox folder
	 * 
	 * @param userName
	 * @param password
	 * @param uid
	 * @return
	 */
	public IncomingEmailDTO getMessageUsingUID(EmailClientDetails emailClientDetails, Long uid, String folderName) {
		Store emailStore = null;
		Folder emailFolder = null;
		UIDFolder uf = null;
		IncomingEmailDTO inboxMessageDTO = new IncomingEmailDTO();
		try {

			EmailClientSession connectedSession = getConnectedClient(emailClientDetails);
			emailStore = connectedSession.getImapStore();
			emailFolder = emailStore.getFolder(folderName);
			emailFolder.open(Folder.READ_ONLY);
			uf = (UIDFolder) emailFolder;

			Message message = getMessage(uid, emailClientDetails.getEmailId(), emailClientDetails.getPassword(),
					folderName, emailStore, uf, emailClientDetails.getImapServerUrl());
			if (null == message) {
				return null;
			}

			inboxMessageDTO.setUid(uf.getUID(message));
			inboxMessageDTO.setSubject(message.getSubject());
			inboxMessageDTO.setContentType(message.getContentType());
			inboxMessageDTO.setRecievedDate(message.getReceivedDate());
			inboxMessageDTO.setSentDate(message.getSentDate());
			inboxMessageDTO.setReciever(message.getAllRecipients());
			inboxMessageDTO.setSender(message.getFrom());
			inboxMessageDTO.setStatusFlag(null);

			String result = "";
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				result = getTextFromMimeMultipart(mimeMultipart);
			}
			inboxMessageDTO.setHtmlContent(result);
			inboxMessageDTO.setTextContent(result);

			Multipart multipart = (Multipart) message.getContent();
			List<AttachmentDTO> attachments = new ArrayList<>();

			for (int i = 0; i < multipart.getCount(); i++) {
				MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					AttachmentDTO attachment = new AttachmentDTO();
					attachment.setFileName(part.getFileName());
					attachment.setContentType(part.getContentType());
					attachments.add(attachment);
				}
			}

			// List<AttachmentDTO> attachments = new ArrayList<>();
			// for (int j = 0; j < multipart.getCount(); j++) {
			// BodyPart bodyPart = multipart.getBodyPart(j);
			//
			// DataSource dataSource =
			// bodyPart.getDataHandler().getDataSource();
			// if (null != dataSource.getName()) {
			// AttachmentDTO attachment = new AttachmentDTO();
			// attachment.setFileName(dataSource.getName());
			// attachment.setContentType(dataSource.getContentType());
			//
			// // InputStream stream = dataSource.getInputStream();
			// // byte[] streamBytes = IOUtils.toByteArray(stream);
			// // String encodedString =
			// // Base64.getEncoder().encodeToString(streamBytes);
			// // attachment.setBase64EnocdedString(encodedString);
			// // attachments.add(attachment);
			//
			// } else {
			// if
			// (bodyPart.getContentType().toLowerCase().startsWith("text/plain"))
			// {
			// inboxMessageDTO.setTextContent(bodyPart.getContent().toString());
			// } else if
			// (bodyPart.getContentType().toLowerCase().startsWith("text/html"))
			// {
			// inboxMessageDTO.setHtmlContent(bodyPart.getContent().toString());
			// }
			// }
			// }

			inboxMessageDTO.setAttachments(attachments);

			Enumeration<?> enums = message.getAllHeaders();
			while (enums.hasMoreElements()) {
				Header hdr = (Header) enums.nextElement();
				if (hdr.getName().equalsIgnoreCase("In-Reply-To") || hdr.getName().equalsIgnoreCase("References")) {
					inboxMessageDTO.setReferencesId(hdr.getValue());
					inboxMessageDTO.setInReplyToId(hdr.getValue());
				} else if (hdr.getName().equalsIgnoreCase("Message-ID")) {
					inboxMessageDTO.setMessageId(hdr.getValue());
				} else if (hdr.getName().equalsIgnoreCase("To")) {
					inboxMessageDTO.setTo(hdr.getValue().toString());
				} else if (hdr.getName().equalsIgnoreCase("Cc")) {
					inboxMessageDTO.setCc(hdr.getValue().toString());
				} else if (hdr.getName().equalsIgnoreCase("X-Original-Sender")) {
					inboxMessageDTO.setOriginalSender(hdr.getValue());
				} else {
					System.out.println("\n" + hdr.getName() + " -> " + hdr.getValue());
				}
			}

			emailFolder.close(false);
			emailStore.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != emailFolder && emailFolder.isOpen()) {
					emailFolder.close(false);
				}
				if (null != emailStore && emailStore.isConnected()) {
					emailStore.close();
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
		return inboxMessageDTO;
	}

	/**
	 * Delete email using message id from inbox
	 * 
	 * @param userName
	 * @param password
	 * @param uid
	 * @return
	 */
	public void deleteMessageUsingUID(EmailClientDetails clientDetails, List<Long> emailMessageIds, String folderName) {
		Store emailStore = null;
		Folder emailFolder = null;
		try {

			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
			Session emailSession = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(clientDetails.getEmailId(), clientDetails.getPassword());
				}
			});
			emailStore = emailSession.getStore("imaps");
			emailStore.connect(clientDetails.getImapServerUrl(), clientDetails.getEmailId(),
					clientDetails.getPassword());

			Folder[] folders = emailStore.getDefaultFolder().list("*");
			for (Folder folder : folders) {
				if (folder.getName().equalsIgnoreCase(folderName)) {
					emailFolder = folder;
					break;
				}
			}

			Folder trash = emailStore.getFolder("[Gmail]/Trash");

			// emailFolder = emailStore.getFolder("INBOX");

			UIDFolder uf = (UIDFolder) emailFolder;
			if (!emailFolder.isOpen()) {
				emailFolder.open(Folder.READ_WRITE);
			}

			Message[] messagesToDelete = new Message[emailMessageIds.size()];
			for (int i=0;i<emailMessageIds.size();i++){ // Long messageId : emailMessageIds) {
				Message message = uf.getMessageByUID(emailMessageIds.get(i));
				if (null != message && null != trash) {
					messagesToDelete[i] = message;
				} else {
					message.setFlag(Flags.Flag.DELETED, true);
				}
			}
			
			if(null != messagesToDelete && messagesToDelete.length > 0 && messagesToDelete[0] != null ){
				emailFolder.copyMessages(messagesToDelete, trash);
			}

			emailFolder.expunge();
			emailFolder.close(true);
			emailStore.close();
			emailStoreService.deleteByUid(emailMessageIds, clientDetails.getEmailId(), folderName);

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (emailFolder.isOpen()) {
					emailFolder.close(false);
				}
				if (emailStore.isConnected()) {
					emailStore.close();
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public Message getMessage(Long uid, String userName, String password, String emailFolderName, Store emailStore,
			UIDFolder uf, String imapServerUrl) {
		try {
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
			Session emailSession = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
			emailStore = emailSession.getStore("imaps");
			emailStore.connect(imapServerUrl, userName, password);

			Folder[] folders = emailStore.getDefaultFolder().list("*");
			Folder folderToGetEmail = null;

			for (Folder folder : folders) {
				if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
					if (null != emailFolderName && !emailFolderName.isEmpty()
							&& folder.getName().equalsIgnoreCase(emailFolderName)) {
						folderToGetEmail = folder;
						break;
					}
				}
			}

			if (folderToGetEmail != null) {
				uf = (UIDFolder) folderToGetEmail;
				folderToGetEmail.open(Folder.READ_WRITE);
				Message message = uf.getMessageByUID(uid);
				return message;
			} else {
				for (Folder folder : folders) {
					if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
						uf = (UIDFolder) folder;
						folder.open(Folder.READ_WRITE);
						Message message = uf.getMessageByUID(uid);
						if (null != message)
							return message;
					}
				}
			}
		} catch (Exception ex) {
			logger.warn(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * to send reply to email message
	 * 
	 * @param messageUid
	 * @param composeEmailDTO
	 */
	public void replyToEmail(Long messageUid, ComposeEmailDTO composeEmailDTO, EmailClientDetails emailClientDetails) {

		Store emailStore = null;
		Folder emailFolder = null;
		UIDFolder uf = null;
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

			Message message = getMessage(messageUid, emailClientDetails.getEmailId(), emailClientDetails.getPassword(),
					composeEmailDTO.getFolderName(), emailStore, uf, emailClientDetails.getImapServerUrl());
			JavaMailSender emailSender = getJavaMailSenderClient(emailClientDetails);
			Message replyMessage = emailSender.createMimeMessage();
			replyMessage = message.reply(false);
			replyMessage.setFrom(new InternetAddress(emailClientDetails.getEmailId()));
			replyMessage.setText(composeEmailDTO.getBody());
			replyMessage.setReplyTo(message.getReplyTo());

			// replyMessage.setContent((Multipart) message.getContent());

			// Create your new message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			// messageBodyPart.setText(composeEmailDTO.getBody() + "<br>");
			messageBodyPart.setContent(composeEmailDTO.getBody(), "text/html; charset=utf-8");

			Multipart multipartFromMessage = (Multipart) message.getContent();
			addFilesToMultipartMessage(fileList, multipartFromMessage);
			multipartFromMessage.addBodyPart(messageBodyPart, 0);

			// // Create a multi-part to combine the parts
			// Multipart multipart = new MimeMultipart();
			// multipart.addBodyPart(messageBodyPart);
			//
			// // Create and fill part for the forwarded content
			// messageBodyPart = new MimeBodyPart();
			// messageBodyPart.setDataHandler(message.getDataHandler());
			//
			// // Add part to multi part
			// multipart.addBodyPart(messageBodyPart);

			// Associate multi-part with message
			replyMessage.setContent(multipartFromMessage, "text/html");

			emailSender.send((MimeMessage) replyMessage);
		} catch (Throwable throwable) {
			logger.warn(throwable.getMessage(), throwable);
		} finally {
			try {
				if (null != emailFolder && emailFolder.isOpen()) {
					emailFolder.close(false);
				}
				if (null != emailStore && emailStore.isConnected()) {
					emailStore.close();
				}
			} catch (MessagingException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	/**
	 * To get JavaMailSender for SMTP
	 * 
	 * @return
	 */
	private JavaMailSender getJavaMailSenderClient(EmailClientDetails clientDetails) {

		String host = clientDetails.getSmtpServerUrl(); // userService.getLoggedInUserObject().getHost();
		if (host == null || host.isEmpty()) {
			return null;
		}
		final String username = clientDetails.getEmailId();
		final String password = clientDetails.getPassword();
		// String protocol = userService.getLoggedInUserObject().getProtocol();
		int port = Integer.parseInt(clientDetails.getSmtpServerPort());

		// sets SMTP server properties
		Properties properties = new Properties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.user", username);
		properties.put("mail.password", password);

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};
		Session session = Session.getInstance(properties, auth);

		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setDefaultEncoding("UTF-8");
		sender.setUsername(username);
		sender.setPassword(password);
		// sender.setProtocol(protocol);
		sender.setPort(port);
		sender.setDefaultFileTypeMap(sender.getDefaultFileTypeMap());
		sender.setSession(session);
		sender.setJavaMailProperties(properties);

		return sender;
	}

	// to get list of attached file from the email
	public Map<String,List<File>> getAttachedFilesFromEmail(EmailClientDetails clientDetails) {
		Map<String,List<File>> emailedFileMap = new HashMap<>();
	    if(clientDetails.getEmailFetchStartDate() == null || clientDetails.getEmailFetchEndDate() == null) {
		    return null;
		}
	    
	    // calculating fetch logic here
	    Date syncStartDate = null;
	    Date syncEndDate = null;
	    
	    long difference = clientDetails.getEmailFetchEndDate().getTime() - clientDetails.getEmailFetchStartDate().getTime();
	    int days = (int) (difference / (1000*60*60*24));

	    // if the difference is more than 5 days  then split it into 5 days chunk
	    if(days > 5) {
		if(clientDetails.getLastFetchedEndDate() == null) {
		    syncStartDate = clientDetails.getEmailFetchStartDate();
		    syncEndDate = new DateTime(syncStartDate.getTime()).plusDays(5).toDate();
		    if(syncEndDate.after(clientDetails.getEmailFetchEndDate())) {
			syncEndDate = clientDetails.getEmailFetchEndDate();
		    }
		    clientDetails.setLastFetchedStartDate(syncStartDate);
		    clientDetails.setLastFetchedEndDate(syncEndDate);
		}else if(clientDetails.getLastFetchedEndDate() != null) {
		    syncStartDate = clientDetails.getLastFetchedEndDate();
		    syncEndDate = new DateTime(clientDetails.getLastFetchedEndDate().getTime()).plusDays(5).toDate();
		    if(syncEndDate.after(clientDetails.getEmailFetchEndDate())) {
			syncEndDate = clientDetails.getEmailFetchEndDate();
		    }
		    clientDetails.setLastFetchedStartDate(syncStartDate);
		    clientDetails.setLastFetchedEndDate(syncEndDate);
		}else {
		    syncStartDate = clientDetails.getEmailFetchStartDate();
		    syncEndDate = clientDetails.getEmailFetchEndDate();
		}
		
	    }else {
		syncStartDate = clientDetails.getEmailFetchStartDate();
		syncEndDate = clientDetails.getEmailFetchEndDate(); 
	    }
	    
	    if(syncStartDate.equals(clientDetails.getEmailFetchEndDate())) {
		// logger.error("Can not sync for same start and last date");
		return null;
	    }
	    
		Store emailStore = null;
		Folder emailFolder = null;
		try {
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
			Session emailSession = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(clientDetails.getEmailId(), clientDetails.getPassword());
				}
			});
			emailStore = emailSession.getStore("imaps");
			emailStore.connect(clientDetails.getImapServerUrl(), clientDetails.getEmailId(),
					clientDetails.getPassword());
			emailFolder = emailStore.getFolder("INBOX");
			if (!emailFolder.isOpen()) {
				emailFolder.open(Folder.READ_ONLY);
			}
			
			UIDFolder uf = (UIDFolder) emailFolder;

			SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE,syncStartDate);
			SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LE, syncEndDate);
			
			SearchTerm andTerm = new AndTerm(newerThan, olderThan);
			
			Message[] messages = emailFolder.search(andTerm);

			// returning if no current message is found
			if (null == messages || messages.length == 0) {
			    logger.warn("\n\n\nNo emails found between " + clientDetails.getEmailFetchStartDate() +" And " + clientDetails.getEmailFetchEndDate());    
			    return null;
			}
			
			logger.warn("\n\n\n Total " + messages.length + " emails found between " +syncStartDate +" And " + syncEndDate +" email "+ clientDetails.getEmailId());
			

			// Sort messages from recent to oldest
			Arrays.sort(messages, (m1, m2) -> {
				try {
					return m2.getReceivedDate().compareTo(m1.getReceivedDate());
				} catch (MessagingException e) {
					throw new RuntimeException(e);
				}
			});
			Long currentMaxUid = uf.getUID(messages[0]);
			Long lastMaxUid = clientDetails.getLastMaxUid();
			// if the last max uid is null then assigning the todays first
			// message uid as last max uid
			
			if (null == lastMaxUid) {
				lastMaxUid = uf.getUID(messages[messages.length - 1]);
			} else if (currentMaxUid.longValue() < lastMaxUid.longValue()) {
				// if the message is delete from mail server chances are there
				// to get lower message uid so in that case returning null file
				// list and updating the last max uid to current one
				clientDetails.setLastMaxUid(currentMaxUid);
				logger.warn("\n\n\n\n\n\n Message UID is lower than the processed on, current" + currentMaxUid.longValue() + ", prev : "+lastMaxUid.longValue());
				return null;
			}
			
			// getting only those message which are between last uid to current uid 
			messages = uf.getMessagesByUID(lastMaxUid, currentMaxUid);
			
			clientDetails.setLastMaxUid(currentMaxUid);

			// all latest message since last fetched
			for (int i = 0; i < messages.length; i++) {
			    List<File> allEmailAttachedFile = new ArrayList<File>();
				Message message = messages[i];
				if (message.getContent() instanceof Multipart) {
					Multipart multipart = (Multipart) message.getContent();
					for (int j = 0; j < multipart.getCount(); j++) {
						BodyPart bodyPart = multipart.getBodyPart(j);

						DataSource dataSource = bodyPart.getDataHandler().getDataSource();
						if (null != dataSource.getName()) {
							logger.warn("******************** \n\nAttachment found : " + dataSource.getName()
									+ "\n\n************************");
							if (dataSource.getName().endsWith(".doc") || dataSource.getName().endsWith(".docx")
									|| dataSource.getName().endsWith(".pdf") || dataSource.getName().endsWith(".zip")
									|| dataSource.getName().endsWith(".rar") || dataSource.getName().endsWith(".html")) {
								InputStream stream = dataSource.getInputStream();
								File attachmentFile = new File(SystemUtils.JAVA_IO_TMPDIR + "/"
										+ System.currentTimeMillis() + "-" + dataSource.getName());
								Files.copy(stream, attachmentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
								allEmailAttachedFile.add(attachmentFile);
							}

						}
					}
				}
				
				// add attachement file to map
				if(null != allEmailAttachedFile && !allEmailAttachedFile.isEmpty()) {
				    String subject = message.getSubject();
				    if(null == subject || subject.trim().isEmpty()) {
					subject = "No Subject, From " +message.getFrom();
				    }
				    if(emailedFileMap.containsKey(subject)) {
					 emailedFileMap.get(subject).addAll(allEmailAttachedFile);
				    }else {
					emailedFileMap.put(subject, allEmailAttachedFile);
				    }
				}
			}

		} catch (NoSuchProviderException e) {
			logger.warn(e.getMessage(), e);
		} catch (MessagingException e) {
			logger.warn(e.getMessage(), e);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} finally {
			try {
				if (null != emailFolder && emailFolder.isOpen()) {
					emailFolder.close(false);
				}
				if (null != emailStore && emailStore.isConnected()) {
					emailStore.close();
				}
			} catch (MessagingException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return emailedFileMap;
	}

	/**
	 * Save email in redis cache
	 * 
	 * @param emailSession
	 * @throws MessagingException
	 */
/*	@Async
	public void readAndStoreInRedis(EmailClientSession emailSession, String accountEmail) throws MessagingException {

		javax.mail.Folder[] folders = emailSession.getImapStore().getDefaultFolder().list("*");

		for (javax.mail.Folder fldr : folders) {
			try {
				String folderKey = accountEmail + "-" + fldr.getName();

				EmailFolder folder = emailStoreService.getFolderByAccountEmailAndFolderName(accountEmail,
						fldr.getName());
				if (folder == null) {
					folder = new EmailFolder();
					folder.setAccountEmail(accountEmail);
					folder.setFolderName(fldr.getName());
					folder.setUniqKey(folderKey);
				}
				folder.setNoOfEmails(fldr.getMessageCount());
				folder.setUnreadCount(fldr.getUnreadMessageCount());
				emailStoreService.saveFolder(folder);
			} catch (Throwable th) {
				// logger.warn(th.getMessage(), th);
			}
		}

		for (javax.mail.Folder emailFolder : folders) {
			if ((emailFolder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {

				Map<String, Object> allInboxMessage = new HashMap<>();
				try {
					UIDFolder uf = (UIDFolder) emailFolder;
					emailFolder.open(Folder.READ_ONLY);

					int totalSize = emailFolder.getMessageCount();
					int lowerLimit = totalSize - GlobalConstants.MAX_FETCH_SIZE;
					if (lowerLimit <= 0) {
						lowerLimit = 1;
					}

					Message[] messages = emailFolder.getMessages(lowerLimit, totalSize);
					ArrayUtils.reverse(messages);

					FetchProfile fp = new FetchProfile();
					fp.add(FetchProfile.Item.ENVELOPE);
					fp.add(FetchProfileItem.FLAGS);
					fp.add(FetchProfileItem.CONTENT_INFO);
					fp.add(FetchProfileItem.UID);

					fp.add("X-mailer");
					emailFolder.fetch(messages, fp);

					allInboxMessage.put("all_mails", messages);

					convertToDTOAndStoreInMongo(accountEmail, uf, messages, emailFolder.getName());
					emailFolder.close(false);
				} catch (NoSuchProviderException e) {
					logger.warn(e.getMessage(), e);
				} catch (MessagingException e) {
					logger.warn(e.getMessage(), e);
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				} finally {
					try {
						if (emailFolder.isOpen()) {
							emailFolder.close(false);
						}
					} catch (MessagingException e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}

	}*/

	public void convertToDTOAndStoreInMongo(String accountEmail, UIDFolder uf, Message[] messages, String folderName)
			throws MessagingException, IOException {

		for (int i = 0; i < messages.length; i++) {
			EmailData inboxMessageDTO = new EmailData();
			inboxMessageDTO.setFolderName(folderName);
			MimeMessage message = (MimeMessage) messages[i];

			Long uid = uf.getUID(message);
			String id = uid + "-" + accountEmail + "-" + folderName;

			if (emailStoreService.isEmailExists(id)) {
				// logger.warn("\n\n------------------Email Exists with key : "
				// + id + " -> folder : " + folderName);
				continue;
			}

			inboxMessageDTO.setId(id);
			inboxMessageDTO.setUid(uid);
			inboxMessageDTO.setSubject(message.getSubject());
			inboxMessageDTO.setContentType(message.getContentType());
			inboxMessageDTO.setRecievedDate(message.getReceivedDate());
			inboxMessageDTO.setSentDate(message.getSentDate());

			String[] reciever = covertAddressArrayToStringArray(message.getAllRecipients());
			String[] sender = covertAddressArrayToStringArray(message.getFrom());
			inboxMessageDTO.setSenderString(sender);
			inboxMessageDTO.setRecieverString(reciever);

			inboxMessageDTO.setRecieverEmail(accountEmail);
			inboxMessageDTO.setStatusFlag(message.isSet(Flags.Flag.SEEN));

			String result = "";
			if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
				result = message.getContent().toString();
				result = StringUtils.textToHTML(result);
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				result = getTextFromMimeMultipart(mimeMultipart);
			}

			try {
				String cssCleanedHtml = HTMLUtil.cleanupInlineCssHTMLEmail(result);
				inboxMessageDTO.setHtmlContent(cssCleanedHtml);
			} catch (Exception ex) {
				inboxMessageDTO.setHtmlContent(result);
				logger.error("\n\n------------------" + ex.getMessage(), ex);
			}

			// inboxMessageDTO.setTextContent(result);

			if (message.getContent() instanceof Multipart) {
				Multipart multipart = (Multipart) message.getContent();
				List<AttachmentDTO> attachments = new ArrayList<>();
				for (int j = 0; j < multipart.getCount(); j++) {
					BodyPart bodyPart = multipart.getBodyPart(j);

					DataSource dataSource = bodyPart.getDataHandler().getDataSource();
					if (null != dataSource.getName()) {
						AttachmentDTO attachment = new AttachmentDTO();
						attachment.setFileName(dataSource.getName());
						attachment.setContentType(dataSource.getContentType());
						attachments.add(attachment);
					}
				}
				inboxMessageDTO.setAttachments(attachments);
			}

			Enumeration<?> enums = message.getAllHeaders();
			while (enums.hasMoreElements()) {
				Header hdr = (Header) enums.nextElement();
				if (hdr.getName().equalsIgnoreCase("In-Reply-To") || hdr.getName().equalsIgnoreCase("References")) {
					inboxMessageDTO.setReferencesId(hdr.getValue());
					inboxMessageDTO.setInReplyToId(hdr.getValue());
				} else if (hdr.getName().equalsIgnoreCase("Message-ID")) {
					inboxMessageDTO.setMessageId(hdr.getValue());
				} else if (hdr.getName().equalsIgnoreCase("Cc")) {
					inboxMessageDTO.setCc(hdr.getValue().toString());
				} else if (hdr.getName().equalsIgnoreCase("X-Original-Sender")) {
					inboxMessageDTO.setOriginalSender(hdr.getValue());
				}
			}

			// save to mongo repo here
			emailStoreService.addEmailToMongo(inboxMessageDTO);
		}
	}

	private String[] covertAddressArrayToStringArray(Address[] allRecipients) {
		if (null == allRecipients || allRecipients.length <= 0) {
			return null;
		}
		String[] imapAddress = new String[allRecipients.length];
		for (int i = 0; i < allRecipients.length; i++) {
			Map<String, String> addressMap = new HashMap<>();
			Address address = allRecipients[i];
			imapAddress[i] = address.toString();
		}
		return imapAddress;
	}

	// to be called from refresh button
	public void refreshMailBox(EmailClientSession emailSession, String accountEmail, String folderName)
			throws MessagingException, IOException {

		Folder emailFolder = null;

		Store store = emailSession.getImapStore();

		Folder[] folders = store.getDefaultFolder().list("*");

		// on refresh of email refreshing the folder list as well for the
		// purpose of email counts in each folder
		for (javax.mail.Folder fldr : folders) {
			try {
				String folderKey = accountEmail + "-" + fldr.getName();

				EmailFolder folder = emailStoreService.getFolderByAccountEmailAndFolderName(accountEmail,
						fldr.getName());
				if (folder == null) {
					folder = new EmailFolder();
					folder.setAccountEmail(accountEmail);
					folder.setFolderName(fldr.getName());
					folder.setUniqKey(folderKey);
				}
				folder.setNoOfEmails(fldr.getMessageCount());
				folder.setUnreadCount(fldr.getUnreadMessageCount());
				emailStoreService.saveFolder(folder);
			} catch (Throwable th) {
				// logger.warn(th.getMessage(), th);
			}
		}

		for (Folder folder : folders) {
			if (null == folderName || folderName.isEmpty()) {
				if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
					refreshFolderFromServerToMongo(accountEmail, folder.getName(), folder);
				}
			} else if (folder.getName().equalsIgnoreCase(folderName)) {
				emailFolder = folder;
				refreshFolderFromServerToMongo(accountEmail, folderName, emailFolder);
				break;
			}
		}
	}

	public void refreshFolderFromServerToMongo(String accountEmail, String folderName, Folder emailFolder)
			throws MessagingException, IOException {
		UIDFolder uf = (UIDFolder) emailFolder;
		emailFolder.open(Folder.READ_ONLY);

		Long lastMaxUid = emailStoreService.getMaxUidForEmail(accountEmail, emailFolder.getName());

		Message[] msgs = uf.getMessagesByUID(lastMaxUid + 1, UIDFolder.LASTUID);

		logger.warn("\n\n++++++++ Got last message after uid " + lastMaxUid + " --- and recent last uid is "
				+ UIDFolder.LASTUID + " for folder " + emailFolder.getName() + " $$$$ total message fetch is "
				+ msgs.length + " --- folder is" + emailFolder.getName());

		convertToDTOAndStoreInMongo(accountEmail, uf, msgs, emailFolder.getName());

		if (emailFolder.isOpen()) {
			emailFolder.close(false);
		}
	}

	public void downloadAllAttchment(EmailClientDetails emailAccount, Long uid, HttpServletResponse response,
			String folderName) {
		Store emailStore = null;
		Folder emailFolder = null;
		UIDFolder uf = null;
		try {
			Message message = getMessage(uid, emailAccount.getEmailId(), emailAccount.getPassword(), folderName,
					emailStore, uf, emailAccount.getImapServerUrl());
			if (null == message) {
				return;
			}

			Multipart multiPart = (Multipart) message.getContent();
			List<String> attachmentFiles = new ArrayList<>();
			for (int i = 0; i < multiPart.getCount(); i++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					String filePath = SystemUtils.JAVA_IO_TMPDIR + "/" + part.getFileName();
					part.saveFile(filePath);
					attachmentFiles.add(filePath);
				}
			}

			// ziping all the files and returning to user as stream
			if (null != attachmentFiles && !attachmentFiles.isEmpty()) {
				String zippedFilePath = createZipFileInTmp(attachmentFiles);
				File zipFile = new File(zippedFilePath);
				sendFileResponse(response, zippedFilePath, zipFile.toPath());
			}

			emailFolder.close(false);
			emailStore.close();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (emailFolder.isOpen()) {
					emailFolder.close(false);
				}
				if (emailStore.isConnected()) {
					emailStore.close();
				}
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	// creating zip file
	private String createZipFileInTmp(List<String> attachmentFiles) {
		String zipFile = SystemUtils.JAVA_IO_TMPDIR + "/" + System.currentTimeMillis() + ".zip";

		try {
			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (String filePath : attachmentFiles) {

				File srcFile = new File(filePath);
				FileInputStream fis = new FileInputStream(srcFile);
				// begin writing a new ZIP entry, positions the stream to the
				// start of the entry data
				zos.putNextEntry(new ZipEntry(srcFile.getName()));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
				// close the InputStream
				fis.close();
			}
			// close the ZipOutputStream
			zos.close();
		} catch (IOException ioe) {
			logger.warn("Error Creatiing zip files \n " + ioe.getMessage(), ioe);
			return null;
		}
		return zipFile;
	}

	public void sendFileResponse(HttpServletResponse response, String fileName, Path path) throws IOException {
		InputStream fileStream = new FileInputStream(path.toFile());
		sendFileResponse(response, fileName, fileStream);
		fileStream.close();
	}

	public void sendFileResponse(HttpServletResponse response, String fileName, InputStream fileStream)
			throws IOException {

		if (fileStream == null) {
			return;
		}

		String mimeType = URLConnection.guessContentTypeFromName(fileName);
		if (mimeType == null) {
			mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		if (fileName.endsWith(".csv")) {
			response.setContentType("application/vnd.ms-excel");
		} else {
			response.setContentType(mimeType);
		}

		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + fileName + "\""));

		response.setContentLength(fileStream.available());
		IOUtils.copy(fileStream, response.getOutputStream());
	}

	public void downloadIndividualAttchment(EmailClientSession emailSession, EmailClientDetails emailAccount, Long uid,
			HttpServletResponse response, String folderName, String fileName) {
		Store emailStore = null;
		Folder emailFolder = null;
		UIDFolder uf = null;
		try {

			Store store = emailSession.getImapStore();

			Folder[] folders = store.getDefaultFolder().list("*");
			for (Folder folder : folders) {
				if (folder.getName().equalsIgnoreCase(folderName)) {
					emailFolder = folder;
					emailFolder.open(Folder.READ_ONLY);
					uf = (UIDFolder) emailFolder;
					break;
				}
			}

			Message message = uf.getMessageByUID(uid);
			if (null == message) {
				return;
			}

			Multipart multiPart = (Multipart) message.getContent();
			List<String> attachmentFiles = new ArrayList<>();

			File attachmentFile = null;
			String filePath = null;

			InputStream fileStream = null;

			for (int i = 0; i < multiPart.getCount(); i++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
						|| Part.INLINE.equalsIgnoreCase(part.getDisposition())) {

					// fileStream = part.getInputStream();
					if (part.getFileName().equalsIgnoreCase(fileName)) {
						filePath = SystemUtils.JAVA_IO_TMPDIR + "/" + part.getFileName();
						attachmentFile = new File(filePath);
						part.saveFile(attachmentFile);
						break;
					}
				}
			}
			sendFileResponse(response, fileName, attachmentFile.toPath());
			emailFolder.close(false);

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				result = StringUtils.textToHTML(result);
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				result = result + "<br>" + html;
				// result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	public void setFlagInEmail(String folderName, String flag, List<Long> uids, EmailClientDetails accountDetails)
			throws MessagingException {

		EmailClientSession connectedSession = getConnectedClient(accountDetails);

		Store emailStore = connectedSession.getImapStore();

		Folder[] folders = emailStore.getDefaultFolder().list("*");
		Folder folder = null;
		for (Folder fldr : folders) {
			if (fldr.getName().equalsIgnoreCase(folderName)) {
				folder = fldr;
				break;
			}
		}

		UIDFolder uf = (UIDFolder) folder;
		folder.open(Folder.READ_WRITE);

		long[] uidArray = new long[uids.size()];

		for (int i = 0; i < uids.size(); i++) {
			uidArray[i] = uids.get(i).longValue();
		}

		Message[] messages = uf.getMessagesByUID(uidArray);
		if (GlobalConstants.FLAG_EMAIL_READ.equalsIgnoreCase(flag)) {
			folder.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
		} else if (GlobalConstants.FLAG_EMAIL_UNREAD.equalsIgnoreCase(flag)) {
			folder.setFlags(messages, new Flags(Flags.Flag.SEEN), false);
		}

		// updating stored email after changing flagon server
		List<EmailData> storedEmail = emailStoreService.findByUidAndRecieverEmailAndFolder(uids,
				accountDetails.getEmailId(), folderName);
		if (null != storedEmail && !storedEmail.isEmpty()) {
			for (EmailData emailData : storedEmail) {
				if (GlobalConstants.FLAG_EMAIL_READ.equalsIgnoreCase(flag)) {
					emailData.setStatusFlag(true);
				} else if (GlobalConstants.FLAG_EMAIL_UNREAD.equalsIgnoreCase(flag)) {
					emailData.setStatusFlag(false);
				}
			}
			emailStoreService.save(storedEmail);
		}

		folder.close(true);
		emailStore.close();
	}

	public void fetchNextPages(final String folderName, final int pageNumber, final EmailClientDetails accountDetails,
			int pageSize) throws MessagingException, IOException {

		final String runningThreadKey = accountDetails.getEmailId() + "-" + folderName;
		// if refresh is running for this account and this folder then down run
		// the refresh task again;
		if (emailStoreService.isRunningThreadExistsInSet(runningThreadKey)) {
			return;
		}

		final EmailClientSession connectedClientSession = getConnectedClient(accountDetails);

		int totalEmailCount = emailStoreService.getEmailCountForFolder(folderName, accountDetails.getEmailId());
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					emailStoreService.addRunningThreadInSet(runningThreadKey);

					int servedEmail = pageNumber * pageSize;
					Long messageInMongoForFolder = emailStoreService.getTotalAvailableMessageInMongoCache(folderName,
							accountDetails.getEmailId());

					int pendingEmailsToFetch = (int) (totalEmailCount - messageInMongoForFolder);

					int avaiableToServe = (int) (messageInMongoForFolder - servedEmail);

					// 2 or more than 2 pages are available in mongo then return
					// it other wise start a thread to fetch message
					if (avaiableToServe >= (2 * pageSize)) {
						emailStoreService.removeRunningThreadFromRefreshSet(runningThreadKey);
						return;
					}

					// all messages are avaiable in cache
					if (pendingEmailsToFetch <= 0) {
						emailStoreService.removeRunningThreadFromRefreshSet(runningThreadKey);
						return;
					}

					Store store = connectedClientSession.getImapStore();
					Folder[] folders = store.getDefaultFolder().list("*");
					Folder folderToFetchMessage = null;
					for (Folder fldr : folders) {
						if (fldr.getName().equalsIgnoreCase(folderName)) {
							folderToFetchMessage = fldr;
							break;
						}
					}

					folderToFetchMessage.open(Folder.READ_ONLY);
					UIDFolder uf = (UIDFolder) folderToFetchMessage;

					// assign pending email to fetch as last index
					int lastIndex = pendingEmailsToFetch;
					int startIndex = 1;

					// if pending email to fetch is gttreater than 2 page then
					// decrease 2 page size and assign value as starting index
					if (pendingEmailsToFetch > 2 * pageSize) {
						startIndex = pendingEmailsToFetch - (2 * pageSize);
					}

					// if start index is less than equals 0 than assign 1 to
					// start index
					if (startIndex <= 0) {
						startIndex = 1;
					}

					Message[] messages = folderToFetchMessage.getMessages(startIndex, lastIndex);
					convertToDTOAndStoreInMongo(accountDetails.getEmailId(), uf, messages, folderName);
					emailStoreService.removeRunningThreadFromRefreshSet(runningThreadKey);
				} catch (Throwable th) {
					logger.warn(th.getMessage(), th);
				} finally {
					emailStoreService.removeRunningThreadFromRefreshSet(runningThreadKey);
				}
			}
		});
		th.setName("RECZ - fetching " + folderName + " other pages -> " + accountDetails.getEmailId());
		th.start();
	}

	private void addFilesToMultipartMessage(List<File> files, Multipart multipartFromMessage) {
		if (null != files && !files.isEmpty()) {
			for (File file : files) {
				try {
					if (file.exists()) {
						BodyPart messageBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(file.getPath());

						if (FilenameUtils.getExtension(file.getName()).endsWith("ics")) {

							messageBodyPart.setHeader("Content-Class", "urn:content-  classes:calendarmessage");

							messageBodyPart.setHeader("Content-ID", "calendar_message");

							messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(
									Files.readAllBytes(file.toPath()), "text/calendar; charset=UTF-8")));// very
							// important

						} else {
							messageBodyPart.setDataHandler(new DataHandler(source));
							messageBodyPart.setFileName(file.getName());
						}
						multipartFromMessage.addBodyPart(messageBodyPart);
					}

				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}

	public boolean testEmailConnection(String host, String username, String password) throws Exception {
		URLName url = new URLName("imaps", host, 993, "INBOX", username, password);

		Session session = null;
		Store store;
		Folder folder;

		if (session == null) {
			Properties props = null;
			try {
				props = System.getProperties();
			} catch (SecurityException sex) {
				props = new Properties();
			}
			session = Session.getInstance(props, null);
		}
		store = session.getStore(url);
		store.connect();
		folder = store.getFolder(url);
		folder.open(Folder.READ_ONLY);
		if (null != folder) {
			return true;
		}

		return false;
	}

}
