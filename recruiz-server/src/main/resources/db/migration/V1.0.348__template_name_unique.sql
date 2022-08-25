ALTER TABLE `onboarding_templates` 
CHANGE COLUMN `name` `name` VARCHAR(1000) NOT NULL ,
ADD UNIQUE INDEX `name_UNIQUE` (`name` ASC);
