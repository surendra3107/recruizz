package com.bbytes.recruiz.domain;

import java.util.HashMap;
import java.util.HashSet;
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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import com.bbytes.recruiz.enums.CategoryOptions;
import com.bbytes.recruiz.enums.IndustryOptions;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.ProspectDBEventListener;
import com.bbytes.recruiz.utils.GlobalConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(exclude = { "notes", "prospectContactInfo", "prospectPositions", "client" })
@Entity(name = "prospect")
@EqualsAndHashCode(callSuper = false, exclude = { "notes", "prospectContactInfo", "prospectPositions", "client" })
@EntityListeners({ ProspectDBEventListener.class, AbstractEntityListener.class })
public class Prospect extends AbstractEntity {

    private static final long serialVersionUID = -1931323132289452514L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long prospectId;

    @Column(length = 1000, unique = true, nullable = false) // Mandatory
    private String companyName;

    @Column(nullable = false) // Mandatory
    private String name;

    @Column(unique = true) // Earlier Mandatory now not mandatory
    private String mobile;

    @Column // Earlier Mandatory now not mandatory
    private String email;

    @Column(nullable = false) // by default it will be the logged in user
    private String owner;

    private String designation;

    private String location;

    private String address;

    private String source;

    private String website;

    private String mode = GlobalConstants.PROSPECT_MODE;

    @Column(nullable = false)
    private String industry = IndustryOptions.IT_SW.name();

    @Column(nullable = false)
    private String category = CategoryOptions.IT_Software_Application_Programming.name();

    @Column(nullable = false)
    private int prospectRating;

    @Column(nullable = false, name = "deal_size")
    private double dealSize; // currently dealsize reffered as In UI 'value'

    @Column(nullable = false)
    private String currency;

    private double percentage;

    private double value;

    private String status = ProspectStatus.New.toString();

    @Column(name = "dummy")
    private boolean dummy = false;

    // if the prospectStatus is Lost then Reason is Mandatory .
    private String reason = "N/A";

    @OneToMany(mappedBy = "prospect", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProspectContactInfo> prospectContactInfo = new HashSet<ProspectContactInfo>();

    @OneToMany(mappedBy = "prospect", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProspectNotes> notes = new HashSet<ProspectNotes>();

    @OneToMany(mappedBy = "prospect", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProspectPosition> prospectPositions = new HashSet<ProspectPosition>();

    @OneToOne(fetch = FetchType.LAZY)
    private Client client;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "custom_field_prospect", joinColumns = @JoinColumn(name = "prospectId"))
    private Map<String, String> customField = new HashMap<>();
    
}
