package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.bbytes.recruiz.enums.ResumeUploadFileStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "importJobBatch" })
@ToString(exclude = { "importJobBatch" })
@NoArgsConstructor
@Entity(name = "import_job_upload_item")
@EntityListeners({ AbstractEntityListener.class })
public class ImportJobUploadItem extends AbstractEntity {

	private static final long serialVersionUID = 3739382890236004471L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "batch_id", nullable = false)
	private String batchId;

	@Column(name = "process_date_time")
	private Date processDateTime = new Date();

	@Column(name = "failed_reason", columnDefinition = "longtext")
	private String failedReason;

	@Column
	private String name;

	@Column
	private String identifier;

	@Column
	private String status = ResumeUploadFileStatus.PENDING.toString();

	@JsonProperty(access=Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	private ImportJobBatch importJobBatch;

	public void setFailedReason(String failedReason) {
		status = ResumeUploadFileStatus.FAILED.toString();
		this.failedReason = failedReason;
	}

}
