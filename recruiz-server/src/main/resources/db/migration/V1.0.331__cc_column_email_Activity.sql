DROP PROCEDURE IF EXISTS add_column_cc_email_activity;

DELIMITER $$
CREATE PROCEDURE add_column_cc_email_activity()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'cc'
             AND table_schema = DATABASE()
             AND column_name = 'cc' ) THEN 
             
      ALTER TABLE `email_activity` 
      ADD COLUMN `cc` VARCHAR(255);
      
    END IF;
    
END $$
DELIMITER ;

call add_column_cc_email_activity();