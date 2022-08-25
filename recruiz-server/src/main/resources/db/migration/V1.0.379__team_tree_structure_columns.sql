
DROP PROCEDURE IF EXISTS addParentIdColumn;
DROP PROCEDURE IF EXISTS dropParentIdFromTeamMemberColumn;


DELIMITER $$
CREATE PROCEDURE addParentIdColumn()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'team'
             AND table_schema = DATABASE()
             AND column_name = 'parent_id' ) THEN 
             
      ALTER TABLE `team` 
      ADD COLUMN `parent_id` bigint(20) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE dropParentIdFromTeamMemberColumn()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'team_member'
             AND table_schema = DATABASE()
             AND column_name = 'parent_id' ) THEN 
             
      ALTER TABLE `team_member` 
      DROP COLUMN `parent_id` ;
      
    END IF;
    
END $$
DELIMITER ;

call addParentIdColumn();
call dropParentIdFromTeamMemberColumn();

