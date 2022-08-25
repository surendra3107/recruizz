package com.bbytes.recruiz.domain;

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
import lombok.ToString;


@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Entity(name = "position_file")
@EntityListeners({ AbstractEntityListener.class })
public class PositionFile extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 1000, name = "file_name")
    private String fileName;

    @Column(length = 1000, name = "file_path")
    private String filePath;

    @Column(length = 1000, name = "file_type")
    private String fileType;

    @Column(name = "position_id")
    private String positionId;
    
    @Column(name = "storageMode")
    private String storageMode;
	
}
