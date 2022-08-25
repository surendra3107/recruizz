DROP PROCEDURE IF EXISTS add_position_column;

DELIMITER $$
CREATE PROCEDURE add_position_column()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'round_candidate'
             AND table_schema = DATABASE()
             AND column_name = 'offer_date' ) THEN

      ALTER TABLE `round_candidate`
      ADD COLUMN `offer_date` DATETIME;

    END IF;

    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'round_candidate_audit'
             AND table_schema = DATABASE()
             AND column_name = 'offer_date' ) THEN

      ALTER TABLE `round_candidate_audit`
      ADD COLUMN `offer_date` DATETIME;

    END IF;

END $$
DELIMITER ;

call add_position_column();
