DROP TABLE IF EXISTS `selected_custom_reports`;

CREATE TABLE IF NOT EXISTS `selected_custom_reports` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `creation_date` DATETIME DEFAULT NULL,
  `modification_date` DATETIME DEFAULT NULL,
  `textValues` MEDIUMTEXT DEFAULT NULL,
  `field1` VARCHAR(255) DEFAULT NULL,
  `field2` VARCHAR(255) DEFAULT NULL,
  `field3` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;