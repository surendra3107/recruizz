package com.bbytes.recruiz.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.domain.InterviewFile;
import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.TaskSchedule;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.IcsFileType;
import com.bbytes.recruiz.enums.TaskState;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TaskItemRepository;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

@Service
public class TaskItemService extends AbstractService<TaskItem, Long> {

    private final Logger logger = LoggerFactory.getLogger(TaskItemService.class);

    private TaskItemRepository taskItemRepository;

    @Autowired
    public TaskItemService(TaskItemRepository taskItemRepository) {
	super(taskItemRepository);
	this.taskItemRepository = taskItemRepository;
    }

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private TaskFolderService taskFolderService;

    @Autowired
    private EmailTemplateDataService emailTemplateDataService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private TaskScheduleService taskScheduleService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private EmailActivityService emailActivityService;

    public Long findTaskItemCountForUser(User user) {
	return this.taskItemRepository.findTaskItemCountForUser(user.getEmail());
    }

    public List<TaskItem> findByName(String name) {
	return this.taskItemRepository.findByName(name);
    }

    public List<TaskItem> findByOwner(User owner) {
	return this.taskItemRepository.findByOwner(owner);
    }

    public TaskItem findByIdAndOwner(Long id, User owner) {
	return this.taskItemRepository.findByIdAndOwner(id, owner);
    }

    public List<TaskItem> findByUser(User user) {
	return this.taskItemRepository.findByUsers(user);
    }

    public List<TaskItem> findByTaskUserIn(List<User> users) {
	return this.taskItemRepository.findByUsersIn(users);
    }

    public List<TaskItem> findByState(TaskState state) {
	return this.taskItemRepository.findByState(state);
    }

    public List<TaskItem> findByUsersIn(List<User> users) {
	return this.taskItemRepository.findByUsersIn(users);
    }

    public List<TaskItem> findByUsersAndTaskFolderIn(User user, List<TaskFolder> taskFolders) {
	return this.taskItemRepository.findByUsersAndTaskFolderIn(user, taskFolders);
    }

    public List<TaskItem> findByOwnerAndTaskFolderIn(User user, List<TaskFolder> taskFolders) {
	return this.taskItemRepository.findByOwnerAndTaskFolderIn(user, taskFolders);
    }

    public List<TaskItem> findByTaskFolder(TaskFolder taskFolder) {
	return this.taskItemRepository.findByTaskFolder(taskFolder);
    }

    public List<TaskItem> findByDueDateTimeBetween(Date start, Date end) {
	return this.taskItemRepository.findByDueDateTimeBetween(start, end);
    }

    public List<TaskItem> findByUsersAndDueDateTimeBetween(User user, Date start, Date end) {
	return this.taskItemRepository.findByUsersAndDueDateTimeBetween(user, start, end);
    }

    public List<TaskItem> findByOwnerAndDueDateTimeBetween(User user, Date start, Date end) {
	return this.taskItemRepository.findByOwnerAndDueDateTimeBetween(user, start, end);
    }

    public List<TaskItem> findByReminderDateTimeBetween(Date start, Date end) {
	return this.taskItemRepository.findByReminderDateTimeBetween(start, end);
    }

    public List<TaskItem> findByUsersAndReminderDateTimeBetween(User user, Date start, Date end) {
	return this.taskItemRepository.findByUsersAndReminderDateTimeBetween(user, start, end);
    }

    public List<TaskItem> findByOwnerAndReminderDateTimeBetween(User user, Date start, Date end) {
	return this.taskItemRepository.findByOwnerAndReminderDateTimeBetween(user, start, end);
    }

    @Transactional
    public TaskItem saveTask(TaskItem taskItem) {
	return taskItemRepository.save(taskItem);
    }

    @Transactional
    public TaskItem saveTaskItem(String folderName, TaskItem taskItem) throws Exception {
	User owner = userService.getLoggedInUserObject();
	TaskFolder taskFolder = taskFolderService.findByNameAndOwner(folderName, owner);

	if (taskFolder == null) {
	    TaskFolder newTaskFolder = new TaskFolder();
	    newTaskFolder.setName(folderName);
	    newTaskFolder.setOwner(owner);
	    taskFolder = taskFolderService.save(newTaskFolder);
	}
	taskItem.setOwner(owner);
	taskItem.setTaskFolder(taskFolder);
	taskItem.processReminderDateTime();
	updateTaskUser(taskItem);

	taskItem = save(taskItem);

	if (taskItem.getReminderPeriod() != null && taskItem.getReminderPeriod() > 0) {
	    taskItem = scheduleTaskReminder(taskItem);
	}
	return taskItem;
    }

