package com.bbytes.recruiz.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Recruiz temp folder clean up . the temp folder is defined in
 * OSUtils.getBulkUploadTempFolder();
 * 
 * commented for now as it is clearing file that r yet to be processed 
 * @author thanneer
 *
 */
@Component
public class TempFolderCleanUp {

	private static Logger logger = LoggerFactory.getLogger(TempFolderCleanUp.class);

	// @Scheduled(cron = "0 0/1 * 1/1 * ?") // every 1 min
//	@Scheduled(cron = "0 0 1 1/1 * ?")
//	public void cleanupRecruizTempFilesAndFolders() {
//		// if only in prod clean up tmp folder
//		if (SpringProfileService.runningProdMode()) {
//			// a Date defined somewhere for the cutoff date of 30 days
//			Date thresholdDate = DateTime.now().minusDays(30).toDate();
//
//			File recruizTempFolder = new File(OSUtils.getBulkUploadTempFolder());
//			if (recruizTempFolder.exists()) {
//				Collection<File> filesAndFolderToBeDeleted = FileUtils.listFilesAndDirs(recruizTempFolder, new AgeFileFilter(thresholdDate),
//						TRUE);
//				for (File fileOrDir : filesAndFolderToBeDeleted) {
//					try {
//						logger.debug("Deleteing file or folder :-  " + fileOrDir.getPath());
//						// avoid cleaing tomcat folder in tmp folder
//						if (!fileOrDir.getPath().contains("tomcat"))
//							fileOrDir.delete();
//					} catch (Throwable e) {
//						// do nothing
//					}
//
//				}
//			}
//
//		}
//
//	}

}
