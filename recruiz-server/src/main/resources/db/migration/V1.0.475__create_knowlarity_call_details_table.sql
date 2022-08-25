DROP TABLE IF EXISTS `knowlarity_call_details`;

CREATE TABLE IF NOT EXISTS `knowlarity_call_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `candidateId` bigint(20) NOT NULL,
  `candidateName` varchar(255) NOT NULL,
  `candidateEmail` varchar(255) NOT NULL,
  `candidateMobile` varchar(255) NOT NULL,
  `agentMobile` varchar(255) NOT NULL,
  `agentEmail` varchar(255) NOT NULL,
  `call_id` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)