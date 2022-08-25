ALTER TABLE `feedback` ADD COLUMN `eventCreatedBy` VARCHAR(255) NULL AFTER `ratings`;

ALTER TABLE `feedback_audit` ADD COLUMN `eventCreatedBy` VARCHAR(255) NULL AFTER `ratings`;
