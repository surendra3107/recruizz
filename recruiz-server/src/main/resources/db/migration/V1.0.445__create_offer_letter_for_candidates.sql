DROP TABLE IF EXISTS `offer_letter_for_candidates`;

CREATE TABLE IF NOT EXISTS `offer_letter_for_candidates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `offerTemplate_id` bigint(20) DEFAULT NULL,
  `candidate_id` bigint(20) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `text` MEDIUMTEXT DEFAULT NULL,
  `component` MEDIUMTEXT DEFAULT NULL,
  `deduction` MEDIUMTEXT DEFAULT NULL,
  `calculation` MEDIUMTEXT DEFAULT NULL,
  `monthlyGross` varchar(255) DEFAULT NULL,
  `annuallyGross` varchar(255) DEFAULT NULL,
  `annuallyDeductions` varchar(255) DEFAULT NULL,
  `monthlyDeductions` varchar(255) DEFAULT NULL,
  `annuallyCtc` varchar(255) DEFAULT NULL,
  `monthlyCtc` varchar(255) DEFAULT NULL,
  `annuallyDeductionsFormula` MEDIUMTEXT DEFAULT NULL,
  `monthlyDeductionsFormula` MEDIUMTEXT DEFAULT NULL,
  `annuallyCtcFormula` MEDIUMTEXT DEFAULT NULL,
  `monthlyCtcFormula` MEDIUMTEXT DEFAULT NULL,
  `finalOfferLetterPath` varchar(255) DEFAULT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;