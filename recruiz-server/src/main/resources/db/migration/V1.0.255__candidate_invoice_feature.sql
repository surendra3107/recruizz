
CREATE TABLE IF NOT EXISTS `candidate_invoice` (
  `candidateInvoiceId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `candidate_email` varchar(255) DEFAULT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `joiningDate` datetime DEFAULT NULL,
  `offeredDate` datetime DEFAULT NULL,
  `offeredSalary` double NOT NULL,
  `postion_code` varchar(255) DEFAULT NULL,
  `postionName` varchar(255) DEFAULT NULL,
  `agencyInvoice_invoiceId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`candidateInvoiceId`),
  KEY `FK3435ukyb5nqnp66xdkq1qk0h4` (`agencyInvoice_invoiceId`),
  CONSTRAINT `FK3435ukyb5nqnp66xdkq1qk0h4` FOREIGN KEY (`agencyInvoice_invoiceId`) REFERENCES `agency_invoice` (`invoiceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
