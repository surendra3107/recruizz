DROP TABLE IF EXISTS `position_offer_cost`;

CREATE TABLE IF NOT EXISTS `position_offer_cost` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `position_id` bigint(20) NOT NULL,
  `billRate` varchar(255) NOT NULL,
  `billHours` varchar(255) NOT NULL,
  `billingDate` datetime NOT NULL,
  `projectDuration` varchar(255) NOT NULL,
  `oneTimeCost` varchar(255) NOT NULL,
  `headHunting` varchar(255) NOT NULL,
  `status` bit(1) DEFAULT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;