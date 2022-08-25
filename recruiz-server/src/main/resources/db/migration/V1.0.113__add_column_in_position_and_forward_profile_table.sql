
DELIMITER $$
CREATE PROCEDURE add_position_code_in_forward_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'forward_profile'
             AND table_schema = DATABASE()
             AND column_name = 'positionCode' ) THEN 
             
      ALTER TABLE `forward_profile` 
      ADD COLUMN `positionCode` VARCHAR(255) NULL AFTER `subject`;
      
    END IF; 
    
END $$
DELIMITER ;




DELIMITER $$
CREATE PROCEDURE add_colsedBy_in_position_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'closedByUser' ) THEN 
             
      ALTER TABLE `position` 
      ADD COLUMN `closedByUser` VARCHAR(255) NULL AFTER `minSal`;
      
    END IF; 
    
END $$
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE add_colsedBy_in_position_audit_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'closedByUser' ) THEN 
             
      ALTER TABLE `position_audit` 
      ADD COLUMN `closedByUser` VARCHAR(255) NULL AFTER `minSal`;
      
    END IF; 
    
END $$
DELIMITER ;


call add_position_code_in_forward_table();
call add_colsedBy_in_position_table();
call add_colsedBy_in_position_audit_table();