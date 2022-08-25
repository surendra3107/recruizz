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
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "email_file_item")
@EntityListeners({ AbstractEntityListener.class })
public class EmailedFileItem extends AbstractEntity {

	private static final long serialVersionUID = -1659574498483772292L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "email_id", nullable = false)
	private String emailId;
	
	@Column(name = "recruiz_email", nullable = false)
	private String recruizEmailId;
	
	@Column(name = "email_uid")
	private String emailUid;
	
	@Column(name = "email_subject", nullable = false,columnDefinition="longtext")
	private String subject;

	@Column(name = "process_date_time")
	private Date processDateTime = new Date();

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "file_path", length = 3000, nullable = false)
	private String filePath;

	@Column(name = "file_size_in_kb")
	private Double fileSize;

	@Column(name = "failed_reason", columnDefinition = "longtext")
	private String failedReason;
	
	@Column(name = "file_system")
	private String fileSystem;

	@Column
	private String status = ResumeUploadFileStatus.PENDING.toString();

	
	public void setFailedReason(String failedReason) {
		status = ResumeUploadFileStatus.FAILED.toString();
		this.failedReason = failedReason;
	}

}
