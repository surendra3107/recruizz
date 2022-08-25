CREATE TABLE IF NOT EXISTS `employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `alternate_email` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `dob` datetime DEFAULT NULL,
  `doj` datetime DEFAULT NULL,
  `emp_id` varchar(255) DEFAULT NULL,
  `employement_status` varchar(255) NOT NULL,
  `employment_type` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `hr_contact` varchar(255) DEFAULT NULL,
  `job_location` varchar(255) DEFAULT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `middle_name` varchar(255) DEFAULT NULL,
  `official_email` varchar(255) NOT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `presonal_email` varchar(255) DEFAULT NULL,
  `primary_contact` varchar(255) DEFAULT NULL,
  `reporting_manager` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `team` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



CREATE TABLE IF NOT EXISTS `employee_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `alternate_email` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `dob` datetime DEFAULT NULL,
  `doj` datetime DEFAULT NULL,
  `emp_id` varchar(255) DEFAULT NULL,
  `employement_status` varchar(255) DEFAULT NULL,
  `employment_type` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `hr_contact` varchar(255) DEFAULT NULL,
  `job_location` varchar(255) DEFAULT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `middle_name` varchar(255) DEFAULT NULL,
  `official_email` varchar(255) DEFAULT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `presonal_email` varchar(255) DEFAULT NULL,
  `primary_contact` varchar(255) DEFAULT NULL,
  `reporting_manager` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `team` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK46ia5ksjyq4c4cl4w3egm3bh0` (`REV`),
  CONSTRAINT `FK46ia5ksjyq4c4cl4w3egm3bh0` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `onboardiing_sub_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `composite_key` varchar(255) NOT NULL,
  `onboard_category` varchar(255) NOT NULL,
  `sub_category_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_aw7li3iibihiuvwyvsklf0een` (`composite_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `onboardiing_sub_category_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `composite_key` varchar(255) DEFAULT NULL,
  `onboard_category` varchar(255) DEFAULT NULL,
  `sub_category_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKkc8cxpmskqodddcu4orboxpds` (`REV`),
  CONSTRAINT `FKkc8cxpmskqodddcu4orboxpds` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `onboarding_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `completed_status` bit(1) DEFAULT NULL,
  `description` longtext NOT NULL,
  `enrolled_people_email` varchar(255) DEFAULT NULL,
  `onboard_category` varchar(255) NOT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `schedule_date` datetime DEFAULT NULL,
  `sub_category_name` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkul97e35up4jjhpetfsqgvr6n` (`candidate_cid`),
  CONSTRAINT `FKkul97e35up4jjhpetfsqgvr6n` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `onboarding_details_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `completed_status` bit(1) DEFAULT NULL,
  `description` longtext,
  `enrolled_people_email` varchar(255) DEFAULT NULL,
  `onboard_category` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `schedule_date` datetime DEFAULT NULL,
  `sub_category_name` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKq6r9gr2ju9sfpo8ah1xa2kcdc` (`REV`),
  CONSTRAINT `FKq6r9gr2ju9sfpo8ah1xa2kcdc` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




