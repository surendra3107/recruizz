DELIMITER $$
CREATE PROCEDURE add_candidate_email_in_agency_invoice_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'agency_invoice'
             AND table_schema = DATABASE()
             AND column_name = 'candidate_email' ) THEN 
             
      ALTER TABLE `agency_invoice` 
      ADD COLUMN `candidate_email`  varchar(255) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_candidate_email_in_agency_invoice_table();
