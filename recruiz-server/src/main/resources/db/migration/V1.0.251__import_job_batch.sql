CREATE TABLE IF NOT EXISTS `import_job_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `batch_id` varchar(255) NOT NULL,
  `failed_row_count` bigint(20) DEFAULT NULL,
  `import_type` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `success_row_count` bigint(20) DEFAULT NULL,
  `total_row_count` bigint(20) DEFAULT NULL,
  `upload_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_41v5l55mbh9gg355wwm59yd7j` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `import_job_upload_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `batch_id` varchar(255) NOT NULL,
  `failed_reason` longtext,
  `identifier` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `process_date_time` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `importJobBatch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtqdgur2chp72rim0erm7y2kq0` (`importJobBatch_id`),
  CONSTRAINT `FKtqdgur2chp72rim0erm7y2kq0` FOREIGN KEY (`importJobBatch_id`) REFERENCES `import_job_batch` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;