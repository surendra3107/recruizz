
ALTER TABLE `position` CHANGE COLUMN `location` `location` BLOB(100000) NOT NULL ;
ALTER TABLE `position_audit` CHANGE COLUMN `location` `location` BLOB(100000) NOT NULL ;

ALTER TABLE `position_request` CHANGE COLUMN `location` `location` BLOB(100000) NOT NULL ;
ALTER TABLE `position_request_audit` CHANGE COLUMN `location` `location` BLOB(100000) NOT NULL ;

