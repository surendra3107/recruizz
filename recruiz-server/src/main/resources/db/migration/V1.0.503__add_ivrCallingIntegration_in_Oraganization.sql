
DROP PROCEDURE IF EXISTS add_ivrCallingIntegration_in_organization;
DROP PROCEDURE IF EXISTS add_ivrCallingIntegration_in_organizationAudit;

DELIMITER $$
CREATE PROCEDURE add_ivrCallingIntegration_in_organization()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'ivrCallingIntegration' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `ivrCallingIntegration` varchar(255) DEFAULT NULL;
      
    END IF; 
	
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_ivrCallingIntegration_in_organizationAudit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'ivrCallingIntegration' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `ivrCallingIntegration` varchar(255) DEFAULT NULL;
      
    END IF; 
	
END $$
DELIMITER ;

CALL add_ivrCallingIntegration_in_organizationAudit;
CALL add_ivrCallingIntegration_in_organization;