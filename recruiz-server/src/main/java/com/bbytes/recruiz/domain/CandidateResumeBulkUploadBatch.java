package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.bbytes.recruiz.enums.ResumeBulkBatchUploadStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "candidateResumeUploadItems" })
@ToString(exclude = { "candidateResumeUploadItems" })
@NoArgsConstructor
@Entity(name = "candidate_resume_bulk_upload")
@EntityListeners({ AbstractEntityListener.class })
public class CandidateResumeBulkUploadBatch extends AbstractEntity {

	private static final long serialVersionUID = -1733333790683438233L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "batch_id", unique = true, nullable = false)
	private String batchId;

	@Column
	private String failedFileZipUrl;
	
	@Column
	private String source;
	
	@Column
	private String folderId;

	@Column
	private String status = ResumeBulkBatchUploadStatus.UPLOADING.toString();

	@JsonProperty(access=Access.WRITE_ONLY)
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "candidateResumeBulkUpload")
	private List<CandidateResumeUploadItem> candidateResumeUploadItems = new ArrayList<CandidateResumeUploadItem>();

	@Column
	private String owner;

	@Column(name = "upload_date_time")
	private Date uploadDateTime = new Date();

	public CandidateResumeUploadItem addCandidateResumeUploadItem(CandidateResumeUploadItem candidateResumeUploadItem) {
		candidateResumeUploadItem.setBatchId(this.batchId);
		candidateResumeUploadItem.setCandidateResumeBulkUpload(this);
		getCandidateResumeUploadItems().add(candidateResumeUploadItem);
		return candidateResumeUploadItem;
	}

	public void addCandidateResumeUploadItem(List<CandidateResumeUploadItem> candidateResumeUploadItemList) {
		if (candidateResumeUploadItemList == null)
			return;

		for (CandidateResumeUploadItem candidateResumeUploadItem : candidateResumeUploadItemList) {
			candidateResumeUploadItem.setBatchId(this.batchId);
			candidateResumeUploadItem.setCandidateResumeBulkUpload(this);
		}
		getCandidateResumeUploadItems().addAll(candidateResumeUploadItemList);
	}

}