DROP TABLE IF EXISTS `sixth_sense_user`;

CREATE TABLE `sixth_sense_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_40fvvy071dnqy9tywk6ei7f5qq` (`user_name`),
  KEY `FKi5bjod37227qitjg5pvstutas1` (`user_id`),
  CONSTRAINT `FKi5bjod37227qitjg5pvstutas1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;