CREATE TABLE IF NOT EXISTS `task_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `dueAt` datetime DEFAULT NULL,
  `notes` longtext,
  `startAt` datetime DEFAULT NULL,
  `taskCreaterEmail` varchar(255) DEFAULT NULL,
  `taskCreaterName` varchar(255) DEFAULT NULL,
  `taskEventId` varchar(255) DEFAULT NULL,
  `taskNoteContent` longtext,
  `taskSubject` longtext,
  `templateName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `task_schedule_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `dueAt` datetime DEFAULT NULL,
  `notes` longtext,
  `startAt` datetime DEFAULT NULL,
  `taskCreaterEmail` varchar(255) DEFAULT NULL,
  `taskCreaterName` varchar(255) DEFAULT NULL,
  `taskEventId` varchar(255) DEFAULT NULL,
  `taskNoteContent` longtext,
  `taskSubject` longtext,
  `templateName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_nwiinc01hiruqcxbxljo7kynf` (`REV`),
  CONSTRAINT `FK_nwiinc01hiruqcxbxljo7kynf` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `task_schedule_event_attendee` (
  `task_schedule_id` bigint(20) NOT NULL,
  `attendee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`task_schedule_id`,`attendee_id`),
  UNIQUE KEY `UK_tcqc643vj4jh11pxvxd8ud14n` (`attendee_id`),
  CONSTRAINT `FK_54xi9pldivkig49h4kfg9w9iy` FOREIGN KEY (`task_schedule_id`) REFERENCES `task_schedule` (`id`),
  CONSTRAINT `FK_tcqc643vj4jh11pxvxd8ud14n` FOREIGN KEY (`attendee_id`) REFERENCES `event_attendee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `task_schedule_event_attendee_audit` (
  `REV` int(11) NOT NULL,
  `task_schedule_id` bigint(20) NOT NULL,
  `attendee_id` bigint(20) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`task_schedule_id`,`attendee_id`),
  CONSTRAINT `FK_j2eymknldcxamnosc5shc7t7n` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


