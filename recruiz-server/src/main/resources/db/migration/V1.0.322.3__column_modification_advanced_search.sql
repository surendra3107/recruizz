DROP PROCEDURE IF EXISTS delete_column_advanced_search;
DROP PROCEDURE IF EXISTS change_column_advanced_search;


DELIMITER $$
CREATE PROCEDURE change_column_advanced_search()
BEGIN
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_field' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_field` `advanced_search_in` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_employement_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_employement_type` `advanced_search_job_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_sourced' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_sourced` `advanced_search_resume_freshness` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_and_keys' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_and_keys` `advanced_search_all_keyword` varchar(1000) DEFAULT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_or_keys' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_or_keys` `advanced_search_any_keyword` varchar(1000) DEFAULT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_not_keys' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      CHANGE COLUMN `advanced_search_not_keys` `advanced_search_exclude_keyword` varchar(1000) DEFAULT NULL;
      
    END IF;

END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE delete_column_advanced_search()
BEGIN
	
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_educational_qualification' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_educational_qualification`;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_educational_institute' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_educational_institute`;
      
    END IF;
    
     IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_status' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_status`;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_experience' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_experience`;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ctc' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_ctc`;
      
    END IF;
    
     IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_skillset' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_skillset`;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_gender' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      DROP COLUMN `advanced_search_gender`;
      
    END IF;
	
END $$
DELIMITER ;

call delete_column_advanced_search();
call change_column_advanced_search();
