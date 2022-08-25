DROP PROCEDURE IF EXISTS add_column_storageMode_in_candidateFileAudit;

DELIMITER $$
CREATE PROCEDURE add_column_storageMode_in_candidateFileAudit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_file_audit'
             AND table_schema = DATABASE()
             AND column_name = 'storageMode' ) THEN 
             
      ALTER TABLE `candidate_file_audit` 
      ADD COLUMN `storageMode` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_storageMode_in_candidateFileAudit;