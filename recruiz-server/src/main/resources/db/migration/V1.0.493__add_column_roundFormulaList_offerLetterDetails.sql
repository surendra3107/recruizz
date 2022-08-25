DROP PROCEDURE IF EXISTS add_column_roundFormulaList_in_offer_letter_details;

DELIMITER $$
CREATE PROCEDURE add_column_roundFormulaList_in_offer_letter_details()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_details'
             AND table_schema = DATABASE()
             AND column_name = 'roundFormulaList' ) THEN 
             
      ALTER TABLE `offer_letter_details` 
      ADD COLUMN `roundFormulaList` varchar(2048) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_roundFormulaList_in_offer_letter_details;