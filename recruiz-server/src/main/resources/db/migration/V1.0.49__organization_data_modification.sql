ALTER TABLE `organization` 
ADD COLUMN `logoUrlPath` VARCHAR(255) NULL AFTER `organizationConfiguration_id`,
ADD COLUMN `websiteUrl` VARCHAR(255) NULL AFTER `logoUrlPath`,
ADD COLUMN `facebookUrl` VARCHAR(255) NULL AFTER `websiteUrl`,
ADD COLUMN `twitterUrl` VARCHAR(255) NULL AFTER `facebookUrl`,
ADD COLUMN `googleUrl` VARCHAR(255) NULL AFTER `twitterUrl`,
ADD COLUMN `linkedInUrl` VARCHAR(255) NULL AFTER `googleUrl`,
ADD COLUMN `slackUrl` VARCHAR(255) NULL AFTER `linkedInUrl`,
ADD COLUMN `gitHubUrl` VARCHAR(255) NULL AFTER `slackUrl`,
ADD COLUMN `hipChatUrl` VARCHAR(255) NULL AFTER `gitHubUrl`,
ADD COLUMN `bitBucketUrl` VARCHAR(255) NULL AFTER `hipChatUrl`;


ALTER TABLE `organization_audit` 
ADD COLUMN `logoUrlPath` VARCHAR(255) NULL AFTER `organizationConfiguration_id`,
ADD COLUMN `websiteUrl` VARCHAR(255) NULL AFTER `logoUrlPath`,
ADD COLUMN `facebookUrl` VARCHAR(255) NULL AFTER `websiteUrl`,
ADD COLUMN `twitterUrl` VARCHAR(255) NULL AFTER `facebookUrl`,
ADD COLUMN `googleUrl` VARCHAR(255) NULL AFTER `twitterUrl`,
ADD COLUMN `linkedInUrl` VARCHAR(255) NULL AFTER `googleUrl`,
ADD COLUMN `slackUrl` VARCHAR(255) NULL AFTER `linkedInUrl`,
ADD COLUMN `gitHubUrl` VARCHAR(255) NULL AFTER `slackUrl`,
ADD COLUMN `hipChatUrl` VARCHAR(255) NULL AFTER `gitHubUrl`,
ADD COLUMN `bitBucketUrl` VARCHAR(255) NULL AFTER `hipChatUrl`;
