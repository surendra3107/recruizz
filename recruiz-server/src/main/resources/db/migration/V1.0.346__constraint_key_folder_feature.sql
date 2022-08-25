DROP PROCEDURE IF EXISTS add_constraint_key_candidate_folder_tbl;
DROP PROCEDURE IF EXISTS add_constraint_key_position_folder_tbl;

DELIMITER $$
CREATE PROCEDURE add_constraint_key_candidate_folder_tbl() 
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS  WHERE  constraint_name = 'folder_id_candidate_id_unique_constraint_key' ) THEN
           
    DELETE FROM `candidate_folder`;
   
	ALTER TABLE `candidate_folder` ADD CONSTRAINT folder_id_candidate_id_unique_constraint_key UNIQUE (candidate_cid, folder_id);
	
   END IF;
   
     
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_constraint_key_position_folder_tbl()
BEGIN
  IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS  WHERE constraint_name = 'folder_id_position_id_unique_constraint_key' ) THEN
           
		
	DELETE FROM `position_folder`;
	
	ALTER TABLE `position_folder` ADD CONSTRAINT folder_id_position_id_unique_constraint_key UNIQUE (position_id, folder_id);
 
   END IF;
 
   
END $$
DELIMITER ;



call add_constraint_key_candidate_folder_tbl();
call add_constraint_key_position_folder_tbl();