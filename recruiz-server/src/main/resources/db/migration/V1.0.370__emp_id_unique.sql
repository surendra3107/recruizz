update employee set emp_id = null where id>0;

ALTER TABLE `employee` 
CHANGE COLUMN `emp_id` `emp_id` VARCHAR(255) NULL ,
ADD UNIQUE INDEX `emp_id_UNIQUE` (`emp_id` ASC);
