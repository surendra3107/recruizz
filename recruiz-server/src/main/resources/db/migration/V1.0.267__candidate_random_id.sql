drop procedure IF EXISTS addd_candidate_randomId;


DELIMITER $$
CREATE PROCEDURE addd_candidate_randomId()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'candidateRandomId' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `candidateRandomId` VARCHAR(100) NULL;
      
    END IF;
    
END $$
DELIMITER ;

call addd_candidate_randomId();
