DROP PROCEDURE IF EXISTS add_total_amount_after_discount_column_agency_invoice_tbl;

DELIMITER $$
CREATE PROCEDURE add_total_amount_after_discount_column_agency_invoice_tbl()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'agency_invoice' AND table_schema = DATABASE() AND column_name = 'total_amount_after_discount' ) THEN
           
ALTER TABLE `agency_invoice` ADD COLUMN `total_amount_after_discount` double DEFAULT 0;

   END IF;
   
END $$
DELIMITER ;

call add_total_amount_after_discount_column_agency_invoice_tbl();