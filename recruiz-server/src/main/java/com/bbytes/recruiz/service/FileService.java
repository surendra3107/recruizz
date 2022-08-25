package com.bbytes.recruiz.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.client.S3Client;
import com.bbytes.recruiz.client.S3DownloadClient;
import com.bbytes.recruiz.domain.CandidateFile;
import com.bbytes.recruiz.domain.ClientFile;
import com.bbytes.recruiz.domain.PositionFile;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.repository.PositionFileRepository;
import com.bbytes.recruiz.rest.dto.models.FileDTO;
import com.bbytes.recruiz.utils.OSUtils;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

@Service
public class FileService {

	private static Logger logger = LoggerFactory.getLogger(FileService.class);

	@Autowired
	private FileFormatConversionService fileFormatConversionService;

	@Autowired
	private S3Client s3Client;

	@Autowired
	private S3DownloadClient s3DownloadClient;

	@Autowired
	CandidateFileService candidateFileService;
	
	@Autowired
	PositionFileRepository positionFileRepository;
	
	@Autowired
	ClientFileService clientFileService;

	@Resource
	private Environment environment;

	@Value("${file.public.access.folder.path}")
	private String publicFolder;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${cloud.aws.s3.bucket.bulk.upload.files}")
	private String bulkUploadBucketbucket;

	@Value("${cloud.aws.s3.bucket.tenant.files}")
	private String tenantBucket;

	@Value("${cloud.aws.s3.bucket.failed.files}")
	private String failedFilesBucket;

	@Value("${export.folderPath.path}")
	private String exportBaseFolderPath;

	@Value("${candidate.folderPath.path}")
	private String folderPath;

	public String getTenantBucket() {
		return tenantBucket;
	}

	public String getBulkUploadBucketbucket() {
		return bulkUploadBucketbucket;
	}

