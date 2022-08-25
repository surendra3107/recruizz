package com.bbytes.recruiz.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "account_activity")
public class AccountActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String activityTitle;

	private String entityID;

	private String details;

	private String date;

	private String userEmail;

}
