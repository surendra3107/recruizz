DROP PROCEDURE IF EXISTS add_extra_column_feedback_expected_status;
DROP PROCEDURE IF EXISTS add_extra_column_feedback_expected_status_audit;

DELIMITER $$
CREATE PROCEDURE add_extra_column_feedback_expected_status()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'interview_schedule' AND table_schema = DATABASE() AND column_name = 'feedbackExpected' ) THEN
           
ALTER TABLE `interview_schedule` 
ADD COLUMN `feedbackExpected` BIT(1) NULL DEFAULT 1 AFTER `feedback_que_set_id`;
   END IF;
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_extra_column_feedback_expected_status_audit()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'interview_schedule_audit' AND table_schema = DATABASE() AND column_name = 'feedbackExpected' ) THEN
           
ALTER TABLE `interview_schedule_audit` 
ADD COLUMN `feedbackExpected` BIT(1) NULL DEFAULT 1 AFTER `feedback_que_set_id`;
   END IF;
   
END $$
DELIMITER ;



call add_extra_column_feedback_expected_status();
call add_extra_column_feedback_expected_status_audit();