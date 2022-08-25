drop PROCEDURE IF EXISTS add_column_actionTime;

DELIMITER $$
CREATE PROCEDURE add_column_actionTime()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign_candidate_action'
             AND table_schema = DATABASE()
             AND column_name = 'actionTime' ) THEN 
             
     ALTER TABLE `campaign_candidate_action` ADD COLUMN `actionTime` DATETIME NOT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_column_actionTime();