ALTER TABLE `import_job_batch`
CHANGE COLUMN `header_map` `header_map` VARCHAR(5000) NULL DEFAULT NULL AFTER `file_path`;