	public String getFailedFilesBucket() {
		return failedFilesBucket;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Used only by bulk upload to control the number of request sent to
	 * conversion service
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws RecruizException
	 */
	public String queueFileConvert(String filePath) throws IOException, RecruizException {
		return fileFormatConversionService.queueFileConvert(filePath);
	}

	public String convert(String filePath) throws IOException {
		return convert(filePath, "pdf");
	}

	public String convertToHTML(String filePath) throws IOException {
		return convert(filePath, "html");
	}

	public String convert(String filePath, String targetFileExtension) throws IOException {
		// file to convert does not exist then return null
		if (!new File(filePath).exists()) {
			return null;
		}

		// sending pdf files for cleanup process
		if (FilenameUtils.getExtension(filePath).equalsIgnoreCase("pdf")) {
			com.bbytes.recruiz.utils.FileUtils.cleanPDFActionIfany(new File(filePath));
		}

		if (FilenameUtils.getExtension(filePath).equalsIgnoreCase(targetFileExtension)) {
			return filePath;
		}
		try {
			File inputFile = new File(filePath);
			File outputFile = new File(FilenameUtils.removeExtension(filePath) + "." + targetFileExtension);
			fileFormatConversionService.convert(inputFile, outputFile);
		} catch (Throwable ex) {
			logger.error("\t\t\t File conversion failed for input file : "+ filePath);
			logger.error(ex.getMessage(), ex);
			return filePath;
		}
		return FilenameUtils.removeExtension(filePath) + "." + targetFileExtension;
	}

	public boolean deleteFile(String filePath) {
		if (filePath.startsWith("http")) {
			try {
				URI uri = new URI(filePath);
				String s3RelativeFilePath = uri.getPath();
				if (s3RelativeFilePath.startsWith("/")) {
					s3RelativeFilePath = s3RelativeFilePath.substring(1, s3RelativeFilePath.length());
				}
				s3DownloadClient.deleteS3File(tenantBucket, s3RelativeFilePath);
			} catch (URISyntaxException e) {
				logger.warn("Failed to delete s3 File", e);
				return false;
			}
			return true;
		} else {
			File file = new File(filePath);
			return file.delete();
		}
	}

	/**
	 * Method used to delete folder for a tenant
	 * 
	 * @author Akshay
	 * @param filePath
	 */
	public void deleteDirectory(String filePath) {
		File directory = new File(filePath);
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public String copyFileToRecruizExportFolder(File srcFile) {
		if (srcFile == null)
			return "";

		try {
			String destPath = exportBaseFolderPath + File.separator + TenantContextHolder.getTenant() + File.separator
					+ System.currentTimeMillis() + "_" + srcFile.getName();
			File destFile = new File(destPath);
			FileUtils.copyFile(srcFile, destFile);
			return destFile.getPath();

		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}

		return srcFile.getPath();
	}

	public String copyFileToRecruizTempForBulkUpload(File file) {
		if (file == null)
			return "";

		try {
			String newPath = OSUtils.getBulkUploadTempFolder() + File.separator + TenantContextHolder.getTenant()
			+ File.separator + System.currentTimeMillis() + file.getName();
			File targetFile = new File(newPath);
			FileUtils.copyFile(file, targetFile);
			return targetFile.getPath();

		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}

		return file.getPath();
	}

	// this folder will have files which failed to process during bulk upload
	// item
	public String copyFileToFailedTempForBulkUpload(File file) {
		if (file == null)
			return "";

		try {
			String failedDirPath = OSUtils.getBulkUploadTempFolder() + File.separator + TenantContextHolder.getTenant()
			+ File.separator + "failedFiles" + File.separator;

			File dir = new File(failedDirPath);
			dir.mkdirs();

			String newPath = failedDirPath + System.currentTimeMillis() + file.getName();

			File targetFile = new File(newPath);
			FileUtils.copyFile(file, targetFile);
			return targetFile.getPath();

		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}

		return file.getPath();
	}

	public File multipartToFile(MultipartFile multipartFile)
			throws IllegalStateException, IOException, RecruizWarnException {
		String tempFolderPath = System.getProperty("java.io.tmpdir");
		return multipartToFile(multipartFile, tempFolderPath);
	}

	public File multipartToFileForBulkUpload(MultipartFile multipartFile)
			throws IllegalStateException, IOException, RecruizWarnException {
		String tempFolderPath = OSUtils.getBulkUploadTempFolder();
		return multipartToFile(multipartFile, tempFolderPath);
	}

	public File multipartToFile(MultipartFile multipartFile, String tempFolderPath)
			throws IllegalStateException, IOException, RecruizWarnException {
		String targetPath = tempFolderPath + File.separator + TenantContextHolder.getTenant() + File.separator
				+ System.currentTimeMillis();

		File file = new File(targetPath);
		if (!file.exists()) {
			file.mkdirs();
		}

		targetPath = targetPath + File.separator + StringUtils.cleanFileName(multipartFile.getOriginalFilename());
		File targetFile = new File(targetPath);

		InputStream source = multipartFile.getInputStream();
		OutputStream output = new FileOutputStream(targetFile);

		try {
			// Copy the contents of the given InputStream to the given
			// OutputStream. Leaves both streams open when done os we need to
			// close it in finally block
			StreamUtils.copy(source, output);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (source != null)
				source.close();

			if (output != null)
				output.close();

		}

		return targetFile;
	}

	/**
	 * To zip the single file, folder or complete directory tree
	 * 
	 * @author Akshay
	 * @param inputFolder
	 * @param targetZippedFolder
	 * @throws IOException
	 */
	public void zip(String inputFolder, String targetZippedFolder) throws IOException {

		FileOutputStream fileOutputStream = null;

		// wrapping a FileOutputStream around a ZipOutputStream to store the zip
		// stream to a file
		fileOutputStream = new FileOutputStream(targetZippedFolder);
		ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

		File inputFile = new File(inputFolder);

		if (inputFile.isFile())
			zipFile(inputFile, "", zipOutputStream);
		else if (inputFile.isDirectory())
			zipFolder(zipOutputStream, inputFile, "");
		// close ZipEntry to store the stream to the file
		zipOutputStream.close();
	}

	/**
	 * This is used to zip the files from folder, parentName argument - That is
	 * basically to easily calculate the absolute path of each file in order to
	 * place it in the correct folder in the archive
	 * 
	 * @author Akshay
	 * @param zipOutputStream
	 * @param inputFolder
	 * @param parentName
	 * @throws IOException
	 */
	public void zipFolder(ZipOutputStream zipOutputStream, File inputFolder, String parentName) throws IOException {
		String zipFileName = parentName + inputFolder.getName() + File.separator;
		ZipEntry folderZipEntry = new ZipEntry(zipFileName);
		// A ZipEntry represents a file entry in the zip archive
		zipOutputStream.putNextEntry(folderZipEntry);
		File[] fileList = inputFolder.listFiles();
		// iterating all files from input folder
		for (File file : fileList) {
			if (file.isFile())
				zipFile(file, zipFileName, zipOutputStream);
			else if (file.isDirectory())
				zipFolder(zipOutputStream, file, zipFileName);
		}
		// close ZipEntry to store the stream to the file
		zipOutputStream.closeEntry();
	}

	/**
	 * Writing each files into zip outstream and creating zip file
	 * 
	 * @author Akshay
	 * @param inputFile
	 * @param parentName
	 * @param zipOutputStream
	 * @throws IOException
	 */
	public void zipFile(File inputFile, String parentName, ZipOutputStream zipOutputStream) throws IOException {

		FileInputStream fileInputStream = null;
		try {
			// A ZipEntry represents a file entry in the zip archive
			// We name the ZipEntry after the original file's name
			ZipEntry zipEntry = new ZipEntry(parentName + inputFile.getName());
			zipOutputStream.putNextEntry(zipEntry);

			fileInputStream = new FileInputStream(inputFile);
			byte[] byteArray = new byte[1024];
			int bytesRead;

			// Read the input file by chucks of 1024 bytes
			// and write the read bytes to the zip stream
			while ((bytesRead = fileInputStream.read(byteArray)) > 0) {
				zipOutputStream.write(byteArray, 0, bytesRead);
			}
		} catch (FileNotFoundException ex) {
			logger.error(ex.getMessage());
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			// close ZipEntry to store the stream to the file and file input
			// stream
			zipOutputStream.closeEntry();
			fileInputStream.close();
		}
	}

	/**
	 * To upload files to pubset and return file pubset path
	 * 
	 * @param fileDTO
	 * @return
	 * @throws IOException
	 */
	public String copyToPubset(FileDTO fileDTO, MultipartFile file) throws IOException {

		if (file != null && !file.isEmpty() && fileDTO.getFileName() != null && !fileDTO.getFileName().isEmpty()
				&& fileDTO.getFileType() != null && !fileDTO.getFileType().isEmpty()) {
			byte[] fileBytes = file.getBytes();
			File publicProfilePath = new File(
					publicFolder + "/" + TenantContextHolder.getTenant() + "/" + fileDTO.getFileType());
			try {
				if (!publicProfilePath.exists())
					org.apache.commons.io.FileUtils.forceMkdir(publicProfilePath);
				File uploadFile = new File(publicProfilePath + "/" + fileDTO.getFileName());
				org.apache.commons.io.FileUtils.writeByteArrayToFile(uploadFile, fileBytes);
				String filePath = uploadFile.getAbsolutePath().replace(publicFolder, "");
				String url = baseUrl + "/pubset/" + filePath;
				return url;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * to delete files from pubset folder
	 * 
	 * @param filePath
	 */
	public void deletedPubsetFile(String url) {
		String filePath = url.substring(url.indexOf("pubset"), url.length());
		filePath = filePath.replace("pubset", "");
		filePath = publicFolder + filePath;
		File fileToDelete = new File(filePath);
		if (fileToDelete.exists())
			fileToDelete.delete();
	}

	/**
	 * Add signature to file
	 */
	public String addSignatureToFile(String signature, File image, File document) {
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = document;
		try {
			PDFParser parser = new PDFParser(new RandomAccessFile(file, "rws"));

			parser.parse();

			cosDoc = parser.getDocument();

			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);

			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(pdDoc.getNumberOfPages());

			String parsedText = pdfStripper.getText(pdDoc);

			addNewPage(pdDoc, pdDoc.getPage(0));
			return parsedText;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void addNewPage(PDDocument document, PDPage page) throws IOException {
		// PDDocument document = new PDDocument();
		// PDPage page = new PDPage();
		// document.addPage( page );

		// Create a new font object selecting one of the PDF base fonts
		PDFont font = PDType1Font.TIMES_ROMAN;

		// Start a new content stream which will "hold" the to be created
		// content
		PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, false, false);

		// the dimension of a document is 595 x 841 in points and the point
		// starts from left bottom of the page
		contentStream.beginText();
		contentStream.setFont(font, 10);
		contentStream.newLineAtOffset(10, 820);
		contentStream.showText("This document is confidential");
		contentStream.endText();

		PDImageXObject imageObj = PDImageXObject.createFromFile("/home/sourav-bb/Pictures/bb_logo.png", document);
		imageObj.setStencil(false);
		contentStream.drawImage(imageObj, 250, 400, 100, 100);

		// Make sure that the content stream is closed:
		contentStream.close();

		// Save the results and ensure that the document is properly closed:

		// replaceText(document, "Beyond", "54******70");
		document.save("sample6.pdf");
		document.close();
	}

	public PDDocument replaceText(PDDocument document, String searchString, String replacement) throws IOException {
		if (searchString.isEmpty() || replacement.isEmpty()) {
			return document;
		}
		PDPageTree pages = document.getDocumentCatalog().getPages();
		for (PDPage page : pages) {

			PDFStreamParser parser = new PDFStreamParser(page);
			parser.parse();

			List tokens = parser.getTokens();
			for (int j = 0; j < tokens.size(); j++) {
				Object next = tokens.get(j);
				if (next instanceof Operator) {
					Operator op = (Operator) next;
					// Tj and TJ are the two operators that display strings in a
					// PDF
					if (op.getName().equals("Tj")) {
						// Tj takes one operator and that is the string to
						// display so lets update that operator

						// //System.out.println(op.getName() + " -> " +
						// op.toString());

						COSString previous = (COSString) tokens.get(j - 1);
						String string = previous.getString();
						string = string.replaceFirst(searchString, replacement);
						previous.setValue(string.getBytes());

						// 08042068178
					} else if (op.getName().equals("TJ")) {

						// System.out.println(op.getName() + " -> " +
						// op.toString());

						COSArray previous = (COSArray) tokens.get(j - 1);
						for (int k = 0; k < previous.size(); k++) {
							Object arrElement = previous.getObject(k);
							if (arrElement instanceof COSString) {
								COSString cosString = (COSString) arrElement;
								String string = cosString.getString();
								// System.out.println("ASCII STRING -> " +
								// cosString.getASCII());
								string = string.replaceAll(searchString, replacement);
								cosString.setValue(string.getBytes());
							}
						}
					}
				}
			}
			// now that the tokens are updated we will replace the page content
			// stream.
			PDStream updatedStream = new PDStream(document);
			OutputStream out = updatedStream.createOutputStream();
			ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
			tokenWriter.writeTokens(tokens);
			page.setContents(updatedStream);
			out.close();
		}
		return document;
	}

	/**
	 * To add header in pdf
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void addHeaderInPdf(String src, String dest, String headerText) throws IOException, DocumentException {
		// deleting the destination file if it exists
		File destFile = new File(dest);
		if (destFile.exists()) {
			destFile.delete();
		}

		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED); // set
		Phrase header = new Phrase(headerText, new Font(bf));
		float x, y;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			// System.out.println(reader.getPageRotation(i));
			if (reader.getPageRotation(i) % 180 == 0) {
				x = reader.getPageSize(i).getWidth() / 2;
				y = reader.getPageSize(i).getTop(20);
			} else {
				// System.out.println("rotated");
				x = reader.getPageSize(i).getHeight() / 2;
				y = reader.getPageSize(i).getRight(20);
			}
			ColumnText.showTextAligned(stamper.getOverContent(i), Element.ALIGN_CENTER, header, x, y, 0);
		}
		stamper.close();
		reader.close();
	}

	/**
	 * To add a image water mark in pdf document
	 * 
	 * @param src
	 * @param dest
	 * @param imagePath
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URISyntaxException
	 */
	public void imageWatermarkPDF(String src, String dest, String imagePath, String orgName)
			throws IOException, DocumentException {

		// deleting the destination file if it exists
		File destFile = new File(dest);
		if (destFile.exists()) {
			destFile.delete();
		}

		PdfReader reader = new PdfReader(src);
		int n = reader.getNumberOfPages();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		stamper.setRotateContents(false);
		// text watermark
		BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED); // set
		Font f = new Font(bf);
		Phrase p = new Phrase(orgName, f);

		// image watermark
		Image img = null;
		float w = 0, h = 0;
		if (imagePath != null && !imagePath.isEmpty()) {
			img = Image.getInstance(imagePath);
			w = img.getScaledWidth();
			h = img.getScaledHeight();
		}

		// transparency
		PdfGState gs1 = new PdfGState();
		gs1.setFillOpacity(0.5f);
		// properties
		PdfContentByte over;
		Rectangle pagesize;
		float x, y;
		// loop over every page
		for (int i = 1; i <= n; i++) {
			pagesize = reader.getPageSize(i);
			x = (pagesize.getLeft() + pagesize.getRight()) / 2;
			y = (pagesize.getTop() + pagesize.getBottom()) / 2;
			over = stamper.getOverContent(i);
			over.saveState();
			over.setGState(gs1);
			if (img != null) {
				over.addImage(img, w, 0, 0, h, x - (w / 2), y - (h / 2));
			} else {
				ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, x, y, 0);
			}

			over.restoreState();
		}
		stamper.close();
		reader.close();
	}

	/**
	 * To find a replace a text in doc file (docx format)
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public void replaceDocxWordFile(String inputFile, String outputFile, Map<String, String> textToReplace,
			String headerText) throws InvalidFormatException, IOException {

		XWPFDocument doc = new XWPFDocument(OPCPackage.open(inputFile));

		// looking for text in paragraph of doc
		for (XWPFParagraph p : doc.getParagraphs()) {
			replaceInParagraphs(textToReplace, p);
		}

		// looking for text in doc tables if any
		for (XWPFTable tbl : doc.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						replaceInParagraphs(textToReplace, p);
					}
				}
			}
		}

		// looking for text in header if any
		for (XWPFHeader header : doc.getHeaderList()) {
			for (XWPFParagraph p : header.getParagraphs()) {
				replaceInParagraphs(textToReplace, p);
			}
		}

		// creating a destination file if it exists then first delete it then
		// create it
		File existingFile = new File(outputFile);
		if (existingFile.exists()) {
			existingFile.delete();
		}

		/****************** setting header here ************************/
		// create header-footer
		XWPFHeaderFooterPolicy headerFooterPolicy = doc.getHeaderFooterPolicy();
		if (headerFooterPolicy == null)
			headerFooterPolicy = doc.createHeaderFooterPolicy();

		// create header start
		XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

		XWPFParagraph paragraph = header.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);

		XWPFRun run = paragraph.createRun();
		run.setText(headerText);

		// create footer start
		XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

		paragraph = footer.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);

		run = paragraph.createRun();
		run.setText(headerText);

		CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();

		CTPageMar pageMar = sectPr.getPgMar();

		if (pageMar == null)
			pageMar = sectPr.addNewPgMar();
		pageMar.setLeft(BigInteger.valueOf(720)); // 720 TWentieths of an Inch
		// Point (Twips) = 720/20 = 36
		// pt = 36/72 = 0.5"
		pageMar.setRight(BigInteger.valueOf(720));
		pageMar.setTop(BigInteger.valueOf(1440)); // 1440 Twips = 1440/20 = 72
		// pt = 72/72 = 1"
		pageMar.setBottom(BigInteger.valueOf(1440));

		pageMar.setHeader(BigInteger.valueOf(908)); // 45.4 pt * 20 = 908 = 45.4
		// pt header from top
		pageMar.setFooter(BigInteger.valueOf(568)); // 28.4 pt * 20 = 568 = 28.4
		// pt footer from bottom

		doc.write(new FileOutputStream(outputFile));
	}

