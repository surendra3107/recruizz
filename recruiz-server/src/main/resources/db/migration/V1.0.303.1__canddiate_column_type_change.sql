DROP PROCEDURE IF EXISTS change_avgstay_longstay_type;;

DELIMITER $$
CREATE PROCEDURE change_avgstay_longstay_type()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'candidate' AND table_schema = DATABASE() AND column_name = 'averageStayInCompany' ) THEN 
             
     ALTER TABLE `candidate` 
CHANGE COLUMN `averageStayInCompany` `averageStayInCompany` DOUBLE NOT NULL DEFAULT 0 ;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'candidate' AND table_schema = DATABASE() AND column_name = 'longestStayInCompany' ) THEN 
             
     ALTER TABLE `candidate` 
CHANGE COLUMN `longestStayInCompany` `longestStayInCompany` DOUBLE NOT NULL DEFAULT 0 ;
      
    END IF;
    
END $$
DELIMITER ;

call change_avgstay_longstay_type();
