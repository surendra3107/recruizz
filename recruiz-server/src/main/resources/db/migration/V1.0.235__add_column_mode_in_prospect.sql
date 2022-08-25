DROP PROCEDURE IF EXISTS add_mode_in_prospect_table;

DELIMITER $$
CREATE PROCEDURE add_mode_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'mode' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `mode`  varchar(255) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_mode_in_prospect_table();
