delete from email_template_data where name = 'Reach Candidates';

DELIMITER $$
DROP PROCEDURE IF EXISTS `add_name_unique_constraint_email_template` $$
CREATE PROCEDURE add_name_unique_constraint_email_template()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='email_template_data' AND index_name='name_UNIQUE' ) THEN
ALTER TABLE `email_template_data` ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC);
      
    END IF; 
    
END $$
DELIMITER ;

call add_name_unique_constraint_email_template();