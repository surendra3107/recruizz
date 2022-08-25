package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ImportFileDTO implements Serializable {

	private static final long serialVersionUID = 3088462175334711832L;

	private List<Map<String, String>> headerMapList = new LinkedList<Map<String, String>>();

	private String filePath;

	private String importType;

}
