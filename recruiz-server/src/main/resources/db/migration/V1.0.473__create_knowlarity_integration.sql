DROP TABLE IF EXISTS `knowlarity_integration`;

CREATE TABLE IF NOT EXISTS `knowlarity_integration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `organization_id` varchar(255) NOT NULL,
  `sr_number` varchar(255) NOT NULL,
  `caller_id` varchar(255) NOT NULL,
  `authorization_key` varchar(255) NOT NULL,
  `xApi_key` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)