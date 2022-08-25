DROP TABLE IF EXISTS `org_tax_details`;
CREATE TABLE IF NOT EXISTS `org_tax_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `tax_name` varchar(255) DEFAULT NULL,
  `tax_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ORG_TAX_DETAILS_IN_TAX_NAME` (`tax_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;