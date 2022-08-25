DROP PROCEDURE IF EXISTS add_GeneratedOfferLetter_In_Candidate_Table;

DELIMITER $$
CREATE PROCEDURE add_GeneratedOfferLetter_In_Candidate_Table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'generatedOfferLetter' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `generatedOfferLetter` bit(1) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_GeneratedOfferLetter_In_Candidate_Table();
