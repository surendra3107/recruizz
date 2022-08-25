DROP PROCEDURE IF EXISTS make_position_industry_null;
DROP PROCEDURE IF EXISTS make_position_functionalArea_null;
DROP PROCEDURE IF EXISTS make_position_type_null;

DELIMITER $$
CREATE PROCEDURE make_position_industry_null()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'industry' ) THEN

      ALTER TABLE `position`
      MODIFY `industry` VARCHAR(255) DEFAULT NULL;

    END IF;

END $$
DELIMITER ;

CALL make_position_industry_null();

DELIMITER $$
CREATE PROCEDURE make_position_functionalArea_null()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'functionalArea' ) THEN

      ALTER TABLE `position`
      MODIFY `functionalArea` VARCHAR(255) DEFAULT NULL;

    END IF;

END $$
DELIMITER ;

CALL make_position_functionalArea_null();

DELIMITER $$
CREATE PROCEDURE make_position_type_null()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'type' ) THEN

      ALTER TABLE `position`
      MODIFY `type` VARCHAR(255) DEFAULT NULL;

    END IF;

END $$
DELIMITER ;

CALL make_position_type_null();



