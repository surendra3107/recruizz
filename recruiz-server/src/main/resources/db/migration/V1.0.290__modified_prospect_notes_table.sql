DROP TABLE IF EXISTS `prospect_notes`;
CREATE TABLE IF NOT EXISTS `prospect_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `prospect_prospectId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq36erbsde03f5htwesepowjxi` (`prospect_prospectId`),
  CONSTRAINT `FKq36erbsde03f5htwesepowjxi` FOREIGN KEY (`prospect_prospectId`) REFERENCES `prospect` (`prospectId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `prospect_notes_audit`;
CREATE TABLE IF NOT EXISTS `prospect_notes_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `addedBy_MOD` bit(1) DEFAULT NULL,
  `notes` longtext,
  `notes_MOD` bit(1) DEFAULT NULL,
  `prospect_prospectId` bigint(20) DEFAULT NULL,
  `prospect_MOD` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK4avayo85g3mk8tnr38hbwnwnn` (`REV`),
  CONSTRAINT `FK4avayo85g3mk8tnr38hbwnwnn` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;