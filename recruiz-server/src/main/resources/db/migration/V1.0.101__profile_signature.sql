DELIMITER $$
CREATE PROCEDURE user_profile_AlterTable()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user'
             AND table_schema = DATABASE()
             AND column_name = 'profile_signature' ) THEN 
             
      ALTER TABLE `user` 
      ADD COLUMN `profile_signature` LONGTEXT NULL AFTER `last_logged_on_time`;
      
    END IF; 
    
END $$
DELIMITER ;



DELIMITER $$
CREATE PROCEDURE user_profile_audit_AlterTable()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'user_audit'
             AND table_schema = DATABASE()
             AND column_name = 'profile_signature' ) THEN 
             
      ALTER TABLE `user_audit`
      ADD COLUMN `profile_signature` LONGTEXT NULL AFTER `last_logged_on_time`;
      
    END IF; 
    
END $$
DELIMITER ;

