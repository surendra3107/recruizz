package com.bbytes.recruiz.scheduler;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.PlutusClientException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.AgencyInvoiceService;
import com.bbytes.recruiz.service.CandidateFileService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.InterviewScheduleService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.QueryService;
import com.bbytes.recruiz.service.RecruizConnectService;
import com.bbytes.recruiz.service.RecruizPlutusClientService;
import com.bbytes.recruiz.service.RoundCandidateService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.service.TaskItemService;
import com.bbytes.recruiz.service.TaskScheduleService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
@EnableScheduling
public class GlobalSchedularService {

	private static final Logger logger = LoggerFactory.getLogger(GlobalSchedularService.class);

	@Autowired
	private QueryService queryService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private InterviewScheduleService interviewScheduleService;

	@Autowired
	private RecruizPlutusClientService recruizPlutusClientService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private FileService fileService;

	@Autowired
	private RecruizConnectService recruizConnectService;

	@Autowired
	private CandidateFileService candidateFileService;

	@Value("${org.signup.notify.email.list}")
	private String notificationEmailIds;

	@Value("${base.url}")
	private String recruizEmailId;

	@Value("${production.summary.report.enabled}")
	private String isProductionSummaryEnabled;

	@Autowired
	private AgencyInvoiceService agencyInvoiceService;

	@Autowired
	private SchedulerTaskTenantState schedulerTaskTenantState;

	@Autowired
	private PositionService positionService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private TaskItemService taskItemService;

	@Autowired
	private TaskScheduleService taskService;

	@Scheduled(cron = "0 0 10 * * *")
	public void productionSummaryReport() throws RecruizException {
		if (isProductionSummaryEnabled.equalsIgnoreCase("yes")) {
			List<String> allTenant = tenantResolverService.findAllTenants();
			if (allTenant != null && !allTenant.isEmpty()) {
				String countString = "";
				for (String tenant : allTenant) {
					try {
						TenantContextHolder.setTenant(tenant);
						countString = countString + "\n\n" + getEntityCount();
					} finally {
						TenantContextHolder.clearContext();
					}
				}
				List<String> emailList = StringUtils.stringToArray(notificationEmailIds);

				if (emailList != null && !emailList.isEmpty()) {
					emailService.sendEmail(emailList, countString, "Production Server Summary", false);
				}
			}
		}
	}

