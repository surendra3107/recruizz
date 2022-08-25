DROP PROCEDURE IF EXISTS add_column_email_client_Details_default_marked;

DELIMITER $$
CREATE PROCEDURE add_column_email_client_Details_default_marked()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'email_client_details' AND table_schema = DATABASE() AND column_name = 'markedDefault' ) THEN 
             
      ALTER TABLE `email_client_details` 
      ADD COLUMN `markedDefault` BIT(1) NOT NULL DEFAULT 0 AFTER `lastMaxUid`;
      
    END IF;
    
END $$
DELIMITER ;

call add_column_email_client_Details_default_marked();
