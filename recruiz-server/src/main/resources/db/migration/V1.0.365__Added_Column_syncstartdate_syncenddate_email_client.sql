ALTER TABLE `email_client_details` 
ADD COLUMN `lastFetchedStartDate` DATETIME NULL,
ADD COLUMN `lastFetchedEndDate` DATETIME NULL;
