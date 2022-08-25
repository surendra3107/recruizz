DROP TABLE IF EXISTS `folder_user_join`;
DROP TABLE IF EXISTS `candidate_folder`;
DROP TABLE IF EXISTS `position_folder`;
DROP TABLE IF EXISTS `folder`;

CREATE TABLE `folder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `folder_desc` varchar(250) DEFAULT NULL,
  `folder_display_name` varchar(60) NOT NULL,
  `folder_public` bit(1) DEFAULT NULL,
  `folder_type` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9pvn9gnesw53tavomvohh0bfh` (`folder_display_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `folder_user_join` (
  `folder_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  KEY `FKh0phe5l6o4cx1g0j9a6j3tb67` (`user_id`),
  KEY `FKcftio14q4cxs015om86scf5n1` (`folder_id`),
  CONSTRAINT `FKcftio14q4cxs015om86scf5n1` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`),
  CONSTRAINT `FKh0phe5l6o4cx1g0j9a6j3tb67` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



CREATE TABLE `candidate_folder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `added_date_time` datetime DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  `folder_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4242vdiu2505ksfrgdj83k93a` (`candidate_cid`),
  KEY `FKna0xdequv9cjg4fo32oxlh0k8` (`folder_id`),
  CONSTRAINT `FK4242vdiu2505ksfrgdj83k93a` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`),
  CONSTRAINT `FKna0xdequv9cjg4fo32oxlh0k8` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `position_folder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `added_date_time` datetime DEFAULT NULL,
  `folder_id` bigint(20) DEFAULT NULL,
  `position_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6wyvcnjpoumc40rqgteaquq6i` (`folder_id`),
  KEY `FKnusvwk0iyalltxpum80ethcop` (`position_id`),
  CONSTRAINT `FK6wyvcnjpoumc40rqgteaquq6i` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`),
  CONSTRAINT `FKnusvwk0iyalltxpum80ethcop` FOREIGN KEY (`position_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




