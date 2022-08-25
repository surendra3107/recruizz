CREATE TABLE IF NOT EXISTS `position_request_notes` (
  `PositionRequest_id` bigint(20),
  `positionRequestNotes` longtext,
  KEY `FKqgs70ou09yrb8a23y2kw90d8k` (`PositionRequest_id`),
  CONSTRAINT `FKqgs70ou09yrb8a23y2kw90d8k` FOREIGN KEY (`PositionRequest_id`) REFERENCES `position_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_request_notes_audit` (
  `REV` int(11) NOT NULL,
  `PositionRequest_id` bigint(20),
  `positionRequestNotes` varchar(555),
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`),
  CONSTRAINT `FKcgnhce1bwho4ky6xhk2pa7kwh` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

