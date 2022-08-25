drop PROCEDURE IF EXISTS add_type_in_prospect_position;
drop PROCEDURE IF EXISTS add_remoteWork_in_prospect_position;
drop PROCEDURE IF EXISTS add_maxSal_in_prospect_position;
drop PROCEDURE IF EXISTS add_minSal_in_prospect_position;
drop PROCEDURE IF EXISTS add_industry_in_prospect_position;
drop PROCEDURE IF EXISTS add_functionalArea_in_prospect_position;
drop PROCEDURE IF EXISTS add_clientName_in_prospect_position;
drop PROCEDURE IF EXISTS add_status_in_prospect_position;
drop PROCEDURE IF EXISTS add_isConvertedToClient_in_prospect_position;

DELIMITER $$
CREATE PROCEDURE add_type_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'type' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `type` varchar(255) NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_remoteWork_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'remoteWork' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `remoteWork` bit(1) NOT NULL DEFAULT b'0';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_maxSal_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'maxSal' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `maxSal` double NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_minSal_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'minSal' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `minSal` double NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_industry_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'industry' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `industry` varchar(255) NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_functionalArea_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'functionalArea' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN  `functionalArea` varchar(255) NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_clientName_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'clientName' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN  `clientName` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_status_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN  `status` varchar(255) NOT NULL DEFAULT 'pending';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_isConvertedToClient_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'isConvertedToClient' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `isConvertedToClient` bit(1) NOT NULL DEFAULT b'0';
      
    END IF;
    
END $$
DELIMITER ;

call add_type_in_prospect_position();
call add_remoteWork_in_prospect_position();
call add_maxSal_in_prospect_position();
call add_minSal_in_prospect_position();
call add_industry_in_prospect_position();
call add_functionalArea_in_prospect_position();
call add_clientName_in_prospect_position();
call add_status_in_prospect_position();
call add_isConvertedToClient_in_prospect_position();


