CREATE TABLE IF NOT EXISTS `position_candidate_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `position_id` bigint(20) NOT NULL ,
  `client_id` bigint(20) NOT NULL ,
  `customer_id` bigint(20) NOT NULL ,
  `logged_user_id` bigint(20) NOT NULL ,
  `from_status` varchar(255) DEFAULT NULL,
  `to_status` varchar(255) DEFAULT NULL,
  `from_stage` varchar(255) DEFAULT NULL,
  `to_stage` varchar(255) DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `field1` varchar(255) NOT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;