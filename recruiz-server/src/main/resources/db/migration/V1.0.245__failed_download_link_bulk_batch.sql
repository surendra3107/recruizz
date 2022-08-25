DROP PROCEDURE IF EXISTS add_failed_file_zip_url;

DELIMITER $$
CREATE PROCEDURE add_failed_file_zip_url()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_resume_bulk_upload'
             AND table_schema = DATABASE()
             AND column_name = 'failedFileZipUrl' ) THEN 
             
      ALTER TABLE `candidate_resume_bulk_upload` 
      ADD COLUMN `failedFileZipUrl`  varchar(555) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_failed_file_zip_url();
