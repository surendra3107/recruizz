DELIMITER $$
CREATE PROCEDURE add_field_captchaSession_In_SixthSenseUser()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'captchaSession' ) THEN 
             
      ALTER TABLE `sixth_sense_user` 
      ADD COLUMN `captchaSession` VARCHAR(255);
      
    END IF; 
    
END $$
DELIMITER ;

call add_field_captchaSession_In_SixthSenseUser();