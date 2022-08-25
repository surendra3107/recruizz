DROP PROCEDURE IF EXISTS add_root_team_column;

DELIMITER $$
CREATE PROCEDURE add_root_team_column()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'team'
             AND table_schema = DATABASE()
             AND column_name = 'root_team' ) THEN

      ALTER TABLE `team`
      ADD COLUMN `root_team` bit(1) DEFAULT NULL;

    END IF;

END $$
DELIMITER ;

CALL add_root_team_column();