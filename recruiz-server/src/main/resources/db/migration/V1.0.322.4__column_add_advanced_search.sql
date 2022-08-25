DROP PROCEDURE IF EXISTS add_column_to_advanced_search;

DELIMITER $$
CREATE PROCEDURE add_column_to_advanced_search()
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_type` varchar(100) NULL DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_basic_search' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_basic_search` longtext DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_show' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_show` varchar(200) NULL DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_job_status' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_job_status` varchar(200) NULL DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_sort_by' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_sort_by` varchar(100) NULL DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_min_exp' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_min_exp` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_max_exp' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_max_exp` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_min_salary' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_min_salary` double DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_max_salary' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_max_salary` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_include_zero_salary' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_include_zero_salary` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_currt_pref_loc_join_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_currt_pref_loc_join_type` varchar(20) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_exact_pre_loc' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_exact_pre_loc` bit(1) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ppg_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ppg_degree` longtext DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ppg_degree_spec' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ppg_degree_spec` longtext DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ppg_degree_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ppg_degree_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pg_postpg_join_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pg_postpg_join_type` varchar(50) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pg_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pg_degree` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pg_degree_spec' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pg_degree_spec` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pg_degree_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pg_degree_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ug_pg_join_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ug_pg_join_type` varchar(50) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ug_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ug_degree` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ug_degree_spec' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ug_degree_spec` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_ug_degree_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_ug_degree_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_higesh_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_higesh_degree` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_university' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_university` varchar(200) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_university_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_university_degree` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pass_year_from' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pass_year_from` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pass_year_to' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pass_year_to` double DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_pass_year_degree' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_pass_year_degree` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_industry' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_industry` longtext DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_functional_area' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_functional_area` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_func_role' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_func_role` longtext DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_designation' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_designation` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_designation_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_designation_type` varchar(200) DEFAULT NULL;
      
    END IF;
	
	IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_include_company' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_include_company` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_include_company_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_include_company_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_exclude_company' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_exclude_company` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_exclude_company_type' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_exclude_company_type` varchar(200) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_min_age' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_min_age` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_max_age' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_max_age` double DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_special_ability' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_special_ability` bit(1) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_premium_resume' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_premium_resume` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_sms_enable' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_sms_enable` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_verified_mobile' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_verified_mobile` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_verified_email' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_verified_email` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_attach_resume' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_attach_resume` bit(1) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_resume_not_viewd' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_resume_not_viewd` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_profile_with_photo' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_profile_with_photo` bit(1) DEFAULT NULL;
      
    END IF;
    
     IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_exclude_confidential_resume' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_exclude_confidential_resume` bit(1) DEFAULT NULL;
      
    END IF;
    
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'advanced_search_query'
             AND table_schema = DATABASE()
             AND column_name = 'advanced_search_only_female_candidate' ) THEN 
             
      ALTER TABLE `advanced_search_query` 
      ADD COLUMN `advanced_search_only_female_candidate` bit(1) DEFAULT NULL;
      
    END IF;
	
END $$
DELIMITER ; 

call add_column_to_advanced_search();
