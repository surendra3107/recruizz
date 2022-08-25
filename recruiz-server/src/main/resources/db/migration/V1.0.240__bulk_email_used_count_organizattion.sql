drop PROCEDURE IF EXISTS add_bulk_email_used_column_with_default_value;

DELIMITER $$
CREATE PROCEDURE add_bulk_email_used_column_with_default_value()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'org_config'
             AND table_schema = DATABASE()
             AND column_name = 'bulkEmailUsed' ) THEN 
             
      ALTER TABLE `org_config` 
      ADD COLUMN `bulkEmailUsed` INT(11) NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;


call add_bulk_email_used_column_with_default_value();