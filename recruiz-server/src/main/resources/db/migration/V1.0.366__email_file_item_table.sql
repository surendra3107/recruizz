CREATE TABLE IF NOT EXISTS `email_file_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email_id` varchar(255) NOT NULL,
  `email_uid` varchar(255) NOT NULL,
  `failed_reason` longtext,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(3000) NOT NULL,
  `file_size_in_kb` double DEFAULT NULL,
  `file_system` varchar(255) DEFAULT NULL,
  `process_date_time` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `email_subject` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
