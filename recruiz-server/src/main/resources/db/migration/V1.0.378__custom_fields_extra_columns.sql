
DROP PROCEDURE IF EXISTS addDataTypeForCustomField;
DROP PROCEDURE IF EXISTS addDropDownValuesForCustomField;

DELIMITER $$
CREATE PROCEDURE addDataTypeForCustomField()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'customfields'
             AND table_schema = DATABASE()
             AND column_name = 'dataType' ) THEN 
             
      ALTER TABLE `customfields` 
      ADD COLUMN `dataType` varchar(255);
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE addDropDownValuesForCustomField()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'customfields'
             AND table_schema = DATABASE()
             AND column_name = 'dropDownValues' ) THEN 
             
      ALTER TABLE `customfields` 
      ADD COLUMN `dropDownValues` varchar(1000);
      
    END IF;
    
END $$
DELIMITER ;

call addDataTypeForCustomField();
call addDropDownValuesForCustomField();
