ALTER TABLE `candidate_assesment` 
ADD COLUMN `totalScoreDouble` BIGINT(20) NULL AFTER `resultScore`,
ADD COLUMN `resultScoreDouble` BIGINT(20) NULL AFTER `totalScoreDouble`,
ADD COLUMN `status` VARCHAR(255) NULL AFTER `resultScoreDouble`;
