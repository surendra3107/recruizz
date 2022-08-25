DROP PROCEDURE IF EXISTS add_columns_in_offer_letter_details_table;

DELIMITER $$
CREATE PROCEDURE add_columns_in_offer_letter_details_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_details'
             AND table_schema = DATABASE()
             AND column_name = 'monthlyCtcValue' ) THEN 
             
      ALTER TABLE `offer_letter_details` 
      ADD COLUMN `monthlyCtcValue` varchar(255) DEFAULT NULL;
      
    END IF; 
	
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_details'
             AND table_schema = DATABASE()
             AND column_name = 'annaullyCtcValue' ) THEN 
             
      ALTER TABLE `offer_letter_details` 
      ADD COLUMN `annaullyCtcValue` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_columns_in_offer_letter_details_table;