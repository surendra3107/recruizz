CREATE TABLE IF NOT EXISTS `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `team_desc` varchar(800) DEFAULT NULL,
  `team_name` varchar(255) DEFAULT NULL,
  `team_leader` bigint(20) NOT NULL,
  `team_target_amount` double DEFAULT NULL,
  `team_target_amount_currency` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sob22siqdnn2rfsxk6f00pgwb` (`team_name`),
  KEY `FKn4gvg3rcm6970rm1urv6gowr1` (`team_leader`),
  CONSTRAINT `FKn4gvg3rcm6970rm1urv6gowr1` FOREIGN KEY (`team_leader`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `team_members` (
  `team_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`team_id`,`user_id`),
  KEY `FKrk1tw9123clx7w5wjx6b58qch` (`user_id`),
  CONSTRAINT `FKb3toat7ors5scfmd3n69dhmr1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FKrk1tw9123clx7w5wjx6b58qch` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `team_user_target` (
  `user_target_id` bigint(20) NOT NULL,
  `user_target_amount` double DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_target_id`,`user_id`),
  KEY `FKdhxvyldlqt0t9huh7mh1i77nd` (`user_id`),
  CONSTRAINT `FKdhxvyldlqt0t9huh7mh1i77nd` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKjb9rn246l8heq4aqqd54x3rly` FOREIGN KEY (`user_target_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



ALTER TABLE `position` 
ADD COLUMN `team_id` bigint(20) DEFAULT NULL , 
ADD CONSTRAINT `FKqvxrh500irlffxruq1advrytn` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`);
