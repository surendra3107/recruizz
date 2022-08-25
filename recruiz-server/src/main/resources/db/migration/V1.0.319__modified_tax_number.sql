DROP TABLE IF EXISTS `tax`;

CREATE TABLE IF NOT EXISTS  `tax` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `tax_name` varchar(255) NOT NULL,
  `tax_number` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_iwgp0qb43xg9du51wphlbmvhi` (`tax_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;