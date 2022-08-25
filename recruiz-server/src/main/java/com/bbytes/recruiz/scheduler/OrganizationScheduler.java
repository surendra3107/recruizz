package com.bbytes.recruiz.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.auth.jwt.TokenDataHolder;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.database.TenantDBService;
import com.bbytes.recruiz.domain.KnowlarityCallDetails;
import com.bbytes.recruiz.domain.KnowlarityIntegration;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseCandidateProfileCache;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.integration.knowlarity.KnowlarityCallDetailService;
import com.bbytes.recruiz.integration.knowlarity.KnowlarityIntegrationService;
import com.bbytes.recruiz.integration.knowlarity.KnowlarityService;
import com.bbytes.recruiz.integration.sixth.sense.SixthSenseCandidateProfileCacheService;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IEmailService;
import com.bbytes.recruiz.service.ImportExportAsyncService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.RecruizPlutusClientService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Organization delete scheduler for deleting database and files completely
 * 
 * @author Akshay
 *
 */
@Component
public class OrganizationScheduler {

	private static Logger logger = LoggerFactory.getLogger(OrganizationScheduler.class);

	private final String accountDeleteTemplate = GlobalConstants.ACCOUNT_DELETION_TEMPLATE;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private SixthSenseCandidateProfileCacheService sixthSenseCandidateProfileCacheService;

	@Autowired
	private UserService userService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private RecruizPlutusClientService recruizPlutusClientService;

	@Autowired
	private FileService fileService;

	@Autowired
	private TenantDBService tenantDBService;

	@Autowired
	KnowlarityCallDetailService knowlarityCallDetailService;

	@Autowired
	KnowlarityIntegrationService knowlarityIntegrationService;
	
	@Autowired
	KnowlarityService knowlarityService;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Value("${candidate.folderPath.path}")
	private String baseFolderPath;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Value("${email.account.deletion.subject}")
	private String accountDeletionSubject;

	@Value("${export.folderPath.path}")
	private String exportDataRootPath;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private ImportExportAsyncService importExportAsyncService;

