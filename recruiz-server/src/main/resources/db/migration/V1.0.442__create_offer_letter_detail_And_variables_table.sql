DROP TABLE IF EXISTS `offer_letter_template_variables`;
DROP TABLE IF EXISTS `offer_letter_details`;

CREATE TABLE IF NOT EXISTS `offer_letter_template_variables` (
  `variableId` bigint(20) NOT NULL AUTO_INCREMENT,
  `variableName` varchar(255) DEFAULT NULL,
  `startTag` varchar(255) DEFAULT NULL,
  `endTag` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`variableId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('text','$#','#$');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('component','#$','$#');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('deduction','$$','$$');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('calculation','##','##');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('monthlyGross','<','>');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('annuallyGross','#<','>#');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('annuallyDeductions','#(',')#');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('monthlyDeductions','(',')');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('annuallyCtc','#[',']#');
INSERT INTO offer_letter_template_variables (variableName,startTag,endTag) VALUES ('monthlyCtc','[',']');



CREATE TABLE IF NOT EXISTS `offer_letter_details` (
  `offerLetterId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `selected_status` bit(1) NOT NULL DEFAULT b'0',
  `templateName` VARCHAR(255) NOT NULL UNIQUE,
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
  `templatePath` varchar(255) DEFAULT NULL,
  `finalOfferLetterPath` varchar(255) DEFAULT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`offerLetterId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;