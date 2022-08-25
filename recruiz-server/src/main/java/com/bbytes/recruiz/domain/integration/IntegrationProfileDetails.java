package com.bbytes.recruiz.domain.integration;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.bbytes.recruiz.domain.AbstractEntity;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "userEmail", "integrationModuleType" }))
@EntityListeners({ AbstractEntityListener.class })
public class IntegrationProfileDetails extends AbstractEntity {

	
	private static final long serialVersionUID = -5743456582333414224L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private String userEmail;

	@Column(nullable = false)
	private String integrationModuleType;

	@ElementCollection
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "integration_details", joinColumns = @JoinColumn(name = "integration_profile_id"))
	private Map<String, String> integrationDetails = new HashMap<>();

}
