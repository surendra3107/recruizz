ALTER TABLE `feedback` 
ADD COLUMN `ratings` VARCHAR(255) NULL AFTER `round_candidate`;


ALTER TABLE `feedback_audit` 
ADD COLUMN `ratings` VARCHAR(255) NULL AFTER `round_candidate`;


CREATE TABLE IF NOT EXISTS `feedback_reason` (
  `id` bigint(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  KEY `FK_c7t0bnwpnjteesdvb27oj9qmn` (`id`),
  CONSTRAINT `FK_c7t0bnwpnjteesdvb27oj9qmn` FOREIGN KEY (`id`) REFERENCES `feedback` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `feedback_reason_audit` (
  `REV` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`id`,`reason`),
  CONSTRAINT `FK_jfvgl0iy4lolyll0a3813vtbp` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



