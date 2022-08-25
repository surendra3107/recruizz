package com.bbytes.recruiz.database;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.EmailTemplateData;
import com.bbytes.recruiz.service.EmailTemplateDataService;
import com.bbytes.recruiz.service.OrganizationService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.TenantContextHolder;

/**
 * This will read email templates from resource folder and update the database
 * if the property ("db.templates.update") is set to true in properies file. if
 * the template is edited by user then it should not update the template.
 * 
 * @author sourav
 *
 */

@Service("UpdateDBTemplateService")
public class UpdateDBTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateDBTemplateService.class);

    // private String dbTemplateFolderPath = "classpath:app-email-templates";

    @Value("${agency.db.template.path}")
    private String agencyDBTemplateFolderPath;

    @Value("${corp.db.template.path}")
    private String corpDBTemplateFolderPath;

    @Autowired
    private EmailTemplateDataService emailTemplateDataService;

    @Autowired
    private OrganizationService organizationService;

    private String templateSubjectConstant = "__templateSubject__";
    // private String templateNameConstant = "__templateName__";

    /**
     * Get org type
     * 
     * @return
     */
    private String getOrgType() {
	if (organizationService.getCurrentOrganization() == null) {
	    return null;
	}

	return organizationService.getCurrentOrganization().getOrgType();
    }

    /**
     * to update email templates stored in file as async calls to speed up
     * startup time
     * 
     * @throws IOException
     */
    @Async
    public void updateDatabaseTemplateFromFile(String tenant, String orgType) throws IOException {
	TenantContextHolder.setTenant(tenant);
	logger.debug("\n\n*******************Current tenant is " + TenantContextHolder.getTenant() + "\n\n\n");
	if (null == orgType || orgType.trim().isEmpty()) {
	    orgType = getOrgType();
	}
	String dbTemplateFolderPath = null;
	if (orgType == null || orgType.trim().isEmpty()) {
	    logger.warn("\n\n\t-----------Org Type null for tenant " + TenantContextHolder.getTenant() + "\n\n\n");
	    return;
	}

	if (orgType.equalsIgnoreCase(GlobalConstants.ORG_TYPE_AGENCY)) {
	    dbTemplateFolderPath = agencyDBTemplateFolderPath;
	} else {
	    dbTemplateFolderPath = corpDBTemplateFolderPath;
	}

	logger.warn("\n\n\n\n\n*******Db template path is " + dbTemplateFolderPath + "\n\n\n\n");

	File file = new File(dbTemplateFolderPath);

	if (!file.exists()) {
	    logger.error("************Template Folder not found, exiting the update process of db template**********");
	    return;
	}

	logger.warn("**************Updating templates from the folder ************");

	// return list of all
	File[] directories = file.listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File file) {
		return file.isDirectory();
	    }
	});

	if (directories.length > 0) {
	    for (File categoryDirectory : directories) {
		Collection<File> files = FileUtils.listFiles(categoryDirectory, null, true);
		String templateFolderPath = categoryDirectory.getPath();

		String templateCategory = templateFolderPath
			.substring(templateFolderPath.lastIndexOf("/") + 1, templateFolderPath.length()).trim();

		for (File templateFile : files) {
		    String templateSubject = "", templateName = "", templateContent = "";
		    long version = 0;

		    String path = templateFile.getParent();
		    templateName = path.substring(path.lastIndexOf("/") + 1, path.length()).trim();
		    String templateFileName = templateFile.getName();
		    version = Long.parseLong(
			    templateFileName.substring(0, templateFileName.indexOf("__")).replace("v", "").trim());
		    if (templateFileName.contains(templateSubjectConstant)) {
			templateSubject = new String(Files.readAllBytes(templateFile.toPath()));
		    } else {
			templateContent = new String(Files.readAllBytes(templateFile.toPath()));
		    }

		    // calling update template here
		    try {
			updateEmailTemplate(templateCategory, templateName, templateSubject, templateContent, version);
		    } catch (Exception ex) {
			logger.warn(
				"Failed to process template : " + templateName + " under category " + templateCategory);
			logger.warn(ex.getMessage(), ex);
		    }

		}
	    }
	}
    }

    @Transactional
    private void updateEmailTemplate(String category, String templateName, String subject, String templateContent,
	    long version) {

	EmailTemplateData emailTemplate = emailTemplateDataService.getTemplateByNameAndCategory(templateName, category);
	if (emailTemplate != null) {
		logger.error(" if   Template name ="+templateName+"  emailTemplate object is not null = "+emailTemplate+" tenant name = "+TenantContextHolder.getTenant());
	    if (!subject.isEmpty()) {
		if (emailTemplate.getSubjectVersion() < version && !emailTemplate.isSubjectEdited()) {
		    emailTemplate.setSubject(subject);
		    emailTemplate.setSubjectVersion(version);
		}
	    } else {
		if (emailTemplate.getBodyVersion() < version && !emailTemplate.isBodyEdited()) {
		    emailTemplate.setBody(templateContent);
		    emailTemplate.setBodyVersion(version);
		}
	    }
	    emailTemplateDataService.save(emailTemplate);
	} else {
		logger.error("  else  Template name ="+templateName+"  emailTemplate object is null creating new object = "+emailTemplate + "tenant name = "+TenantContextHolder.getTenant());
	    emailTemplate = new EmailTemplateData();
	    emailTemplate.setBody(templateContent);
	    emailTemplate.setBodyVersion(0);
	    emailTemplate.setCategory(category);
	    emailTemplate.setName(templateName);
	    emailTemplate.setSubject(subject);
	    emailTemplate.setSubjectVersion(0);
	    emailTemplateDataService.save(emailTemplate);
	}

    }

}