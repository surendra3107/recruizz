DELIMITER $$
CREATE PROCEDURE add_dummy_column_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `dummy` bit(1) NOT NULL DEFAULT b'0';
      
    END IF;
    
END $$
DELIMITER ;

call add_dummy_column_in_prospect_table();
