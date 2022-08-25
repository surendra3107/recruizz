ALTER TABLE `position_request` 
ADD COLUMN `clientName` varchar(255) NULL AFTER `positionId`;

ALTER TABLE `position_request_audit` 
ADD COLUMN `clientName` varchar(255) NULL AFTER `positionId`;
