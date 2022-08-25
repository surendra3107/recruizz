drop procedure IF EXISTS remove_not_null_constraint_email_in_propsect_table;
drop procedure IF EXISTS remove_not_null_constraint_mobile_in_propsect_table;
drop procedure IF EXISTS remove_not_null_constraint_industry_in_propsect_table;
drop procedure IF EXISTS remove_not_null_constraint_category_in_propsect_table;

DELIMITER $$
CREATE PROCEDURE remove_not_null_constraint_email_in_propsect_table()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'email' ) THEN 
             
      ALTER TABLE `prospect` MODIFY `email` varchar(255) NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE remove_not_null_constraint_mobile_in_propsect_table()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'mobile' ) THEN 
             
      ALTER TABLE `prospect` MODIFY `mobile` varchar(255) NULL;
     
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE remove_not_null_constraint_industry_in_propsect_table()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'industry' ) THEN 
             
      ALTER TABLE `prospect` MODIFY `industry` varchar(255) NULL;
     
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE remove_not_null_constraint_category_in_propsect_table()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'category' ) THEN 
             
      ALTER TABLE `prospect` MODIFY `category` varchar(255) NULL;
     
    END IF;
    
END $$
DELIMITER ;


call remove_not_null_constraint_email_in_propsect_table();
call remove_not_null_constraint_mobile_in_propsect_table();
call remove_not_null_constraint_industry_in_propsect_table();
call remove_not_null_constraint_category_in_propsect_table();