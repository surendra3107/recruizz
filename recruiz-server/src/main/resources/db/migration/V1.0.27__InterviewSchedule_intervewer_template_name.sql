ALTER TABLE `interview_schedule` 
ADD COLUMN `interviewerTemplateName` longtext NULL AFTER `interviewerTemplateSubject`;

ALTER TABLE `interview_schedule_audit` 
ADD COLUMN `interviewerTemplateName` longtext NULL AFTER `interviewerTemplateSubject`;
