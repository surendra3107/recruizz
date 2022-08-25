DROP PROCEDURE IF EXISTS add_column_ivr_integration_in_candidateActivity;
DROP PROCEDURE IF EXISTS add_column_ivr_integration_in_positionActivity;

DELIMITER $$
CREATE PROCEDURE add_column_ivr_integration_in_candidateActivity()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_activity'
             AND table_schema = DATABASE()
             AND column_name = 'ivr_integration' ) THEN 
             
      ALTER TABLE `candidate_activity` 
      ADD COLUMN `ivr_integration` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_column_ivr_integration_in_positionActivity()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_activity'
             AND table_schema = DATABASE()
             AND column_name = 'ivr_integration' ) THEN 
             
      ALTER TABLE `position_activity` 
      ADD COLUMN `ivr_integration` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_ivr_integration_in_candidateActivity;
CALL add_column_ivr_integration_in_positionActivity;