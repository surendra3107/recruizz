package com.bbytes.recruiz.search.domain;

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Document(indexName = ClientSearch.INDEX_NAME, type = ClientSearch.INDEX_NAME)
public class ClientSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME= "client";
	
	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	protected Long docId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String)
	private String clientName;

	@Field(type = FieldType.String)
	private String address;

	@Field(type = FieldType.String)
	private String status;

	@Field(type = FieldType.String)
	private String website;

	@Field(type = FieldType.String)
	private String clientLocation;

	@Field(type = FieldType.String)
	private String notes;


}