	private String getStarsString(int length) {
		String starString = "";
		for (int i = 0; i < length; i++) {
			starString = starString + "*";
		}
		return starString;
	}

	private void replace(XWPFParagraph paragraph, String searchValue, String replacement) {
		if (hasReplaceableItem(paragraph.getText(), searchValue)) {
			String replacedText = org.apache.commons.lang.StringUtils.replace(paragraph.getText(), searchValue,
					replacement);

			removeAllRuns(paragraph);

			insertReplacementRuns(paragraph, replacedText);
		}
	}

	private void insertReplacementRuns(XWPFParagraph paragraph, String replacedText) {
		String[] replacementTextSplitOnCarriageReturn = org.apache.commons.lang.StringUtils.split(replacedText, "\n");

		for (int j = 0; j < replacementTextSplitOnCarriageReturn.length; j++) {
			String part = replacementTextSplitOnCarriageReturn[j];

			XWPFRun newRun = paragraph.insertNewRun(j);
			newRun.setText(part);

			if (j + 1 < replacementTextSplitOnCarriageReturn.length) {
				newRun.addCarriageReturn();
			}
		}
	}

	private void removeAllRuns(XWPFParagraph paragraph) {
		int size = paragraph.getRuns().size();
		for (int i = 0; i < size; i++) {
			paragraph.removeRun(0);
		}
	}

