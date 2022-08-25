package com.bbytes.recruiz.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.bbytes.recruiz.repository.event.AbstractEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity(name = "onboarding_templates")
@EntityListeners({ AbstractEntityListener.class })
public class OnBoardingTemplate extends AbstractEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 1000, name = "name", nullable = false, unique = true)
    private String templateName;

    @JoinTable(name = "onboarding_templates_tasks")
    @ManyToMany(fetch = FetchType.LAZY,cascade=CascadeType.REMOVE)
    private Set<OnBoardingDetailsAdmin> tasks = new HashSet<>();
}
