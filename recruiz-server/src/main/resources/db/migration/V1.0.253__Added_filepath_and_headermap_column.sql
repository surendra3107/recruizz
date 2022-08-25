drop procedure IF EXISTS add_filepath_headermap_column;

DELIMITER $$
CREATE PROCEDURE add_filepath_headermap_column()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'import_job_batch'
             AND table_schema = DATABASE()
             AND column_name = 'file_path' ) THEN 
             
      ALTER TABLE `import_job_batch` 
      ADD COLUMN `file_path` varchar(1000) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'import_job_batch'
             AND table_schema = DATABASE()
             AND column_name = 'header_map' ) THEN 
             
      ALTER TABLE `import_job_batch` 
      ADD COLUMN `header_map` varchar(1000) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_filepath_headermap_column();