DROP PROCEDURE IF EXISTS add_column_to_sixth_sense_resume_view;

DELIMITER $$
CREATE PROCEDURE add_column_to_sixth_sense_resume_view()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_resume_view'
             AND table_schema = DATABASE()
             AND column_name = 'source' ) THEN 
             
      ALTER TABLE `sixth_sense_resume_view` 
      ADD COLUMN `source` varchar(200) NOT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_to_sixth_sense_resume_view();