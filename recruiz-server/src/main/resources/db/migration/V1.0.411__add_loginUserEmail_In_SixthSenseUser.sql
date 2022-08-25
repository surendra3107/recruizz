DROP PROCEDURE IF EXISTS add_loginUserEmail_In_SixthSenseUser;

DELIMITER $$
CREATE PROCEDURE add_loginUserEmail_In_SixthSenseUser()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'sixth_sense_user'
             AND table_schema = DATABASE()
             AND column_name = 'login_user_email' ) THEN

      ALTER TABLE `sixth_sense_user`
      ADD COLUMN `login_user_email` varchar(255);

    END IF;

END $$
DELIMITER ;

CALL add_loginUserEmail_In_SixthSenseUser();