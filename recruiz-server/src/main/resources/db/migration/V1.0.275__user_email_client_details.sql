CREATE TABLE IF NOT EXISTS `email_client_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `emailClientName` varchar(255) NOT NULL,
  `emailId` varchar(255) NOT NULL,
  `imapServerPort` varchar(255) NOT NULL,
  `imapServerUrl` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `smtpServerPort` varchar(255) NOT NULL,
  `smtpServerUrl` varchar(255) NOT NULL,
  `user_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_63jf7pdoi20cy4d14uiyssqt6` (`emailClientName`),
  UNIQUE KEY `UK_20g5idpfia224t08qkmmwkia3` (`emailId`),
  UNIQUE KEY `UK_hg1ag09jnpxh3rrhxg2wwsedb` (`imapServerPort`),
  UNIQUE KEY `UK_jk8a7b0eejq7oaqslp40u43ii` (`imapServerUrl`),
  UNIQUE KEY `UK_h1i6y8fcql8iwjd1coyga6d04` (`password`),
  UNIQUE KEY `UK_k8iu74wux2bbj5amqlvcrvic9` (`smtpServerPort`),
  UNIQUE KEY `UK_q1gik7xe6wmm6em7b9jsnf0dr` (`smtpServerUrl`),
  KEY `FKjads13wt08b36k0h9k387gl1l` (`user_user_id`),
  CONSTRAINT `FKjads13wt08b36k0h9k387gl1l` FOREIGN KEY (`user_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `user_email_client_details` (
  `user_user_id` bigint(20) NOT NULL,
  `emailDetails_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_qkmiqri7uuvlvtr0glb2o35j1` (`emailDetails_id`),
  KEY `FKt1revkp4c2jwn8dcglc31j15p` (`user_user_id`),
  CONSTRAINT `FK8vfytx1tyv9nkfi0vr3sqcgb0` FOREIGN KEY (`emailDetails_id`) REFERENCES `email_client_details` (`id`),
  CONSTRAINT `FKt1revkp4c2jwn8dcglc31j15p` FOREIGN KEY (`user_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `user_email_client_details_audit` (
  `REV` int(11) NOT NULL,
  `user_user_id` bigint(20) NOT NULL,
  `emailDetails_id` bigint(20) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`user_user_id`,`emailDetails_id`),
  CONSTRAINT `FKldcig7ii9dvtegajftsaqyblx` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;