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
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "position", "folder" })
@ToString(exclude = { "position", "folder" })
@NoArgsConstructor
@Entity(name = "position_folder")
@Table(name = "position_folder", uniqueConstraints = @UniqueConstraint(columnNames = { "position_id", "folder_id" }))
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@EntityListeners({ AbstractEntityListener.class })
/**
 * Relationship between position and folder 
 */
public class PositionFolderLink extends AbstractEntity {

	private static final long serialVersionUID = 5802780825411859368L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch=FetchType.LAZY)
	private Position position;

	@ManyToOne(fetch=FetchType.LAZY)
	private Folder folder;
	
	@Column(name = "added_by_user_email")
	private String addedByUserEmail;

	@Column(name = "added_date_time")
	private Date addedDate;

}
