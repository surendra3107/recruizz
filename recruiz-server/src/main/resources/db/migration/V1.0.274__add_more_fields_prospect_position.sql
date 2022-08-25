DROP TABLE IF EXISTS `prospect_position_key_skills`;
DROP TABLE IF EXISTS `prospect_position_location`;
DROP TABLE IF EXISTS `prospect_position_education_qualification`;
DROP TABLE IF EXISTS `prospect_position`;
CREATE TABLE IF NOT EXISTS `prospect_position` (
  `positionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `closureDate` datetime NOT NULL,
  `maxExperience` double NOT NULL,
  `minExperience` double NOT NULL,
  `number_of_openings` int(11) NOT NULL,
  `percentage` double DEFAULT NULL,
  `positionName` varchar(255) NOT NULL,
  `value` double DEFAULT NULL,
  `prospect_prospectId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`positionId`),
  KEY `FK9mi5w066oulf54mxo7rk0hip1` (`prospect_prospectId`),
  CONSTRAINT `FK9mi5w066oulf54mxo7rk0hip1` FOREIGN KEY (`prospect_prospectId`) REFERENCES `prospect` (`prospectId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `prospect_position_key_skills` (
  `prospect_position_positionId` bigint(20) NOT NULL,
  `keySkills` varchar(255) DEFAULT NULL,
  KEY `FKdrpvb8qxvbp41237xosjrjvu4` (`prospect_position_positionId`),
  CONSTRAINT `FKdrpvb8qxvbp41237xosjrjvu4` FOREIGN KEY (`prospect_position_positionId`) REFERENCES `prospect_position` (`positionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `prospect_position_location` (
  `prospect_position_positionId` bigint(20) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  KEY `FKjohogu2w00na1lxiibirjq7qj` (`prospect_position_positionId`),
  CONSTRAINT `FKjohogu2w00na1lxiibirjq7qj` FOREIGN KEY (`prospect_position_positionId`) REFERENCES `prospect_position` (`positionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `prospect_position_education_qualification` (
  `prospect_position_positionId` bigint(20) NOT NULL,
  `educationQualification` varchar(255) DEFAULT NULL,
  KEY `FK7ddix58533hkk1784uk8l6t5k` (`prospect_position_positionId`),
  CONSTRAINT `FK7ddix58533hkk1784uk8l6t5k` FOREIGN KEY (`prospect_position_positionId`) REFERENCES `prospect_position` (`positionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

