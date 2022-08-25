package com.bbytes.recruiz.mail.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MailgunMailAttachment {

	@JsonProperty("url")
	private String url;
	
	@JsonProperty("content-type")
	private String contentType;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("size")
	private Integer size;
	
	@JsonProperty("filename")
	private String filename;

	/**
	 * 
	 * @return The url
	 */
	@JsonProperty("url")
	public String getUrl() {
		return url;
	}

	/**
	 * 
	 * @param url
	 *            The url
	 */
	@JsonProperty("url")
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 
	 * @return The contentType
	 */
	@JsonProperty("content-type")
	public String getContentType() {
		return contentType;
	}

	/**
	 * 
	 * @param contentType
	 *            The content-type
	 */
	@JsonProperty("content-type")
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * 
	 * @return The name
	 */
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 *            The name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return The size
	 */
	@JsonProperty("size")
	public Integer getSize() {
		return size;
	}

	/**
	 * 
	 * @param size
	 *            The size
	 */
	@JsonProperty("size")
	public void setSize(Integer size) {
		this.size = size;
	}
	
	/**
	 * 
	 * @param contentType
	 *            The content-type
	 */
	@JsonProperty("filename")
	public void setFilename(String filename) {
		this.filename = filename;
		this.name = filename;
	}
}
