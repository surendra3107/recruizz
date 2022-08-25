package com.bbytes.recruiz.search.domain;

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.bbytes.recruiz.enums.CategoryOptions;
import com.bbytes.recruiz.enums.IndustryOptions;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.utils.GlobalConstants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@Document(indexName = ProspectSearch.INDEX_NAME, type = ProspectSearch.INDEX_NAME)
@Setting(settingPath = "/elasticsearch/settings.json")
public class ProspectSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME = "prospect";

	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	protected Long docId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String)
	private String companyName;

	@Field(type = FieldType.String)
	private String name;

	@Field(type = FieldType.String)
	private String mobile;

	@Field(type = FieldType.String)
	private String email;

	@Field(type = FieldType.String)
	private String owner;

	@Field(type = FieldType.String)
	private String designation;

	@Field(type = FieldType.String)
	private String location;

	@Field(type = FieldType.String)
	private String address;

	@Field(type = FieldType.String)
	private String source;

	@Field(type = FieldType.String)
	private String website;
	
	@Field(type = FieldType.String)
	private String mode = GlobalConstants.PROSPECT_MODE;

	@Field(type = FieldType.String)
	private String industry = IndustryOptions.IT_SW.name();

	@Field(type = FieldType.String)
	private String category = CategoryOptions.IT_Software_Application_Programming.name();

	@Field(type = FieldType.Integer)
	private int prospectRating;

	@Field(type = FieldType.Double)
	private double dealSize;  // currently dealsize reffered as In UI 'value'
	
	@Field(type = FieldType.String)
	private String currency ;
	
	@Field(type = FieldType.Double)
    private double percentage;
	
	@Field(type = FieldType.Double)
	private double value;
	
	@Field(type = FieldType.String)
	private String status = ProspectStatus.New.toString();
	
}
