ALTER TABLE `candidate_resume_upload_item` 
ADD COLUMN `creation_date` datetime NULL AFTER `id`;

ALTER TABLE `candidate_resume_upload_item` 
ADD COLUMN `modification_date` datetime NULL AFTER `id`;