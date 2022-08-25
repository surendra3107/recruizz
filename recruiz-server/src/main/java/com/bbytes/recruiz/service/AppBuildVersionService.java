package com.bbytes.recruiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:git.properties")
public class AppBuildVersionService {

	@Autowired
    private Environment env;
	
	public String getBuildDateTime(){
		return env.getProperty("git.build.time");
	}
	
	public String getBuildBranch(){
		return env.getProperty("git.branch");
	}
	
	public String getBuildId(){
		return env.getProperty("git.commit.id");
	}
	
	public String getAppVersion(){
		return env.getProperty("info.version");
	}
	
	public String getLastCommitUser(){
		return env.getProperty("git.commit.user.name");
	}
	
	public String getLastCommitMessage(){
		return env.getProperty("git.commit.message.full");
	}
	
	
	
}
