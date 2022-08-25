drop procedure IF EXISTS add_candidate_more_info;


DELIMITER $$
CREATE PROCEDURE add_candidate_more_info()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'address' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `address` TEXT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'previous_employment' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `previous_employment` varchar(255) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'industry' ) THEN 
             
      ALTER TABLE `candidate`
      ADD COLUMN `industry` varchar(255) DEFAULT NULL;
      
      END IF;
      
      IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'last_active' ) THEN
             
      ALTER TABLE `candidate` 
      ADD COLUMN `last_active` datetime DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_candidate_more_info();
