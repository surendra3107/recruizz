package com.bbytes.recruiz.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppStatusController {

	@RequestMapping("/app/status")
	public String appCurrentStatus() {
		return "Application running fine !!";
	}

}
