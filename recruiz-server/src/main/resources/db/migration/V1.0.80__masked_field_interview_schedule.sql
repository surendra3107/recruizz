ALTER TABLE `interview_schedule` 
ADD COLUMN `profileMasked` BIT(1) NULL DEFAULT 0 AFTER `interviewerTemplateData`;

ALTER TABLE `interview_schedule_audit`
ADD COLUMN `profileMasked` BIT(1) NULL DEFAULT 0 AFTER `interviewerTemplateData`;
