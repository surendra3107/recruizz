update vendor set status='1' where status='Active';
update vendor_audit set status='1' where status='Active';

ALTER TABLE `vendor`
CHANGE COLUMN `status` `status` BIT(1) NULL DEFAULT NULL ;

ALTER TABLE `vendor_audit` 
CHANGE COLUMN `status` `status` BIT(1) NULL DEFAULT NULL ;
