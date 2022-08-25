package com.bbytes.recruiz.service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.EmployeeActivity;
import com.bbytes.recruiz.domain.OnBoardingDetails;
import com.bbytes.recruiz.enums.EmployeeActivityType;
import com.bbytes.recruiz.enums.OnBoardingCategory;
import com.bbytes.recruiz.repository.OnBoardingDetailsRepository;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.GlobalConstants;

import ch.qos.logback.classic.Logger;

@Service
public class OnBoardingDetailsService extends AbstractService<OnBoardingDetails, Long> {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(OnBoardingCategoryService.class);

    private OnBoardingDetailsRepository onBoardingRepository;

    @Autowired
    public OnBoardingDetailsService(OnBoardingDetailsRepository onBoardingRepository) {
	super(onBoardingRepository);
	this.onBoardingRepository = onBoardingRepository;
    }

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private EmployeeActivityService activityService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private EmailTemplateDataService emailTemplateDataService;

    @Autowired
    private OnBoardingCategoryService onBoardingCategoryService;

    public List<OnBoardingDetails> getEmployeeOnBoardActivityList(Long eid, String category, String state) {
	Employee employee = employeeService.findOne(eid);
	return onBoardingRepository.findByEidAndOnboardCategoryAndState(employee, category, state);
    }
    
    public List<OnBoardingDetails> getEmployeeOnBoardActivityList(Long eid) {
	Employee employee = employeeService.findOne(eid);
	return onBoardingRepository.findByEid(employee);
    }

    public OnBoardingDetails addOnBoardingActivity(OnBoardingDetails onBoardingDetails) {
	onBoardingRepository.save(onBoardingDetails);

	// send email here to the added email ids
	try {
	    if (null != onBoardingDetails.getEnrolledPeopleEmails()
		    && !onBoardingDetails.getEnrolledPeopleEmails().trim().isEmpty()) {
		final String emailTemplate = GlobalConstants.EMAIL_TEMPLATE_ON_BOARDING_ASSIGNMENT;
		String emailSubject = "You have been asssigned to work on following onboarding task for '"
			+ onBoardingDetails.getEid().getFirstName();

		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put(GlobalConstants.TaskTitle, onBoardingDetails.getTitle());
		bodyMap.put(GlobalConstants.TaskDescription, onBoardingDetails.getDescription());
		bodyMap.put(GlobalConstants.TaskTime,
			DateTimeUtils.getDateAsString(onBoardingDetails.getScheduleDate(), "dd-MMM-yyyy"));
		bodyMap.put(GlobalConstants.EmployeeName, onBoardingDetails.getEid().getFirstName());

		String templateString = emailTemplateDataService.getHtmlContentFromFile(bodyMap, emailTemplate);
		String masterRenderedTemplate = emailTemplateDataService.getMasterTemplateWithoutButton(templateString);
		emailService.sendEmail(onBoardingDetails.getEnrolledPeoples(), masterRenderedTemplate, emailSubject,
			true);
	    }
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
	}

	return onBoardingDetails;
    }

    // change status on on boarding
    public OnBoardingDetails changeCompletedStatus(Long id, boolean status) {
	OnBoardingDetails onBoardingDetails = onBoardingRepository.findOne(id);
	onBoardingDetails.setCompletedStatus(status);
	onBoardingRepository.save(onBoardingDetails);

	// making entry to employee activity
	EmployeeActivity activity = new EmployeeActivity(userService.getLoggedInUserEmail(),
		userService.getLoggedInUserObject().getName(),
		EmployeeActivityType.OnBoardingTaskStatusChange.getDisplayName(), onBoardingDetails.getTitle(),
		new Date(), onBoardingDetails.getEid().getId());
	activityService.addActivity(activity);

	return onBoardingDetails;
    }

    // to get all distinct category for a employee tasks
    public List<String> getAllDistinctStatusForEmployeeTask(String eid, String category) {
	return onBoardingRepository.findDistinctSubCategoryByCategoryAndEmployee(category, eid);
    }

    // to get grouped map of on boarding activity
    public Map<String, Object> getGroupedMapOfOnBoardingActivity(Long eid, String state) {
	Employee emp = employeeService.findOne(eid);
	LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
	for (OnBoardingCategory category : OnBoardingCategory.values()) {
	    List<String> distinctSubCategory = onBoardingCategoryService
		    .getAllDistinctStatusForEmployeeTask( eid + "",category.name());
	    if (null != distinctSubCategory && !distinctSubCategory.isEmpty()) {
		Map<String, Object> subCategoryMap = new HashMap<>();
		for (String subCategory : distinctSubCategory) {
		    List<OnBoardingDetails> onBoardingDetails = onBoardingRepository
			    .findByEidAndSubCategoryNameAndOnboardCategoryAndState(emp, subCategory, category.name(),
				    state);
		    
		    // adding all activityCommentsToEachTask
		    for (OnBoardingDetails onBoardingTask : onBoardingDetails) {
			onBoardingTask.getComments().size();
		    }
		    
		    // adding list to map
		    if (null != onBoardingDetails && !onBoardingDetails.isEmpty()) {
			subCategoryMap.put(subCategory, onBoardingDetails);
		    }
		}

		if (null != subCategoryMap && subCategoryMap.size() > 0) {
		    responseMap.put(category.name(), subCategoryMap);
		}
	    }
	}

	return responseMap;
    }

    // to get employee on boarding status
    public Long getOnBoardingCountForEmployee(Employee emp, String state, Boolean status) {
	return onBoardingRepository.countByEidAndStateAndCompletedStatus(emp, state, status);
    }

}
