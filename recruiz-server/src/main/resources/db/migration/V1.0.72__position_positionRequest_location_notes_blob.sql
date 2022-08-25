ALTER TABLE `position` 
CHANGE COLUMN `description` `description` BLOB(100000) NULL DEFAULT NULL ,
CHANGE COLUMN `notes` `notes` BLOB(100000) NULL DEFAULT NULL ;

ALTER TABLE `position_audit` 
CHANGE COLUMN `description` `description` BLOB(100000) NULL DEFAULT NULL ,
CHANGE COLUMN `notes` `notes` BLOB(100000) NULL DEFAULT NULL ;

ALTER TABLE `position_request` 
CHANGE COLUMN `description` `description` BLOB(100000) NOT NULL ,
CHANGE COLUMN `notes` `notes` BLOB(100000) NULL DEFAULT NULL ;

ALTER TABLE `position_request_audit` 
CHANGE COLUMN `description` `description` BLOB(100000) NOT NULL ,
CHANGE COLUMN `notes` `notes` BLOB(100000) NULL DEFAULT NULL ;

