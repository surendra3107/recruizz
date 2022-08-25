package com.bbytes.recruiz.search.domain;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public abstract class AbstractSearchEntity {

	@Id
	protected String entityIdWithTenantName;

	public void setId(Long id, String tenantName) {
		this.entityIdWithTenantName = getId(id, tenantName);
		setDocId(id);
		setTenantName(tenantName);
	}

	public void setId(String prefix, Long id, String tenantName) {
		this.entityIdWithTenantName = getId(prefix, id, tenantName);
		setDocId(id);
		setTenantName(tenantName);
	}

	public String getId() {
		return this.entityIdWithTenantName;
	}

	public static String getId(Long id, String tenantName) {
		return id + ":" + tenantName;
	}

	public static String getId(String prefix, Long id, String tenantName) {
		return prefix + ":" + id + ":" + tenantName;
	}

	protected String getListAsString(List<String> items) {
		String result = "";
		for (String item : items) {
			result = result + " " + item;
		}

		return result;

	}

	protected void setEntityIdWithTenantName(String entityIdWithTenantName) {
		this.entityIdWithTenantName = entityIdWithTenantName;
	}

	protected abstract void setTenantName(String tenantName);

	protected abstract String getTenantName();

	protected abstract void setDocId(Long id);

	protected abstract Long getDocId();

}