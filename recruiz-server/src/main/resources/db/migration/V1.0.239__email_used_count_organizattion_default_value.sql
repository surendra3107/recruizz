drop PROCEDURE IF EXISTS add_email_used_column_delete;

DELIMITER $$
CREATE PROCEDURE add_email_used_column_delete()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'org_config'
             AND table_schema = DATABASE()
             AND column_name = 'emailUsed' ) THEN 
             
      ALTER TABLE `org_config` 
       DROP COLUMN `emailUsed`;
      
    END IF;
    
END $$
DELIMITER ;

drop PROCEDURE IF EXISTS add_email_used_column_with_default_value;

DELIMITER $$
CREATE PROCEDURE add_email_used_column_with_default_value()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'org_config'
             AND table_schema = DATABASE()
             AND column_name = 'emailUsed' ) THEN 
             
      ALTER TABLE `org_config` 
      ADD COLUMN `emailUsed` INT(11) NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;


call add_email_used_column_delete();
call add_email_used_column_with_default_value();