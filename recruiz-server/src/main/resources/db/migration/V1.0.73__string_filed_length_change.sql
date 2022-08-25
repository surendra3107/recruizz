ALTER TABLE `advanced_search_query` 
CHANGE COLUMN `advanced_search_and_keys` `advanced_search_and_keys` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_boolean_query` `advanced_search_boolean_query` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_ctc` `advanced_search_ctc` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_curr_location` `advanced_search_curr_location` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_employement_type` `advanced_search_employement_type` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_experience` `advanced_search_experience` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_not_keys` `advanced_search_not_keys` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_notice_period` `advanced_search_notice_period` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_or_keys` `advanced_search_or_keys` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_pref_location` `advanced_search_pref_location` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_name` `advanced_search_name` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_field` `advanced_search_field` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_skillset` `advanced_search_skillset` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_sourced` `advanced_search_sourced` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_status` `advanced_search_status` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `advanced_search_tab` `advanced_search_tab` VARCHAR(1000) NULL DEFAULT NULL ;


ALTER TABLE `candidate` 
CHANGE COLUMN `communication` `communication` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `currentCompany` `currentCompany` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `currentLocation` `currentLocation` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `facebookProf` `facebookProf` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `fullName` `fullName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `githubProf` `githubProf` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `highestQual` `highestQual` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `linkedinProf` `linkedinProf` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `preferredLocation` `preferredLocation` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `profile_url` `profile_url` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `resumeLink` `resumeLink` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `source` `source` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `sourceDetails` `sourceDetails` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `twitterProf` `twitterProf` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `sourceName` `sourceName` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `candidate_file` 
CHANGE COLUMN `fileName` `fileName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `filePath` `filePath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `fileType` `fileType` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `candidate_file_audit` 
CHANGE COLUMN `fileName` `fileName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `filePath` `filePath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `fileType` `fileType` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `client` 
CHANGE COLUMN `address` `address` VARCHAR(700) NULL DEFAULT NULL ,
CHANGE COLUMN `clientLocation` `clientLocation` VARCHAR(700) NOT NULL ,
CHANGE COLUMN `clientName` `clientName` VARCHAR(700) NOT NULL ,
CHANGE COLUMN `website` `website` VARCHAR(700) NULL DEFAULT NULL ;

ALTER TABLE `client_audit` 
CHANGE COLUMN `address` `address` VARCHAR(700) NULL DEFAULT NULL ,
CHANGE COLUMN `clientLocation` `clientLocation` VARCHAR(700) NOT NULL ,
CHANGE COLUMN `clientName` `clientName` VARCHAR(700) NOT NULL ,
CHANGE COLUMN `website` `website` VARCHAR(700) NULL DEFAULT NULL ;

ALTER TABLE `feedback` 
CHANGE COLUMN `feedbackBy` `feedbackBy` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `feedbackByName` `feedbackByName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `type` `type` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `eventCreatedBy` `eventCreatedBy` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `clientName` `clientName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionName` `positionName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `roundName` `roundName` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `feedback_audit` 
CHANGE COLUMN `feedbackBy` `feedbackBy` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `feedbackByName` `feedbackByName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `type` `type` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `eventCreatedBy` `eventCreatedBy` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `clientName` `clientName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionName` `positionName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `roundName` `roundName` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `forward_profile` 
CHANGE COLUMN `attachmentLink` `attachmentLink` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `subject` `subject` VARCHAR(1000) NULL DEFAULT NULL ;


ALTER TABLE `interview_schedule` 
CHANGE COLUMN `clientName` `clientName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `roundName` `roundName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `templateName` `templateName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `templateSubject` `templateSubject` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `interview_schedule_audit` 
CHANGE COLUMN `clientName` `clientName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `roundName` `roundName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `templateName` `templateName` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `templateSubject` `templateSubject` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `notification` 
CHANGE COLUMN `message` `message` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `notification_audit` 
CHANGE COLUMN `message` `message` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `organization` 
CHANGE COLUMN `logoUrlPath` `logoUrlPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `websiteUrl` `websiteUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `facebookUrl` `facebookUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `twitterUrl` `twitterUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `googleUrl` `googleUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `linkedInUrl` `linkedInUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `slackUrl` `slackUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `gitHubUrl` `gitHubUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `hipChatUrl` `hipChatUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `bitBucketUrl` `bitBucketUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `organization_audit` 
CHANGE COLUMN `logoUrlPath` `logoUrlPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `websiteUrl` `websiteUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `facebookUrl` `facebookUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `twitterUrl` `twitterUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `googleUrl` `googleUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `linkedInUrl` `linkedInUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `slackUrl` `slackUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `gitHubUrl` `gitHubUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `hipChatUrl` `hipChatUrl` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `bitBucketUrl` `bitBucketUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `position` 
CHANGE COLUMN `jdPath` `jdPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionUrl` `positionUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `position_audit` 
CHANGE COLUMN `jdPath` `jdPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionUrl` `positionUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `position_request` 
CHANGE COLUMN `jdPath` `jdPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionUrl` `positionUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `position_request_audit` 
CHANGE COLUMN `jdPath` `jdPath` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `positionUrl` `positionUrl` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `routemodel` 
CHANGE COLUMN `mailGunRouteId` `mailGunRouteId` VARCHAR(700) NULL DEFAULT NULL ,
CHANGE COLUMN `mailId` `mailId` VARCHAR(700) NULL DEFAULT NULL ,
CHANGE COLUMN `webHookURL` `webHookURL` VARCHAR(700) NULL DEFAULT NULL ;


ALTER TABLE `user` 
CHANGE COLUMN `profile_url` `profile_url` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `timezone` `timezone` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `user_audit` 
CHANGE COLUMN `profile_url` `profile_url` VARCHAR(1000) NULL DEFAULT NULL ,
CHANGE COLUMN `timezone` `timezone` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `user_notification` 
CHANGE COLUMN `details` `details` VARCHAR(1000) NULL DEFAULT NULL ;

ALTER TABLE `user_notification` 
CHANGE COLUMN `details` `details` VARCHAR(1000) NULL DEFAULT NULL ;











