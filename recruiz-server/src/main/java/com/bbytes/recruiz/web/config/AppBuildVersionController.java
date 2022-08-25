package com.bbytes.recruiz.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.AppBuildVersionService;

@RestController
public class AppBuildVersionController {

	@Autowired
	private AppBuildVersionService appBuildVersionService;

	/**
	 * Get current app verison and build information like commit id , date etc
	 * 
	 * @return
	 * @throws RecruizException
	 */
	@RequestMapping(value = "/public/app/version", method = RequestMethod.GET)
	public RestResponse appVersionInfo() throws RecruizException {
		AppVersion appVersion = new AppVersion(appBuildVersionService.getAppVersion(), appBuildVersionService.getBuildId(),
				appBuildVersionService.getBuildBranch(), appBuildVersionService.getBuildDateTime(),
				appBuildVersionService.getLastCommitUser(), appBuildVersionService.getLastCommitMessage());
		RestResponse response = new RestResponse(RestResponse.SUCCESS, appVersion);
		return response;
	}

	class AppVersion {

		private String id;

		private String version;

		private String branch;

		private String timeStamp;

		private String lastCommitUser;

		private String lastCommitMessage;

		public AppVersion(String version, String id, String branch, String timeStamp, String lastCommitUser, String lastCommitMessage) {
			this.version = version;
			this.id = id;
			this.branch = branch;
			this.timeStamp = timeStamp;
			this.lastCommitUser = lastCommitUser;
			this.lastCommitMessage = lastCommitMessage;
		}

		public String getBranch() {
			return branch;
		}

		public String getTimeStamp() {
			return timeStamp;
		}

		public String getId() {
			return id;
		}

		public void setBranch(String branch) {
			this.branch = branch;
		}

		public void setTimeStamp(String timeStamp) {
			this.timeStamp = timeStamp;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getLastCommitUser() {
			return lastCommitUser;
		}

		public String getLastCommitMessage() {
			return lastCommitMessage;
		}

		public void setLastCommitUser(String lastCommitUser) {
			this.lastCommitUser = lastCommitUser;
		}

		public void setLastCommitMessage(String lastCommitMessage) {
			this.lastCommitMessage = lastCommitMessage;
		}

	}
}