	/**
	 * This method is supposed to run every one hour and delete database and
	 * files/folders for a tenant
	 * 
	 * @author Akshay
	 * @throws RecruizException
	 */
	// @Scheduled(cron = "0 0 5 * * ?")
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void cleanUpMarkForDeleteData() throws RecruizException {

		List<String> tenantList = tenantResolverService.findAllTenants();
		for (String tenant : tenantList) {

			try {
				TenantContextHolder.setTenant(tenant);

				Organization organization = organizationService.findByOrgId(tenant);
				if (organization != null && organization.getMarkForDelete() && organization.getMarkForDeleteDate() != null) {

					// checking 2 days prior the date of mark for delete
					sendAccountDeleteEmail(tenant, organization);

					if (organization.getMarkForDeleteDate().before(DateTime.now().toDate())) {

						ResponseEntity<RestResponse> response = recruizPlutusClientService.removeOrganization(tenant);

						if (response != null && response.getBody().isSuccess()) {
							deleteOrgDBAndFiles(tenant, organization);
						}
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} 

		}

	}

	/**
	 * @param tenant
	 * @param organization
	 * @throws RecruizException
	 */
	private void sendAccountDeleteEmail(String tenant, Organization organization) throws RecruizException {

		Map<String, Object> emailBody = new HashMap<>();
		Set<String> emailListToSend = new HashSet<String>();

		if (DateTime.now().plusHours(2).toDate().after(organization.getMarkForDeleteDate())
				&& DateTime.now().toDate().before(organization.getMarkForDeleteDate())) {

			// getting hours count when data will be deleted
			long hours = (new DateTime(organization.getMarkForDeleteDate()).getMillis() - DateTime.now().getMillis()) / 3600000;
			List<User> userList = userService.findAll();

			if (hours > 0) {
				for (User user : userList) {
					// sending email who is active user
					if (user != null && userService.isActiveUser(user)) {

						// adding email addresses to list and sending complete
						// list
						// to send email method
						emailListToSend.add(user.getEmail());
						// adding recruiz and sales email address
						emailListToSend.add(GlobalConstants.RECRUIZ_EMAIL_ADDRESS);
						emailListToSend.add(GlobalConstants.SALES_EMAIL_ADDRESS);
					}
				}
				emailBody.put(GlobalConstants.HOURS, hours <= 1 ? hours + " hour" : hours + " hours");

				sendEmail(emailBody, new ArrayList<String>(emailListToSend));
			}
		}
	}

	/**
	 * This method is used to delete complete db for organization and files as
	 * well
	 * 
	 * @param tenant
	 * @param organization
	 */
	private void deleteOrgDBAndFiles(String tenant, Organization organization) {

		List<User> userList = userService.findAll();

		for (User user : userList) {
			// checking user signup with multiple organization,
			// return
			// count = 1 means single org signup
			if (tenantResolverService.getTenantsCount(user.getEmail()) == 1) {
				// deleting entry from user table -
				// tenant_management db
				tenantResolverService.deleteUserFromTenantResolver(user.getEmail());
			}

			// deleting entry from tenant resolver
			tenantResolverService.deleteTenantResolver(user.getEmail(), tenant);
		}

		// deleting files and folder for a tenant
		String tenantDocsFolderPath = baseFolderPath + File.separator + "files" + File.separator + tenant;
		fileService.deleteDirectory(tenantDocsFolderPath);

		// deleting organization logo for a tenant from pubset
		String pubSetFolderPath = publicFolder + File.separator + tenant;
		fileService.deleteDirectory(pubSetFolderPath);

		// deleting from s3
		try {
			s3DownloadClient.deleteS3Folder(fileService.getTenantBucket(), "files" + "/" + tenant + "/");
		} catch (Exception e) {
			logger.warn("######### Failed to delete folder from s3", e);
		}

		// dropping complete database
		tenantDBService.closeDataSource(tenant);
		tenantResolverService.deleteDB(tenant);

		logger.info("Tenant database deleted successfully");
	}

	private void sendEmail(Map<String, Object> emailBody, List<String> emailList) throws RecruizException {

		emailService.sendEmail(accountDeleteTemplate, emailList, accountDeletionSubject, emailBody);

		logger.info("Recruiz account delete alert successfully sent");
	}

	/**
	 * This cron expression supposed to run at 6 AM every day and delete all
	 * exported data files/folders older than 2 days
	 * 
	 * @author Akshay
	 */
	@Scheduled(cron = "0 0 6 * * ?")
	public void deleteExportFiles() {

		File exportDataFolder = new File(exportDataRootPath + File.separator);

		if (exportDataFolder.exists()) {

			File[] listFiles = exportDataFolder.listFiles();

			// checking files older than 3 days
			long eligibleForDeletion = DateTime.now().getMillis() - (2 * 24 * 60 * 60 * 1000);

			// iterating all files
			for (File listFile : listFiles) {

				try {
					if (listFile.lastModified() < eligibleForDeletion) {
						if (listFile.isDirectory())
							FileUtils.deleteDirectory(listFile);
						else if (!listFile.delete()) {
							logger.error("Export data - Unable to delete file - " + listFile.getName());
						}
					}
				} catch (IOException ex) {
					logger.error(ex.getMessage());
				}
			}
		}
	}


	@Scheduled(cron = "0 30 16 * * ?")
	public void createExcelFilesOfAllStagesAndStatus() {
		List<String> tenantList = tenantResolverService.findAllTenants();

		for (String tenant : tenantList) {

			try {
				TenantContextHolder.setTenant(tenant);

				Organization organization = organizationService.findByOrgId(tenant);
				if (organization != null && !organization.getMarkForDelete() && !organization.getDisableStatus()) {

					String orgMailId = null;
					List<User> user = userService.findAll();
					for (User user2 : user) {
						orgMailId = user2.getEmail();
						if(orgMailId!=null)
							break;
					}

					TokenDataHolder tokenDataHolder = tokenAuthenticationProvider
							.getAuthTokenHolderForUser(orgMailId, tenant);
					MultiTenantAuthenticationToken auth = new MultiTenantAuthenticationToken(tenant,
							tokenDataHolder.getUser(), tokenDataHolder.getWebMode(),
							tokenDataHolder.getWebRequestMode(), tokenDataHolder.getUserTimeZone(),
							tokenDataHolder.getUserLocale());

					importExportAsyncService.createExcelReportAsync(tenant, auth);

				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				TenantContextHolder.clearContext();
			}

		}

	}



	/**
	 * This cron expression supposed to run at 4 AM every day and delete all
	 * cache sixth sense profile data older than 1 days
	 * 
	 * @author Akshay
	 */
	@Scheduled(cron = "0 0 23 * * ?")
	public void cleanSixthSenseCache() {

		logger.error("Call Clear cache from mongodb   Date = "+new Date());
		List<SixthSenseCandidateProfileCache> cacheData = sixthSenseCandidateProfileCacheService.findAll();
		int x=0;
		if (cacheData != null && !cacheData.isEmpty()) {
			for (SixthSenseCandidateProfileCache cache : cacheData) {
				// checking data older than 1 day
				long eligibleForDeletion = DateTime.now().getMillis() - (1 * 24 * 60 * 60 * 1000);
				if (cache.getCreationDate().getTime() < eligibleForDeletion) {
					sixthSenseCandidateProfileCacheService.delete(cache);
					x = x+1;
				}
			}
		}
		List<SixthSenseCandidateProfileCache> cacheDataLeft = sixthSenseCandidateProfileCacheService.findAll();
		logger.error("Clear cache from mongodb :=   total cache data list ="+cacheData.size()+"  Total deleled cache = "+x+"  left cache in mongodb =  "+cacheDataLeft.size()+ "  Date = "+new Date());

	}



	
	@Scheduled(cron = "0 0/5 * * * ?") // every 5 mins
	public void updateKnowlarityCallLogsBycall_id() {
	//	System.out.println(" =========== updateKnowlarityCallLogsBycall_id  =   schdular = "+new Date());
		List<String> tenantList = tenantResolverService.findAllTenants();
		for (String tenant : tenantList) {

			try {
				TenantContextHolder.setTenant(tenant);

				Organization organization = organizationService.findByOrgId(tenant);
				if (organization != null && !organization.getMarkForDelete() && !organization.getDisableStatus()) {

					KnowlarityIntegration knowlarityIntegration = knowlarityIntegrationService.findByOrgName(organization.getOrgId());

					if(knowlarityIntegration!=null){
						List<KnowlarityCallDetails> listCallDetails = knowlarityCallDetailService.findByPendingCalllogstatus();

						for (KnowlarityCallDetails knowlarityCallDetails : listCallDetails) {

							if(knowlarityCallDetails.getCall_id()!=null){

								try{
									knowlarityService.saveKnowlarityCallLogs(knowlarityCallDetails,knowlarityIntegration);
								}catch(Exception e){
									logger.error(e.getMessage(),e);
								}
								
							}
						}

					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				TenantContextHolder.clearContext();
			}

		}

	}





}
