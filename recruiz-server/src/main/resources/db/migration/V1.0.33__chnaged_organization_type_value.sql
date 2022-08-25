
UPDATE `organization` 
SET `orgType`='Corporate'
WHERE `orgType`='Organization';

UPDATE `organization_audit` 
SET `orgType`='Corporate'
WHERE `orgType`='Organization';