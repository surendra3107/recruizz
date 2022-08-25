
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `prospect`;
DROP TABLE IF EXISTS `prospectcontactinfo`;
DROP TABLE IF EXISTS `prospectactivity`;
DROP TABLE IF EXISTS `prospectnotes`;
DROP TABLE IF EXISTS `prospectnotes_audit`;

SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE IF NOT EXISTS `prospect` (
  `prospectId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `companyName` varchar(1000) NOT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `owner` varchar(255) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`prospectId`),
  UNIQUE KEY `UK_gxrtwwwtsnssxgdpmkhruijab` (`email`),
  UNIQUE KEY `UK_cjqdi48fakcm4wmnidd7n95j8` (`mobile`),
  UNIQUE KEY `UK_hwk4a7mtk4edtg8ipatrs9rgw` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `prospect_contact_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(1000) DEFAULT NULL,
  `prospect_prospectId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK683w4rmsybtxp60petngw64on` (`prospect_prospectId`),
  CONSTRAINT `FK683w4rmsybtxp60petngw64on` FOREIGN KEY (`prospect_prospectId`) REFERENCES `prospect` (`prospectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `prospect_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `prospectId` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `what` text,
  `whatTime` datetime DEFAULT NULL,
  `who` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `prospect_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `prospectId_prospectId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3fap36o01ume73gi6yg5agtad` (`prospectId_prospectId`),
  CONSTRAINT `FK3fap36o01ume73gi6yg5agtad` FOREIGN KEY (`prospectId_prospectId`) REFERENCES `prospect` (`prospectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `prospect_notes_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `addedBy_MOD` bit(1) DEFAULT NULL,
  `notes` longtext,
  `notes_MOD` bit(1) DEFAULT NULL,
  `prospectId_prospectId` bigint(20) DEFAULT NULL,
  `prospectId_MOD` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKjsus8r93ne5bmfyg30rwe1j78` (`REV`),
  CONSTRAINT `FKjsus8r93ne5bmfyg30rwe1j78` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;