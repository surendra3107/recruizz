DELIMITER $$
CREATE PROCEDURE add_industry_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'industry' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `industry` VARCHAR(255) NOT NULL DEFAULT 'IT-Software / Software Services';
      
    END IF;
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_category_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'category' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `category` VARCHAR(255) NOT NULL DEFAULT 'IT Software – Application Programming';
      
    END IF; 
    
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_prospectRating_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'prospectRating' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `prospectRating` int(11) NOT NULL DEFAULT 0;
      
    END IF; 
    
END $$
DELIMITER ;

call add_industry_in_prospect_table();
call add_category_in_prospect_table();
call add_prospectRating_in_prospect_table();
