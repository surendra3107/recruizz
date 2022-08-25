package com.bbytes.recruiz.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.bbytes.recruiz.service.NotificationService;
import com.bbytes.recruiz.service.UserService;

@Controller
public class NotificationController {

	@Autowired
	SimpMessagingTemplate template;

	@Autowired
	NotificationService notificationService;

	@Autowired
	private UserService userService;
	
	

	@SendTo("/queue/greetings")
	void sendPong() {
		// template.convertAndSend("/topic/greetings", "connected to
		// notification server");
		// template.convertAndSendToUser(userService.getLoggedInUserEmail(),
		// "/queue/greetings", "connected to notification server");

	}

	@MessageMapping("/ping")
	@SendTo("/queue/greetingss")
	void sendUserMsg(SimpMessageHeaderAccessor headerAccessor) {
		Map<String, Object> attrs = headerAccessor.getSessionAttributes();
		notificationService.sendMsg(userService.getLoggedInUserEmail(),"Test notification");
	}
}
