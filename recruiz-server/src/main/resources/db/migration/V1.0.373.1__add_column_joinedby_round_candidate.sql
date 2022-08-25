DROP PROCEDURE IF EXISTS addJoinedByInRoundCandidateTable;
DROP PROCEDURE IF EXISTS addJoinedByInRoundCandidateTable_Audit;

DELIMITER $$
CREATE PROCEDURE addJoinedByInRoundCandidateTable()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'round_candidate'
             AND table_schema = DATABASE()
             AND column_name = 'joinedByHr' ) THEN 
             
      ALTER TABLE `round_candidate` 
      ADD COLUMN `joinedByHr` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE addJoinedByInRoundCandidateTable_Audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'round_candidate_audit'
             AND table_schema = DATABASE()
             AND column_name = 'joinedByHr' ) THEN 
             
      ALTER TABLE `round_candidate_audit` 
      ADD COLUMN `joinedByHr` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;

call addJoinedByInRoundCandidateTable();
call addJoinedByInRoundCandidateTable_Audit();