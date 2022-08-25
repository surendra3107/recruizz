DELETE from `permission` where permissionName = 'Career Site';

INSERT INTO `permission` (`role_name`, `permissionName`)
select id, 'Career Site' from `user_roles` where role_name = 'Super Admin';