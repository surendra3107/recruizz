package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TimePickerDateSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Date> {

	@Override
	public void serialize(Date value, com.fasterxml.jackson.core.JsonGenerator gen,
			com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException, JsonProcessingException {

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String format = formatter.format(value);
		gen.writeString(format);
	}

}