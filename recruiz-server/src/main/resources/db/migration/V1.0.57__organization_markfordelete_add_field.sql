ALTER TABLE `organization` 
ADD COLUMN `marked_delete` BIT NULL AFTER `bitBucketUrl`,
ADD COLUMN `marked_delete_date` DATETIME NULL AFTER `marked_delete`;

ALTER TABLE `organization_audit` 
ADD COLUMN `marked_delete` BIT NULL AFTER `bitBucketUrl`,
ADD COLUMN `marked_delete_date` DATETIME NULL AFTER `marked_delete`;

UPDATE `organization` 
SET `marked_delete`=0;

UPDATE `organization_audit` 
SET `marked_delete`=0;