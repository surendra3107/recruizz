CREATE TABLE IF NOT EXISTS `custom_field_candidate` (
  `cid` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`cid`,`name`),
  CONSTRAINT `FK3pfjwd7r0aym4qvp6ure9v239` FOREIGN KEY (`cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  IF NOT EXISTS `custom_field_client` (
  `client_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`client_id`,`name`),
  CONSTRAINT `FK912pak32amtgtq1vbyp6tgcyp` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `custom_field_client_audit` (
  `REV` int(11) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`client_id`,`value`,`name`),
  CONSTRAINT `FK9sp0204rt1f8xwcu62iucg99d` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `custom_field_employee` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  CONSTRAINT `FKk4m9322cu8tjjdm552sbh2ukm` FOREIGN KEY (`id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  IF NOT EXISTS `custom_field_employee_audit` (
  `REV` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`id`,`value`,`name`),
  CONSTRAINT `FK613m8uhhl7d60s22tq8l022rf` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE  IF NOT EXISTS `custom_field_position` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  CONSTRAINT `FK9ba5i6dytcrcvmu4g90t2ly8w` FOREIGN KEY (`id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE  IF NOT EXISTS `custom_field_position_audit` (
  `REV` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`id`,`value`,`name`),
  CONSTRAINT `FKdu5s0jex9k88nlms093hnrtkl` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `custom_field_prospect` (
  `prospectId` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`prospectId`,`name`),
  CONSTRAINT `FKo5isqldv5cwldfq5p1ajnnd49` FOREIGN KEY (`prospectId`) REFERENCES `prospect` (`prospectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