	private boolean hasReplaceableItem(String runText, String searchValue) {
		return org.apache.commons.lang.StringUtils.contains(runText, searchValue);
	}

	private long replaceInParagraphs(Map<String, String> replacements, XWPFParagraph p) {

		int numberOfRuns = p.getRuns().size();

		// Collate text of all runs
		StringBuilder sb = new StringBuilder();
		for (XWPFRun r : p.getRuns()) {
			int pos = r.getTextPosition();
			if (r.getText(pos) != null && !r.getText(pos).trim().isEmpty()) {

				// System.out.println("# pos text : " +r.getText(pos));

				for (Map.Entry<String, String> replaceableStrings : replacements.entrySet()) {
					// System.out.println("# pos text : " + r.getText(pos) + "
					// with -> " + replaceableStrings.getKey());
					if (r.getText(pos).toUpperCase().contains(replaceableStrings.getKey().toUpperCase())) {
						String replaced = r.getText(pos).replaceAll("(?i)" + replaceableStrings.getKey(),
								replaceableStrings.getValue());
						r.setText(replaced, 0);
					}

					// added by thanner: Replace any mail pattern with ****
					// partially
					// String maskedEmailText =
					// StringUtils.maskEmailsInString(r.getText(pos));
					// r.setText(maskedEmailText, 0);
				}
				sb.append(r.getText(pos));
			}
		}

		return 0;
	}

