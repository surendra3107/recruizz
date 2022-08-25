drop PROCEDURE IF EXISTS add_currency_in_prospect_position;

DELIMITER $$
CREATE PROCEDURE add_currency_in_prospect_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect_position'
             AND table_schema = DATABASE()
             AND column_name = 'currency' ) THEN 
             
      ALTER TABLE `prospect_position` 
      ADD COLUMN `currency` varchar(255) NULL;
      
    END IF;
    
END $$
DELIMITER ;


call add_currency_in_prospect_position();


