DROP PROCEDURE IF EXISTS add_column_duplicateCheck_in_organization_table;
DROP PROCEDURE IF EXISTS add_column_duplicateCheck_in_organizationAudit_table;

DELIMITER $$
CREATE PROCEDURE add_column_duplicateCheck_in_organization_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'duplicateCheck' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `duplicateCheck` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_column_duplicateCheck_in_organizationAudit_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'duplicateCheck' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `duplicateCheck` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_column_duplicateCheck_in_organizationAudit_table;
CALL add_column_duplicateCheck_in_organization_table;