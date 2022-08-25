DELIMITER $$
CREATE PROCEDURE add_column_s3_candidate()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 's3Enabled' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `s3Enabled` bit(1) NOT NULL DEFAULT 0;
      
    END IF; 
    
END $$
DELIMITER ;

call add_column_s3_candidate();