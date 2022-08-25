DROP PROCEDURE IF EXISTS add_constraint_positioncode_c_id_in_roundcandidate;
DROP PROCEDURE IF EXISTS add_constraint_positioncode_c_id_in_rc_audit;

DELIMITER $$
CREATE PROCEDURE add_constraint_positioncode_c_id_in_roundcandidate()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'round_candidate' AND table_schema = DATABASE() AND column_name = 'positionCandidateKey' ) THEN 
             
      ALTER TABLE `round_candidate` 
ADD COLUMN `positionCandidateKey` VARCHAR(255) AFTER `sourcedBy`,
ADD UNIQUE INDEX `positionCandidateKey_UNIQUE` (`positionCandidateKey` ASC);

UPDATE round_candidate SET positionCandidateKey =  CONCAT(positionCode, '-' ,candidate_cid) where id > 0;
      
    END IF;
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_constraint_positioncode_c_id_in_rc_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'round_candidate_audit' AND table_schema = DATABASE() AND column_name = 'positionCandidateKey' ) THEN 
             
      ALTER TABLE `round_candidate_audit` 
ADD COLUMN `positionCandidateKey` VARCHAR(255) NULL AFTER `sourcedBy`;
      
    END IF;
    
END $$
DELIMITER ;

call add_constraint_positioncode_c_id_in_roundcandidate();
call add_constraint_positioncode_c_id_in_rc_audit();
