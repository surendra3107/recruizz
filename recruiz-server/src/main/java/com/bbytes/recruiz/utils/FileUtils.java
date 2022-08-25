package com.bbytes.recruiz.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.FileService;
import com.github.junrar.extract.ExtractArchive;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public final class FileUtils {

	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	@Value("${candidate.folderPath.path}")
	private String folderPath;
	

	public static File writeToFile(String pFilename, byte[] fileContent) throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tempDir, pFilename);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(fileContent);
		fos.close();
		return file;
	}

	public static File writeToFile(String pFilename, InputStream fileStream) throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tempDir, pFilename);
		if (!file.exists()) {
			file.createNewFile();
		}
		Files.copy(fileStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

	public static File writeToFile(String folderName, String pFilename, InputStream fileStream) throws IOException {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tempDir, folderName + "/" + pFilename);
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			file.createNewFile();
		}
		Files.copy(fileStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

	public static File createTempFileCopy(String sourceFilePath) throws IOException {
		File file = new File(sourceFilePath);
		if (!file.exists()) {
			return null;
		}

		String targetPath = System.getProperty("java.io.tmpdir") + File.separator + TenantContextHolder.getTenant() + File.separator
				+ System.currentTimeMillis() + File.separator + file.getName();
		File targetFile = new File(targetPath);

		if (targetFile != null && !targetFile.exists())
			targetFile.mkdirs();

		Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return targetFile;
	}

	/**
	 * Upload attachment to local server
	 * 
	 * @param file
	 * @param fileName
	 * @param path
	 * @return
	 * @throws RecruizException
	 * @throws IOException
	 */
	public static String uploadAttachmentToLocalServer(MultipartFile file, String fileName, String path)
			throws RecruizException, IOException {
		String filePath = "";
		if (file != null && !file.isEmpty()) {
			byte[] bytes = file.getBytes();
			// Create the file on server
			File serverFile = new File(path + File.separator + fileName);
			if (serverFile.exists()) {
				// this will replace the resume file and JD file if it is
				// present (latest requirement)
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				//log.error("Server File Location=" + serverFile.getAbsolutePath());
				filePath = serverFile.getAbsolutePath();

			} else {
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				//log.error("Server File Location=" + serverFile.getAbsolutePath());
				filePath = serverFile.getAbsolutePath();
			}
		} else {
		}
		return filePath;
	}

	public static String readDocFile(File file) throws IOException {

		FileInputStream fis = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			fis = new FileInputStream(file.getAbsolutePath());
			HWPFDocument doc = new HWPFDocument(fis);
			WordExtractor we = new WordExtractor(doc);
			String[] paragraphs = we.getParagraphText();
			for (String para : paragraphs) {
				stringBuilder.append(para.toString());
			}

		} finally {
			if (fis != null)
				fis.close();
		}

		return stringBuilder.toString();

	}

	public static String readDocxFile(File file) throws IOException {

		FileInputStream fis = null;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			fis = new FileInputStream(file.getAbsolutePath());
			XWPFDocument document = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphs = document.getParagraphs();
			for (XWPFParagraph para : paragraphs) {
				stringBuilder.append(para.getText());
			}
		} finally {
			if (fis != null)
				fis.close();
		}

		return stringBuilder.toString();
	}

	/**
	 * Extracts the text from a PDF file.
	 *
	 * @return the document content as plain text
	 */
	public static String readPdfText(File pdfFile) throws IOException {
		PDFTextStripper textStripper = new PDFTextStripper();
		PDDocument document = PDDocument.load(pdfFile);
		String text = textStripper.getText(document);
		document.close();
		return text.trim();
	}

	/**
	 * Extracts the text from a PDF file.
	 *
	 * @return the document content as plain text
	 */
	public static String readFileContent(File file) throws IOException {
		if (FilenameUtils.getExtension(file.getName()).equals("pdf")) {
			return readPdfText(file);
		}

		if (FilenameUtils.getExtension(file.getName()).equals("doc")) {
			return readDocFile(file);
		}

		if (FilenameUtils.getExtension(file.getName()).equals("docx")) {
			return readDocxFile(file);
		}

		byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
		return new String(encoded, Charset.defaultCharset());
	}

	/**
	 * Test if the data in the given byte array represents a PDF file.
	 * 
	 * @throws IOException
	 */
	public static boolean isFilePDF(File file) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(file.getPath()));

		if (data != null && data.length > 4 && data[0] == 0x25 && // %
				data[1] == 0x50 && // P
				data[2] == 0x44 && // D
				data[3] == 0x46 && // F
				data[4] == 0x2D) { // -

			// version 1.3 file terminator
			if (data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x33 && data[data.length - 7] == 0x25 && // %
					data[data.length - 6] == 0x25 && // %
					data[data.length - 5] == 0x45 && // E
					data[data.length - 4] == 0x4F && // O
					data[data.length - 3] == 0x46 && // F
					data[data.length - 2] == 0x20 && // SPACE
					data[data.length - 1] == 0x0A) { // EOL
				return true;
			}

			// version 1.3 file terminator
			if (data[5] == 0x31 && data[6] == 0x2E && data[7] == 0x34 && data[data.length - 6] == 0x25 && // %
					data[data.length - 5] == 0x25 && // %
					data[data.length - 4] == 0x45 && // E
					data[data.length - 3] == 0x4F && // O
					data[data.length - 2] == 0x46 && // F
					data[data.length - 1] == 0x0A) { // EOL
				return true;
			}
		}
		return false;
	}

	/**
	 * This method cleans up any open url action in pdf which would redirect the
	 * open command to a url and the detail candidate page crashes. So we need
	 * to call this method to clean up pdf urls if resume is pdf format
	 * 
	 * @param pdfFile
	 * @throws IOException
	 */
	public static void cleanPDFActionIfany(File pdfFile) throws IOException {
		PDDocument document = PDDocument.load(pdfFile);

		try {
			boolean docModified = false;
			if (document.getDocumentCatalog().getOpenAction() != null) {
				document.getDocumentCatalog().setOpenAction(null);
				docModified = true;
			}

			if (document.getDocumentCatalog().getActions() != null) {
				document.getDocumentCatalog().setOpenAction(null);
				docModified = true;
			}

			if (docModified) {
				document.save(pdfFile);
			}
		} finally {
			if (document != null)
				document.close();
		}

	}

	public static String getTempFilePath() throws Exception {
		// create a temp file
		File temp = File.createTempFile("temp-file-name" + new Date().getTime(), ".txt");

		String absolutePath = temp.getAbsolutePath();
		String tempFilePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
		if (temp.exists())
			temp.delete();

		return tempFilePath;
	}

	public static String getTempDir(String fileName) {
		String path = System.getProperty("java.io.tmpdir") + File.separator + TenantContextHolder.getTenant() + File.separator
				+ System.currentTimeMillis() + File.separator + fileName;
		return path;
	}

	public static String getTempDirForBulkUpload(String fileName) {
		String path = OSUtils.getBulkUploadTempFolder() + File.separator + TenantContextHolder.getTenant() + File.separator
				+ System.currentTimeMillis() + File.separator + fileName;
		return path;
	}

	public static List<File> listFileTree(File dir) {
		List<File> fileTree = new ArrayList<File>();
		if (dir == null || dir.listFiles() == null) {
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile())
				fileTree.add(entry);
			else
				fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

	public static List<File> unZip(File zip, String destination) throws ZipException {
		if (!zip.exists()) {
			throw new RuntimeException("The archive does not exit: " + zip);
		}
		File dest = new File(destination);

		if (!dest.exists()) {
			dest.mkdirs();
		}

		ZipFile zipFile = new ZipFile(zip);
		zipFile.extractAll(destination);
		return listFileTree(dest);
	}

	public static List<File> unRar(File rar, String destination) {

		if (!rar.exists()) {
			throw new RuntimeException("The archive does not exit: " + rar);
		}
		File dest = new File(destination);

		if (!dest.exists()) {
			dest.mkdirs();
		}

		ExtractArchive extractArchive = new ExtractArchive();
		extractArchive.extractArchive(rar, dest);

		return listFileTree(dest);
	}

	public static boolean isZip(File file) {
		if (file == null || file.isDirectory()) {
			return false;
		}

		if (!file.canRead()) {
			return false;
		}

		if (file.isFile()) {
			String fileName = file.getName();
			if (fileName.endsWith(".zip")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRar(File file) {
		if (file == null || file.isDirectory()) {
			return false;
		}

		if (!file.canRead()) {
			return false;
		}

		if (file.isFile()) {
			String fileName = file.getName();
			if (fileName.endsWith(".rar")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isZipOrRar(File file) {
		if (isRar(file))
			return true;

		if (isZip(file))
			return true;

		return false;
	}
}
