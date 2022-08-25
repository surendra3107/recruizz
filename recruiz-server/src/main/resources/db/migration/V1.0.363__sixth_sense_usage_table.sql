DROP TABLE IF EXISTS `sixth_sense_user_usage`;
DROP TABLE IF EXISTS `sixth_sense_resume_view`;

CREATE TABLE `sixth_sense_user_usage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `date_time` datetime NOT NULL,
  `email` varchar(255) NOT NULL,
  `usage_type` varchar(255) DEFAULT NULL,
  `view_count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `sixth_sense_resume_view` (
  `resume_id` varchar(1000) NOT NULL,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `view_on_date` datetime NOT NULL,
  PRIMARY KEY (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;