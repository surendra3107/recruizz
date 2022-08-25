DROP PROCEDURE IF EXISTS add_candidate_sha1_hash;
DROP PROCEDURE IF EXISTS add_external_app_candidate_id;

DELIMITER $$
CREATE PROCEDURE add_candidate_sha1_hash()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'candidate_sha1_hash' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `candidate_sha1_hash` varchar(255) NULL DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_candidate_sha1_hash();

DELIMITER $$
CREATE PROCEDURE add_external_app_candidate_id()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'candidate'
             AND table_schema = DATABASE()
             AND column_name = 'external_app_candidate_id' ) THEN 
             
      ALTER TABLE `candidate` 
      ADD COLUMN `external_app_candidate_id` varchar(255) NULL DEFAULT NULL;
      
    END IF;
    
END $$
DELIMITER ;

call add_external_app_candidate_id();



DELIMITER $$
DROP PROCEDURE IF EXISTS `drop_candidate_unique_index` $$
CREATE PROCEDURE drop_candidate_unique_index()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='candidate' AND index_name='UK_mm03i5fptbrfs3d6kvncqx2dl' ) THEN
		ALTER TABLE `candidate` ADD CONSTRAINT UK_mm03i5fptbrfs3d6kvncqx2dl UNIQUE (candidate_sha1_hash);      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='candidate' AND index_name='UK_2i7ubsxdgghn50841nyhbvqim' ) THEN
		ALTER TABLE `candidate` ADD CONSTRAINT UK_2i7ubsxdgghn50841nyhbvqim UNIQUE (external_app_candidate_id);      
    END IF;
    
END $$
DELIMITER ;

call drop_candidate_unique_index();
