DROP PROCEDURE IF EXISTS add_column_to_sixth_sense_user;

DELIMITER $$
CREATE PROCEDURE add_column_to_sixth_sense_user()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'usage_type' ) THEN 
             
      ALTER TABLE `sixth_sense_user` 
      ADD COLUMN `usage_type` varchar(200) DEFAULT NULL;
      
    END IF; 
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'view_count' ) THEN 
             
      ALTER TABLE `sixth_sense_user` 
      ADD COLUMN `view_count` int DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

CALL add_column_to_sixth_sense_user();