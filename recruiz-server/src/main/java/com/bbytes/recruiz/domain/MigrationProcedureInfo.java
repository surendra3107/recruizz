package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "migration_procedure_info")
public class MigrationProcedureInfo extends AbstractEntity {

	private static final long serialVersionUID = 1266712204495460574L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name="procedure_name")
	private String procedureName;

	@Column(name="tenant_id")
	private String tenantId;

	@Column(name="execution_state")
	private Boolean executionState=false;

	@Column(name="version")
	private String version;
	
	@Column(name="description",columnDefinition="LONGTEXT")
	private String description;

}
