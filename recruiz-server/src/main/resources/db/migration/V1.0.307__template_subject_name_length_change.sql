
DROP PROCEDURE IF EXISTS remove_unique_constraints_on_name_email_template;

DELIMITER $$
CREATE PROCEDURE remove_unique_constraints_on_name_email_template()
BEGIN
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_template_data' AND index_name = 'name_UNIQUE') > 0) THEN
             
      ALTER TABLE `email_template_data` DROP INDEX `name_UNIQUE` ;
     
    END IF;
    
END $$
DELIMITER ;

call remove_unique_constraints_on_name_email_template();


ALTER TABLE `email_template_data` 
CHANGE COLUMN `name` `name` varchar(555) NULL DEFAULT NULL ,
CHANGE COLUMN `subject` `subject` varchar(555) NULL DEFAULT NULL ;

ALTER TABLE `email_template_data` ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC);
