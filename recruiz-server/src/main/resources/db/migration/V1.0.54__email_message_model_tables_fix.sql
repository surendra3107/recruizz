
CREATE TABLE IF NOT EXISTS `email_message_model` (
  `mid` bigint(20) NOT NULL AUTO_INCREMENT,
  `cc` varchar(255) DEFAULT NULL,
  `domain` varchar(255) DEFAULT NULL,
  `fromEmail` varchar(255) DEFAULT NULL,
  `htmlBody` varchar(255) DEFAULT NULL,
  `id` varchar(255) DEFAULT NULL,
  `inReplyTo` varchar(255) DEFAULT NULL,
  `messageHeader` varchar(255) DEFAULT NULL,
  `messageId` varchar(255) DEFAULT NULL,
  `messageURL` varchar(255) DEFAULT NULL,
  `recieved` bit(1) NOT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `textBody` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `toEmail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mid`),
  UNIQUE KEY `UK_io09inlxbv28ikoba2qucn5eb` (`messageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email_message_model_audit` (
  `mid` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `cc` varchar(255) DEFAULT NULL,
  `domain` varchar(255) DEFAULT NULL,
  `fromEmail` varchar(255) DEFAULT NULL,
  `htmlBody` varchar(255) DEFAULT NULL,
  `id` varchar(255) DEFAULT NULL,
  `inReplyTo` varchar(255) DEFAULT NULL,
  `messageHeader` varchar(255) DEFAULT NULL,
  `messageId` varchar(255) DEFAULT NULL,
  `messageURL` varchar(255) DEFAULT NULL,
  `recieved` bit(1) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `textBody` varchar(255) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `toEmail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`mid`,`REV`),
  KEY `FK_octnw7dblps1pac3rny07s7t1` (`REV`),
  CONSTRAINT `FK_octnw7dblps1pac3rny07s7t1` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