	/**
	 * To Replace Email id in doc file
	 * 
	 * @param sourcePath
	 * @param destinationFile
	 * @param emailToReplace
	 * @throws IOException
	 */
	public void maskEmailInDoc(String sourcePath, String destinationFile, String emailToReplace) throws IOException {

		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(sourcePath));
		HWPFDocument doc = new HWPFDocument(fs);

		Range r1 = doc.getRange();

		// Map<String, String> extraTextToReplace = new HashMap<>();

		for (int i = 0; i < r1.numSections(); ++i) {
			Section s = r1.getSection(i);
			for (int x = 0; x < s.numParagraphs(); x++) {
				Paragraph p = s.getParagraph(x);
				for (int z = 0; z < p.numCharacterRuns(); z++) {
					CharacterRun run = p.getCharacterRun(z);
					String text = run.text();

					// added by thanner: Replace any mail pattern with ****
					// partially
					// String maskedEmailText =
					// StringUtils.maskEmailsInString(text);
					// doc.getRange().replaceText(text, maskedEmailText);

					// old logic added by sourav commneted below
					if (StringUtils.containsEmailId(text)) {
						// System.out.println("Email id String : " + text);
						Set<String> emailsIdsFromString = StringUtils.getEmailListFromString(text);
						if (emailsIdsFromString != null && !emailsIdsFromString.isEmpty()) {
							for (String emailsInFile : emailsIdsFromString) {
								// System.out.println("Email after break : " +
								// emailsInFile);
								if (emailToReplace.equalsIgnoreCase(emailsInFile)) {
									doc.getRange().replaceText(emailsInFile, getStarsString(emailsInFile.length()));
								}
							}
						}
					}

				}
			}
		}

