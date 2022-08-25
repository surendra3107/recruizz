DROP PROCEDURE IF EXISTS add_extra_column_organization_tbl;
DROP PROCEDURE IF EXISTS add_extra_column_organization_audit;

DELIMITER $$
CREATE PROCEDURE add_extra_column_organization_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'organization' AND table_schema = DATABASE() AND column_name = 'address_l1' ) THEN
           
ALTER TABLE `organization` 
ADD COLUMN `address_l1` LONGTEXT,
ADD COLUMN `address_l2` LONGTEXT NULL AFTER `address_l1`,
ADD COLUMN `city` VARCHAR(255) NULL AFTER `address_l2`,
ADD COLUMN `pincode` VARCHAR(255) NULL AFTER `city`,
ADD COLUMN `country` VARCHAR(255) NULL AFTER `pincode`,
ADD COLUMN `state` VARCHAR(255) NULL AFTER `country`,
ADD COLUMN `phone` VARCHAR(255) NULL AFTER `state`;


   END IF;
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_extra_column_organization_audit()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'organization_audit' AND table_schema = DATABASE() AND column_name = 'address_l1' ) THEN
           
ALTER TABLE `organization_audit` 
ADD COLUMN `address_l1` LONGTEXT,
ADD COLUMN `address_l2` LONGTEXT NULL AFTER `address_l1`,
ADD COLUMN `city` VARCHAR(255) NULL AFTER `address_l2`,
ADD COLUMN `pincode` VARCHAR(255) NULL AFTER `city`,
ADD COLUMN `country` VARCHAR(255) NULL AFTER `pincode`,
ADD COLUMN `state` VARCHAR(255) NULL AFTER `country`,
ADD COLUMN `phone` VARCHAR(255) NULL AFTER `state`;

   END IF;
   
END $$
DELIMITER ;



call add_extra_column_organization_tbl();
call add_extra_column_organization_audit();