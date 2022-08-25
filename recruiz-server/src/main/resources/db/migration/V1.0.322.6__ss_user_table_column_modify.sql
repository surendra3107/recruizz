DROP PROCEDURE IF EXISTS add_column_sixth_sense_user_tbl;

DELIMITER $$
CREATE PROCEDURE add_column_sixth_sense_user_tbl()
BEGIN
	
	IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'sources' ) THEN   
     ALTER TABLE `sixth_sense_user` ADD COLUMN `sources` varchar(255) DEFAULT NULL;
      
    END IF;
   
END $$
DELIMITER ;

call add_column_sixth_sense_user_tbl();