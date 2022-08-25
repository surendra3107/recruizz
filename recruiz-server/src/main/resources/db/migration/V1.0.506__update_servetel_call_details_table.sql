DROP TABLE IF EXISTS `servetel_call_details`;

CREATE TABLE `servetel_call_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `organization_id` varchar(255) NOT NULL,
  `candiateMobile` varchar(255) NOT NULL,
  `agentMobile` varchar(255) NOT NULL,
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