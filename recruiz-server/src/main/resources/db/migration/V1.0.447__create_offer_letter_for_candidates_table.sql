DROP TABLE IF EXISTS `offer_letter_for_candidates`;

CREATE TABLE IF NOT EXISTS `offer_letter_for_candidates` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `offerTemplate_id` BIGINT(20) DEFAULT NULL,
  `candidate_id` BIGINT(20) DEFAULT NULL,
  `creation_date` DATETIME DEFAULT NULL,
  `modification_date` DATETIME DEFAULT NULL,
  `textValues` MEDIUMTEXT DEFAULT NULL,
  `annuallyDeductionValues` MEDIUMTEXT DEFAULT NULL,
  `monthlyDeductionValues` MEDIUMTEXT DEFAULT NULL,
  `annuallyCtcValues` MEDIUMTEXT DEFAULT NULL,
  `monthlyCtcValues` MEDIUMTEXT DEFAULT NULL,
  `monthlyGrossTotal` VARCHAR(255) DEFAULT NULL,
  `annuallyGrossTotal` VARCHAR(255) DEFAULT NULL,
  `annuallyDeductionsTotal` MEDIUMTEXT DEFAULT NULL,
  `monthlyDeductionsTotal` MEDIUMTEXT DEFAULT NULL,
  `annuallyCtcTotal` MEDIUMTEXT DEFAULT NULL,
  `monthlyCtcTotal` MEDIUMTEXT DEFAULT NULL,
  `finalOfferLetterPath` VARCHAR(255) DEFAULT NULL,
  `field1` VARCHAR(255) DEFAULT NULL,
  `field2` VARCHAR(255) DEFAULT NULL,
  `field3` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;