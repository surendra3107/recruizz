package com.bbytes.recruiz.mail.service;

import javax.mail.Folder;
import javax.mail.Store;

import lombok.Data;

@Data
public class EmailClientSession {

	private Store imapStore;
	private Folder inboxFolder;
	
	
}
