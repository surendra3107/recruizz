package com.bbytes.recruiz.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class TestFileService extends RecruizBaseApplicationTests {

	@Autowired
	private ImportExportService importExportService;

	String tenantId = "test_org";

	@Value("${export.folderPath.path}")
	private String rootFolderPath;

	Organization org;

	User testUser;

	@Test
	public void testExportData() throws RecruizException, SQLException, IOException {
		TenantContextHolder.setTenant(tenantId);
		importExportService.exportData(tenantId, true, null);
	}

	@Test
	public void getOldFilesTest() throws IOException {
		File folder = new File(rootFolderPath + File.separator);

		if (folder.exists()) {

			File[] listFiles = folder.listFiles();

			long eligibleForDeletion = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000);

			for (File listFile : listFiles) {

				if (listFile.lastModified() < eligibleForDeletion) {

					if (listFile.isDirectory())
						FileUtils.deleteDirectory(listFile);
					if (!listFile.delete()) {
						System.out.println("Sorry Unable to Delete Files..");

					}
				}
			}
		}
	}

}
