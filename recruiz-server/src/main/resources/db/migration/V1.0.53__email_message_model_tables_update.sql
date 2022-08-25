CREATE TABLE IF NOT EXISTS `email_attachements` (
  `mid` bigint(20) NOT NULL,
  `attachments` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `email_attachements_audit` (
  `REV` int(11) NOT NULL,
  `mid` bigint(20) NOT NULL,
  `attachments` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`mid`,`attachments`),
  CONSTRAINT `FK_rv9gj0lnjhmu2mrw5ymd2xbuf` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `routemodel` (
  `id` varchar(255) NOT NULL,
  `mailGunRouteId` varchar(255) DEFAULT NULL,
  `mailId` varchar(255) DEFAULT NULL,
  `webHookURL` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kj8lpu6646u7gxd6de46bfonm` (`mailGunRouteId`),
  UNIQUE KEY `UK_m4sgb64j5nv5j3buk65xtx8wy` (`mailId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


