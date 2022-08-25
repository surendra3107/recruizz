ALTER TABLE `notification` 
ADD COLUMN `interviewScheduleId` BIGINT(20) NULL AFTER `roundCandidateId`;


ALTER TABLE `notification_audit` 
ADD COLUMN `interviewScheduleId` BIGINT(20) NULL AFTER `roundCandidateId`;


