drop procedure IF EXISTS add_billing_column_org_table;

DELIMITER $$
CREATE PROCEDURE add_billing_column_org_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'tax_registration_id' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `tax_registration_id` varchar(255) DEFAULT NULL;
      
    END IF; 
    

    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'tax_registration_id' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `tax_registration_id` varchar(255) DEFAULT NULL;
      
    END IF;
    
    #    adding gst colummn 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'gst_id' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `gst_id` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'gst_id' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `gst_id` varchar(255) DEFAULT NULL;
      
    END IF; 
    
    #    adding address colummn 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization'
             AND table_schema = DATABASE()
             AND column_name = 'address' ) THEN 
             
      ALTER TABLE `organization` 
      ADD COLUMN `address` longtext DEFAULT NULL;
      
    END IF; 
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'organization_audit'
             AND table_schema = DATABASE()
             AND column_name = 'address' ) THEN 
             
      ALTER TABLE `organization_audit` 
      ADD COLUMN `address` longtext DEFAULT NULL;
      
    END IF; 
    
END $$
DELIMITER ;

CALL add_billing_column_org_table();