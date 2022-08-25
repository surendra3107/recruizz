package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.OrderBy;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "feedback")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class Feedback extends AbstractEntity {

	private static final long serialVersionUID = 7928438909943298289L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@OrderBy("id desc")
	private long id;

	private String roundId;

	private String roundCandidateId;

	@Column(length = 1000)
	private String feedbackBy;

	@Column(length = 1000)
	private String feedbackByName;

	private String feedbackByMobile;

	@Column(columnDefinition = "longtext")
	private String feedback = "Yet to give";

	private String status = "N/A";

	@Column(length = 1000)
	private String type;

	private boolean active = true;

	@Column(length = 1000)
	private String positionName;

	@Column(length = 1000)
	private String clientName;

	@Column(length = 1000)
	private String roundName;

	// storing feedback share result id for custom feedback form
	@Column(name = "feedback_share_result_id")
	private String levelbarFeedbackShareResultId;

	private String candidateId;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "round_candidate")
	private RoundCandidate roundCandidate;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "feedback_reason", joinColumns = { @JoinColumn(name = "id") })
	private List<String> reason = new ArrayList<String>();

	private String ratings;

	@Column(length = 1000)
	private String eventCreatedBy;

	@Column(name="event_creator_email")
	private String eventCreatorEmail;
	
	@Column(name="profileMasked")
	private boolean profileMasked = false;
}
