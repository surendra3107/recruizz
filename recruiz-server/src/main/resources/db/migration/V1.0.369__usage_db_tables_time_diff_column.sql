DROP PROCEDURE IF EXISTS usage_db_time_diff_column;

DELIMITER $$
CREATE PROCEDURE usage_db_time_diff_column()
BEGIN
	
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = DATABASE() AND table_schema = 'tenant_usage_db') THEN 
             
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = DATABASE()
             AND table_schema = 'tenant_usage_db'
             AND column_name = 'last_hit_gap' ) THEN 

             SET @queryStatment = CONCAT('ALTER TABLE tenant_usage_db.',DATABASE(),' ADD COLUMN `last_hit_gap` BIGINT(20) NOT NULL DEFAULT 0');
             
      PREPARE stmt1 FROM @queryStatment;
      EXECUTE stmt1;
      DEALLOCATE PREPARE stmt1;
         
    END IF; 
     END IF; 
    
END $$
DELIMITER ;

CALL usage_db_time_diff_column();