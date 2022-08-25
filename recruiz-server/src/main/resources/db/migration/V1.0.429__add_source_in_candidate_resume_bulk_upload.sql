DELIMITER $$
CREATE PROCEDURE add_source_in_candidate_resume_bulk_upload()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_resume_bulk_upload'
             AND table_schema = DATABASE()
             AND column_name = 'source' ) THEN 
             
      ALTER TABLE `candidate_resume_bulk_upload` 
      ADD COLUMN `source` VARCHAR(255);
      
    END IF; 
    
END $$
DELIMITER ;


call add_source_in_candidate_resume_bulk_upload();
