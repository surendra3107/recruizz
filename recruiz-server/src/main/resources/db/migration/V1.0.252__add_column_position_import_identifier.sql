drop procedure IF EXISTS add_import_identifier_in_position_table;
DELIMITER $$
CREATE PROCEDURE add_import_identifier_in_position_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'import_identifier' ) THEN 
             
      ALTER TABLE `position` 
      ADD COLUMN `import_identifier`  varchar(255) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;


drop procedure IF EXISTS add_import_identifier_in_position_audit_table;
DELIMITER $$
CREATE PROCEDURE add_import_identifier_in_position_audit_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'import_identifier' ) THEN 
             
      ALTER TABLE `position_audit` 
      ADD COLUMN `import_identifier`  varchar(255) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_import_identifier_in_position_table();
call add_import_identifier_in_position_audit_table();
