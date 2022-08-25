CREATE TABLE IF NOT EXISTS `campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `campaignRenderedTemplate` longtext,
  `clientId` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `positionCode` bigint(20) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `campaign_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedToCampaignDate` datetime DEFAULT NULL,
  `campaignRunDate` datetime DEFAULT NULL,
  `campaignRunStatus` bit(1) DEFAULT NULL,
  `memberEmailId` varchar(255) NOT NULL,
  `campaign_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKchylg7emrxfbi6i5lv3iiustd` (`campaign_id`),
  CONSTRAINT `FKchylg7emrxfbi6i5lv3iiustd` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `campaign_member_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actionInterval` int(11) NOT NULL,
  `actionResponse` longtext,
  `actionType` varchar(255) DEFAULT NULL,
  `campaignMember_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnbpv5fcnydthmj1xcvy5jc7pw` (`campaignMember_id`),
  CONSTRAINT `FKnbpv5fcnydthmj1xcvy5jc7pw` FOREIGN KEY (`campaignMember_id`) REFERENCES `campaign_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


