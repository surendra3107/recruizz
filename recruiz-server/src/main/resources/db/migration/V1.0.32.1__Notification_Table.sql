CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `actionByEmal` varchar(255) DEFAULT NULL,
  `actionByName` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `notificationEventType` varchar(255) DEFAULT NULL,
  `readState` bit(1) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `notification_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `actionByEmal` varchar(255) DEFAULT NULL,
  `actionByName` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `notificationEventType` varchar(255) DEFAULT NULL,
  `readState` bit(1) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_f6lkys6s2kpj7jtb1gtahswea` (`REV`),
  CONSTRAINT `FK_f6lkys6s2kpj7jtb1gtahswea` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


