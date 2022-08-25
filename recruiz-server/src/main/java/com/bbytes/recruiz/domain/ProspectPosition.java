package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "keySkills", "prospect", "educationQualification", "location" })
@ToString(exclude = { "keySkills", "prospect", "educationQualification", "location" })
@NoArgsConstructor
@Entity(name = "prospect_position")
@EntityListeners({ AbstractEntityListener.class })
public class ProspectPosition extends AbstractEntity implements Comparable<ProspectPosition> {

	private static final long serialVersionUID = -8096156797957354248L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long positionId;

	private String positionName;

	private double percentage;

	private double value;

	private Date closureDate;

	@Column(name = "number_of_openings")
	private int numberOfOpenings;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "prospect_position_location")
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> location = new HashSet<String>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "prospect_position_education_qualification")
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> educationQualification = new HashSet<String>();

	private double minExperience;

	private double maxExperience;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "prospect_position_key_skills")
	@JsonProperty(access = Access.READ_WRITE)
	private Set<String> keySkills = new HashSet<String>();

	@ManyToOne(fetch = FetchType.LAZY)
	private Prospect prospect;
	
	@Column(nullable = false) // contract .. etc
	private String type;
	
	@Column(nullable = false)
	private boolean remoteWork;
	
	@Column(nullable = false)
	private double maxSal;

	@Column(nullable = false)
	private double minSal;
	
	@Column(nullable = false)
	private String industry;

	@Column(nullable = false)
	private String functionalArea;
	
    private String clientName;
    
    private String status;
    
    private Boolean isConvertedToClient = false;
    
    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String clientStatus;
    
    private String currency = Currency.Rupee.toString();

	@JsonSerialize(using = DatePickerDateSerializer.class)
	public Date getClosureDate() {
		return closureDate;
	}

	@JsonDeserialize(using = DatePickerDateDeSerializer.class)
	public void setClosureDate(Date closureDate) {
		this.closureDate = closureDate;
	}

	@Override
	public int compareTo(ProspectPosition prospectPosition) {
		return getCreationDate().compareTo(prospectPosition.getCreationDate());
	}
}
