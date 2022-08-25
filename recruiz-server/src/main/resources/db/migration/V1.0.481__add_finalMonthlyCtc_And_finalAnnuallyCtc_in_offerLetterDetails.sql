DROP PROCEDURE IF EXISTS add_column_finalMonthlyCtc_in_OfferLetterDetails;
DROP PROCEDURE IF EXISTS add_column_finalAnnaullyCtc_in_OfferLetterDetails;

DELIMITER $$
CREATE PROCEDURE add_column_finalMonthlyCtc_in_OfferLetterDetails()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_details'
             AND table_schema = DATABASE()
             AND column_name = 'finalMonthlyCtc' ) THEN 
             
      ALTER TABLE `offer_letter_details` 
      ADD COLUMN `finalMonthlyCtc` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_column_finalAnnaullyCtc_in_OfferLetterDetails()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_details'
             AND table_schema = DATABASE()
             AND column_name = 'finalAnnaullyCtc' ) THEN 
             
      ALTER TABLE `offer_letter_details` 
      ADD COLUMN `finalAnnaullyCtc` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_finalMonthlyCtc_in_OfferLetterDetails;
CALL add_column_finalAnnaullyCtc_in_OfferLetterDetails;