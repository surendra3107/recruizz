drop procedure IF EXISTS add_column_file_system_bulp_upload_item;


DELIMITER $$
CREATE PROCEDURE add_column_file_system_bulp_upload_item()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_resume_upload_item'
             AND table_schema = DATABASE()
             AND column_name = 'file_system' ) THEN 
             
      ALTER TABLE `candidate_resume_upload_item` 
      ADD COLUMN `file_system` TEXT NULL;
      
    END IF;    
END $$
DELIMITER ;

call add_column_file_system_bulp_upload_item();
