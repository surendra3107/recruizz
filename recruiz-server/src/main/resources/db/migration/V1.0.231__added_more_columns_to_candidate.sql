drop PROCEDURE IF EXISTS add_more_columns_to_candidate_table;

DELIMITER $$
CREATE PROCEDURE add_more_columns_to_candidate_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'languages' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `languages` longtext;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'summary' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `summary` longtext;
      
    END IF; 
    
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'nationality' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `nationality` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'maritalStatus' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `maritalStatus` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'category' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `category` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'subCategory' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `subCategory` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'averageStayInCompany' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN  `averageStayInCompany` int(11) NOT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'longestStayInCompany' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN   `longestStayInCompany` int(11) NOT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

call add_more_columns_to_candidate_table();