DROP PROCEDURE IF EXISTS remove_unique_constraints_from_email_Client_details;

DELIMITER $$
CREATE PROCEDURE remove_unique_constraints_from_email_Client_details()
BEGIN
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_63jf7pdoi20cy4d14uiyssqt6') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_63jf7pdoi20cy4d14uiyssqt6;
     
    END IF;
    
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_jk8a7b0eejq7oaqslp40u43ii') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_jk8a7b0eejq7oaqslp40u43ii;
     
    END IF;
    
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_hg1ag09jnpxh3rrhxg2wwsedb') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_hg1ag09jnpxh3rrhxg2wwsedb;
     
    END IF;
    
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_h1i6y8fcql8iwjd1coyga6d04') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_h1i6y8fcql8iwjd1coyga6d04;
     
    END IF;
    
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_k8iu74wux2bbj5amqlvcrvic9') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_k8iu74wux2bbj5amqlvcrvic9;
     
    END IF;
    
    IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
      'email_client_details' AND index_name = 'UK_q1gik7xe6wmm6em7b9jsnf0dr') > 0) THEN
             
      ALTER TABLE `email_client_details` DROP INDEX UK_q1gik7xe6wmm6em7b9jsnf0dr;
     
    END IF;
    
END $$
DELIMITER ;

call remove_unique_constraints_from_email_Client_details();
