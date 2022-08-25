DROP PROCEDURE IF EXISTS add_team_target_position_opening_closure_team_table;
DROP PROCEDURE IF EXISTS add_team_target_position_opening_closure_team_member_table;


DELIMITER $$
CREATE PROCEDURE add_team_target_position_opening_closure_team_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'team'
             AND table_schema = DATABASE()
             AND column_name = 'team_target_position_opening_closure' ) THEN 
             
      ALTER TABLE `team` 
      ADD COLUMN `team_target_position_opening_closure` INT NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_team_target_position_opening_closure_team_member_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'team_member'
             AND table_schema = DATABASE()
             AND column_name = 'target_position_opening_closure' ) THEN 
             
      ALTER TABLE `team_member` 
      ADD COLUMN `target_position_opening_closure` INT NOT NULL DEFAULT 0;
      
    END IF;
    
END $$
DELIMITER ;

call add_team_target_position_opening_closure_team_table();
call add_team_target_position_opening_closure_team_member_table();