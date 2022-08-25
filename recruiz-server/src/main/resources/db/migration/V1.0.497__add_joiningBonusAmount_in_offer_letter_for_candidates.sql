DROP PROCEDURE IF EXISTS add_joiningBonusAmount_in_offerLetterForCandidate;

DELIMITER $$
CREATE PROCEDURE add_joiningBonusAmount_in_offerLetterForCandidate()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'offer_letter_for_candidates'
             AND table_schema = DATABASE()
             AND column_name = 'joiningBonusAmount' ) THEN 
             
      ALTER TABLE `offer_letter_for_candidates` 
      ADD COLUMN `joiningBonusAmount` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_joiningBonusAmount_in_offerLetterForCandidate;