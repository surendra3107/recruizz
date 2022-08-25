DROP PROCEDURE IF EXISTS add_corporateId_recruiz_connect_column_position;

DELIMITER $$
CREATE PROCEDURE add_corporateId_recruiz_connect_column_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'connect_corporate_id' ) THEN 
             
      ALTER TABLE `position` ADD COLUMN `connect_corporate_id` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_corporateId_recruiz_connect_column_position_audit;

DELIMITER $$
CREATE PROCEDURE add_corporateId_recruiz_connect_column_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'connect_corporate_id' ) THEN 
             
      ALTER TABLE `position_audit` ADD COLUMN `connect_corporate_id` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_instanceId_recruiz_connect_column_position;

DELIMITER $$
CREATE PROCEDURE add_instanceId_recruiz_connect_column_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'connect_instance_id' ) THEN 
             
      ALTER TABLE `position` ADD COLUMN `connect_instance_id` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_instanceId_recruiz_connect_column_position_position_audit;

DELIMITER $$
CREATE PROCEDURE add_instanceId_recruiz_connect_column_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'connect_instance_id' ) THEN 
             
      ALTER TABLE `position_audit` ADD COLUMN `connect_instance_id` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_publish_mode_recruiz_connect_column_position;

DELIMITER $$
CREATE PROCEDURE add_publish_mode_recruiz_connect_column_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'publish_mode' ) THEN 
             
      ALTER TABLE `position` ADD COLUMN `publish_mode` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_publish_mode_recruiz_connect_column_position_audit;

DELIMITER $$
CREATE PROCEDURE add_publish_mode_recruiz_connect_column_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'publish_mode' ) THEN 
             
      ALTER TABLE `position_audit` ADD COLUMN `publish_mode` varchar(200) DEFAULT NULL;

    END IF;
    
END $$
DELIMITER ;

call add_corporateId_recruiz_connect_column_position();
call add_corporateId_recruiz_connect_column_position_audit();
call add_instanceId_recruiz_connect_column_position();
call add_instanceId_recruiz_connect_column_position_audit();
call add_publish_mode_recruiz_connect_column_position();
call add_publish_mode_recruiz_connect_column_position_audit();
