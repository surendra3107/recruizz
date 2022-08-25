DELETE from `permission` where permissionName = 'Generate Invoice';

INSERT INTO `permission` (`role_name`, `permissionName`)
select id, 'Generate Invoice' from `user_roles` where role_name = 'Super Admin';