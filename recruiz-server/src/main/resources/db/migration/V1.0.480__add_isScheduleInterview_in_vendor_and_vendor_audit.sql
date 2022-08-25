DROP PROCEDURE IF EXISTS add_column_isInterviewSchedule_in_vendor;
DROP PROCEDURE IF EXISTS add_column_isInterviewSchedule_in_vendor_audit;

DELIMITER $$
CREATE PROCEDURE add_column_isInterviewSchedule_in_vendor()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'vendor'
             AND table_schema = DATABASE()
             AND column_name = 'isInterviewSchedule' ) THEN 
             
      ALTER TABLE `vendor` 
      ADD COLUMN `isInterviewSchedule` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_column_isInterviewSchedule_in_vendor_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'vendor_audit'
             AND table_schema = DATABASE()
             AND column_name = 'isInterviewSchedule' ) THEN 
             
      ALTER TABLE `vendor_audit` 
      ADD COLUMN `isInterviewSchedule` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_isInterviewSchedule_in_vendor;
CALL add_column_isInterviewSchedule_in_vendor_audit;