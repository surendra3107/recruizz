
UPDATE `user_roles` 
SET `role_name`='Super Admin'
WHERE `role_name`='Organization Admin';

UPDATE `permission` 
SET `permissionName`='Super Admin'
WHERE `permissionName`='Org Admin';