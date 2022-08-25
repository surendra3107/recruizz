ALTER TABLE `employee_audit` 
ADD COLUMN `position_code` VARCHAR(255) NULL AFTER `status`,
ADD COLUMN `position_title` VARCHAR(255) NULL AFTER `position_code`,
ADD COLUMN `client_name` VARCHAR(255) NULL AFTER `position_title`;
