DROP PROCEDURE IF EXISTS add_dummy_column_client;

DELIMITER $$
CREATE PROCEDURE add_dummy_column_client()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'client'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `client` ADD COLUMN `dummy` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_dummy_column_client_audit;

DELIMITER $$
CREATE PROCEDURE add_dummy_column_client_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'client_audit'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `client_audit` ADD COLUMN `dummy` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_dummy_column_position;

DELIMITER $$
CREATE PROCEDURE add_dummy_column_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `position` ADD COLUMN `dummy` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_dummy_column_position_audit;

DELIMITER $$
CREATE PROCEDURE add_dummy_column_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `position_audit` ADD COLUMN `dummy` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_dummy_column_candidate;

DELIMITER $$
CREATE PROCEDURE add_dummy_column_candidate()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'dummy' ) THEN 
             
      ALTER TABLE `candidate` ADD COLUMN `dummy` BIT(1) NOT NULL DEFAULT false;
      
    END IF;
    
END $$
DELIMITER ;


call add_dummy_column_client();
call add_dummy_column_client_audit();
call add_dummy_column_position_audit();
call add_dummy_column_position();
call add_dummy_column_candidate();