    private void updateTaskUser(TaskItem taskItem) {
	Set<User> users = new HashSet<>();
	if (taskItem.getUsers() != null) {
	    for (User user : taskItem.getUsers()) {
		users.add(userService.getUserByEmail(user.getEmail()));
	    }
	}
	taskItem.setUsers(users);
    }

    /**
     * This method will create a ICS file to send calendar invite to task and
     * also it will send a calendar schedule member.
     */
    @Transactional
    public TaskItem scheduleTaskReminder(TaskItem taskItem) throws Exception {

	List<String> emailList = new ArrayList<>();

	// finding and adding user as event attendee
	Set<EventAttendee> taskUsers = new HashSet<EventAttendee>();
	List<String> taskUserNames = new ArrayList<>();
	for (User user : taskItem.getUsers()) {
	    EventAttendee taskUser = new EventAttendee();
	    taskUser.setEmail(user.getEmail());
	    taskUser.setName(user.getName());
	    taskUsers.add(taskUser);
	    emailList.add(user.getEmail());
	    taskUserNames.add(user.getName());
	}

	// adding owner to email list here
	emailList.add(userService.getLoggedInUserEmail());

	// start time will be due date time
	Date reminderDate = taskItem.processReminderDateTime();
	DateTime start = new DateTime(reminderDate.getTime());
	// end time will be remoder date time
	DateTime end = new DateTime(taskItem.getDueDateTime().getTime());

	String taskEventId = calendarService.generatedEventUid();

	File taskScheduleIcsFile = calendarService.createTaskFile(taskItem.getName(), start, end, taskUsers, taskItem.getName(), taskEventId);

	// setting reminder note
	String div = "<div style=\"background: #fff; border: solid 1px #e8e8e8; border-top: solid 3px #21BCB0; border-bottom: solid 3px #21BCB0; margin: 20px auto 0; overflow: hidden; position: relative; padding: 20px; border-radius: 4px; box-shadow: 0px 10px 10px -5px #e8e8e8; width: 100%; max-width: 768px; font-family: Helvetica, Arial, sans-serif;\">";
	String divClose = "</div>";
	String reminderNote = div + "<p style=\"text-align: center;\"><strong>Task Reminder<br /></strong></p>"
		+ "<p style=\"text-align: justify;\">Task Name : " + taskItem.getName() + "</p>"
		+ "<p style=\"text-align: justify;\">Reminder Details : " + taskItem.getNotes() + "</p>"
		+ "<p style=\"text-align: justify;\">Date : "
		+ dateTimeService.getUserTimezoneDateString(taskItem.getDueDateTime(),
			userService.getLoggedInUserEmail())
		+ "</p>" + "<p style=\"text-align: justify;\">User List : " + StringUtils.commaSeparate(taskUserNames)
		+ "</p>" + divClose;

	// taskItem.getName() + "<br/><br/>" + taskItem.getNotes();
	String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(reminderNote);

	InterviewFile taskEventFile = new InterviewFile();
	taskEventFile.setFile(Files.readAllBytes(Paths.get(taskScheduleIcsFile.getAbsolutePath())));
	taskEventFile.setFileType(IcsFileType.Task.getDisplayName());

	List<InterviewFile> eventFiles = new ArrayList<InterviewFile>();
	eventFiles.add(taskEventFile);

	// save task schedule here
	TaskSchedule taskSchedule = new TaskSchedule();
	taskSchedule.setActive(true);
	taskSchedule.setAttendee(taskUsers);
	taskSchedule.setDueAt(taskItem.getDueDateTime());
	taskSchedule.setStartAt(taskItem.getReminderDateTime());
	taskSchedule.setTaskCreaterEmail(userService.getLoggedInUserEmail());
	taskSchedule.setTaskCreaterName(userService.getLoggedInUserObject().getName());
	taskSchedule.setTaskEventId(taskEventId);
	taskSchedule.setTaskNoteContent(taskItem.getNotes());
	taskSchedule.setTaskSubject(taskItem.getName());
	taskSchedule.setTemplateName(taskItem.getName());
	taskSchedule.setFile((new HashSet<>(eventFiles)));

	taskEventFile.setTaskSchedule(taskSchedule);

	taskItem.setTaskSchedule(taskSchedule);
	taskScheduleService.save(taskSchedule);
	taskItem.setOwner(userService.getLoggedInUserObject());
	save(taskItem);

	emailService.sendCalenderInvite(emailList, renderedMasterTemplate, taskItem.getName(),
		taskScheduleIcsFile.getAbsolutePath(), userService.getLoggedInUserEmail(), null, null);

	// adding to email repository
	emailActivityService.saveEmail(renderedMasterTemplate, taskItem.getName(), emailList, null,
		userService.getLoggedInUserEmail(), taskScheduleIcsFile.getAbsolutePath());

	return taskItem;
    }

