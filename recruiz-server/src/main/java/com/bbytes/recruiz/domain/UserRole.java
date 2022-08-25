package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "user_roles")
public class UserRole extends AbstractEntity {

	private static final long serialVersionUID = -991935115273114411L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "role_name", unique = true)
	private String roleName;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "permission", joinColumns = { @JoinColumn(name = "role_name") })
	private Set<Permission> permissions = new HashSet<Permission>();

	@Transient
	@JsonProperty(access=Access.READ_WRITE)
	private List<String> permissionList = new ArrayList<String>();
}
