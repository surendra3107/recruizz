ALTER TABLE `client` 
CHANGE COLUMN `address` `address` VARCHAR(255) NULL ;

ALTER TABLE `client_audit` 
CHANGE COLUMN `address` `address` VARCHAR(255) NULL ;
