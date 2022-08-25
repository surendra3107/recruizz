DROP PROCEDURE IF EXISTS add_documentsCheck_mandatoryDocs_in_organization;
DROP PROCEDURE IF EXISTS add_documentsCheck_mandatoryDocs_in_organizationAudit;

DELIMITER $$
CREATE PROCEDURE add_documentsCheck_mandatoryDocs_in_organization()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'documentsCheck' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `documentsCheck` varchar(255) DEFAULT NULL;
      
    END IF; 
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'mandatoryDocs' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `mandatoryDocs` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_documentsCheck_mandatoryDocs_in_organizationAudit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'documentsCheck' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `documentsCheck` varchar(255) DEFAULT NULL;
      
    END IF; 
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'mandatoryDocs' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `mandatoryDocs` varchar(255) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_documentsCheck_mandatoryDocs_in_organizationAudit;
CALL add_documentsCheck_mandatoryDocs_in_organization;