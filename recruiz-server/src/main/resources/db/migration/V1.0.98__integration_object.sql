CREATE TABLE `integrationprofiledetails` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `integrationModuleType` varchar(255) DEFAULT NULL,
  `userEmail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `integration_details` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  CONSTRAINT `FK9bch0f65vdi63wtcosnvoviq8` FOREIGN KEY (`id`) REFERENCES `integrationprofiledetails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
