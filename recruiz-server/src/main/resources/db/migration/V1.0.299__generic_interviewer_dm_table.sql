CREATE TABLE IF NOT EXISTS `generic_decisionmaker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `mobile` varchar(255) NOT NULL,
  `name` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kr8j1brgqmgyboypp7ofjcrxn` (`email`),
  UNIQUE KEY `UK_his9p1huim4lw7s9yujiw731m` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `generic_decisionmaker_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKchl758jtscdfnig02jvky2ice` (`REV`),
  CONSTRAINT `FKchl758jtscdfnig02jvky2ice` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `generic_interviewer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `mobile` varchar(255) NOT NULL,
  `name` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_oh02nhae5xu5u355dxel6pehw` (`email`),
  UNIQUE KEY `UK_rdhri359n4n1v6mnqkxfamfnl` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `generic_interviewer_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKnhbgi28jgi9x53hbb3kj7gyv3` (`REV`),
  CONSTRAINT `FKnhbgi28jgi9x53hbb3kj7gyv3` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



