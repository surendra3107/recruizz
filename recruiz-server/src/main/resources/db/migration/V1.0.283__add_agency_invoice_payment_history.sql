DROP TABLE IF EXISTS `agency_invoice_payment_history`;
CREATE TABLE IF NOT EXISTS `agency_invoice_payment_history` (
  `historyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `paymentDate` datetime DEFAULT NULL,
  `recivedAmount` double NOT NULL,
  `totalAmount` double NOT NULL,
  `agencyInvoice_invoiceId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`historyId`),
  KEY `FKkwuql01dnc09k9i8ognu0qx3e` (`agencyInvoice_invoiceId`),
  CONSTRAINT `FKkwuql01dnc09k9i8ognu0qx3e` FOREIGN KEY (`agencyInvoice_invoiceId`) REFERENCES `agency_invoice` (`invoiceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;