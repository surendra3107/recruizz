DROP PROCEDURE IF EXISTS update_documentCheck_mandatoryDoc_in_organization;
DROP PROCEDURE IF EXISTS update_documentCheck_mandatoryDoc_in_organizationAudit;

DROP PROCEDURE IF EXISTS add_documentCheck_mandatoryDoc_in_organization;
DROP PROCEDURE IF EXISTS add_documentCheck_mandatoryDoc_in_organizationAudit;


DELIMITER $$
CREATE PROCEDURE documentCheck_mandatoryDoc_in_organization()
BEGIN

    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'documentsCheck' ) THEN 
             
      ALTER TABLE `organization` 
       DROP COLUMN `documentsCheck`;
      
    END IF;
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'mandatoryDocs' ) THEN 
             
      ALTER TABLE `organization` 
       DROP COLUMN `mandatoryDocs`;
      
    END IF;
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
      ADD COLUMN `mandatoryDocs` varchar(5048) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE documentCheck_mandatoryDoc_in_organizationAudit()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'documentsCheck' ) THEN 
             
      ALTER TABLE `organization` 
       DROP COLUMN `documentsCheck`;
      
    END IF;
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'mandatoryDocs' ) THEN 
             
      ALTER TABLE `organization` 
       DROP COLUMN `mandatoryDocs`;
      
    END IF;
	IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'documentsCheck' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `documentsCheck` varchar(255) DEFAULT NULL;
      
    END IF; 
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'mandatoryDocs' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `mandatoryDocs` varchar(5048) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL documentCheck_mandatoryDoc_in_organizationAudit;
CALL documentCheck_mandatoryDoc_in_organization;