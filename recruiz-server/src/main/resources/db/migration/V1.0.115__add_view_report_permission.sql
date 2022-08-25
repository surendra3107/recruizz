DELETE from `permission` where permissionName = 'View Reports';

INSERT INTO `permission` (`role_name`, `permissionName`)
select id, 'View Reports' from `user_roles` where role_name = 'Super Admin';