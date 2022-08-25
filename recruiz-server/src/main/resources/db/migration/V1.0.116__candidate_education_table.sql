CREATE TABLE IF NOT EXISTS `candidate_education_details` (
  `cid` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `board` varchar(255) DEFAULT NULL,
  `college` varchar(255) DEFAULT NULL,
  `passingPercent` double DEFAULT NULL,
  `passingYear` int(11) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`cid`),
  KEY `FKfw1vi9aw0pji893mgdvhqdftg` (`candidate_cid`),
  CONSTRAINT `FKfw1vi9aw0pji893mgdvhqdftg` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
