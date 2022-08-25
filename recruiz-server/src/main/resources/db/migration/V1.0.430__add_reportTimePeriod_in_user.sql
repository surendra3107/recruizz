
DELIMITER $$
CREATE PROCEDURE add_reportTimePeriod_Column_in_User_Table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'reporttimeperiod' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `reporttimeperiod` VARCHAR(255);
      
    END IF; 
    
END $$
DELIMITER ;

call add_reportTimePeriod_Column_in_User_Table();