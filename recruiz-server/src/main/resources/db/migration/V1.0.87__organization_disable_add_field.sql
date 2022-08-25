ALTER TABLE `organization` 
ADD COLUMN `disable_status` BIT NULL AFTER `org_api_token`,
ADD COLUMN `disable_reason` VARCHAR(255) NULL AFTER `disable_status`;

ALTER TABLE `organization_audit` 
ADD COLUMN `disable_status` BIT NULL AFTER `org_api_token`,
ADD COLUMN `disable_reason` VARCHAR(255) NULL AFTER `disable_status`;

UPDATE `organization` 
SET `disable_status`=0;

UPDATE `organization_audit` 
SET `disable_status`=0;