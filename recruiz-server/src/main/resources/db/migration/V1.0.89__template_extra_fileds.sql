ALTER TABLE `email_template_data` 
ADD COLUMN `subjectVersion` BIGINT(20) NULL AFTER `category`,
ADD COLUMN `bodyVersion` BIGINT(20) NULL AFTER `subjectVersion`,
ADD COLUMN `subjectEdited` BIT(1) NULL AFTER `bodyVersion`,
ADD COLUMN `bodyEdited` BIT(1) NULL AFTER `subjectEdited`;
