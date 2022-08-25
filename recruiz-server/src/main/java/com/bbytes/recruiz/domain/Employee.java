package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "employee")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class Employee extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "official_email", nullable = false)
    private String officialEmail;

    @Column(name = "employement_status", nullable = false)
    private String employmentStatus;

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(name = "job_location")
    private String jobLocation;

    @Column(name = "reporting_manager")
    private String reportingManager;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "doj")
    private Date doj;

    @Column(name = "role")
    private String role;

    @Column(name = "emp_id")
    private String empID;

    @Column(name = "team")
    private String team;

    @Column(name = "hr_contact")
    private String hrContact;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "presonal_email")
    private String presonalEmail;

    @Column(name = "primary_contact")
    private String primaryContact;

    @Column(name = "alternate_email")
    private String alternateEmail;

    @Column(name = "placed_at")
    private String placedAt;

    @Column(name = "status")
    private String status;

    @Column(name = "position_code")
    private String positionCode;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "client_name")
    private String clientName;

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeFile> files = new HashSet<EmployeeFile>();

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "custom_field_employee", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> customField = new HashMap<>();
    
    
}
