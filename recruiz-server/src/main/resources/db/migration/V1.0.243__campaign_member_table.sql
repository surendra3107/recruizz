SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `campaign_member`;
DROP TABLE IF EXISTS `campaign_member_action`;


CREATE TABLE IF NOT EXISTS `campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `campaignRenderedTemplate` longtext,
  `clientId` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `campaignSubjectTemplate` longtext,
  `status` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `campaign_candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedToCampaignDate` datetime DEFAULT NULL,
  `campaignRunDate` datetime DEFAULT NULL,
  `campaignRunStatus` bit(1) DEFAULT NULL,
  `memberEmailId` varchar(255) NOT NULL,
  `memberName` varchar(255) DEFAULT NULL,
  `campaign_id` bigint(20) DEFAULT NULL,
  `mailgunEmailId` longtext,
  PRIMARY KEY (`id`),
  KEY `FKbdq3ahvntruhe7dx34cuntt9i` (`campaign_id`),
  CONSTRAINT `FKbdq3ahvntruhe7dx34cuntt9i` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `campaign_candidate_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actionInterval` int(11) NOT NULL,
  `actionResponse` longtext,
  `actionType` varchar(255) DEFAULT NULL,
  `campaignCandidate_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdfn2j6ngro2jngqbedp5k6otf` (`campaignCandidate_id`),
  CONSTRAINT `FKdfn2j6ngro2jngqbedp5k6otf` FOREIGN KEY (`campaignCandidate_id`) REFERENCES `campaign_candidate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `campaign_user` (
  `campaign_id` bigint(20) NOT NULL,
  `campaignHrMembers_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`campaign_id`,`campaignHrMembers_user_id`),
  KEY `FKmavo251580bj2bddscy11taqc` (`campaignHrMembers_user_id`),
  CONSTRAINT `FK73t6e0kck8owyt0tiuy1n38i4` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`),
  CONSTRAINT `FKmavo251580bj2bddscy11taqc` FOREIGN KEY (`campaignHrMembers_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

SET FOREIGN_KEY_CHECKS=1;