		FileOutputStream out = null;
		try {
			// creating a destination file if it exists then first delete it
			// then create it
			File existingFile = new File(destinationFile);
			if (existingFile.exists()) {
				existingFile.delete();
			}

			out = new FileOutputStream(destinationFile);
			doc.write(out);
		} finally {
			out.close();
		}
	}

	/**
	 * To replace name in doc file
	 * 
	 * @param sourcePath
	 * @param destinationFile
	 * @param nameToReplace
	 * @throws IOException
	 */
	public void maskNameInDoc(String sourcePath, String destinationFile, String nameToReplace) throws IOException {

		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(sourcePath));
		HWPFDocument doc = new HWPFDocument(fs);

		Range r1 = doc.getRange();

		Map<String, String> extraTextToReplace = new HashMap<>();

		for (int i = 0; i < r1.numSections(); ++i) {
			Section s = r1.getSection(i);
			for (int x = 0; x < s.numParagraphs(); x++) {
				Paragraph p = s.getParagraph(x);
				for (int z = 0; z < p.numCharacterRuns(); z++) {
					CharacterRun run = p.getCharacterRun(z);
					String text = run.text();
					String textToReplace = text;
					text = text.trim();
					while (!text.isEmpty() && !Character.isLetterOrDigit(text.charAt(0))) {
						text = text.substring(1, text.length());
					}

					while (!text.isEmpty() && !Character.isLetterOrDigit(text.charAt(text.length() - 1))) {
						text = text.substring(0, text.length() - 1);
					}

					// System.out.println(text + " -> " + nameToReplace);
					if (!text.isEmpty() && text.toUpperCase().equalsIgnoreCase(nameToReplace.toUpperCase())) {
						if (textToReplace.endsWith("\r")) {
							doc.getRange().replaceText(textToReplace, getStarsString(nameToReplace.length()) + "\r");
						} else {
							doc.getRange().replaceText(textToReplace, getStarsString(nameToReplace.length()));
						}
					} else if (!text.isEmpty() && text.toUpperCase().contains(nameToReplace.toUpperCase())) {
						doc.getRange().replaceText(nameToReplace, getStarsString(nameToReplace.length()));
					}
				}
			}
		}

		FileOutputStream out = null;
		try {
			// creating a destination file if it exists then first delete it
			// then create it
			File existingFile = new File(destinationFile);
			if (existingFile.exists()) {
				existingFile.delete();
			}

			out = new FileOutputStream(destinationFile);
			doc.write(out);
		} finally {
			out.close();
		}
	}

	/**
	 * mask mobile number in doc file
	 * 
	 * @param sourcePath
	 * @param destinationFile
	 * @param nameToReplace
	 * @throws IOException
	 */
	public void maskMobileInDoc(String sourcePath, String destinationFile, String mobileToReplace) throws IOException {

		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(sourcePath));
			HWPFDocument doc = new HWPFDocument(fs);

			Range r1 = doc.getRange();

			for (int i = 0; i < r1.numSections(); ++i) {
				// System.out.println("masking mobile 1");
				Section s = r1.getSection(i);
				for (int x = 0; x < s.numParagraphs(); x++) {
					// System.out.println("masking mobile 2");
					Paragraph p = s.getParagraph(x);
					for (int z = 0; z < p.numCharacterRuns(); z++) {
						// System.out.println("masking mobile 3");
						CharacterRun run = p.getCharacterRun(z);
						String text = run.text();
						String textToReplace = text;
						text = text.trim();
						while (!text.isEmpty() && !Character.isLetterOrDigit(text.charAt(0))) {
							// System.out.println("\t\tmasking mobile 4");
							text = text.substring(1, text.length());
						}

						while (!text.isEmpty() && !Character.isLetterOrDigit(text.charAt(text.length() - 1))) {
							// System.out.println("\t\tmasking mobile 5 " +
							// text);
							text = text.substring(0, text.length() - 1);

							// System.out.println("\t\t\t substring text : " +
							// text);

						}

						// System.out.println(text + " -> " + mobileToReplace);
						if (!text.isEmpty() && text.endsWith(mobileToReplace)) {
							// System.out.println("\t\t text ends with mobile to
							// replace");
							if (textToReplace.endsWith("\r")) {
								doc.getRange().replaceText(textToReplace,
										textToReplace.substring(0, textToReplace.length() - mobileToReplace.length())
										+ getStarsString(mobileToReplace.length()) + "\r");
							} else {
								doc.getRange().replaceText(textToReplace,
										textToReplace.substring(0, textToReplace.length() - mobileToReplace.length())
										+ getStarsString(mobileToReplace.length()));
							}

						} else if (!text.isEmpty() && text.contains(mobileToReplace)) {
							// System.out.println("\t\t text contains with
							// mobile to replace");
							if (textToReplace.endsWith("\r")) {
								String toReplace = textToReplace.replace(mobileToReplace,
										getStarsString(mobileToReplace.length()));
								doc.getRange().replaceText(textToReplace, toReplace);
							}
						}
					}
					// System.out.println("\t\t\t masking mobile 6 :");
				}
			}

			// System.out.println("\t\t\tmasking mobile done 6");

			FileOutputStream out = null;
			try {
				// creating a destination file if it exists then first delete it
				// then create it
				File existingFile = new File(destinationFile);
				if (existingFile.exists()) {
					existingFile.delete();
				}
				out = new FileOutputStream(destinationFile);
				doc.write(out);
			} catch (Throwable th) {
				logger.error(th.getMessage(), th);
			} finally {
				out.close();
			}
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	/**
	 * To delete a file in a new threads
	 * 
	 * @param resumeFile
	 */
	public void deleteFileInNewThread(File resumeFile) {
		Thread fileDeleteThread = new Thread() {
			public void run() {
				try {
					Files.delete(resumeFile.toPath());
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		};
		fileDeleteThread.start();
	}

	public void addHeaderInDocx(XWPFDocument doc, String headerText) throws IOException {

		// XWPFDocument doc= new XWPFDocument();
		XWPFHeaderFooterPolicy policy = doc.getHeaderFooterPolicy();
		// in an empty document always will be null
		if (policy == null) {
			CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
			policy = new XWPFHeaderFooterPolicy(doc, sectPr);
		}

		if (policy.getDefaultHeader() == null && policy.getFirstPageHeader() == null
				&& policy.getDefaultFooter() == null) {
			XWPFHeader headerD = policy.createHeader(policy.DEFAULT);
			headerD.getParagraphs().get(0).createRun().setText(headerText);

		}
		FileOutputStream out = new FileOutputStream(System.currentTimeMillis() + "_test1_header.docx");
		doc.write(out);
		out.close();
		doc.close();
	}

	public void deleteLocalFileDuringAWS() {

		//for candidate files
		try{
			List<CandidateFile> candidateFiles = candidateFileService.getCandidateFileByStorageModeAWS();

			for (CandidateFile candidateFile : candidateFiles) {
				try{
					if (null != candidateFile) {
						String filePath = candidateFile.getFilePath();
						filePath = folderPath+"/files"+filePath.split("/files")[1];

						File diskFile = new File(filePath);

						if (diskFile.exists()) {
							diskFile.delete();
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				candidateFile.setStorageMode("deleted");
				candidateFileService.save(candidateFile);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		//for position files
		try{
			List<PositionFile> positionFiles = positionFileRepository.getPositionFileByStorageModeAWS();

			for (PositionFile positionFile : positionFiles) {
				try{
					if (null != positionFile) {
						String filePath = positionFile.getFilePath();
						filePath = folderPath+"/files"+filePath.split("/files")[1];

						File diskFile = new File(filePath);

						if (diskFile.exists()) {
							diskFile.delete();
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				positionFile.setStorageMode("deleted");
				positionFileRepository.save(positionFile);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		//for client files
				try{
					List<ClientFile> clientFiles = clientFileService.getClientFileByStorageModeAWS();

					for (ClientFile clientFile : clientFiles) {
						try{
							if (null != clientFile) {
								String filePath = clientFile.getFilePath();
								filePath = folderPath+"/files"+filePath.split("/files")[1];

								File diskFile = new File(filePath);

								if (diskFile.exists()) {
									diskFile.delete();
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
						clientFile.setStorageMode("deleted");
						clientFileService.save(clientFile);
					}

				}catch(Exception e){
					e.printStackTrace();
				}
		
	}



}
