DROP PROCEDURE IF EXISTS add_column_to_email_candidate_data;

DELIMITER $$
CREATE PROCEDURE add_column_to_email_candidate_data()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'email_template_data'
             AND table_schema = DATABASE()
             AND column_name = 'variableData' ) THEN 
             
      ALTER TABLE `email_template_data` 
      ADD COLUMN `variableData` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_to_email_candidate_data();