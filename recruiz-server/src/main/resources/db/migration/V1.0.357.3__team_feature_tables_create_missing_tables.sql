
CREATE TABLE IF NOT EXISTS `user_to_team_member` (
  `user_id` bigint(20) NOT NULL,
  `team_member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`team_member_id`),
  KEY `FKog9hp4qghkbwis3xbucs5gye6` (`team_member_id`),
  CONSTRAINT `FK8qpd27is27plgylyrc6gc80a9` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKog9hp4qghkbwis3xbucs5gye6` FOREIGN KEY (`team_member_id`) REFERENCES `team_member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

