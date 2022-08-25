
CREATE TABLE IF NOT EXISTS `candidate_resume_bulk_upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `batch_id` varchar(255) NOT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `upload_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bte79759a6gwgx6a9ogfs38c` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `candidate_resume_upload_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` varchar(255) NOT NULL,
  `failed_reason` longtext,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(3000) NOT NULL,
  `file_size_in_kb` double DEFAULT NULL,
  `process_date_time` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `candidateResumeBulkUpload_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpqyh4cl1096ai8nuxiiketalv` (`candidateResumeBulkUpload_id`),
  CONSTRAINT `FKpqyh4cl1096ai8nuxiiketalv` FOREIGN KEY (`candidateResumeBulkUpload_id`) REFERENCES `candidate_resume_bulk_upload` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



