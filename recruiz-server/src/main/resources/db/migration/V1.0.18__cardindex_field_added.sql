ALTER TABLE `round_candidate` 
ADD COLUMN `cardIndex` int(5) AFTER `round_id`;

ALTER TABLE `round_candidate_audit` 
ADD COLUMN `cardIndex` int(5) AFTER `round_id`;