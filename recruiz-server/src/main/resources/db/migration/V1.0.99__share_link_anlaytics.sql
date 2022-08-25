CREATE TABLE `sharedlinkanalytics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `eventType` varchar(255) DEFAULT NULL,
  `linkFrom` varchar(255) DEFAULT NULL,
  `platform` varchar(255) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
