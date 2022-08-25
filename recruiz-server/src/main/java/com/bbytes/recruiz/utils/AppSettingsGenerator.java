package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;

public final class AppSettingsGenerator {

	private static final Logger logger = LoggerFactory.getLogger(AppSettingsGenerator.class);

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	public static Date eventDate;

	public static String generateAndSaveSettings(String orgName) throws IOException, ParseException {

		Date todaysDate = getCurrentDateTime();
		Date expiryDate = addDaysToDate(todaysDate, 90);

		String expiresOn = expiryDate.getTime() + "";
		String startOn = todaysDate.getTime() + "";

		Map<String, String> settingsMap = new LinkedHashMap<String, String>();
		settingsMap.put("max.user.count", "-1");
		settingsMap.put("advanced.search", "on");
		settingsMap.put("max.parser.count", "10000");
		settingsMap.put("position.social.sharing", "on");
		settingsMap.put("organization.name", orgName);
		settingsMap.put("vendor.feature", "on");
		settingsMap.put("vendor.count", "-1");
		settingsMap.put("vendor.user.count", "-1");
		settingsMap.put("date.expire.on", expiresOn);
		settingsMap.put("date.start.on", startOn);
		settingsMap.put("max.candidate.count", "-1");
		settingsMap.put("max.department.head.user", "-1");
		settingsMap.put("email.usage.count", "15");

		String mapString = settingsMap.toString();
		String encryptedString = EncryptKeyUtils.getEncryptedKey(mapString);

		// System.out.println("\n\n"+mapString+"\n\n"+encryptedString);

		return encryptedString;
	}

	public static Date addDaysToDate(Date todaysDate, int validDaysDuration) {
		Calendar c = Calendar.getInstance();
		c.setTime(todaysDate);
		c.add(Calendar.DATE, validDaysDuration); // Adding x days
		return c.getTime();
	}

	public static Date getCurrentDateTime() throws UnknownHostException, IOException, ParseException {

		return new Date();

		/*
		 * URL url = new URL("http://www.timeapi.org/utc/now.json"); final
		 * ObjectMapper mapper = new ObjectMapper(); final DateFormat df = new
		 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); mapper.setDateFormat(df);
		 * JsonNode node = mapper.readTree(url); JsonNode date =
		 * node.get("dateString"); return df.parse(date.asText());
		 */

		/*
		 * Date serverDate = new Date(date.longValue() * 1000); String
		 * TIME_SERVER = "time-a.nist.gov"; NTPUDPClient timeClient = new
		 * NTPUDPClient(); InetAddress inetAddress =
		 * InetAddress.getByName(TIME_SERVER); TimeInfo timeInfo =
		 * timeClient.getTime(inetAddress); long returnTime =
		 * timeInfo.getMessage().getTransmitTimeStamp().getTime(); Date time =
		 * new Date(returnTime); return time;
		 */
	}
}
