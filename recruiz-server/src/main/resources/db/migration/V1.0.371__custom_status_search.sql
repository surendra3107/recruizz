CREATE TABLE IF NOT EXISTS `board_custom_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status_key` varchar(255) NOT NULL,
  `status_value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;