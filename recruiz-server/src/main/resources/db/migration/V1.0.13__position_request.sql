CREATE TABLE  IF NOT EXISTS `position_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `closeByDate` datetime NOT NULL,
  `description` longtext NOT NULL,
  `experienceRange` varchar(255) NOT NULL,
  `functionalArea` varchar(255) NOT NULL,
  `industry` varchar(255) NOT NULL,
  `jdPath` varchar(255) DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `maxSal` double NOT NULL,
  `minSal` double NOT NULL,
  `notes` longtext,
  `openedDate` datetime NOT NULL,
  `positionUrl` varchar(255) DEFAULT NULL,
  `remoteWork` bit(1) NOT NULL,
  `requested_by_email` varchar(255) DEFAULT NULL,
  `requested_by_name` varchar(255) DEFAULT NULL,
  `requested_by_phone` varchar(255) DEFAULT NULL,
  `salUnit` varchar(255) DEFAULT NULL,
  `status` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `totalPosition` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_request_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `closeByDate` datetime DEFAULT NULL,
  `description` longtext,
  `experienceRange` varchar(255) DEFAULT NULL,
  `functionalArea` varchar(255) DEFAULT NULL,
  `industry` varchar(255) DEFAULT NULL,
  `jdPath` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `maxSal` double DEFAULT NULL,
  `minSal` double DEFAULT NULL,
  `notes` longtext,
  `openedDate` datetime DEFAULT NULL,
  `positionUrl` varchar(255) DEFAULT NULL,
  `remoteWork` bit(1) DEFAULT NULL,
  `requested_by_email` varchar(255) DEFAULT NULL,
  `requested_by_name` varchar(255) DEFAULT NULL,
  `requested_by_phone` varchar(255) DEFAULT NULL,
  `salUnit` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `totalPosition` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_1nxlg92gcphi1g6cq3nr1x087` (`REV`),
  CONSTRAINT `FK_1nxlg92gcphi1g6cq3nr1x087` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_request_educationa_qualification` (
  `PositionRequest_id` bigint(20) NOT NULL,
  `educationalQualification` varchar(255) DEFAULT NULL,
  KEY `FK_83fd2uxv8q6qr7ti6hhksja9r` (`PositionRequest_id`),
  CONSTRAINT `FK_83fd2uxv8q6qr7ti6hhksja9r` FOREIGN KEY (`PositionRequest_id`) REFERENCES `position_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_request_educationa_qualification_audit` (
  `REV` int(11) NOT NULL,
  `PositionRequest_id` bigint(20) NOT NULL,
  `educationalQualification` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`PositionRequest_id`,`educationalQualification`),
  CONSTRAINT `FK_1amrioh7sfel1ijxjlwywn4ld` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `position_request_good_skill_set` (
  `PositionRequest_id` bigint(20) NOT NULL,
  `goodSkillSet` varchar(255) DEFAULT NULL,
  KEY `FK_hoowyog31dilnl7pg4lv11fhd` (`PositionRequest_id`),
  CONSTRAINT `FK_hoowyog31dilnl7pg4lv11fhd` FOREIGN KEY (`PositionRequest_id`) REFERENCES `position_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `position_request_good_skill_set_audit` (
  `REV` int(11) NOT NULL,
  `PositionRequest_id` bigint(20) NOT NULL,
  `goodSkillSet` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`PositionRequest_id`,`goodSkillSet`),
  CONSTRAINT `FK_oi57qevylp1b3lpn6k1a3noqm` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_request_req_skill_set` (
  `PositionRequest_id` bigint(20) NOT NULL,
  `reqSkillSet` varchar(255) DEFAULT NULL,
  KEY `FK_9u6ic31usvt3b34y6oiyvbjw6` (`PositionRequest_id`),
  CONSTRAINT `FK_9u6ic31usvt3b34y6oiyvbjw6` FOREIGN KEY (`PositionRequest_id`) REFERENCES `position_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `position_request_req_skill_set_audit` (
  `REV` int(11) NOT NULL,
  `PositionRequest_id` bigint(20) NOT NULL,
  `reqSkillSet` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`PositionRequest_id`,`reqSkillSet`),
  CONSTRAINT `FK_b4lssghpu6x65k3419x693kl` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

