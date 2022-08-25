ALTER TABLE `user` 
ADD COLUMN `isNotificationOn` BIT(1) NULL AFTER `last_logged_on_time`;


ALTER TABLE `user_audit` 
ADD COLUMN `isNotificationOn` BIT(1) NULL AFTER `last_logged_on_time`;
