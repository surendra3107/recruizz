DROP PROCEDURE IF EXISTS usage_db_time_diff_column;

DELIMITER $$
CREATE PROCEDURE usage_db_time_diff_column()
BEGIN	
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = DATABASE() AND table_schema = 'tenant_usage_db') THEN 
      SET @queryStatment = CONCAT('delete from tenant_usage_db.',DATABASE(),' where id > 0');
             
      PREPARE stmt1 FROM @queryStatment;
      EXECUTE stmt1;
      DEALLOCATE PREPARE stmt1;

     END IF; 
    
END $$
DELIMITER ;

CALL usage_db_time_diff_column();