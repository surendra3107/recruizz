DROP PROCEDURE IF EXISTS change_column_integration_profile;


DELIMITER $$
CREATE PROCEDURE change_column_integration_profile()
BEGIN
	IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'integration_details'
             AND table_schema = DATABASE()
             AND column_name = 'id' ) THEN 
             
      ALTER TABLE `integration_details` 
      CHANGE COLUMN `id` `integration_profile_id` bigint(20) NOT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'IntegrationProfileDetails'
             AND table_schema = DATABASE()
             AND column_name = 'integrationModuleType' ) THEN 
             
      ALTER TABLE `IntegrationProfileDetails` 
      CHANGE COLUMN `integrationModuleType` `integrationModuleType` varchar(200) NOT NULL;
      
    END IF;
    
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'IntegrationProfileDetails'
             AND table_schema = DATABASE()
             AND column_name = 'userEmail' ) THEN 
             
      ALTER TABLE `IntegrationProfileDetails` 
      CHANGE COLUMN `userEmail` `userEmail` varchar(200) NOT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
    					WHERE table_schema=DATABASE() 
    					AND table_name='IntegrationProfileDetails' 
    					AND index_name='UKiela06qnhf243rrhqot4r0ouw123' ) THEN
		ALTER TABLE `IntegrationProfileDetails` ADD CONSTRAINT UKiela06qnhf243rrhqot4r0ouw123 UNIQUE (`userEmail`,`integrationModuleType`);      
    END IF;

END $$
DELIMITER ;

call change_column_integration_profile();
