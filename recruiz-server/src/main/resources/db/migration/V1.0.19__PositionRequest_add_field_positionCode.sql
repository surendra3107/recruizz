ALTER TABLE `position_request` 
ADD COLUMN `positionCode` varchar(255) NULL AFTER `clientName`;

ALTER TABLE `position_request_audit` 
ADD COLUMN `positionCode` varchar(255) NULL AFTER `clientName`;
