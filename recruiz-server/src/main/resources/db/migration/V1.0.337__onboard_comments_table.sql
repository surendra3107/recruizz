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
