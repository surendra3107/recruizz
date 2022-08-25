drop procedure IF EXISTS add_amount_in_candidate_invoice_table;


DELIMITER $$
CREATE PROCEDURE add_amount_in_candidate_invoice_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_invoice'
             AND table_schema = DATABASE()
             AND column_name = 'amount' ) THEN 
             
      ALTER TABLE `candidate_invoice` 
      ADD COLUMN `amount` double NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;


call add_amount_in_candidate_invoice_table();


