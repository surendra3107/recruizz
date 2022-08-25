package com.bbytes.recruiz.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.transaction.TransactionTimedOutException;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import edu.emory.mathcs.backport.java.util.Collections;

public final class StringUtils {

	private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

	private static SecureRandom random = new SecureRandom();

	private static String emailPattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

	private StringUtils() {
	}

	public static String getLocation(String input) {
		String[] strArray = input.split(",");
		if (strArray.length == 0)
			return input;

		return strArray[0].trim();
	}

	public static boolean isValid(String input) {
		return input != null && !input.isEmpty();
	}

	public static String checkNotBlank(String string) {
		Preconditions.checkArgument(string != null && string.trim().length() > 0);
		return string;
	}

	public static String commaSeparate(Collection<String> collectionOfStrings) {
		return Joiner.on(",").join(collectionOfStrings);
	}

	public static List<String> commaSeparateStringToList(String commaSeparateString) {
		return Arrays.asList(commaSeparateString.split("\\s*,\\s*"));
	}

	public static List<String> pipeSeparateStringToList(String commaSeparateString) {
		return Arrays.asList(commaSeparateString.split("\\|"));
	}

	public static List<String> colonSeparateStringToList(String colonSeparateString) {
		return Arrays.asList(colonSeparateString.split(":"));
	}

	public static List<String> commaORSemicolonSeparateStringToList(String str) {
		if (null != str && !str.isEmpty()) {
			List<String> list = new ArrayList<String>(Arrays.asList(Pattern.compile("[,;]+").split(str)));
			return list;
		}
		return new ArrayList<String>();
	}

	public static String getUserInforToEncrypt(String email, String name, String mobile) throws ParseException {
		return email + "," + name + "," + mobile;
	}

	public static String getNameFromEmail(String email) {
		int index = email.indexOf('@');
		return email.substring(0, index);
	}

