drop PROCEDURE IF EXISTS add_email_used_column;

DELIMITER $$
CREATE PROCEDURE add_email_used_column()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'org_config'
             AND table_schema = DATABASE()
             AND column_name = 'emailUsed' ) THEN 
             
      ALTER TABLE `org_config` 
      ADD COLUMN `emailUsed` INT(11);
      
    END IF;
    
END $$
DELIMITER ;

call add_email_used_column();