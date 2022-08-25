package com.bbytes.recruiz.domain;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.ClientDBEventListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "clientDecisionMaker", "clientInterviewerPanel", "positions","files","customeField" })
@ToString(exclude = { "clientDecisionMaker", "clientInterviewerPanel", "positions","files","customeField"})
@NoArgsConstructor
@Entity(name = "client")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ ClientDBEventListener.class, AbstractEntityListener.class })
@NamedQueries({
	@NamedQuery(name = "Client.clientListWithTotalOpening", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO(c, COUNT(p))"
		+ "FROM client c LEFT JOIN  c.positions p where c.id IN :ids GROUP BY c"),
	@NamedQuery(name = "Client.clientWithTotalOpening", query = "SELECT NEW com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO(c, COUNT(p))  "
		+ "FROM client c LEFT JOIN  c.positions p  where c.id = ?"), })
public class Client extends AbstractEntity {

	private static final long serialVersionUID = 8249374589229148434L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 1000)
    private String clientName;

    @Column(nullable = false, length = 1000)
    private String address;

    @Column(length = 1000)
    private String website;

    @Column(nullable = false, length = 1000)
    private String clientLocation;

    private String empSize;

    private String turnOvr;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 100000)
    private byte[] notes;

    private String status;

    private String owner;

    @Column
    private boolean dummy = false;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id")
    private Set<Position> positions;

    @ElementCollection(fetch=FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "invoice_info", joinColumns = @JoinColumn(name = "client_id"))
    private Map<String, String> invoiceInfo = new HashMap<>();

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonProperty(access = Access.READ_WRITE)
    private Set<ClientDecisionMaker> clientDecisionMaker;

    @NotAudited
    @JsonProperty(access = Access.READ_WRITE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ClientInterviewerPanel> clientInterviewerPanel;

    @OneToMany(mappedBy = "clientId", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientNotes> clientNotes = new HashSet<ClientNotes>();

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientFile> files = new HashSet<ClientFile>();

    
    @ElementCollection(fetch=FetchType.LAZY)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "custom_field_client", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> customeField = new HashMap<>();

    
    public void addClientDecisionMaker(ClientDecisionMaker decisonMakers) {

	decisonMakers.setClient(Client.this);
	if (getClientDecisionMaker() != null) {
	    getClientDecisionMaker().add(decisonMakers);
	} else {
	    clientDecisionMaker = new HashSet<ClientDecisionMaker>();
	    clientDecisionMaker.add(decisonMakers);
	}
    }

    public void addClientDecisionMaker(Collection<ClientDecisionMaker> decisonMakersList) {

	if (decisonMakersList == null)
	    return;

	for (ClientDecisionMaker decisionMaker : decisonMakersList) {
	    addClientDecisionMaker(decisionMaker);
	}
	if (getClientDecisionMaker() != null) {
	    getClientDecisionMaker().addAll(decisonMakersList);
	} else {
	    clientDecisionMaker = new HashSet<ClientDecisionMaker>();
	    clientDecisionMaker.addAll(decisonMakersList);
	}
    }

    public void addClientInterviewerPanel(ClientInterviewerPanel interviewerPanel) {

	interviewerPanel.setClient(this);
	if (getClientInterviewerPanel() != null) {
	    getClientInterviewerPanel().add(interviewerPanel);
	} else {
	    clientInterviewerPanel = new HashSet<ClientInterviewerPanel>();
	    clientInterviewerPanel.add(interviewerPanel);
	}
    }

    public void addClientInterviewerPanel(Collection<ClientInterviewerPanel> interviewerPanelList) {
	if (interviewerPanelList == null)
	    return;

	for (ClientInterviewerPanel interviewerPanel : interviewerPanelList) {
	    addClientInterviewerPanel(interviewerPanel);
	}
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

}
