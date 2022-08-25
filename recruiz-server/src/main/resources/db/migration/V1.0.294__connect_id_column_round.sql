DROP PROCEDURE IF EXISTS add_connect_id_column_round;

DELIMITER $$
CREATE PROCEDURE add_connect_id_column_round()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'rounds'
             AND table_schema = DATABASE()
             AND column_name = 'connect_id' ) THEN 
             
      ALTER TABLE `rounds` ADD COLUMN `connect_id` varchar(255) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_connect_id_column_round_audit;

DELIMITER $$
CREATE PROCEDURE add_connect_id_column_round_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'rounds_audit'
             AND table_schema = DATABASE()
             AND column_name = 'connect_id' ) THEN 
             
      ALTER TABLE `rounds_audit` ADD COLUMN `connect_id` varchar(255) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS `add_connect_id_unique_constraint_rounds`;
DELIMITER $$
CREATE PROCEDURE add_connect_id_unique_constraint_rounds()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='rounds' AND index_name='UK_connectid_0a3tfwne9ny' ) THEN
	ALTER TABLE `rounds` ADD UNIQUE INDEX `UK_connectid_0a3tfwne9ny` (`connect_id` ASC);
      
    END IF; 
    
END $$
DELIMITER ;

call add_connect_id_column_round();
call add_connect_id_column_round_audit();
call add_connect_id_unique_constraint_rounds();
