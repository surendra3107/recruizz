DROP PROCEDURE IF EXISTS add_owner_column_for_folder_tbl;

DELIMITER $$
CREATE PROCEDURE add_owner_column_for_folder_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'folder' AND table_schema = DATABASE() AND column_name = 'added_by_user_email' ) THEN
           
	ALTER TABLE `folder` ADD COLUMN  `owner_email` varchar(255) NOT NULL;
 

   END IF;
   
END $$
DELIMITER ;

call add_owner_column_for_folder_tbl();
