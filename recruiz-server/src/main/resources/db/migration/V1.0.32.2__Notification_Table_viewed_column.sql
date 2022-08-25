
ALTER TABLE `notification` 
ADD COLUMN `viewState` bit(1) NULL AFTER `user`;

ALTER TABLE `notification_audit` 
ADD COLUMN `viewState` bit(1) NULL AFTER `user`;

