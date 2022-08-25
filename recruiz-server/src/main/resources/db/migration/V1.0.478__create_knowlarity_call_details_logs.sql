DROP TABLE IF EXISTS `knowlarity_call_detail_logs`;

CREATE TABLE IF NOT EXISTS `knowlarity_call_detail_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `knowlarityCallDetails_Id` bigint(20) NOT NULL,
  `posting_time` varchar(255) NOT NULL,
  `hangup_time` varchar(255) NOT NULL,
  `outcall_pickup_time` varchar(255) NOT NULL,
  `rec_timetaken` varchar(255) NOT NULL,
  `recordingurl_system` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  `field5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)