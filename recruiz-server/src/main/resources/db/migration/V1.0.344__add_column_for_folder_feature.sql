DROP PROCEDURE IF EXISTS add_user_added_date_time_column_candidate_folder_tbl;
DROP PROCEDURE IF EXISTS add_user_added_date_time_column_position_folder_tbl;

DELIMITER $$
CREATE PROCEDURE add_user_added_date_time_column_candidate_folder_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'candidate_folder' AND table_schema = DATABASE() AND column_name = 'added_by_user_email' ) THEN
           
	ALTER TABLE `candidate_folder` ADD COLUMN `added_by_user_email` varchar(255) DEFAULT NULL;
 

   END IF;
   
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'candidate_folder' AND table_schema = DATABASE() AND column_name = 'added_date_time' ) THEN
           
	ALTER TABLE `candidate_folder` ADD COLUMN  `added_date_time` datetime DEFAULT NULL;
 

   END IF;
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_user_added_date_time_column_position_folder_tbl()
BEGIN
  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'position_folder' AND table_schema = DATABASE() AND column_name = 'added_by_user_email' ) THEN
           
		ALTER TABLE `position_folder` ADD COLUMN `added_by_user_email` varchar(255) DEFAULT NULL;
 
   END IF;
   
  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'position_folder' AND table_schema = DATABASE() AND column_name = 'added_date_time' ) THEN
           
		ALTER TABLE `position_folder` ADD COLUMN  `added_date_time` datetime DEFAULT NULL;
 
   END IF;
   
END $$
DELIMITER ;



call add_user_added_date_time_column_candidate_folder_tbl();
call add_user_added_date_time_column_position_folder_tbl();