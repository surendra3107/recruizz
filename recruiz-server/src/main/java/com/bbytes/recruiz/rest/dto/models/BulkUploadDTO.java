package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class BulkUploadDTO implements Serializable {

	private static final long serialVersionUID = -2190129400799015471L;

	private List<MultipartFile> files = new ArrayList<MultipartFile>();
}
