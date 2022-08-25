DROP PROCEDURE IF EXISTS add_custom_report_in_or_setting;

DELIMITER $$
CREATE PROCEDURE add_custom_report_in_or_setting()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE table_name = 'org_config'
             AND table_schema = DATABASE()
             AND column_name = 'customReportEnabled' ) THEN

      ALTER TABLE `org_config`
      ADD COLUMN `customReportEnabled` BIT(1);

    END IF;

END $$
DELIMITER ;

call add_custom_report_in_or_setting();
