DELIMITER $$
CREATE PROCEDURE add_column_degree_candidate_eductaion()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate_education_details'
             AND table_schema = DATABASE()
             AND column_name = 'degree' ) THEN 
             
     ALTER TABLE `candidate_education_details` 
      CHANGE COLUMN `passingYear` `passingYear` VARCHAR(255) NULL DEFAULT NULL ,
        ADD COLUMN `degree` VARCHAR(255) NULL AFTER `candidate_cid`;
      
    END IF; 
    
END $$
DELIMITER ;

call add_column_degree_candidate_eductaion();