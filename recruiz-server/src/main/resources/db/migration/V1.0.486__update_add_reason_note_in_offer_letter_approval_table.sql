DROP TABLE IF EXISTS `offerletter_approvals`;

CREATE TABLE `offerletter_approvals` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `client_id` bigint(20) NOT NULL,
  `position_id` bigint(20) NOT NULL,
  `position_code` varchar(255) NOT NULL,
  `candidate_id` bigint(20) NOT NULL,
  `workflow_id` bigint(20) NOT NULL,
  `request_send_from_user` bigint(20) NOT NULL,
  `approval_status` varchar(255) NOT NULL,
  `reject_reason` varchar(2000) NOT NULL,
  `deliveryLead` varchar(255) DEFAULT NULL,
  `recruitersName` varchar(255) DEFAULT NULL,
  `accountManager` varchar(255) DEFAULT NULL,
  `client` varchar(255) DEFAULT NULL,
  `clientPoC` varchar(255) DEFAULT NULL,
  `clientPhone` varchar(255) DEFAULT NULL,
  `clientEmail` varchar(255) DEFAULT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `dateOfOffer` datetime DEFAULT NULL,
  `experience` varchar(255) DEFAULT NULL,
  `doj` datetime DEFAULT NULL,
  `billingDate` datetime DEFAULT NULL,
  `compensationAnnual` varchar(255) DEFAULT NULL,
  `projectDurationMonths` varchar(255) DEFAULT NULL,
  `billingHours` varchar(255) DEFAULT NULL,
  `billRatePerHour` varchar(255) DEFAULT NULL,
  `subConCompensation` varchar(255) DEFAULT NULL,
  `serviceTax` varchar(255) DEFAULT NULL,
  `oneTimeCost` varchar(255) DEFAULT NULL,
  `relocationCost` varchar(255) DEFAULT NULL,
  `noticePeriodCost` varchar(255) DEFAULT NULL,
  `joiningBonusCost` varchar(255) DEFAULT NULL,
  `headHunting` varchar(255) DEFAULT NULL,
  `otherAssociatedCost` varchar(255) DEFAULT NULL,
  `monthlyCost` varchar(255) DEFAULT NULL,
  `costProjectDuration` varchar(255) DEFAULT NULL,
  `billingMonthly` varchar(255) DEFAULT NULL,
  `billingProjectDuration` varchar(255) DEFAULT NULL,
  `monthlyDirectCost` varchar(255) DEFAULT NULL,
  `annualDirectCost` varchar(255) DEFAULT NULL,
  `montlyGrossMargin` varchar(255) DEFAULT NULL,
  `annualGorssMargin` varchar(255) DEFAULT NULL,
  `grossMarginPercentage` varchar(255) DEFAULT NULL,
  `approvedDate` datetime DEFAULT NULL,
  `approvedBy` varchar(255) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `offerStatus` varchar(255) DEFAULT NULL,
  `iSJoinedOn` datetime DEFAULT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)