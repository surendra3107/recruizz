package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.EmployeeFile;
import com.bbytes.recruiz.domain.OnBoardingDetails;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.EmployeeDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.EmployeeFileService;
import com.bbytes.recruiz.service.EmployeeService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.OnBoardingDetailsService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.UploadFileService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Client Controller
 *
 */
@RestController
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @Autowired
    private PageableService pageableService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private FileService fileService;

    @Autowired
    private EmployeeFileService employeeFileService;

    @Autowired
    private OnBoardingDetailsService onBoardingDetailsService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;

    // to add or update a sub category
    @RequestMapping(value = "/api/v1/employee/{eid}/edit", method = RequestMethod.POST)
    public RestResponse addUpdateEmployee(@PathVariable Long eid, @RequestBody EmployeeDTO employee)
	    throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.EmployeeAddUpdate.name());*/

	try {
	    if(employee.getEmpID() != null && employeeService.getByEmpId(employee.getEmpID()) != null) {
		 return new RestResponse(RestResponse.FAILED, ErrorHandler.DUPLICATE_EMPLOYEE_ID, ErrorHandler.EMPLOYEE_ID_EXISTS);
	    }
	    employeeService.editEmployee(eid, employee);
	    return new RestResponse(RestResponse.SUCCESS, SuccessHandler.EMPOYEE_EDITED);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.NO_CATEGORY_SENT, ErrorHandler.NO_DATA_FOUND);
	}
    }

    // to delete a
    @RequestMapping(value = "/api/v1/employee/{eid}/delete", method = RequestMethod.DELETE)
    public RestResponse deleteEmployee(@PathVariable Long eid) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteEmployee.name());*/

	RestResponse response = null;
	try {
	    List<OnBoardingDetails> empOnBoardingTasks = onBoardingDetailsService.getEmployeeOnBoardActivityList(eid);
	    if (null != empOnBoardingTasks && !empOnBoardingTasks.isEmpty()) {
		for (OnBoardingDetails onBoardingDetails : empOnBoardingTasks) {
		    onBoardingDetails.setEid(null);
		    onBoardingDetailsService.save(onBoardingDetails);
		}
	    }

	    employeeService.deleteEmployee(eid);
	    response = new RestResponse(true, SuccessHandler.EMPLOYEE_DELETED);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(RestResponse.FAILED, ErrorHandler.DELETING_EMPLOYEE_FAILED,
		    ErrorHandler.DELETE_FAILED);
	}
	return response;
    }

    // to upload employee file

    @RequestMapping(value = "/api/v1/employee/{eid}/upload/file", method = RequestMethod.POST)
    public RestResponse uploadFiles(@RequestPart("file") MultipartFile file, @RequestParam("fileName") String fileName,
	    @RequestParam("fileType") String fileType, @PathVariable("eid") Long eid)
	    throws RecruizException, IOException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.AddEmployeeFiles.name());*/

	if (fileName == null || fileName.isEmpty() || fileType == null || fileType.isEmpty() || eid == null)
	    return null;

	Employee emp = employeeService.findOne(eid);

	if (fileName != null && !fileName.isEmpty()) {
	    fileName = StringUtils.cleanFileName(fileName);
	}

	String employeeFolder = uploadFileService.createFolderStructureForEmployee(eid + "");
	File fileToUpload = new File(employeeFolder + File.separator + fileName);
	if (fileToUpload.exists()) {
	    return new RestResponse(false, ErrorHandler.FILE_UPLOAD_FAILED, ErrorHandler.FILE_EXISTS);
	}

	File tmpFile = fileService.multipartToFile(file);
	Files.copy(tmpFile.toPath(), fileToUpload.toPath());

	EmployeeFile employeeFile = new EmployeeFile();
	employeeFile.setEid(eid + "");
	employeeFile.setFileName(fileName);
	employeeFile.setFilePath(fileToUpload.getPath());
	employeeFile.setFileType(fileType);

	emp.getFiles().add(employeeFile);
	employeeService.save(emp);

	if (null != emp.getFiles() && !emp.getFiles().isEmpty()) {
	    emp.getFiles().size();
	}

	RestResponse candidateAddResponse = new RestResponse(RestResponse.SUCCESS, emp.getFiles(), null);
	return candidateAddResponse;
    }

    // to get all client files
    @RequestMapping(value = "/api/v1/employee/{eid}/files/all", method = RequestMethod.GET)
    public RestResponse getAllEmployeeFiles(@PathVariable("eid") String eid) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetEmployeeFiles.name());*/

	RestResponse response = null;
	List<EmployeeFile> files = employeeFileService.getEmployeeFilesByeid(eid);
	response = new RestResponse(true, files);
	return response;
    }

    // to get all client files
    @RequestMapping(value = "/api/v1/employee/file/{fileId}/delete", method = RequestMethod.DELETE)
    public RestResponse deleteEmployeeFile(@PathVariable("fileId") Long fileId) throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.DeleteEmployee.name());*/

	RestResponse response = null;
	EmployeeFile file = employeeFileService.findOne(fileId);
	if (null != file) {
	    String filePath = file.getFilePath();
	    File diskFile = new File(filePath);

	    if (null != file.getEid() && !file.getEid().trim().isEmpty()) {
		Employee emp = employeeService.findOne(Long.parseLong(file.getEid()));
		emp.getFiles().remove(file);
		employeeService.save(emp);
		employeeFileService.delete(file);
	    }
	    if (diskFile.exists()) {
		diskFile.delete();
	    }
	}
	response = new RestResponse(true, SuccessHandler.FILE_DELETED);
	return response;
    }

    @RequestMapping(value = "/api/v1/employee/dashboard/count", method = RequestMethod.GET)
    public RestResponse getEmployeeDashboardCount() throws RecruizException {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetEmployeeDashboard.name());*/

	RestResponse response = null;
	Map<String, Object> employeeDashBoardCount = employeeService.getDashboardCount();
	response = new RestResponse(true, employeeDashBoardCount);
	return response;
    }

    @RequestMapping(value = "/api/v1/employee/all", method = RequestMethod.GET)
    public RestResponse getAllEmployee(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllEmployee.name());*/

	RestResponse response = new RestResponse(true, employeeService.getAllEmployeeDTO(
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder))));

	return response;
    }

    @RequestMapping(value = "/api/v1/employee/yet/to/onboard", method = RequestMethod.GET)
    public RestResponse getAllYetToOnBoardCandidate(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllYetToOnBoardCandidate.name());*/

	RestResponse response = new RestResponse(true, employeeService.getYetToOnBoardEmployeeDTO(
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder))));

	return response;
    }

    @RequestMapping(value = "/api/v1/employee/details/{eid}", method = RequestMethod.GET)
    public RestResponse getEmployeeDetails(@PathVariable Long eid) throws Exception {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetEmployeeDetails.name());*/

	RestResponse response = new RestResponse(true, employeeService.getEmployeeeById(eid));
	return response;
    }

    // get employee activity
    @RequestMapping(value = "/api/v1/employee/{eid}/activity/all", method = RequestMethod.GET)
    public RestResponse getEmployeeActivity(@PathVariable Long eid,
	    @RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder) throws Exception {

	RestResponse response = new RestResponse(true, employeeService.getAllActivityForEmployee(eid,
		pageableService.getPageRequestObject(pageNo, sortField, pageableService.getSortDirection(sortOrder))));

	return response;
    }

    // to get employee details by presonal email ID
    @RequestMapping(value = "/api/v1/employee/find/email", method = RequestMethod.GET)
    public RestResponse getEmployeeDetailsByPresonalEmail(@RequestParam String emailId) throws Exception {

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetEmployeeDetails.name());*/

	RestResponse response = new RestResponse(true, employeeService.getEmployeeByPresonalEmail(emailId));
	return response;
    }

    @RequestMapping(value = "/api/v1/employee/exists", method = RequestMethod.GET)
    public RestResponse isEmployeeExists(@RequestParam String personalEmailId) throws RecruizException {

	try {
	    Employee emp = employeeService.getEmployeeByPresonalEmail(personalEmailId);
	    if (null != emp) {
		return new RestResponse(RestResponse.SUCCESS, SuccessHandler.EMPOYEE_EXISTS,
			SuccessHandler.EMPOYEE_EXISTS_REASON);
	    }
	    return new RestResponse(RestResponse.SUCCESS, SuccessHandler.EMPOYEE_NOT_EXISTS,
		    SuccessHandler.EMPOYEE_NOT_EXISTS_REASON);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    return new RestResponse(RestResponse.FAILED, ErrorHandler.EMPLOYEE_NOT_AVAILABLE,
		    ErrorHandler.EMPLOYEE_NOT_FOUND);
	}
    }

}
