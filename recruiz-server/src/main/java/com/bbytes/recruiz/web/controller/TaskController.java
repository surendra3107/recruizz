package com.bbytes.recruiz.web.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.TaskFolder;
import com.bbytes.recruiz.domain.TaskItem;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.TaskPeriod;
import com.bbytes.recruiz.enums.TaskState;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.TaskItemDTO;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.TaskFolderService;
import com.bbytes.recruiz.service.TaskItemService;
import com.bbytes.recruiz.service.TaskScheduleService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.RestResponseConstant;

@RestController
public class TaskController {

	private static Logger logger = LoggerFactory.getLogger(TaskController.class);

	@Autowired
	private TaskFolderService taskFolderService;

	@Autowired
	private TaskItemService taskItemService;

	@Autowired
	private DataModelToDTOConversionService conversionService;

	@Autowired
	private UserService userService;

	@Autowired
	private TaskScheduleService taskScheduleService;

	@Autowired
	private CheckUserPermissionService checkUserPermissionService;

	/**
	 * Add new task item
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/task/item", method = RequestMethod.POST)
	public RestResponse createTaskItem(@RequestParam String folderName, @RequestBody TaskItem taskItem)
			throws RecruizException, ParseException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// checking for back date
		if (taskItem.getDueDateTime().before(new Date())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.BACK_DATE_NOT_ALLOWED,
					ErrorHandler.BACK_DATE_SELECTED);
		}

		User owner = userService.getLoggedInUserObject();
		TaskFolder taskFolder = taskFolderService.findByNameAndOwner(folderName, owner);

		if (taskFolder == null) {
			RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_FOLDER_DOESNT_EXIST,
					ErrorHandler.TASK_ITEM_SAVE_UPDATE_FAILED);
			return response;
		}
		
		try {
			taskItem = taskItemService.saveTaskItem(folderName, taskItem);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_SCHEDULE_FAILED,
					RestResponseConstant.CAN_NOT_SCHEDULE_TASK);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_ADDED);
		return response;
	}

	/**
	 * Add new task item to folder, if folder does not exist then create or else
	 * add to existing folder
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/task/folder/item", method = RequestMethod.POST)
	public RestResponse addTaskItem(@RequestParam String folderName, @RequestBody TaskItem taskItem)
			throws RecruizException, ParseException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// checking for back date
		if (taskItem.getDueDateTime().before(new Date())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.BACK_DATE_NOT_ALLOWED,
					ErrorHandler.BACK_DATE_SELECTED);
		}

		try {
			taskItem = taskItemService.saveTaskItem(folderName, taskItem);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_SCHEDULE_FAILED,
					RestResponseConstant.CAN_NOT_SCHEDULE_TASK);
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_ADDED);
		return response;
	}

	

	/**
	 * update task item
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/task/item/{taskItemId}", method = RequestMethod.PUT)
	public RestResponse updateTaskItem(@PathVariable Long taskItemId, @RequestBody TaskItem taskItem)
			throws RecruizException, ParseException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// checking for back date
		if (taskItem.getDueDateTime().before(new Date())) {
			return new RestResponse(RestResponse.FAILED, ErrorHandler.BACK_DATE_NOT_ALLOWED,
					ErrorHandler.BACK_DATE_SELECTED);
		}

		TaskItem taskItemFromDB = taskItemService.findOne(taskItemId);
		if (taskItemFromDB == null) {
			RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_ITEM_DOESNT_EXIST,
					ErrorHandler.TASK_ITEM_SAVE_UPDATE_FAILED);
			return response;
		}
		try {
			taskItemService.updateUserAndReminderToTask(taskItem, taskItemFromDB);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_ITEM_UPDATE_FAILED,
					ErrorHandler.TASK_ITEM_SAVE_UPDATE_FAILED);
			return response;
		}

		// taskItemFromDB.setName(taskItem.getName());
		// taskItemFromDB.setNotes(taskItem.getNotes());
		// taskItemFromDB.setDueDateTime(taskItem.getDueDateTime());
		// taskItemFromDB.setReminderPeriod(taskItem.getReminderPeriod());
		// taskItemFromDB.setReminderPeriodType(taskItem.getReminderPeriodType());
		// taskItemFromDB.setUsers(taskItem.getUsers());
		// taskItemFromDB.setReminderDateTime(taskItem.getReminderDateTime());
		// taskItem = taskItemService.save(taskItemFromDB);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_UPDATED);
		return response;
	}

	/**
	 * task item delete
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/item/{taskItemId}/{state}", method = RequestMethod.POST)
	public RestResponse updateTaskItemState(@PathVariable Long taskItemId, @PathVariable String state)
			throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		// making changes, now task state will be updated any just using the ID
		// User owner = userService.getLoggedInUserObject();
		TaskItem taskItem = taskItemService.findOne(taskItemId);

		TaskState taskState = TaskState.valueOf(state);
		if (taskItem != null && taskState != null) {
			taskItem.setState(taskState);
			taskItemService.save(taskItem);
			RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_UPDATED);
			return response;
		}

		RestResponse response = new RestResponse(RestResponse.FAILED,
				RestResponseConstant.TASK_ITEM_OR_STATE_DOESNT_EXIST, ErrorHandler.TASK_ITEM_SAVE_UPDATE_FAILED);
		return response;
	}

	/**
	 * task item delete
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/item/{taskItemId}", method = RequestMethod.DELETE)
	public RestResponse deleteTaskItem(@PathVariable Long taskItemId) throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		try {
			taskItemService.deleteTaskItem(taskItemId, userService.getLoggedInUserObject());
		} catch (Exception ex) {
			RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_ITEM_DOESNT_EXIST,
					ErrorHandler.TASK_ITEM_DELETE_FAILED);
			return response;
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_ITEM_DELETED);
		return response;
	}

	/**
	 * get task item
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/item/folder", method = RequestMethod.GET)
	public RestResponse getTaskItem(@RequestParam String folderName) throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User owner = userService.getLoggedInUserObject();
		List<TaskFolder> taskFolders = taskFolderService.findByName(folderName);
		Set<TaskItem> result = new HashSet<TaskItem>();
		if (taskFolders != null && !taskFolders.isEmpty()) {
			List<TaskItem> taskItems = taskItemService.findByUsersAndTaskFolderIn(owner, taskFolders);
			List<TaskItem> taskItemsOwner = taskItemService.findByOwnerAndTaskFolderIn(owner, taskFolders);
			result.addAll(taskItems);
			result.addAll(taskItemsOwner);
		}

		Set<TaskItemDTO> resultDTOs = conversionService.convertTaskItem(result);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, resultDTOs);
		return response;
	}

	/**
	 * get task item
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/item/period/{timePeriod}", method = RequestMethod.GET)
	public RestResponse getTaskItemForTimePeriod(@PathVariable String timePeriod) throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User owner = userService.getLoggedInUserObject();
		Set<TaskItem> result = new HashSet<TaskItem>();
		List<TaskItem> taskItemsUser = null;
		List<TaskItem> taskItemsOwner = null;
		TaskPeriod taskPeriod = TaskPeriod.valueOf(timePeriod);
		switch (taskPeriod) {
		case Today:
			taskItemsUser = taskItemService.findByUsersAndDueDateTimeBetween(owner,
					DateTime.now().withTimeAtStartOfDay().toDate(),
					DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate());
			taskItemsOwner = taskItemService.findByOwnerAndDueDateTimeBetween(owner,
					DateTime.now().withTimeAtStartOfDay().toDate(),
					DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate());

			break;
		case Tomorrow:
			taskItemsUser = taskItemService.findByUsersAndDueDateTimeBetween(owner,
					DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate(),
					DateTime.now().plusDays(2).withTimeAtStartOfDay().toDate());

			taskItemsOwner = taskItemService.findByOwnerAndDueDateTimeBetween(owner,
					DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate(),
					DateTime.now().plusDays(2).withTimeAtStartOfDay().toDate());
			break;
		case ThisWeek:
			taskItemsUser = taskItemService.findByUsersAndDueDateTimeBetween(owner,
					DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY).toDate(),
					DateTime.now().withDayOfWeek(DateTimeConstants.SUNDAY).toDate());
			taskItemsOwner = taskItemService.findByOwnerAndDueDateTimeBetween(owner,
					DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY).toDate(),
					DateTime.now().withDayOfWeek(DateTimeConstants.SUNDAY).toDate());
			break;
		case ThisMonth:

			DateTime startOfMonth = DateTime.now().dayOfMonth().withMinimumValue();
			DateTime endOfMonth = DateTime.now().dayOfMonth().withMaximumValue();

			taskItemsUser = taskItemService.findByUsersAndDueDateTimeBetween(owner, startOfMonth.toDate(),
					endOfMonth.toDate());
			taskItemsOwner = taskItemService.findByOwnerAndDueDateTimeBetween(owner, startOfMonth.toDate(),
					endOfMonth.toDate());
			break;
		}

		result.addAll(taskItemsUser);
		result.addAll(taskItemsOwner);

		Set<TaskItemDTO> resultDTOs = conversionService.convertTaskItem(result);

		RestResponse response = new RestResponse(RestResponse.SUCCESS, resultDTOs);
		return response;
	}

	/**
	 * Add new task folder
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/folder", method = { RequestMethod.POST, RequestMethod.PUT })
	public RestResponse createTaskFolder(@RequestParam String folderName) throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User owner = userService.getLoggedInUserObject();

		if (taskFolderService.findByNameAndOwner(folderName, owner) == null) {
			TaskFolder taskFolder = new TaskFolder();
			taskFolder.setName(folderName);
			taskFolder.setOwner(userService.getLoggedInUserObject());
			taskFolder = taskFolderService.save(taskFolder);

			RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_FOLDER_ADDED);
			return response;
		}

		RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_FOLDER_EXIST,
				ErrorHandler.TASK_FOLDER_SAVE_UPDATE_FAILED);
		return response;
	}

	/**
	 * Delete task folder
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/folder", method = RequestMethod.DELETE)
	public RestResponse deleteTaskFolder(@RequestParam String folderName) throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User owner = userService.getLoggedInUserObject();
		TaskFolder taskFolder = taskFolderService.findByNameAndOwner(folderName, owner);
		if (taskFolder != null) {
			taskFolderService.delete(taskFolder);
			RestResponse response = new RestResponse(RestResponse.SUCCESS, RestResponseConstant.TASK_FOLDER_DELETED);
			return response;
		}

		RestResponse response = new RestResponse(RestResponse.FAILED, RestResponseConstant.TASK_FOLDER_DOESNT_EXIST,
				ErrorHandler.TASK_FOLDER_DELETE_FAILED);
		return response;

	}

	/**
	 * Get time period list
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/timeperiod", method = RequestMethod.GET)
	public RestResponse getTaskTimePeriods() throws RecruizException {

		TaskPeriod periods[] = TaskPeriod.values();
		List<String> result = new ArrayList<>();
		for (TaskPeriod period : periods) {
			result.add(period.getDisplayName());
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, result);
		return response;

	}

	/**
	 * Get task folder names for current user
	 * 
	 * @param folderName
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/api/v1/task/folder", method = RequestMethod.GET)
	public RestResponse getAllTaskFolderNames() throws RecruizException {

		if (!checkUserPermissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		User owner = userService.getLoggedInUserObject();
		List<TaskFolder> taskFolders = taskFolderService.findByOwner(owner);
		List<String> folderNames = new ArrayList<>();
		if (taskFolders != null && !taskFolders.isEmpty()) {
			for (TaskFolder folder : taskFolders) {
				folderNames.add(folder.getName());
			}
		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, folderNames);
		return response;

	}

}
