drop PROCEDURE IF EXISTS add_campaign_email_subject;

DELIMITER $$
CREATE PROCEDURE add_campaign_email_subject()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign'
             AND table_schema = DATABASE()
             AND column_name = 'campaignSubjectTemplate' ) THEN 
             
      ALTER TABLE `campaign` 
      ADD COLUMN `campaignSubjectTemplate` longtext;
      
    END IF;
    
END $$
DELIMITER ;


drop PROCEDURE IF EXISTS add_member_name_campaign_member;

DELIMITER $$
CREATE PROCEDURE add_member_name_campaign_member()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'campaign_member'
             AND table_schema = DATABASE()
             AND column_name = 'memberName' ) THEN 
             
      ALTER TABLE `campaign_member` 
      ADD COLUMN `memberName` VARCHAR(255);
      
    END IF;
    
END $$
DELIMITER ;

call add_campaign_email_subject();
call add_member_name_campaign_member();