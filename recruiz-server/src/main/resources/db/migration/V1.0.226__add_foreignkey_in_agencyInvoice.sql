DROP TABLE IF EXISTS `agency_invoice`;
DROP TABLE IF EXISTS `candidate_status`;

CREATE TABLE IF NOT EXISTS `candidate_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `candidateId` bigint(20) NOT NULL,
  `clientId` bigint(20) NOT NULL,
  `current` bit(1) DEFAULT NULL,
  `joiningDate` tinyblob,
  `notes` longtext,
  `onBoarded` bit(1) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `statusChangedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `agency_invoice` (
  `invoiceId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `amount` double NOT NULL,
  `candidate_email` varchar(255) DEFAULT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `discount` double NOT NULL,
  `dueDate` datetime DEFAULT NULL,
  `informationFilledByUser` varchar(255) DEFAULT NULL,
  `invoiceStatus` varchar(255) DEFAULT NULL,
  `joiningDate` datetime DEFAULT NULL,
  `offeredDate` datetime DEFAULT NULL,
  `offeredSalary` double NOT NULL,
  `paymentReceived` double NOT NULL,
  `paymentReceivedDate` datetime DEFAULT NULL,
  `postionName` varchar(255) DEFAULT NULL,
  `taxes` double NOT NULL,
  `totalAmount` double NOT NULL,
  `totalAmountInWords` varchar(255) DEFAULT NULL,
  `candidateStatusId_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`invoiceId`),
  KEY `FK6nvjqj933bh0jw8q1ptfa8nb7` (`candidateStatusId_id`),
  CONSTRAINT `FK6nvjqj933bh0jw8q1ptfa8nb7` FOREIGN KEY (`candidateStatusId_id`) REFERENCES `candidate_status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

