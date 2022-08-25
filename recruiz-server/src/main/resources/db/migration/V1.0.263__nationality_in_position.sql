drop procedure IF EXISTS add_nationality_position;


DELIMITER $$
CREATE PROCEDURE add_nationality_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'nationality' ) THEN 
             
      ALTER TABLE `position` 
      ADD COLUMN `nationality` VARCHAR(100) NULL AFTER `import_identifier`;
      
    END IF;
    
END $$
DELIMITER ;



drop procedure IF EXISTS add_nationality_position_audit;

DELIMITER $$
CREATE PROCEDURE add_nationality_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'nationality' ) THEN 
             
      ALTER TABLE `position_audit` 
      ADD COLUMN `nationality` VARCHAR(100) NULL AFTER `import_identifier`;

      
    END IF;
    
END $$
DELIMITER ;


call add_nationality_position();
call add_nationality_position_audit();