package com.bbytes.recruiz.search.domain;

import java.util.Date;

import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.completion.Completion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
@Document(indexName = UserSearch.INDEX_NAME, type =  UserSearch.INDEX_NAME)
public class UserSearch extends AbstractSearchEntity {

	public static final String INDEX_NAME= "user";
	
	
	@Field(type = FieldType.String)
	private String tenantName;

	@Field(type = FieldType.Long)
	protected Long docId;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date creationDate;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date modificationDate;

	@Field(type = FieldType.String)
	private String name;

	@Field(type = FieldType.String)
	private String email;

	@Field(type = FieldType.String)
	private String profileUrl;

	@Field(type = FieldType.Boolean)
	private Boolean joinedStatus;

	@Field(type = FieldType.Boolean)
	private Boolean accountStatus;

	@Field(type = FieldType.Date, index = FieldIndex.not_analyzed)
	private Date joinedDate ;
	
	@CompletionField(maxInputLength = 30, payloads = true)
	private Completion emailSuggest;
	
	@CompletionField(maxInputLength = 30, payloads = true)
	private Completion nameSuggest;
	
	public void setEmail(String email) {
		this.email = email;
		if (email != null && !email.isEmpty()) {
			emailSuggest = new Completion(new String[] { email });
			if (getTenantName() == null || getTenantName().isEmpty())
				throw new IllegalArgumentException("Tenant info cannnot be null or empty");

			emailSuggest.setPayload(getTenantName());
		}
	}
	
	public void setName(String name) {
		this.name = name;
		if (name != null && !name.isEmpty()) {
			nameSuggest = new Completion(new String[] { name });
			if (getTenantName() == null || getTenantName().isEmpty())
				throw new IllegalArgumentException("Tenant info cannnot be null or empty");

			nameSuggest.setPayload(getTenantName());
		}
	}
	
}
