ALTER TABLE `candidate_file` 
ADD COLUMN `candidateId` varchar(255) NULL AFTER `fileType`;

ALTER TABLE `candidate_file_audit` 
ADD COLUMN `candidateId` varchar(255) NULL AFTER `fileType`;

