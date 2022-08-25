package com.bbytes.recruiz.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;
import com.bbytes.recruiz.utils.StringUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false,exclude= {"enrolledPeoples"})
@ToString(exclude= {"enrolledPeoples"})
@NoArgsConstructor
@Entity(name = "onboarding_details_admin")
@EntityListeners({ AbstractEntityListener.class })
public class OnBoardingDetailsAdmin extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "sub_category_name", nullable = false)
    private String subCategoryName;

    @Column(name = "onboard_category", nullable = false)
    private String onboardCategory;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "longtext")
    private String description;

    @Column(name = "schedule_date")
    private Date scheduleDate;

    @Column(name = "completed_status")
    private Boolean completedStatus = false;

    @Column
    private String owner;

    @Column(name = "enrolled_people_email")
    private String enrolledPeopleEmails;

    @Transient
    @JsonSerialize
    @JsonDeserialize
    private List<String> enrolledPeoples;
    
    public void setEnrolledPeopleEmails(String email) {
	this.enrolledPeopleEmails = StringUtils.commaSeparate(this.enrolledPeoples);
    }

}
