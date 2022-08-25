ALTER TABLE `notification` 
ADD COLUMN `candidateId` BIGINT(20) NULL AFTER `viewState`,
ADD COLUMN `clientId` BIGINT(20) NULL AFTER `candidateId`,
ADD COLUMN `newUserEmail` VARCHAR(255) NULL AFTER `clientId`,
ADD COLUMN `positionCode` VARCHAR(255) NULL AFTER `newUserEmail`,
ADD COLUMN `requestedPositionId` BIGINT(20) NULL AFTER `positionCode`,
ADD COLUMN `roundId` BIGINT(20) NULL AFTER `requestedPositionId`,
ADD COLUMN `roundCandidateId` BIGINT(20) NULL AFTER `roundId`;

ALTER TABLE `notification_audit` 
ADD COLUMN `candidateId` BIGINT(20) NULL AFTER `viewState`,
ADD COLUMN `clientId` BIGINT(20) NULL AFTER `candidateId`,
ADD COLUMN `newUserEmail` VARCHAR(255) NULL AFTER `clientId`,
ADD COLUMN `positionCode` VARCHAR(255) NULL AFTER `newUserEmail`,
ADD COLUMN `requestedPositionId` BIGINT(20) NULL AFTER `positionCode`,
ADD COLUMN `roundId` BIGINT(20) NULL AFTER `requestedPositionId`,
ADD COLUMN `roundCandidateId` BIGINT(20) NULL AFTER `roundId`;
