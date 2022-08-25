DROP TABLE IF EXISTS `onboarding_templates_tasks`;

CREATE TABLE IF NOT EXISTS `onboarding_templates_tasks` (
  `onboarding_templates_id` bigint(20) NOT NULL,
  `tasks_id` bigint(20) NOT NULL,
  PRIMARY KEY (`onboarding_templates_id`,`tasks_id`),
  KEY `FK49d7alhl2j28695ls2b65io1s` (`tasks_id`),
  CONSTRAINT `FK49d7alhl2j28695ls2b65io1s` FOREIGN KEY (`tasks_id`) REFERENCES `onboarding_details_admin` (`id`),
  CONSTRAINT `FKfyt74ktc6xrht9ayccpjphho6` FOREIGN KEY (`onboarding_templates_id`) REFERENCES `onboarding_templates` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;