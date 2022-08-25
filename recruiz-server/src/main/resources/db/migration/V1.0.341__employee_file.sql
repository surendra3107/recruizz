CREATE TABLE IF NOT EXISTS `employee_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `employee_id` varchar(255) DEFAULT NULL,
  `file_name` varchar(1000) DEFAULT NULL,
  `file_path` varchar(1000) DEFAULT NULL,
  `file_type` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `employee_employee_file` (
  `employee_id` bigint(20) NOT NULL,
  `files_id` bigint(20) NOT NULL,
  PRIMARY KEY (`employee_id`,`files_id`),
  UNIQUE KEY `UK_ikndd62jruaysotjwdw2w0649` (`files_id`),
  CONSTRAINT `FKkwgrdpvser7sb8xxp7j02pyqs` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `FKo8cutksavi6uur8its3d0m5kd` FOREIGN KEY (`files_id`) REFERENCES `employee_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
