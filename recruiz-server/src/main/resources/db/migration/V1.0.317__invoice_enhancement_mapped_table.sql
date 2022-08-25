CREATE TABLE IF NOT EXISTS `agency_invoice_agency_invoice_payment_history` (
  `agency_invoice_id` bigint(20) NOT NULL,
  `agencyInvoicePaymentHistories_id` bigint(20) NOT NULL,
  PRIMARY KEY (`agency_invoice_id`,`agencyInvoicePaymentHistories_id`),
  UNIQUE KEY `UK_sao8jms8i2p0q63a0t9lvntn` (`agencyInvoicePaymentHistories_id`),
  CONSTRAINT `FKhymji9cwixm508armmu8qnq0r` FOREIGN KEY (`agency_invoice_id`) REFERENCES `agency_invoice` (`id`),
  CONSTRAINT `FKil763hruaxdc4tm8uyjcfj3ro` FOREIGN KEY (`agencyInvoicePaymentHistories_id`) REFERENCES `agency_invoice_payment_history` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;