CREATE TABLE IF NOT EXISTS `client_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `clientId_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkakc18b4i5dqjnng5hmbmjjmy` (`clientId_id`),
  CONSTRAINT `FKkakc18b4i5dqjnng5hmbmjjmy` FOREIGN KEY (`clientId_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `client_notes_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `clientId_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKgrc16dkjq7twks79ivfk1lau5` (`REV`),
  CONSTRAINT `FKgrc16dkjq7twks79ivfk1lau5` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `position_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `positionID_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd831uio9onpeapggbnysn7289` (`positionID_id`),
  CONSTRAINT `FKd831uio9onpeapggbnysn7289` FOREIGN KEY (`positionID_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `position_notes_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `positionID_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKd1vsfv0ia9e7yqi4fp5dadyo4` (`REV`),
  CONSTRAINT `FKd1vsfv0ia9e7yqi4fp5dadyo4` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