    /**
     * To delete task item,it will also cancel task reminder if created
     * 
     * @param taskItemId
     * @throws RecruizException
     * @throws ParseException
     * @throws MessagingException
     * @throws ParserException
     * @throws IOException
     */
    @Transactional
    public void deleteTaskItem(Long taskItemId, User owner)
	    throws IOException, ParserException, MessagingException, ParseException, RecruizException {
	TaskItem taskItem = findByIdAndOwner(taskItemId, owner);
	if (taskItem != null) {
	    // if a reminder is added, cancel it and then delete it
	    if (taskItem.getTaskSchedule() != null) {
		List<String> emailList = new ArrayList<String>();
		for (EventAttendee eventAttendee : taskItem.getTaskSchedule().getAttendee()) {
		    emailList.add(eventAttendee.getEmail());
		}
		sendCancelTaskEmail(taskItem.getTaskSchedule(), taskItem.getName(), taskItem.getNotes(), emailList);
		taskScheduleService.delete(taskItem.getTaskSchedule());
	    } else {
		delete(taskItem);
	    }
	}

    }

    // send cancel email to task event Attendee
    @Transactional
    private void sendCancelTaskEmail(TaskSchedule taskSchedule, String taskItemName, String taskNotes,
	    List<String> emailList)
	    throws IOException, ParserException, MessagingException, ParseException, RecruizException {
	InputStream taskFileStream = null;
	for (InterviewFile taskFile : taskSchedule.getFile()) {
	    taskFileStream = new ByteArrayInputStream(taskFile.getFile());
	}
	DateTime start = new DateTime(taskSchedule.getStartAt().getTime());
	DateTime end = new DateTime(taskSchedule.getDueAt().getTime());

	// setting reminder note
	String reminderNote = "Scheduled task <b>" + taskItemName + "</b> is cancelled.<br/><br/>" + taskNotes;
	String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(reminderNote);

	calendarService.cancelInvite(taskSchedule.getNotes(), emailList, start, end, taskFileStream,
		taskSchedule.getTaskSubject(), renderedMasterTemplate, "Task deleted : " + taskItemName);
    }

    /**
     * To update the task item and also update the task schedule if any
     * 
     * @param taskItem
     * @throws Exception
     */
    @Transactional
    public void updateUserAndReminderToTask(TaskItem taskItem, TaskItem taskItemFromDB) throws Exception {
	Set<User> users = new HashSet<>();
	List<String> emailList = new ArrayList<>();
	if (taskItem.getUsers() != null) {
	    for (User user : taskItem.getUsers()) {
		users.add(userService.getUserByEmail(user.getEmail()));
		emailList.add(user.getEmail());
	    }
	}

	taskItem.setUsers(users);
	taskItem.processReminderDateTime();

	// if any task reminder is created then updating it below
	if (taskItemFromDB.getTaskSchedule() != null) {
	    TaskSchedule existingTaskSchedule = taskItemFromDB.getTaskSchedule();
	    Set<EventAttendee> existingEventAttendee = existingTaskSchedule.getAttendee();
	    // if all users are removed then put the existing event attendee in
	    // removed user List
	    List<String> removedUsers = new ArrayList<>();
	    if ((users == null || users.isEmpty())
		    && (existingEventAttendee != null && !existingEventAttendee.isEmpty())) {
		for (EventAttendee attendee : existingEventAttendee) {
		    removedUsers.add(attendee.getEmail());
		}
	    } else {
		for (EventAttendee attendee : existingEventAttendee) {
		    if (!emailList.contains(attendee.getEmail())) {
			removedUsers.add(attendee.getEmail());
		    }
		}
	    }
	    // if user is removed then send cancel email to them
	    sendCancelTaskEmail(existingTaskSchedule, taskItemFromDB.getName(), taskItemFromDB.getNotes(),
		    removedUsers);
	    // reschedule task here
	    taskItem.setId(taskItemFromDB.getId());
	    taskItem.setTaskFolder(taskItemFromDB.getTaskFolder());
	    taskItem.setOwner(taskItemFromDB.getOwner());
	    rescheduleTaskEvent(taskItem.getUsers(), existingTaskSchedule, taskItem);
	} else {
	    // updating the actual object here
	    taskItem.setId(taskItemFromDB.getId());
	    taskItem.setOwner(taskItemFromDB.getOwner());
	    taskItem.setTaskFolder(taskItemFromDB.getTaskFolder());
	}

	taskItem = save(taskItem);
    }

