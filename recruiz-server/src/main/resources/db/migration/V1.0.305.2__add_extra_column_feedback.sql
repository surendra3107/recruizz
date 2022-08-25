DROP PROCEDURE IF EXISTS add_extra_column_feedback;
DROP PROCEDURE IF EXISTS add_extra_column_feedback_audit;

DELIMITER $$
CREATE PROCEDURE add_extra_column_feedback()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'feedback' AND table_schema = DATABASE() AND column_name = 'event_creator_email' ) THEN
           
ALTER TABLE `feedback` 
ADD COLUMN `event_creator_email` VARCHAR(255) NULL AFTER `feedback_share_result_id`,
ADD COLUMN `profileMasked` BIT(1) NULL DEFAULT 0 AFTER `event_creator_email`;

   END IF;
   
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_extra_column_feedback_audit()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'feedback_audit' AND table_schema = DATABASE() AND column_name = 'event_creator_email' ) THEN
           
ALTER TABLE `feedback_audit` 
ADD COLUMN `event_creator_email` VARCHAR(255) NULL AFTER `feedback_share_result_id`,
ADD COLUMN `profileMasked` BIT(1) NULL DEFAULT 0 AFTER `event_creator_email`;

   END IF;
   
END $$
DELIMITER ;



call add_extra_column_feedback();
call add_extra_column_feedback_audit();