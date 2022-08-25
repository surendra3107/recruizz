ALTER TABLE `email_message_model_audit` 
CHANGE COLUMN `htmlBody` `htmlBody` LONGTEXT NULL DEFAULT NULL ,
CHANGE COLUMN `messageHeader` `messageHeader` LONGTEXT NULL DEFAULT NULL ,
CHANGE COLUMN `textBody` `textBody` LONGTEXT NULL DEFAULT NULL ;
