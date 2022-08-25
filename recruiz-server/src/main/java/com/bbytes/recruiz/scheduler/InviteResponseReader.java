package com.bbytes.recruiz.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.InterviewSchedule;
import com.bbytes.recruiz.repository.InterviewScheduleRepository;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.UserService;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Attendee;

@Component
//@EnableScheduling
public class InviteResponseReader {

	private static Logger logger = LoggerFactory.getLogger(InviteResponseReader.class);
	
	@Autowired
	private InterviewScheduleService scheduleService;

	@Autowired
	private InterviewScheduleRepository interviewScheduleRepo;

	@Autowired
	private UserService userService;

	@Value("${meeting.invite.email}")
	private String meetingEmail;

	@Value("${meeting.invite.password}")
	private String meetingPassword;

	@Value("${meeting.email.protocol}")
	private String meetingEmailProtocol;

	//@PostConstruct
	@Async
	//@Scheduled(fixedDelay = 30000)
	public void readEmail() throws MessagingException {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = null;
		Store store = null;
		try {
			session = Session.getInstance(props, null);
			store = session.getStore();
			store.connect(meetingEmailProtocol, meetingEmail, meetingPassword);
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);

			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
			Message[] unreadMsgs = inbox.search(unseenFlagTerm);
			for (Message message : unreadMsgs) {

				Address[] address = message.getFrom();
				String fromEmail = address[0].toString();
//				logger.debug("Reading email  : " + fromEmail);

				String messageType = message.getContentType();
				if (messageType.toUpperCase().contains("TEXT/CALENDAR")) {
					DataHandler handler = message.getDataHandler();
					InputStream is = handler.getInputStream();
					createCalender(fromEmail, is);
				} else if (messageType.toLowerCase().contains("multipart")) {
					Multipart mp = (Multipart) message.getContent();
					for (int i = 0; i < address.length; i++) {
						for (int j = 0; j < mp.getCount(); j++) {
							BodyPart bodyPart = mp.getBodyPart(j);
							String disposition = bodyPart.getDisposition();
							if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
								DataHandler handler = bodyPart.getDataHandler();
								InputStream is = handler.getInputStream();
								createCalender(fromEmail, is);
							}
						}
					}
					// message.setFlag(Flags.Flag.DELETED, true);
				}

			}

		} catch (Exception mex) {
			logger.error(mex.getMessage(),mex);
		} finally {
			if (store != null)
				store.close();
		}
	}

	private void createCalender(String fromEmail, InputStream is) throws IOException, ParserException {
		if (fromEmail.contains("<"))
			fromEmail = fromEmail.substring(fromEmail.indexOf("<") + 1, fromEmail.indexOf(">"));
		String status = "", comment = "";
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(is);
		VEvent event = (VEvent) calendar.getComponent(VEvent.VEVENT);
		List<Attendee> attnedees = event.getProperties(Attendee.ATTENDEE);
		for (Attendee attendee : attnedees) {
			String email = attendee.getValue();
			//status = attendee.getParameter(Parameter.PARTSTAT).getValue();
			// comment = event.getProperty(Property.COMMENT);
			//System.out.println(email + "\n" + event.getProperty(Property.COMMENT));
			//System.out.println("Status" + attendee.getParameter(Parameter.PARTSTAT));

		}

		String eventId = event.getUid().getValue();
		InterviewSchedule scheduleDetails = scheduleService.getScheduleDetailsByEventId(eventId, eventId);
		if(scheduleDetails != null){
			if (scheduleDetails.getCandidateEventId().equalsIgnoreCase(eventId)) {
				scheduleDetails.setCandidateAccepted(status);
			} else if (scheduleDetails.getInterviewerEventId().equalsIgnoreCase(eventId)) {
				Set<EventAttendee> attendees = scheduleDetails.getAttendee();
				for (EventAttendee eventAttendee : attendees) {
					if (eventAttendee.getEmail().equalsIgnoreCase(fromEmail))
						eventAttendee.setStatus(status);
				}
			}
			scheduleService.save(scheduleDetails);
		}
	}
}
