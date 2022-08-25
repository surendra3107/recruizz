DELIMITER $$
CREATE PROCEDURE add_column_to_sixth_sense_user_table_procedure()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'captcha_status' ) THEN 
             
      ALTER TABLE `sixth_sense_user` 
      ADD COLUMN `captcha_status`  VARCHAR(5);
      
    END IF;
    
END $$
DELIMITER ;

CALL add_column_to_sixth_sense_user_table_procedure();