	public static String convertMapToString(List<Map<String, String>> mapList) {
		List<String> pair = new ArrayList<>();

		if (mapList == null)
			return "";

		for (Map<String, String> map : mapList) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				pair.add(key + ":" + value);
			}
		}

		return org.apache.commons.lang3.StringUtils.join(pair, ',');
	}

	public static List<String> stringToArray(String str) {
		if (null != str && !str.isEmpty()) {
			List<String> list = new ArrayList<String>(Arrays.asList(Pattern.compile(GlobalConstants.SEARCH_KEY_SEPERATOR).split(str)));
			return list;
		}
		return new ArrayList<String>();
	}

	public static String arrayToString(List<String> list) {
		String str = "";
		if (null != list && !list.isEmpty()) {
			str = org.apache.commons.lang3.StringUtils.join(list, GlobalConstants.SEARCH_KEY_SEPERATOR);
		}
		return str;
	}

	public static String compress(String srcTxt) {
		if (srcTxt != null) {
			try {
				ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(rstBao);
				zos.write(srcTxt.getBytes());
				IOUtils.closeQuietly(zos);

				byte[] bytes = rstBao.toByteArray();

				return new String(Base64.encode(bytes));

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return srcTxt;
	}

	public static String uncompress(String zippedBase64Str) {

		if (zippedBase64Str != null) {
			try {
				String result = null;
				byte[] bytes = Base64.decode(zippedBase64Str.getBytes());
				GZIPInputStream zi = null;
				try {
					zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
					result = IOUtils.toString(zi);
				} finally {
					IOUtils.closeQuietly(zi);
				}
				return result;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return zippedBase64Str;
	}

	public static boolean yesNoBoolean(String input) {
		if (input != null && input.equalsIgnoreCase("yes"))
			return true;

		return false;
	}

	public static String[] extractExpYears(String experienceRange) {
		// checking size for old data i.e. 10+ exp
		// clean up old data when we used pattern like 1-3 Years , 1 Year ,
		// 10+ Years etc
		String exp = experienceRange.replaceAll("Years", "");
		exp = exp.replaceAll("Year", "");
		exp = exp.replaceAll("\\+", "-40");
		String[] result = exp.split("-");

		return result;
	}

	/**
	 * Random String generator
	 *
	 * @author Akshay
	 */
	public static String randomString() {
		return new BigInteger(40, random).toString(32);
	}

	/**
	 * Get big Random String generator
	 *
	 * @author Akshay
	 */
	public static String longRandomString() {
		return new BigInteger(200, random).toString(32);
	}

	/**
	 * to clean fileName if it has any special char like %,$ etc by _
	 *
	 * @param fileName
	 * @return
	 */
	public static String cleanFileName(String fileName) {
		String newFileName = "";
		newFileName = fileName.replaceAll("[^a-zA-Z0-9_.\\s+]+", "_");
		return newFileName;
	}

	public static Set<String> cleanSuggestValues(Set<String> input) {
		Set<String> output = new HashSet<String>();

		for (Iterator<String> iterator = input.iterator(); iterator.hasNext();) {
			String value = (String) iterator.next();
			output.add(cleanSuggestValue(value));

		}
		return output;
	}

	/**
	 * Clean up suggest values passed for auto completion
	 *
	 * @param input
	 * @return
	 */
	public static String cleanSuggestValue(String input) {
		input = input.trim();
		input = input.toLowerCase();
		return org.apache.commons.lang3.StringUtils.capitalize(input);
	}

	/**
	 * Clean up space in boolean query passed to elastic search
	 *
	 * @param find
	 * @param replace
	 * @param booleanQuery
	 * @return
	 */
	public static String findReplace(String find, String replace, String booleanQuery) {
		Pattern r = Pattern.compile("\\b" + find + "\\b", Pattern.CASE_INSENSITIVE);
		Matcher matcher = r.matcher(booleanQuery);
		while (matcher.find()) {
			booleanQuery = matcher.replaceAll(replace);
		}
		return booleanQuery;
	}

	public static String cleanFilePath(String filePath) {
		String newFileName = "";
		newFileName = filePath.replaceAll("[^a-zA-Z0-9.-/]", "_");
		newFileName = newFileName.replaceAll(" ", "_");
		return newFileName;
	}

	public static String getNumberFromString(String str) {
		if (str == null || str.isEmpty())
			return "0";
		String numberOnly = str.replaceAll("[^0-9]", "");
		if (numberOnly == null || numberOnly.isEmpty())
			return "0";
		return numberOnly;
	}

	public static Double getDoubleFromString(String str) {
		if (str == null || str.isEmpty())
			return 0D;

		Pattern p = Pattern.compile("(\\d+(?:\\.\\d+))");
		Matcher m = p.matcher(str);
		while (m.find()) {
			Double d = Double.parseDouble(m.group(1));
			return d;
		}

		return 0D;

	}

	public static Integer getIntegerFromString(String str) {
		Double d = getDoubleFromString(str);
		return d.intValue();
	}

	/**
	 * Get number with decimal
	 *
	 * @author Akshay
	 * @param str
	 * @return
	 */
	public static String getDecimalNumberFromString(String str) {
		Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
		Matcher matcher = regex.matcher(str);
		while (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * Check if the skill set parsed is correct
	 *
	 * @param input
	 * @return
	 */
	public static boolean isValidSkill(String input) {
		if (input == null || input.trim().isEmpty() || NumberUtils.isNumber(input) || input.trim().length() > 18 || DateUtil.isDate(input)
				|| inValidSkillSet().contains(input))
			return false;

		return true;
	}

	public static List<String> inValidSkillSet() {

		String[] invalidSkills = { "false", "true", "country", "location", "planned", "rules", "skill", "stage", "best practices",
				"scratch", "smart", "joiners", "check outs", "pages", "ExperienceSection", "SkillSection", "ProjectSection",
				"SummarySection", "OperationalSkill", "Base", "SoftSkill", "BehaviourSkill", "ExperienceSection", "SummarySection",
				"SkillSection", "ProjectSection", "OperationalSkill", "Software Industry", "Effectiveness", "Comprehensive" };

		return new ArrayList<String>(Arrays.asList(invalidSkills));
	}

	// formatting number,
	public static String formatParseNumber(String mobileNo) {
		if (mobileNo != null) {
			mobileNo = mobileNo.replace("-", "");
			mobileNo = mobileNo.replaceAll("\\D", "");
		}
		// planning to remove mobile number widget in frontend so below code not
		// required
		// int length = mobileNo.length();
		// if (length == 10) {
		// return "91" + mobileNo;
		// } else if (length == 11 && mobileNo.charAt(0) == 0) {
		// mobileNo.substring(1, mobileNo.length());
		// return "91" + mobileNo;
		// }
		return mobileNo;
	}

	public static String parseGender(String gender) {
		if (gender == null || gender.isEmpty())
			return "N/A";

		if (gender.startsWith("M") || gender.startsWith("m") || gender.equalsIgnoreCase("m"))
			gender = "Male";

		if (gender.startsWith("F") || gender.startsWith("f") || gender.equalsIgnoreCase("f"))
			gender = "Female";

		return gender;
	}

	public static double parseSalaryString(String salaryString) {

		String[] wordsMatchesToLacs = { "lac(s)", "lac", "lacs", "lakhs", "lakh(s)", "lakh", "l", "lpa", "lakh per annum",
				"lac per annum" };
		String[] wordsMatchesToThousand = { "thousand(s)", "thousands", "thousand" };
		String[] wordsMatchesToMillion = { "million(s)", "millions", "million" };
		for (int i = 0; i <= wordsMatchesToLacs.length - 1; i++) {

			if (salaryString.toLowerCase().indexOf(wordsMatchesToLacs[i].toLowerCase()) != -1) {

				String numberOnly = getDecimalNumberFromString(salaryString);
				if (numberOnly != null && !numberOnly.trim().isEmpty()) {
					String[] sal = numberOnly.split("\\.");
					Double salInDouble = 0D;
					salInDouble = salInDouble + Double.valueOf(sal[0]) * 100000;
					if (sal.length > 1) {
						Double secondVal = Double.valueOf(sal[1]);
						if (secondVal > 9) {
							salInDouble = salInDouble + Double.valueOf(sal[1]) * 1000;
						} else {
							salInDouble = salInDouble + Double.valueOf(sal[1]) * 10000;
						}
					}
					return salInDouble;
				} else
					return 0;
			}
		}
		for (int i = 0; i <= wordsMatchesToThousand.length - 1; i++) {

			if (salaryString.toLowerCase().indexOf(wordsMatchesToThousand[i].toLowerCase()) != -1) {

				String numberOnly = getDecimalNumberFromString(salaryString);
				if (numberOnly != null && !numberOnly.trim().isEmpty()) {
					String[] sal = numberOnly.split("\\.");
					Double salInDouble = 0D;
					salInDouble = salInDouble + Double.valueOf(sal[0]) * 10000;
					if (sal.length > 1) {
						Double secondVal = Double.valueOf(sal[1]);
						if (secondVal > 9) {
							salInDouble = salInDouble + Double.valueOf(sal[1]) * 10;
						} else {
							salInDouble = salInDouble + Double.valueOf(sal[1]) * 1000;
						}
					}
					return salInDouble;
				} else
					return 0;
			}
		}
		for (int i = 0; i <= wordsMatchesToMillion.length - 1; i++) {
			if (salaryString.toLowerCase().indexOf(wordsMatchesToMillion[i].toLowerCase()) != -1) {
				String numberOnly = getDecimalNumberFromString(salaryString);
				if (numberOnly != null && !numberOnly.trim().isEmpty())
					return Double.valueOf(numberOnly);
				else
					return 0;
			}
		}

		if (salaryString != null && !salaryString.trim().isEmpty()) {
			String numberOnly = getDecimalNumberFromString(salaryString);
			if (numberOnly != null && !numberOnly.trim().isEmpty())
				return Double.valueOf(numberOnly);
			else
				return 0;
		}

		return 0;
	}

	public static int parseNoticePeriodString(String noticePeriod) {

		if (null == noticePeriod || noticePeriod.trim().isEmpty()) {
			return 0;
		}

		String[] wordsMatchesToMonths = { "months", "month", "month(s)" };
		String[] wordsMatchesToDays = { "day(s)", "days", "days" };
		String[] wordsMatchesToYears = { "year(s)", "year", "years" };

		for (int i = 0; i <= wordsMatchesToMonths.length - 1; i++) {

			if (noticePeriod.toLowerCase().indexOf(wordsMatchesToMonths[i].toLowerCase()) != -1) {

				String numberOnly = getDecimalNumberFromString(noticePeriod);
				if (numberOnly != null && !numberOnly.trim().isEmpty())
					return 30 * Integer.valueOf(numberOnly);
				else
					return 0;
			}
		}

		for (int i = 0; i <= wordsMatchesToDays.length - 1; i++) {

			if (noticePeriod.toLowerCase().indexOf(wordsMatchesToDays[i].toLowerCase()) != -1) {

				String numberOnly = getDecimalNumberFromString(noticePeriod);
				if (numberOnly != null && !numberOnly.trim().isEmpty())
					return 1 * Integer.valueOf(numberOnly);
				else
					return 0;
			}
		}

		for (int i = 0; i <= wordsMatchesToYears.length - 1; i++) {

			if (noticePeriod.toLowerCase().indexOf(wordsMatchesToYears[i].toLowerCase()) != -1) {

				String numberOnly = getDecimalNumberFromString(noticePeriod);
				if (numberOnly != null && !numberOnly.trim().isEmpty())
					return 365 * Integer.valueOf(numberOnly);
				else
					return 0;
			}
		}

		return 0;
	}

	public static String utf8CleanUp(String input) {

		if (input == null || input.isEmpty())
			return input;

		CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
		utf8Decoder.onMalformedInput(CodingErrorAction.REPLACE);
		utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		utf8Decoder.replaceWith("?");

		ByteBuffer inputData = ByteBuffer.wrap(input.getBytes());

		// UTF-8 decoding
		CharBuffer output = null;
		try {
			output = utf8Decoder.decode(inputData);
		} catch (CharacterCodingException e) {
			logger.error(e.getMessage(), e);
		}

		// Char buffer to string
		if (output != null) {
			String outputString = output.toString();
			return outputString;
		}

		return input;
	}

	public static Map<String, String> convertStringtoMap(String str) {

		String convertedString = str.replace("{", "").replace("}", "");
		Map<String, String> map = Splitter.on(",").withKeyValueSeparator("=").split(convertedString);
		Map<String, String> headerMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			headerMap.put(entry.getKey().trim(), entry.getValue().trim());
		}
		return headerMap;
	}

	public static String getExceptionMessage(Throwable exception, String importType) {

		String message = null;

		if (exception instanceof DataIntegrityViolationException) {

			switch (importType) {
			case GlobalConstants.CLIENTS:
			case GlobalConstants.DEPARTMENTS:
				message = "Duplicate entry with same client name";
				break;
			case GlobalConstants.POSITIONS:
				message = "Duplicate entry with same position code or name";
				break;
			case GlobalConstants.CANDIDATES:
				message = "Duplicate entry with same email address";
				break;
			default:
				message = "Duplicate entry with same identifier";
				break;
			}

			return message;
		} else if (exception instanceof ConstraintViolationException) {
			switch (importType) {
			case GlobalConstants.CLIENTS:
			case GlobalConstants.DEPARTMENTS:
				message = "Duplicate entry with same client name";
				break;
			case GlobalConstants.POSITIONS:
				message = "Duplicate entry with same position code or name";
				break;
			case GlobalConstants.CANDIDATES:
				message = "Duplicate entry with same email address";
				break;
			default:
				message = "Duplicate entry with same identifier";
				break;
			}
			return message;
		} else if (exception instanceof SQLException) {
			message = "Could not save data in db";
			return message;
		} else if (exception instanceof JDBCConnectionException) {
			message = "Error in database connection";
			return message;
		} else if (exception instanceof LockAcquisitionException) {
			message = "Error acquiring a lock in database for request";
			return message;
		} else if (exception instanceof TypeMismatchDataAccessException) {
			message = "Wrong type database column";
			return message;
		} else if (exception instanceof TransactionTimedOutException) {
			message = "Database transaction time out";
			return message;
		} else if (exception instanceof NumberFormatException) {
			message = "Could not resolve number format";
			return message;
		} else if (exception instanceof DataFormatException) {
			message = "Data format mismatch";
			return message;
		} else if (exception instanceof NullPointerException) {
			message = "Some of the field is empty or null";
			return message;
		} else if (exception instanceof FileNotFoundException) {
			message = "Dummy resume file not found";
			return message;
		} else {
			message = "Error occured while saving into database";
			return message;
		}
	}

	public static double getRoundValue(double value) {
		return (double) Math.round(value * 100) / 100;
	}

	public static String getEmailAddress(String textToMatch) {
		// Simple expression to find a valid e-mail address in a file
		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
		textToMatch = textToMatch.trim();
		Set<String> lines = new HashSet<>();
		lines.add(textToMatch);

		lines = splitStringBySpecialChar(textToMatch, lines);

		for (String line : lines) {
			Matcher matcher = pattern.matcher(line.toUpperCase());
			if (matcher.matches()) {
				return line;
			}
		}
		return textToMatch;
	}

	public static boolean containsEmailId(String textToMatch) {
		// Simple expression to find a valid e-mail address in a file
		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
		int matches = 0;

		textToMatch = textToMatch.trim();

		Set<String> lines = new HashSet<>();
		lines.add(textToMatch);

		lines = splitStringBySpecialChar(textToMatch, lines);

		for (String line : lines) {
			line = line.trim();
			while (!line.isEmpty() && !Character.isLetterOrDigit(line.charAt(0))) {
				line = line.substring(1, line.length());
			}
			Matcher matcher = pattern.matcher(line.toUpperCase());
			if (matcher.matches()) {
				matches++;
			}
		}

		// output of summary
		if (matches == 0) {
			return false;
		} else {
			return true;
		}
	}

	// replace all special chars from beginning and end of the string
	public static String replaceSpecialCharFromStartEnd(String sourceString) {
		sourceString = sourceString.trim();

		if (!sourceString.isEmpty() && !Character.isLetterOrDigit(sourceString.charAt(0))) {
			sourceString = sourceString.substring(1, sourceString.length());
		}

		if (!sourceString.isEmpty() && !Character.isLetterOrDigit(sourceString.charAt(sourceString.length() - 1))) {
			sourceString = sourceString.substring(0, sourceString.length() - 1);
		}

		return sourceString;
	}

	public static Set<String> getEmailListFromString(String textToMatch) {
		// Simple expression to find a valid e-mail address in a file
		Set<String> emailsIds = new HashSet<>();
		Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
		textToMatch = textToMatch.trim();
		Set<String> lines = new HashSet<String>();
		lines.add(textToMatch);

		lines = splitStringBySpecialChar(textToMatch, lines);

		for (String line : lines) {
			line = line.trim();

			while (!line.isEmpty() && !Character.isLetterOrDigit(line.charAt(0))) {
				line = line.substring(1, line.length());
			}

			if (containsEmailId(line)) {
				lines = splitStringBySpecialChar(textToMatch, lines);
			}

		}

		for (String line : lines) {
			Matcher matcher = pattern.matcher(line.toUpperCase());
			if (matcher.matches()) {
				emailsIds.add(line);
			}
		}

		return emailsIds;
	}

	/**
	 * to get set of string by spliting using ":"/ , "/" , " "
	 *
	 * @param textToMatch
	 * @param lines
	 * @return
	 */
	private static Set<String> splitStringBySpecialChar(String textToMatch, Set<String> lines) {
		if (textToMatch.contains(" ")) {
			List<String> splitList = new ArrayList<>();
			Collections.addAll(splitList, textToMatch.split(" "));
			lines.addAll(splitList);
		}

		if (textToMatch.contains(":")) {
			List<String> splitList = new ArrayList<>();
			Collections.addAll(splitList, textToMatch.split(":"));
			lines.addAll(splitList);
		}
		if (textToMatch.contains("/")) {
			List<String> splitList = new ArrayList<>();
			Collections.addAll(splitList, textToMatch.split("/"));
			lines.addAll(splitList);
		}

		return lines;
	}

	// to get random digit of 6 digit
	public static int get6RandomDigit() {
		Random r = new Random(System.currentTimeMillis() + System.nanoTime());
		return ((1 + r.nextInt(8)) * 100000 + r.nextInt(100000));
	}

	public static boolean hasEmailsInString(String contentToSearch) {
		Matcher m = Pattern.compile(emailPattern).matcher(contentToSearch);

		while (m.find()) {
			return true;
		}

		return false;
	}

	public static String maskEmailsInString(String contentToSearch) {
		Matcher m = Pattern.compile(emailPattern).matcher(contentToSearch);

		while (m.find()) {
			String originalEmail = m.group();
			String[] emailSplit = originalEmail.split("[@]");

			String replaceString = "**";

			if (emailSplit[0].length() >= 4)
				replaceString = "****";

			if (emailSplit[0].length() >= 8)
				replaceString = "********";

			if (emailSplit[0].length() >= replaceString.length()) {
				String maskedEmailStrip = emailSplit[0].substring(0, emailSplit[0].length() - replaceString.length()) + replaceString;
				String maskedEmail = maskedEmailStrip + "@" + emailSplit[1];

				contentToSearch = contentToSearch.replaceAll(originalEmail, maskedEmail);
			}

		}

		return contentToSearch;
	}

	public static String textToHTML(String textContent) {

		if (textContent == null)
			return textContent;

		StringBuilder builder = new StringBuilder();
		boolean previousWasASpace = false;
		for (char c : textContent.toCharArray()) {
			if (c == ' ') {
				if (previousWasASpace) {
					builder.append("&nbsp;");
					previousWasASpace = false;
					continue;
				}
				previousWasASpace = true;
			} else {
				previousWasASpace = false;
			}
			switch (c) {
			// commented below as asked by UI not to convert these to html
			// entities
			// case '<':
			// builder.append("&lt;");
			// break;
			// case '>':
			// builder.append("&gt;");
			// break;
			case '&':
				builder.append("&amp;");
				break;
			case '"':
				builder.append("&quot;");
				break;
			case '\n':
				builder.append("<br>");
				break;
			// We need Tab support here, because we print StackTraces as HTML
			case '\t':
				builder.append("&nbsp; &nbsp; &nbsp;");
				break;
			default:
				if (c < 128) {
					builder.append(c);
				} else {
					builder.append("&#").append((int) c).append(";");
				}
			}
		}
		return builder.toString();
	}

	public static String getDefaultOrgEmail() {
		String defaulOrgEmail = TenantContextHolder.getTenant() + IntegrationConstants.SIXTH_SENSE_DEFAULT_ORG_DOMAIN;
		return defaulOrgEmail;
	}

	public static double getFormattedExpForCandidate(String totalExp) {
		try {
			String[] exp = totalExp.split("yr");
			int year = Integer.parseInt(exp[0].trim().replaceAll("[\\D]", ""));
			int month = Integer.parseInt(exp[1].trim().replaceAll("[\\D]", ""));
			return Double.parseDouble(year + "." + month);
		} catch (Exception e) {
			return 0D;
		}
	}

	public static double getFormattedCTCForCandidate(String currentCtc) {
		Double doubleCTC = parseSalaryString(currentCtc);
		// double ctc =
		// Double.parseDouble(currentCtc.trim().replaceAll("[\\D.]", ""));
		return doubleCTC;
	}

}
