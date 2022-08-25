package com.bbytes.recruiz.utils;

public class OSUtils {

	public static String RECRUIZ_TEMP_FOLDER = "/var/tmp/recruiz_tmp_folder";

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public static String getBulkUploadTempFolder() {
		if (isUnix() || isMac()) {
			return RECRUIZ_TEMP_FOLDER;
		} else {
			return System.getProperty("java.io.tmpdir");
		}
	}

}