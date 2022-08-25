DROP PROCEDURE IF EXISTS candidateFormFields_candidateMandatoryFields_org;
DROP PROCEDURE IF EXISTS candidateFormFields_candidateMandatoryFields_orgAudit;

DELIMITER $$
CREATE PROCEDURE candidateFormFields_candidateMandatoryFields_org()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'candidateFormFields' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `candidateFormFields` varchar(2048) DEFAULT NULL;
      
    END IF; 
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'candidateMandatoryFields' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `candidateMandatoryFields` varchar(2048) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE candidateFormFields_candidateMandatoryFields_orgAudit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'candidateFormFields' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `candidateFormFields` varchar(2048) DEFAULT NULL;
      
    END IF; 
	 IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'candidateMandatoryFields' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `candidateMandatoryFields` varchar(2048) DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL candidateFormFields_candidateMandatoryFields_org;
CALL candidateFormFields_candidateMandatoryFields_orgAudit;