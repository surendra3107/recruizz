package com.bbytes.recruiz.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.auth.jwt.TokenDataHolder;
import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.domain.ImportJobBatch;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.service.BulkUploadResumeService;
import com.bbytes.recruiz.service.CandidateResumeBulkUploadBatchService;
import com.bbytes.recruiz.service.CandidateResumeUploadItemService;
import com.bbytes.recruiz.service.EmailedResumeUploadService;
import com.bbytes.recruiz.service.ImportExportAsyncService;
import com.bbytes.recruiz.service.ImportJobBatchService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * Resume bulk upload scheduler which will look for pending resume in db and
 * process it in the night
 * 
 * @author thanneer
 *
 */
@Component
public class ResumeBulkUploadAndImportJobScheduler {

	private static Logger logger = LoggerFactory.getLogger(ResumeBulkUploadAndImportJobScheduler.class);

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private CandidateResumeBulkUploadBatchService candidateResumeBulkUploadBatchService;

	@Autowired
	private CandidateResumeUploadItemService candidateResumeUploadItemService;

	@Autowired
	private BulkUploadResumeService bulkUploadResumeService;

	@Autowired
	private SchedulerTaskTenantState resumeBulkTenantState;

	@Autowired
	private ImportJobBatchService importJobBatchService;

	@Autowired
	private UserService userService;

	@Autowired
	private ImportExportAsyncService importExportAsyncService;

	@Autowired
	private EmailedResumeUploadService emailedResumeUploadService;

