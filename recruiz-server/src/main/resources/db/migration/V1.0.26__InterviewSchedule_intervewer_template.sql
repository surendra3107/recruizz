ALTER TABLE `interview_schedule` 
ADD COLUMN `interviewerTemplateSubject` longtext NULL AFTER `templateSubject`,
ADD COLUMN `interviewerTemplateData` longtext NULL AFTER `interviewerTemplateSubject`;

ALTER TABLE `interview_schedule_audit` 
ADD COLUMN `interviewerTemplateSubject` longtext NULL AFTER `templateSubject`,
ADD COLUMN `interviewerTemplateData` longtext NULL AFTER `interviewerTemplateSubject`;