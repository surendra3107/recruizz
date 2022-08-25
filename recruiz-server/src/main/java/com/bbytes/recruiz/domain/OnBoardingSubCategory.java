package com.bbytes.recruiz.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString()
@NoArgsConstructor
@Entity(name = "onboardiing_sub_category")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class OnBoardingSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "sub_category_name", nullable = false)
    private String subCategoryName;

    @Column(name = "onboard_category", nullable = false)
    private String onboardCategory;

    @Column(name = "composite_key", nullable = false, unique = true)
    private String compositeKey;

    public void setCompositeKey(String onboardCategory) {
	this.compositeKey = this.onboardCategory + "-" + this.subCategoryName;
    }

}
