package com.bbytes.recruiz.search.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Document(indexName = PositionRequestSearch.INDEX_NAME, type = PositionRequestSearch.INDEX_NAME)
@Setting(settingPath = "/elasticsearch/settings.json")
public class PositionRequestSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME = "position_request";

	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	protected Long docId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String)
	private String positionCode;

	@Field(type = FieldType.String)
	private String positionTitle;

	@Field(type = FieldType.String)
	private String positionLocation;

	@Field(type = FieldType.Integer)
	private int totalPosition;

	@Field(type = FieldType.Date)
	private Date openedDate = DateTime.now().toDate();

	@Field(type = FieldType.Date)
	private Date closeByDate;

	@Field(type = FieldType.String)
	private String positionUrl;

	@Field(type = FieldType.String)
	private Set<String> positionRequestGoodSkillSet = new HashSet<String>();

	@Field(type = FieldType.String)
	private Set<String> postionRequestReqSkillSet = new HashSet<String>();

	@Field(type = FieldType.String)
	private String type; // Payroll or onContract basis

	@Field(type = FieldType.Boolean)
	private boolean remoteWork;

	@Field(type = FieldType.Double)
	private double maxSal;

	@Field(type = FieldType.String)
	private String notes;

	@Field(type = FieldType.String)
	private String description;

	@Field(type = FieldType.String)
	private String status;

	public void setPositionRequestGoodSkillSet(Set<String> goodSkillSet) {
		if (goodSkillSet == null)
			return;

		this.positionRequestGoodSkillSet = goodSkillSet;

	}

	public void setPositionRequestReqSkillSet(Set<String> reqSkillSet) {
		if (reqSkillSet == null)
			return;

		this.postionRequestReqSkillSet = reqSkillSet;

	}

	public void setPositionRequestLocation(String location) {
		this.positionLocation = location;
	}

	public void setPositionRequestTitle(String title) {
		this.positionTitle = title;
	}

}
