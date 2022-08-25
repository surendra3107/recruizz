DELIMITER $$
CREATE PROCEDURE add_column_to_advanced_search_table_procedure()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_educational_institute' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_educational_institute` text DEFAULT NULL;
      
    END IF; 
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_educational_qualification' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_educational_qualification` text DEFAULT NULL;
      
    END IF; 
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_gender' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_gender` text DEFAULT NULL;
      
    END IF; 
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_source' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_source` text DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_to_advanced_search_table_procedure();