DROP PROCEDURE IF EXISTS add_column_status_employee;
DROP PROCEDURE IF EXISTS add_column_status_employee_audit;

DELIMITER $$
CREATE PROCEDURE add_column_status_employee()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'employee'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      ALTER TABLE `employee` 
      ADD COLUMN `status` VARCHAR(255);
      
    END IF;
    
END $$
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE add_column_status_employee_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'employee_audit'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      ALTER TABLE `employee_audit` 
      ADD COLUMN `status` VARCHAR(255);
      
    END IF;
    
END $$
DELIMITER ;

call add_column_status_employee();
call add_column_status_employee_audit();