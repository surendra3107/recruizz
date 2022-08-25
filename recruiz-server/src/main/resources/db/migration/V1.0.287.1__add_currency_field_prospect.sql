drop PROCEDURE IF EXISTS add_currency_in_prospect;

DELIMITER $$
CREATE PROCEDURE add_currency_in_prospect()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'currency' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `currency` varchar(255) NULL;
      
    END IF;
    
END $$
DELIMITER ;


call add_currency_in_prospect();


