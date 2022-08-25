CREATE TABLE IF NOT EXISTS `invoice_info` (
  `client_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`client_id`,`name`),
  CONSTRAINT `FKj5w7cvidr18nerrv36cfxw9hy` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `invoice_info_audit` (
  `REV` int(11) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `value` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`client_id`,`value`,`name`),
  CONSTRAINT `FKnqungcdjjbitll4hdenp6t25r` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

