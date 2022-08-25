package com.bbytes.recruiz.web.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.domain.CandidateResumeBulkUploadBatch;
import com.bbytes.recruiz.domain.CandidateResumeUploadItem;
import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizPermissionDeniedException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.CandidateResumeBulkUploadBatchService;
import com.bbytes.recruiz.service.CandidateResumeUploadItemService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.QueryService;
import com.bbytes.recruiz.service.TaskItemService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

@RestController
public class DashboardController {

	@Autowired
	private QueryService queryService;

	@Autowired
	private CheckUserPermissionService permissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private CandidateResumeBulkUploadBatchService batchUploadService;

	@Autowired
	private CandidateResumeUploadItemService resumeUploadItemService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private TaskItemService taskService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;

	@Autowired
	private CheckUserPermissionService checkUserPermission;

	/**
	 *
	 * @return
	 * @throws RecruizException
	 * @throws ParseException
	 */
	@RequestMapping(value = "/api/v1/dashboard", method = RequestMethod.GET)
	public RestResponse getActiveEntityCountForHrExecutive() throws RecruizException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetHRDashBoard.name());

		if (!permissionService.isUserTypeApp())
			throw new RecruizPermissionDeniedException(GlobalConstants.INVALID_USER);

		if (permissionService.hasNormalRole()) {
			throw new RecruizWarnException(ErrorHandler.NOT_ALLOWED, ErrorHandler.INSUFFICIENT_PRIVILEGE);
		}

		Map<String, Object> dashboardResponse = new HashMap<String, Object>();
		if (permissionService.isSuperAdmin()) {
			dashboardResponse = queryService.getDashboardDataForSuperAdmin();

			Map<String, Object> entityCount = new HashMap<String, Object>();
			entityCount.put("positionCount", positionService.count());
			entityCount.put("totalOpenedPosition", positionService.getTotalPositionCount());
			entityCount.put("clientCount", clientService.count());
			entityCount.put("candidateCount", candidateService.count());
			entityCount.put("taskCount", taskService.findTaskItemCountForUser(userService.getLoggedInUserObject()));

			dashboardResponse.put("entityCounts", entityCount);
		} else if (permissionService.belongsToHrExecGroup(userService.getLoggedInUserObject().getUserRole())) {
			dashboardResponse = queryService.getDashboardDataForHRExec();
			dashboardResponse.put("positionCount", positionService.getPositionCountForLoggedInUser());
			dashboardResponse.put("totalOpenedPosition", positionService.getTotalPositionCountForLoggedInUser());
			dashboardResponse.put("clientCount", clientService.getClientCountForLoggedInUser());
			dashboardResponse.put("taskCount", taskService.findTaskItemCountForUser(userService.getLoggedInUserObject()));
			
			if (checkUserPermission.hasViewAllCandidatesPermission())
				dashboardResponse.put("candidateCount", candidateService.count());
			else
				dashboardResponse.put("candidateCount", candidateService.getCandidateCountForLoggedInUser());

		} else if (permissionService.belongsToHrManagerGroup(userService.getLoggedInUserObject().getUserRole())) {
			dashboardResponse = queryService.getDashboardDataForHRM();
			Map<String, Object> entityCount = new HashMap<String, Object>();
			entityCount = queryService.getDashboardData();
			dashboardResponse.put("entityCounts", entityCount);
		} else {
			return null;
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, dashboardResponse, null);
		return response;
	}

	@RequestMapping(value = "/api/v1/bulk/upload/stat", method = RequestMethod.GET)
	public RestResponse getBulkUploadStat() throws RecruizException, ParseException {

/*		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(), userService.getLoggedInUserEmail(),
				userService.getLoggedInUserName(), UsageActionType.GetBulkUploadStat.name());
*/
		List<Map<String, Object>> allBatches = new LinkedList<>();
		List<CandidateResumeBulkUploadBatch> uploadBatchesForLoggedInUser = batchUploadService
				.findTop100ByOwner(userService.getLoggedInUserEmail());
		if (uploadBatchesForLoggedInUser != null && !uploadBatchesForLoggedInUser.isEmpty()) {
			for (CandidateResumeBulkUploadBatch batch : uploadBatchesForLoggedInUser) {
				Map<String, Object> batchDetails = new LinkedHashMap<>();
				batchDetails.put("BatchID", batch.getBatchId());
				batchDetails.put("UploadedDate", batch.getUploadDateTime());
				try {
					batchDetails.put("Status", ResumeBulkBatchUploadStatus.valueOf(batch.getStatus()).getDisplayName());
				} catch (Exception ex) {
					// TODO
				}

				Long successCount = resumeUploadItemService.countByBatchIdAndStatus(batch.getBatchId(),
						ResumeUploadFileStatus.SUCCESS.name());

				Long inprogressCount = resumeUploadItemService.countByBatchIdAndStatus(batch.getBatchId(),
						ResumeUploadFileStatus.PROCESSING.name());

				Long failedCount = resumeUploadItemService.countByBatchIdAndStatus(batch.getBatchId(),
						ResumeUploadFileStatus.FAILED.name());

				List<CandidateResumeUploadItem> failedItems = resumeUploadItemService.findByBatchIdAndStatus(batch.getBatchId(),
						ResumeUploadFileStatus.FAILED.name());

				Long pendingCount = resumeUploadItemService.countByBatchIdAndStatus(batch.getBatchId(),
						ResumeUploadFileStatus.PENDING.name());

				batchDetails.put("SuccessCount", successCount);
				batchDetails.put("FailedCount", failedCount);
				batchDetails.put("InProgressCount", inprogressCount);
				batchDetails.put("PendingCount", pendingCount);
				batchDetails.put("totalCount", pendingCount + successCount + failedCount + inprogressCount);
				batchDetails.put("failedItems", failedItems);

				allBatches.add(batchDetails);
			}
		}

		if (allBatches != null && !allBatches.isEmpty()) {

		}

		RestResponse response = new RestResponse(RestResponse.SUCCESS, allBatches, null);
		return response;
	}
}
