
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = database()
and COLUMN_NAME = 'last_logged_on_time'
AND table_name = 'user_audit';

set @query1 = IF(@exist <= 0, 'alter table user_audit add column last_logged_on_time datetime DEFAULT NULL', 
'select \'Column Exists\' status');

prepare stmt1 from @query1;

EXECUTE stmt1;


