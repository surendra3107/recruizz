DROP TABLE IF EXISTS `sending_email_id_list`;

CREATE TABLE IF NOT EXISTS `sending_email_id_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email_id` varchar(255) NOT NULL,
  `list_type` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
)