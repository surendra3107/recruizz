drop procedure IF EXISTS add_feedback_queset_interviewschedule;

DELIMITER $$
CREATE PROCEDURE add_feedback_queset_interviewschedule()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'interview_schedule'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_que_set_id' ) THEN 
             
      ALTER TABLE `interview_schedule` 
      ADD COLUMN `feedback_que_set_id` VARCHAR(100) NULL AFTER `feedback_share_id`;
      
    END IF;
    
END $$
DELIMITER ;


drop procedure IF EXISTS add_feedback_queset_interviewschedule_audit;

DELIMITER $$
CREATE PROCEDURE add_feedback_queset_interviewschedule_audit()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'interview_schedule_audit'
             AND table_schema = DATABASE()
             AND column_name = 'feedback_que_set_id' ) THEN 
             
      ALTER TABLE `interview_schedule_audit` 
      ADD COLUMN `feedback_que_set_id` VARCHAR(100) NULL AFTER `feedback_share_id`;

      
    END IF;
    
END $$
DELIMITER ;

call add_feedback_queset_interviewschedule();
call add_feedback_queset_interviewschedule_audit();