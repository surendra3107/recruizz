drop procedure IF EXISTS add_percentage_in_propsect_table;
drop procedure IF EXISTS add_value_in_propsect_table;

DELIMITER $$
CREATE PROCEDURE add_percentage_in_propsect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'percentage' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `percentage` double NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_value_in_propsect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'value' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `value` double NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;


call add_percentage_in_propsect_table();
call add_value_in_propsect_table();