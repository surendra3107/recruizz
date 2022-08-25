DROP PROCEDURE IF EXISTS add_pan_column_organization_tbl;
DROP PROCEDURE IF EXISTS add_pan_column_organization_audit;

DELIMITER $$
CREATE PROCEDURE add_pan_column_organization_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'organization' AND table_schema = DATABASE() AND column_name = 'pan_no' ) THEN
           
ALTER TABLE `organization` ADD COLUMN `pan_no` VARCHAR(45);

   END IF;
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_pan_column_organization_audit()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'organization_audit' AND table_schema = DATABASE() AND column_name = 'pan_no' ) THEN
           
ALTER TABLE `organization_audit` ADD COLUMN `pan_no` VARCHAR(45);

   END IF;
   
END $$
DELIMITER ;



call add_pan_column_organization_tbl();
call add_pan_column_organization_audit();