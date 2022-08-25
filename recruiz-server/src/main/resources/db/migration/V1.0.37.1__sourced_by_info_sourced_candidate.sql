ALTER TABLE `round_candidate` 
ADD COLUMN `sourcedBy` VARCHAR(255) NULL AFTER `cardIndex`;


ALTER TABLE `round_candidate_audit`
ADD COLUMN `sourcedBy` VARCHAR(255) NULL AFTER `cardIndex`;