    /**
     * To reschedule existing task schedule
     * 
     * @param taskScheduleFileBytes
     * @param attendeeList
     * @param startDate
     * @param endDate
     * @param templateName
     * @param templateSubject
     * @param scheduleTemplate
     * @param taskSchedule
     * @param task
     * @throws Exception
     */
    public void rescheduleTaskEvent(Set<User> userList, TaskSchedule taskSchedule, TaskItem task) throws Exception {

	Set<String> newEmailList = new HashSet<String>();

	// adding HR executive to new attendee list
	newEmailList.add(userService.getLoggedInUserEmail());

	// start time will be due date time
	Date reminderDate = task.processReminderDateTime();
	DateTime start = new DateTime(reminderDate.getTime());
	// end time will be remoder date time
	DateTime end = new DateTime(task.getDueDateTime().getTime());

	InputStream stream = null;
	for (InterviewFile taskFile : taskSchedule.getFile()) {
	    stream = new ByteArrayInputStream(taskFile.getFile());
	}

	CalendarBuilder builder = new CalendarBuilder();
	Calendar calendar = builder.build(stream);
	VEvent event = (VEvent) calendar.getComponent(VEvent.VEVENT);

	Sequence sequence = event.getSequence();

	int seqVal = sequence.getSequenceNo();

	List<Attendee> attendees = event.getProperties(Attendee.ATTENDEE);

	Organizer organizer = event.getOrganizer();

	Calendar rescheduleCalender = new Calendar();
	rescheduleCalender.getProperties().add(new ProdId("//BeyondBytes//iCal4j 1.0//EN"));
	rescheduleCalender.getProperties().add(Version.VERSION_2_0);
	rescheduleCalender.getProperties().add(Method.PUBLISH);
	rescheduleCalender.getProperties().add(Method.REQUEST);

	VEvent vevent = new VEvent(start, end, "Task rescheduled");

	// if someone is removed then a cancel event has to be sent to them so
	// adding them to removed attendee list
	Set<String> removedAttendee = new HashSet<String>();
	for (Attendee attende : attendees) {
	    removedAttendee.add(attende.getValue().replace("mailto:", ""));
	}
	for (String newAttendee : newEmailList) {
	    boolean exists = false;
	    for (Attendee attende : attendees) {
		if (attende.getValue().contains(newAttendee)) {
		    vevent.getProperties().add(attende);
		    removedAttendee.remove(attende.getValue().replace("mailto:", ""));
		    exists = true;
		    break;
		} else {
		    vevent.getProperties().remove(attende);
		    exists = false;
		}
	    }
	    if (!exists) {
		Attendee newAttende = new Attendee(URI.create("mailto:" + newAttendee));
		((Property) newAttende).getParameters().add(Role.REQ_PARTICIPANT);
		((Property) newAttende).getParameters().add(Rsvp.TRUE);
		vevent.getProperties().add(newAttende);
	    }
	}

	vevent.getProperties().add(new Transp("OPAQUE"));
	vevent.getProperties().add(new DtEnd(end));
	vevent.getProperties().add(new Created(new DateTime(System.currentTimeMillis())));
	vevent.getProperties().add(new DtStamp(new DateTime(System.currentTimeMillis())));
	vevent.getProperties().add(new Description(taskSchedule.getNotes()));
	vevent.getProperties().add(new Uid(event.getUid().getValue()));

	vevent.getProperties().add(organizer);
	vevent.getProperties().add(Priority.HIGH);
	vevent.getProperties().remove(sequence);
	vevent.getProperties().add(new Sequence(seqVal + 1));

	rescheduleCalender.getComponents().add(vevent);

	File taskScheduleIcsFile = new File("schedule.ics");
	taskScheduleIcsFile.createNewFile();
	String calFile = taskScheduleIcsFile.getPath();
	FileOutputStream fout = null;

	try {
	    fout = new FileOutputStream(calFile);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	CalendarOutputter outputter = new CalendarOutputter();
	outputter.setValidating(false);

	try {
	    outputter.output(rescheduleCalender, fout);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ValidationException e) {
	    e.printStackTrace();
	}

	// getting rendered template here and sending re schedule email
	String template = taskSchedule.getTaskNoteContent();
	String renderedMasterTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(template);
	List<String> emailList = new ArrayList<String>(newEmailList);
	emailService.sendCalenderInvite(emailList, renderedMasterTemplate, task.getName(),
		taskScheduleIcsFile.getAbsolutePath(), userService.getLoggedInUserEmail(), null, null);

	// save actual task object here
	InterviewFile taskEventFile = new InterviewFile();
	taskEventFile.setFile(Files.readAllBytes(Paths.get(taskScheduleIcsFile.getAbsolutePath())));
	taskEventFile.setFileType(IcsFileType.Task.getDisplayName());

	List<InterviewFile> eventFiles = new ArrayList<InterviewFile>();
	eventFiles.add(taskEventFile);

	// finding and adding user as event attendee
	EventAttendee taskUser = null;
	Set<EventAttendee> taskUsers = new HashSet<EventAttendee>();
	for (User user : task.getUsers()) {
	    taskUser = new EventAttendee();
	    taskUser.setEmail(user.getEmail());
	    taskUser.setName(user.getName());
	    taskUsers.add(taskUser);
	}
	// save task schedule here
	taskSchedule.setActive(true);
	taskSchedule.setAttendee(taskUsers);
	taskSchedule.setDueAt(task.getDueDateTime());
	taskSchedule.setStartAt(task.getReminderDateTime());
	taskSchedule.setTaskCreaterEmail(userService.getLoggedInUserEmail());
	taskSchedule.setTaskCreaterName(userService.getLoggedInUserObject().getName());
	taskSchedule.setTaskNoteContent(task.getNotes());
	taskSchedule.setTaskSubject(task.getName());
	taskSchedule.setTemplateName(task.getName());

	// taskSchedule.getFile().clear();

	taskSchedule.setFile(new HashSet<>(eventFiles));

	taskEventFile.setTaskSchedule(taskSchedule);

	task.setTaskSchedule(taskSchedule);
	taskScheduleService.save(taskSchedule);

	save(task);
    }

    /**
     * to send interview Schedule reminder
     * 
     * @throws ParseException
     * @throws IOException
     * @throws MessagingException
     * @throws RecruizException
     */
    @Transactional(readOnly = true)
    public void sendTaskReminderAsEmail() throws ParseException, MessagingException, IOException, RecruizException {
	org.joda.time.DateTime startDate = new org.joda.time.DateTime(DateTimeZone.UTC);
	org.joda.time.DateTime endDate = new org.joda.time.DateTime(DateTimeZone.UTC).plusMinutes(30);

	List<TaskItem> taskItems = new ArrayList<>();

	List<TaskItem> taskItemsByReminderDate = findByReminderDateTimeBetween(startDate.toDate(), endDate.toDate());

	List<TaskItem> taskItemByDueDate = findByDueDateTimeBetween(startDate.toDate(), endDate.toDate());

	taskItems.addAll(taskItemsByReminderDate);
	taskItems.addAll(taskItemByDueDate);

	final String reminderTemplate = "email-template-task-item-reminder-email.html";

	String reminderSubject = "Task reminder  - ";

	if (taskItems != null && !taskItems.isEmpty()) {
	    logger.debug(
		    " total " + taskItems.size() + " schedules found in tenant " + TenantContextHolder.getTenant());
	    for (TaskItem taskItem : taskItems) {
		reminderSubject = reminderSubject + taskItem.getName();

		Set<User> usersToBeReminded = taskItem.getUsers();
		List<String> emailList = new ArrayList<>();
		for (User user : usersToBeReminded) {
		    emailList.add(user.getEmail());
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

		Map<String, Object> emailBodyMap = new HashMap<>();
		emailBodyMap.put(GlobalConstants.TASK_NAME, taskItem.getName());
		emailBodyMap.put(GlobalConstants.TASK_NOTES, taskItem.getNotes());

		if (taskItem.getDueDateTime() != null)
		    emailBodyMap.put(GlobalConstants.TASK_DUE_DATE, dateFormat.format(taskItem.getDueDateTime()));

		emailService.sendEmail(reminderTemplate, emailList, reminderSubject, emailBodyMap);

	    }
	} else {
	    logger.debug("No task schedules found for tenant" + TenantContextHolder.getTenant());
	}
    }
}
