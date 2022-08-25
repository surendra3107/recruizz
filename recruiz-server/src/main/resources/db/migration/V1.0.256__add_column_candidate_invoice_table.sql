DELIMITER $$
CREATE PROCEDURE add_percentage_in_candidate_invoice_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_invoice'
             AND table_schema = DATABASE()
             AND column_name = 'percentage' ) THEN 
             
      ALTER TABLE `candidate_invoice` 
      ADD COLUMN `percentage` double NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_value_in_candidate_invoice_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_invoice'
             AND table_schema = DATABASE()
             AND column_name = 'value' ) THEN 
             
      ALTER TABLE `candidate_invoice` 
      ADD COLUMN `value` double NOT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

call add_percentage_in_candidate_invoice_table();
call add_value_in_candidate_invoice_table();

