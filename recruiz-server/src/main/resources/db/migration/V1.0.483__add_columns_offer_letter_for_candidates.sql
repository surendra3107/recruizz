DROP PROCEDURE IF EXISTS add_column_in_offer_letter_for_candidates;

DELIMITER $$
CREATE PROCEDURE add_column_in_offer_letter_for_candidates()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_for_candidates'
             AND table_schema = DATABASE()
             AND column_name = 'finalMonthlyCtc' ) THEN 
             
      ALTER TABLE `offer_letter_for_candidates` 
      ADD COLUMN `finalMonthlyCtc` varchar(255) DEFAULT NULL;
      
    END IF; 
	
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_for_candidates'
             AND table_schema = DATABASE()
             AND column_name = 'finalAnnaullyCtc' ) THEN 
             
      ALTER TABLE `offer_letter_for_candidates` 
      ADD COLUMN `finalAnnaullyCtc` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_in_offer_letter_for_candidates;