DROP TABLE IF EXISTS `prospect_position`;
DROP TABLE IF EXISTS `prospect_position_key_skills`;

CREATE TABLE IF NOT EXISTS `prospect_position` (
  `positionId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `percentage` double NOT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `value` double NOT NULL,
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