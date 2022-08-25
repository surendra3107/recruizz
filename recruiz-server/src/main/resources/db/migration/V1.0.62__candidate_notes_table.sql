-- candiate notes table for storing candidate notes
CREATE TABLE IF NOT EXISTS `candidate_notes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `candidateId_cid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_to5y6u58ji203lp0om9rg9cy` (`candidateId_cid`),
  CONSTRAINT `FK_to5y6u58ji203lp0om9rg9cy` FOREIGN KEY (`candidateId_cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- candiate notes table for storing candidate notes audit
CREATE TABLE IF NOT EXISTS `candidate_notes_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `addedBy` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `candidateId_cid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_5pn6lk20gascjw59wspc8o45i` (`REV`),
  CONSTRAINT `FK_5pn6lk20gascjw59wspc8o45i` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
