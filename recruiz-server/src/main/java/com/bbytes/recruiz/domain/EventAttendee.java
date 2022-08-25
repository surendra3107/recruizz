package com.bbytes.recruiz.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author souravkumar
 *
 */

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity(name = "event_attendee")
public class EventAttendee extends AbstractEntity {

	private static final long serialVersionUID = 8163210603472677715L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;
	
	private String email;
	
	private String status = "Not_Yet_Responded" ;	// this will be the default value
	
}
