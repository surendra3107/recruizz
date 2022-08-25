DROP PROCEDURE IF EXISTS add_column_email_client_Details;

DELIMITER $$
CREATE PROCEDURE add_column_email_client_Details()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'email_client_details' AND table_schema = DATABASE() AND column_name = 'recruizEmail' ) THEN 
             
      ALTER TABLE `email_client_details` 
      ADD COLUMN `recruizEmail` VARCHAR(255) NOT NULL AFTER `user_user_id`;
      
    END IF;
    
END $$
DELIMITER ;

call add_column_email_client_Details();
