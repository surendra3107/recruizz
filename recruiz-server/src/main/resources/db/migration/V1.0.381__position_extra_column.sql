DROP PROCEDURE IF EXISTS add_position_column;

DELIMITER $$
CREATE PROCEDURE add_position_column()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'position'
             AND table_schema = DATABASE()
             AND column_name = 'screener' ) THEN

      ALTER TABLE `position`
      ADD COLUMN `screener` VARCHAR(255),
      ADD COLUMN `requisitionId` VARCHAR(255);

    END IF;

    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'position_audit'
             AND table_schema = DATABASE()
             AND column_name = 'screener' ) THEN

      ALTER TABLE `position_audit`
      ADD COLUMN `screener` VARCHAR(255),
      ADD COLUMN `requisitionId` VARCHAR(255);

    END IF;

END $$
DELIMITER ;

call add_position_column();
