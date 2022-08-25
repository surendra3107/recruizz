
DROP TABLE IF EXISTS `sixth_sense_city`;
DROP TABLE IF EXISTS `sixth_sense_functional_area`;
DROP TABLE IF EXISTS `sixth_sense_functional_area_role`;
DROP TABLE IF EXISTS `sixth_sense_industry`;
DROP TABLE IF EXISTS `sixth_sense_pg_degree`;
DROP TABLE IF EXISTS `sixth_sense_pg_degree_specialization`;
DROP TABLE IF EXISTS `sixth_sense_ppg_degree`;
DROP TABLE IF EXISTS `sixth_sense_ppg_degree_specialization`;
DROP TABLE IF EXISTS `sixth_sense_ug_degree`;
DROP TABLE IF EXISTS `sixth_sense_ug_degree_specialization`;
DROP TABLE IF EXISTS `external_app_user`;

CREATE TABLE `sixth_sense_city` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `group_label` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_functional_area` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_functional_area_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `group_label` varchar(500) DEFAULT NULL,
  `functional_area_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `sixth_sense_industry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_pg_degree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_pg_degree_specialization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `group_label` varchar(500) DEFAULT NULL,
  `degree_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_ppg_degree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_ppg_degree_specialization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `group_label` varchar(500) DEFAULT NULL,
  `degree_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_ug_degree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `sixth_sense_ug_degree_specialization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `group_label` varchar(500) DEFAULT NULL,
  `degree_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `external_app_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8so1kv0snpfbm4v0oq5ocf41v` (`email`),
  UNIQUE KEY `UK_huuey3uvpkmuu3387q0ld9sk6` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;