package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "importJobUploadItems" })
@ToString(exclude = { "importJobUploadItems" })
@NoArgsConstructor
@Entity(name = "import_job_batch")
@EntityListeners({ AbstractEntityListener.class })
public class ImportJobBatch extends AbstractEntity {

	private static final long serialVersionUID = -5322644231817962090L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "batch_id", unique = true, nullable = false)
	private String batchId;

	@Column
	private String status = ResumeUploadFileStatus.PROCESSING.toString();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "importJobBatch")
	private Set<ImportJobUploadItem> importJobUploadItems = new HashSet<ImportJobUploadItem>();

	@Column
	private String owner;

	@Column(name = "import_type")
	private String importType;

	@Column(name = "success_row_count")
	private long successRowCount;

	@Column(name = "failed_row_count")
	private long failedRowCount;

	@Column(name = "total_row_count")
	private long totalRowCount;

	@Transient
	@JsonProperty(access = Access.WRITE_ONLY)
	private long pendingRowCount;

	@Column(name = "upload_date_time")
	private Date uploadDateTime = new Date();

	@Column(name = "file_path")
	private String filePath;

	@Column(name = "header_map")
	private String headerMap;

	public long getPendingRowCount() {
		return (this.totalRowCount - (this.successRowCount + this.failedRowCount));
	}

	public void addSuccessRowCount(long count) {
		this.setSuccessRowCount(this.successRowCount + count);
	}

	public void addFailedRowCount(long count) {
		this.setFailedRowCount(this.failedRowCount + count);
	}

	public ImportJobUploadItem addImportJobBatchItem(ImportJobUploadItem importJobUploadItem) {
		importJobUploadItem.setBatchId(this.batchId);
		importJobUploadItem.setImportJobBatch(this);
		getImportJobUploadItems().add(importJobUploadItem);
		return importJobUploadItem;
	}

	public void addImportJobBatchItem(Set<ImportJobUploadItem> importJobUploadItemList) {
		if (importJobUploadItemList == null)
			return;

		for (ImportJobUploadItem importJobUploadItem : importJobUploadItemList) {
			importJobUploadItem.setBatchId(this.batchId);
			importJobUploadItem.setImportJobBatch(this);
		}
		getImportJobUploadItems().addAll(importJobUploadItemList);
	}

}