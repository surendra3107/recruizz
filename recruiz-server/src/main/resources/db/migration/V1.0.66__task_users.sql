CREATE TABLE IF NOT EXISTS `task_users` (
  `id` bigint(20) NOT NULL,
  `users_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`,`users_user_id`),
  UNIQUE KEY `UK_agoriv21rnlqtvj7orokabwf8` (`users_user_id`),
  CONSTRAINT `FK_7ubth73sdsso0p7ekmc1h6iw1` FOREIGN KEY (`id`) REFERENCES `task_item` (`id`),
  CONSTRAINT `FK_agoriv21rnlqtvj7orokabwf8` FOREIGN KEY (`users_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
