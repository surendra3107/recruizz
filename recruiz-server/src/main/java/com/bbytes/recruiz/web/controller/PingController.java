package com.bbytes.recruiz.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

	@RequestMapping(value = "/public/apiserver/status")
	String status() {
		return "Server running fine";
	}

}