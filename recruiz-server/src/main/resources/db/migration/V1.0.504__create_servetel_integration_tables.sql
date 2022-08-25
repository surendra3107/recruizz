DROP TABLE IF EXISTS `servetel_integration`;
DROP TABLE IF EXISTS `servetel_agent`;
DROP TABLE IF EXISTS `servetel_call_details`;

CREATE TABLE `servetel_integration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `organization_id` varchar(255) NOT NULL,
  `login_id` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `productId` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `servetel_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `organization_id` varchar(255) NOT NULL,
  `agentName` varchar(255) NOT NULL,
  `mobile` varchar(255) NOT NULL,
  `agentId` varchar(255) DEFAULT NULL,
  `userId` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `servetel_call_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `organization_id` varchar(255) NOT NULL,
  `candiateMobile` varchar(255) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `candidateId` bigint(20) NOT NULL,
  `agentId` bigint(20) NOT NULL,
  `recordingUrl` varchar(255) NOT NULL,
  `callStatus` varchar(255) NOT NULL,
  `duration` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=latin1;