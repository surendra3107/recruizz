ALTER TABLE `position_request` 
ADD COLUMN `positionId` BIGINT(20) NULL AFTER `type`;

ALTER TABLE `position_request_audit` 
ADD COLUMN `positionId` BIGINT(20) NULL AFTER `type`;
