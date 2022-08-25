DROP PROCEDURE IF EXISTS add_column_email_client_Details_last_max_uid;

DELIMITER $$
CREATE PROCEDURE add_column_email_client_Details_last_max_uid()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'email_client_details' AND table_schema = DATABASE() AND column_name = 'lastMaxUid' ) THEN 
             
      ALTER TABLE `email_client_details` 
      ADD COLUMN `lastMaxUid` BIGINT(20);
      
    END IF;
    
END $$
DELIMITER ;

call add_column_email_client_Details_last_max_uid();
