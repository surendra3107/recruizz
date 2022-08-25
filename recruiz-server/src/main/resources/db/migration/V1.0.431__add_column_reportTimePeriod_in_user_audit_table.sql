
DELIMITER $$
CREATE PROCEDURE add_column_reportTimePeriod_in_user_audit_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'reporttimeperiod' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `reporttimeperiod` VARCHAR(255);
      
    END IF; 
    
END $$
DELIMITER ;

call add_column_reportTimePeriod_in_user_audit_table();