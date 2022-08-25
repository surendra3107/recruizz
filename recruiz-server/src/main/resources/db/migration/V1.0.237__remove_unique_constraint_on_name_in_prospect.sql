DROP PROCEDURE IF EXISTS remove_unique_constraints_on_name_in_prospect_table;

DELIMITER $$
CREATE PROCEDURE remove_unique_constraints_on_name_in_prospect_table()
BEGIN
		IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
			'prospect' AND index_name = 'UK_hwk4a7mtk4edtg8ipatrs9rgw') > 0) THEN
             
      ALTER TABLE `prospect` DROP INDEX UK_hwk4a7mtk4edtg8ipatrs9rgw;
     
    END IF;
    
END $$
DELIMITER ;

call remove_unique_constraints_on_name_in_prospect_table();
