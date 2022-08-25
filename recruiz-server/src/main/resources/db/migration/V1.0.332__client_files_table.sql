CREATE TABLE IF NOT EXISTS `client_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `file_name` varchar(1000) DEFAULT NULL,
  `file_path` varchar(1000) DEFAULT NULL,
  `file_type` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `client_client_file` (
  `client_id` bigint(20) NOT NULL,
  `files_id` bigint(20) NOT NULL,
  PRIMARY KEY (`client_id`,`files_id`),
  UNIQUE KEY `UK_eg543v8ks7lmmrrw3yv47h6bw` (`files_id`),
  CONSTRAINT `FKc0vbmgifak188384sqr7w77ud` FOREIGN KEY (`files_id`) REFERENCES `client_file` (`id`),
  CONSTRAINT `FKgg3rd0telx2q6680e9uyafaoh` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;