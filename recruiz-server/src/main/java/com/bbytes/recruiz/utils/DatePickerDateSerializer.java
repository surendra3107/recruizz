package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bbytes.recruiz.auth.jwt.MultiTenantAuthenticationToken;
import com.fasterxml.jackson.core.JsonProcessingException;

import ch.qos.logback.classic.Logger;

public class DatePickerDateSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Date> {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(DatePickerDateSerializer.class);

	@Override
	public void serialize(Date value, com.fasterxml.jackson.core.JsonGenerator gen,
			com.fasterxml.jackson.databind.SerializerProvider serializers) throws IOException, JsonProcessingException {
		if (value != null) {
			MultiTenantAuthenticationToken token = null;
			TimeZone timeZone = null;
			try {
				token = (MultiTenantAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
			} catch (Exception ex) {
				logger.debug("\n\nFailed to get timezone and locale from token in DatePickerDateSerializer class\n\n");
			}
			// doing a null check because when user do not have auth token it
			// will be null and a ClassCastException will be thrown for
			// MultiTenantAuthenticationToken and so the default time zone will
			// be used.
			if (token == null || token.getUserTimeZone() == null) {
				timeZone = TimeZone.getDefault();
			} else {
				timeZone = token.getUserTimeZone().toTimeZone();
			}

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			formatter.setTimeZone(timeZone);
			String format = formatter.format(value);
			gen.writeString(format);
		}
	}
}