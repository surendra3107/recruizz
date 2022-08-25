CREATE TABLE IF NOT EXISTS `onboarding_details_admin` (
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
