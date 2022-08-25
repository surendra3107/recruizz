drop PROCEDURE IF EXISTS add_column_status_campaign;

DELIMITER $$
CREATE PROCEDURE add_column_status_campaign()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      ALTER TABLE `campaign` 
      ADD COLUMN `status` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;


drop PROCEDURE IF EXISTS add_column_owner_campaign;
DELIMITER $$
CREATE PROCEDURE add_column_owner_campaign()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign'
             AND table_schema = DATABASE()
             AND column_name = 'owner' ) THEN 
             
      ALTER TABLE `campaign` 
      ADD COLUMN `owner` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;


drop PROCEDURE IF EXISTS add_column_start_date_campaign;
DELIMITER $$
CREATE PROCEDURE add_column_start_date_campaign()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign'
             AND table_schema = DATABASE()
             AND column_name = 'startDate' ) THEN 
             
      ALTER TABLE `campaign` 
      ADD COLUMN `startDate` DATETIME;
      
    END IF;
    
END $$
DELIMITER ;


call add_column_status_campaign();
call add_column_owner_campaign();
call add_column_start_date_campaign();