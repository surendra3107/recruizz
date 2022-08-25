DROP TABLE IF EXISTS `candidate_rating`;
DROP TABLE IF EXISTS `candidate_rating_questions`;

CREATE TABLE `candidate_rating_questions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `custom` bit(1) DEFAULT NULL,
  `rating_question` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `candidate_rating` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `rating_score` double DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  `candidate_rating_question_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKowk7pyx5sngsllf4o3krohygd` (`candidate_cid`),
  KEY `FKpcqlvedb728uqirncb0624ruk` (`candidate_rating_question_id`),
  CONSTRAINT `FKowk7pyx5sngsllf4o3krohygd` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`),
  CONSTRAINT `FKpcqlvedb728uqirncb0624ruk` FOREIGN KEY (`candidate_rating_question_id`) REFERENCES `candidate_rating_questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



