DROP TABLE IF EXISTS `offer_letter_workflow`;

CREATE TABLE IF NOT EXISTS `offer_letter_workflow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `workflow_name` varchar(255) NOT NULL,
  `lower_margin` varchar(255) NOT NULL,
  `upper_margin` varchar(255) NOT NULL,
  `lower_margin_operator` varchar(255) NOT NULL,
  `upper_margin_operator` varchar(255) NOT NULL,
  `approver_name` varchar(255) NOT NULL,
  `approver_email` varchar(255) NOT NULL,
  `other_loop` varchar(255) DEFAULT NULL,
  `other_loop_name` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;