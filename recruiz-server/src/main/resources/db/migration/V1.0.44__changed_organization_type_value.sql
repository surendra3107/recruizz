
UPDATE `organization` 
SET `orgType`='Corporate'
WHERE `orgType`='Company';

UPDATE `organization_audit` 
SET `orgType`='Corporate'
WHERE `orgType`='Company';