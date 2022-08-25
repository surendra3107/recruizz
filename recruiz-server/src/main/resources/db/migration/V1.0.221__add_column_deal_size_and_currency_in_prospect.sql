DELIMITER $$
CREATE PROCEDURE add_deal_size_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'deal_size' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `deal_size` double NOT NULL DEFAULT 0.0;
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_currency_in_prospect_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'prospect'
             AND table_schema = DATABASE()
             AND column_name = 'currency' ) THEN 
             
      ALTER TABLE `prospect` 
      ADD COLUMN `currency` VARCHAR(255) NOT NULL DEFAULT 'Rupee';
      
    END IF;
    
END $$
DELIMITER ;

call add_deal_size_in_prospect_table();
call add_currency_in_prospect_table();