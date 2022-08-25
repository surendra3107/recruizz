package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.EmailActivity;
import com.bbytes.recruiz.domain.SendingEmailList;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.SendingEmailListRepository;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.EmailActivityService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.ProspectActivityService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;

@RestController
public class EmailActivityController {

    private final Logger logger = LoggerFactory.getLogger(EmailActivityController.class);

    @Autowired
    private EmailActivityService emailActivityService;

    @Autowired
    private PageableService pageableService;
    
    @Autowired
    private ProspectActivityService prospectActivityService;

    @Autowired
    private UserService userService;

    @Autowired
    private TenantUsageStatService tenantUsageStatService;
    
    @Autowired
    SendingEmailListRepository sendingEmailListRepository;
    
    /**
     * Send email to candidate
     * 
     * @param emailActivity
     * @param file
     * @param fileName
     * @param roundCandidateId
     * @return
     * @throws RecruizException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/email/activity", method = RequestMethod.POST)
    public RestResponse getAllEmailTemplate(@RequestPart("json") @Valid EmailActivity emailActivity,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam(value = "fileName", required = false) String fileName,
	    @RequestParam(value = "roundCandidateId", required = false) String roundCandidateId,
	    @RequestParam(value = "templateType", required = false) String templateType,@RequestParam(value = "additionalAttachment", required = false) String additionalAttachment)
	    throws RecruizException, IOException {

	emailActivityService.saveEmailActivity(emailActivity, roundCandidateId, file, fileName, null, false,
		templateType, additionalAttachment);
	
	RestResponse emailActivityResponse = new RestResponse(RestResponse.SUCCESS, "Saved", null);
	return emailActivityResponse;
    }

    /**
     * Send bulk email to candidate
     * 
     * @author Akshay
     * @param emailActivity
     * @param file
     * @param fileName
     * @param roundCandidateId
     * @return
     * @throws RecruizException
     * @throws IOException
     */
    @RequestMapping(value = "/api/v1/bulk/email/activity", method = RequestMethod.POST)
    public RestResponse sendBulkEmailTemplate(@RequestPart("json") @Valid EmailActivity emailActivity,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam(value = "fileName", required = false) String fileName,
	    @RequestParam(value = "emailList", required = false) List<String> emailList,
	    @RequestParam(value = "templateType", required = false) String templateType)
	    throws RecruizException, IOException {
	

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.SendEmail.name());
*/
	emailActivityService.sendBulkEmailActivity(emailActivity, emailList, file, fileName, false, templateType);
	RestResponse emailActivityResponse = new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
	return emailActivityResponse;
    }
    
   
    
    @RequestMapping(value = "/api/v1/prospect/email/activity", method = RequestMethod.POST)
    public RestResponse sendProspectEmailTemplate(@RequestPart("json") @Valid EmailActivity emailActivity,
	    @RequestPart(value = "file", required = false) MultipartFile file,
	    @RequestParam(value = "fileName", required = false) String fileName,
	    @RequestParam(value = "emailList", required = false) List<String> emailList,
	    @RequestParam(value = "templateType", required = false) String templateType,
	    @RequestParam(value = "prospectId") String prospectId)
	    throws RecruizException, IOException {
	

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.SendEmail.name());
*/
	emailActivityService.sendBulkEmailActivity(emailActivity, emailList, file, fileName, false, templateType);
	
	prospectActivityService.addActivity(emailActivity.getBody(), userService.getLoggedInUserEmail(), prospectId+"", "Email");
	
	
	try{
		
		//add emails for auto search	
		
		Set<String> foo = new HashSet<String>();
		if(emailActivity.getCcEmails()!=null && emailActivity.getCcEmails().size()>0)
		foo.addAll(emailActivity.getCcEmails());
		
		if(emailActivity.getToEmails()!=null && emailActivity.getToEmails().size()>0)
		foo.addAll(emailActivity.getToEmails());
		
		if(emailActivity.getEmailTo()!=null && !emailActivity.getEmailTo().trim().equalsIgnoreCase(""))
			foo.add(emailActivity.getEmailTo());
		
		if(emailActivity.getEmailFrom()!=null && !emailActivity.getEmailFrom().trim().equalsIgnoreCase(""))
			foo.add(emailActivity.getEmailFrom());
		
		
		for (String email : foo) {
			
			SendingEmailList data = sendingEmailListRepository.findByEmail(email);
			
			if(data==null){
				
				SendingEmailList sendingEmail = new SendingEmailList();
				
				sendingEmail.setCreationDate(new Date());
				sendingEmail.setModificationDate(new Date());
				sendingEmail.setEmail_id(email);
				sendingEmail.setList_type("Prospect");
				sendingEmail.setStatus("Active");
				sendingEmailListRepository.save(sendingEmail);
			}
		}
		
	}catch(Exception e){
		logger.error("getting error while add emails for auto search "+e);
	}
	
	RestResponse emailActivityResponse = new RestResponse(RestResponse.SUCCESS, "Email sent successfully");
	return emailActivityResponse;
    }
    
    
    

    // to get all emails sent from recruiz
    @RequestMapping(value = "/api/v1/email/all", method = RequestMethod.GET)
    public RestResponse getAllEmails(@RequestParam(value = "pageNo", required = false) String pageNo,
	    @RequestParam(value = "sortField", required = false) String sortField,
	    @RequestParam(value = "sortOrder", required = false) String sortOrder) throws RecruizException {
	RestResponse response = null;
	

/*	// making entry to usage stat table
	tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
		userService.getLoggedInUserEmail(), userService.getLoggedInUserName(),
		UsageActionType.GetAllSentEmail.name());*/

	if (sortField == null || sortField.trim().isEmpty()) {
	    sortField = "id";
	}
	try {
	    Pageable pageable = pageableService.getPageRequestObject(pageNo,sortField,pageableService.getSortDirection(SortOrder.DESC.toString()));
	  Page<EmailActivity> emails =  emailActivityService.getAllEmailsSentBy(userService.getLoggedInUserEmail(), pageable);
	  for (EmailActivity emailActivity : emails) {
	      emailActivity.setToEmails(emailActivity.getEmailTo());
	      emailActivity.setCcEmails(emailActivity.getCc());
	      emailActivity.setAttachments(emailActivity.getAttachmentLink());
	}
	    response = new RestResponse(true, emails);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	    response = new RestResponse(false, ErrorHandler.FAILED_TO_GET_EMAIL, ErrorHandler.FAILED_GETTING_EMAIL);
	}

	return response;

    }

}
