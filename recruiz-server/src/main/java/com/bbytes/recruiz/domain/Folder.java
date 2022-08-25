package com.bbytes.recruiz.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.bbytes.recruiz.enums.FolderType;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "sharedUserList", "candidateFolderLinks", "positionFolderLinks" })
@ToString(exclude = { "sharedUserList", "candidateFolderLinks", "positionFolderLinks" })
@NoArgsConstructor
@Entity(name = "folder")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@EntityListeners({ AbstractEntityListener.class })
/**
 * Object that contains folder metadata like name , desc , users shared with etc
 */
public class Folder extends AbstractEntity {

	private static final long serialVersionUID = -5462329476078405251L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "folder_user_join", joinColumns = { @JoinColumn(name = "folder_id") }, inverseJoinColumns = {
			@JoinColumn(name = "user_id") })
	private Set<User> sharedUserList;

	@Column(name = "folder_display_name", length = 60, unique = true, nullable = false)
	private String displayName;

	@Column(name = "folder_desc", length = 250, nullable = true)
	private String desc;
	
	@Column(name = "owner_email",nullable=false)
	private String owner;

	@Column(name = "folder_public")
	private boolean folderPublic;

	@Enumerated(EnumType.STRING)
	@Column(name = "folder_type", length = 25)
	private FolderType folderType = FolderType.CANDIDATE_FOLDER;

	@OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<CandidateFolderLink> candidateFolderLinks;

	@OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<PositionFolderLink> positionFolderLinks;

	@Transient
	private Integer sharedUserCount = 0;

	@Transient
	private Integer candidateCount = 0;

	@Transient
	private Integer positionCount = 0;

	public void addUserToFolder(User user) {
		if (getSharedUserList() == null)
			sharedUserList = new HashSet<>();

		getSharedUserList().add(user);

	}

	public void addUsersToFolder(Collection<User> users) {
		if (getSharedUserList() == null)
			sharedUserList = new HashSet<>();

		getSharedUserList().addAll(users);

	}

	public Integer getSharedUserCount() {
		if (getSharedUserList() != null)
			sharedUserCount = getSharedUserList().size();

		if(sharedUserCount>0)
			return sharedUserCount-1;
		
		return sharedUserCount;
	}

	public Integer getCandidateCount() {
		if (getCandidateFolderLinks() != null)
			candidateCount = getCandidateFolderLinks().size();

		return candidateCount;
	}

	public Integer getPositionCount() {
		if (getPositionFolderLinks() != null)
			positionCount = getPositionFolderLinks().size();

		return positionCount;
	}

}
