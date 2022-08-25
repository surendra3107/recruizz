ALTER TABLE `generic_interviewer` 
CHANGE COLUMN `mobile` `mobile` VARCHAR(255) NULL ,
DROP INDEX `UK_rdhri359n4n1v6mnqkxfamfnl` ;

ALTER TABLE `generic_decisionmaker` 
CHANGE COLUMN `mobile` `mobile` VARCHAR(255) NULL ,
DROP INDEX `UK_his9p1huim4lw7s9yujiw731m` ;
