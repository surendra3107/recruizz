
CREATE TABLE IF NOT EXISTS `email_template_category_variable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoryName` varchar(255) DEFAULT NULL,
  `categoryValue` varchar(255) DEFAULT NULL,
  `variable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;