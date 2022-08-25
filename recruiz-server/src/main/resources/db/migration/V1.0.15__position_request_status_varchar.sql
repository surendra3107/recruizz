ALTER TABLE `position_request` 
CHANGE COLUMN `status` `status` VARCHAR(55) NOT NULL ;


ALTER TABLE `position_request_audit` 
CHANGE COLUMN `status` `status` VARCHAR(55) NOT NULL ;
