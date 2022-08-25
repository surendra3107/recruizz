SET foreign_key_checks = 0;

DROP TABLE IF EXISTS `onboarding_details`;
DROP TABLE IF EXISTS `onboarding_details_audit`;
DROP TABLE IF EXISTS `onboarding_details_comments`;
DROP TABLE IF EXISTS `onboarding_details_onboarding_details_comments`;


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
  `state` varchar(255) DEFAULT NULL,
  `sub_category_name` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `eid_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq5x4fu2gyhwy2ob1mipg0tlc0` (`eid_id`),
  CONSTRAINT `FKq5x4fu2gyhwy2ob1mipg0tlc0` FOREIGN KEY (`eid_id`) REFERENCES `employee` (`id`)
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
  `state` varchar(255) DEFAULT NULL,
  `sub_category_name` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `eid_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FKq6r9gr2ju9sfpo8ah1xa2kcdc` (`REV`),
  CONSTRAINT `FKq6r9gr2ju9sfpo8ah1xa2kcdc` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `onboarding_details_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `comment` longtext NOT NULL,
  `commented_by` varchar(255) DEFAULT NULL,
  `onBoardingDetails_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqb513tvscr0w9ew8mc3ffnax4` (`onBoardingDetails_id`),
  CONSTRAINT `FKqb513tvscr0w9ew8mc3ffnax4` FOREIGN KEY (`onBoardingDetails_id`) REFERENCES `onboarding_details` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `onboarding_details_onboarding_details_comments` (
  `onboarding_details_id` bigint(20) NOT NULL,
  `comments_id` bigint(20) NOT NULL,
  PRIMARY KEY (`onboarding_details_id`,`comments_id`),
  UNIQUE KEY `UK_n6crv69t50f0jq9mj9xxuglc0` (`comments_id`),
  CONSTRAINT `FK4bf37jnyyj0pvc4c6uisvdod8` FOREIGN KEY (`onboarding_details_id`) REFERENCES `onboarding_details` (`id`),
  CONSTRAINT `FKnnk0o12q95rocif38mwwtey1g` FOREIGN KEY (`comments_id`) REFERENCES `onboarding_details_comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

SET foreign_key_checks = 0;
