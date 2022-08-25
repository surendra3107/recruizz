SET foreign_key_checks = 0;

CREATE TABLE IF NOT EXISTS `onboarding_templates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `name` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `onboarding_templates_tasks` (
  `onboarding_templates_id` bigint(20) NOT NULL,
  `tasks_id` bigint(20) NOT NULL,
  PRIMARY KEY (`onboarding_templates_id`,`tasks_id`),
  UNIQUE KEY `UK_hymvgqebsxtvas51l8avrgrt9` (`tasks_id`),
  CONSTRAINT `FK49d7alhl2j28695ls2b65io1s` FOREIGN KEY (`tasks_id`) REFERENCES `onboarding_details_admin` (`id`),
  CONSTRAINT `FKfyt74ktc6xrht9ayccpjphho6` FOREIGN KEY (`onboarding_templates_id`) REFERENCES `onboarding_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


SET foreign_key_checks = 1;
