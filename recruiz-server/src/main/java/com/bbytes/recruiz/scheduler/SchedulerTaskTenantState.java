package com.bbytes.recruiz.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SchedulerTaskTenantState {

	private volatile Map<String, Boolean> tenantIdToResumeBulkTaskRunning = new HashMap<>();

	private volatile Map<String, Boolean> tenantIdToImportTaskRunning = new HashMap<>();

	private volatile Map<String, Boolean> tenantIdToExportTaskRunning = new HashMap<>();

	private volatile Map<String, Boolean> tenantIdToS3UploadTaskRunning = new HashMap<>();

	private volatile Map<String, Boolean> tenantIdToDeleteLocalFilesTaskRunning = new HashMap<>();

	private volatile Map<String, String> tenantIdToCheckExisitingFilesTaskRunning = new HashMap<>();
	
	private volatile Map<String, Boolean> tenantIdToCheckEmailFecthTaskRunning = new HashMap<>();

	private volatile Map<String, Boolean> tenantIdToCheckEmailFileProcessTaskRunning = new HashMap<>();

	public boolean isTenantIdToCheckEmailFileProcessingTaskRunning(String tenantId) {
		Boolean running = tenantIdToCheckEmailFileProcessTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}

	public boolean isTenantBulkResumeUploadTaskRunning(String tenantId) {
		Boolean running = tenantIdToResumeBulkTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}
	
	public boolean isTenantIdToCheckEmailFecthTaskRunning(String tenantId) {
		Boolean running = tenantIdToCheckEmailFecthTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}
	
	public void setTenantIdToCheckEmailFileProcessTaskRunning(String tenantId) {
	    tenantIdToCheckEmailFileProcessTaskRunning.put(tenantId, Boolean.TRUE);
	}
	
	public void setTenantIdToCheckEmailFecthTaskRunning(String tenantId) {
	    tenantIdToCheckEmailFecthTaskRunning.put(tenantId, Boolean.TRUE);
	}
	
	public void setTenantIdToCheckEmailFileProcessTaskDone(String tenantId) {
	    tenantIdToCheckEmailFileProcessTaskRunning.put(tenantId, Boolean.FALSE);
	}
	
	public void setTenantIdToCheckEmailFecthTaskDone(String tenantId) {
	    tenantIdToCheckEmailFecthTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public void setResumeBulkTaskRunningNow(String tenantId) {
		tenantIdToResumeBulkTaskRunning.put(tenantId, Boolean.TRUE);
	}

	public void setResumeBulkTaskDone(String tenantId) {
		tenantIdToResumeBulkTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public boolean isTenantImportJobUploadTaskRunning(String tenantId) {
		Boolean running = tenantIdToImportTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}

	public void setImportJobTaskRunningNow(String tenantId) {
		tenantIdToImportTaskRunning.put(tenantId, Boolean.TRUE);
	}

	public void setImportJobTaskDone(String tenantId) {
		tenantIdToImportTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public boolean isTenantExportJobTaskRunning(String tenantId) {
		Boolean running = tenantIdToExportTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}

	public void setExportJobTaskRunningNow(String tenantId) {
		tenantIdToExportTaskRunning.put(tenantId, Boolean.TRUE);
	}

	public void setExportJobTaskDone(String tenantId) {
		tenantIdToExportTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public boolean isTenantS3UploadTaskRunning(String tenantId) {
		Boolean running = tenantIdToS3UploadTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}

	public void setS3UploadTaskRunningNow(String tenantId) {
		tenantIdToS3UploadTaskRunning.put(tenantId, Boolean.TRUE);
	}

	public void setS3UploadTaskDone(String tenantId) {
		tenantIdToS3UploadTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public boolean isTenantDeleteLocalFilesTaskRunning(String tenantId) {
		Boolean running = tenantIdToDeleteLocalFilesTaskRunning.get(tenantId);
		if (running == null)
			return false;

		return running.booleanValue();
	}

	public void setDeleteLocalFilesTaskRunningNow(String tenantId) {
		tenantIdToDeleteLocalFilesTaskRunning.put(tenantId, Boolean.TRUE);
	}

	public void setDeleteLocalFilesTaskDone(String tenantId) {
		tenantIdToDeleteLocalFilesTaskRunning.put(tenantId, Boolean.FALSE);
	}

	public void setTenantIdToCheckExisitingFilesTaskRunning(String tenantId, String status) {
		tenantIdToCheckExisitingFilesTaskRunning.put(tenantId, status);
	}

}
