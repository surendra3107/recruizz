DROP PROCEDURE IF EXISTS add_team_leader_target_amount_column_for_team_tbl;

DELIMITER $$
CREATE PROCEDURE add_team_leader_target_amount_column_for_team_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'team' AND table_schema = DATABASE() AND column_name = 'team_leader_target_amount' ) THEN
           
	ALTER TABLE `team` ADD COLUMN  `team_leader_target_amount`  double DEFAULT NULL;
 
   END IF;
   
END $$
DELIMITER ;

call add_team_leader_target_amount_column_for_team_tbl();
