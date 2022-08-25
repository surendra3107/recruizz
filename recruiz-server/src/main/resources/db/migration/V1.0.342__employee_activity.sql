
CREATE TABLE IF NOT EXISTS `employee_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `actionByEmal` varchar(255) DEFAULT NULL,
  `actionByName` varchar(255) DEFAULT NULL,
  `activityType` varchar(255) DEFAULT NULL,
  `employee_id` bigint(20) DEFAULT NULL,
  `message` varchar(1000) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
