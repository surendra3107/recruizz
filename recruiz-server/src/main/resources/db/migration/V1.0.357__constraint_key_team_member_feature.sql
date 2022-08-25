DROP PROCEDURE IF EXISTS add_constraint_key_team_member_tbl;


DELIMITER $$
CREATE PROCEDURE add_constraint_key_team_member_tbl() 
BEGIN
  
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='team_member' AND index_name='team_id_user_id_unique_constraint_key' ) THEN
		ALTER TABLE `team_member` ADD CONSTRAINT team_id_user_id_unique_constraint_key UNIQUE (team_id, user_id);
    END IF;
   
     
   
END $$
DELIMITER ;


call add_constraint_key_team_member_tbl();
