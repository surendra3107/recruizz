//package com.bbytes.recruiz.mail.service.imap;
//
//import java.util.List;
//
//import com.lafaspot.imapnio.client.IMAPSession;
//import com.lafaspot.imapnio.listener.IMAPCommandListener;
//import com.lafaspot.logfast.logging.LogContext;
//import com.lafaspot.logfast.logging.LogManager;
//import com.lafaspot.logfast.logging.Logger;
//import com.lafaspot.logfast.logging.Logger.Level;
//import com.sun.mail.imap.protocol.IMAPResponse;
//
//public class ImapCommandListenerImpl implements IMAPCommandListener {
//
//	private String tag;
//	private LogManager logManager;
//	private Logger log;
//
//	public ImapCommandListenerImpl(String tag) {
//		this.tag = tag;
//		logManager = new LogManager(Level.DEBUG, 5);
//		logManager.setLegacy(true);
//		log = logManager.getLogger(new LogContext("ImapClientIT") {
//		});
//	}
//
//	@Override
//	public void onMessage(IMAPSession arg0, IMAPResponse message) {
//		log.info(tag + " -> " + message, null);
//	}
//
//	@Override
//	public void onResponse(IMAPSession session, String arg1, List<IMAPResponse> responses) {
//		for (final IMAPResponse response : responses) {
//			log.info(tag + " -> " + response, null);
//		}
//
//	}
//
//}
