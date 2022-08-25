DROP TABLE IF EXISTS `offerletter_approvals`;

CREATE TABLE IF NOT EXISTS `offerletter_approvals` (
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
  `current_ctc` varchar(255) NOT NULL,
  `percentage_hike` varchar(255) NOT NULL,
  `ctc_offered` varchar(255) NOT NULL,
  `profit_margin` varchar(255) NOT NULL,
  `field1` varchar(255) DEFAULT NULL,
  `field2` varchar(255) DEFAULT NULL,
  `field3` varchar(255) DEFAULT NULL,
  `field4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;