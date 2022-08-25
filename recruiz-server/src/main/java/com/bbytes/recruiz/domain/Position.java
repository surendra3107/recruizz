package com.bbytes.recruiz.domain;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;

import com.bbytes.recruiz.enums.Currency;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.PositionDBEventListener;
import com.bbytes.recruiz.utils.DatePickerDateDeSerializer;
import com.bbytes.recruiz.utils.DatePickerDateSerializer;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "board", "decisionMakers", "interviewers", "hrExecutives", "client",
	"educationalQualification", "location", "description", "vendors", "team", "positionFolderLinks" })
@ToString(exclude = { "board", "decisionMakers", "interviewers", "hrExecutives", "client", "educationalQualification",
	"location", "description", "vendors", "team", "positionFolderLinks" })
@NoArgsConstructor
@Entity(name = "position")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ PositionDBEventListener.class, AbstractEntityListener.class })
public class Position extends AbstractEntity {

    private static final long serialVersionUID = -5684497214923801390L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = true, unique = true)
    private String positionCode;

    @Column(nullable = false)
    private String title;

    // location might contain special chars so made it blob instead of charset
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 100000)
    private byte[] location;

    @Column(nullable = false)
    private int totalPosition;

    @Column(nullable = false)
    private Date openedDate = DateTime.now().toDate();

    @Column
    private Date closedDate;

    @Column(nullable = false)
    private Date closeByDate;

    @Column(length = 1000)
    private String positionUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "position_good_skill_set")
    @JsonProperty(access = Access.READ_WRITE)
    private Set<String> goodSkillSet = new HashSet<String>();

    @ElementCollection(fetch = FetchType.EAGER) 
    @CollectionTable(name = "position_req_skill_set")
    @JsonProperty(access = Access.READ_WRITE)
    private Set<String> reqSkillSet = new HashSet<String>();

    @ElementCollection
    @CollectionTable(name = "position_educationa_qualification")
    @JsonProperty(access = Access.READ_WRITE)
    private Set<String> educationalQualification = new HashSet<String>();

    @NotAudited
    @JsonIgnore
    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PositionFolderLink> positionFolderLinks;

    @Column(nullable = false)
    private String experienceRange;

    @Column
    private String type; // Payroll or onContract basis

    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private String typeDisplayName; // Payroll or onContract basis

    @Column
    private boolean remoteWork=false;

    @Column(nullable = false)
    private double maxSal;

    @Column(nullable = false)
    private double minSal;

    private String salUnit = Currency.Dollar.toString();

    @Column
    private String industry;

    @Column
    private String functionalArea;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 100000)
    private byte[] notes;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 100000)
    private byte[] description;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String jdPath;

    private String clientStatus = Status.Active.toString();

    @Column(nullable = false)
    private String owner;

    @Column
    private String closedByUser;

    @Column
    private boolean dummy = false;

    @Column(name = "publish_career_site")
    private boolean publishCareerSite = false;

    @Column(name = "publish_recruiz_connect")
    private boolean publishRecruizConnect = false;

    @Column(name = "connect_corporate_id")
    private String connectCorporateId;

    @Column(name = "connect_instance_id")
    private String connectInstanceId;

    @Column(name = "publish_mode")
    private String publishMode;

    @Column(name = "import_identifier", unique = true)
    private String importIdentifier;

    @Column
    private String nationality;

    @Column
    private String hiringManager = "NA"; // will have "Account/Hiring Manager/RMG";

    @Column
    private String verticalCluster = "NA";

    @Column
    private String endClient = "NA";  //Account / End Client

    @Column
    private String screener = "NA";  //exclusive for teamware

    private String spoc = "NA";

    @Column
    private String requisitionId = "NA";

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonProperty(access = Access.READ_WRITE)
    private Client client;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JsonProperty(access = Access.READ_WRITE)
    private Team team;

    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonProperty(access = Access.WRITE_ONLY)
    @JoinTable(name = "position_hr", joinColumns = @JoinColumn(name = "Position_ID", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "HR_ID", referencedColumnName = "user_id"))
    private Set<User> hrExecutives = new HashSet<User>();

    @NotAudited
    @JsonProperty(access = Access.READ_WRITE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "position", cascade = CascadeType.ALL)
    private Set<ClientDecisionMaker> decisionMakers = new HashSet<ClientDecisionMaker>();

    @NotAudited
    @JsonProperty(access = Access.READ_WRITE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "position_interviewer", joinColumns = @JoinColumn(name = "Position_ID", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "interviewer_Id", referencedColumnName = "id"))
    private Set<ClientInterviewerPanel> interviewers = new HashSet<ClientInterviewerPanel>();

    @NotAudited
    @JsonProperty(access = Access.READ_WRITE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "position_vendors", joinColumns = @JoinColumn(name = "Position_ID", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "vendor", referencedColumnName = "id"))
    private Set<Vendor> vendors = new HashSet<Vendor>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Board board;

    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private Map<String, String> boardCandidateCount = new HashMap<String, String>();

    // the status of client and position combined . We do an 'OR' operation on
    // client and position status
    @Transient
    @JsonProperty(access = Access.READ_WRITE)
    private String finalStatus;

    @OneToMany(mappedBy = "positionID", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PositionNotes> positionNotes = new HashSet<PositionNotes>();

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "custom_field_position", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> customField = new HashMap<>();

    public double getMinExp() {
	double minExp = 0.0;
	if (this.experienceRange != null) {
	    String[] result = StringUtils.extractExpYears(this.experienceRange);
	    minExp = Double.parseDouble(result[0].trim());
	}

	return minExp;
    }

    public double getMaxExp() {
	double maxExp = 0.0;
	if (this.experienceRange != null) {
	    String[] result = StringUtils.extractExpYears(this.experienceRange);
	    maxExp = Double.parseDouble(result[1].trim());
	}
	return maxExp;
    }

    public void addInterviewerPanel(ClientInterviewerPanel interviewerPanel) {
	interviewerPanel.setClient(this.getClient());
	if (getInterviewers() != null) {
	    getInterviewers().add(interviewerPanel);
	} else {
	    interviewers = new HashSet<ClientInterviewerPanel>();
	    interviewers.add(interviewerPanel);
	}
    }

    public void addInterviewerPanel(Collection<ClientInterviewerPanel> interviewerPanelList) {
	if (interviewerPanelList == null)
	    return;

	for (ClientInterviewerPanel interviewerPanel : interviewerPanelList) {
	    addInterviewerPanel(interviewerPanel);
	}
    }

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getOpenedDate() {
	return openedDate;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setOpenedDate(Date openedDate) {
	this.openedDate = openedDate;
    }

    @JsonSerialize(using = DatePickerDateSerializer.class)
    public Date getCloseByDate() {
	return closeByDate;
    }

    @JsonDeserialize(using = DatePickerDateDeSerializer.class)
    public void setCloseByDate(Date closeByDate) {
	this.closeByDate = closeByDate;
    }

    // changing location String to blob
    public void setLocation(String locationString) {
	if (locationString != null && !locationString.isEmpty()) {
	    byte[] locationBlob = locationString.getBytes(StandardCharsets.UTF_8);
	    location = locationBlob;
	}
    }

    // changing location blob to String
    public String getLocation() {
	String locationString = new String(location, StandardCharsets.UTF_8);
	return locationString;
    }

    // changing notes String to blob
    public void setNotes(String notesString) {
	if (notesString != null && !notesString.isEmpty()) {
	    byte[] notesBlob = notesString.getBytes(StandardCharsets.UTF_8);
	    notes = notesBlob;
	} else {
	    notes = null;
	}
    }

    // changing notes blob to String
    public String getNotes() {
	if (notes != null && notes.length > 0) {
	    String notesString = new String(notes, StandardCharsets.UTF_8);
	    return notesString;
	}
	return null;
    }

    // changing notes String to blob
    public void setDescription(String descriptionString) {
	if (descriptionString != null && !descriptionString.isEmpty()) {
	    byte[] descriptionBlob = descriptionString.getBytes(StandardCharsets.UTF_8);
	    description = descriptionBlob;
	} else {
	    description = null;
	}
    }

    // changing notes blob to String
    public String getDescription() {
	if (description != null && description.length > 0) {
	    String descriptionString = new String(description, StandardCharsets.UTF_8);
	    return descriptionString;
	}
	return null;

    }

    @JsonGetter("directHrList")
    public Set<User> getDirectHrExecutives() {
	//return this.hrExecutives;
	return getTeamAndHrExecutives();
    }

    public Set<User> getHrExecutives() {
	return this.hrExecutives;
    }


    public Set<User> getTeamAndHrExecutives() {
	Set<User> allHrs = new HashSet<>();
	if (null != this.hrExecutives && !this.hrExecutives.isEmpty()) {
	    allHrs.addAll(this.hrExecutives);
	}

	if (this.team != null && this.team.getMembers() != null && !this.team.getMembers().isEmpty()) {
	    Set<TeamMember> teamMembers = team.getMembers();
	    if (null != teamMembers && !teamMembers.isEmpty()) {
		for (TeamMember teamMember : teamMembers) {
		    allHrs.add(teamMember.getUser());
		}
	    }
	}
	if (allHrs == null || allHrs.isEmpty()) {
	    return this.hrExecutives;
	}
	return allHrs;
    }

}
