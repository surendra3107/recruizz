DROP TABLE IF EXISTS `org_bank_info`;
CREATE TABLE IF NOT EXISTS `org_bank_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `accountNumber` varchar(255) NOT NULL,
  `addedBy` varchar(255) NOT NULL,
  `bankName` varchar(255) NOT NULL,
  `branch` varchar(255) DEFAULT NULL,
  `defaultBankDetails` bit(1) DEFAULT NULL,
  `ifsc_code` varchar(255) DEFAULT NULL,
  `accountName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_66hxtw4dttwbfwge42p467e5t` (`accountNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;