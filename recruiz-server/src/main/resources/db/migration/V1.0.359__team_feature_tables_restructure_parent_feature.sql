DROP PROCEDURE IF EXISTS remove_constraints_from_position_team_relation;

DELIMITER $$
CREATE PROCEDURE remove_constraints_from_position_team_relation()
BEGIN
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.TABLE_CONSTRAINTS  WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'position' AND CONSTRAINT_TYPE = 'FOREIGN KEY'   AND  CONSTRAINT_NAME = 'FKqvxrh500irlffxruq1advrytn' ) > 0) THEN
             
      ALTER TABLE `position` DROP FOREIGN KEY FKqvxrh500irlffxruq1advrytn;
	  ALTER TABLE `position` DROP KEY FKqvxrh500irlffxruq1advrytn;
     
    END IF;
    
END $$
DELIMITER ;

call remove_constraints_from_position_team_relation();


DROP TABLE IF EXISTS `user_to_team_member`;
DROP TABLE IF EXISTS `team_to_team_member`;
DROP TABLE IF EXISTS `team_member`;
DROP TABLE IF EXISTS `team`;


CREATE TABLE IF NOT EXISTS `team` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `team_desc` varchar(500) DEFAULT NULL,
  `team_name` varchar(255) DEFAULT NULL,
  `team_target_amount` double DEFAULT NULL,
  `team_target_amount_currency` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sob22siqdnn2rfsxk6f00pgwb` (`team_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `team_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `target_amount` double DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `team_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj6w13vhqtsr7j4ood3u049ttc` (`team_id`,`user_id`),
  KEY `FK4xxp6ayejqxfojdweodvbqxbc` (`parent_id`),
  KEY `FKg24qjftfifisxhilscl0vmrb1` (`user_id`),
  CONSTRAINT `FK4xxp6ayejqxfojdweodvbqxbc` FOREIGN KEY (`parent_id`) REFERENCES `team_member` (`id`),
  CONSTRAINT `FK9ubp79ei4tv4crd0r9n7u5i6e` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `FKg24qjftfifisxhilscl0vmrb1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



DROP PROCEDURE IF EXISTS add_constraints_from_position_team_relation;

DELIMITER $$
CREATE PROCEDURE add_constraints_from_position_team_relation()
BEGIN
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.TABLE_CONSTRAINTS  WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'position' AND CONSTRAINT_TYPE = 'FOREIGN KEY'   AND  CONSTRAINT_NAME = 'FKqvxrh500irlffxruq1advrytn' ) >= 0) THEN
            
      ALTER TABLE `position` ADD  CONSTRAINT `FKqvxrh500irlffxruq1advrytn` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`);
     
    END IF;
    
END $$
DELIMITER ;

call add_constraints_from_position_team_relation();