	@Scheduled(fixedDelay = 60000)
	// @Scheduled(cron = "0 0/1 * 1/1 * ?") // every 1 min
	public void processPendingResumeFromBulkUpload() {

		long hours = ((new Date()).getTime() / (1000 * 60 * 60)) % 24;

		if(hours>=14 || hours<=2){

			List<String> tenantList = tenantResolverService.findAllTenants();
			for (String tenant : tenantList) {

				try {
					TenantContextHolder.setTenant(tenant);

					Organization organization = organizationService.findByOrgId(tenant);
					if (organization != null && !organization.getMarkForDelete() && !organization.getDisableStatus()) {
						List<String> itemStatusList = new ArrayList<String>();
						itemStatusList.add(ResumeUploadFileStatus.PENDING.toString());
						itemStatusList.add(ResumeUploadFileStatus.PROCESSING.toString());

						Long pendingFileCount = candidateResumeUploadItemService.countByStatusIn(itemStatusList);

						if (pendingFileCount == 0)
							continue;

						if (resumeBulkTenantState.isTenantBulkResumeUploadTaskRunning(tenant)) {
							logger.info("#################  Currently this tenant bulk upload is busy  = " + tenant
									+ " ###############");
							continue;
						}

						// pick only one batch which is in progress or uploading

						List<String> statues = new ArrayList<String>();
						statues.add(ResumeBulkBatchUploadStatus.UPLOADING.toString());
						statues.add(ResumeBulkBatchUploadStatus.IN_PROGRESS.toString());
						statues.add(ResumeBulkBatchUploadStatus.SCHEDULED.toString());

						List<CandidateResumeBulkUploadBatch> candidateResumeBulkUploadBatches = candidateResumeBulkUploadBatchService
								.findTop10ByStatusIn(statues);

						// the tenant don't have any item for bulk upload it will be
						// null so continuing for next iteration
						if (candidateResumeBulkUploadBatches == null || candidateResumeBulkUploadBatches.isEmpty()) {
							continue;
						}

						logger.info("********************* GET BULK UPLOAD TOP 10 BATCH FOR TENANT " + tenant
								+ " and batch size " + candidateResumeBulkUploadBatches.size()
								+ "  *******************************");

						for (CandidateResumeBulkUploadBatch candidateResumeBulkUploadBatch : candidateResumeBulkUploadBatches) {

							Long count = candidateResumeUploadItemService
									.countByBatchIdAndStatusIn(candidateResumeBulkUploadBatch.getBatchId(), itemStatusList);

							if (count == null || count == 0) {
								candidateResumeBulkUploadBatch.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.toString());
								candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);
							} else {
								logger.info(
										"********************* GOING TO START THE BULK UPLOAD BACKGROUND JOB FOR TENANT "
												+ tenant + " and batch  " + candidateResumeBulkUploadBatch.getBatchId()
												+ "  *******************************");
								TokenDataHolder tokenDataHolder = tokenAuthenticationProvider
										.getAuthTokenHolderForUser(candidateResumeBulkUploadBatch.getOwner(), tenant);
								MultiTenantAuthenticationToken auth = new MultiTenantAuthenticationToken(tenant,
										tokenDataHolder.getUser(), tokenDataHolder.getWebMode(),
										tokenDataHolder.getWebRequestMode(), tokenDataHolder.getUserTimeZone(),
										tokenDataHolder.getUserLocale());

								List<CandidateResumeUploadItem> candidateResumeUploadItems = candidateResumeUploadItemService
										.findTop1000ByBatchIdAndStatusInOrderByStatusDesc(
												candidateResumeBulkUploadBatch.getBatchId(), itemStatusList);

								String batchId = candidateResumeBulkUploadBatch.getBatchId();

								candidateResumeBulkUploadBatch
								.setStatus(ResumeBulkBatchUploadStatus.IN_PROGRESS.toString());
								candidateResumeBulkUploadBatchService.save(candidateResumeBulkUploadBatch);

								bulkUploadResumeService.resumeBulkUploadAsync(tenant, auth, candidateResumeUploadItems,batchId);
								break;
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

	// every 1 hour
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void processPendingRowsFromImportUpload() {

		long hours = ((new Date()).getTime() / (1000 * 60 * 60)) % 24;

		if(hours>=14 || hours<=2){

			List<String> tenantList = tenantResolverService.findAllTenants();

			for (String tenant : tenantList) {

				try {
					if (resumeBulkTenantState.isTenantImportJobUploadTaskRunning(tenant)) {
						logger.info("#################  Currently this tenant import job upload is busy  = " + tenant
								+ " ###############");
						continue;
					}

					TenantContextHolder.setTenant(tenant);

					Organization organization = organizationService.findByOrgId(tenant);
					if (organization != null && !organization.getMarkForDelete() && !organization.getDisableStatus()) {

						List<ImportJobBatch> importJobBatches = importJobBatchService
								.findByStatus(ResumeUploadFileStatus.PROCESSING.toString());

						// the tenant don't have any item for bulk upload it
						// will be
						// null so continuing for next iteration
						if (importJobBatches == null || importJobBatches.isEmpty()) {
							continue;
						}

						for (ImportJobBatch importJobBatch : importJobBatches) {

							if (importJobBatch != null) {

								logger.info(
										"********************* GOING TO START THE IMPORT JOB BATCH UPLOAD BACKGROUND JOB FOR TENANT "
												+ tenant + " and batch  " + importJobBatch.getBatchId()
												+ "  *******************************");

								User user = userService.getUserByEmail(importJobBatch.getOwner());

								TokenDataHolder tokenDataHolder = tokenAuthenticationProvider
										.getAuthTokenHolderForUser(user.getEmail(), tenant);

								MultiTenantAuthenticationToken auth = new MultiTenantAuthenticationToken(tenant,
										tokenDataHolder.getUser(), tokenDataHolder.getWebMode(),
										tokenDataHolder.getWebRequestMode(), tokenDataHolder.getUserTimeZone(),
										tokenDataHolder.getUserLocale());

								Long count = importJobBatch.getPendingRowCount();

								if (count == null || count == 0) {
									importJobBatch.setStatus(ResumeBulkBatchUploadStatus.COMPLETED.toString());
									importJobBatchService.save(importJobBatch);
								} else {
									if (user != null) {
										File importFile = new File(importJobBatch.getFilePath());
										if (importFile != null && importFile.exists()) {
											importExportAsyncService.startImportDataAsync(tenant, auth, user, importFile,
													StringUtils.convertStringtoMap(importJobBatch.getHeaderMap()),
													importJobBatch.getImportType(), importJobBatch.getBatchId());
										}
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

	@Scheduled(fixedDelay = 60000)
	// @Scheduled(cron = "0 0/2 * * * ?")
	public void fetchFromEmailAndAddToCandidateDB() {
		List<String> tenantList = tenantResolverService.findAllTenants();
		emailedResumeUploadService.processTenantListToAddEmailedFiles(tenantList);
	}


	@Scheduled(fixedDelay = 60000)
	// @Scheduled(cron = "0 0/2 * * * ?")
	public void processFilesFetchedFromEmail() {
		List<String> tenantList = tenantResolverService.findAllTenants();
		emailedResumeUploadService.processEmailFiles(tenantList);
	}

}
