package com.bbytes.recruiz.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.repository.event.UserDBEventListener;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "positions", "emailDetails","members" })
@ToString(exclude = { "positions", "emailDetails","members" })
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="userId")
@Entity(name = "user")
@EntityListeners({ UserDBEventListener.class, AbstractEntityListener.class })
public class User extends AbstractEntity {

	private static final long serialVersionUID = -111935115273119911L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "name")
	private String name;

	@Column(unique = true)
	private String email;

	@Column
	private String mobile;
	
	@Column
	private String reporttimeperiod;

	@Column(name = "profile_url", length = 1000)
	private String profileUrl;

	@Column(name = "password")
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;

	@ManyToOne
	@JoinColumn(name = "role")
	private UserRole userRole;

	@ManyToOne
	@JoinColumn(nullable = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private Organization organization;

	@Column(name = "joined_status")
	private Boolean joinedStatus;

	@Column(name = "account_status")
	private Boolean accountStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
	@Column(name = "joinedDate_date")
	private Date joinedDate = DateTime.now().toDate();

	@Column(name = "last_logged_on_time")
	private Date loggedOn;

	@Column(name = "timezone")
	private String timezone = "Asia/Kolkata";

	@Column(name = "locale")
	private String locale = "en";

	@Column(name = "designation")
	private String designation;

	@Column(name = "marked_delete")
	private Boolean markForDelete = false;

	@Column(name = "user_type")
	private String userType = GlobalConstants.USER_TYPE_APP;

	@Column(name = "vendor_id")
	private String vendorId;

	@Column(name = "api_token", length = 5000)
	private String apiToken;

	@Column
	private Boolean isNotificationOn = false;

	@ManyToMany(mappedBy = "hrExecutives", fetch = FetchType.LAZY)
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<Position> positions = new ArrayList<Position>();

	@Column(name = "profile_signature", columnDefinition = "longtext")
	private String profileSignature;

	// email configuration details
	@Column
	private String username;

	@Column
	private String emailPassoword;

	@Column
	private String host;

	@Column
	private Integer port = 0;

	@Column
	private String protocol;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private List<EmailClientDetails> emailDetails = new ArrayList<EmailClientDetails>();
	
	@NotAudited
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy="user")
	private Set<TeamMember> members = new HashSet<TeamMember>();

	public User(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getLocale() {
		if (this.locale == null)
			this.locale = "en";

		return this.locale;
	}

	/**
	 * this setter will disable user if it is marked for delete and vice-versa
	 * 
	 * @param markForDelete
	 */
	public void setMarkForDelete(boolean markForDelete) {
		this.markForDelete = markForDelete;
		this.accountStatus = !markForDelete;
	}

	public static String PENDING = "Pending";
	public static String JOINED = "Joined";
}
