drop procedure IF EXISTS add_user_email_details;

DELIMITER $$
CREATE PROCEDURE add_user_email_details()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'username' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `username` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'emailPassoword' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `emailPassoword` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'host' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `host` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'port' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `port` INT(11) DEFAULT 0;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'protocol' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `protocol` varchar(255) DEFAULT NULL;
      
    END IF; 
    
   
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'username' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `username` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'emailPassoword' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `emailPassoword` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'host' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `host` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'port' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `port` INT(11) DEFAULT 0;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'protocol' ) THEN 
             
      ALTER TABLE `user_audit` 
      ADD COLUMN `protocol` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    
    
END $$
DELIMITER ;

CALL add_user_email_details();