
SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = database()
and COLUMN_NAME = 'api_token'
AND table_name = 'user';

set @query1 = IF(@exist <= 0, 'alter table user add column api_token varchar(5000) NULL', 
'select \'Column Exists\' status');

prepare stmt1 from @query1;

EXECUTE stmt1;

SELECT count(*)
INTO @exist
FROM information_schema.columns 
WHERE table_schema = database()
and COLUMN_NAME = 'api_token'
AND table_name = 'user_audit';

set @query2 = IF(@exist <= 0, 'alter table user_audit add column api_token varchar(5000) NULL', 
'select \'Column Exists\' status');

prepare stmt2 from @query2;

EXECUTE stmt2;

