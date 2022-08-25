package com.bbytes.recruiz.mail.service.imap;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.bbytes.recruiz.RecruizWebBaseApplicationTests;

public class ImapClientTest extends RecruizWebBaseApplicationTests{

	
	@Test
	public void testCreateImapClient() {
		fail("Not yet implemented");
	}
	
	@Before
	public void setUp(){
		super.setUp();
	}

	@Test
	public void testConnectToGmailServer() {
		try {
			//sIMAPClient imapClient = client.createImapClient();
			//client.connectToGmailServer(imapClient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
