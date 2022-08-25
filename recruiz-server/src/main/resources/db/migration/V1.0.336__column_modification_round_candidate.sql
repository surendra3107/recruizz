DROP PROCEDURE IF EXISTS change_column_card_index;

DELIMITER $$
CREATE PROCEDURE change_column_card_index()
BEGIN
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'round_candidate'
             AND table_schema = DATABASE()
             AND column_name = 'cardIndex' ) THEN 
             
      ALTER TABLE `round_candidate` 
      CHANGE COLUMN `cardIndex` `cardIndex` double DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call change_column_card_index();
