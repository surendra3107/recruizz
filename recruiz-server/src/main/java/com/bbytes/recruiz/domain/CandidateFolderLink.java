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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "candidate", "folder" })
@ToString(exclude = { "candidate", "folder" })
@NoArgsConstructor
@Entity(name = "candidate_folder")
@Table(name = "candidate_folder", uniqueConstraints = @UniqueConstraint(columnNames = { "candidate_cid", "folder_id" }))
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@EntityListeners({ AbstractEntityListener.class })
/**
 * Candidate folder will contain the relationship between a folder and candidate
 * object
 */
public class CandidateFolderLink extends AbstractEntity {

	private static final long serialVersionUID = 3571315005089316758L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	private Candidate candidate;

	@ManyToOne(fetch = FetchType.EAGER)
	private Folder folder;

	@Column(name = "added_by_user_email")
	private String addedByUserEmail;

	@Column(name = "added_date_time")
	private Date addedDate;

}
