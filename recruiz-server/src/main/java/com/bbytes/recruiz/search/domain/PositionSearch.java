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

import com.bbytes.recruiz.domain.Vendor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Document(indexName = PositionSearch.INDEX_NAME, type = PositionSearch.INDEX_NAME)
@Setting(settingPath = "/elasticsearch/settings.json")
public class PositionSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME = "position";

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
	private String title;

	@Field(type = FieldType.String)
	private String location;

	@Field(type = FieldType.Integer)
	private int totalPosition;

	@Field(type = FieldType.Date)
	private Date openedDate = DateTime.now().toDate();

	@Field(type = FieldType.Date)
	private Date closeByDate;

	@Field(type = FieldType.String)
	private String positionUrl;

	@Field(type = FieldType.String)
	private Set<String> positionGoodSkillSet = new HashSet<String>();

	@Field(type = FieldType.String)
	private Set<String> positionReqSkillSet = new HashSet<String>();

	@Field(type = FieldType.String)
	private Set<String> vendorEmails = new HashSet<String>();

	@Field(type = FieldType.String)
	private String type; // Payroll or onContract basis

	@Field(type = FieldType.Boolean)
	private boolean remoteWork;

	@Field(type = FieldType.Double)
	private double maxSal;

	@Field(type = FieldType.Double)
	private double minSal;

	@Field(type = FieldType.Double)
	private double maxExp;

	@Field(type = FieldType.Double)
	private double minExp;

	@Field(type = FieldType.String)
	private String notes;

	@Field(type = FieldType.String)
	private String description;

	@Field(type = FieldType.String)
	private String status;
	
	@Field(type = FieldType.String)
	private String finalStatus;


	public void setGoodSkillSet(Set<String> goodSkillSet) {
		if (goodSkillSet == null)
			return;

		this.positionGoodSkillSet = goodSkillSet;
	}

	public void setReqSkillSet(Set<String> reqSkillSet) {
		if (reqSkillSet == null)
			return;

		this.positionReqSkillSet = reqSkillSet;
		
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void addVendorEmails(Set<Vendor> vendors) {
		if(vendors != null && !vendors.isEmpty()){
			for (Vendor vendor : vendors) {
				vendorEmails.add(vendor.getEmail());
			}
		}
	}

}
