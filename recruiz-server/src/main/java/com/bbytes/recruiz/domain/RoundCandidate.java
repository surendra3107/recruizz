package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "points", "candidate", "round", "feedback" })
@ToString(exclude = { "points", "candidate", "round", "feedback" })
@NoArgsConstructor
@Entity(name = "round_candidate")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners({ AbstractEntityListener.class })
public class RoundCandidate extends AbstractEntity implements Comparable<RoundCandidate> {

	private static final long serialVersionUID = -5502156797959954118L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	private String roundId;

	@Column
	private String positionCode;

	@Column
	private Double cardIndex;

	@ManyToOne
	private Candidate candidate;

	@Column(name="joined_date")
	private Date joinedDate;

	@Column(name="offer_date")
	private Date offerDate;

	private String status = BoardStatus.InProgress.toString();

	@OneToMany(mappedBy = "roundCandidate", orphanRemoval = true)
	private List<Points> points = new ArrayList<Points>();

	@OrderBy("id DESC")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "roundCandidate", cascade = CascadeType.ALL)
	private Set<Feedback> feedback;

	/*
	 * @OneToMany(mappedBy="roundCandidate",orphanRemoval= true) private
	 * Set<Points> points = new HashSet<Points>();
	 */

	@ManyToOne
	private Round round;

	private String sourcedBy;


	private String joinedByHr;

	// this will store the position code and candidate id map key so that the
	// candidate will not be repeated in same board for a position
	@Column(nullable = false, unique = true)
	private String positionCandidateKey;

	public void addFeedback(Feedback addFeedback) {
		addFeedback.setRoundCandidate(RoundCandidate.this);
		if (getFeedback() != null) {
			getFeedback().add(addFeedback);
		} else {
			feedback = new HashSet<Feedback>();
			feedback.add(addFeedback);
		}
	}

	public void addFeedback(Collection<Feedback> feedbackPeopleList) {

		for (Feedback feedback : feedbackPeopleList) {
			addFeedback(feedback);
		}
		if (getFeedback() != null) {
			getFeedback().addAll(feedbackPeopleList);
		} else {
			feedback = new HashSet<Feedback>();
			feedback.addAll(feedbackPeopleList);
		}
	}

	// comparable for sorting by card index
	@Override
	public int compareTo(RoundCandidate roundCandidate) {
		if (roundCandidate.getCardIndex() != null) {
			if (getCardIndex() == null)
				cardIndex = 0.0;
			return getCardIndex().compareTo(roundCandidate.getCardIndex());
		}
		return 0;
	}

	public void setPositionCandidateKey() {
		this.positionCandidateKey = positionCode + "-" + candidate.getCid();
	}
}
