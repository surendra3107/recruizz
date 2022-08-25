DROP PROCEDURE IF EXISTS add_invoice_number_column_agency_invoice_tbl;


DELIMITER $$
CREATE PROCEDURE add_invoice_number_column_agency_invoice_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'agency_invoice' AND table_schema = DATABASE() AND column_name = 'invoice_number' ) THEN
           
ALTER TABLE `agency_invoice` ADD COLUMN `invoice_number` VARCHAR(45) DEFAULT NULL;

   END IF;
   
END $$
DELIMITER ;


call add_invoice_number_column_agency_invoice_tbl();
