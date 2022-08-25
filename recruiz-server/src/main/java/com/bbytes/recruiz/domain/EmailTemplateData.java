package com.bbytes.recruiz.domain;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import org.springframework.data.repository.cdi.Eager;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "email_template_data")
@EntityListeners({AbstractEntityListener.class })
public class EmailTemplateData extends AbstractEntity {

	private static final long serialVersionUID = 458887938226263945L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(unique=true)
	private String name;

	@Column
	private String subject;

	@Column(columnDefinition = "longtext")
	private String body;

	@Column
	private String category;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "template_variable", joinColumns = { @JoinColumn(name = "templateId") })
	private List<String> templateVariable = new LinkedList<>();
//	private Set<String> templateVariableSet = new LinkedHashSet<String>();

	@Column
	private long subjectVersion = 0;

	@Column
	private long bodyVersion = 0;

	@Column
	private boolean subjectEdited = false;


	@Column
	private boolean bodyEdited = false;
	
	@Column
	private String variableData = "";

//	@Transient
//	@JsonSerialize
//	@JsonDeserialize
//	List<String> templateVariable = new LinkedList<>();

//	public void setTemplateVariableSet(List<String> templateVariable) {
//	    if(null != templateVariable && !templateVariable.isEmpty()) {
//		for (String variable : templateVariable) {
//		    templateVariableSet.add(variable);
//		}
//	    }
//	}


}