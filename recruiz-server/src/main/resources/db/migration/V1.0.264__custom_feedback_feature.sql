drop procedure IF EXISTS add_levelbar_feedback_interviewschedule;

DELIMITER $$
CREATE PROCEDURE add_levelbar_feedback_interviewschedule()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'interview_schedule'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_share_id' ) THEN 
             
      ALTER TABLE `interview_schedule` 
      ADD COLUMN `feedback_share_id` VARCHAR(100) NULL AFTER `profileMasked`;
      
    END IF;
    
END $$
DELIMITER ;


drop procedure IF EXISTS add_levelbar_feedback_interviewschedule_audit;

DELIMITER $$
CREATE PROCEDURE add_levelbar_feedback_interviewschedule_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'interview_schedule_audit'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_share_id' ) THEN 
             
      ALTER TABLE `interview_schedule_audit` 
      ADD COLUMN `feedback_share_id` VARCHAR(100) NULL AFTER `profileMasked`;

      
    END IF;
    
END $$
DELIMITER ;

drop procedure IF EXISTS add_levelbar_feedback_share;

DELIMITER $$
CREATE PROCEDURE add_levelbar_feedback_share()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'feedback'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_share_result_id' ) THEN 
             
      ALTER TABLE `feedback` 
      ADD COLUMN `feedback_share_result_id` VARCHAR(100) NULL AFTER `roundName`;

      
    END IF;
    
END $$
DELIMITER ;

drop procedure IF EXISTS add_levelbar_feedback_share_audit;

DELIMITER $$
CREATE PROCEDURE add_levelbar_feedback_share_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'feedback_audit'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_share_result_id' ) THEN 
             
      ALTER TABLE `feedback_audit` 
      ADD COLUMN `feedback_share_result_id` VARCHAR(100) NULL AFTER `roundName`;

      
    END IF;
    
END $$
DELIMITER ;


call add_levelbar_feedback_interviewschedule();
call add_levelbar_feedback_interviewschedule_audit();
call add_levelbar_feedback_share();
call add_levelbar_feedback_share_audit();