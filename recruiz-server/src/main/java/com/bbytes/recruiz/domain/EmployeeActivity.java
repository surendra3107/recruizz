package com.bbytes.recruiz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity(name = "employee_activity")
@EntityListeners({ AbstractEntityListener.class })
public class EmployeeActivity extends AbstractEntity {

    private static final long serialVersionUID = -7160900260784830724L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String actionByEmal;

    @Column
    private String actionByName;

    @Column
    private String activityType;

    @Column(length = 1000)
    private String message;

    @Column
    private Date time;

    @Column(name = "employee_id")
    private Long eid;

    public EmployeeActivity(String actionByEmail, String actionByName, String activityType, String message,
	    Date time, Long eid) {
	this.actionByEmal = actionByEmail;
	this.actionByName = actionByName;
	this.message = message;
	this.activityType = activityType;
	this.time = time;
	this.eid = eid;
    }

}
