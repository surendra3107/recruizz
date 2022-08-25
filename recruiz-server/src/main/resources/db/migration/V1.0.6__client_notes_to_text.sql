ALTER TABLE `client` 
CHANGE COLUMN `notes` `notes` longtext NULL DEFAULT NULL ;

ALTER TABLE `client_audit` 
CHANGE COLUMN `notes` `notes` longtext NULL DEFAULT NULL ;