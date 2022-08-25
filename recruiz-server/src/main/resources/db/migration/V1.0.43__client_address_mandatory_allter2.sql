-- use this procedure if trying to alter table and add column

DROP PROCEDURE IF EXISTS alterProcedure;

DELIMITER $$

CREATE DEFINER=CURRENT_USER PROCEDURE alterProcedure ( ) 
BEGIN
DECLARE ratings TEXT;
SELECT count(*) INTO ratings
FROM information_schema.columns 
WHERE table_name = 'feedback'
AND column_name = 'ratings' AND TABLE_SCHEMA = (SELECT DATABASE());

IF ratings is null THEN 
    ALTER TABLE `feedback` ADD COLUMN `ratings` VARCHAR(255) NULL AFTER `round_candidate`;
END IF;
END$$

DELIMITER ;

CALL alterProcedure;

DROP PROCEDURE alterProcedure;