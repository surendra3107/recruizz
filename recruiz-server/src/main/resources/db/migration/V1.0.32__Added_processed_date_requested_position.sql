ALTER TABLE `position_request` 
ADD COLUMN `processedDate` DATETIME NULL AFTER `positionCode`;


ALTER TABLE `position_request_audit` 
ADD COLUMN `processedDate` DATETIME NULL AFTER `positionCode`;

