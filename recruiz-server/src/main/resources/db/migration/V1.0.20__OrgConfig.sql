CREATE TABLE IF NOT EXISTS `org_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `licence` longtext,
  `setting_info` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE `organization` 
ADD COLUMN `organizationConfiguration_id` bigint(20) NULL AFTER `time_preference`;



ALTER TABLE organization ADD FOREIGN KEY (organizationConfiguration_id) REFERENCES org_config(id);


ALTER TABLE `organization_audit` 
ADD COLUMN `organizationConfiguration_id` bigint(20) NULL AFTER `time_preference`;

ALTER TABLE organization_audit ADD FOREIGN KEY (organizationConfiguration_id) REFERENCES org_config(id);

