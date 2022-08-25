DROP PROCEDURE IF EXISTS add_publish_careersite_column_position;

DELIMITER $$
CREATE PROCEDURE add_publish_careersite_column_position()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'publish_career_site' ) THEN 
             
      ALTER TABLE `position` ADD COLUMN `publish_career_site` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;


DROP PROCEDURE IF EXISTS add_publish_careersite_column_position_audit;

DELIMITER $$
CREATE PROCEDURE add_publish_careersite_column_position_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'publish_career_site' ) THEN 
             
      ALTER TABLE `position_audit` ADD COLUMN `publish_career_site` BIT(1) NOT NULL DEFAULT false;

    END IF;
    
END $$
DELIMITER ;

call add_publish_careersite_column_position();
call add_publish_careersite_column_position_audit();
