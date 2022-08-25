DROP PROCEDURE IF EXISTS add_column_candidate_modification_date;
DROP PROCEDURE IF EXISTS add_column_candidate_modification_date_audit;

DELIMITER $$
CREATE PROCEDURE add_column_candidate_modification_date()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'candidate_modification_days' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `candidate_modification_days` BIGINT(20);
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_column_candidate_modification_date_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'candidate_modification_days' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `candidate_modification_days` BIGINT(20);
      
    END IF;
    
END $$
DELIMITER ;

call add_column_candidate_modification_date();
call add_column_candidate_modification_date_audit();