
DROP PROCEDURE IF EXISTS alterTeamColumType;
DROP PROCEDURE IF EXISTS alterTeamMemeberColumType;


DELIMITER $$
CREATE PROCEDURE alterTeamColumType()
BEGIN
             
      ALTER TABLE `team` 
      MODIFY  `team_target_amount` bigint(20) DEFAULT NULL;
      
      ALTER TABLE `team` 
      MODIFY  `team_target_position_opening_closure` bigint(20) DEFAULT NULL;
      
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE alterTeamMemeberColumType()
BEGIN
             
      ALTER TABLE `team_member` 
      MODIFY  `target_amount` bigint(20) DEFAULT NULL;
      
      ALTER TABLE `team_member` 
      MODIFY  `target_position_opening_closure` bigint(20) DEFAULT NULL;
    
END $$
DELIMITER ;

call alterTeamColumType();
call alterTeamMemeberColumType();

