package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Embeddable
public class Permission {

	@Column(name = "permissionName")
	private String permissionName;

	public Permission(String permission_name) {
		this.permissionName = permission_name;
	}

}