	// @Scheduled(cron = "0 0/5 * * * ?") //every 5 mins
	@Scheduled(cron = "0 0 11 * * *")
	public void userLoggedOnSummaryReport() throws RecruizException {
		if (isProductionSummaryEnabled.equalsIgnoreCase("yes")) {
			List<String> allTenants = tenantResolverService.findAllTenants();
			Map<String, String> orgIdToName = tenantResolverService.getOrgIdToOrgNameMap(allTenants);
			if (orgIdToName != null && !orgIdToName.isEmpty()) {
				String summaryResult = "";
				for (String tenant : orgIdToName.keySet()) {
					try {
						TenantContextHolder.setTenant(tenant);

						summaryResult = summaryResult + "\n\n"
								+ "-----------------------------------------------------------" + "\n";
						summaryResult = summaryResult + "\n\n" + "Current Organization : " + orgIdToName.get(tenant);
						for (String email : getEmailList()) {
							User user = tenantResolverService.findUserByEmail(email);
							if (user != null) {
								summaryResult = summaryResult + "\n\n" + getUserLoggedOnStats(user);
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						TenantContextHolder.clearContext();
					}

				}

				List<String> emailList = StringUtils.stringToArray(notificationEmailIds);

				if (emailList != null && !emailList.isEmpty()) {
					emailService.sendEmail(emailList, summaryResult, "Recruiz User login summary", false);
				}
			}
		}
	}

	private String getUserLoggedOnStats(User user) {
		if (user.getLoggedOn() == null) {
			return user.getName() + ", " + user.getEmail() + ", " + user.getUserType() + ", Never logged in";
		} else {
			DateTimeZone indianTimeZone = DateTimeZone.forID("Asia/Kolkata");
			return user.getName() + ", " + user.getEmail() + ", " + user.getUserType() + ", "
					+ new DateTime(user.getLoggedOn()).withZone(indianTimeZone);
		}
	}

	public String getEntityCount() {
		return queryService.getOrgStatsCount();
	}

	public List<String> getEmailList() {
		return queryService.getOrgUserEmailList();
	}

	/**
	 * to send interviewer invite in next 15 mins
	 * 
	 * @throws RecruizException
	 */
	@Async
	@Scheduled(cron = "0 0/15 * * * ?") // every 15 mins
	public void sendEmailReminderForInterviewSchedules() throws RecruizException {
		List<String> allTenant = tenantResolverService.findAllTenants();
		if (allTenant != null && !allTenant.isEmpty()) {
			for (String tenant : allTenant) {
				try {
					TenantContextHolder.setTenant(tenant);
					interviewScheduleService.sendInterviewScheduleReminder();
				} catch (ParseException | MessagingException | IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}

			}
		}
	}

	@Async
	@Scheduled(cron = "0 0/59 * * * ?") // every 59 mins
	public void sendEmailReminderForTasks() throws RecruizException {
		List<String> allTenant = tenantResolverService.findAllTenants();
		if (allTenant != null && !allTenant.isEmpty()) {
			for (String tenant : allTenant) {
				try {
					TenantContextHolder.setTenant(tenant);
					taskService.sendTaskReminder();
				} catch (ParseException | MessagingException | IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}

			}
		}
	}

	/**
	 * To send stat data to plutus
	 */
	// @Scheduled(cron = "0 0/5 * * * ?")
	@Scheduled(cron = "0 0 6 * * *")
	public void sendStatToPlutus() {

		Map<String, Object> orgStatMap = new HashMap<>();

		try {
			List<String> allTenant = tenantResolverService.findAllTenants();
			if (allTenant != null && !allTenant.isEmpty()) {
				for (String tenant : allTenant) {
					try {
						TenantContextHolder.setTenant(tenant);
						Map<String, Object> plutusStatMap = queryService.getOrgStatForPlutus();
						orgStatMap.put(TenantContextHolder.getTenant(), plutusStatMap);
						logger.debug("Stat count org " + TenantContextHolder.getTenant(), plutusStatMap.toString());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						TenantContextHolder.clearContext();
					}

				}
			}
			// send this map to plutus server below
			recruizPlutusClientService.pushStatToPlutus(orgStatMap);
		} catch (Exception ex) {
			logger.error("Failed to push data to plutus server", ex);
		}
	}

	/**
	 * to sync organization which is not registered on plutus
	 */
	@Scheduled(cron = "0 0 5 * * *")
	public void syncOrgWithPlutus() {
		try {
			List<String> allTenant = tenantResolverService.findAllTenants();
			if (allTenant != null && !allTenant.isEmpty()) {
				for (String tenant : allTenant) {
					try {
						PlutusOrganizationInfo plutusOrginfo = tenantResolverService.getPlutusOrgInfo(tenant);
						if (plutusOrginfo == null || plutusOrginfo.getSubscriptionId() == null
								|| plutusOrginfo.getSubscriptionId().isEmpty() || plutusOrginfo.getOrgType() == null
								|| plutusOrginfo.getOrgType().isEmpty()) {
							// sync with plutus here
							TenantContextHolder.setTenant(tenant);
							syncOrganizationRegistrationWithPlutus();
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					} finally {
						TenantContextHolder.clearContext();
					}

				}
			}
		} catch (Exception ex) {
			logger.error("Failed to push data to plutus server", ex);
		}

	}

	/**
	 * to sync with server if registration is failed
	 * 
	 * @throws PlutusClientException
	 */
	private void syncOrganizationRegistrationWithPlutus() throws PlutusClientException {
		Organization org = null;
		try {
			org = organizationService.getCurrentOrganization();
			String superAdminEmailID = "", mobileNumber = "";
			List<User> users = userService.getAllByRoleName(GlobalConstants.SUPER_ADMIN_USER_ROLE);
			if (users != null && !users.isEmpty()) {
				superAdminEmailID = users.get(0).getEmail();
				mobileNumber = users.get(0).getMobile();
			}
			if (org != null) {
				org.setOrganizationEmail(superAdminEmailID);
				org.setRegisteredMobile(mobileNumber);
				recruizPlutusClientService.registerOnPlutus(org, null);
			}
		} catch (Exception ex) {
			logger.error("Failed during org registration sync for org " + org, ex);
		}
	}

//	@Async
//	@Scheduled(cron = "0 0/50 * * * ?") // every 50 mins
	public void uploadFilesToS3Server() throws RecruizException {

		List<String> allTenant = tenantResolverService.findAllTenants();
		if (allTenant != null && !allTenant.isEmpty()) {
			for (String tenant : allTenant) {

				if (schedulerTaskTenantState.isTenantS3UploadTaskRunning(tenant)) {
					continue;
				}

				try {
					TenantContextHolder.setTenant(tenant);
					schedulerTaskTenantState.setS3UploadTaskRunningNow(tenant);
					// candidateFileService.uploadCandidateFilesToS3(tenant);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
					schedulerTaskTenantState.setS3UploadTaskDone(tenant);
				}
			}
		}
	}
	
	// to delete local files uploaded on s3
//	@Async
//	@Scheduled(cron = "0 0 */3 ? * *") // every 3 hrs
	public void deleteUploadedFilesFromLocalServer() throws RecruizException {

		List<String> allTenant = tenantResolverService.findAllTenants();
		if (allTenant != null && !allTenant.isEmpty()) {
			for (String tenant : allTenant) {
				if (schedulerTaskTenantState.isTenantDeleteLocalFilesTaskRunning(tenant)) {
					continue;
				}
				try {
					TenantContextHolder.setTenant(tenant);
					schedulerTaskTenantState.setDeleteLocalFilesTaskRunningNow(tenant);
					// candidateFileService.deleteLocalFiles();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
					schedulerTaskTenantState.setDeleteLocalFilesTaskDone(tenant);
				}
			}
		}
	}

	// @Scheduled(cron = "0 0/1 * 1/1 * *")
	@Scheduled(cron = "0 27 4 * * MON,THU") // it will fire at 4:27 AM of every
	// MON & THU
	public void invoiceStatusOfAgency() throws RecruizException {
		agencyInvoiceService.sendStatusOfAgencyInvoice();
	}

	// below code is written to check if a file exists on s3. this will run for
	// each tenant

	/*
	 * 
	 * // to delete local files uploaded on s3
	 * 
	 * @Async
	 * 
	 * @Scheduled(cron = "0 0/2 * * * ?") // every day 1 O'clock mins public
	 * void tempSchedulerToVerifyS3ExistingFile() throws RecruizException {
	 * 
	 * List<String> allTenant = tenantResolverService.findAllTenants(); if
	 * (allTenant != null && !allTenant.isEmpty()) { for (String tenant :
	 * allTenant) { try { if
	 * (schedulerTaskTenantState.isTenantDeleteLocalFilesTaskRunning(tenant)) {
	 * continue; } TenantContextHolder.setTenant(tenant);
	 * schedulerTaskTenantState.setDeleteLocalFilesTaskRunningNow(tenant); try {
	 * candidateFileService.fileExistsOnServer(); } catch (Exception e) {
	 * logger.error(e.getMessage(), e); } finally {
	 * TenantContextHolder.clearContext(); logger.info(
	 * "***************Done for tenat*************" + tenant + "*********"); //
	 * schedulerTaskTenantState.setDeleteLocalFilesTaskDone(tenant); } } catch
	 * (Exception ex) { logger.warn(ex.getMessage(), ex); } } logger. info(
	 * "##################***************Done for All tenat*************##############"
	 * ); } }
	 */

/*	//Removing this method from the scheduler during connection leak fix - Sajin
	// @Scheduled(cron = "0 0/2 * * * ?")
	@Scheduled(cron = "0 0 5 * * *")
	public void reminderForNearByPositionCLosing() throws RecruizException {

		List<String> allTenants = tenantResolverService.findAllTenants();
		Map<String, String> orgIdToName = tenantResolverService.getOrgIdToOrgNameMap(allTenants);
		if (orgIdToName != null && !orgIdToName.isEmpty()) {
			for (String tenant : orgIdToName.keySet()) {
				try {
					TenantContextHolder.setTenant(tenant);
					positionService.sendNearClosureReminder();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}

			}
		}

	}
*/
	// send reminder to hr for inactive candidate in a pipeline

	// @Scheduled(cron = "0 0/2 * * * ?")
	//@Scheduled(cron = "0 0 6 * * *")
	public void reminderForInactiveCandidate() throws RecruizException {

		List<String> allTenants = tenantResolverService.findAllTenants();
		Map<String, String> orgIdToName = tenantResolverService.getOrgIdToOrgNameMap(allTenants);
		if (orgIdToName != null && !orgIdToName.isEmpty()) {
			for (String tenant : orgIdToName.keySet()) {
				try {
					TenantContextHolder.setTenant(tenant);
					roundCandidateService.sendInactiveCandidateReminder();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}
			}
		}

	}

	/**
	 * to send task reminder in next 30 mins
	 * 
	 * @throws RecruizException
	 */
	// @Async
	// @Scheduled(cron = "0 0/30 * * * ?") // every 30 mins
	public void sendTaskScheduleReminderAsEmail() throws RecruizException {
		List<String> allTenant = tenantResolverService.findAllTenants();
		if (allTenant != null && !allTenant.isEmpty()) {
			for (String tenant : allTenant) {
				TenantContextHolder.setTenant(tenant);
				try {
					taskItemService.sendTaskReminderAsEmail();
				} catch (ParseException | MessagingException | IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}
			}
		}
	}

//	@Scheduled(cron = "0 0/2 * * * ?")
//	@Scheduled(cron = "0 0 7 * * *")
	public void sendDailyUsageStatForHybridOrEnterpriseApp() throws RecruizException {

	    if(SpringProfileService.runningHybridMode()) {
		List<String> allTenants = tenantResolverService.findAllTenants();
		Map<String, String> orgIdToName = tenantResolverService.getOrgIdToOrgNameMap(allTenants);
		if (orgIdToName != null && !orgIdToName.isEmpty()) {
			for (String tenant : orgIdToName.keySet()) {
				try {
					TenantContextHolder.setTenant(tenant);
					organizationService.sendHybridUsageStat();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}
			}
		}
	    }
		

	}
	
	
	
	@Scheduled(cron = "0 0 23 * * ?")
	public void deleteLocalFilesDuringAWS() {

		logger.error("Call  deleteLocalFilesDuringAWS =========================================");
			List<String> tenantList = tenantResolverService.findAllTenants();

			for (String tenant : tenantList) {

				try {
					TenantContextHolder.setTenant(tenant);

					Organization organization = organizationService.findByOrgId(tenant);
					if (organization != null && !organization.getMarkForDelete() && !organization.getDisableStatus()) {
				
						fileService.deleteLocalFileDuringAWS();
						
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					TenantContextHolder.clearContext();
				}
			}

		
	}
	
	
	
}
