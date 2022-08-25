package com.bbytes.recruiz.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.InterviewFile;
import com.bbytes.recruiz.domain.TaskSchedule;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TaskScheduleRespository;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class TaskScheduleService extends AbstractService<TaskSchedule, Long> {

    private Logger logger = LoggerFactory.getLogger(TaskScheduleService.class);

    private TaskScheduleRespository taskScheduleRespository;

    @Autowired
    public TaskScheduleService(TaskScheduleRespository taskScheduleRespository) {
	super(taskScheduleRespository);
	this.taskScheduleRespository = taskScheduleRespository;
    }

    @Autowired
    private EmailTemplateDataService emailTemplateDataService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private IEmailService emailService;

    @Transactional(readOnly = true)
    public List<TaskSchedule> getAllTask() {
	return taskScheduleRespository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaskSchedule> getAllBetweenTime() {
	return taskScheduleRespository.findAll();
    }

    @Transactional(readOnly = true)
    public void sendTaskReminder()
	    throws ParseException, MessagingException, IOException, RecruizException {
	DateTime startDate = new DateTime(DateTimeZone.UTC).plusMinutes(15);
	DateTime endDate = new DateTime(DateTimeZone.UTC).plusMinutes(30);

	List<TaskSchedule> remindableTasks = taskScheduleRespository.findByStartAtBetweenAndActive(startDate.toDate(),
		endDate.toDate(), true);

	if (remindableTasks != null && !remindableTasks.isEmpty()) {
	      for (TaskSchedule task : remindableTasks) {
		Set<EventAttendee> taskTaggedUser = task.getAttendee();
		sendTaskReminderToUser(task, taskTaggedUser);
	    }
	} 
    }

    /**
     * 
     * @param task
     * @param tasktaggedUser
     * @param emailBodyMap
     * @throws MessagingException
     * @throws IOException
     */
    private void sendTaskReminderToUser(TaskSchedule task, Set<EventAttendee> tasktaggedUser)
	    throws MessagingException, IOException {

	String interviewerList = "";
	List<String> interviewerEmail = new ArrayList<>();
	if (tasktaggedUser != null && !tasktaggedUser.isEmpty()) {
	    for (EventAttendee eventAttendee : tasktaggedUser) {
		interviewerList = interviewerList + "<br />" + eventAttendee.getName() + " (" + eventAttendee.getEmail()
			+ ")";
		interviewerEmail.add(eventAttendee.getEmail());
	    }
	}

	// setting reminder note
	String div = "<div style=\"background: #fff; border: solid 1px #e8e8e8; border-top: solid 3px #21BCB0; border-bottom: solid 3px #21BCB0; margin: 20px auto 0; overflow: hidden; position: relative; padding: 20px; border-radius: 4px; box-shadow: 0px 10px 10px -5px #e8e8e8; width: 100%; max-width: 768px; font-family: Helvetica, Arial, sans-serif;\">";
	String divClose = "</div>";
	String reminderNote = div + "<p style=\"text-align: center;\"><strong>Task Reminder<br /></strong></p>"
		+ "<p style=\"text-align: justify;\">Task Name : " + task.getTaskItem().getName() + "</p>"
		+ "<p style=\"text-align: justify;\">Reminder Details : " + task.getTaskItem().getNotes()+ "</p>"
		+ "<p style=\"text-align: justify;\">Schedule Date : "
		+ dateTimeService.getUserTimezoneDateString(task.getDueAt(), task.getTaskCreaterEmail()) + "</p>"
		+ divClose;

	// taskItem.getName() + "<br/><br/>" + taskItem.getNotes();
	String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(reminderNote);

	String subject = "Reminder - " + task.getTaskSubject();

	// sending reminder email here after adding the feedback link
	if (tasktaggedUser != null && !tasktaggedUser.isEmpty()) {
	    for (EventAttendee eventAttendee : tasktaggedUser) {
		try {
		    List<String> receiverEmail = new ArrayList<>();
		    receiverEmail.add(eventAttendee.getEmail());

		    emailService.sendCalenderInvite(receiverEmail, renderedMasterTemplate, subject, null, null, null, null);

		} catch (Exception ex) {
		    logger.error("Error while sending reminder for " + task, ex);
		}
	    }
	}
    }

}
