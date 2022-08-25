DROP TABLE IF EXISTS `agency_invoice_agency_invoice_payment_history`;
DROP TABLE IF EXISTS `agency_invoice_payment_history`;
DROP TABLE IF EXISTS `candidate_invoice`;
DROP TABLE IF EXISTS `invoice_tax_details`;
DROP TABLE IF EXISTS `invoice_tax_related_details`;
DROP TABLE IF EXISTS `agency_invoice`;
DROP TABLE IF EXISTS `invoice_settings_tax_details`;
DROP TABLE IF EXISTS `invoice_settings_tax_related_details`;
DROP TABLE IF EXISTS `invoice_settings`;
DROP TABLE IF EXISTS `tax`;

CREATE TABLE IF NOT EXISTS `agency_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sub_total` double DEFAULT NULL,
  `bill_city` varchar(255) DEFAULT NULL,
  `bill_client_name` varchar(255) DEFAULT NULL,
  `bill_contact_name` varchar(255) DEFAULT NULL,
  `bill_country` varchar(255) DEFAULT NULL,
  `bill_phone` varchar(255) DEFAULT NULL,
  `bill_pin_code` varchar(255) DEFAULT NULL,
  `bill_state` varchar(255) DEFAULT NULL,
  `bill_address_line_1` longtext,
  `bill_address_line_2` longtext,
  `cheque_payable` varchar(255) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `client_name` varchar(255) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `due_date` datetime DEFAULT NULL,
  `invoice_raised_by` varchar(255) DEFAULT NULL,
  `invoice_id` bigint(20) DEFAULT NULL,
  `invoice_status` varchar(255) DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `note` longtext,
  `org_account_name` varchar(255) DEFAULT NULL,
  `org_account_number` varchar(255) DEFAULT NULL,
  `org_bank_branch_name` varchar(255) DEFAULT NULL,
  `org_bank_ifsc` varchar(255) DEFAULT NULL,
  `org_bank_name` varchar(255) DEFAULT NULL,
  `org_city` varchar(255) DEFAULT NULL,
  `org_country` varchar(255) DEFAULT NULL,
  `org_name` varchar(255) DEFAULT NULL,
  `org_phone` varchar(255) DEFAULT NULL,
  `org_pin_code` varchar(255) DEFAULT NULL,
  `org_state` varchar(255) DEFAULT NULL,
  `org_address_line_1` longtext,
  `org_address_line_2` longtext,
  `payment_received` double DEFAULT NULL,
  `payment_received_date` datetime DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `total_amount_in_words` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9jpaeikgljcrdvxnnvvc3ydet` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `agency_invoice_payment_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `payment_date` datetime DEFAULT NULL,
  `payment_received_by` varchar(255) DEFAULT NULL,
  `received_amount` double DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `candidate_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `candidate_email` varchar(255) DEFAULT NULL,
  `candidate_name` varchar(255) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `client_name` varchar(255) DEFAULT NULL,
  `joining_date` datetime DEFAULT NULL,
  `position_code` varchar(255) DEFAULT NULL,
  `position_name` varchar(255) DEFAULT NULL,
  `agencyInvoice_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpvmwtbxybgava8bj36r6mpgsf` (`agencyInvoice_id`),
  CONSTRAINT `FKpvmwtbxybgava8bj36r6mpgsf` FOREIGN KEY (`agencyInvoice_id`) REFERENCES `agency_invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `invoice_tax_details` (
  `agency_invoice_id` bigint(20) NOT NULL,
  `value` double DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`agency_invoice_id`,`name`),
  CONSTRAINT `FK5bnr8k2brjpeitnri64ec1ix7` FOREIGN KEY (`agency_invoice_id`) REFERENCES `agency_invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `invoice_tax_related_details` (
  `agency_invoice_id` bigint(20) NOT NULL,
  `number` varchar(255) DEFAULT NULL,
  `tax_name` varchar(255) NOT NULL,
  PRIMARY KEY (`agency_invoice_id`,`tax_name`),
  CONSTRAINT `FK7sngdlp3fmwjwduqwoj46bgym` FOREIGN KEY (`agency_invoice_id`) REFERENCES `agency_invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `agency_invoice_agency_invoice_payment_history` (
  `agency_invoice_id` bigint(20) NOT NULL,
  `agencyInvoicePaymentHistories_id` bigint(20) NOT NULL,
  PRIMARY KEY (`agency_invoice_id`,`agencyInvoicePaymentHistories_id`),
  UNIQUE KEY `UK_sao8jms8i2p0q63a0t9lvntn` (`agencyInvoicePaymentHistories_id`),
  CONSTRAINT `FKhymji9cwixm508armmu8qnq0r` FOREIGN KEY (`agency_invoice_id`) REFERENCES `agency_invoice` (`id`),
  CONSTRAINT `FKil763hruaxdc4tm8uyjcfj3ro` FOREIGN KEY (`agencyInvoicePaymentHistories_id`) REFERENCES `agency_invoice_payment_history` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `invoice_settings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `bill_city` varchar(255) DEFAULT NULL,
  `bill_client_name` varchar(255) DEFAULT NULL,
  `bill_contact_name` varchar(255) DEFAULT NULL,
  `bill_country` varchar(255) DEFAULT NULL,
  `bill_phone` varchar(255) DEFAULT NULL,
  `bill_pin_code` varchar(255) DEFAULT NULL,
  `bill_state` varchar(255) DEFAULT NULL,
  `bill_address_1` longtext,
  `bill_address_2` longtext,
  `cheque_payable` varchar(255) DEFAULT NULL,
  `note` longtext,
  `org_account_name` varchar(255) DEFAULT NULL,
  `org_account_number` varchar(255) DEFAULT NULL,
  `org_bank_branch_name` varchar(255) DEFAULT NULL,
  `org_bank_ifsc` varchar(255) DEFAULT NULL,
  `org_bank_name` varchar(255) DEFAULT NULL,
  `org_city` varchar(255) DEFAULT NULL,
  `org_country` varchar(255) DEFAULT NULL,
  `org_name` varchar(255) DEFAULT NULL,
  `org_phone` varchar(255) DEFAULT NULL,
  `org_pin_code` varchar(255) DEFAULT NULL,
  `org_state` varchar(255) DEFAULT NULL,
  `org_address_line_1` longtext,
  `org_address_line_2` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `invoice_settings_tax_details` (
  `invoice_settings_id` bigint(20) NOT NULL,
  `value` double DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`invoice_settings_id`,`name`),
  CONSTRAINT `FKsr8dfvugcjbpd3eruyqf9oshp` FOREIGN KEY (`invoice_settings_id`) REFERENCES `invoice_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `invoice_settings_tax_related_details` (
  `invoice_settings_id` bigint(20) NOT NULL,
  `number` varchar(255) DEFAULT NULL,
  `tax_name` varchar(255) NOT NULL,
  PRIMARY KEY (`invoice_settings_id`,`tax_name`),
  CONSTRAINT `FKaqg5stad1m2kcfw420dy0um0o` FOREIGN KEY (`invoice_settings_id`) REFERENCES `invoice_settings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tax` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `tax_name` varchar(255) NOT NULL,
  `tax_number` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_iwgp0qb43xg9du51wphlbmvhi` (`tax_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
