CREATE TABLE IF NOT EXISTS `candidate_assesment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `candidateEmailId` varchar(255) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `resultScore` varchar(255) DEFAULT NULL,
  `testId` varchar(255) DEFAULT NULL,
  `totalScore` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
