CREATE TABLE IF NOT EXISTS `client_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `actionByEmal` varchar(255) DEFAULT NULL,
  `actionByName` varchar(255) DEFAULT NULL,
  `activityType` varchar(255) DEFAULT NULL,
  `clientId` bigint(20) DEFAULT NULL,
  `message` varchar(1000) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `position_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `actionByEmal` varchar(255) DEFAULT NULL,
  `actionByName` varchar(255) DEFAULT NULL,
  `activityType` varchar(255) DEFAULT NULL,
  `candidateId` bigint(20) DEFAULT NULL,
  `clientId` bigint(20) DEFAULT NULL,
  `interviewScheduleId` bigint(20) DEFAULT NULL,
  `message` varchar(1000) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `requestedPositionId` bigint(20) DEFAULT NULL,
  `roundCandidateId` bigint(20) DEFAULT NULL,
  `roundId` bigint(20) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
