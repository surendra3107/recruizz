DROP PROCEDURE IF EXISTS add_column_storageMode_in_candidateFile;
DROP PROCEDURE IF EXISTS add_column_storageMode_in_positionFile;
DROP PROCEDURE IF EXISTS add_column_storageMode_in_clientFile;

DELIMITER $$
CREATE PROCEDURE add_column_storageMode_in_candidateFile()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_file'
             AND table_schema = DATABASE()
             AND column_name = 'storageMode' ) THEN 
             
      ALTER TABLE `candidate_file` 
      ADD COLUMN `storageMode` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_column_storageMode_in_positionFile()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_file'
             AND table_schema = DATABASE()
             AND column_name = 'storageMode' ) THEN 
             
      ALTER TABLE `position_file` 
      ADD COLUMN `storageMode` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_column_storageMode_in_clientFile()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'client_file'
             AND table_schema = DATABASE()
             AND column_name = 'storageMode' ) THEN 
             
      ALTER TABLE `client_file` 
      ADD COLUMN `storageMode` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_storageMode_in_candidateFile;
CALL add_column_storageMode_in_positionFile;
CALL add_column_storageMode_in_clientFile;