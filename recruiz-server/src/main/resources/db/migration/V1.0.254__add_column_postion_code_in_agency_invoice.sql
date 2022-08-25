
CREATE TABLE IF NOT EXISTS `agency_invoice` (
  `invoiceId` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `amount` double NOT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `discount` double NOT NULL,
  `dueDate` datetime DEFAULT NULL,
  `informationFilledByUser` varchar(255) DEFAULT NULL,
  `invoiceStatus` varchar(255) DEFAULT NULL,
  `joiningDate` datetime DEFAULT NULL,
  `offeredDate` datetime DEFAULT NULL,
  `offeredSalary` double NOT NULL,
  `paymentReceived` double NOT NULL,
  `paymentReceivedDate` datetime DEFAULT NULL,
  `postionName` varchar(255) DEFAULT NULL,
  `taxes` double NOT NULL,
  `totalAmount` double NOT NULL,
  `totalAmountInWords` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`invoiceId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


drop procedure IF EXISTS add_postion_code_in_agency_invoice_table;
DELIMITER $$
CREATE PROCEDURE add_postion_code_in_agency_invoice_table()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'agency_invoice'
             AND table_schema = DATABASE()
             AND column_name = 'postion_code' ) THEN 
             
      ALTER TABLE `agency_invoice` 
      ADD COLUMN `postion_code`  varchar(255) DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_postion_code_in_agency_invoice_table();
