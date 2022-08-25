package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class DatePickerDateDeSerializer extends JsonDeserializer<Date> {
	@Override
	public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);
		if (node != null) {
			final String date = node.asText();
			DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			DateTime dateTime = dtf.parseDateTime(date);
			Date formattedDate = dateTime.toDate();
			return formattedDate;
		}
		return DateTime.now().toDate();
	}
}