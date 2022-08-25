package com.bbytes.recruiz.web.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.service.FileService;

@RestController
public class AppDummyController {

	@Autowired
	private FileService fileService;

	@RequestMapping("/app/pdf/file/get")
	public String appCurrentStatus() throws Exception {
		File document = new File("/home/sourav-bb/Downloads/sample.pdf");

		fileService.addHeaderInPdf("/home/sourav-bb/Downloads/547836270_Jul2017.pdf",
				"/home/sourav-bb/Downloads/sample_edited2.pdf",
				"This Documents belongs to Beyond Bytes Technologies PVT LTD");

		return "Done";
	}

	@RequestMapping("/app/doc/file/edit")
	public String editDocFile() throws Exception {

		String name= "Bijoy T V";
		
		String[] nameArray = name.split(" ");
		String nameToReplace = name;
		if(nameArray.length > 0){
			for (String namePart : nameArray) {
				if(namePart.trim().length() > 2){
					nameToReplace = namePart.trim();
					break;
				}
			}
		}
		
		Map<String, String> textToReplace = new HashMap<>();
		textToReplace.put("572", "***");
		textToReplace.put(nameToReplace, "XXXXXXXXXXXX");
		
//		fileService.addHeaderInPdf("/home/sourav-bb/Downloads/547836270_Jul2017.pdf",
//				"/home/sourav-bb/Downloads/sample_edited2.pdf",
//				"This Documents belongs to Beyond Bytes Technologies PVT LTD");

		String outputFile = "/home/sourav-bb/Downloads/Test Resume/BijoyTV[7_0]_masked.docx";
		fileService.replaceDocxWordFile("/home/sourav-bb/Downloads/Test Resume/BijoyTV[7_0].docx", outputFile,textToReplace,"hdr text");

		return "done";
	}
}
